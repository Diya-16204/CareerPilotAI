package com.careerpilot.jobs;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.*;

public final class JobDtos {
    private JobDtos() {}
    public record JobResponse(UUID id,String title,String company,String location,String employmentType,String description,
                              List<String> skills,String source,boolean sample,boolean saved) {}
    public record ImportRequest(@NotBlank @Size(max=160) String title,@NotBlank @Size(max=160) String company,@NotBlank @Size(max=160) String location,
            @NotBlank @Pattern(regexp="(?i)FULL_TIME|PART_TIME|CONTRACT|INTERNSHIP|TEMPORARY") String employmentType,
            @NotBlank @Size(max=10000) String description,@NotNull @Size(max=40) List<@NotBlank @Size(max=80) String> skills){}
    public record PageResponse<T>(List<T> items,int page,int size,long totalItems,int totalPages) {}
    public static <T,R> PageResponse<R> map(org.springframework.data.domain.Page<T> page,java.util.function.Function<T,R> mapper){
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),page.getNumber(),page.getSize(),page.getTotalElements(),page.getTotalPages());
    }
}
