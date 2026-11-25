package com.dances.component;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Mark;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.dto.ParticipantTournamentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SocialPassingStrategy implements PassingStrategy{


    @Override
    public boolean supports(CategoryTournament category) {
        String name = category.getNameCategory();

        return name != null
                && name.toLowerCase(Locale.ROOT).contains("social");
    }

    @Override
    public List<ParticipantTournamentDto> calculate(
            List<Mark> marks,
            int couplesToNextRound
    ) {
        Map<ParticipantTournament, List<Mark>> byParticipant = marks.stream()
                .collect(Collectors.groupingBy(Mark::getParticipantTournament));

        List<ParticipantTournamentDto> passed = new ArrayList<>();

        for (Map.Entry<ParticipantTournament, List<Mark>> entry
                : byParticipant.entrySet()) {

            List<Mark> participantMarks = entry.getValue();

            long passedCount = participantMarks.stream()
                    .filter(Mark::isPassed)
                    .count();

            if (passedCount > participantMarks.size() / 2) {
                passed.add(ParticipantTournamentDto.fromEntity(entry.getKey()));
            }
        }

        log.info(
                "Social passing calculated: participants={}, passed={}",
                byParticipant.size(),
                passed.size()
        );

        return passed;
    }
}
