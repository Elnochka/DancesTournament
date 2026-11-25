package com.dances.component;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Mark;
import com.dances.entity.dto.ParticipantTournamentDto;

import java.util.List;

public interface PassingStrategy {

    boolean supports(CategoryTournament category);

    List<ParticipantTournamentDto> calculate(
            List<Mark> marks,
            int couplesToNextRound
    );

}
