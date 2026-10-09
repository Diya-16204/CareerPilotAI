package com.careerpilot.matching;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class MatchingServiceTest {
    @Test void computesStableCoverageAndNormalizesAliases(){
        var required=List.of("JavaScript","React","PostgreSQL","Spring Boot");
        var skills=List.of("JS","React.js","Postgres");
        assertThat(MatchingService.deterministicScore(required,skills)).isEqualTo(75);
        assertThat(MatchingService.deterministicScore(required,skills)).isEqualTo(75);
    }
    @Test void returnsZeroWhenJobHasNoStructuredSkills(){assertThat(MatchingService.deterministicScore(List.of(),List.of("Java"))).isZero();}
}
