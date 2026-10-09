package com.careerpilot.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MatchSkillRepository extends JpaRepository<MatchSkill,UUID> {}
