package com.careerpilot.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class LocalTemplateAiProviderTest {
    @Test void localDraftUsesOnlySuppliedFactsAndLabelsItsSource(){
        AiProvider.Draft draft=new LocalTemplateAiProvider().generateCoverLetter("Alex Example","Java Engineer","Northstar Labs","Build backend services.","Java and Spring Boot",List.of("Java","Spring Boot"),"direct");
        assertThat(draft.source()).isEqualTo("LOCAL_TEMPLATE");
        assertThat(draft.body()).contains("Java Engineer","Northstar Labs","Alex Example","Java and Spring Boot");
        assertThat(draft.body()).doesNotContain("10 years","increased revenue");
    }
}

//This unit test (LocalTemplateAiProviderTest) verifies the behavior of LocalTemplateAiProvider—a deterministic, fallback cover letter generator that operates locally without making external LLM (e.g., OpenAI or Claude) API calls.
