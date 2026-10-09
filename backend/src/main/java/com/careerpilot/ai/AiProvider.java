package com.careerpilot.ai;

import java.util.List;

public interface AiProvider {
    Draft generateCoverLetter(String candidateName,String jobTitle,String company,String jobDescription,
                              String reviewedResumeText,List<String> verifiedResumeSkills,String tone);
    record Draft(String body,String source){}
}
