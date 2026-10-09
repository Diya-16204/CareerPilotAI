package com.careerpilot.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimulatedPortalServiceTest {
    @Test void requiresConsentAndValidResumeBeforeSaving(){
        var repository=mock(SimulatedPortalSubmissionRepository.class);
        var service=new SimulatedPortalService(repository,new ObjectMapper());
        var owner=UUID.randomUUID();
        var request=new SimulatedPortalDtos.SubmissionRequest("standard-product-analyst","Test User","test@example.com",null,null,"I like this role.","resume.pdf",Base64.getEncoder().encodeToString("%PDF-1.7".getBytes(StandardCharsets.US_ASCII)),Map.of(),false);
        assertThrows(ResponseStatusException.class,()->service.submit(owner,"PORTAL_A",UUID.randomUUID(),request));
        verify(repository,never()).saveAndFlush(any());
    }

    @Test void reusesConfirmationForAnIdenticalIdempotentRetry(){
        var repository=mock(SimulatedPortalSubmissionRepository.class);
        var service=new SimulatedPortalService(repository,new ObjectMapper());
        var owner=UUID.randomUUID();
        var questions=service.jobs("PORTAL_B").getFirst().questions();
        var file=Base64.getEncoder().encodeToString("%PDF-1.7 test".getBytes(StandardCharsets.US_ASCII));
        var request=new SimulatedPortalDtos.SubmissionRequest("wizard-software-engineer","Test User","test@example.com","Bachelor degree","Built a test project.",null,"resume.pdf",file,Map.of(questions.get(0),"Bachelor degree",questions.get(1),"Built a test project."),true);
        var idempotency=UUID.randomUUID();
        when(repository.findByOwnerUserIdAndPortalKeyAndIdempotencyKey(owner,"PORTAL_B",idempotency)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(invocation->invocation.getArgument(0));
        var created=service.submit(owner,"portal_b",idempotency,request);
        var saved=captureSaved(repository);
        when(repository.findByOwnerUserIdAndPortalKeyAndIdempotencyKey(owner,"PORTAL_B",idempotency)).thenReturn(Optional.of(saved));
        var retried=service.submit(owner,"PORTAL_B",idempotency,request);
        var freshKey=UUID.randomUUID();
        when(repository.findByOwnerUserIdAndPortalKeyAndIdempotencyKey(owner,"PORTAL_B",freshKey)).thenReturn(Optional.empty());
        when(repository.findByOwnerUserIdAndPortalKeyAndJobKeyAndApplicantEmailIgnoreCase(owner,"PORTAL_B","wizard-software-engineer","test@example.com")).thenReturn(Optional.of(saved));
        var duplicate=service.submit(owner,"PORTAL_B",freshKey,request);
        assertEquals(created.confirmationId(),retried.confirmationId());
        assertTrue(retried.reused());
        assertEquals(created.confirmationId(),duplicate.confirmationId());
        assertTrue(duplicate.reused());
        verify(repository,times(1)).saveAndFlush(any());
    }

    private SimulatedPortalSubmission captureSaved(SimulatedPortalSubmissionRepository repository){
        var captor=org.mockito.ArgumentCaptor.forClass(SimulatedPortalSubmission.class);
        verify(repository).saveAndFlush(captor.capture());
        return captor.getValue();
    }
}
