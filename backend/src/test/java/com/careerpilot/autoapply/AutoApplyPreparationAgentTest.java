package com.careerpilot.autoapply;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutoApplyPreparationAgentTest {
    @Test
    void treatsSensitiveAndEligibilityQuestionsAsManualOnly() {
        assertThat(AutoApplyPreparationAgent.isSensitive("Do you need visa sponsorship?"))
                .isTrue();
        assertThat(AutoApplyPreparationAgent.isSensitive("Are you authorized to work in this country?"))
                .isTrue();
        assertThat(AutoApplyPreparationAgent.isSensitive("What is your favorite programming language?"))
                .isFalse();
    }

    @Test
    void normalizesQuestionTextBeforeCreatingReuseKey() {
        assertThat(AutoApplyPreparationAgent.questionHash("What is your experience?"))
                .isEqualTo(AutoApplyPreparationAgent.questionHash(" what IS your   experience "));
        assertThat(AutoApplyPreparationAgent.questionHash("Work authorization?"))
                .isNotEqualTo(AutoApplyPreparationAgent.questionHash("Salary expectations?"));
    }
}
