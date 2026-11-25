package com.dances.service.impl;

import com.dances.component.PassingStrategy;
import com.dances.entity.CategoryTournament;
import com.dances.entity.Mark;
import com.dances.entity.dto.ParticipantTournamentDto;
import com.dances.model.RoundEnum;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.MarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantPassingService {

    private final CategoryTournamentRepository categoryTournamentRepository;
    private final MarkRepository markRepository;
    private final List<PassingStrategy> passingStrategies;

    public List<ParticipantTournamentDto> calculatePassing(
            Long categoryId,
            RoundEnum round,
            int couplesToNextRound
    ) {
        CategoryTournament category = categoryTournamentRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "CategoryTournament not found: " + categoryId
                        )
                );

        List<Mark> marks = markRepository.findByCategoryTournamentIdAndRound(categoryId, round);

        if (marks.isEmpty()) {
            log.info(
                    "No marks found for category={}, round={}",
                    categoryId,
                    round
            );

            return List.of();
        }

        PassingStrategy strategy = passingStrategies.stream()
                .filter(candidate -> candidate.supports(category))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No passing strategy for category: "
                                        + categoryId
                        )
                );

        List<ParticipantTournamentDto> result = strategy.calculate(
                marks,
                couplesToNextRound
        );

        log.info(
                "Passing calculated: category={}, round={}, marks={}, passed={}",
                categoryId,
                round,
                marks.size(),
                result.size()
        );

        return result;
    }
}
