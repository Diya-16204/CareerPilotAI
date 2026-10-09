package com.careerpilot.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion,UUID> {
    @Query("select q from InterviewQuestion q where (:topic is null or q.topic=:topic) and (:difficulty is null or q.difficulty=:difficulty) order by q.topic,q.difficulty")
    List<InterviewQuestion> search(@Param("topic") String topic,@Param("difficulty") String difficulty);
    boolean existsByTopicAndDifficulty(String topic,String difficulty);
    List<InterviewQuestion> findAllByTopicAndDifficultyOrderBySeedKey(String topic,String difficulty);
}
