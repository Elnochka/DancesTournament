package com.dances.service;

import com.dances.entity.*;
import com.dances.entity.dto.*;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.repository.*;
import com.dances.service.impl.MarkServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkServiceImplTest {

    @Mock
    private MarkRepository markRepository;

    @Mock
    private ParticipantTournamentRepository participantRepository;

    @Mock
    private CategoryTournamentRepository categoryRepository;

    @Mock
    private JudgeRepository judgeRepository;

    @Mock
    private FinalMarkRepository finalMarkRepository;

    @Mock
    private DanceRepository danceRepository;

    @InjectMocks
    private MarkServiceImpl markService;

    @Test
    void getAll_shouldReturnEmptyList() {

        when(markRepository.findAll())
                .thenReturn(List.of());

        List<MarkDto> result = markService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(markRepository).findAll();
    }

    @Test
    void create_shouldCreateMark() {

        MarkRequestDto dto = mock(MarkRequestDto.class);

        when(dto.getParticipantTournamentId()).thenReturn(10L);
        when(dto.getCategoryTournamentId()).thenReturn(20L);
        when(dto.getJudgeId()).thenReturn(30L);
        when(dto.getRound()).thenReturn(RoundEnum.QUALIFICATION);
        when(dto.isPassed()).thenReturn(true);

        ParticipantTournament participant =
                new ParticipantTournament();
        participant.setId(10L);

        CategoryTournament category =
                new CategoryTournament();
        category.setId(20L);

        Judge judge = new Judge();
        judge.setId(30L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(20L))
                .thenReturn(Optional.of(category));

        when(judgeRepository.findById(30L))
                .thenReturn(Optional.of(judge));

        when(markRepository.save(any(Mark.class)))
                .thenAnswer(invocation -> {
                    Mark mark = invocation.getArgument(0);
                    mark.setId(100L);
                    return mark;
                });

        MarkDto result = markService.create(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertTrue(result.isPassed());

        verify(markRepository).save(any(Mark.class));
    }

    @Test
    void create_shouldThrowWhenParticipantNotFound() {

        MarkRequestDto dto = mock(MarkRequestDto.class);

        when(dto.getParticipantTournamentId()).thenReturn(10L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> markService.create(dto)
        );

        verify(markRepository, never()).save(any());
        verify(categoryRepository, never()).findById(any());
        verify(judgeRepository, never()).findById(any());
    }

    @Test
    void create_shouldThrowWhenCategoryNotFound() {

        MarkRequestDto dto = mock(MarkRequestDto.class);

        when(dto.getParticipantTournamentId()).thenReturn(10L);
        when(dto.getCategoryTournamentId()).thenReturn(20L);

        ParticipantTournament participant =
                new ParticipantTournament();
        participant.setId(10L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> markService.create(dto)
        );

        verify(markRepository, never()).save(any());
        verify(judgeRepository, never()).findById(any());
    }

    @Test
    void create_shouldThrowWhenJudgeNotFound() {

        MarkRequestDto dto = mock(MarkRequestDto.class);

        when(dto.getParticipantTournamentId()).thenReturn(10L);
        when(dto.getCategoryTournamentId()).thenReturn(20L);
        when(dto.getJudgeId()).thenReturn(30L);

        ParticipantTournament participant =
                new ParticipantTournament();
        participant.setId(10L);

        CategoryTournament category =
                new CategoryTournament();
        category.setId(20L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(20L))
                .thenReturn(Optional.of(category));

        when(judgeRepository.findById(30L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> markService.create(dto)
        );

        verify(markRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldUpdateMark() {

        Mark existing = new Mark();
        existing.setId(1L);
        existing.setPassed(false);
        existing.setLocked(false);

        MarkPatchDto dto = mock(MarkPatchDto.class);

        when(dto.isPassed()).thenReturn(true);
        when(dto.getRound()).thenReturn(null);
        when(dto.getParticipantTournamentId()).thenReturn(null);
        when(dto.getJudgeId()).thenReturn(null);
        when(dto.getCategoryTournamentId()).thenReturn(null);
        when(dto.getDanceId()).thenReturn(null);

        when(markRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(markRepository.save(existing))
                .thenReturn(existing);

        MarkDto result = markService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertTrue(existing.isPassed());

        verify(markRepository).save(existing);
    }

    @Test
    void patchUpdate_shouldThrowWhenLocked() {

        Mark existing = new Mark();
        existing.setId(1L);
        existing.setLocked(true);

        MarkPatchDto dto = mock(MarkPatchDto.class);

        when(markRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(
                IllegalStateException.class,
                () -> markService.patchUpdate(1L, dto)
        );

        verify(markRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenMarkNotFound() {

        when(markRepository.findById(1L))
                .thenReturn(Optional.empty());

        MarkPatchDto dto = mock(MarkPatchDto.class);

        assertThrows(
                NotFoundException.class,
                () -> markService.patchUpdate(1L, dto)
        );

        verify(markRepository, never()).save(any());
    }

    @Test
    void updatePassed_shouldUpdatePassed() {

        Mark existing = new Mark();
        existing.setId(1L);
        existing.setPassed(false);
        existing.setLocked(false);

        MarkPatchJudgeDto dto = mock(MarkPatchJudgeDto.class);

        when(dto.isPassed()).thenReturn(true);

        when(markRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(markRepository.save(existing))
                .thenReturn(existing);

        MarkDto result = markService.updatePassed(1L, dto);

        assertNotNull(result);
        assertTrue(existing.isPassed());

        verify(markRepository).save(existing);
    }

    @Test
    void updatePassed_shouldThrowWhenLocked() {

        Mark existing = new Mark();
        existing.setId(1L);
        existing.setLocked(true);

        MarkPatchJudgeDto dto = mock(MarkPatchJudgeDto.class);

        when(markRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(
                IllegalStateException.class,
                () -> markService.updatePassed(1L, dto)
        );

        verify(markRepository, never()).save(any());
    }

    @Test
    void getMarks_shouldReturnMarksForCategoryAndRound() {

        Mark mark = new Mark();
        mark.setId(1L);

        when(markRepository.findByCategoryTournamentIdAndRound(
                10L,
                RoundEnum.QUALIFICATION
        )).thenReturn(List.of(mark));

        List<MarkDto> result =
                markService.getMarks(10L, RoundEnum.QUALIFICATION);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());

        verify(markRepository)
                .findByCategoryTournamentIdAndRound(
                        10L,
                        RoundEnum.QUALIFICATION
                );
    }

    @Test
    void getMarksForJudgeName_shouldReturnJudgeMarks() {

        Mark mark1 = new Mark();
        mark1.setId(1L);

        Mark mark2 = new Mark();
        mark2.setId(2L);

        when(markRepository.findByJudge_NameJudge("Judge 1"))
                .thenReturn(List.of(mark1, mark2));

        List<MarkDto> result =
                markService.getMarksForJudgeName("Judge 1");

        assertEquals(2, result.size());

        verify(markRepository)
                .findByJudge_NameJudge("Judge 1");
    }

    @Test
    void validateNonSocialMarks_shouldThrowWhenDanceIsNull() {
        Mark mark = mock(Mark.class);

        when(mark.getDance()).thenReturn(null);
        when(mark.getId()).thenReturn(1L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> markService.validateNonSocialMarks(List.of(mark))
        );

        assertEquals(
                "Dance is required for non-social mark: 1",
                exception.getMessage()
        );
    }

    @Test
    void validateNonSocialMarks_shouldThrowWhenDuplicateExists() {
        Mark firstMark = mock(Mark.class);
        Mark secondMark = mock(Mark.class);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        Dance dance = new Dance();
        dance.setId(30L);

        for (Mark mark : List.of(firstMark, secondMark)) {
            when(mark.getDance()).thenReturn(dance);
            when(mark.getParticipantTournament()).thenReturn(participant);
            when(mark.getJudge()).thenReturn(judge);
        }

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> markService.validateNonSocialMarks(
                        List.of(firstMark, secondMark)
                )
        );

        assertEquals(
                "Duplicate mark for participant=10, judge=20, dance=30",
                exception.getMessage()
        );
    }

    @Test
    void validateNonSocialMarks_shouldAllowDifferentDances() {
        Mark firstMark = mock(Mark.class);
        Mark secondMark = mock(Mark.class);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        Dance firstDance = new Dance();
        firstDance.setId(30L);

        Dance secondDance = new Dance();
        secondDance.setId(40L);

        when(firstMark.getDance()).thenReturn(firstDance);
        when(firstMark.getParticipantTournament()).thenReturn(participant);
        when(firstMark.getJudge()).thenReturn(judge);

        when(secondMark.getDance()).thenReturn(secondDance);
        when(secondMark.getParticipantTournament()).thenReturn(participant);
        when(secondMark.getJudge()).thenReturn(judge);

        assertDoesNotThrow(() ->
                markService.validateNonSocialMarks(
                        List.of(firstMark, secondMark)
                )
        );
    }

    @Test
    void createMarks_shouldCreateSocialRoundMarksWithoutDance() {
        CategoryTournament category = mock(CategoryTournament.class);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        markService.createMarks(
                category,
                RoundEnum.QUARTER_FINAL, // заміни на наявний у твоєму RoundEnum звичайний раунд
                List.of(participantDto),
                List.of(judge),
                true
        );

        ArgumentCaptor<Mark> captor =
                ArgumentCaptor.forClass(Mark.class);

        verify(markRepository).save(captor.capture());

        Mark savedMark = captor.getValue();

        assertEquals(judge, savedMark.getJudge());
        assertEquals(participant, savedMark.getParticipantTournament());
        assertEquals(category, savedMark.getCategoryTournament());
        assertEquals(RoundEnum.QUARTER_FINAL, savedMark.getRound());
        assertNull(savedMark.getDance());
        assertFalse(savedMark.isPassed());
        assertFalse(savedMark.isLocked());

        verifyNoInteractions(danceRepository);
    }

    @Test
    void createMarks_shouldCreateRoundMarksForEachDance() {
        CategoryTournament category = mock(CategoryTournament.class);

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(5L);

        when(category.getKindOfDance()).thenReturn(kindOfDance);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        Dance firstDance = new Dance();
        firstDance.setId(30L);

        Dance secondDance = new Dance();
        secondDance.setId(40L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(danceRepository.findByKindOfDanceId(5L))
                .thenReturn(List.of(firstDance, secondDance));

        markService.createMarks(
                category,
                RoundEnum.QUARTER_FINAL,
                List.of(participantDto),
                List.of(judge),
                false
        );

        ArgumentCaptor<Mark> captor =
                ArgumentCaptor.forClass(Mark.class);

        verify(markRepository, times(2)).save(captor.capture());

        List<Mark> savedMarks = captor.getAllValues();

        assertEquals(30L, savedMarks.get(0).getDance().getId());
        assertEquals(40L, savedMarks.get(1).getDance().getId());

        for (Mark savedMark : savedMarks) {
            assertEquals(judge, savedMark.getJudge());
            assertEquals(participant, savedMark.getParticipantTournament());
            assertEquals(category, savedMark.getCategoryTournament());
            assertEquals(RoundEnum.QUARTER_FINAL, savedMark.getRound());
            assertFalse(savedMark.isPassed());
            assertFalse(savedMark.isLocked());
        }
    }

    @Test
    void createMarks_shouldCreateSocialFinalMarksWithoutDance() {
        CategoryTournament category = mock(CategoryTournament.class);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        markService.createMarks(
                category,
                RoundEnum.FINAL,
                List.of(participantDto),
                List.of(judge),
                true
        );

        ArgumentCaptor<FinalMark> captor =
                ArgumentCaptor.forClass(FinalMark.class);

        verify(finalMarkRepository).save(captor.capture());

        FinalMark savedMark = captor.getValue();

        assertEquals(judge, savedMark.getJudge());
        assertEquals(participant, savedMark.getParticipantTournament());
        assertEquals(category, savedMark.getCategoryTournament());
        assertEquals(0, savedMark.getPlace());
        assertFalse(savedMark.isLocked());
        assertNull(savedMark.getDance());

        verifyNoInteractions(danceRepository);
        verifyNoInteractions(markRepository);
    }

    @Test
    void createMarks_shouldCreateFinalMarksForEachDance() {
        CategoryTournament category = mock(CategoryTournament.class);

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(5L);

        when(category.getKindOfDance()).thenReturn(kindOfDance);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        Dance firstDance = new Dance();
        firstDance.setId(30L);

        Dance secondDance = new Dance();
        secondDance.setId(40L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(danceRepository.findByKindOfDanceId(5L))
                .thenReturn(List.of(firstDance, secondDance));

        markService.createMarks(
                category,
                RoundEnum.FINAL,
                List.of(participantDto),
                List.of(judge),
                false
        );

        ArgumentCaptor<FinalMark> captor =
                ArgumentCaptor.forClass(FinalMark.class);

        verify(finalMarkRepository, times(2)).save(captor.capture());

        List<FinalMark> savedMarks = captor.getAllValues();

        assertEquals(30L, savedMarks.get(0).getDance().getId());
        assertEquals(40L, savedMarks.get(1).getDance().getId());

        for (FinalMark savedMark : savedMarks) {
            assertEquals(judge, savedMark.getJudge());
            assertEquals(participant, savedMark.getParticipantTournament());
            assertEquals(category, savedMark.getCategoryTournament());
            assertEquals(0, savedMark.getPlace());
            assertFalse(savedMark.isLocked());
        }

        verifyNoInteractions(markRepository);
    }

    @Test
    void createMarks_shouldThrowWhenParticipantNotFound() {
        CategoryTournament category = mock(CategoryTournament.class);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> markService.createMarks(
                        category,
                        RoundEnum.FINAL,
                        List.of(participantDto),
                        List.of(judge),
                        true
                )
        );

        assertEquals(
                "ParticipantTournament not found: 10",
                exception.getMessage()
        );

        verifyNoInteractions(markRepository);
        verifyNoInteractions(finalMarkRepository);
    }

    @Test
    void createMarks_shouldCreateMarksForEachParticipantAndJudge() {
        CategoryTournament category = mock(CategoryTournament.class);

        ParticipantTournamentDto firstDto = new ParticipantTournamentDto();
        firstDto.setId(10L);

        ParticipantTournamentDto secondDto = new ParticipantTournamentDto();
        secondDto.setId(11L);

        ParticipantTournament firstParticipant = new ParticipantTournament();
        firstParticipant.setId(10L);

        ParticipantTournament secondParticipant = new ParticipantTournament();
        secondParticipant.setId(11L);

        Judge firstJudge = new Judge();
        firstJudge.setId(20L);

        Judge secondJudge = new Judge();
        secondJudge.setId(21L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(firstParticipant));

        when(participantRepository.findById(11L))
                .thenReturn(Optional.of(secondParticipant));

        markService.createMarks(
                category,
                RoundEnum.QUARTER_FINAL,
                List.of(firstDto, secondDto),
                List.of(firstJudge, secondJudge),
                true
        );

        ArgumentCaptor<Mark> captor =
                ArgumentCaptor.forClass(Mark.class);

        verify(markRepository, times(4)).save(captor.capture());

        List<Mark> savedMarks = captor.getAllValues();

        assertEquals(4, savedMarks.size());

        assertEquals(
                Set.of(10L, 11L),
                savedMarks.stream()
                        .map(mark -> mark.getParticipantTournament().getId())
                        .collect(Collectors.toSet())
        );

        assertEquals(
                Set.of(20L, 21L),
                savedMarks.stream()
                        .map(mark -> mark.getJudge().getId())
                        .collect(Collectors.toSet())
        );

        assertTrue(savedMarks.stream().allMatch(mark ->
                mark.getCategoryTournament().equals(category)
                        && mark.getRound() == RoundEnum.QUARTER_FINAL
                        && mark.getDance() == null
                        && !mark.isPassed()
                        && !mark.isLocked()
        ));
    }

    @Test
    void createMarks_shouldNotCreateRoundMarksWhenNoDancesExist() {
        CategoryTournament category = mock(CategoryTournament.class);

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(5L);

        when(category.getKindOfDance()).thenReturn(kindOfDance);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(danceRepository.findByKindOfDanceId(5L))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                markService.createMarks(
                        category,
                        RoundEnum.QUARTER_FINAL,
                        List.of(participantDto),
                        List.of(judge),
                        false
                )
        );

        verify(markRepository, never()).save(any());
        verifyNoInteractions(finalMarkRepository);
    }

    @Test
    void createMarks_shouldNotCreateFinalMarksWhenNoDancesExist() {
        CategoryTournament category = mock(CategoryTournament.class);

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(5L);

        when(category.getKindOfDance()).thenReturn(kindOfDance);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(danceRepository.findByKindOfDanceId(5L))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                markService.createMarks(
                        category,
                        RoundEnum.FINAL,
                        List.of(participantDto),
                        List.of(judge),
                        false
                )
        );

        verify(finalMarkRepository, never()).save(any());
        verifyNoInteractions(markRepository);
    }

    @Test
    void createMarks_shouldCreateRoundMarksForEachParticipantJudgeAndDance() {
        CategoryTournament category = mock(CategoryTournament.class);

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(5L);
        when(category.getKindOfDance()).thenReturn(kindOfDance);

        ParticipantTournamentDto firstDto = new ParticipantTournamentDto();
        firstDto.setId(10L);

        ParticipantTournamentDto secondDto = new ParticipantTournamentDto();
        secondDto.setId(11L);

        ParticipantTournament firstParticipant = new ParticipantTournament();
        firstParticipant.setId(10L);

        ParticipantTournament secondParticipant = new ParticipantTournament();
        secondParticipant.setId(11L);

        Judge firstJudge = new Judge();
        firstJudge.setId(20L);

        Judge secondJudge = new Judge();
        secondJudge.setId(21L);

        Dance firstDance = new Dance();
        firstDance.setId(30L);

        Dance secondDance = new Dance();
        secondDance.setId(40L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(firstParticipant));
        when(participantRepository.findById(11L))
                .thenReturn(Optional.of(secondParticipant));

        when(danceRepository.findByKindOfDanceId(5L))
                .thenReturn(List.of(firstDance, secondDance));

        markService.createMarks(
                category,
                RoundEnum.QUARTER_FINAL,
                List.of(firstDto, secondDto),
                List.of(firstJudge, secondJudge),
                false
        );

        ArgumentCaptor<Mark> captor =
                ArgumentCaptor.forClass(Mark.class);

        verify(markRepository, times(8)).save(captor.capture());

        List<Mark> savedMarks = captor.getAllValues();

        assertEquals(8, savedMarks.size());

        assertEquals(
                Set.of(10L, 11L),
                savedMarks.stream()
                        .map(mark -> mark.getParticipantTournament().getId())
                        .collect(Collectors.toSet())
        );

        assertEquals(
                Set.of(20L, 21L),
                savedMarks.stream()
                        .map(mark -> mark.getJudge().getId())
                        .collect(Collectors.toSet())
        );

        assertEquals(
                Set.of(30L, 40L),
                savedMarks.stream()
                        .map(mark -> mark.getDance().getId())
                        .collect(Collectors.toSet())
        );

        assertTrue(savedMarks.stream().allMatch(mark ->
                mark.getCategoryTournament().equals(category)
                        && mark.getRound() == RoundEnum.QUARTER_FINAL
                        && !mark.isPassed()
                        && !mark.isLocked()
        ));
    }

    @Test
    void createMarks_shouldNotCreateMarksWhenParticipantsListIsEmpty() {
        CategoryTournament category = mock(CategoryTournament.class);

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(5L);

        when(category.getKindOfDance()).thenReturn(kindOfDance);

        when(danceRepository.findByKindOfDanceId(5L))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                markService.createMarks(
                        category,
                        RoundEnum.FINAL,
                        Collections.emptyList(),
                        List.of(new Judge()),
                        false
                )
        );

        verifyNoInteractions(participantRepository);
        verify(danceRepository).findByKindOfDanceId(5L);
        verifyNoInteractions(markRepository);
        verifyNoInteractions(finalMarkRepository);
    }

    @Test
    void createMarks_shouldNotCreateMarksWhenJudgesListIsEmpty() {
        CategoryTournament category = mock(CategoryTournament.class);

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(5L);

        when(category.getKindOfDance()).thenReturn(kindOfDance);

        ParticipantTournamentDto participantDto =
                new ParticipantTournamentDto();
        participantDto.setId(10L);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(danceRepository.findByKindOfDanceId(5L))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                markService.createMarks(
                        category,
                        RoundEnum.FINAL,
                        List.of(participantDto),
                        Collections.emptyList(),
                        false
                )
        );

        verify(participantRepository).findById(10L);
        verifyNoInteractions(markRepository);
        verifyNoInteractions(finalMarkRepository);
    }

    @Test
    void validateNonSocialMarks_shouldAcceptEmptyList() {
        assertDoesNotThrow(() ->
                markService.validateNonSocialMarks(Collections.emptyList())
        );
    }

    @Test
    void validateNonSocialMarks_shouldAcceptSingleValidMark() {
        Mark mark = mock(Mark.class);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Judge judge = new Judge();
        judge.setId(20L);

        Dance dance = new Dance();
        dance.setId(30L);

        when(mark.getDance()).thenReturn(dance);
        when(mark.getParticipantTournament()).thenReturn(participant);
        when(mark.getJudge()).thenReturn(judge);

        assertDoesNotThrow(() ->
                markService.validateNonSocialMarks(List.of(mark))
        );
    }

    @Test
    void validateNonSocialMarks_shouldAllowDifferentJudges() {
        Mark firstMark = mock(Mark.class);
        Mark secondMark = mock(Mark.class);

        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(10L);

        Dance dance = new Dance();
        dance.setId(30L);

        Judge firstJudge = new Judge();
        firstJudge.setId(20L);

        Judge secondJudge = new Judge();
        secondJudge.setId(21L);

        when(firstMark.getDance()).thenReturn(dance);
        when(firstMark.getParticipantTournament()).thenReturn(participant);
        when(firstMark.getJudge()).thenReturn(firstJudge);

        when(secondMark.getDance()).thenReturn(dance);
        when(secondMark.getParticipantTournament()).thenReturn(participant);
        when(secondMark.getJudge()).thenReturn(secondJudge);

        assertDoesNotThrow(() ->
                markService.validateNonSocialMarks(
                        List.of(firstMark, secondMark)
                )
        );
    }

    @Test
    void validateNonSocialMarks_shouldAllowDifferentParticipants() {
        Mark firstMark = mock(Mark.class);
        Mark secondMark = mock(Mark.class);

        ParticipantTournament firstParticipant = new ParticipantTournament();
        firstParticipant.setId(10L);

        ParticipantTournament secondParticipant = new ParticipantTournament();
        secondParticipant.setId(11L);

        Judge judge = new Judge();
        judge.setId(20L);

        Dance dance = new Dance();
        dance.setId(30L);

        when(firstMark.getDance()).thenReturn(dance);
        when(firstMark.getParticipantTournament()).thenReturn(firstParticipant);
        when(firstMark.getJudge()).thenReturn(judge);

        when(secondMark.getDance()).thenReturn(dance);
        when(secondMark.getParticipantTournament()).thenReturn(secondParticipant);
        when(secondMark.getJudge()).thenReturn(judge);

        assertDoesNotThrow(() ->
                markService.validateNonSocialMarks(
                        List.of(firstMark, secondMark)
                )
        );
    }

}
