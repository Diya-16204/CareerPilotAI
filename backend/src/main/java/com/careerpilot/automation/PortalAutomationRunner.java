package com.careerpilot.automation;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public interface PortalAutomationRunner {
    RunnerResult run(RunContext context) throws Exception;

    record RunContext(UUID runId,String portalKey,String jobKey,String jobTitle,UUID idempotencyKey,
                      String accessToken,String frontendUrl,String apiBaseUrl,
                      String applicantName,String applicantEmail,String education,String experience,
                      String coverLetter,Map<String,String> answers,Path resumeFile,String resumeFileName) {}

    record RunnerResult(UUID confirmationId,String portalKey,String jobKey,boolean independentlyVerified) {}
}
