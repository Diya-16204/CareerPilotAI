package com.careerpilot.ai;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CoverLetterRepository extends JpaRepository<CoverLetter,UUID> {
    Page<CoverLetter> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId,Pageable pageable);
    Optional<CoverLetter> findByIdAndUser_Id(UUID id,UUID userId);
}

//JpaRepository<CoverLetter, UUID>: Tells Spring Data JPA to target the CoverLetter entity using UUID as its Primary Key data type. Provides out-of-the-box methods like save(), findAll(), findById(), and deleteById().

//Return Type: Page<CoverLetter> — a paginated slice of records along with metadata like total pages and total element count.
//pageable: Specifies requested page number, page size, and execution parameters.
