package com.careerpilot.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer,UUID> {
    boolean existsBySessionIdAndQuestionId(UUID sessionId,UUID questionId);
    int countBySessionId(UUID sessionId);
    List<InterviewAnswer> findAllBySessionIdOrderByOrdinal(UUID sessionId);
    @Query("select count(a) from InterviewAnswer a where a.session.user.id=:owner")
    long countByOwner(@Param("owner") UUID owner);
}
