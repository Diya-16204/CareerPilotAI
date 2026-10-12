package com.careerpilot.autoapply;

import com.careerpilot.ai.AiProvider;
import com.careerpilot.ai.CoverLetterDtos;
import com.careerpilot.ai.CoverLetterService;
import com.careerpilot.auth.UserAccount;
import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.resume.Resume;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeStorage;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import com.careerpilot.user.UserProfile;
import com.careerpilot.user.UserProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.careerpilot.autoapply.ApplicationPrepDtos.*;

/** Bounded preparation agent. It can only call the registered, owner-scoped application tools below. */
@Component
public class AutoApplyPreparationAgent {
    static final int MAX_TOOL_CALLS = 28;
    private static final TypeReference<List<AnswerResponse>> ANSWER_LIST = new TypeReference<>() {};
    private final JobRepository jobs;
    private final ResumeVersionRepository versions;
    private final ResumeRepository resumes;
    private final UserAccountRepository users;
    private final UserProfileRepository profiles;
    private final ConfirmedApplicationAnswerRepository confirmedAnswers;
    private final CoverLetterService coverLetters;
    private final AiProvider ai;
    private final ResumeStorage storage;
    private final ObjectMapper mapper;

    public AutoApplyPreparationAgent(JobRepository jobs, ResumeVersionRepository versions, ResumeRepository resumes,
                                    UserAccountRepository users, UserProfileRepository profiles,
                                    ConfirmedApplicationAnswerRepository confirmedAnswers,
                                    CoverLetterService coverLetters, AiProvider ai, ResumeStorage storage,
                                    ObjectMapper mapper) {
        this.jobs = jobs; this.versions = versions; this.resumes = resumes; this.users = users;
        this.profiles = profiles; this.confirmedAnswers = confirmedAnswers; this.coverLetters = coverLetters;
        this.ai = ai; this.storage = storage; this.mapper = mapper;
    }

    public Prepared prepare(UUID owner, UUID jobId, UUID resumeVersionId, String tone,
                            List<QuestionRequest> questions) throws Exception {
        ToolBudget budget = new ToolBudget();
        Job job = tool(budget, () -> jobs.findVisibleById(jobId, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found.")));
        ResumeVersion version = tool(budget, () -> versions.findById(resumeVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found.")));
        Resume resume = tool(budget, () -> resumes.findByIdAndUser_Id(version.getResumeId(), owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found.")));
        if (!resume.getId().equals(version.getResumeId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found.");
        UserAccount account = tool(budget, () -> users.findById(owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in again to continue.")));
        UserProfile profile = tool(budget, () -> profiles.findByUser_Id(owner).orElse(null));

        String fileName = version.getOriginalFileName();
        boolean fileAvailable;
        try { tool(budget, () -> storage.resolve(version.getStorageKey())); fileAvailable = true; }
        catch (IOException ex) { fileAvailable = false; }
        if (!version.isReviewed()) {
            List<AnswerResponse> pending = questions.stream().map(q -> new AnswerResponse(q.key(), q.label(), q.required(),
                    q.consequential() || isSensitive(q.label()), "", "USER_REQUIRED",
                    "Review the resume first; answer and confirm this field yourself.", "", false, null)).toList();
            return new Prepared(null, null, null, fileName, fileAvailable, pending,
                    List.of("Review the selected resume version in Resume Studio before preparing an application."));
        }

        CoverLetterDtos.Response letter = tool(budget, () -> coverLetters.generate(owner,
                new CoverLetterDtos.GenerateRequest(jobId, resumeVersionId, tone)));
        List<AnswerResponse> answerDrafts = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        if (!fileAvailable) missing.add("Re-upload this resume version: its file is missing from the current server storage.");
        if (job.getDescription() == null || job.getDescription().isBlank()) missing.add("Add the job description before preparing a job-specific application.");

        String profileContext = profile == null ? "" : String.join("\n", nonBlank(profile.getHeadline(), profile.getLocation(), profile.getSummary()));
        int toolCount = budget.count;
        for (QuestionRequest question : questions) {
            if (++toolCount > MAX_TOOL_CALLS) throw new IllegalStateException("The bounded preparation step limit was reached.");
            boolean sensitive = question.consequential() || isSensitive(question.label());
            AiProvider.ApplicationAnswerDraft draft = sensitive
                    ? new AiProvider.ApplicationAnswerDraft("", true, "This is consequential or sensitive; only you can provide and confirm it.", "USER_REQUIRED")
                    : ai.draftApplicationAnswer(account.getDisplayName(), job.getTitle(), job.getCompany(), job.getDescription(),
                        profileContext, version.getExtractedText(), version.getSkillsText().isBlank() ? List.of() :
                            java.util.Arrays.stream(version.getSkillsText().split(",")).map(String::trim).filter(s -> !s.isBlank()).toList(), question.label());
            String reusable = null;
            if (!sensitive) reusable = confirmedAnswers.findByOwner_IdAndJob_IdAndQuestionHash(owner, job.getId(), questionHash(question.label()))
                    .filter(ConfirmedApplicationAnswer::isRecent).map(ConfirmedApplicationAnswer::getAnswerText).orElse(null);
            if (question.required()) missing.add("Answer and confirm: " + question.label());
            answerDrafts.add(new AnswerResponse(question.key(), question.label(), question.required(), sensitive,
                    safe(draft.answer(), 2000), safe(draft.source(), 32), safe(draft.reason(), 500), "", false, reusable));
        }
        return new Prepared(letter.id(), letter.body(), letter.generationSource(), fileName, fileAvailable,
                List.copyOf(answerDrafts), List.copyOf(missing));
    }

    static boolean isSensitive(String question) {
        String text = question.toLowerCase(Locale.ROOT);
        return text.matches(".*\\b(work authorization|authorized to work|visa|sponsor(ship)?|ethnicity|race|gender|disability|criminal|conviction|citizenship|veteran|salary history|date of birth|medical|religion|marital)\\b.*");
    }

    static String questionHash(String label) {
        try {
            String normalized = label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) { throw new IllegalStateException("Could not fingerprint an application question.", ex); }
    }

    private <T> T tool(ToolBudget budget, java.util.concurrent.Callable<T> action) throws Exception {
        if (++budget.count > MAX_TOOL_CALLS) throw new IllegalStateException("The bounded preparation step limit was reached.");
        return action.call();
    }
    private String safe(String value, int max) { if (value == null) return ""; return value.length() <= max ? value : value.substring(0, max); }
    private List<String> nonBlank(String... values) { return java.util.Arrays.stream(values).filter(v -> v != null && !v.isBlank()).toList(); }
    private static final class ToolBudget { int count; }

    public record Prepared(UUID coverLetterId, String coverLetterDraft, String coverLetterSource,
                           String resumeFileName, boolean resumeFileAvailable,
                           List<AnswerResponse> answers, List<String> missingInformation) {}

    List<AnswerResponse> readAnswers(String json) throws IOException { return mapper.readValue(json, ANSWER_LIST); }
    String writeAnswers(List<AnswerResponse> answers) throws IOException { return mapper.writeValueAsString(answers); }
    String writeMissing(List<String> missing) throws IOException { return mapper.writeValueAsString(missing); }
    List<String> readMissing(String json) throws IOException { return mapper.readValue(json, new TypeReference<List<String>>() {}); }
}
