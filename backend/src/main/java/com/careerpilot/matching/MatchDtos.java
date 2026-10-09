package com.careerpilot.matching;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MatchDtos {
    private MatchDtos(){}
    public record Request(@NotNull UUID resumeVersionId,@NotNull UUID jobId){}
    public record SkillEvidence(String skill,String resumeEvidence,String jobEvidence){}
    public record Response(UUID id,UUID resumeVersionId,UUID jobId,int score,String formulaVersion,int requiredSkillCoverage,List<SkillEvidence> matchedSkills,List<SkillEvidence> missingSkills,List<SkillEvidence> partialSkills,List<String> recommendations,String explanation,Instant createdAt){}
}
