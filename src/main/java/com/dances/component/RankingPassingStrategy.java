package com.dances.component;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Mark;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.dto.ParticipantTournamentDto;
import com.dances.service.MarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RankingPassingStrategy implements PassingStrategy {

    private final MarkService markService;

    @Override
    public boolean supports(CategoryTournament category) {

        String name = category.getNameCategory();

        return name == null
                || !name.toLowerCase(Locale.ROOT).contains("social");
    }

    @Override
    public List<ParticipantTournamentDto> calculate(List<Mark> marks, int couplesToNextRound) {

        if (couplesToNextRound <= 0) {
            throw new IllegalArgumentException(
                    "couplesToNextRound must be greater than zero"
            );
        }

        markService.validateNonSocialMarks(marks);

        Map<ParticipantTournament, Long> counts = marks.stream()
                .filter(Mark::isPassed)
                .collect(Collectors.groupingBy(
                        Mark::getParticipantTournament,
                        Collectors.counting()
                ));

        List<Map.Entry<ParticipantTournament, Long>> ranking = counts.entrySet()
                        .stream()
                        .sorted(Map.Entry.<ParticipantTournament, Long> comparingByValue().reversed())
                        .toList();

        if (ranking.isEmpty()) {
            return List.of();
        }

        int limit = Math.min(
                couplesToNextRound,
                ranking.size()
        );

        long cutoff = ranking.get(limit - 1).getValue();

        return ranking.stream()
                .filter(entry -> entry.getValue() >= cutoff)
                .map(Map.Entry::getKey)
                .map(ParticipantTournamentDto::fromEntity)
                .toList();
    }

}
