package com.careerpilot.autoapply;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import com.careerpilot.resume.ResumeVersion;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ApplicationPrepSessionTest {
    @Test
    void confirmedAnswerHashMappingMatchesTheMySqlMigration() throws NoSuchFieldException {
        Column mapping = ConfirmedApplicationAnswer.class.getDeclaredField("questionHash").getAnnotation(Column.class);
        assertThat(mapping.columnDefinition()).isEqualTo("char(64)");
    }

    @Test
    void approvalHashMappingMatchesTheMySqlMigration() throws NoSuchFieldException {
        Column mapping = ApplicationPrepSession.class.getDeclaredField("approvedContentHash").getAnnotation(Column.class);
        assertThat(mapping.columnDefinition()).isEqualTo("char(64)");
    }

    @Test
    void changingDraftInvalidatesApprovalForPreviouslyReviewedContent() {
        var session = new ApplicationPrepSession(mock(UserAccount.class), mock(Job.class), mock(ResumeVersion.class), null);
        session.prepared(null, "Original letter", "LOCAL_TEMPLATE", "[]", "[]",
                ApplicationPrepStatus.READY_FOR_REVIEW, "Ready.");
        session.approve("reviewed-content-fingerprint");

        session.updateDraft("Edited letter", "[]", ApplicationPrepStatus.READY_FOR_REVIEW);

        assertThat(session.getStatus()).isEqualTo(ApplicationPrepStatus.READY_FOR_REVIEW);
        assertThat(session.getApprovedContentHash()).isNull();
        assertThat(session.getApprovedAt()).isNull();
    }
}
