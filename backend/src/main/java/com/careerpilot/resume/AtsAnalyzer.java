package com.careerpilot.resume;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Pattern;
import static com.careerpilot.resume.AtsDtos.*;

@Component
public class AtsAnalyzer {
    public static final String FORMULA_VERSION="careerpilot-ats-v1";
    private static final Map<String,List<String>> ALIASES=aliases();
    private static final Set<String> STOP=Set.of("the","and","for","with","that","this","from","your","you","our","are","will","have","has","was","were","who","what","when","where","which","into","about","role","work","working","team","job","candidate","responsibilities","requirements","experience","years","skills","ability","strong","including","using","use","across","such","their","they","must","preferred","required","plus","nice","good","excellent","support","build","develop","create","ensure","provide","help","deliver","new","also","etc");
    private final double requiredWeight,preferredWeight,experienceWeight,projectsWeight,educationWeight,keywordsWeight;

    public AtsAnalyzer(@Value("${app.ats.weights.required-skills:0.40}") double requiredWeight,
                       @Value("${app.ats.weights.preferred-skills:0.15}") double preferredWeight,
                       @Value("${app.ats.weights.relevant-experience:0.15}") double experienceWeight,
                       @Value("${app.ats.weights.projects-certifications:0.10}") double projectsWeight,
                       @Value("${app.ats.weights.education:0.10}") double educationWeight,
                       @Value("${app.ats.weights.role-keywords:0.10}") double keywordsWeight) {
        double total=requiredWeight+preferredWeight+experienceWeight+projectsWeight+educationWeight+keywordsWeight;
        if(requiredWeight<0||preferredWeight<0||experienceWeight<0||projectsWeight<0||educationWeight<0||keywordsWeight<0||total<=0)
            throw new IllegalArgumentException("ATS scoring weights must be non-negative and have a positive sum.");
        this.requiredWeight=requiredWeight;this.preferredWeight=preferredWeight;this.experienceWeight=experienceWeight;
        this.projectsWeight=projectsWeight;this.educationWeight=educationWeight;this.keywordsWeight=keywordsWeight;
    }

    public Result analyze(String resumeText,String extractedEmail,String extractedPhone,String skillsText,
                          String jobDescription,List<String> structuredJobSkills) {
        String text=Objects.requireNonNullElse(resumeText,"").trim();
        if(text.isBlank()) throw new IllegalArgumentException("The selected resume has no readable extracted text.");
        if(jobDescription==null||jobDescription.isBlank()) return general(text,extractedEmail,extractedPhone,skillsText);
        return jobSpecific(text,extractedEmail,extractedPhone,skillsText,jobDescription,structuredJobSkills==null?List.of():structuredJobSkills);
    }

    private Result general(String text,String email,String phone,String skillsText) {
        String lower=text.toLowerCase(Locale.ROOT);
        List<Category> categories=new ArrayList<>();List<Issue> issues=new ArrayList<>();List<String> strengths=new ArrayList<>(),suggestions=new ArrayList<>();
        int contact=(email!=null&&!email.isBlank()?50:0)+(phone!=null&&!phone.isBlank()?50:0);
        categories.add(new Category("contact","Contact details",contact,0.25,"Email and phone are scored from fields extracted by the resume parser."));
        if(contact==100) strengths.add("Email and phone were extracted from this resume version.");
        if(contact<100) issue(issues,suggestions,"Contact","high",true,"Contact details are incomplete","Recruiters need a reliable way to contact you.","The parser found "+contact+"% of the checked contact fields.","Add a clear email and phone number near the top of the resume.");
        int sectionCount=countMatches(lower,List.of("experience","work history","employment","education","skills","projects","certifications"));
        int sectionScore=Math.min(100,(int)Math.round(sectionCount/5.0*100));
        categories.add(new Category("sections","Recognizable sections",sectionScore,0.25,"Checks for common resume section headings in extracted text; it does not judge visual design."));
        if(sectionCount>=4) strengths.add("Several standard resume sections are recognizable in the extracted text.");
        if(!containsAny(lower,List.of("experience","work history","employment"))) issue(issues,suggestions,"Experience","medium",true,"No experience section heading was detected","A clear heading helps readers and parsers find work history.","Experience content may be harder to identify.","Add an Experience or Work History heading if applicable.");
        if(!lower.contains("education")) issue(issues,suggestions,"Education","medium",true,"No education heading was detected","A conventional heading helps identify qualifications.","Education may not be recognized as a separate section.","Add an Education heading if you want to include qualifications.");
        boolean dates=Pattern.compile("(?i)\\b(19|20)\\d{2}\\b").matcher(text).find();
        categories.add(new Category("dates","Date evidence",dates?100:0,0.15,"Checks whether any year-like date appears in the extracted text; individual date consistency is not inferred."));
        if(!dates) issue(issues,suggestions,"Dates","medium",true,"No year-like dates were detected","Dates help readers understand education and work timelines.","Timeline completeness could not be established.","Add accurate start/end dates to relevant experience and education entries.");
        int lengthScore=text.length()<300?25:text.length()<800?60:text.length()<18000?100:55;
        categories.add(new Category("readability","Extracted text",lengthScore,0.20,"Based on extracted text length only; this is not a grammar or visual-format score."));
        if(text.length()<300) issue(issues,suggestions,"Resume text","high",true,"Very little readable text was extracted","A short extraction may indicate a scanned or image-only resume.","The analysis may be incomplete.","Use a text-based PDF or DOCX and verify the extracted text preview.");
        else if(text.length()>18000) issue(issues,suggestions,"Resume text","low",true,"The extracted resume text is unusually long","Long resumes can make key qualifications harder to find.","The analyzer may include redundant content.","Review for repeated or unrelated content; keep relevant details.");
        int repetition=repetitionScore(lower);
        categories.add(new Category("keywordUsage","Keyword balance",repetition,0.15,"Flags extreme repeated-token patterns only; it does not grade spelling or grammar."));
        if(repetition<100) issue(issues,suggestions,"Keywords","medium",true,"Some words repeat unusually often","Excessive repetition can make a resume harder to read.","Repeated terms may be overemphasized.","Keep keywords where they describe real work; remove unnecessary repetition.");
        else strengths.add("No extreme repeated-word pattern was detected.");
        int score=weighted(categories);
        List<String> resumeSkills=splitSkills(skillsText);
        return new Result(score,"GENERAL",categories,resumeSkills,List.of(),List.of(),List.of(),List.of(),strengths,issues,suggestions,
                "General score = weighted average of contact (25%), recognizable sections (25%), date evidence (15%), extracted-text length (20%), and keyword balance (15%). This is a heuristic quality check, not an employer ATS score.");
    }

    private Result jobSpecific(String text,String email,String phone,String skillsText,String jd,List<String> structuredSkills) {
        String resume=text.toLowerCase(Locale.ROOT), description=jd.toLowerCase(Locale.ROOT);
        Set<String> resumeTerms=terms(resume,skillsText), jdTerms=terms(description,String.join(",",structuredSkills));
        List<String> jdSkills=extractSkills(description,structuredSkills);
        Set<String> preferred=preferredTerms(description,jdSkills);
        List<String> required=jdSkills.stream().filter(skill->!preferred.contains(skill)).toList();
        if(required.isEmpty()&&preferred.isEmpty()) required=jdSkills;
        List<String> matched=new ArrayList<>(),missingSkills=new ArrayList<>(),missingKeywords=new ArrayList<>(),partial=new ArrayList<>(),irrelevant=new ArrayList<>();
        for(String skill:jdSkills){if(hasCanonical(resumeTerms,skill)) matched.add(skill);else if(hasRelatedVariant(resumeTerms,skill)) partial.add(skill);else {missingSkills.add(skill);missingKeywords.add(skill);}}
        for(String keyword:jdTerms) if(!hasCanonical(resumeTerms,keyword)&&!jdSkills.contains(keyword)&&missingKeywords.size()<30) missingKeywords.add(keyword);
        for(String keyword:resumeTerms)if(!jdTerms.contains(keyword)&&!jdSkills.contains(keyword)&&irrelevant.size()<30)irrelevant.add(keyword);

        List<Category> categories=new ArrayList<>();
        addSkillCategory(categories,"requiredSkills","Required skills",required,resumeTerms,requiredWeight,"Derived from the job's structured skill list or required-skill language.");
        addSkillCategory(categories,"preferredSkills","Preferred skills",jdSkills.stream().filter(preferred::contains).toList(),resumeTerms,preferredWeight,"Only scored when preferred/nice-to-have language identifies skills.");
        String experienceSection=section(resume,List.of("experience","work history","employment"),List.of("education","projects","skills","certifications"));
        Integer experienceScore=experienceSection.isBlank()?null:similarity(experienceSection,description);
        addOptionalCategory(categories,"experience","Relevant experience",experienceScore,experienceWeight,"Resume experience section overlap with role terminology; no years or seniority are inferred.");
        String projectSection=section(resume,List.of("projects","certifications","achievements"),List.of("education","skills","experience","work history"));
        Integer projectScore=projectSection.isBlank()?null:similarity(projectSection,description);
        addOptionalCategory(categories,"projects","Projects and certifications",projectScore,projectsWeight,"Overlap in extracted projects/certification text; absent sections are excluded from the weighted score.");
        String educationSection=section(resume,List.of("education"),List.of("experience","work history","projects","skills","certifications"));
        boolean jdHasEducation=containsAny(description,List.of("bachelor","master","degree","education","qualification","graduate"));
        Integer educationScore=jdHasEducation?(educationSection.isBlank()?0:similarity(educationSection,description)):null;
        addOptionalCategory(categories,"education","Education and qualifications",educationScore,educationWeight,"Scored only when the job description mentions education/qualifications.");
        List<String> important=jdTerms.stream().filter(term->!jdSkills.contains(term)).limit(80).toList();
        Integer keywordScore=important.isEmpty()?null:percent(important.stream().filter(term->hasCanonical(resumeTerms,term)).count(),important.size());
        addOptionalCategory(categories,"keywords","Role-specific keywords",keywordScore,keywordsWeight,"Overlap of informative terms after common job-description words are removed.");
        int score=weighted(categories);

        List<Issue> issues=new ArrayList<>();List<String> strengths=new ArrayList<>(),suggestions=new ArrayList<>();
        if(!emailMatches(email)) issue(issues,suggestions,"Contact","medium",true,"No email was extracted","Contact information is commonly used for applications.","The parser did not find an email address.","Add a valid email near the top and verify it in Resume Studio.");
        if(!phoneMatches(phone)) issue(issues,suggestions,"Contact","low",true,"No phone was extracted","Some applications ask for a phone number.","The parser did not find a phone number.","Add a phone number only if you want employers to contact you by phone.");
        if(!containsAny(resume,List.of("experience","work history","employment"))) issue(issues,suggestions,"Experience","medium",true,"No experience heading was detected","A conventional heading can help parsing.","Experience matching is limited.","Add an Experience heading if applicable; do not invent experience.");
        if(!containsAny(resume,List.of("projects","certifications","certificates"))) issue(issues,suggestions,"Projects / certifications","low",true,"No projects or certifications heading was detected","Relevant projects can demonstrate skills when work history is limited.","This category was excluded from the score.","Add only real projects or certifications relevant to this role.");
        if(!required.isEmpty()&&missingSkills.isEmpty())strengths.add("All detected required skills appear in the resume's extracted skills or text.");
        if(!matched.isEmpty())strengths.add("Matched job skills: "+String.join(", ",matched)+".");
        for(String skill:missingSkills) suggestions.add("If you have genuinely used "+skill+", add one specific example in the relevant section. Otherwise do not add it just for keyword matching.");
        if(!partial.isEmpty())suggestions.add("Review partial skill matches: "+String.join(", ",partial)+"; the analyzer found a related wording but cannot confirm equivalence.");
        if(issues.stream().noneMatch(issue->issue.section().equals("Contact"))) strengths.add("Contact information was extracted from the resume.");
        String explanation="Job-specific score = weighted average of required skills ("+weight(requiredWeight)+"), preferred skills ("+weight(preferredWeight)+"), relevant experience ("+weight(experienceWeight)+"), projects/certifications ("+weight(projectsWeight)+"), education ("+weight(educationWeight)+"), and role keywords ("+weight(keywordsWeight)+"). These are configured category weights; categories without enough evidence are excluded and remaining weights are renormalized. Skill aliases are normalized (for example JS/JavaScript and ML/Machine Learning). This is an estimate, not an employer ATS score or selection guarantee.";
        return new Result(score,"JOB_SPECIFIC",categories,matched,missingKeywords,partial,irrelevant,missingSkills,strengths,issues,suggestions,explanation);
    }

    private static void addSkillCategory(List<Category> out,String key,String label,List<String> required,Set<String> resume,double weight,String explanation){
        if(required.isEmpty())return;int score=percent(required.stream().filter(skill->hasCanonical(resume,skill)).count(),required.size());
        out.add(new Category(key,label,score,weight,explanation));
    }
    private static void addOptionalCategory(List<Category> out,String key,String label,Integer score,double weight,String explanation){if(score!=null)out.add(new Category(key,label,score,weight,explanation));}
    private static int weighted(List<Category> categories){double weight=categories.stream().mapToDouble(Category::weight).sum();if(weight<=0)return 0;return Math.max(0,Math.min(100,(int)Math.round(categories.stream().mapToDouble(c->c.score()*c.weight()).sum()/weight)));}
    private static int percent(long matched,long total){return total==0?0:(int)Math.round(100.0*matched/total);}
    private static String weight(double value){return String.format(Locale.ROOT,"%.0f%%",value*100);}
    private static int countMatches(String text,List<String> phrases){return (int)phrases.stream().filter(text::contains).count();}
    private static boolean containsAny(String text,List<String> phrases){return phrases.stream().anyMatch(text::contains);}
    private static boolean emailMatches(String value){return value!=null&&!value.isBlank();}
    private static boolean phoneMatches(String value){return value!=null&&!value.isBlank();}
    private static void issue(List<Issue> issues,List<String> suggestions,String section,String severity,boolean confirmed,String problem,String why,String impact,String fix){issues.add(new Issue(section,severity,confirmed,problem,why,impact,fix));suggestions.add(fix);}
    private static int repetitionScore(String text){String[] words=text.split("[^a-z0-9+#.]+");if(words.length<20)return 100;Map<String,Integer> counts=new HashMap<>();for(String word:words)if(word.length()>3)counts.merge(word,1,Integer::sum);int max=counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);double ratio=(double)max/words.length;return ratio>.12?35:ratio>.08?65:100;}
    private static Set<String> terms(String text,String listedSkills){Set<String> found=new LinkedHashSet<>();String normalized=norm(text+" "+Objects.requireNonNullElse(listedSkills,""));for(var entry:ALIASES.entrySet())if(containsTerm(normalized,entry.getKey()))found.add(canonical(entry.getKey()));for(String word:normalized.split("[^a-z0-9+#.]+"))if(word.length()>=4&&!STOP.contains(word)&&!word.matches("\\d+"))found.add(word);return found;}
    private static List<String> extractSkills(String description,List<String> structured){LinkedHashSet<String> found=new LinkedHashSet<>();for(String skill:structured)found.add(canonical(skill));String normalized=norm(description);for(var entry:ALIASES.entrySet())if(containsTerm(normalized,entry.getKey()))found.add(canonical(entry.getKey()));return found.stream().filter(s->!s.isBlank()).toList();}
    private static Set<String> preferredTerms(String jd,List<String> skills){
        Set<String> result=new HashSet<>();
        for(String skill:skills){int at=jd.indexOf(skill);if(at<0)continue;
            String before=jd.substring(Math.max(0,at-180),at);
            int preferred=Math.max(Math.max(before.lastIndexOf("preferred"),before.lastIndexOf("nice to have")),Math.max(before.lastIndexOf("bonus"),before.lastIndexOf("desirable")));
            int required=Math.max(before.lastIndexOf("required"),before.lastIndexOf("must have"));
            if(preferred>=0&&preferred>required)result.add(skill);
        }
        return result;
    }
    private static Set<String> normalizeTerms(Collection<String> input){Set<String> out=new HashSet<>();input.forEach(value->out.add(canonical(value)));return out;}
    private static boolean hasCanonical(Set<String> present,String target){String c=canonical(target);return present.stream().anyMatch(s->canonical(s).equals(c));}
    private static boolean hasRelatedVariant(Set<String> present,String target){String c=canonical(target);return ALIASES.getOrDefault(c,List.of(c)).stream().anyMatch(alias->present.stream().anyMatch(s->s.equals(alias)));}
    private static List<String> splitSkills(String text){if(text==null||text.isBlank())return List.of();return Arrays.stream(text.split(",")).map(String::trim).filter(s->!s.isBlank()).toList();}
    private static String canonical(String value){String n=norm(value);for(var entry:ALIASES.entrySet())if(entry.getKey().equals(n))return entry.getValue().get(0);return n;}
    private static String norm(String value){return Objects.requireNonNullElse(value,"").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+#.]+"," ").trim().replaceAll("\\s+"," ");}
    private static boolean containsTerm(String text,String term){
        String quoted=Arrays.stream(term.split("\\s+")).map(Pattern::quote).collect(java.util.stream.Collectors.joining("\\s+"));
        return Pattern.compile("(?<![a-z0-9])"+quoted+"(?![a-z0-9])").matcher(text).find();
    }
    private static String section(String text,List<String> headings,List<String> endHeadings){int start=-1;for(String h:headings){int i=text.indexOf(h);if(i>=0&&(start<0||i<start))start=i;}if(start<0)return "";int end=text.length();for(String h:endHeadings){int i=text.indexOf(h,start+1);if(i>=0&&i<end)end=i;}return text.substring(start,end);}
    private static int similarity(String a,String b){Set<String> at=terms(a,""),bt=terms(b,"");if(bt.isEmpty())return 0;return percent(at.stream().filter(bt::contains).count(),bt.size());}
    private static Map<String,List<String>> aliases(){Map<String,List<String>> m=new LinkedHashMap<>();for(String s:List.of("java","javascript","typescript","react","spring boot","python","sql","postgresql","mysql","rest apis","docker","kubernetes","aws","git","playwright","node.js","html","css","figma","data analysis","machine learning","excel","communication","project management","ci/cd","leadership","agile","testing","azure","gcp","terraform","c++","c#"))m.put(s,new ArrayList<>(List.of(s)));for(String alias:List.of("js|javascript","ecmascript|javascript","ts|typescript","react.js|react","springboot|spring boot","postgres|postgresql","rest api|rest apis","restful api|rest apis","restful apis|rest apis","nodejs|node.js","ml|machine learning","k8s|kubernetes","cicd|ci/cd")){String[] p=alias.split("\\|",2);m.computeIfAbsent(p[1],k->new ArrayList<>(List.of(p[1]))).add(p[0]);}Map<String,List<String>> reverse=new LinkedHashMap<>();m.forEach((canonical,values)->values.forEach(alias->reverse.put(alias,List.copyOf(values))));return reverse;}
}
