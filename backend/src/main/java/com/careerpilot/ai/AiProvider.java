package com.careerpilot.ai;

import java.util.List;

public interface AiProvider {
    Draft generateCoverLetter(String candidateName,String jobTitle,String company,String jobDescription,
                              String reviewedResumeText,List<String> verifiedResumeSkills,String tone);
    default ApplicationAnswerDraft draftApplicationAnswer(String candidateName, String jobTitle, String company,
                                                           String jobDescription, String verifiedProfile,
                                                           String reviewedResumeText, List<String> verifiedResumeSkills,
                                                           String question) {
        return new ApplicationAnswerDraft("", true, "This answer needs your own input.", "LOCAL_NEEDS_USER_INPUT");
    }
    record Draft(String body,String source){}
    record ApplicationAnswerDraft(String answer, boolean needsUserInput, String reason, String source) {}
}
