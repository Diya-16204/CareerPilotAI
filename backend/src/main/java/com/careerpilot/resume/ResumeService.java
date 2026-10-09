package com.careerpilot.resume;

import com.careerpilot.auth.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.HexFormat;

import static com.careerpilot.resume.ResumeDtos.*;

@Service
public class ResumeService {
    private static final long MAX_FILE_SIZE=5L*1024*1024;
    private static final String PDF_TYPE="application/pdf";
    private static final String DOCX_TYPE="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final ResumeRepository resumes;private final ResumeVersionRepository versions;private final UserAccountRepository users;private final ResumeStorage storage;private final ResumeParser parser;
    public ResumeService(ResumeRepository resumes,ResumeVersionRepository versions,UserAccountRepository users,ResumeStorage storage,ResumeParser parser){this.resumes=resumes;this.versions=versions;this.users=users;this.storage=storage;this.parser=parser;}

    @Transactional
    public ResumeResponse upload(UUID ownerId,UUID resumeId,String requestedTitle,MultipartFile file){
        if(file==null||file.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a PDF or DOCX resume to upload.");
        if(file.getSize()>MAX_FILE_SIZE)throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Resume files must be 5 MB or smaller.");
        String filename=safeFilename(file.getOriginalFilename());String extension=extension(filename);byte[] bytes;
        try{bytes=file.getBytes();}catch(IOException exception){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The uploaded file could not be read.");}
        ResumeParser.Format format;
        if("pdf".equals(extension)&&startsWith(bytes,"%PDF-")){format=ResumeParser.Format.PDF;}
        else if("docx".equals(extension)&&bytes.length>3&&bytes[0]=='P'&&bytes[1]=='K'){format=ResumeParser.Format.DOCX;}
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"File contents do not match a supported PDF or DOCX document.");
        ResumeParser.Parsed parsed=parser.parse(bytes,format);
        String contentType=format==ResumeParser.Format.PDF?PDF_TYPE:DOCX_TYPE;
        String title=cleanTitle(requestedTitle,filename);
        Resume resume=resumeId==null?resumes.save(new Resume(users.getReferenceById(ownerId),title)):
                resumes.findByIdAndUser_Id(resumeId,ownerId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume not found."));
        int number=versions.countByResumeId(resume.getId())+1;
        String key=UUID.randomUUID().toString();
        try{
            storage.store(key,bytes);
            ResumeVersion version=versions.save(new ResumeVersion(resume,number,key,filename,contentType,sha256(bytes),parsed.text(),parsed.email(),parsed.phone(),String.join(",",parsed.skills())));
            resume.setActiveVersionNumber(number);
            return detail(resume,versions.findAllByResumeIdOrderByVersionNumberDesc(resume.getId()));
        }catch(RuntimeException|IOException exception){storage.delete(key);throw exception instanceof RuntimeException runtime?runtime:new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Resume storage failed.");}
    }

    @Transactional(readOnly=true)
    public List<ResumeSummary> list(UUID ownerId){return resumes.findAllByUser_IdOrderByUpdatedAtDesc(ownerId).stream().map(resume->{
        List<ResumeVersion> all=versions.findAllByResumeIdOrderByVersionNumberDesc(resume.getId());ResumeVersion latest=all.isEmpty()?null:all.getFirst();
        return new ResumeSummary(resume.getId(),resume.getTitle(),resume.getActiveVersionNumber(),resume.getCreatedAt(),resume.getUpdatedAt(),all.size(),latest==null?null:latest.getOriginalFileName());
    }).toList();}

    @Transactional(readOnly=true)
    public ResumeResponse get(UUID id,UUID ownerId){Resume resume=owned(id,ownerId);return detail(resume,versions.findAllByResumeIdOrderByVersionNumberDesc(id));}

    @Transactional
    public VersionResponse review(UUID id,UUID versionId,UUID ownerId,ReviewRequest request){
        owned(id,ownerId);ResumeVersion version=versions.findByIdAndResumeId(versionId,id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found."));
        String skills=request.skills()==null?"":request.skills().stream().map(String::trim).filter(s->!s.isEmpty()).distinct().collect(java.util.stream.Collectors.joining(","));
        version.review(blankToNull(request.email()),blankToNull(request.phone()),skills);return versionDto(version);
    }

    @Transactional(readOnly=true)
    public DownloadFile download(UUID id,UUID versionId,UUID ownerId){
        owned(id,ownerId);ResumeVersion version=versions.findByIdAndResumeId(versionId,id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found."));
        try{return new DownloadFile(storage.resolve(version.getStorageKey()),version.getOriginalFileName(),version.getContentType());}
        catch(IOException exception){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume file is unavailable.");}
    }

    @Transactional
    public void delete(UUID id,UUID ownerId){Resume resume=owned(id,ownerId);versions.findAllByResumeIdOrderByVersionNumberDesc(id).forEach(v->storage.delete(v.getStorageKey()));resumes.delete(resume);}

    private Resume owned(UUID id,UUID owner){return resumes.findByIdAndUser_Id(id,owner).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume not found."));}
    private ResumeResponse detail(Resume resume,List<ResumeVersion> found){return new ResumeResponse(resume.getId(),resume.getTitle(),resume.getActiveVersionNumber(),resume.getCreatedAt(),resume.getUpdatedAt(),found.stream().map(this::versionDto).toList());}
    private VersionResponse versionDto(ResumeVersion v){List<String> skills=v.getSkillsText().isBlank()?List.of():Arrays.stream(v.getSkillsText().split(",")).map(String::trim).filter(s->!s.isBlank()).toList();return new VersionResponse(v.getId(),v.getVersionNumber(),v.getOriginalFileName(),v.getContentType(),v.getExtractedEmail(),v.getExtractedPhone(),skills,v.isReviewed(),v.getCreatedAt(),v.getExtractedText());}
    private String safeFilename(String name){if(name==null||name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The uploaded file needs a name.");String basename=name.replace('\\','/');basename=basename.substring(basename.lastIndexOf('/')+1).replaceAll("[\\p{Cntrl}]","").trim();if(basename.length()>255)basename=basename.substring(basename.length()-255);return basename;}
    private String extension(String name){int dot=name.lastIndexOf('.');return dot<0?"":name.substring(dot+1).toLowerCase(Locale.ROOT);}
    private String cleanTitle(String title,String filename){String result=title==null||title.isBlank()?filename.replaceFirst("(?i)\\.(pdf|docx)$",""):title.trim();if(result.length()>160)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Resume title must be 160 characters or fewer.");return result;}
    private boolean startsWith(byte[] data,String header){return data.length>=header.length()&&Arrays.equals(Arrays.copyOf(data,header.length()),header.getBytes(StandardCharsets.US_ASCII));}
    private String sha256(byte[] bytes){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(Exception ex){throw new IllegalStateException(ex);}}
    private String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
    public record DownloadFile(java.nio.file.Path path,String filename,String contentType){}
}
