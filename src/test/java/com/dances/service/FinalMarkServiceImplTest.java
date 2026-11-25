package com.dances.service;

import com.dances.entity.*;
import com.dances.entity.dto.FinalMarkDto;
import com.dances.entity.dto.FinalMarkPatchDto;
import com.dances.entity.dto.FinalMarkPatchJudgeDto;
import com.dances.entity.dto.FinalMarkRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.*;
import com.dances.service.impl.FinalMarkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FinalMarkServiceImplTest {

    @Mock
    private FinalMarkRepository finalMarkRepository;

    @Mock
    private ParticipantTournamentRepository participantRepository;

    @Mock
    private CategoryTournamentRepository categoryRepository;

    @Mock
    private JudgeRepository judgeRepository;

    @Mock
    private DanceRepository danceRepository;

    @InjectMocks
    private FinalMarkServiceImpl finalMarkService;

    private FinalMark finalMark;
    private CategoryTournament category;
    private Judge judge;
    private ParticipantTournament participant;

    @BeforeEach
    void setUp() {
        category = CategoryTournament.builder()
                .id(10L)
                .build();

        judge = Judge.builder()
                .id(20L)
                .nameJudge("Judge 1")
                .build();

        participant = ParticipantTournament.builder()
                .id(30L)
                .startNumber(101)
                .categoryTournament(category)
                .build();

        finalMark = FinalMark.builder()
                .id(1L)
                .participantTournament(participant)
                .categoryTournament(category)
                .judge(judge)
                .place(1)
                .locked(false)
                .build();
    }

    // ---------------------------------------------------------
    // getAll()
    // ---------------------------------------------------------

    @Test
    void getAll_shouldReturnAllFinalMarks() {

        when(finalMarkRepository.findAll())
                .thenReturn(List.of(finalMark));

        List<FinalMarkDto> result =
                finalMarkService.getAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertEquals(101, result.getFirst().getStartNumber());
        assertEquals(1, result.getFirst().getPlace());

        verify(finalMarkRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList() {

        when(finalMarkRepository.findAll())
                .thenReturn(List.of());

        List<FinalMarkDto> result =
                finalMarkService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(finalMarkRepository).findAll();
    }

    // ---------------------------------------------------------
    // getByCategory()
    // ---------------------------------------------------------

    @Test
    void getByCategory_shouldReturnMarksForCategory() {

        when(finalMarkRepository.findByCategoryTournamentId(10L))
                .thenReturn(List.of(finalMark));

        List<FinalMarkDto> result =
                finalMarkService.getByCategory(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getCategoryTournamentId());

        verify(finalMarkRepository)
                .findByCategoryTournamentId(10L);
    }

    // ---------------------------------------------------------
    // create()
    // ---------------------------------------------------------

    @Test
    void create_shouldCreateFinalMark() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        FinalMarkRequestDto dto = mock(FinalMarkRequestDto.class);

        when(dto.getParticipantId()).thenReturn(30L);
        when(dto.getCategoryTournamentId()).thenReturn(10L);
        when(dto.getJudgeId()).thenReturn(20L);
        when(dto.getDanceId()).thenReturn(30L);
        when(dto.getPlace()).thenReturn(1);

        when(dto.toEntity()).thenReturn(
                FinalMark.builder()
                        .place(1)
                        .build()
        );

        when(participantRepository.findById(30L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(judgeRepository.findById(20L))
                .thenReturn(Optional.of(judge));

        when(danceRepository.findById(30L))
                .thenReturn(Optional.of(dance));

        when(finalMarkRepository
                .countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(any(FinalMark.class)))
                .thenReturn(finalMark);

        FinalMarkDto result =
                finalMarkService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getPlace());

        verify(finalMarkRepository).save(any(FinalMark.class));
    }

    @Test
    void create_shouldThrowWhenParticipantNotFound() {

        FinalMarkRequestDto dto = mock(FinalMarkRequestDto.class);

        when(dto.getParticipantId()).thenReturn(30L);

        when(participantRepository.findById(30L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.create(dto)
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void create_shouldThrowWhenCategoryNotFound() {

        FinalMarkRequestDto dto = mock(FinalMarkRequestDto.class);

        when(dto.toEntity()).thenReturn(
                FinalMark.builder()
                        .place(1)
                        .build()
        );

        when(dto.getParticipantId()).thenReturn(30L);
        when(dto.getCategoryTournamentId()).thenReturn(10L);

        when(participantRepository.findById(30L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.create(dto)
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void create_shouldThrowWhenJudgeNotFound() {

        FinalMarkRequestDto dto = mock(FinalMarkRequestDto.class);

        when(dto.toEntity()).thenReturn(
                FinalMark.builder()
                        .place(1)
                        .build()
        );

        when(dto.getParticipantId()).thenReturn(30L);
        when(dto.getCategoryTournamentId()).thenReturn(10L);
        when(dto.getJudgeId()).thenReturn(20L);

        when(participantRepository.findById(30L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(judgeRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.create(dto)
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    // ---------------------------------------------------------
    // validatePlace()
    // ---------------------------------------------------------

    @Test
    void create_shouldRejectNegativePlace() {
        FinalMarkRequestDto dto = mock(FinalMarkRequestDto.class);

        Dance dance = new Dance();
        dance.setId(30L);

        when(dto.getParticipantId()).thenReturn(30L);
        when(dto.getCategoryTournamentId()).thenReturn(10L);
        when(dto.getJudgeId()).thenReturn(20L);
        when(dto.getDanceId()).thenReturn(30L);
        when(dto.getPlace()).thenReturn(-1);

        when(dto.toEntity()).thenReturn(
                FinalMark.builder()
                        .place(-1)
                        .build()
        );

        when(participantRepository.findById(30L))
                .thenReturn(Optional.of(participant));
        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));
        when(judgeRepository.findById(20L))
                .thenReturn(Optional.of(judge));
        when(danceRepository.findById(30L))
                .thenReturn(Optional.of(dance));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.create(dto)
        );

        assertEquals("Place must be between 1 and 5 or 0 ", exception.getMessage());

        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectPlaceGreaterThanParticipantsCount() {

        FinalMarkRequestDto dto = mock(FinalMarkRequestDto.class);

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(dto.getParticipantId()).thenReturn(30L);
        when(dto.getCategoryTournamentId()).thenReturn(10L);
        when(dto.getJudgeId()).thenReturn(20L);
        when(dto.getDanceId()).thenReturn(30L);
        when(dto.getPlace()).thenReturn(6);

        when(dto.toEntity()).thenReturn(
                FinalMark.builder()
                        .place(6)
                        .build()
        );

        when(participantRepository.findById(30L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(judgeRepository.findById(20L))
                .thenReturn(Optional.of(judge));

        when(danceRepository.findById(30L))
                .thenReturn(Optional.of(dance));

        when(finalMarkRepository
                .countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> finalMarkService.create(dto)
                );

        assertEquals(
                "Place must be between 1 and 5 or 0 ",
                exception.getMessage()
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void create_shouldRejectDuplicatePlace() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        FinalMarkRequestDto dto = mock(FinalMarkRequestDto.class);

        when(dto.getParticipantId()).thenReturn(30L);
        when(dto.getCategoryTournamentId()).thenReturn(10L);
        when(dto.getJudgeId()).thenReturn(20L);
        when(dto.getDanceId()).thenReturn(30L);
        when(dto.getPlace()).thenReturn(2);

        when(dto.toEntity()).thenReturn(
                FinalMark.builder()
                        .place(2)
                        .build()
        );

        when(participantRepository.findById(30L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(judgeRepository.findById(20L))
                .thenReturn(Optional.of(judge));

        when(danceRepository.findById(30L))
                .thenReturn(Optional.of(dance));

        when(finalMarkRepository
                .countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlace(
                        10L,
                        20L,
                        30L,
                        2
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> finalMarkService.create(dto)
                );

        assertEquals(
                "Judge has already assigned place 2",
                exception.getMessage()
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    // ---------------------------------------------------------
    // updatePlace()
    // ---------------------------------------------------------

    @Test
    void updatePlace_shouldUpdatePlaceSuccessfully() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository
                .countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        FinalMarkPatchJudgeDto dto =
                new FinalMarkPatchJudgeDto();

        dto.setPlace(3);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkDto result =
                finalMarkService.updatePlace(1L, dto);

        assertEquals(3, finalMark.getPlace());
        assertEquals(3, result.getPlace());

        verify(finalMarkRepository)
                .save(finalMark);
    }

    @Test
    void updatePlace_shouldRejectDuplicatePlace() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository
                .countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L,
                        20L,
                        30L,
                        3,
                        1L
                ))
                .thenReturn(true);

        FinalMarkPatchJudgeDto dto =
                new FinalMarkPatchJudgeDto();

        dto.setPlace(3);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.updatePlace(1L, dto)
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void updatePlace_shouldRejectPlaceOutsideRange() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository
                .countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        FinalMarkPatchJudgeDto dto =
                new FinalMarkPatchJudgeDto();

        dto.setPlace(6);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.updatePlace(1L, dto)
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void updatePlace_shouldThrowWhenFinalMarkNotFound() {

        when(finalMarkRepository.findById(100L))
                .thenReturn(Optional.empty());

        FinalMarkPatchJudgeDto dto =
                new FinalMarkPatchJudgeDto();

        dto.setPlace(2);

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.updatePlace(100L, dto)
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void updatePlace_shouldRejectLockedFinalMark() {

        finalMark.setLocked(true);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        FinalMarkPatchJudgeDto dto =
                new FinalMarkPatchJudgeDto();

        dto.setPlace(2);

        assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.updatePlace(1L, dto)
        );

        verify(finalMarkRepository, never())
                .countDistinctParticipantsByCategory(anyLong());

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenLocked() {

        finalMark.setLocked(true);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        FinalMarkPatchDto dto =
                new FinalMarkPatchDto();

        dto.setPlace(2);

        assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(finalMarkRepository, never())
                .save(any());
    }

    @Test
    void patchUpdate_shouldUpdateParticipantCategoryAndJudge() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        ParticipantTournament newParticipant =
                ParticipantTournament.builder()
                        .id(40L)
                        .startNumber(200)
                        .categoryTournament(category)
                        .build();

        CategoryTournament newCategory =
                CategoryTournament.builder()
                        .id(50L)
                        .build();

        Judge newJudge =
                Judge.builder()
                        .id(60L)
                        .nameJudge("Judge 2")
                        .build();

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(participantRepository.findById(40L))
                .thenReturn(Optional.of(newParticipant));

        when(categoryRepository.findById(50L))
                .thenReturn(Optional.of(newCategory));

        when(judgeRepository.findById(60L))
                .thenReturn(Optional.of(newJudge));

        when(finalMarkRepository
                .countDistinctParticipantsByCategory(50L))
                .thenReturn(5L);

        FinalMarkPatchDto dto =
                new FinalMarkPatchDto();

        dto.setParticipantId(40L);
        dto.setCategoryTournamentId(50L);
        dto.setJudgeId(60L);
        dto.setPlace(1);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        finalMarkService.patchUpdate(1L, dto);

        assertEquals(newParticipant, finalMark.getParticipantTournament());
        assertEquals(newCategory, finalMark.getCategoryTournament());
        assertEquals(newJudge, finalMark.getJudge());
        assertEquals(1, finalMark.getPlace());

        verify(finalMarkRepository).save(finalMark);
    }

    // ---------------------------------------------------------
    // delete()
    // ---------------------------------------------------------

    @Test
    void delete_shouldDeleteFinalMark() {

        doNothing()
                .when(finalMarkRepository)
                .deleteById(1L);

        finalMarkService.delete(1L);

        verify(finalMarkRepository)
                .deleteById(1L);
    }

    // ---------------------------------------------------------
    // getFinalMarksForJudgeName()
    // ---------------------------------------------------------

    @Test
    void getFinalMarksForJudgeName_shouldReturnMarks() {

        when(finalMarkRepository
                .findByJudge_NameJudge("Judge 1"))
                .thenReturn(List.of(finalMark));

        List<FinalMarkDto> result =
                finalMarkService
                        .getFinalMarksForJudgeName("Judge 1");

        assertEquals(1, result.size());
        assertEquals("Judge 1", result.getFirst().getJudge());
        assertEquals(1, result.getFirst().getPlace());

        verify(finalMarkRepository)
                .findByJudge_NameJudge("Judge 1");
    }

    @Test
    void getFinalMarksForJudgeName_shouldReturnEmptyList() {

        when(finalMarkRepository
                .findByJudge_NameJudge("Judge 1"))
                .thenReturn(List.of());

        List<FinalMarkDto> result =
                finalMarkService
                        .getFinalMarksForJudgeName("Judge 1");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(finalMarkRepository)
                .findByJudge_NameJudge("Judge 1");
    }

    @Test
    void patchUpdate_shouldUpdatePlace() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 4, 1L
                ))
                .thenReturn(false);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(4, finalMark.getPlace());

        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldThrowNotFoundExceptionWhenFinalMarkDoesNotExist() {
        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.empty());

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(finalMarkRepository).findById(1L);
        verify(finalMarkRepository, never()).save(any());
        verifyNoInteractions(
                participantRepository,
                categoryRepository,
                judgeRepository,
                danceRepository
        );
    }

    @Test
    void patchUpdate_shouldThrowNotFoundExceptionWhenParticipantDoesNotExist() {
        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(participantRepository.findById(100L))
                .thenReturn(Optional.empty());

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setParticipantId(100L);

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(participantRepository).findById(100L);
        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowNotFoundExceptionWhenCategoryDoesNotExist() {
        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(categoryRepository.findById(100L))
                .thenReturn(Optional.empty());

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setCategoryTournamentId(100L);

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(categoryRepository).findById(100L);
        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowNotFoundExceptionWhenJudgeDoesNotExist() {
        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(judgeRepository.findById(100L))
                .thenReturn(Optional.empty());

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setJudgeId(100L);

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(judgeRepository).findById(100L);
        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowIllegalStateExceptionWhenFinalMarkIsLocked() {
        finalMark.setLocked(true);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        assertEquals("Round is locked", exception.getMessage());

        verify(finalMarkRepository, never()).save(any());
        verifyNoInteractions(danceRepository);
    }

    @Test
    void patchUpdate_shouldThrowNotFoundExceptionWhenDanceDoesNotExist() {
        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(danceRepository.findById(100L))
                .thenReturn(Optional.empty());

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setDanceId(100L);

        assertThrows(
                NotFoundException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(danceRepository).findById(100L);
        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldSetPlaceToZeroWhenPlaceIsNull() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        finalMark.setPlace(3);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(null);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(0, finalMark.getPlace());

        verify(finalMarkRepository).save(finalMark);
        verify(finalMarkRepository, never())
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        anyLong(), anyLong(), anyLong(), anyInt(), anyLong()
                );
    }

    @Test
    void patchUpdate_shouldThrowExceptionWhenPlaceIsNegative() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(-1);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(finalMarkRepository, never()).save(any());
        verify(finalMarkRepository, never())
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        anyLong(), anyLong(), anyLong(), anyInt(), anyLong()
                );
    }

    @Test
    void patchUpdate_shouldThrowExceptionWhenPlaceExceedsParticipantsCount() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(6);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(finalMarkRepository).countDistinctParticipantsByCategory(10L);
        verify(finalMarkRepository, never()).save(any());
        verify(finalMarkRepository, never())
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        anyLong(), anyLong(), anyLong(), anyInt(), anyLong()
                );
    }

    @Test
    void patchUpdate_shouldUpdatePlaceWhenNoDuplicateExists() {

        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 4, 1L
                ))
                .thenReturn(false);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(4, finalMark.getPlace());

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 4, 1L
                );

        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldRejectDuplicatePlace() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 4, 1L
                ))
                .thenReturn(true);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 4, 1L
                );

        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldKeepSamePlaceWhenPlaceIsNotChanged() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);
        finalMark.setPlace(3);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 3, 1L
                ))
                .thenReturn(false);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(3);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(3, finalMark.getPlace());

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 3, 1L
                );
        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldUpdateDance() {

        Dance newDance = new Dance();
        newDance.setId(40L);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(danceRepository.findById(40L))
                .thenReturn(Optional.of(newDance));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setDanceId(40L);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(40L, finalMark.getDance().getId());

        verify(danceRepository).findById(40L);
        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldUpdateParticipant() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        ParticipantTournament newParticipant = new ParticipantTournament();
        newParticipant.setId(100L);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(participantRepository.findById(100L))
                .thenReturn(Optional.of(newParticipant));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setParticipantId(100L);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(100L, finalMark.getParticipantTournament().getId());

        verify(participantRepository).findById(100L);
        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldUpdateCategory() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        CategoryTournament newCategory = new CategoryTournament();
        newCategory.setId(40L);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(categoryRepository.findById(40L))
                .thenReturn(Optional.of(newCategory));

        when(finalMarkRepository.countDistinctParticipantsByCategory(40L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setCategoryTournamentId(40L);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(40L, finalMark.getCategoryTournament().getId());

        verify(categoryRepository).findById(40L);
        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldUpdateJudge() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        Judge newJudge = new Judge();
        newJudge.setId(40L);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(judgeRepository.findById(40L))
                .thenReturn(Optional.of(newJudge));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setJudgeId(40L);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(40L, finalMark.getJudge().getId());

        verify(judgeRepository).findById(40L);
        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldUpdateAllFields() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        ParticipantTournament newParticipant = new ParticipantTournament();
        newParticipant.setId(100L);

        CategoryTournament newCategory = new CategoryTournament();
        newCategory.setId(40L);

        Judge newJudge = new Judge();
        newJudge.setId(50L);

        Dance newDance = new Dance();
        newDance.setId(60L);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(participantRepository.findById(100L))
                .thenReturn(Optional.of(newParticipant));

        when(categoryRepository.findById(40L))
                .thenReturn(Optional.of(newCategory));

        when(judgeRepository.findById(50L))
                .thenReturn(Optional.of(newJudge));

        when(danceRepository.findById(60L))
                .thenReturn(Optional.of(newDance));

        when(finalMarkRepository.countDistinctParticipantsByCategory(40L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setParticipantId(100L);
        dto.setCategoryTournamentId(40L);
        dto.setJudgeId(50L);
        dto.setDanceId(60L);
        dto.setPlace(4);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(100L, finalMark.getParticipantTournament().getId());
        assertEquals(40L, finalMark.getCategoryTournament().getId());
        assertEquals(50L, finalMark.getJudge().getId());
        assertEquals(60L, finalMark.getDance().getId());
        assertEquals(4, finalMark.getPlace());

        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldNotChangePlaceWhenDuplicateExists() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);
        finalMark.setPlace(3);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        10L, 20L, 30L, 4, 1L
                ))
                .thenReturn(true);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        assertEquals(3, finalMark.getPlace());

        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldAllowPlaceEqualToParticipantsCount() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(5);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(5, finalMark.getPlace());

        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldKeepZeroPlaceWhenAlreadyZero() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);
        finalMark.setPlace(0);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(0);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(0, finalMark.getPlace());

        verify(finalMarkRepository).save(finalMark);
        verify(finalMarkRepository, never())
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        anyLong(), anyLong(), anyLong(), anyInt(), anyLong()
                );
    }

    @Test
    void patchUpdate_shouldRejectPlaceWhenCategoryHasNoParticipants() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(0L);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldRejectDuplicatePlaceWhenDanceIdIsZero() {
        Dance dance = new Dance();
        dance.setId(0L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndJudgeIdAndPlaceAndIdNot(
                        10L, 20L, 4, 1L
                ))
                .thenReturn(true);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.patchUpdate(1L, dto)
        );

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndJudgeIdAndPlaceAndIdNot(
                        10L, 20L, 4, 1L
                );

        verify(finalMarkRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldUpdatePlaceWhenDanceIdIsZero() {
        Dance dance = new Dance();
        dance.setId(0L);
        finalMark.setDance(dance);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(5L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(4);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(4, finalMark.getPlace());

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndJudgeIdAndPlaceAndIdNot(
                        10L, 20L, 4, 1L
                );

        verify(finalMarkRepository).save(finalMark);
    }

    @Test
    void patchUpdate_shouldAllowZeroPlaceWhenCategoryHasNoParticipants() {
        Dance dance = new Dance();
        dance.setId(30L);
        finalMark.setDance(dance);
        finalMark.setPlace(3);

        when(finalMarkRepository.findById(1L))
                .thenReturn(Optional.of(finalMark));

        when(finalMarkRepository.countDistinctParticipantsByCategory(10L))
                .thenReturn(0L);

        when(finalMarkRepository.save(finalMark))
                .thenReturn(finalMark);

        FinalMarkPatchDto dto = new FinalMarkPatchDto();
        dto.setPlace(0);

        FinalMarkDto result = finalMarkService.patchUpdate(1L, dto);

        assertNotNull(result);
        assertEquals(0, finalMark.getPlace());

        verify(finalMarkRepository).save(finalMark);
        verify(finalMarkRepository, never())
                .existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
                        anyLong(), anyLong(), anyLong(), anyInt(), anyLong()
                );
    }

    @Test
    void validateSingleDanceFinalMarks_shouldThrowWhenMarksContainDifferentDances() {
        Dance dance1 = new Dance();
        dance1.setId(10L);

        Dance dance2 = new Dance();
        dance2.setId(20L);

        FinalMark mark1 = new FinalMark();
        mark1.setDance(dance1);

        FinalMark mark2 = new FinalMark();
        mark2.setDance(dance2);

        List<FinalMark> marks = List.of(mark1, mark2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.validateSingleDanceFinalMarks(marks)
        );

        assertEquals(
                "computeSkating expects marks for one dance only",
                exception.getMessage()
        );
    }

    @Test
    void validateSingleDanceFinalMarks_shouldThrowWhenMarksContainNullAndNonNullDance() {
        Dance dance = new Dance();
        dance.setId(10L);

        FinalMark markWithDance = new FinalMark();
        markWithDance.setDance(dance);

        FinalMark markWithoutDance = new FinalMark();
        markWithoutDance.setDance(null);

        List<FinalMark> marks = List.of(markWithDance, markWithoutDance);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> finalMarkService.validateSingleDanceFinalMarks(marks)
        );

        assertEquals(
                "computeSkating expects marks for one dance only",
                exception.getMessage()
        );
    }

    @Test
    void validateSingleDanceFinalMarks_shouldAllowAllMarksWithoutDance() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        Judge judge1 = new Judge();
        judge1.setId(10L);

        Judge judge2 = new Judge();
        judge2.setId(20L);

        FinalMark mark1 = new FinalMark();
        mark1.setDance(null);
        mark1.setParticipantTournament(participant);
        mark1.setJudge(judge1);
        mark1.setPlace(1);

        FinalMark mark2 = new FinalMark();
        mark2.setDance(null);
        mark2.setParticipantTournament(participant);
        mark2.setJudge(judge2);
        mark2.setPlace(1);

        List<FinalMark> marks = List.of(mark1, mark2);

        assertDoesNotThrow(
                () -> finalMarkService.validateSingleDanceFinalMarks(marks)
        );
    }

    @Test
    void validateSingleDanceFinalMarks_shouldAllowMarksForSingleDance() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        Judge judge = new Judge();
        judge.setId(10L);

        Dance dance = new Dance();
        dance.setId(30L);

        FinalMark mark = new FinalMark();
        mark.setId(100L);
        mark.setParticipantTournament(participant);
        mark.setJudge(judge);
        mark.setDance(dance);
        mark.setPlace(1);

        assertDoesNotThrow(
                () -> finalMarkService.validateSingleDanceFinalMarks(List.of(mark))
        );
    }

    @Test
    void validateSingleDanceFinalMarks_shouldThrowWhenMarksAreEmpty() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateSingleDanceFinalMarks(Collections.emptyList())
        );

        assertEquals("No judge marks found", exception.getMessage());
    }

    @Test
    void validateFinalMarks_shouldThrowWhenParticipantHasIncompleteJudgeMarks() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        FinalMark mark = new FinalMark();

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                Map.of(participant, List.of(mark));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 2, 3)
        );

        assertEquals(
                "Participant 1 does not have marks from all judges",
                exception.getMessage()
        );
    }

    @Test
    void validateFinalMarks_shouldThrowWhenJudgeIsNull() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        FinalMark mark = new FinalMark();
        mark.setId(100L);
        mark.setJudge(null);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                Map.of(participant, List.of(mark));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 1, 3)
        );

        assertEquals(
                "Judge is missing for final mark 100",
                exception.getMessage()
        );
    }

    @Test
    void validateFinalMarks_shouldThrowWhenJudgeIdIsNull() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        Judge judge = new Judge();

        FinalMark mark = new FinalMark();
        mark.setId(100L);
        mark.setJudge(judge);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                Map.of(participant, List.of(mark));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 1, 3)
        );

        assertEquals(
                "Judge is missing for final mark 100",
                exception.getMessage()
        );
    }

    @Test
    void validateFinalMarks_shouldThrowWhenParticipantHasDuplicateJudgeMarks() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        Judge judge = new Judge();
        judge.setId(10L);

        FinalMark mark1 = new FinalMark();
        mark1.setJudge(judge);
        mark1.setPlace(1);

        FinalMark mark2 = new FinalMark();
        mark2.setJudge(judge);
        mark1.setPlace(1);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                Map.of(participant, List.of(mark1, mark2));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 2, 3)
        );

        assertEquals(
                "Duplicate judge mark for participant 1",
                exception.getMessage()
        );
    }

    @Test
    void validateFinalMarks_shouldThrowWhenPlaceIsLessThanOne() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        Judge judge = new Judge();
        judge.setId(10L);

        FinalMark mark = new FinalMark();
        mark.setId(100L);
        mark.setJudge(judge);
        mark.setPlace(0);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                Map.of(participant, List.of(mark));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 1, 3)
        );

        assertEquals(
                "Invalid place 0 for participant 1",
                exception.getMessage()
        );
    }

    @Test
    void validateFinalMarks_shouldThrowWhenPlaceExceedsParticipantCount() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        Judge judge = new Judge();
        judge.setId(10L);

        FinalMark mark = new FinalMark();
        mark.setId(100L);
        mark.setJudge(judge);
        mark.setPlace(4);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                Map.of(participant, List.of(mark));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 1, 3)
        );

        assertEquals(
                "Invalid place 4 for participant 1",
                exception.getMessage()
        );
    }

    @Test
    void validateFinalMarks_shouldThrowWhenJudgesCountIsZero() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(Collections.emptyMap(), 0, 3)
        );

        assertEquals("No judge marks found", exception.getMessage());
    }

    @Test
    void validateFinalMarks_shouldAcceptValidMarksForMultipleParticipants() {
        ParticipantTournament participant1 = new ParticipantTournament();
        participant1.setId(1L);

        ParticipantTournament participant2 = new ParticipantTournament();
        participant2.setId(2L);

        Judge judge1 = new Judge();
        judge1.setId(10L);

        Judge judge2 = new Judge();
        judge2.setId(20L);

        FinalMark mark1 = new FinalMark();
        mark1.setJudge(judge1);
        mark1.setPlace(1);

        FinalMark mark2 = new FinalMark();
        mark2.setJudge(judge2);
        mark2.setPlace(2);

        FinalMark mark3 = new FinalMark();
        mark3.setJudge(judge1);
        mark3.setPlace(2);

        FinalMark mark4 = new FinalMark();
        mark4.setJudge(judge2);
        mark4.setPlace(1);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                new LinkedHashMap<>();

        marksByParticipant.put(participant1, List.of(mark1, mark2));
        marksByParticipant.put(participant2, List.of(mark3, mark4));

        assertDoesNotThrow(
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 2, 2)
        );
    }

    @Test
    void validateFinalMarks_shouldThrowWhenSecondParticipantHasIncompleteJudgeMarks() {
        ParticipantTournament participant1 = new ParticipantTournament();
        participant1.setId(1L);

        ParticipantTournament participant2 = new ParticipantTournament();
        participant2.setId(2L);

        Judge judge1 = new Judge();
        judge1.setId(10L);

        Judge judge2 = new Judge();
        judge2.setId(20L);

        FinalMark mark1 = new FinalMark();
        mark1.setJudge(judge1);
        mark1.setPlace(1);

        FinalMark mark2 = new FinalMark();
        mark2.setJudge(judge2);
        mark2.setPlace(2);

        FinalMark mark3 = new FinalMark();
        mark3.setJudge(judge1);
        mark3.setPlace(1);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                new LinkedHashMap<>();

        marksByParticipant.put(participant1, List.of(mark1, mark2));
        marksByParticipant.put(participant2, List.of(mark3));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 2, 2)
        );

        assertEquals(
                "Participant 2 does not have marks from all judges",
                exception.getMessage()
        );
    }

    @Test
    void validateFinalMarks_shouldAllowSameJudgesForDifferentParticipants() {
        ParticipantTournament participant1 = new ParticipantTournament();
        participant1.setId(1L);

        ParticipantTournament participant2 = new ParticipantTournament();
        participant2.setId(2L);

        Judge judge1 = new Judge();
        judge1.setId(10L);

        Judge judge2 = new Judge();
        judge2.setId(20L);

        FinalMark mark1 = new FinalMark();
        mark1.setJudge(judge1);
        mark1.setPlace(1);

        FinalMark mark2 = new FinalMark();
        mark2.setJudge(judge2);
        mark2.setPlace(2);

        FinalMark mark3 = new FinalMark();
        mark3.setJudge(judge1);
        mark3.setPlace(2);

        FinalMark mark4 = new FinalMark();
        mark4.setJudge(judge2);
        mark4.setPlace(1);

        Map<ParticipantTournament, List<FinalMark>> marksByParticipant =
                new LinkedHashMap<>();

        marksByParticipant.put(participant1, List.of(mark1, mark2));
        marksByParticipant.put(participant2, List.of(mark3, mark4));

        assertDoesNotThrow(
                () -> finalMarkService.validateFinalMarks(marksByParticipant, 2, 2)
        );
    }

    @Test
    void validateSingleDanceFinalMarks_shouldAllowMultipleMarksForSameDance() {
        ParticipantTournament participant = new ParticipantTournament();
        participant.setId(1L);

        Dance dance = new Dance();
        dance.setId(30L);

        Judge judge1 = new Judge();
        judge1.setId(10L);

        Judge judge2 = new Judge();
        judge2.setId(20L);

        FinalMark mark1 = new FinalMark();
        mark1.setId(100L);
        mark1.setParticipantTournament(participant);
        mark1.setJudge(judge1);
        mark1.setDance(dance);
        mark1.setPlace(1);

        FinalMark mark2 = new FinalMark();
        mark2.setId(101L);
        mark2.setParticipantTournament(participant);
        mark2.setJudge(judge2);
        mark2.setDance(dance);
        mark2.setPlace(1);

        assertDoesNotThrow(
                () -> finalMarkService.validateSingleDanceFinalMarks(List.of(mark1, mark2))
        );
    }

}
