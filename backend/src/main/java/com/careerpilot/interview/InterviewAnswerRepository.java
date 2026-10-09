package com.careerpilot.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer,UUID> {
    boolean existsBySession_IdAndQuestion_Id(UUID sessionId,UUID questionId);
    int countBySession_Id(UUID sessionId);
    List<InterviewAnswer> findAllBySession_IdOrderByOrdinal(UUID sessionId);
    @Query("select count(a) from InterviewAnswer a where a.session.user.id=:owner")
    long countByOwner(@Param("owner") UUID owner);
}
