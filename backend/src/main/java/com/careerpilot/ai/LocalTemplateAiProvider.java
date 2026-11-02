package com.careerpilot.ai;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.List;

@Component
@ConditionalOnProperty(name="app.ai.provider",havingValue="local",matchIfMissing=true)
public class LocalTemplateAiProvider implements AiProvider {
    @Override public Draft generateCoverLetter(String candidateName,String jobTitle,String company,String jobDescription,
                                               String reviewedResumeText,List<String> verifiedResumeSkills,String tone){
        String greeting="Dear Hiring Team,";
        String skillSentence=verifiedResumeSkills.isEmpty()?"My resume is attached for your review.":"My resume highlights experience with "+join(verifiedResumeSkills)+".";
        String opening=switch(tone==null?"professional":tone.toLowerCase(java.util.Locale.ROOT)){
            case "warm" -> "I was glad to learn about the "+jobTitle+" opportunity at "+company+".";
            case "direct" -> "I am applying for the "+jobTitle+" role at "+company+".";
            default -> "I am interested in the "+jobTitle+" opportunity at "+company+".";
        };
        String body=greeting+"\n\n"+opening+" "+skillSentence+" I would welcome the opportunity to discuss how my background may fit the needs of your team.\n\nThank you for your consideration.\n\nSincerely,\n"+candidateName;
        return new Draft(body,"LOCAL_TEMPLATE");
    }
    private String join(List<String> skills){if(skills.size()==1)return skills.getFirst();if(skills.size()==2)return skills.get(0)+" and "+skills.get(1);return String.join(", ",skills.subList(0,skills.size()-1))+", and "+skills.getLast();}
}

//Imports annotation to enable or disable Spring bean creation based on properties in application.properties or application.yml.

//Instructs Spring Boot to load this implementation only when app.ai.provider=local in configuration, or by default if app.ai.provider is omitted (matchIfMissing = true).

//Private utility method to assemble skills into grammatically correct English string lists.
//If list has 1 item, returns item directly using Java 21 getFirst().
//If list has 2 items, formats as "Item1 and Item2".
//Formats 3 or more items with Oxford comma syntax (e.g., "Java, Spring Boot, and PostgreSQL" using Java 21 getLast()).
