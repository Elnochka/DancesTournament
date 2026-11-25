package com.dances.service;

import com.dances.entity.CategoryTournament;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.SkatingResult;
import com.dances.entity.dto.SkatingResultDto;
import com.dances.entity.dto.SkatingResultRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.ParticipantTournamentRepository;
import com.dances.repository.SkatingResultsRepository;
import com.dances.service.impl.SkatingResultsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkatingResultsServiceImplTest {

    @Mock
    private SkatingResultsRepository skatingResultsRepository;

    @Mock
    private ParticipantTournamentRepository participantRepository;

    @Mock
    private CategoryTournamentRepository categoryRepository;

    @InjectMocks
    private SkatingResultsServiceImpl service;

    @Test
    void getAll_shouldReturnResults() {

        SkatingResult result1 = new SkatingResult();
        result1.setId(1L);

        SkatingResult result2 = new SkatingResult();
        result2.setId(2L);

        when(skatingResultsRepository.findAll())
                .thenReturn(List.of(result1, result2));

        List<SkatingResultDto> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(skatingResultsRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList() {

        when(skatingResultsRepository.findAll())
                .thenReturn(List.of());

        List<SkatingResultDto> result = service.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(skatingResultsRepository).findAll();
    }

    @Test
    void getByCategory_shouldReturnResults() {

        SkatingResult result = new SkatingResult();
        result.setId(1L);

        when(skatingResultsRepository.findByCategoryTournamentId(10L))
                .thenReturn(List.of(result));

        List<SkatingResultDto> results =
                service.getByCategory(10L);

        assertEquals(1, results.size());
        assertEquals(1L, results.getFirst().getId());

        verify(skatingResultsRepository)
                .findByCategoryTournamentId(10L);
    }

    @Test
    void create_shouldCreateResult() {

        SkatingResultRequestDto dto =
                mock(SkatingResultRequestDto.class);

        when(dto.getParticipantId()).thenReturn(10L);
        when(dto.getCategoryTournamentId()).thenReturn(20L);
        when(dto.getFinalPlace()).thenReturn(BigDecimal.valueOf(1));

        SkatingResult entity = new SkatingResult();

        when(dto.toEntity())
                .thenReturn(entity);

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

        when(skatingResultsRepository.save(entity))
                .thenAnswer(invocation -> {
                    SkatingResult saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        SkatingResultDto result =
                service.create(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());

        assertEquals(participant, entity.getParticipantTournament());
        assertEquals(category, entity.getCategoryTournament());
        assertEquals(BigDecimal.valueOf(1), entity.getFinalPlace());

        verify(skatingResultsRepository)
                .save(entity);
    }

    @Test
    void create_shouldThrowWhenParticipantNotFound() {

        SkatingResultRequestDto dto =
                mock(SkatingResultRequestDto.class);

        when(dto.toEntity())
                .thenReturn(new SkatingResult());

        when(dto.getParticipantId())
                .thenReturn(10L);

        when(participantRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.create(dto)
        );

        verify(skatingResultsRepository, never())
                .save(any());
    }

    @Test
    void create_shouldThrowWhenCategoryNotFound() {

        SkatingResultRequestDto dto =
                mock(SkatingResultRequestDto.class);

        when(dto.toEntity())
                .thenReturn(new SkatingResult());

        when(dto.getParticipantId())
                .thenReturn(10L);

        when(dto.getCategoryTournamentId())
                .thenReturn(20L);

        ParticipantTournament participant =
                new ParticipantTournament();

        when(participantRepository.findById(10L))
                .thenReturn(Optional.of(participant));

        when(categoryRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.create(dto)
        );

        verify(skatingResultsRepository, never())
                .save(any());
    }

    @Test
    void delete_shouldDeleteResult() {

        service.delete(100L);

        verify(skatingResultsRepository)
                .deleteById(100L);
    }
}
