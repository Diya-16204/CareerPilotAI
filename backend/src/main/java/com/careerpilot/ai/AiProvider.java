package com.careerpilot.ai;

import java.util.List;

public interface AiProvider {
    Draft generateCoverLetter(String candidateName,String jobTitle,String company,List<String> verifiedResumeSkills,String tone);
    record Draft(String body,String source){}
}
