package com.careerpilot.interview;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InterviewServiceTest {
    @Test void keywordCoverageIsRepeatableAndRewardsRelevantContent(){
        String expected="Atomicity means all operations succeed or none. Consistency preserves constraints. Isolation limits interference. Durability keeps committed work.";
        assertThat(InterviewService.coverage(expected,expected)).isEqualTo(100);
        assertThat(InterviewService.coverage(expected,"Atomicity only.")).isLessThan(InterviewService.coverage(expected,expected));
        assertThat(InterviewService.coverage(expected,"Atomicity only.")).isEqualTo(InterviewService.coverage(expected,"Atomicity only."));
    }
}
