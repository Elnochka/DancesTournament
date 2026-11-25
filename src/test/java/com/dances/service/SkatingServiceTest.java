package com.dances.service;

import com.dances.entity.*;
import com.dances.entity.dto.ParticipantTournamentDto;
import com.dances.entity.dto.SkatingResultDto;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.repository.*;
import com.dances.service.impl.RoundSettingsServiceImpl;
import com.dances.service.impl.SkatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkatingServiceTest {

    @Mock
    private FinalMarkRepository finalMarkRepository;

    @Mock
    private SkatingResultsRepository skatingResultsRepository;

    @Mock
    private MarkRepository markRepository;

    @Mock
    private CategoryTournamentRepository categoryRepository;

    @Mock
    private JudgeRepository judgeRepository;

    @Mock
    private RoundSettingsRepository roundSettingsRepository;

    @Mock
    private RoundSettingsServiceImpl roundSettingsService;

    @Mock
    private MarkService markService;

    @Mock
    private ParticipantTournamentService participantService;

    @Mock
    private FinalMarkService finalMarkService;

    @Mock
    private DanceRepository danceRepository;

    @InjectMocks
    private SkatingService service;

    @Mock
    private CategoryTournament category;

    @Test
    void lockDance_shouldThrowWhenCategoryNotFound() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.lockDance(categoryId, roundId, danceId)
        );

        assertEquals(
                "CategoryTournament not found",
                exception.getMessage()
        );

        verify(categoryRepository).findById(categoryId);

        verifyNoInteractions(
                roundSettingsRepository,
                finalMarkRepository,
                markRepository
        );
    }

    @Test
    void lockDance_shouldThrowWhenRoundSettingsNotFound() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.lockDance(categoryId, roundId, danceId)
        );

        assertEquals("RoundSettings not found", exception.getMessage());

        verify(categoryRepository).findById(categoryId);
        verify(roundSettingsRepository).findById(roundId);

        verifyNoInteractions(
                finalMarkRepository,
                markRepository,
                judgeRepository
        );
    }

    @Test
    void lockDance_shouldNotLockWhenCategoryIsSocial() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;

        RoundSettings roundSettings = mock(RoundSettings.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Social Dance");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getRound()).thenReturn(RoundEnum.FINAL);

        service.lockDance(categoryId, roundId, danceId);

        verify(categoryRepository).findById(categoryId);
        verify(roundSettingsRepository).findById(roundId);

        verifyNoInteractions(finalMarkRepository, markRepository);
    }

    @Test
    void lockDance_shouldThrowWhenFinalMarksHaveUnassignedPlaces() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;

        RoundSettings roundSettings = mock(RoundSettings.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Standard");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getRound()).thenReturn(RoundEnum.FINAL);

        when(finalMarkRepository.existsByCategoryTournamentIdAndDanceIdAndPlace(
                categoryId, danceId, 0
        )).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.lockDance(categoryId, roundId, danceId)
        );

        assertEquals(
                "Cannot lock dance: some final marks have place = 0",
                exception.getMessage()
        );

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndDanceIdAndPlace(categoryId, danceId, 0);

        verify(finalMarkRepository, never()).lockDanceRound(categoryId, danceId);
    }

    @Test
    void lockDance_shouldLockFinalDanceWhenAllPlacesAssigned() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;

        RoundSettings roundSettings = mock(RoundSettings.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Standard");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getRound()).thenReturn(RoundEnum.FINAL);

        when(finalMarkRepository.existsByCategoryTournamentIdAndDanceIdAndPlace(
                categoryId, danceId, 0
        )).thenReturn(false);

        service.lockDance(categoryId, roundId, danceId);

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndDanceIdAndPlace(categoryId, danceId, 0);

        verify(finalMarkRepository).lockDanceRound(categoryId, danceId);

        verifyNoInteractions(markRepository, judgeRepository);
    }

    @Test
    void lockDance_shouldLockDanceWhenAllJudgesPassedRequiredNumberOfCouples() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;
        Long judgeId = 4L;

        int couplesToNextRound = 4;

        RoundEnum round = RoundEnum.SEMI_FINAL;

        RoundSettings roundSettings = mock(RoundSettings.class);
        Judge judge = mock(Judge.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Standard");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getRound()).thenReturn(round);
        when(roundSettings.getCouplesToNextRound()).thenReturn(couplesToNextRound);

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(judge));

        when(judge.getId()).thenReturn(judgeId);

        when(markRepository.countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                categoryId, round, judgeId, danceId
        )).thenReturn((long) couplesToNextRound);

        service.lockDance(categoryId, roundId, danceId);

        verify(markRepository).lockDanceRound(categoryId, round, danceId);

        verify(finalMarkRepository, never())
                .lockDanceRound(anyLong(), anyLong());
    }

    @Test
    void lockDance_shouldThrowWhenJudgePassedLessCouplesThanRequired() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;
        Long judgeId = 4L;

        int couplesToNextRound = 4;
        RoundEnum round = RoundEnum.SEMI_FINAL;

        RoundSettings roundSettings = mock(RoundSettings.class);
        Judge judge = mock(Judge.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Standard");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getRound()).thenReturn(round);
        when(roundSettings.getCouplesToNextRound()).thenReturn(couplesToNextRound);

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(judge));

        when(judge.getId()).thenReturn(judgeId);
        when(judge.getNameJudge()).thenReturn("Judge 1");

        when(markRepository.countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                categoryId, round, judgeId, danceId
        )).thenReturn(3L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.lockDance(categoryId, roundId, danceId)
        );

        assertEquals(
                "Not enough or more than need passed participants. Required: 4, actual: 3, judge: Judge 1",
                exception.getMessage()
        );

        verify(markRepository, never())
                .lockDanceRound(categoryId, round, danceId);

        verifyNoInteractions(finalMarkRepository);
    }

    @Test
    void lockDance_shouldThrowWhenJudgePassedMoreCouplesThanRequired() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long danceId = 3L;
        Long judgeId = 4L;

        int couplesToNextRound = 4;
        RoundEnum round = RoundEnum.SEMI_FINAL;

        RoundSettings roundSettings = mock(RoundSettings.class);
        Judge judge = mock(Judge.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Standard");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getRound()).thenReturn(round);
        when(roundSettings.getCouplesToNextRound()).thenReturn(couplesToNextRound);

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(judge));

        when(judge.getId()).thenReturn(judgeId);
        when(judge.getNameJudge()).thenReturn("Judge 1");

        when(markRepository.countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                categoryId, round, judgeId, danceId
        )).thenReturn(5L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.lockDance(categoryId, roundId, danceId)
        );

        assertEquals(
                "Not enough or more than need passed participants. Required: 4, actual: 5, judge: Judge 1",
                exception.getMessage()
        );

        verify(markRepository, never())
                .lockDanceRound(categoryId, round, danceId);

        verifyNoInteractions(finalMarkRepository);
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldThrowWhenCategoryNotFound() {
        Long categoryId = 1L;
        Long roundId = 2L;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId)
        );

        assertEquals(
                "CategoryTournament not found: " + categoryId,
                exception.getMessage()
        );

        verify(categoryRepository).findById(categoryId);

        verifyNoInteractions(
                roundSettingsRepository,
                judgeRepository,
                danceRepository,
                roundSettingsService,
                participantService,
                markService
        );
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldThrowWhenRoundSettingsNotFound() {
        Long categoryId = 1L;
        Long roundId = 2L;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId)
        );

        assertEquals(
                "RoundSettings not found: " + roundId,
                exception.getMessage()
        );

        verify(categoryRepository).findById(categoryId);
        verify(roundSettingsRepository).findById(roundId);

        verifyNoInteractions(
                judgeRepository,
                danceRepository,
                roundSettingsService,
                participantService,
                markService
        );
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldThrowWhenRoundSettingsBelongsToAnotherCategory() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long anotherCategoryId = 10L;

        RoundSettings roundSettings = mock(RoundSettings.class);
        CategoryTournament anotherCategory = mock(CategoryTournament.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getCategoryTournament())
                .thenReturn(anotherCategory);

        when(anotherCategory.getId()).thenReturn(anotherCategoryId);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId)
        );

        assertEquals(
                "RoundSettings does not belong to category: " + categoryId,
                exception.getMessage()
        );

        verify(categoryRepository).findById(categoryId);
        verify(roundSettingsRepository).findById(roundId);

        verifyNoInteractions(
                judgeRepository,
                danceRepository,
                roundSettingsService,
                participantService,
                markService
        );
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldCreateMarksForSocialCategory() {
        Long categoryId = 1L;
        Long roundId = 2L;

        RoundSettings roundSettings = mock(RoundSettings.class);
        Judge judge = mock(Judge.class);

        List<Judge> judges = List.of(judge);
        List<ParticipantTournamentDto> participants = List.of();

        RoundEnum nextRound = RoundEnum.FINAL;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Social Dance");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getCategoryTournament()).thenReturn(category);
        when(category.getId()).thenReturn(categoryId);

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(judges);

        when(roundSettingsService.resolveNextRound(categoryId, roundSettings))
                .thenReturn(nextRound);

        when(participantService.selectParticipants(category, roundSettings, true))
                .thenReturn(participants);

        service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId);

        verify(danceRepository, never()).findByKindOfDanceId(anyLong());

        verify(roundSettingsService).resolveNextRound(categoryId, roundSettings);

        verify(participantService).selectParticipants(category, roundSettings, true);

        verify(markService).createMarks(category, nextRound, participants, judges, true);
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldThrowWhenNoActiveJudges() {
        Long categoryId = 1L;
        Long roundId = 2L;

        RoundSettings roundSettings = mock(RoundSettings.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getNameCategory()).thenReturn("Social Dance");

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getCategoryTournament()).thenReturn(category);
        when(category.getId()).thenReturn(categoryId);

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId)
        );

        assertEquals("List of the active judges is empty!", exception.getMessage());

        verify(roundSettingsService, never())
                .resolveNextRound(anyLong(), any(RoundSettings.class));

        verifyNoInteractions(participantService, markService);
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldCreateMarksForRegularCategory() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long judgeId = 3L;
        Long kindOfDanceId = 4L;
        Long danceId = 5L;

        int couplesToNextRound = 4;

        RoundEnum currentRound = RoundEnum.SEMI_FINAL;
        RoundEnum nextRound = RoundEnum.FINAL;

        RoundSettings roundSettings = mock(RoundSettings.class);
        Judge judge = mock(Judge.class);
        Dance dance = mock(Dance.class);
        KindOfDance kindOfDance = mock(KindOfDance.class);

        List<Judge> judges = List.of(judge);
        List<Dance> dances = List.of(dance);
        List<ParticipantTournamentDto> participants = List.of();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(categoryId);

        when(category.getNameCategory()).thenReturn("Standard");
        when(category.getKindOfDance()).thenReturn(kindOfDance);
        when(kindOfDance.getId()).thenReturn(kindOfDanceId);

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getCategoryTournament()).thenReturn(category);
        when(roundSettings.getRound()).thenReturn(currentRound);
        when(roundSettings.getCouplesToNextRound()).thenReturn(couplesToNextRound);

        when(danceRepository.findByKindOfDanceId(kindOfDanceId))
                .thenReturn(dances);

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(judges);

        when(judge.getId()).thenReturn(judgeId);

        when(markRepository.countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                categoryId, currentRound, judgeId, danceId
        )).thenReturn(4L);

        when(dance.getId()).thenReturn(danceId);

        when(roundSettingsService.resolveNextRound(categoryId, roundSettings))
                .thenReturn(nextRound);

        when(participantService.selectParticipants(category, roundSettings, false))
                .thenReturn(participants);

        service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId);

        verify(danceRepository).findByKindOfDanceId(kindOfDanceId);

        verify(markRepository)
                .countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                        categoryId, currentRound, judgeId, danceId
                );

        verify(roundSettingsService).resolveNextRound(categoryId, roundSettings);

        verify(participantService).selectParticipants(category, roundSettings, false);

        verify(markService).createMarks(category, nextRound, participants, judges, false);
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldThrowWhenJudgePassedLessCouplesThanRequired() {
        Long categoryId = 1L;
        Long roundId = 2L;
        Long judgeId = 3L;
        Long kindOfDanceId = 4L;
        Long danceId = 5L;

        int couplesToNextRound = 4;

        RoundEnum currentRound = RoundEnum.SEMI_FINAL;

        RoundSettings roundSettings = mock(RoundSettings.class);
        Judge judge = mock(Judge.class);
        Dance dance = mock(Dance.class);
        KindOfDance kindOfDance = mock(KindOfDance.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(categoryId);
        when(category.getNameCategory()).thenReturn("Standard");
        when(category.getKindOfDance()).thenReturn(kindOfDance);
        when(kindOfDance.getId()).thenReturn(kindOfDanceId);

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getCategoryTournament()).thenReturn(category);
        when(roundSettings.getRound()).thenReturn(currentRound);
        when(roundSettings.getCouplesToNextRound()).thenReturn(couplesToNextRound);

        when(danceRepository.findByKindOfDanceId(kindOfDanceId))
                .thenReturn(List.of(dance));

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(judge));

        when(judge.getId()).thenReturn(judgeId);
        when(judge.getNameJudge()).thenReturn("Judge 1");
        when(dance.getId()).thenReturn(danceId);

        when(markRepository.countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                categoryId, currentRound, judgeId, danceId
        )).thenReturn(3L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId)
        );

        assertEquals(
                "Not enough or more than need passed participants. Required: 4, actual: 3, judge: Judge 1",
                exception.getMessage()
        );

        verify(markService, never()).createMarks(
                any(), any(), any(), any(), anyBoolean()
        );

        verifyNoInteractions(roundSettingsService, participantService);
    }

    @Test
    void createMarksOrFinalMarksForCategoryAndRound_shouldCreateMarksWhenNoDancesFound() {
        Long categoryId = 1L;
        Long roundId = 2L;

        RoundEnum nextRound = RoundEnum.FINAL;

        RoundSettings roundSettings = mock(RoundSettings.class);
        Judge judge = mock(Judge.class);

        List<Judge> judges = List.of(judge);
        List<ParticipantTournamentDto> participants = List.of();
        KindOfDance kindOfDance = mock(KindOfDance.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(categoryId);
        when(category.getNameCategory()).thenReturn("Standard");
        when(category.getKindOfDance()).thenReturn(kindOfDance);

        when(kindOfDance.getId()).thenReturn(4L);

        when(roundSettingsRepository.findById(roundId))
                .thenReturn(Optional.of(roundSettings));

        when(roundSettings.getCategoryTournament()).thenReturn(category);

        when(danceRepository.findByKindOfDanceId(4L))
                .thenReturn(Collections.emptyList());

        when(judgeRepository.findAllByCreatedAtBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(judges);

        when(roundSettingsService.resolveNextRound(categoryId, roundSettings))
                .thenReturn(nextRound);

        when(participantService.selectParticipants(category, roundSettings, false))
                .thenReturn(participants);

        service.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundId);

        verify(danceRepository).findByKindOfDanceId(4L);

        verify(markRepository, never())
                .countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
                        anyLong(), any(), anyLong(), anyLong()
                );

        verify(roundSettingsService).resolveNextRound(categoryId, roundSettings);

        verify(participantService).selectParticipants(category, roundSettings, false);

        verify(markService).createMarks(category, nextRound, participants, judges, false);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenNoFinalMarksFound() {
        Long categoryId = 1L;

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(Collections.emptyList());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "No final marks found for category " + categoryId,
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);

        verifyNoInteractions(
                finalMarkService,
                skatingResultsRepository
        );
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenFinalMarksMixSingleAndMultiDanceMarks() {
        Long categoryId = 1L;

        FinalMark markWithDance = mock(FinalMark.class);
        FinalMark markWithoutDance = mock(FinalMark.class);
        Dance dance = mock(Dance.class);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(markWithDance, markWithoutDance));

        when(markWithDance.getDance()).thenReturn(dance);
        when(markWithoutDance.getDance()).thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Final marks cannot mix single-dance and multi-dance marks",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);

        verify(finalMarkRepository, never()).lockRound(anyLong());

        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldCalculateAndSaveSingleDanceResults() {
        Long categoryId = 1L;

        ParticipantTournament participant = mock(ParticipantTournament.class);
        CategoryTournament category = mock(CategoryTournament.class);

        FinalMark mark = mock(FinalMark.class);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark));

        when(mark.getDance()).thenReturn(null);
        when(mark.getParticipantTournament()).thenReturn(participant);
        when(mark.getPlace()).thenReturn(1);

        when(participant.getCategoryTournament()).thenReturn(category);

        when(category.getId()).thenReturn(categoryId);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(1L);
                    return result;
                });

        when(participant.getId()).thenReturn(10L);

        List<SkatingResultDto> result = service.calculateFinalPlaces(categoryId);

        verify(finalMarkRepository).lockRound(categoryId);
        verify(finalMarkService).validateSingleDanceFinalMarks(List.of(mark));

        verify(skatingResultsRepository).deleteByCategoryTournamentId(categoryId);
        verify(skatingResultsRepository).save(any(SkatingResult.class));

        assertEquals(1, result.size());
        assertEquals(BigDecimal.ONE, result.getFirst().getFinalPlace());
    }

    @Test
    void calculateFinalPlaces_shouldAssignAveragePlaceWhenParticipantsAreTied() {
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        CategoryTournament category = mock(CategoryTournament.class);

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);
        FinalMark mark3 = mock(FinalMark.class);
        FinalMark mark4 = mock(FinalMark.class);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark1, mark2, mark3, mark4));

        when(mark1.getDance()).thenReturn(null);
        when(mark2.getDance()).thenReturn(null);
        when(mark3.getDance()).thenReturn(null);
        when(mark4.getDance()).thenReturn(null);

        when(mark1.getParticipantTournament()).thenReturn(participant1);
        when(mark2.getParticipantTournament()).thenReturn(participant1);
        when(mark3.getParticipantTournament()).thenReturn(participant2);
        when(mark4.getParticipantTournament()).thenReturn(participant2);

        when(mark1.getPlace()).thenReturn(1);
        when(mark2.getPlace()).thenReturn(2);
        when(mark3.getPlace()).thenReturn(2);
        when(mark4.getPlace()).thenReturn(1);

        when(participant1.getCategoryTournament()).thenReturn(category);
        when(participant2.getCategoryTournament()).thenReturn(category);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament() == participant1 ? 1L : 2L);
                    return result;
                });

        List<SkatingResultDto> result = service.calculateFinalPlaces(categoryId);

        verify(finalMarkRepository).lockRound(categoryId);
        verify(finalMarkService).validateSingleDanceFinalMarks(
                List.of(mark1, mark2, mark3, mark4)
        );

        verify(skatingResultsRepository, times(2)).save(any(SkatingResult.class));

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("1.5"), result.get(0).getFinalPlace());
        assertEquals(new BigDecimal("1.5"), result.get(1).getFinalPlace());
    }

    @Test
    void calculateFinalPlaces_shouldAssignPlacesWhenThereIsClearWinner() {
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        CategoryTournament category = mock(CategoryTournament.class);

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);
        FinalMark mark3 = mock(FinalMark.class);
        FinalMark mark4 = mock(FinalMark.class);
        FinalMark mark5 = mock(FinalMark.class);
        FinalMark mark6 = mock(FinalMark.class);

        List<FinalMark> marks = List.of(
                mark1, mark2, mark3,
                mark4, mark5, mark6
        );

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(mark1.getDance()).thenReturn(null);
        when(mark2.getDance()).thenReturn(null);
        when(mark3.getDance()).thenReturn(null);
        when(mark4.getDance()).thenReturn(null);
        when(mark5.getDance()).thenReturn(null);
        when(mark6.getDance()).thenReturn(null);

        when(mark1.getParticipantTournament()).thenReturn(participant1);
        when(mark2.getParticipantTournament()).thenReturn(participant1);
        when(mark3.getParticipantTournament()).thenReturn(participant1);

        when(mark4.getParticipantTournament()).thenReturn(participant2);
        when(mark5.getParticipantTournament()).thenReturn(participant2);
        when(mark6.getParticipantTournament()).thenReturn(participant2);

        when(mark1.getPlace()).thenReturn(1);
        when(mark2.getPlace()).thenReturn(1);
        when(mark3.getPlace()).thenReturn(2);

        when(mark4.getPlace()).thenReturn(2);
        when(mark5.getPlace()).thenReturn(2);
        when(mark6.getPlace()).thenReturn(1);

        when(participant1.getCategoryTournament()).thenReturn(category);
        when(participant2.getCategoryTournament()).thenReturn(category);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(
                            result.getParticipantTournament() == participant1 ? 1L : 2L
                    );
                    return result;
                });

        List<SkatingResultDto> result = service.calculateFinalPlaces(categoryId);

        verify(finalMarkRepository).lockRound(categoryId);
        verify(finalMarkService).validateSingleDanceFinalMarks(marks);

        verify(skatingResultsRepository, times(2))
                .save(any(SkatingResult.class));

        assertEquals(2, result.size());
        assertEquals(BigDecimal.ONE, result.get(0).getFinalPlace());
        assertEquals(new BigDecimal("2"), result.get(1).getFinalPlace());
    }

    @Test
    void getResults_shouldReturnEmptyListWhenNoResultsFound() {
        // Arrange
        Long categoryId = 1L;

        when(skatingResultsRepository
                .findByCategoryTournamentIdOrderByFinalPlace(categoryId))
                .thenReturn(List.of());

        // Act
        List<SkatingResultDto> result = service.getResults(categoryId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(skatingResultsRepository)
                .findByCategoryTournamentIdOrderByFinalPlace(categoryId);
    }

    @Test
    void getResults_shouldReturnMappedResults() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        SkatingResult result1 = new SkatingResult();
        result1.setId(10L);
        result1.setParticipantTournament(participant1);
        result1.setFinalPlace(BigDecimal.ONE);

        SkatingResult result2 = new SkatingResult();
        result2.setId(11L);
        result2.setParticipantTournament(participant2);
        result2.setFinalPlace(BigDecimal.valueOf(2));

        when(skatingResultsRepository
                .findByCategoryTournamentIdOrderByFinalPlace(categoryId))
                .thenReturn(List.of(result1, result2));

        // Act
        List<SkatingResultDto> result = service.getResults(categoryId);

        // Assert
        assertEquals(2, result.size());

        assertEquals(10L, result.get(0).getId());
        assertEquals(BigDecimal.ONE, result.get(0).getFinalPlace());

        assertEquals(11L, result.get(1).getId());
        assertEquals(BigDecimal.valueOf(2), result.get(1).getFinalPlace());

        verify(skatingResultsRepository)
                .findByCategoryTournamentIdOrderByFinalPlace(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenMarksMixSingleAndMultiDance() {
        // Arrange
        Long categoryId = 1L;

        FinalMark singleDanceMark = mock(FinalMark.class);
        FinalMark multiDanceMark = mock(FinalMark.class);
        Dance dance = mock(Dance.class);

        when(singleDanceMark.getDance()).thenReturn(null);
        when(multiDanceMark.getDance()).thenReturn(dance);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(singleDanceMark, multiDanceMark));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Final marks cannot mix single-dance and multi-dance marks",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);

        verifyNoInteractions(
                finalMarkService,
                skatingResultsRepository
        );
    }

    @Test
    void calculateFinalPlaces_shouldNotSaveResultsWhenSingleDanceValidationFails() {
        // Arrange
        Long categoryId = 1L;

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);

        List<FinalMark> marks = List.of(mark1, mark2);

        when(mark1.getDance()).thenReturn(null);
        when(mark2.getDance()).thenReturn(null);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        doThrow(new IllegalArgumentException("Invalid final marks"))
                .when(finalMarkService)
                .validateSingleDanceFinalMarks(marks);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals("Invalid final marks", exception.getMessage());

        verify(finalMarkService).validateSingleDanceFinalMarks(marks);
        verify(finalMarkRepository).lockRound(categoryId);

        verifyNoInteractions(skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenParticipantSetsDifferBetweenDances() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        Dance dance1 = mock(Dance.class);
        Dance dance2 = mock(Dance.class);

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);
        FinalMark mark3 = mock(FinalMark.class);

        Judge judge = mock(Judge.class);
        when(judge.getId()).thenReturn(100L);

        when(mark1.getJudge()).thenReturn(judge);
        when(mark2.getJudge()).thenReturn(judge);

        when(mark1.getPlace()).thenReturn(1);
        when(mark2.getPlace()).thenReturn(2);

        when(dance1.getId()).thenReturn(1L);
        when(dance2.getId()).thenReturn(2L);

        when(participant1.getId()).thenReturn(10L);
        when(participant2.getId()).thenReturn(20L);

        when(mark1.getDance()).thenReturn(dance1);
        when(mark2.getDance()).thenReturn(dance1);
        when(mark3.getDance()).thenReturn(dance2);

        when(mark1.getParticipantTournament()).thenReturn(participant1);
        when(mark2.getParticipantTournament()).thenReturn(participant2);
        when(mark3.getParticipantTournament()).thenReturn(participant1);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark1, mark2, mark3));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Different participant sets between dances",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);
        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenJudgeSetsDifferBetweenDances() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        Dance dance1 = mock(Dance.class);
        Dance dance2 = mock(Dance.class);

        Judge judge1 = mock(Judge.class);
        Judge judge2 = mock(Judge.class);

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);
        FinalMark mark3 = mock(FinalMark.class);
        FinalMark mark4 = mock(FinalMark.class);

        when(dance1.getId()).thenReturn(1L);
        when(dance2.getId()).thenReturn(2L);

        when(participant1.getId()).thenReturn(10L);
        when(participant2.getId()).thenReturn(20L);

        when(judge1.getId()).thenReturn(100L);
        when(judge2.getId()).thenReturn(200L);

        when(mark1.getDance()).thenReturn(dance1);
        when(mark2.getDance()).thenReturn(dance1);
        when(mark3.getDance()).thenReturn(dance2);
        when(mark4.getDance()).thenReturn(dance2);

        when(mark1.getParticipantTournament()).thenReturn(participant1);
        when(mark2.getParticipantTournament()).thenReturn(participant2);
        when(mark3.getParticipantTournament()).thenReturn(participant1);
        when(mark4.getParticipantTournament()).thenReturn(participant2);

        when(mark1.getJudge()).thenReturn(judge1);
        when(mark2.getJudge()).thenReturn(judge1);
        when(mark3.getJudge()).thenReturn(judge2);
        when(mark4.getJudge()).thenReturn(judge2);

        when(mark1.getPlace()).thenReturn(1);
        when(mark2.getPlace()).thenReturn(2);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark1, mark2, mark3, mark4));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Different judge sets between dances",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);
        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenJudgeIsMissing() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant = mock(ParticipantTournament.class);
        Dance dance = mock(Dance.class);

        FinalMark mark = mock(FinalMark.class);

        when(dance.getId()).thenReturn(1L);
        when(participant.getId()).thenReturn(10L);

        when(mark.getDance()).thenReturn(dance);
        when(mark.getParticipantTournament()).thenReturn(participant);
        when(mark.getJudge()).thenReturn(null);
        when(mark.getId()).thenReturn(50L);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Judge is missing for final mark 50",
                exception.getMessage()
        );

        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenParticipantHasDuplicateJudgeMarks() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant = mock(ParticipantTournament.class);
        Dance dance = mock(Dance.class);
        Judge judge = mock(Judge.class);

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);

        when(dance.getId()).thenReturn(1L);
        when(participant.getId()).thenReturn(10L);
        when(judge.getId()).thenReturn(100L);

        when(mark1.getDance()).thenReturn(dance);
        when(mark2.getDance()).thenReturn(dance);

        when(mark1.getParticipantTournament()).thenReturn(participant);
        when(mark2.getParticipantTournament()).thenReturn(participant);

        when(mark1.getJudge()).thenReturn(judge);
        when(mark2.getJudge()).thenReturn(judge);

        when(mark1.getPlace()).thenReturn(1);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark1, mark2));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Duplicate judge mark for participant 10 in dance 1",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);
        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenPlaceIsLessThanOne() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant = mock(ParticipantTournament.class);
        Dance dance = mock(Dance.class);
        Judge judge = mock(Judge.class);
        FinalMark mark = mock(FinalMark.class);

        when(dance.getId()).thenReturn(1L);
        when(participant.getId()).thenReturn(10L);
        when(judge.getId()).thenReturn(100L);

        when(mark.getDance()).thenReturn(dance);
        when(mark.getParticipantTournament()).thenReturn(participant);
        when(mark.getJudge()).thenReturn(judge);
        when(mark.getPlace()).thenReturn(0);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Invalid place 0 for participant 10",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);
        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenPlaceExceedsParticipantCount() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        Dance dance = mock(Dance.class);
        Judge judge = mock(Judge.class);

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);

        when(dance.getId()).thenReturn(1L);
        when(participant1.getId()).thenReturn(10L);
        when(participant2.getId()).thenReturn(20L);
        when(judge.getId()).thenReturn(100L);

        when(mark1.getDance()).thenReturn(dance);
        when(mark2.getDance()).thenReturn(dance);

        when(mark1.getParticipantTournament()).thenReturn(participant1);
        when(mark2.getParticipantTournament()).thenReturn(participant2);

        when(mark1.getJudge()).thenReturn(judge);
        when(mark2.getJudge()).thenReturn(judge);

        when(mark1.getPlace()).thenReturn(3);
        when(mark2.getPlace()).thenReturn(2);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark1, mark2));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Invalid place 3 for participant 10",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);
        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldThrowWhenParticipantDoesNotHaveMarksFromAllJudges() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        Dance dance = mock(Dance.class);

        Judge judge1 = mock(Judge.class);
        Judge judge2 = mock(Judge.class);

        FinalMark mark1 = mock(FinalMark.class);
        FinalMark mark2 = mock(FinalMark.class);
        FinalMark mark3 = mock(FinalMark.class);

        when(dance.getId()).thenReturn(1L);

        when(participant1.getId()).thenReturn(10L);
        when(participant2.getId()).thenReturn(20L);

        when(judge1.getId()).thenReturn(100L);
        when(judge2.getId()).thenReturn(200L);

        when(mark1.getDance()).thenReturn(dance);
        when(mark2.getDance()).thenReturn(dance);
        when(mark3.getDance()).thenReturn(dance);

        when(mark1.getParticipantTournament()).thenReturn(participant1);
        when(mark2.getParticipantTournament()).thenReturn(participant1);
        when(mark3.getParticipantTournament()).thenReturn(participant2);

        when(mark1.getJudge()).thenReturn(judge1);
        when(mark2.getJudge()).thenReturn(judge2);
        when(mark3.getJudge()).thenReturn(judge1);

        when(mark3.getPlace()).thenReturn(1);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(mark1, mark2, mark3));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals(
                "Participant 20 does not have marks from all judges",
                exception.getMessage()
        );

        verify(finalMarkRepository).findByCategoryTournamentId(categoryId);
        verifyNoInteractions(finalMarkService, skatingResultsRepository);
    }

    @Test
    void calculateFinalPlaces_shouldCalculateMultiDanceResultsSuccessfully() {
        // Arrange
        Long categoryId = 1L;

        ParticipantTournament participant1 = mock(ParticipantTournament.class);
        ParticipantTournament participant2 = mock(ParticipantTournament.class);

        Dance dance1 = mock(Dance.class);
        Dance dance2 = mock(Dance.class);

        Judge judge1 = mock(Judge.class);
        Judge judge2 = mock(Judge.class);

        when(participant1.getId()).thenReturn(10L);
        when(participant2.getId()).thenReturn(20L);

        when(participant1.getCategoryTournament()).thenReturn(mock(CategoryTournament.class));
        when(participant2.getCategoryTournament()).thenReturn(mock(CategoryTournament.class));

        when(dance1.getId()).thenReturn(1L);
        when(dance2.getId()).thenReturn(2L);

        when(judge1.getId()).thenReturn(100L);
        when(judge2.getId()).thenReturn(200L);

        FinalMark mark1 = createFinalMark(participant1, dance1, judge1, 1);
        FinalMark mark2 = createFinalMark(participant2, dance1, judge1, 2);
        FinalMark mark3 = createFinalMark(participant1, dance1, judge2, 1);
        FinalMark mark4 = createFinalMark(participant2, dance1, judge2, 2);

        FinalMark mark5 = createFinalMark(participant1, dance2, judge1, 1);
        FinalMark mark6 = createFinalMark(participant2, dance2, judge1, 2);
        FinalMark mark7 = createFinalMark(participant1, dance2, judge2, 1);
        FinalMark mark8 = createFinalMark(participant2, dance2, judge2, 2);

        List<FinalMark> marks = List.of(
                mark1, mark2, mark3, mark4,
                mark5, mark6, mark7, mark8
        );

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        // Act
        List<SkatingResultDto> result = service.calculateFinalPlaces(categoryId);

        // Assert
        assertEquals(2, result.size());

        assertEquals(BigDecimal.ONE, result.stream()
                .filter(r -> r.getParticipantTournamentId().equals(10L))
                .findFirst()
                .orElseThrow()
                .getFinalPlace());

        assertEquals(BigDecimal.valueOf(2), result.stream()
                .filter(r -> r.getParticipantTournamentId().equals(20L))
                .findFirst()
                .orElseThrow()
                .getFinalPlace());

        verify(skatingResultsRepository).deleteByCategoryTournamentId(categoryId);
        verify(skatingResultsRepository, times(2)).save(any(SkatingResult.class));
    }

    @Test
    void calculateFinalPlaces_shouldResolveTieByRule10WhenParticipantHasMoreFirstPlaces() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);
        ParticipantTournament participantC = mock(ParticipantTournament.class);

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);
        when(participantC.getId()).thenReturn(30L);

        CategoryTournament category = mock(CategoryTournament.class);

        when(participantA.getCategoryTournament()).thenReturn(category);
        when(participantB.getCategoryTournament()).thenReturn(category);
        when(participantC.getCategoryTournament()).thenReturn(category);

        Dance dance1 = mock(Dance.class);
        Dance dance2 = mock(Dance.class);
        Dance dance3 = mock(Dance.class);

        when(dance1.getId()).thenReturn(1L);
        when(dance2.getId()).thenReturn(2L);
        when(dance3.getId()).thenReturn(3L);

        Judge judge1 = mock(Judge.class);
        Judge judge2 = mock(Judge.class);
        Judge judge3 = mock(Judge.class);

        when(judge1.getId()).thenReturn(1L);
        when(judge2.getId()).thenReturn(2L);
        when(judge3.getId()).thenReturn(3L);

        List<FinalMark> marks = new ArrayList<>();

        // Dance 1: A = 1, B = 2, C = 3
        marks.add(createFinalMark(participantA, dance1, judge1, 1));
        marks.add(createFinalMark(participantA, dance1, judge2, 1));
        marks.add(createFinalMark(participantA, dance1, judge3, 3));

        marks.add(createFinalMark(participantB, dance1, judge1, 2));
        marks.add(createFinalMark(participantB, dance1, judge2, 2));
        marks.add(createFinalMark(participantB, dance1, judge3, 3));

        marks.add(createFinalMark(participantC, dance1, judge1, 3));
        marks.add(createFinalMark(participantC, dance1, judge2, 3));
        marks.add(createFinalMark(participantC, dance1, judge3, 1));

        // Dance 2: A = 1, B = 2, C = 3
        marks.add(createFinalMark(participantA, dance2, judge1, 1));
        marks.add(createFinalMark(participantA, dance2, judge2, 1));
        marks.add(createFinalMark(participantA, dance2, judge3, 3));

        marks.add(createFinalMark(participantB, dance2, judge1, 2));
        marks.add(createFinalMark(participantB, dance2, judge2, 2));
        marks.add(createFinalMark(participantB, dance2, judge3, 3));

        marks.add(createFinalMark(participantC, dance2, judge1, 3));
        marks.add(createFinalMark(participantC, dance2, judge2, 3));
        marks.add(createFinalMark(participantC, dance2, judge3, 1));

        // Dance 3: A = 3, B = 1, C = 2
        marks.add(createFinalMark(participantA, dance3, judge1, 3));
        marks.add(createFinalMark(participantA, dance3, judge2, 3));
        marks.add(createFinalMark(participantA, dance3, judge3, 1));

        marks.add(createFinalMark(participantB, dance3, judge1, 1));
        marks.add(createFinalMark(participantB, dance3, judge2, 1));
        marks.add(createFinalMark(participantB, dance3, judge3, 3));

        marks.add(createFinalMark(participantC, dance3, judge1, 2));
        marks.add(createFinalMark(participantC, dance3, judge2, 2));
        marks.add(createFinalMark(participantC, dance3, judge3, 3));

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(3)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(3, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(10L));
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(20L));
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(30L));

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);

    }

    @Test
    void calculateFinalPlaces_shouldResolveTieByRule10WhenSumOfPlacesIsLower() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);
        ParticipantTournament participantC = mock(ParticipantTournament.class);
        ParticipantTournament participantD = mock(ParticipantTournament.class);

        List<ParticipantTournament> participants = List.of(
                participantA, participantB, participantC, participantD
        );

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);
        when(participantC.getId()).thenReturn(30L);
        when(participantD.getId()).thenReturn(40L);

        CategoryTournament category = mock(CategoryTournament.class);

        participants.forEach(p ->
                when(p.getCategoryTournament()).thenReturn(category)
        );

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class)
        );

        for (int i = 0; i < dances.size(); i++) {
            when(dances.get(i).getId()).thenReturn((long) i + 1);
        }

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        // Рядки — танці, стовпці — учасники A, B, C, D.
        int[][] placesByDance = {
                {1, 2, 3, 4},
                {1, 2, 3, 4},
                {4, 3, 1, 2},
                {4, 3, 1, 2}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                for (int participantIndex = 0;
                     participantIndex < participants.size();
                     participantIndex++) {

                    marks.add(createFinalMark(
                            participants.get(participantIndex),
                            dances.get(danceIndex),
                            judge,
                            placesByDance[danceIndex][participantIndex]
                    ));
                }
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(4)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(4, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(30L)); // C
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(4), actualPlaces.get(40L)); // D

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldResolveTieByRule11() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);
        ParticipantTournament participantC = mock(ParticipantTournament.class);
        ParticipantTournament participantD = mock(ParticipantTournament.class);
        ParticipantTournament participantE = mock(ParticipantTournament.class);

        List<ParticipantTournament> participants = List.of(
                participantA, participantB, participantC, participantD, participantE
        );

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);
        when(participantC.getId()).thenReturn(30L);
        when(participantD.getId()).thenReturn(40L);
        when(participantE.getId()).thenReturn(50L);

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class)
        );

        for (int i = 0; i < dances.size(); i++) {
            when(dances.get(i).getId()).thenReturn((long) i + 1);
        }

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        int[][] placesByDance = {
                {1, 2, 3, 4, 5},
                {2, 1, 3, 4, 5},
                {3, 4, 1, 2, 5},
                {5, 4, 1, 2, 3}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                for (int participantIndex = 0;
                     participantIndex < participants.size();
                     participantIndex++) {

                    marks.add(createFinalMark(
                            participants.get(participantIndex),
                            dances.get(danceIndex),
                            judge,
                            placesByDance[danceIndex][participantIndex]
                    ));
                }
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(5)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(5, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(30L)); // C
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(4), actualPlaces.get(40L)); // D
        assertEquals(BigDecimal.valueOf(5), actualPlaces.get(50L)); // E

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldAssignAveragePlaceWhenRule11CannotResolveTie() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);

        CategoryTournament category = mock(CategoryTournament.class);

        when(participantA.getCategoryTournament()).thenReturn(category);
        when(participantB.getCategoryTournament()).thenReturn(category);

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class)
        );

        when(dances.get(0).getId()).thenReturn(1L);
        when(dances.get(1).getId()).thenReturn(2L);

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        int[][] placesByDance = {
                {1, 2},
                {2, 1}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                marks.add(createFinalMark(
                        participantA,
                        dances.get(danceIndex),
                        judge,
                        placesByDance[danceIndex][0]
                ));

                marks.add(createFinalMark(
                        participantB,
                        dances.get(danceIndex),
                        judge,
                        placesByDance[danceIndex][1]
                ));
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(2)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(2, result.size());

        assertEquals(new BigDecimal("1.5"), actualPlaces.get(10L));
        assertEquals(new BigDecimal("1.5"), actualPlaces.get(20L));

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldRecalculateRemainingParticipantsAfterRule11() {
        Long categoryId = 1L;

        List<ParticipantTournament> participants = List.of(
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class)
        );

        for (int i = 0; i < participants.size(); i++) {
            when(participants.get(i).getId()).thenReturn((long) (i + 1) * 10);
        }

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class)
        );

        for (int i = 0; i < dances.size(); i++) {
            when(dances.get(i).getId()).thenReturn((long) i + 1);
        }

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        // Колонки: A, B, C, D, E
        int[][] placesByDance = {
                {2, 5, 1, 3, 4},
                {4, 2, 5, 3, 1},
                {4, 2, 1, 3, 5},
                {2, 4, 3, 5, 1},
                {3, 2, 5, 1, 4}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                for (int participantIndex = 0;
                     participantIndex < participants.size();
                     participantIndex++) {

                    marks.add(createFinalMark(
                            participants.get(participantIndex),
                            dances.get(danceIndex),
                            judge,
                            placesByDance[danceIndex][participantIndex]
                    ));
                }
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(5)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(5, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(30L)); // C — Rule 11
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(20L)); // B — повторний Rule 10
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(40L)); // D
        assertEquals(BigDecimal.valueOf(4), actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(5), actualPlaces.get(50L)); // E

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldContinueAfterRule10SelectsClearWinner() {
        Long categoryId = 1L;

        List<ParticipantTournament> participants = List.of(
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class)
        );

        for (int i = 0; i < participants.size(); i++) {
            when(participants.get(i).getId()).thenReturn((long) (i + 1) * 10);
        }

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class)
        );

        for (int i = 0; i < dances.size(); i++) {
            when(dances.get(i).getId()).thenReturn((long) i + 1);
        }

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        // Колонки: A, B, C, D
        int[][] placesByDance = {
                {1, 2, 3, 4},
                {1, 3, 2, 4},
                {1, 3, 2, 4}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                for (int participantIndex = 0;
                     participantIndex < participants.size();
                     participantIndex++) {

                    marks.add(createFinalMark(
                            participants.get(participantIndex),
                            dances.get(danceIndex),
                            judge,
                            placesByDance[danceIndex][participantIndex]
                    ));
                }
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(4)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(4, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(30L)); // C
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(4), actualPlaces.get(40L)); // D

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldAssignAveragePlaceAndContinueWithRemainingParticipants() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);
        ParticipantTournament participantC = mock(ParticipantTournament.class);

        List<ParticipantTournament> participants = List.of(
                participantA, participantB, participantC
        );

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);
        when(participantC.getId()).thenReturn(30L);

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class)
        );

        when(dances.get(0).getId()).thenReturn(1L);
        when(dances.get(1).getId()).thenReturn(2L);

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        int[][] placesByDance = {
                {1, 2, 3},
                {2, 1, 3}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                for (int participantIndex = 0;
                     participantIndex < participants.size();
                     participantIndex++) {

                    marks.add(createFinalMark(
                            participants.get(participantIndex),
                            dances.get(danceIndex),
                            judge,
                            placesByDance[danceIndex][participantIndex]
                    ));
                }
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(3)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(3, result.size());

        assertEquals(new BigDecimal("1.5"), actualPlaces.get(10L)); // A
        assertEquals(new BigDecimal("1.5"), actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(30L)); // C

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldAssignAveragePlaceToThreeTiedParticipants() {
        Long categoryId = 1L;

        List<ParticipantTournament> participants = List.of(
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class)
        );

        for (int i = 0; i < participants.size(); i++) {
            when(participants.get(i).getId()).thenReturn((long) (i + 1) * 10);
        }

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class)
        );

        for (int i = 0; i < dances.size(); i++) {
            when(dances.get(i).getId()).thenReturn((long) i + 1);
        }

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        int[][] placesByDance = {
                {1, 2, 3, 4},
                {2, 3, 1, 4},
                {3, 1, 2, 4}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                for (int participantIndex = 0;
                     participantIndex < participants.size();
                     participantIndex++) {

                    marks.add(createFinalMark(
                            participants.get(participantIndex),
                            dances.get(danceIndex),
                            judge,
                            placesByDance[danceIndex][participantIndex]
                    ));
                }
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(4)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(4, result.size());

        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(30L)); // C
        assertEquals(BigDecimal.valueOf(4), actualPlaces.get(40L)); // D

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldReturnSameResultsRegardlessOfMarksOrder() {
        Long categoryId = 1L;

        List<ParticipantTournament> participants = List.of(
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class),
                mock(ParticipantTournament.class)
        );

        for (int i = 0; i < participants.size(); i++) {
            when(participants.get(i).getId()).thenReturn((long) (i + 1) * 10);
        }

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        List<Dance> dances = List.of(
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class),
                mock(Dance.class)
        );

        for (int i = 0; i < dances.size(); i++) {
            when(dances.get(i).getId()).thenReturn((long) i + 1);
        }

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        // Колонки: A, B, C, D
        int[][] placesByDance = {
                {1, 2, 3, 4},
                {1, 2, 3, 4},
                {4, 3, 1, 2},
                {4, 3, 1, 2}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int danceIndex = 0; danceIndex < dances.size(); danceIndex++) {
            for (Judge judge : judges) {
                for (int participantIndex = 0;
                     participantIndex < participants.size();
                     participantIndex++) {

                    marks.add(createFinalMark(
                            participants.get(participantIndex),
                            dances.get(danceIndex),
                            judge,
                            placesByDance[danceIndex][participantIndex]
                    ));
                }
            }
        }

        // Перемішуємо оцінки у фіксованому порядку.
        Collections.shuffle(marks, new Random(42));

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(4)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(4, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(30L)); // C
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(4), actualPlaces.get(40L)); // D

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldCalculatePlacesUsingDifferentJudgeMarks() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);
        ParticipantTournament participantC = mock(ParticipantTournament.class);

        List<ParticipantTournament> participants = List.of(
                participantA, participantB, participantC
        );

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);
        when(participantC.getId()).thenReturn(30L);

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        Dance dance = mock(Dance.class);
        when(dance.getId()).thenReturn(1L);

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        int[][] placesByJudge = {
                {1, 2, 3},
                {1, 2, 3},
                {2, 1, 3},
                {2, 1, 3},
                {3, 1, 2}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int judgeIndex = 0; judgeIndex < judges.size(); judgeIndex++) {
            for (int participantIndex = 0;
                 participantIndex < participants.size();
                 participantIndex++) {

                marks.add(createFinalMark(
                        participants.get(participantIndex),
                        dance,
                        judges.get(judgeIndex),
                        placesByJudge[judgeIndex][participantIndex]
                ));
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(3)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(3, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(30L)); // C

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldFindMajorityAtSecondColumn() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);
        ParticipantTournament participantC = mock(ParticipantTournament.class);

        List<ParticipantTournament> participants = List.of(
                participantA, participantB, participantC
        );

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);
        when(participantC.getId()).thenReturn(30L);

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        Dance dance = mock(Dance.class);
        when(dance.getId()).thenReturn(1L);

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        int[][] placesByJudge = {
                {1, 2, 3},
                {2, 1, 3},
                {3, 1, 2},
                {3, 2, 1},
                {2, 3, 1}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int judgeIndex = 0; judgeIndex < judges.size(); judgeIndex++) {
            for (int participantIndex = 0;
                 participantIndex < participants.size();
                 participantIndex++) {

                marks.add(createFinalMark(
                        participants.get(participantIndex),
                        dance,
                        judges.get(judgeIndex),
                        placesByJudge[judgeIndex][participantIndex]
                ));
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(3)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(3, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(30L)); // C
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(10L)); // A

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldResolveRule7TieByLowerSumOfMarks() {
        Long categoryId = 1L;

        ParticipantTournament participantA = mock(ParticipantTournament.class);
        ParticipantTournament participantB = mock(ParticipantTournament.class);
        ParticipantTournament participantC = mock(ParticipantTournament.class);

        List<ParticipantTournament> participants = List.of(
                participantA, participantB, participantC
        );

        when(participantA.getId()).thenReturn(10L);
        when(participantB.getId()).thenReturn(20L);
        when(participantC.getId()).thenReturn(30L);

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        Dance dance = mock(Dance.class);
        when(dance.getId()).thenReturn(1L);

        List<Judge> judges = List.of(
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class),
                mock(Judge.class)
        );

        for (int i = 0; i < judges.size(); i++) {
            when(judges.get(i).getId()).thenReturn((long) i + 1);
        }

        int[][] placesByJudge = {
                {1, 2, 3},
                {1, 2, 3},
                {2, 1, 3},
                {2, 3, 1},
                {3, 2, 1}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int judgeIndex = 0; judgeIndex < judges.size(); judgeIndex++) {
            for (int participantIndex = 0;
                 participantIndex < participants.size();
                 participantIndex++) {

                marks.add(createFinalMark(
                        participants.get(participantIndex),
                        dance,
                        judges.get(judgeIndex),
                        placesByJudge[judgeIndex][participantIndex]
                ));
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        List<SkatingResultDto> result =
                service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(3)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(3, result.size());

        assertEquals(BigDecimal.ONE, actualPlaces.get(10L)); // A
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(20L)); // B
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(30L)); // C

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldContinueRule7WhenCountsAndSumsAreEqual() {
        Long categoryId = 1L;

        ParticipantTournament a = mock(ParticipantTournament.class);
        ParticipantTournament b = mock(ParticipantTournament.class);
        ParticipantTournament c = mock(ParticipantTournament.class);
        ParticipantTournament d = mock(ParticipantTournament.class);

        when(a.getId()).thenReturn(1L);
        when(b.getId()).thenReturn(2L);
        when(c.getId()).thenReturn(3L);
        when(d.getId()).thenReturn(4L);

        Dance dance = mock(Dance.class);
        Judge judge1 = mock(Judge.class);
        Judge judge2 = mock(Judge.class);
        Judge judge3 = mock(Judge.class);
        Judge judge4 = mock(Judge.class);
        Judge judge5 = mock(Judge.class);

        List<Judge> judges = List.of(
                judge1, judge2, judge3, judge4, judge5
        );

        // Рядки — судді, стовпці — учасники A, B, C, D.
        int[][] placesByJudge = {
                {1, 2, 3, 4},
                {1, 2, 4, 3},
                {2, 1, 3, 4},
                {2, 1, 4, 3},
                {3, 4, 1, 2}
        };

        List<ParticipantTournament> participants = List.of(a, b, c, d);
        List<FinalMark> marks = new ArrayList<>();

        for (int i = 0; i < judges.size(); i++) {
            for (int j = 0; j < participants.size(); j++) {
                marks.add(createSkatingMark(
                        participants.get(j),
                        placesByJudge[i][j]
                ));
            }
        }

        CategoryTournament category = mock(CategoryTournament.class);

        for (ParticipantTournament participant : participants) {
            when(participant.getCategoryTournament()).thenReturn(category);
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        service.calculateFinalPlaces(categoryId);

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        verify(skatingResultsRepository, times(4)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        result -> result.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(BigDecimal.ONE, actualPlaces.get(1L));
        assertEquals(BigDecimal.valueOf(2), actualPlaces.get(2L));
        assertEquals(BigDecimal.valueOf(3), actualPlaces.get(3L));
        assertEquals(BigDecimal.valueOf(4), actualPlaces.get(4L));
    }

    @Test
    void calculateFinalPlaces_shouldAssignAveragePlacesWhenRule7TieRemainsUnresolved() {
        Long categoryId = 1L;

        ParticipantTournament a = mock(ParticipantTournament.class);
        ParticipantTournament b = mock(ParticipantTournament.class);
        ParticipantTournament c = mock(ParticipantTournament.class);
        ParticipantTournament d = mock(ParticipantTournament.class);

        when(a.getId()).thenReturn(1L);
        when(b.getId()).thenReturn(2L);
        when(c.getId()).thenReturn(3L);
        when(d.getId()).thenReturn(4L);

        when(a.getCategoryTournament()).thenReturn(category);
        when(b.getCategoryTournament()).thenReturn(category);
        when(c.getCategoryTournament()).thenReturn(category);
        when(d.getCategoryTournament()).thenReturn(category);

        List<ParticipantTournament> participants = List.of(a, b, c, d);

        int[][] placesByJudge = {
                {1, 4, 2, 3},
                {1, 2, 3, 4},
                {2, 1, 3, 4},
                {2, 1, 3, 4},
                {4, 2, 1, 3}
        };

        List<FinalMark> marks = new ArrayList<>();

        for (int[] places : placesByJudge) {
            for (int i = 0; i < participants.size(); i++) {
                marks.add(createSkatingMark(participants.get(i), places[i]));
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        ArgumentCaptor<SkatingResult> captor =
                ArgumentCaptor.forClass(SkatingResult.class);

        service.calculateFinalPlaces(categoryId);

        verify(skatingResultsRepository, times(4)).save(captor.capture());

        Map<Long, BigDecimal> actualPlaces = captor.getAllValues()
                .stream()
                .collect(Collectors.toMap(
                        result -> result.getParticipantTournament().getId(),
                        SkatingResult::getFinalPlace
                ));

        assertEquals(new BigDecimal("1.5"), actualPlaces.get(1L));
        assertEquals(new BigDecimal("1.5"), actualPlaces.get(2L));
        assertEquals(new BigDecimal("3"), actualPlaces.get(3L));
        assertEquals(new BigDecimal("4"), actualPlaces.get(4L));
    }

    @Test
    void calculateFinalPlaces_shouldLockRoundAndReplaceExistingResults() {
        Long categoryId = 1L;

        ParticipantTournament a = mock(ParticipantTournament.class);
        ParticipantTournament b = mock(ParticipantTournament.class);

        when(a.getId()).thenReturn(1L);
        when(b.getId()).thenReturn(2L);

        when(a.getCategoryTournament()).thenReturn(category);
        when(b.getCategoryTournament()).thenReturn(category);

        List<FinalMark> marks = List.of(
                createSkatingMark(a, 1),
                createSkatingMark(a, 1),
                createSkatingMark(a, 2),
                createSkatingMark(b, 2),
                createSkatingMark(b, 2),
                createSkatingMark(b, 1)
        );

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        service.calculateFinalPlaces(categoryId);

        InOrder inOrder = inOrder(finalMarkRepository, skatingResultsRepository);

        inOrder.verify(finalMarkRepository).lockRound(categoryId);
        inOrder.verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
        inOrder.verify(skatingResultsRepository, times(2))
                .save(any(SkatingResult.class));
    }

    @Test
    void calculateFinalPlaces_shouldNotLockRoundForMultiDanceMarks() {
        Long categoryId = 1L;

        ParticipantTournament a = mock(ParticipantTournament.class);
        ParticipantTournament b = mock(ParticipantTournament.class);

        when(a.getId()).thenReturn(1L);
        when(b.getId()).thenReturn(2L);

        when(a.getCategoryTournament()).thenReturn(category);
        when(b.getCategoryTournament()).thenReturn(category);

        Dance dance1 = mock(Dance.class);
        Dance dance2 = mock(Dance.class);

        when(dance1.getId()).thenReturn(1L);
        when(dance2.getId()).thenReturn(2L);

        List<Judge> judges = new ArrayList<>();

        for (long id = 1; id <= 3; id++) {
            Judge judge = mock(Judge.class);
            when(judge.getId()).thenReturn(id);
            judges.add(judge);
        }

        List<FinalMark> marks = new ArrayList<>();

        for (Dance dance : List.of(dance1, dance2)) {
            for (Judge judge : judges) {
                marks.add(createFinalMark(a, dance, judge, 1));
                marks.add(createFinalMark(b, dance, judge, 2));
            }
        }

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        service.calculateFinalPlaces(categoryId);

        verify(finalMarkRepository, never()).lockRound(categoryId);
        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);
        verify(skatingResultsRepository, times(2))
                .save(any(SkatingResult.class));
    }

    @Test
    void calculateFinalPlaces_shouldNotDeleteExistingResultsWhenMultiDanceValidationFails() {
        Long categoryId = 1L;

        ParticipantTournament a = mock(ParticipantTournament.class);

        when(a.getId()).thenReturn(1L);

        Dance dance1 = mock(Dance.class);

        when(dance1.getId()).thenReturn(1L);

        Judge judge1 = mock(Judge.class);

        when(judge1.getId()).thenReturn(1L);

        FinalMark first = mock(FinalMark.class);
        when(first.getParticipantTournament()).thenReturn(a);
        when(first.getDance()).thenReturn(dance1);
        when(first.getJudge()).thenReturn(judge1);

        FinalMark duplicate = mock(FinalMark.class);
        when(duplicate.getParticipantTournament()).thenReturn(a);
        when(duplicate.getDance()).thenReturn(dance1);
        when(duplicate.getJudge()).thenReturn(judge1);

        List<FinalMark> marks = List.of(first, duplicate);

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        assertThrows(IllegalStateException.class,
                () -> service.calculateFinalPlaces(categoryId));

        verify(skatingResultsRepository, never())
                .deleteByCategoryTournamentId(categoryId);

        verify(skatingResultsRepository, never())
                .save(any(SkatingResult.class));

        verify(finalMarkRepository, never()).lockRound(categoryId);
    }

    @Test
    void calculateFinalPlaces_shouldReturnCalculatedResults() {
        Long categoryId = 1L;

        ParticipantTournament a = mock(ParticipantTournament.class);
        ParticipantTournament b = mock(ParticipantTournament.class);

        when(a.getId()).thenReturn(1L);
        when(b.getId()).thenReturn(2L);

        when(a.getCategoryTournament()).thenReturn(category);
        when(b.getCategoryTournament()).thenReturn(category);

        List<FinalMark> marks = List.of(
                createSkatingMark(a, 1),
                createSkatingMark(a, 1),
                createSkatingMark(a, 2),
                createSkatingMark(b, 2),
                createSkatingMark(b, 2),
                createSkatingMark(b, 1)
        );

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenAnswer(invocation -> {
                    SkatingResult result = invocation.getArgument(0);
                    result.setId(result.getParticipantTournament().getId());
                    return result;
                });

        List<SkatingResultDto> results = service.calculateFinalPlaces(categoryId);

        assertEquals(2, results.size());

        Map<Long, BigDecimal> actualPlaces = results.stream()
                .collect(Collectors.toMap(
                        SkatingResultDto::getParticipantTournamentId,
                        SkatingResultDto::getFinalPlace
                ));

        assertEquals(new BigDecimal("1"), actualPlaces.get(1L));
        assertEquals(new BigDecimal("2"), actualPlaces.get(2L));
    }

    @Test
    void calculateFinalPlaces_shouldPropagateExceptionWhenSavingResultFails() {
        Long categoryId = 1L;

        ParticipantTournament a = mock(ParticipantTournament.class);
        ParticipantTournament b = mock(ParticipantTournament.class);

        when(a.getCategoryTournament()).thenReturn(category);

        List<FinalMark> marks = List.of(
                createSkatingMark(a, 1),
                createSkatingMark(a, 1),
                createSkatingMark(a, 2),
                createSkatingMark(b, 2),
                createSkatingMark(b, 2),
                createSkatingMark(b, 1)
        );

        when(finalMarkRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(marks);

        when(skatingResultsRepository.save(any(SkatingResult.class)))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.calculateFinalPlaces(categoryId)
        );

        assertEquals("Database error", exception.getMessage());

        verify(skatingResultsRepository)
                .deleteByCategoryTournamentId(categoryId);

        verify(skatingResultsRepository)
                .save(any(SkatingResult.class));

        verify(skatingResultsRepository, times(1))
                .deleteByCategoryTournamentId(categoryId);
    }

    private FinalMark createFinalMark(
            ParticipantTournament participant,
            Dance dance,
            Judge judge,
            int place
    ) {
        FinalMark mark = mock(FinalMark.class);

        when(mark.getParticipantTournament()).thenReturn(participant);
        when(mark.getDance()).thenReturn(dance);
        when(mark.getJudge()).thenReturn(judge);
        when(mark.getPlace()).thenReturn(place);

        return mark;
    }

    private FinalMark createSkatingMark(
            ParticipantTournament participant,
            int place
    ) {
        FinalMark mark = mock(FinalMark.class);

        when(mark.getParticipantTournament()).thenReturn(participant);
        when(mark.getPlace()).thenReturn(place);

        return mark;
    }

}
