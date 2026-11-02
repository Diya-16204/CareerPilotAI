package com.careerpilot.autoapply;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.EntityGraph;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPrepSessionRepositoryTest {
    @Test
    void sessionQueriesFetchRelationsUsedByTheResponseWhenOpenInViewIsDisabled() throws NoSuchMethodException {
        EntityGraph listGraph = ApplicationPrepSessionRepository.class
                .getMethod("findTop50ByOwner_IdOrderByUpdatedAtDesc", java.util.UUID.class)
                .getAnnotation(EntityGraph.class);
        EntityGraph detailGraph = ApplicationPrepSessionRepository.class
                .getMethod("findByIdAndOwner_Id", java.util.UUID.class, java.util.UUID.class)
                .getAnnotation(EntityGraph.class);

        assertThat(listGraph.attributePaths()).containsExactly("job", "resumeVersion");
        assertThat(detailGraph.attributePaths()).containsExactly("job", "resumeVersion");
    }
}

//While assertThat() checks the state of an object (what value a method returned), verify() checks the behavior of your code—specifically whether a mock object's method was actually called, how many times it was called, and what arguments were passed to it.
