package com.careerpilot.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Component
@ConditionalOnProperty(name="app.ai.provider",havingValue="local",matchIfMissing=false)
public class UnconfiguredAiProvider implements AiProvider {
    @Override public Draft generateCoverLetter(String candidateName,String jobTitle,String company,List<String> skills,String tone){
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"The requested AI provider is not configured in this build. Select AI_PROVIDER=local to use the clearly labeled local template.");
    }
}
