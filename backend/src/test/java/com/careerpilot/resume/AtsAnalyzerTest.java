package com.careerpilot.resume;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AtsAnalyzerTest {
    private final AtsAnalyzer analyzer=new AtsAnalyzer(.40,.15,.15,.10,.10,.10);

    @Test void jobSpecificScoreUsesResumeContentAndListsMissingSkills(){
        String resume="Jordan Doe\njordan@example.com\n555-123-4567\n\nExperience\nBuilt Java Spring Boot REST APIs and SQL services.\n\nProjects\nCreated a React dashboard.\n\nEducation\nBachelor of Science 2023";
        var result=analyzer.analyze(resume,"jordan@example.com","555-123-4567","Java, Spring Boot, SQL, React",
                "Required: JavaScript, Spring Boot, PostgreSQL. Preferred: AWS. Build REST APIs.",List.of("JavaScript","Spring Boot","PostgreSQL","AWS"));
        assertEquals("JOB_SPECIFIC",result.analysisType());
        assertTrue(result.overallScore()>=0&&result.overallScore()<=100);
        assertTrue(result.missingSkills().contains("postgresql"));
        assertTrue(result.matchedKeywords().contains("spring boot"));
        assertTrue(result.categories().stream().anyMatch(c->c.key().equals("requiredSkills")));
        assertTrue(result.formulaExplanation().contains("estimate"));
    }

    @Test void generalScoreIsClearlyLabeledAndUsesExtractedContactAndSections(){
        var result=analyzer.analyze("Experience\nSoftware development\nEducation\nBSc 2024\nSkills\nJava and SQL\nProjects\nPortfolio",null,null,"Java, SQL",null,List.of());
        assertEquals("GENERAL",result.analysisType());
        assertTrue(result.issues().stream().anyMatch(issue->issue.section().equals("Contact")));
        assertTrue(result.formulaExplanation().contains("not an employer ATS score"));
    }

    @Test void rejectsResumeWithNoExtractedText(){
        assertThrows(IllegalArgumentException.class,()->analyzer.analyze(" ",null,null,"",null,List.of()));
    }
}
