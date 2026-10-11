package com.careerpilot.matching;

import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.regex.Pattern;
import static com.careerpilot.matching.MatchDtos.*;

@Service
public class MatchingService {
    public static final String FORMULA_VERSION="skill-coverage-v1";
    private static final Map<String,String> ALIASES=Map.ofEntries(
            Map.entry("js","javascript"),Map.entry("ecmascript","javascript"),Map.entry("react.js","react"),
            Map.entry("postgres","postgresql"),Map.entry("postgresql","postgresql"),Map.entry("springboot","spring boot"),
            Map.entry("rest api","rest apis"),Map.entry("restful api","rest apis"),Map.entry("restful apis","rest apis"),
            Map.entry("nodejs","node.js"),Map.entry("node.js","node.js"));
    private final ResumeVersionRepository versions;private final ResumeRepository resumes;private final JobRepository jobs;private final ResumeJobMatchRepository matches;private final UserAccountRepository users;private final SkillRepository skills;private final MatchSkillRepository matchSkills;
    public MatchingService(ResumeVersionRepository versions,ResumeRepository resumes,JobRepository jobs,ResumeJobMatchRepository matches,UserAccountRepository users,SkillRepository skills,MatchSkillRepository matchSkills){this.versions=versions;this.resumes=resumes;this.jobs=jobs;this.matches=matches;this.users=users;this.skills=skills;this.matchSkills=matchSkills;}

    @Transactional
    public Response match(UUID owner,Request request){
        ResumeVersion version=versions.findById(request.resumeVersionId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found."));
        if(resumes.findByIdAndUser_Id(version.getResumeId(),owner).isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found.");
        Job job=jobs.findVisibleById(request.jobId(),owner).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found."));
        List<String> required=job.getSkills().stream().map(MatchingService::canonical).distinct().toList();
        List<String> present=version.getSkillsText().isBlank()?List.of():Arrays.stream(version.getSkillsText().split(",")).map(String::trim).map(MatchingService::canonical).distinct().toList();
        List<SkillEvidence> found=new ArrayList<>(),missing=new ArrayList<>();List<String> recommendations=new ArrayList<>();
        for(String skill:required){
            String evidence=findEvidence(version.getExtractedText(),skill);
            if(present.contains(skill))found.add(new SkillEvidence(skill,evidence==null?"Detected in resume skills.":evidence,"Listed as a job skill."));
            else {missing.add(new SkillEvidence(skill,null,"Listed as a job skill."));recommendations.add("If you have used "+skill+", add a specific, truthful example; otherwise consider a small practice project to build evidence.");}
        }
        int score=required.isEmpty()?0:(int)Math.round(100.0*found.size()/required.size());
        int coverage=score;
        String explanation=required.isEmpty()?"This listing does not provide a structured skills list, so no score was calculated.":"Score = matching listed skills ÷ total listed job skills × 100. Each listed skill has equal weight; this is a skills coverage indicator, not a hiring probability.";
        Map<String,Object> stored=Map.of("matchedSkills",found,"missingSkills",missing,"partialSkills",List.of(),"recommendations",recommendations,"explanation",explanation,"requiredSkillCoverage",coverage);
        ResumeJobMatch entity=matches.saveAndFlush(new ResumeJobMatch(users.getReferenceById(owner),version,job,score,FORMULA_VERSION,stored));
        List<MatchSkill> classifications=new ArrayList<>();
        for(SkillEvidence item:found){Skill skill=skills.findByCanonicalNameIgnoreCase(item.skill()).orElseGet(()->skills.save(new Skill(item.skill())));classifications.add(new MatchSkill(entity,skill,"MATCHED",item.resumeEvidence(),item.jobEvidence()));}
        for(SkillEvidence item:missing){Skill skill=skills.findByCanonicalNameIgnoreCase(item.skill()).orElseGet(()->skills.save(new Skill(item.skill())));classifications.add(new MatchSkill(entity,skill,"MISSING",null,item.jobEvidence()));}
        matchSkills.saveAll(classifications);
        return new Response(entity.getId(),version.getId(),job.getId(),score,FORMULA_VERSION,coverage,found,missing,List.of(),recommendations,explanation,entity.getCreatedAt());
    }

    public static int deterministicScore(List<String> required,List<String> resumeSkills){
        if(required==null||required.isEmpty())return 0;Set<String> available=new HashSet<>();if(resumeSkills!=null)resumeSkills.stream().map(MatchingService::canonical).forEach(available::add);
        long count=required.stream().map(MatchingService::canonical).distinct().filter(available::contains).count();return (int)Math.round(100.0*count/required.stream().map(MatchingService::canonical).distinct().count());
    }
    private static String canonical(String name){String normalized=name.toLowerCase(Locale.ROOT).replaceAll("\\s+"," ").trim();return ALIASES.getOrDefault(normalized,normalized);}
    private String findEvidence(String text,String skill){Pattern pattern=Pattern.compile("(?i)(?<![a-z0-9])"+Pattern.quote(skill)+"(?![a-z0-9])");var matcher=pattern.matcher(text);return matcher.find()?"Resume text includes “"+matcher.group()+"”.":null;}
}
