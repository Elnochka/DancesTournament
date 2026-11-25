package com.dances.component;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Mark;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.dto.ParticipantTournamentDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SocialPassingStrategyTest {

    private final SocialPassingStrategy strategy = new SocialPassingStrategy();

    // ---------- supports() ----------

    @Test
    void supports_shouldReturnTrue_whenCategoryContainsSocial() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn("Social Dance");

        assertTrue(strategy.supports(category));
    }

    @Test
    void supports_shouldIgnoreCase() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn("SOCIAL DANCE");

        assertTrue(strategy.supports(category));
    }

    @Test
    void supports_shouldReturnFalse_whenCategoryIsNotSocial() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn("Standard");

        assertFalse(strategy.supports(category));
    }

    @Test
    void supports_shouldReturnFalse_whenCategoryNameIsNull() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn(null);

        assertFalse(strategy.supports(category));
    }


    // ---------- calculate() ----------

    @Test
    void calculate_shouldReturnEmptyList_whenMarksAreEmpty() {
        List<ParticipantTournamentDto> result =
                strategy.calculate(List.of(), 2);

        assertTrue(result.isEmpty());
    }

    @Test
    void calculate_shouldPassParticipant_whenMoreThanHalfMarksArePassed() {
        ParticipantTournament participant = createParticipant(1L);

        List<Mark> marks = List.of(
                createMark(participant, true),
                createMark(participant, true),
                createMark(participant, false)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        assertEquals(
                List.of(ParticipantTournamentDto.fromEntity(participant)),
                result
        );
    }

    @Test
    void calculate_shouldNotPassParticipant_whenExactlyHalfMarksArePassed() {

        ParticipantTournament participant = createParticipant();

        List<Mark> marks = List.of(
                createMark(participant, true),
                createMark(participant, false)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        assertTrue(result.isEmpty());

    }

    @Test
    void calculate_shouldNotPassParticipant_whenLessThanHalfMarksArePassed() {

        ParticipantTournament participant = createParticipant();

        List<Mark> marks = List.of(
                createMark(participant, true),
                createMark(participant, false),
                createMark(participant, false)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        assertTrue(result.isEmpty());

    }

    @Test
    void calculate_shouldPassAllParticipantsWhoHaveMoreThanHalfPassedMarks() {

        ParticipantTournament participant1 = createParticipant();
        ParticipantTournament participant2 = createParticipant();
        ParticipantTournament participant3 = createParticipant();

        when(participant1.getId()).thenReturn(1L);
        when(participant2.getId()).thenReturn(2L);

        List<Mark> marks = List.of(
                // participant1: 3/3
                createMark(participant1, true),
                createMark(participant1, true),
                createMark(participant1, true),

                // participant2: 2/3
                createMark(participant2, true),
                createMark(participant2, true),
                createMark(participant2, false),

                // participant3: 1/3
                createMark(participant3, true),
                createMark(participant3, false),
                createMark(participant3, false)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        Set<ParticipantTournamentDto> expected = Set.of(
                ParticipantTournamentDto.fromEntity(participant1),
                ParticipantTournamentDto.fromEntity(participant2)
        );

        assertEquals(expected, new HashSet<>(result));
        assertEquals(2, result.size());

    }

    @Test
    void calculate_shouldNotPassAnyone_whenAllMarksAreFalse() {

        ParticipantTournament participant1 = createParticipant();
        ParticipantTournament participant2 = createParticipant();

        List<Mark> marks = List.of(
                createMark(participant1, false),
                createMark(participant1, false),
                createMark(participant2, false),
                createMark(participant2, false)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        assertTrue(result.isEmpty());

    }


    // ---------- helpers ----------

    private ParticipantTournament createParticipant(Long id) {
        ParticipantTournament participant =
                mock(ParticipantTournament.class);

        when(participant.getId()).thenReturn(id);

        return participant;
    }

    private ParticipantTournament createParticipant() {
        return mock(ParticipantTournament.class);
    }

    private Mark createMark(
            ParticipantTournament participant,
            boolean passed
    ) {
        Mark mark = mock(Mark.class);

        when(mark.getParticipantTournament()).thenReturn(participant);
        when(mark.isPassed()).thenReturn(passed);

        return mark;
    }

}
