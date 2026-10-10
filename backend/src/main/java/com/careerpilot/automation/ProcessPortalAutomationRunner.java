package com.careerpilot.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessPortalAutomationRunner implements PortalAutomationRunner {
    private final ObjectMapper mapper;
    private final String nodeExecutable;
    private final Path runnerScript;
    private final Path frontendDirectory;
    private final String browser;
    private final Duration timeout;
    private final Path evidenceDirectory;

    public ProcessPortalAutomationRunner(ObjectMapper mapper,
            @Value("${app.automation.node-executable:node}") String nodeExecutable,
            @Value("${app.automation.runner-script:../frontend/scripts/portal-automation.mjs}") String runnerScript,
            @Value("${app.automation.frontend-directory:../frontend}") String frontendDirectory,
            @Value("${app.automation.browser:msedge}") String browser,
            @Value("${app.automation.timeout-seconds:90}") long timeoutSeconds,
            @Value("${app.automation.evidence-directory:../.tools/automation-evidence}") String evidenceDirectory) {
        this.mapper=mapper;
        this.nodeExecutable=nodeExecutable;
        this.frontendDirectory=Path.of(frontendDirectory).toAbsolutePath().normalize();
        this.runnerScript=Path.of(runnerScript).toAbsolutePath().normalize();
        this.browser=browser;
        this.timeout=Duration.ofSeconds(Math.clamp(timeoutSeconds,15,180));
        this.evidenceDirectory=Path.of(evidenceDirectory).toAbsolutePath().normalize();
        if(!this.runnerScript.startsWith(this.frontendDirectory))throw new IllegalArgumentException("Automation script must be inside the frontend directory.");
    }

    @Override
    public RunnerResult run(RunContext context) throws Exception {
        requireLocal(context.frontendUrl());
        requireLocal(context.apiBaseUrl());
        if(!Path.of(context.resumeFile().toAbsolutePath().normalize().toString()).isAbsolute())throw new IllegalArgumentException("Resume file path must be absolute.");
        Map<String,Object> payload=Map.ofEntries(
                Map.entry("runId",context.runId().toString()),
                Map.entry("portalKey",context.portalKey()),
                Map.entry("jobKey",context.jobKey()),
                Map.entry("jobTitle",context.jobTitle()),
                Map.entry("idempotencyKey",context.idempotencyKey().toString()),
                Map.entry("accessToken",context.accessToken()),
                Map.entry("frontendUrl",context.frontendUrl()),
                Map.entry("apiBaseUrl",context.apiBaseUrl()),
                Map.entry("applicantName",context.applicantName()),
                Map.entry("applicantEmail",context.applicantEmail()),
                Map.entry("education",context.education()==null?"":context.education()),
                Map.entry("experience",context.experience()==null?"":context.experience()),
                Map.entry("coverLetter",context.coverLetter()==null?"":context.coverLetter()),
                Map.entry("answers",context.answers()),
                Map.entry("resumeFile",context.resumeFile().toAbsolutePath().normalize().toString()),
                Map.entry("resumeFileName",context.resumeFileName()),
                Map.entry("evidenceDirectory",evidenceDirectory.toString()),
                Map.entry("browser",browser));

        if(!Files.isRegularFile(runnerScript))throw new AutomationExecutionException("The Playwright runner is unavailable.",false);
        if(!Files.isRegularFile(context.resumeFile()))throw new AutomationExecutionException("The selected resume file is unavailable.",false);
        Process process=new ProcessBuilder(nodeExecutable,runnerScript.toString())
                .directory(frontendDirectory.toFile())
                .redirectErrorStream(false)
                .start();
        CompletableFuture.runAsync(()->drain(process));
        try(OutputStream input=process.getOutputStream()) {
            input.write(mapper.writeValueAsBytes(payload));
        }
        if(!process.waitFor(timeout.toSeconds(),TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AutomationExecutionException("The browser run timed out.",true);
        }
        String output=new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8).trim();
        if(output.isBlank())throw new AutomationExecutionException("The browser workflow did not return a confirmation.",true);
        JsonNode result;
        try{result=mapper.readTree(output);}
        catch(Exception ex){throw new AutomationExecutionException("The browser workflow returned an invalid result.",true);}
        if(process.exitValue()!=0)throw new AutomationExecutionException(
                "The simulated portal workflow failed: "+result.path("diagnostic").asText("unknown browser error").substring(0,Math.min(300,result.path("diagnostic").asText("unknown browser error").length())),
                result.path("outcomeMayBeUnknown").asBoolean(true));
        if(!result.path("independentlyVerified").asBoolean(false))throw new AutomationExecutionException("The saved submission could not be independently verified.",true);
        return new RunnerResult(java.util.UUID.fromString(result.path("confirmationId").asText()),
                result.path("portalKey").asText(),result.path("jobKey").asText(),true);
    }

    private void requireLocal(String url) {
        URI uri=URI.create(url);
        String host=uri.getHost();
        if(!uri.getScheme().equals("http")||host==null||!(host.equalsIgnoreCase("localhost")||host.equals("127.0.0.1")||host.equals("::1")))
            throw new IllegalArgumentException("The simulated automation runner only accepts loopback URLs.");
    }

    private void drain(Process process) {
        var errorStream=process.getErrorStream();
        try(errorStream){errorStream.transferTo(OutputStream.nullOutputStream());}
        catch(Exception ignored){}
    }

    public static class AutomationExecutionException extends RuntimeException {
        private final boolean outcomeMayBeUnknown;
        public AutomationExecutionException(String message,boolean outcomeMayBeUnknown){super(message);this.outcomeMayBeUnknown=outcomeMayBeUnknown;}
        public boolean outcomeMayBeUnknown(){return outcomeMayBeUnknown;}
    }
}
