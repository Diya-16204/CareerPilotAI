package com.careerpilot.automation;

import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Extension contract for a specifically authorized external job-site integration.
 * The application currently registers no implementations; simulated portals use
 * {@link PortalAutomationRunner} and are intentionally kept on loopback only.
 */
public interface RealJobSiteAdapter {
    String siteId();

    boolean supports(URI sourceUrl);

    ApplicationPreview preview(SubmissionDraft draft);

    SubmissionResult submit(SubmissionDraft draft, ExplicitApproval approval);

    record SubmissionDraft(URI sourceUrl, String jobTitle, String applicantName, String applicantEmail,
                          Path resumeFile, Map<String, String> approvedAnswers) {
        public SubmissionDraft {
            if (sourceUrl == null || jobTitle == null || applicantName == null || applicantEmail == null || resumeFile == null)
                throw new IllegalArgumentException("An external application draft needs its source, applicant, and resume.");
            approvedAnswers = approvedAnswers == null ? Map.of() : Map.copyOf(approvedAnswers);
        }
    }

    record ExplicitApproval(UUID ownerUserId, UUID idempotencyKey, Instant approvedAt) {
        public ExplicitApproval {
            if (ownerUserId == null || idempotencyKey == null || approvedAt == null)
                throw new IllegalArgumentException("External submission requires a recorded user approval and idempotency key.");
        }
    }

    record ApplicationPreview(String siteId, URI sourceUrl, Map<String, String> visibleFields, Instant capturedAt) {
        public ApplicationPreview {
            if (siteId == null || sourceUrl == null || capturedAt == null)
                throw new IllegalArgumentException("A preview must identify its site, source, and capture time.");
            visibleFields = visibleFields == null ? Map.of() : Map.copyOf(visibleFields);
        }
    }

    enum Outcome { VERIFIED_SUCCESS, FAILED, UNVERIFIED }

    record SubmissionResult(String siteId, URI sourceUrl, Outcome outcome, String confirmationId,
                            String safeMessage, Instant completedAt, String evidenceReference) {
        public SubmissionResult {
            if (siteId == null || sourceUrl == null || outcome == null || completedAt == null)
                throw new IllegalArgumentException("A result must identify its site, source, outcome, and completion time.");
            if (outcome == Outcome.VERIFIED_SUCCESS && (confirmationId == null || confirmationId.isBlank()))
                throw new IllegalArgumentException("Verified external submission requires an independently checked confirmation.");
        }
    }
}
