package com.dances.service;

import com.dances.entity.CategoryTournament;
import com.dances.entity.RoundSettings;
import com.dances.entity.dto.RoundSettingsDto;
import com.dances.entity.dto.RoundSettingsRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.ParticipantTournamentRepository;
import com.dances.repository.RoundSettingsRepository;
import com.dances.service.impl.RoundSettingsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoundSettingsServiceImplTest {

    @Mock
    private CategoryTournamentRepository categoryRepository;

    @Mock
    private RoundSettingsRepository roundSettingsRepository;

    @Mock
    private ParticipantTournamentRepository participantRepository;

    @InjectMocks
    private RoundSettingsServiceImpl service;

    @Mock
    private CategoryTournament category;

    private static final Long CATEGORY_ID = 1L;

    @Test
    void getSettings_shouldThrowWhenCategoryNotFound() {
        Long categoryId = CATEGORY_ID;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.getSettings(categoryId, RoundEnum.DEFAULT)
        );

        assertEquals(
                "CategoryTournament not found: " + categoryId,
                exception.getMessage()
        );

        verify(categoryRepository).findById(categoryId);

        verifyNoInteractions(
                participantRepository,
                roundSettingsRepository
        );
    }

    @Test
    void getSettings_shouldReturnRecommendedSettingsWhenNotConfigured() {
        Long categoryId = CATEGORY_ID;
        long participantsCount = 16L;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(participantRepository.countByCategoryTournamentId(categoryId))
                .thenReturn(participantsCount);

        when(roundSettingsRepository.findByCategoryTournamentIdAndRound(
                categoryId, RoundEnum.DEFAULT
        )).thenReturn(Optional.empty());

        RoundSettingsDto result = service.getSettings(categoryId, RoundEnum.DEFAULT);

        assertEquals(categoryId, result.getCategoryTournamentId());
        assertEquals(RoundEnum.DEFAULT, result.getRound());
        assertEquals(16, result.getCouplesToNextRound());
        assertNull(result.getId());

        verify(categoryRepository).findById(categoryId);
        verify(participantRepository).countByCategoryTournamentId(categoryId);
        verify(roundSettingsRepository)
                .findByCategoryTournamentIdAndRound(categoryId, RoundEnum.DEFAULT);
    }

    @Test
    void getSettings_shouldCalculateRecommendedCouplesWhenSettingsNotConfigured() {
        Long categoryId = CATEGORY_ID;
        long participantsCount = 16L;

        RoundEnum round = RoundEnum.QUARTER_FINAL;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(participantRepository.countByCategoryTournamentId(categoryId))
                .thenReturn(participantsCount);

        when(roundSettingsRepository.findByCategoryTournamentIdAndRound(
                categoryId, round
        )).thenReturn(Optional.empty());

        RoundSettingsDto result = service.getSettings(categoryId, round);

        assertEquals(categoryId, result.getCategoryTournamentId());
        assertEquals(round, result.getRound());
        assertEquals(8, result.getCouplesToNextRound());
        assertNull(result.getId());
    }

    @Test
    void getSettings_shouldReturnExistingSettings() {
        Long categoryId = CATEGORY_ID;
        RoundEnum round = RoundEnum.QUARTER_FINAL;

        RoundSettings settings = mock(RoundSettings.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(participantRepository.countByCategoryTournamentId(categoryId))
                .thenReturn(16L);

        when(roundSettingsRepository.findByCategoryTournamentIdAndRound(
                categoryId, round
        )).thenReturn(Optional.of(settings));

        when(settings.getId()).thenReturn(10L);
        when(settings.getCouplesToNextRound()).thenReturn(6);

        RoundSettingsDto result = service.getSettings(categoryId, round);

        assertEquals(10L, result.getId());
        assertEquals(categoryId, result.getCategoryTournamentId());
        assertEquals(round, result.getRound());
        assertEquals(6, result.getCouplesToNextRound());

        verify(categoryRepository).findById(categoryId);
        verify(participantRepository).countByCategoryTournamentId(categoryId);
        verify(roundSettingsRepository)
                .findByCategoryTournamentIdAndRound(categoryId, round);
    }

    @Test
    void createRoundSettings_shouldCreateSettings() {
        Long categoryId = CATEGORY_ID;
        RoundEnum round = RoundEnum.DEFAULT;

        RoundSettingsRequestDto dto = new RoundSettingsRequestDto(
                null,
                categoryId,
                round,
                1,
                8
        );

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(categoryId);

        when(roundSettingsRepository.save(any(RoundSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoundSettingsDto result = service.createRoundSettings(dto);

        assertNotNull(result);
        assertEquals(categoryId, result.getCategoryTournamentId());
        assertEquals(round, result.getRound());
        assertEquals(1, result.getRoundOrder());
        assertEquals(8, result.getCouplesToNextRound());

        ArgumentCaptor<RoundSettings> captor =
                ArgumentCaptor.forClass(RoundSettings.class);

        verify(roundSettingsRepository).save(captor.capture());

        RoundSettings saved = captor.getValue();

        assertEquals(category, saved.getCategoryTournament());
        assertEquals(round, saved.getRound());
        assertEquals(1, saved.getRoundOrder());
        assertEquals(8, saved.getCouplesToNextRound());

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void createRoundSettings_shouldThrowWhenCategoryNotFound() {
        Long categoryId = CATEGORY_ID;

        RoundSettingsRequestDto dto = new RoundSettingsRequestDto(
                null,
                categoryId,
                RoundEnum.DEFAULT,
                1,
                8
        );

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.createRoundSettings(dto)
        );

        assertEquals(
                "CategoryTournament not found",
                exception.getMessage()
        );

        verify(categoryRepository).findById(categoryId);
        verifyNoInteractions(roundSettingsRepository);
    }

    @Test
    void getAll_shouldReturnAllSettings() {
        RoundSettings first = RoundSettings.builder()
                .id(1L)
                .categoryTournament(category)
                .round(RoundEnum.DEFAULT)
                .roundOrder(1)
                .couplesToNextRound(8)
                .build();

        RoundSettings second = RoundSettings.builder()
                .id(2L)
                .categoryTournament(category)
                .round(RoundEnum.FINAL)
                .roundOrder(2)
                .couplesToNextRound(4)
                .build();

        when(roundSettingsRepository.findAll())
                .thenReturn(List.of(first, second));

        List<RoundSettingsDto> result = service.getAll();

        assertEquals(2, result.size());

        assertEquals(1L, result.getFirst().getId());
        assertEquals(RoundEnum.DEFAULT, result.getFirst().getRound());
        assertEquals(8, result.getFirst().getCouplesToNextRound());

        assertEquals(2L, result.get(1).getId());
        assertEquals(RoundEnum.FINAL, result.get(1).getRound());
        assertEquals(4, result.get(1).getCouplesToNextRound());

        verify(roundSettingsRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList() {
        when(roundSettingsRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<RoundSettingsDto> result = service.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(roundSettingsRepository).findAll();
    }

    @Test
    void getRounds_shouldReturnSettingsForCategory() {
        Long categoryId = CATEGORY_ID;

        RoundSettings first = RoundSettings.builder()
                .id(1L)
                .categoryTournament(category)
                .round(RoundEnum.DEFAULT)
                .roundOrder(1)
                .couplesToNextRound(8)
                .build();

        RoundSettings second = RoundSettings.builder()
                .id(2L)
                .categoryTournament(category)
                .round(RoundEnum.FINAL)
                .roundOrder(2)
                .couplesToNextRound(4)
                .build();

        when(roundSettingsRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(List.of(first, second));

        List<RoundSettingsDto> result = service.getRounds(categoryId);

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals(RoundEnum.DEFAULT, result.get(0).getRound());

        assertEquals(2L, result.get(1).getId());
        assertEquals(RoundEnum.FINAL, result.get(1).getRound());

        verify(roundSettingsRepository).findByCategoryTournamentId(categoryId);
    }

    @Test
    void getRounds_shouldReturnEmptyListWhenNoSettingsExist() {
        Long categoryId = CATEGORY_ID;

        when(roundSettingsRepository.findByCategoryTournamentId(categoryId))
                .thenReturn(Collections.emptyList());

        List<RoundSettingsDto> result = service.getRounds(categoryId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(roundSettingsRepository).findByCategoryTournamentId(categoryId);
    }

    @Test
    void getById_shouldReturnSettingsWhenFound() {
        Long id = 1L;

        RoundSettings settings = RoundSettings.builder()
                .id(id)
                .categoryTournament(category)
                .round(RoundEnum.DEFAULT)
                .roundOrder(1)
                .couplesToNextRound(8)
                .build();

        when(roundSettingsRepository.findById(id))
                .thenReturn(Optional.of(settings));

        Optional<RoundSettingsDto> result = service.getById(id);

        assertTrue(result.isPresent());

        RoundSettingsDto dto = result.get();

        assertEquals(id, dto.getId());
        assertEquals(RoundEnum.DEFAULT, dto.getRound());
        assertEquals(1, dto.getRoundOrder());
        assertEquals(8, dto.getCouplesToNextRound());

        verify(roundSettingsRepository).findById(id);
    }

    @Test
    void getById_shouldReturnEmptyWhenSettingsNotFound() {
        Long id = 100L;

        when(roundSettingsRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<RoundSettingsDto> result = service.getById(id);

        assertTrue(result.isEmpty());

        verify(roundSettingsRepository).findById(id);
    }

    @Test
    void deleteById_shouldDeleteSettings() {
        Long registrationId = 100L;

        service.deleteById(registrationId);

        verify(roundSettingsRepository).deleteById(registrationId);
    }

}
