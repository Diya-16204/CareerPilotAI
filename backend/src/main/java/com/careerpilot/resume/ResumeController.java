package com.careerpilot.resume;

import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
import static com.careerpilot.resume.ResumeDtos.*;

@RestController @RequestMapping("/api/v1/resumes")
public class ResumeController {
    private final ResumeService service;
    public ResumeController(ResumeService service){this.service=service;}

    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> upload(@RequestParam(required=false) UUID resumeId,@RequestParam(required=false) String title,@RequestPart("file") MultipartFile file,@AuthenticationPrincipal UUID owner){
        return ResponseEntity.status(resumeId==null?201:200).body(service.upload(owner,resumeId,title,file));
    }
    @GetMapping public List<ResumeSummary> list(@AuthenticationPrincipal UUID owner){return service.list(owner);}
    @GetMapping("/{id}") public ResumeResponse get(@PathVariable UUID id,@AuthenticationPrincipal UUID owner){return service.get(id,owner);}
    @PutMapping("/{id}/versions/{versionId}/review") public VersionResponse review(@PathVariable UUID id,@PathVariable UUID versionId,@Valid @RequestBody ReviewRequest request,@AuthenticationPrincipal UUID owner){return service.review(id,versionId,owner,request);}
    @GetMapping("/{id}/versions/{versionId}/download") public ResponseEntity<FileSystemResource> download(@PathVariable UUID id,@PathVariable UUID versionId,@AuthenticationPrincipal UUID owner){
        var file=service.download(id,versionId,owner);return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType())).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(file.filename()).build().toString()).body(new FileSystemResource(file.path()));
    }
    @DeleteMapping("/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id,@AuthenticationPrincipal UUID owner){service.delete(id,owner);}
}
