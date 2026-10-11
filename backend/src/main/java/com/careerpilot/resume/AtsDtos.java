package com.careerpilot.resume;

import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AtsDtos {
    private AtsDtos() {}
    public record AnalyzeRequest(UUID jobId, @Size(max=20000) String jobDescription) {}
    public record Category(String key, String label, Integer score, double weight, String explanation) {}
    public record Issue(String section, String severity, boolean confirmed, String problem, String whyItMatters, String impact, String suggestedFix) {}
    public record Result(int overallScore, String analysisType, List<Category> categories,
                         List<String> matchedKeywords, List<String> missingKeywords,
                         List<String> partialKeywords, List<String> irrelevantKeywords,
                         List<String> missingSkills, List<String> strengths, List<Issue> issues,
                         List<String> suggestions, String formulaExplanation) {}
    public record AnalysisResponse(UUID id, UUID resumeVersionId, UUID jobId, String jobTitle,
                                   int overallScore, String analysisType, Result result, Instant createdAt) {}
}
