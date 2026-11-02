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

    /*These are Java records.You can think of them as small classes made for storing data.*/

    /*This creates a data type called:
        Draft

        It stores two pieces of data:
        Draft
        ├── body
        └── source
        For example:
        Draft myDraft = new Draft(
            "Dear Hiring Manager, I am interested in this role...",
            "OPENAI"
        );
    */

/*
A package in Java is used to organize related classes.
Think of it like a folder/category for Java code.
src/main/java/
└── com/
    └── careerpilot/
        └── ai/
            └── AiProvider.java





Everything stays organized.
So instead of writing:
    java.util.List<String>
everywhere, we write:
    import java.util.List;
and then simply use:
    List<String>






interface Animal {
    void makeSound();
}
This says:
    Any class implementing Animal must provide makeSound().






This means the method returns an object of type: Draft
    record Draft(String body,String source){}
    So Draft is a nested record defined inside this interface.







What is a default method in an interface?
Traditionally, interface methods were abstract.
interface Animal {
    void makeSound();
}
But Java allows interfaces to contain default methods.
Example:
interface Animal {
    default void sleep() {
        System.out.println("Sleeping");
    }
}


Not every provider may be capable of intelligently answering job application questions.
A default method has an actual implementation.
Instead of forcing every implementation to implement:
draftApplicationAnswer(...)
the interface provides a safe default response.
This is an important distinction.








*/
