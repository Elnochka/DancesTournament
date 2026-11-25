package com.dances.component;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Mark;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.dto.ParticipantTournamentDto;
import com.dances.service.MarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RankingPassingStrategyTest {

    @Mock
    private MarkService markService;

    private RankingPassingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RankingPassingStrategy(markService);
    }

    // ------------------------------------------------
    // supports()
    // ------------------------------------------------

    @Test
    void supports_shouldReturnTrue_whenCategoryNameIsNull() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn(null);

        assertTrue(strategy.supports(category));
    }

    @Test
    void supports_shouldReturnTrue_whenCategoryIsNotSocial() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn("Standard");

        assertTrue(strategy.supports(category));
    }

    @Test
    void supports_shouldReturnFalse_whenCategoryIsSocial() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn("Social Dance");

        assertFalse(strategy.supports(category));
    }

    @Test
    void supports_shouldIgnoreCase_whenCheckingSocialCategory() {
        CategoryTournament category = mock(CategoryTournament.class);

        when(category.getNameCategory()).thenReturn("SOCIAL");

        assertFalse(strategy.supports(category));
    }

    // ------------------------------------------------
    // calculate()
    // ------------------------------------------------

    @Test
    void calculate_shouldThrowException_whenCouplesToNextRoundIsZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.calculate(List.of(), 0)
        );

        verifyNoInteractions(markService);
    }

    @Test
    void calculate_shouldThrowException_whenCouplesToNextRoundIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.calculate(List.of(), -1)
        );

        verifyNoInteractions(markService);
    }

    @Test
    void calculate_shouldReturnEmptyList_whenMarksAreEmpty() {
        List<ParticipantTournamentDto> result =
                strategy.calculate(List.of(), 3);

        assertTrue(result.isEmpty());

        verify(markService).validateNonSocialMarks(List.of());
    }

    @Test
    void calculate_shouldReturnOnlyPassedParticipants() {
        ParticipantTournament participant1 =
                mock(ParticipantTournament.class);

        ParticipantTournament participant2 =
                mock(ParticipantTournament.class);

        List<Mark> marks = List.of(
                createMark(participant1, true),
                createMark(participant1, true),
                createMark(participant2, false)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        assertEquals(
                List.of(
                        ParticipantTournamentDto.fromEntity(participant1)
                ),
                result
        );

        verify(markService).validateNonSocialMarks(marks);
    }

    @Test
    void calculate_shouldSortParticipantsByPassedMarks() {
        ParticipantTournament participant1 =
                mock(ParticipantTournament.class);

        ParticipantTournament participant2 =
                mock(ParticipantTournament.class);

        ParticipantTournament participant3 =
                mock(ParticipantTournament.class);

        List<Mark> marks = List.of(
                createMark(participant1, true),
                createMark(participant1, true),
                createMark(participant1, true),

                createMark(participant2, true),
                createMark(participant2, true),

                createMark(participant3, true)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        assertEquals(
                List.of(
                        ParticipantTournamentDto.fromEntity(participant1),
                        ParticipantTournamentDto.fromEntity(participant2)
                ),
                result
        );
    }

    @Test
    void calculate_shouldIncludeAllParticipantsWithSameCutoffScore() {

        ParticipantTournament participant1 =
                mock(ParticipantTournament.class);

        ParticipantTournament participant2 =
                mock(ParticipantTournament.class);

        ParticipantTournament participant3 =
                mock(ParticipantTournament.class);

        when(participant1.getId()).thenReturn(1L);
        when(participant2.getId()).thenReturn(2L);
        when(participant3.getId()).thenReturn(3L);

        List<Mark> marks = List.of(
                // 3 passed
                createMark(participant1, true),
                createMark(participant1, true),
                createMark(participant1, true),

                // 2 passed
                createMark(participant2, true),
                createMark(participant2, true),

                // 2 passed
                createMark(participant3, true),
                createMark(participant3, true)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        Set<ParticipantTournamentDto> expected = Set.of(
                ParticipantTournamentDto.fromEntity(participant1),
                ParticipantTournamentDto.fromEntity(participant2),
                ParticipantTournamentDto.fromEntity(participant3)
        );

        assertEquals(expected, new HashSet<>(result));
        assertEquals(3, result.size());

    }

    @Test
    void calculate_shouldReturnAllParticipants_whenLimitIsGreaterThanRankingSize() {

        ParticipantTournament participant1 =
                mock(ParticipantTournament.class);

        ParticipantTournament participant2 =
                mock(ParticipantTournament.class);

        when(participant1.getId()).thenReturn(1L);
        when(participant2.getId()).thenReturn(2L);

        List<Mark> marks = List.of(
                createMark(participant1, true),
                createMark(participant2, true)
        );

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 10);

        assertEquals(2, result.size());

        assertEquals(
                Set.of(
                        ParticipantTournamentDto.fromEntity(participant1),
                        ParticipantTournamentDto.fromEntity(participant2)
                ),
                new HashSet<>(result)
        );

    }

    @Test
    void calculate_shouldNotCountFailedMarks() {

        Mark mark1 = mock(Mark.class);
        Mark mark2 = mock(Mark.class);
        Mark mark3 = mock(Mark.class);

        when(mark1.isPassed()).thenReturn(false);
        when(mark2.isPassed()).thenReturn(false);
        when(mark3.isPassed()).thenReturn(false);

        List<Mark> marks = List.of(mark1, mark2, mark3);

        List<ParticipantTournamentDto> result =
                strategy.calculate(marks, 2);

        assertTrue(result.isEmpty());

        verify(mark1).isPassed();
        verify(mark2).isPassed();
        verify(mark3).isPassed();

    }

    // ------------------------------------------------
    // Helper
    // ------------------------------------------------

    private Mark createMark(ParticipantTournament participant, boolean passed) {
        Mark mark = mock(Mark.class);

        when(mark.isPassed()).thenReturn(passed);

        if (passed) {
            when(mark.getParticipantTournament()).thenReturn(participant);
        }

        return mark;
    }

}
