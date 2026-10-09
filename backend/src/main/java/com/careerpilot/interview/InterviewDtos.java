package com.careerpilot.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class InterviewDtos {
    private InterviewDtos(){}
    public record StartRequest(@NotBlank @Size(max=160) String role,@NotBlank @Size(max=40) String topic,@NotBlank @Size(max=24) String difficulty){}
    public record AnswerRequest(@NotNull UUID questionId,@NotBlank @Size(max=12000) String answer){}
    public record QuestionResponse(UUID id,String topic,String difficulty,String prompt){}
    public record SessionResponse(UUID id,String role,String topic,String difficulty,String state,Instant startedAt,Instant endedAt,Integer averageScore,int answerCount){}
    public record AnswerResponse(UUID id,UUID questionId,int ordinal,String question,String answer,int contentCoverageScore,String feedback,String evaluationSource,String modelAnswer,Instant createdAt){}
    public record SessionDetail(SessionResponse session,List<AnswerResponse> answers){}
}
