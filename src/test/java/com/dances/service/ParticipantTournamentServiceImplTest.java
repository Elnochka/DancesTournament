package com.dances.service;

import com.dances.entity.*;
import com.dances.entity.dto.ParticipantTournamentDto;
import com.dances.entity.dto.ParticipantTournamentPatchDto;
import com.dances.entity.dto.ParticipantTournamentRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.model.SoloCoupleEnum;
import com.dances.repository.*;
import com.dances.service.impl.ParticipantPassingService;
import com.dances.service.impl.ParticipantTournamentServiceImpl;
import com.dances.service.impl.ParticipantTournamentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;


import static com.dances.model.SoloCoupleEnum.COUPLE;
import static com.dances.model.SoloCoupleEnum.SOLO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantTournamentServiceImplTest {

    @Mock
    private ParticipantTournamentRepository participantTournamentRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CoupleRepository coupleRepository;

    @Mock
    private CategoryTournamentRepository categoryTournamentRepository;

    @Mock
    private MarkRepository markRepository;

    @Mock
    private FinalMarkRepository finalMarkRepository;

    @Mock
    private ParticipantPassingService participantPassingService;

    @Mock
    private ParticipantTournamentValidator participantTournamentValidator;

    @InjectMocks
    private ParticipantTournamentServiceImpl service;

    @Mock
    private ParticipantTournamentRequestDto dto;

    @Mock
    private CategoryTournament category;

    @Mock
    private RoundSettings roundSettings;

    @Mock
    private Participant participant;

    @Mock
    private Couple couple;

    @Mock
    private Participant male;

    @Mock
    private Participant female;

    private static final Long CATEGORY_ID = 1L;
    private static final Long PARTICIPANT_ID = 10L;
    private static final Long COUPLE_ID = 20L;

    @Test
    void create_shouldCreateSoloRegistration() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(SOLO);
        when(dto.getParticipantId()).thenReturn(PARTICIPANT_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Adult");

        when(participantRepository.findById(PARTICIPANT_ID))
                .thenReturn(Optional.of(participant));

        when(participant.getId()).thenReturn(PARTICIPANT_ID);
        when(participant.getFirstnameUkr()).thenReturn("Іван");
        when(participant.getLastnameUkr()).thenReturn("Петренко");

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndParticipantId(CATEGORY_ID, PARTICIPANT_ID))
                .thenReturn(false);

        when(participantTournamentRepository.findLastStartNumber(
                CATEGORY_ID, SOLO))
                .thenReturn(5);

        when(participantTournamentRepository.save(any(ParticipantTournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParticipantTournamentDto result = service.create(dto);

        ArgumentCaptor<ParticipantTournament> captor =
                ArgumentCaptor.forClass(ParticipantTournament.class);

        verify(participantTournamentRepository).save(captor.capture());

        ParticipantTournament saved = captor.getValue();

        assertEquals(6, saved.getStartNumber());
        assertEquals(SOLO, saved.getSoloCouple());
        assertSame(participant, saved.getParticipant());
        assertNull(saved.getCouple());
        assertSame(category, saved.getCategoryTournament());

        assertEquals(6, result.getStartNumber());
        assertEquals(PARTICIPANT_ID, result.getParticipantId());
        assertEquals("Іван Петренко", result.getParticipantName());
        assertEquals("Adult", result.getCategoryTournamentName());
        assertEquals("Іван Петренко", result.getDisplayName());

        verify(participantTournamentValidator)
                .validateSoloCouple(SOLO, category);

        verify(participantTournamentValidator)
                .validateParticipantMatchesCategory(participant, category);
    }

    @Test
    void create_shouldCreateCoupleRegistration() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(COUPLE);
        when(dto.getCoupleId()).thenReturn(COUPLE_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Adult");

        when(coupleRepository.findById(COUPLE_ID))
                .thenReturn(Optional.of(couple));

        when(couple.getId()).thenReturn(COUPLE_ID);
        when(couple.getNameCouple()).thenReturn("Іван & Марія");

        when(couple.getParticipantMale()).thenReturn(male);
        when(couple.getParticipantFemale()).thenReturn(female);

        when(male.getFirstnameUkr()).thenReturn("Іван");
        when(male.getLastnameUkr()).thenReturn("Петренко");

        when(female.getFirstnameUkr()).thenReturn("Марія");
        when(female.getLastnameUkr()).thenReturn("Коваленко");

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndCoupleId(CATEGORY_ID, COUPLE_ID))
                .thenReturn(false);

        when(participantTournamentRepository.findLastStartNumber(
                CATEGORY_ID, COUPLE))
                .thenReturn(10);

        when(participantTournamentRepository.save(any(ParticipantTournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParticipantTournamentDto result = service.create(dto);

        ArgumentCaptor<ParticipantTournament> captor =
                ArgumentCaptor.forClass(ParticipantTournament.class);

        verify(participantTournamentRepository).save(captor.capture());

        ParticipantTournament saved = captor.getValue();

        assertEquals(11, saved.getStartNumber());
        assertEquals(COUPLE, saved.getSoloCouple());
        assertNull(saved.getParticipant());
        assertSame(couple, saved.getCouple());
        assertSame(category, saved.getCategoryTournament());

        assertEquals(11, result.getStartNumber());
        assertEquals(COUPLE_ID, result.getCoupleId());
        assertEquals("Іван & Марія", result.getCoupleName());
        assertEquals("Adult", result.getCategoryTournamentName());
        assertEquals("Іван Петренко & Марія Коваленко", result.getDisplayName());

        verify(participantTournamentValidator)
                .validateSoloCouple(COUPLE, category);

        verify(participantTournamentValidator)
                .validateCoupleMatchesCategory(couple, category);
    }

    @Test
    void create_shouldThrowWhenCategoryNotFound() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(dto));

        verify(participantTournamentRepository, never()).save(any());
        verifyNoInteractions(participantRepository, coupleRepository,
                participantTournamentValidator);
    }

    @Test
    void create_shouldThrowWhenSoloCoupleIsNull() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(dto.getSoloCouple()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));

        verifyNoInteractions(participantRepository, coupleRepository,
                participantTournamentValidator);
        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenRegistrationTypeDoesNotMatchCategory() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(SOLO);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        doThrow(new IllegalArgumentException("Registration type does not match category tournament type"))
                .when(participantTournamentValidator)
                .validateSoloCouple(SOLO, category);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));

        verifyNoInteractions(participantRepository, coupleRepository);
        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenParticipantNotFound() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(SOLO);
        when(dto.getParticipantId()).thenReturn(PARTICIPANT_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(participantRepository.findById(PARTICIPANT_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(dto));

        verify(participantTournamentValidator)
                .validateSoloCouple(SOLO, category);

        verify(participantTournamentValidator, never())
                .validateParticipantMatchesCategory(any(), any());

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenCoupleNotFound() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(COUPLE);
        when(dto.getCoupleId()).thenReturn(COUPLE_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(coupleRepository.findById(COUPLE_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(dto));

        verify(participantTournamentValidator)
                .validateSoloCouple(COUPLE, category);

        verify(participantTournamentValidator, never())
                .validateCoupleMatchesCategory(any(), any());

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenParticipantDoesNotMatchCategory() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(SOLO);
        when(dto.getParticipantId()).thenReturn(PARTICIPANT_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(participantRepository.findById(PARTICIPANT_ID))
                .thenReturn(Optional.of(participant));

        doThrow(new IllegalArgumentException("Participant does not match category"))
                .when(participantTournamentValidator)
                .validateParticipantMatchesCategory(participant, category);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenCoupleDoesNotMatchCategory() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(COUPLE);
        when(dto.getCoupleId()).thenReturn(COUPLE_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(coupleRepository.findById(COUPLE_ID))
                .thenReturn(Optional.of(couple));

        doThrow(new IllegalArgumentException("Couple does not match category"))
                .when(participantTournamentValidator)
                .validateCoupleMatchesCategory(couple, category);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenParticipantAlreadyRegistered() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(SOLO);
        when(dto.getParticipantId()).thenReturn(PARTICIPANT_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(CATEGORY_ID);

        when(participantRepository.findById(PARTICIPANT_ID))
                .thenReturn(Optional.of(participant));

        when(participant.getId()).thenReturn(PARTICIPANT_ID);

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndParticipantId(CATEGORY_ID, PARTICIPANT_ID))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));

        verify(participantTournamentRepository, never())
                .findLastStartNumber(any(), any());

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenCoupleAlreadyRegistered() {
        when(dto.getCategoryTournamentId()).thenReturn(CATEGORY_ID);
        when(dto.getSoloCouple()).thenReturn(COUPLE);
        when(dto.getCoupleId()).thenReturn(COUPLE_ID);

        when(categoryTournamentRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(CATEGORY_ID);

        when(coupleRepository.findById(COUPLE_ID))
                .thenReturn(Optional.of(couple));

        when(couple.getId()).thenReturn(COUPLE_ID);

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndCoupleId(CATEGORY_ID, COUPLE_ID))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));

        verify(participantTournamentRepository, never())
                .findLastStartNumber(any(), any());

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldUpdateSoloRegistration() {
        Long registrationId = 100L;
        Long newParticipantId = 11L;

        Participant oldParticipant = mock(Participant.class);
        Participant newParticipant = mock(Participant.class);

        when(oldParticipant.getId()).thenReturn(PARTICIPANT_ID);

        when(newParticipant.getId()).thenReturn(newParticipantId);
        when(newParticipant.getFirstnameUkr()).thenReturn("Марія");
        when(newParticipant.getLastnameUkr()).thenReturn("Коваленко");

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Adult");

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(oldParticipant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto =
                mock(ParticipantTournamentPatchDto.class);

        when(dto.getCategoryTournamentId()).thenReturn(null);
        when(dto.getParticipantId()).thenReturn(newParticipantId);
        when(dto.getCoupleId()).thenReturn(null);
        when(dto.getSoloCouple()).thenReturn(SOLO);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(markRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(false);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(false);

        when(participantRepository.findById(newParticipantId))
                .thenReturn(Optional.of(newParticipant));

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndParticipantIdAndIdNot(
                        CATEGORY_ID, newParticipantId, registrationId))
                .thenReturn(false);

        when(participantTournamentRepository.save(any(ParticipantTournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParticipantTournamentDto result = service.patchUpdate(registrationId, dto);

        ArgumentCaptor<ParticipantTournament> captor =
                ArgumentCaptor.forClass(ParticipantTournament.class);

        verify(participantTournamentRepository).save(captor.capture());

        ParticipantTournament saved = captor.getValue();

        assertEquals(registrationId, saved.getId());
        assertEquals(5, saved.getStartNumber());
        assertSame(category, saved.getCategoryTournament());
        assertSame(newParticipant, saved.getParticipant());
        assertNull(saved.getCouple());
        assertEquals(SOLO, saved.getSoloCouple());

        assertEquals(registrationId, result.getId());
        assertEquals(newParticipantId, result.getParticipantId());
        assertEquals("Марія Коваленко", result.getParticipantName());
        assertEquals("Adult", result.getCategoryTournamentName());
        assertEquals("Марія Коваленко", result.getDisplayName());

        verify(participantTournamentValidator)
                .validateSoloCouple(SOLO, category);

        verify(participantTournamentValidator)
                .validateParticipantMatchesCategory(newParticipant, category);
    }

    @Test
    void patchUpdate_shouldThrowWhenMarksExist() {
        Long registrationId = 100L;
        Long newParticipantId = 11L;

        Participant oldParticipant = mock(Participant.class);
        when(oldParticipant.getId()).thenReturn(PARTICIPANT_ID);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(oldParticipant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto =
                mock(ParticipantTournamentPatchDto.class);

        when(dto.getCategoryTournamentId()).thenReturn(null);
        when(dto.getParticipantId()).thenReturn(newParticipantId);

        when(category.getId()).thenReturn(CATEGORY_ID);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(markRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(true);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.patchUpdate(registrationId, dto)
        );

        assertEquals(
                "Cannot change participant: marks already exist",
                exception.getMessage()
        );

        verify(markRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId);

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId);

        verifyNoInteractions(
                participantRepository,
                coupleRepository,
                participantTournamentValidator
        );

        verify(participantTournamentRepository, never())
                .save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenFinalMarksExist() {
        Long registrationId = 100L;
        Long newParticipantId = 11L;

        Participant oldParticipant = mock(Participant.class);
        when(oldParticipant.getId()).thenReturn(PARTICIPANT_ID);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(oldParticipant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto =
                mock(ParticipantTournamentPatchDto.class);

        when(dto.getCategoryTournamentId()).thenReturn(null);
        when(dto.getParticipantId()).thenReturn(newParticipantId);

        when(category.getId()).thenReturn(CATEGORY_ID);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(markRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(false);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.patchUpdate(registrationId, dto)
        );

        assertEquals(
                "Cannot change participant: marks already exist",
                exception.getMessage()
        );

        verifyNoInteractions(
                participantRepository,
                coupleRepository,
                participantTournamentValidator
        );

        verify(participantTournamentRepository, never())
                .save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenChangingCategoryAndMarksExist() {
        Long registrationId = 100L;
        Long newCategoryId = 2L;

        Participant oldParticipant = mock(Participant.class);
        when(oldParticipant.getId()).thenReturn(PARTICIPANT_ID);

        CategoryTournament newCategory = mock(CategoryTournament.class);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(oldParticipant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto =
                mock(ParticipantTournamentPatchDto.class);

        when(dto.getCategoryTournamentId()).thenReturn(newCategoryId);

        when(category.getId()).thenReturn(CATEGORY_ID);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(categoryTournamentRepository.findById(newCategoryId))
                .thenReturn(Optional.of(newCategory));

        when(markRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(true);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.patchUpdate(registrationId, dto)
        );

        assertEquals(
                "Cannot change participant: marks already exist",
                exception.getMessage()
        );

        verify(categoryTournamentRepository).findById(newCategoryId);

        verify(markRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId);

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId);

        verifyNoInteractions(
                participantRepository,
                coupleRepository,
                participantTournamentValidator
        );

        verify(participantTournamentRepository, never())
                .save(any());
    }

    @Test
    void patchUpdate_shouldUpdateCategoryWhenNoMarksExist() {
        Long registrationId = 100L;
        Long newCategoryId = 2L;

        Participant oldParticipant = mock(Participant.class);
        when(oldParticipant.getId()).thenReturn(PARTICIPANT_ID);
        when(oldParticipant.getFirstnameUkr()).thenReturn("Іван");
        when(oldParticipant.getLastnameUkr()).thenReturn("Петренко");

        CategoryTournament newCategory = mock(CategoryTournament.class);
        when(category.getId()).thenReturn(CATEGORY_ID);
        when(newCategory.getId()).thenReturn(newCategoryId);
        when(newCategory.getNameCategory()).thenReturn("Junior");

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(oldParticipant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();
        dto.setCategoryTournamentId(newCategoryId);
        dto.setSoloCouple(SOLO);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(categoryTournamentRepository.findById(newCategoryId))
                .thenReturn(Optional.of(newCategory));

        when(markRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(false);

        when(finalMarkRepository
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId))
                .thenReturn(false);

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndParticipantIdAndIdNot(
                        newCategoryId, PARTICIPANT_ID, registrationId))
                .thenReturn(false);

        when(participantTournamentRepository.save(any(ParticipantTournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParticipantTournamentDto result = service.patchUpdate(registrationId, dto);

        ArgumentCaptor<ParticipantTournament> captor =
                ArgumentCaptor.forClass(ParticipantTournament.class);

        verify(participantTournamentRepository).save(captor.capture());

        ParticipantTournament saved = captor.getValue();

        assertEquals(registrationId, saved.getId());
        assertEquals(5, saved.getStartNumber());
        assertSame(newCategory, saved.getCategoryTournament());
        assertSame(oldParticipant, saved.getParticipant());
        assertNull(saved.getCouple());
        assertEquals(SOLO, saved.getSoloCouple());

        assertEquals(newCategoryId, result.getCategoryTournamentId());
        assertEquals("Junior", result.getCategoryTournamentName());
        assertEquals(PARTICIPANT_ID, result.getParticipantId());
        assertEquals("Іван Петренко", result.getDisplayName());

        verify(participantTournamentValidator)
                .validateSoloCouple(SOLO, newCategory);

        verify(participantTournamentValidator)
                .validateParticipantMatchesCategory(oldParticipant, newCategory);
    }

    @Test
    void patchUpdate_shouldUpdateWhenParticipantRemainsTheSame() {
        Long registrationId = 100L;

        Participant participant = mock(Participant.class);

        when(participant.getId()).thenReturn(PARTICIPANT_ID);
        when(participant.getFirstnameUkr()).thenReturn("Іван");
        when(participant.getLastnameUkr()).thenReturn("Петренко");

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Adult");

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();
        dto.setParticipantId(PARTICIPANT_ID);
        dto.setSoloCouple(SOLO);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(participantRepository.findById(PARTICIPANT_ID))
                .thenReturn(Optional.of(participant));

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndParticipantIdAndIdNot(
                        CATEGORY_ID, PARTICIPANT_ID, registrationId))
                .thenReturn(false);

        when(participantTournamentRepository.save(any(ParticipantTournament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParticipantTournamentDto result = service.patchUpdate(registrationId, dto);

        assertEquals(registrationId, result.getId());
        assertEquals(PARTICIPANT_ID, result.getParticipantId());
        assertEquals("Іван Петренко", result.getDisplayName());

        verifyNoInteractions(markRepository, finalMarkRepository);

        verify(participantTournamentValidator)
                .validateSoloCouple(SOLO, category);

        verify(participantTournamentValidator)
                .validateParticipantMatchesCategory(participant, category);

        verify(participantTournamentRepository)
                .existsByCategoryTournamentIdAndParticipantIdAndIdNot(
                        CATEGORY_ID, PARTICIPANT_ID, registrationId);

        verify(participantTournamentRepository).save(existing);
    }

    @Test
    void patchUpdate_shouldThrowWhenRegistrationNotFound() {
        Long registrationId = 100L;

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.patchUpdate(registrationId, dto)
        );

        assertEquals("ParticipantTournament not found", exception.getMessage());

        verify(participantTournamentRepository).findById(registrationId);

        verifyNoInteractions(
                categoryTournamentRepository,
                participantRepository,
                coupleRepository,
                markRepository,
                finalMarkRepository,
                participantTournamentValidator
        );

        verify(participantTournamentRepository, never())
                .save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenCategoryNotFound() {
        Long registrationId = 100L;
        Long newCategoryId = 2L;

        Participant participant = mock(Participant.class);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();
        dto.setCategoryTournamentId(newCategoryId);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(categoryTournamentRepository.findById(newCategoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.patchUpdate(registrationId, dto)
        );

        verify(categoryTournamentRepository).findById(newCategoryId);

        verifyNoInteractions(
                participantRepository,
                coupleRepository,
                markRepository,
                finalMarkRepository,
                participantTournamentValidator
        );

        verify(participantTournamentRepository, never())
                .save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenParticipantNotFound() {
        Long registrationId = 100L;
        Long newParticipantId = 11L;

        Participant oldParticipant = mock(Participant.class);

        when(category.getId()).thenReturn(CATEGORY_ID);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(oldParticipant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();
        dto.setParticipantId(newParticipantId);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(participantRepository.findById(newParticipantId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.patchUpdate(registrationId, dto)
        );

        assertEquals("Participant not found", exception.getMessage());

        verify(participantRepository).findById(newParticipantId);

        verifyNoInteractions(coupleRepository);

        verify(participantTournamentValidator)
                .validateSoloCouple(SOLO, category);

        verify(participantTournamentValidator, never())
                .validateParticipantMatchesCategory(any(), any());

        verify(participantTournamentValidator, never())
                .validateCoupleMatchesCategory(any(), any());

        verify(markRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId
                );

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId
                );

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenCoupleNotFound() {
        Long registrationId = 100L;
        Long newCoupleId = 20L;

        Couple oldCouple = mock(Couple.class);

        when(category.getId()).thenReturn(CATEGORY_ID);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .couple(oldCouple)
                .categoryTournament(category)
                .soloCouple(COUPLE)
                .build();

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();
        dto.setCoupleId(newCoupleId);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(coupleRepository.findById(newCoupleId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.patchUpdate(registrationId, dto)
        );

        assertEquals("Couple not found", exception.getMessage());

        verify(coupleRepository).findById(newCoupleId);

        verifyNoInteractions(participantRepository);

        verify(participantTournamentValidator)
                .validateSoloCouple(COUPLE, category);

        verify(participantTournamentValidator, never())
                .validateParticipantMatchesCategory(any(), any());

        verify(participantTournamentValidator, never())
                .validateCoupleMatchesCategory(any(), any());

        verify(markRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId
                );

        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(
                        CATEGORY_ID, registrationId
                );

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenParticipantDoesNotMatchCategory() {
        Long registrationId = 100L;

        Participant participant = mock(Participant.class);

        when(category.getId()).thenReturn(CATEGORY_ID);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();

        doThrow(new IllegalArgumentException("Participant does not match category"))
                .when(participantTournamentValidator)
                .validateParticipantMatchesCategory(participant, category);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> service.patchUpdate(registrationId, dto));

        verify(participantTournamentValidator).validateSoloCouple(SOLO, category);
        verify(participantTournamentValidator)
                .validateParticipantMatchesCategory(participant, category);

        verify(participantTournamentValidator, never())
                .validateCoupleMatchesCategory(any(), any());

        verifyNoInteractions(markRepository, finalMarkRepository);
        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenCoupleDoesNotMatchCategory() {
        Long registrationId = 100L;

        Couple couple = mock(Couple.class);

        when(category.getId()).thenReturn(CATEGORY_ID);

        ParticipantTournament  existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .couple(couple)
                .categoryTournament(category)
                .soloCouple(COUPLE)
                .build();

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();

        doThrow(new IllegalArgumentException("Couple does not match category"))
                .when(participantTournamentValidator)
                .validateCoupleMatchesCategory(couple, category);

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> service.patchUpdate(registrationId, dto));

        verify(participantTournamentValidator).validateSoloCouple(COUPLE, category);
        verify(participantTournamentValidator)
                .validateCoupleMatchesCategory(couple, category);

        verify(participantTournamentValidator, never())
                .validateParticipantMatchesCategory(any(), any());

        verifyNoInteractions(markRepository, finalMarkRepository);
        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenParticipantIsRequired() {
        Long registrationId = 100L;

//        when(participantTournamentRepository.findById(registrationId))
//                .thenReturn(Optional.of(existing));

        when(category.getId()).thenReturn(CATEGORY_ID);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();

        assertThrows(IllegalArgumentException.class,
                () -> service.patchUpdate(registrationId, dto));

        verify(participantTournamentValidator).validateSoloCouple(SOLO, category);

        verify(participantTournamentValidator, never())
                .validateParticipantMatchesCategory(any(), any());

        verify(participantTournamentRepository, never()).save(any());

        verifyNoInteractions(markRepository, finalMarkRepository);
    }

    @Test
    void patchUpdate_shouldThrowWhenCoupleIsRequired() {
        Long registrationId = 100L;

        when(category.getId()).thenReturn(CATEGORY_ID);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .categoryTournament(category)
                .soloCouple(COUPLE)
                .build();

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();

        assertThrows(IllegalArgumentException.class,
                () -> service.patchUpdate(registrationId, dto));

        verify(participantTournamentValidator).validateSoloCouple(COUPLE, category);

        verify(participantTournamentValidator, never())
                .validateParticipantMatchesCategory(any(), any());

        verify(participantTournamentValidator, never())
                .validateCoupleMatchesCategory(any(), any());

        verifyNoInteractions(markRepository, finalMarkRepository);

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenParticipantAlreadyRegistered() {
        Long registrationId = 100L;
        Long newParticipantId = 11L;

        Participant oldParticipant = mock(Participant.class);
        Participant newParticipant = mock(Participant.class);

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(newParticipant.getId()).thenReturn(newParticipantId);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(oldParticipant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(participantRepository.findById(newParticipantId))
                .thenReturn(Optional.of(newParticipant));

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndParticipantIdAndIdNot(
                        CATEGORY_ID, newParticipantId, registrationId))
                .thenReturn(true);

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();
        dto.setParticipantId(newParticipantId);

        assertThrows(IllegalArgumentException.class,
                () -> service.patchUpdate(registrationId, dto));

        verify(markRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(CATEGORY_ID, registrationId);
        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(CATEGORY_ID, registrationId);

        verify(participantRepository).findById(newParticipantId);

        verify(participantTournamentValidator)
                .validateSoloCouple(SOLO, category);
        verify(participantTournamentValidator)
                .validateParticipantMatchesCategory(newParticipant, category);

        verify(participantTournamentRepository)
                .existsByCategoryTournamentIdAndParticipantIdAndIdNot(
                        CATEGORY_ID, newParticipantId, registrationId);

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void patchUpdate_shouldThrowWhenCoupleAlreadyRegistered() {
        Long registrationId = 100L;
        Long newCoupleId = 20L;

        Couple oldCouple = mock(Couple.class);
        Couple newCouple = mock(Couple.class);

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(newCouple.getId()).thenReturn(newCoupleId);

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .couple(oldCouple)
                .categoryTournament(category)
                .soloCouple(COUPLE)
                .build();

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        when(coupleRepository.findById(newCoupleId))
                .thenReturn(Optional.of(newCouple));

        when(participantTournamentRepository
                .existsByCategoryTournamentIdAndCoupleIdAndIdNot(
                        CATEGORY_ID, newCoupleId, registrationId))
                .thenReturn(true);

        ParticipantTournamentPatchDto dto = new ParticipantTournamentPatchDto();
        dto.setCoupleId(newCoupleId);

        assertThrows(IllegalArgumentException.class,
                () -> service.patchUpdate(registrationId, dto));

        verify(markRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(CATEGORY_ID, registrationId);
        verify(finalMarkRepository)
                .existsByCategoryTournamentIdAndParticipantTournamentId(CATEGORY_ID, registrationId);

        verify(coupleRepository).findById(newCoupleId);

        verify(participantTournamentValidator)
                .validateSoloCouple(COUPLE, category);
        verify(participantTournamentValidator)
                .validateCoupleMatchesCategory(newCouple, category);

        verify(participantTournamentRepository)
                .existsByCategoryTournamentIdAndCoupleIdAndIdNot(
                        CATEGORY_ID, newCoupleId, registrationId);

        verify(participantTournamentRepository, never()).save(any());
    }

    @Test
    void getAll_shouldReturnAllRegistrations() {
        Participant participant = mock(Participant.class);

        when(participant.getFirstnameUkr()).thenReturn("Іван");
        when(participant.getLastnameUkr()).thenReturn("Петренко");
        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Юніори");

        ParticipantTournament first = ParticipantTournament.builder()
                .id(1L)
                .startNumber(10)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournament second = ParticipantTournament.builder()
                .id(2L)
                .startNumber(11)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        when(participantTournamentRepository.findAll())
                .thenReturn(List.of(first, second));

        List<ParticipantTournamentDto> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(10, result.get(0).getStartNumber());
        assertEquals("Іван Петренко", result.get(0).getParticipantName());
        assertEquals("Юніори", result.get(0).getCategoryTournamentName());

        assertEquals(2L, result.get(1).getId());
        assertEquals(11, result.get(1).getStartNumber());

        verify(participantTournamentRepository).findAll();
    }

    @Test
    void getById_shouldReturnRegistrationWhenFound() {
        Long registrationId = 100L;

        Participant participant = mock(Participant.class);

        when(participant.getFirstnameUkr()).thenReturn("Іван");
        when(participant.getLastnameUkr()).thenReturn("Петренко");
        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Юніори");

        ParticipantTournament existing = ParticipantTournament.builder()
                .id(registrationId)
                .startNumber(5)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.of(existing));

        Optional<ParticipantTournamentDto> result = service.getById(registrationId);

        assertTrue(result.isPresent());
        assertEquals(registrationId, result.get().getId());
        assertEquals(5, result.get().getStartNumber());
        assertEquals("Іван Петренко", result.get().getParticipantName());
        assertEquals("Юніори", result.get().getCategoryTournamentName());

        verify(participantTournamentRepository).findById(registrationId);
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        Long registrationId = 100L;

        when(participantTournamentRepository.findById(registrationId))
                .thenReturn(Optional.empty());

        Optional<ParticipantTournamentDto> result = service.getById(registrationId);

        assertTrue(result.isEmpty());

        verify(participantTournamentRepository).findById(registrationId);
    }

    @Test
    void getByCategory_shouldReturnRegistrationsForCategory() {
        Participant participant = mock(Participant.class);

        when(participant.getFirstnameUkr()).thenReturn("Іван");
        when(participant.getLastnameUkr()).thenReturn("Петренко");
        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Юніори");

        ParticipantTournament first = ParticipantTournament.builder()
                .id(1L)
                .startNumber(10)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournament second = ParticipantTournament.builder()
                .id(2L)
                .startNumber(11)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        when(participantTournamentRepository.findByCategoryTournamentId(CATEGORY_ID))
                .thenReturn(List.of(first, second));

        List<ParticipantTournamentDto> result = service.getByCategory(CATEGORY_ID);

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals(10, result.get(0).getStartNumber());
        assertEquals("Іван Петренко", result.get(0).getParticipantName());

        assertEquals(2L, result.get(1).getId());
        assertEquals(11, result.get(1).getStartNumber());

        verify(participantTournamentRepository).findByCategoryTournamentId(CATEGORY_ID);
    }

    @Test
    void getByCategory_shouldReturnEmptyListWhenNoRegistrations() {
        when(participantTournamentRepository.findByCategoryTournamentId(CATEGORY_ID))
                .thenReturn(List.of());

        List<ParticipantTournamentDto> result = service.getByCategory(CATEGORY_ID);

        assertTrue(result.isEmpty());

        verify(participantTournamentRepository).findByCategoryTournamentId(CATEGORY_ID);
    }

    @Test
    void getByCategoryAndType_shouldReturnRegistrationsForCategoryAndType() {
        Participant participant = mock(Participant.class);

        when(participant.getFirstnameUkr()).thenReturn("Іван");
        when(participant.getLastnameUkr()).thenReturn("Петренко");
        when(category.getId()).thenReturn(CATEGORY_ID);
        when(category.getNameCategory()).thenReturn("Юніори");

        ParticipantTournament first = ParticipantTournament.builder()
                .id(1L)
                .startNumber(10)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        ParticipantTournament second = ParticipantTournament.builder()
                .id(2L)
                .startNumber(11)
                .participant(participant)
                .categoryTournament(category)
                .soloCouple(SOLO)
                .build();

        when(participantTournamentRepository
                .findByCategoryTournamentIdAndSoloCouple(CATEGORY_ID, SOLO))
                .thenReturn(List.of(first, second));

        List<ParticipantTournamentDto> result =
                service.getByCategoryAndType(CATEGORY_ID, SOLO);

        assertEquals(2, result.size());

        assertEquals(1L, result.getFirst().getId());
        assertEquals(10, result.getFirst().getStartNumber());
        assertEquals("Іван Петренко", result.get(0).getParticipantName());
        assertEquals(SOLO, result.get(0).getSoloCouple());

        assertEquals(2L, result.get(1).getId());
        assertEquals(11, result.get(1).getStartNumber());

        verify(participantTournamentRepository)
                .findByCategoryTournamentIdAndSoloCouple(CATEGORY_ID, SOLO);
    }

    @Test
    void getByCategoryAndType_shouldReturnEmptyListWhenNoRegistrations() {
        when(participantTournamentRepository
                .findByCategoryTournamentIdAndSoloCouple(CATEGORY_ID, SOLO))
                .thenReturn(List.of());

        List<ParticipantTournamentDto> result =
                service.getByCategoryAndType(CATEGORY_ID, SOLO);

        assertTrue(result.isEmpty());

        verify(participantTournamentRepository)
                .findByCategoryTournamentIdAndSoloCouple(CATEGORY_ID, SOLO);
    }

    @Test
    void deleteById_shouldDeleteRegistration() {
        Long registrationId = 100L;

        service.deleteById(registrationId);

        verify(participantTournamentRepository).deleteById(registrationId);
    }

    @Test
    void selectParticipants_shouldReturnAllParticipantsWhenRoundIsDefault() {
        when(category.getId()).thenReturn(CATEGORY_ID);
        when(roundSettings.getRound()).thenReturn(RoundEnum.DEFAULT);

        ParticipantTournament participantTournament = mock(ParticipantTournament.class);

        when(participantTournamentRepository.findByCategoryTournamentId(CATEGORY_ID))
                .thenReturn(List.of(participantTournament));

        List<ParticipantTournamentDto> result =
                service.selectParticipants(category, roundSettings, false);

        assertEquals(1, result.size());

        verify(participantTournamentRepository)
                .findByCategoryTournamentId(CATEGORY_ID);

        verifyNoInteractions(participantPassingService);
        verifyNoInteractions(markRepository);
    }

    @Test
    void selectParticipants_shouldLockRoundAndCalculatePassingWhenSocial() {
        RoundEnum round = RoundEnum.QUARTER_FINAL;
        int couplesToNextRound = 12;

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(roundSettings.getRound()).thenReturn(round);
        when(roundSettings.getCouplesToNextRound()).thenReturn(couplesToNextRound);

        List<ParticipantTournamentDto> expected = List.of(
                new ParticipantTournamentDto()
        );

        when(participantPassingService.calculatePassing(
                CATEGORY_ID, round, couplesToNextRound
        )).thenReturn(expected);

        List<ParticipantTournamentDto> result =
                service.selectParticipants(category, roundSettings, true);

        assertEquals(expected, result);

        verify(markRepository).lockRound(CATEGORY_ID, round);

        verify(participantPassingService)
                .calculatePassing(CATEGORY_ID, round, couplesToNextRound);

        verifyNoInteractions(participantTournamentRepository);
    }

    @Test
    void selectParticipants_shouldCalculatePassingWithoutLockWhenNotSocial() {
        RoundEnum round = RoundEnum.SEMI_FINAL;
        int couplesToNextRound = 6;

        when(category.getId()).thenReturn(CATEGORY_ID);
        when(roundSettings.getRound()).thenReturn(round);
        when(roundSettings.getCouplesToNextRound()).thenReturn(couplesToNextRound);

        List<ParticipantTournamentDto> expected = List.of(
                new ParticipantTournamentDto()
        );

        when(participantPassingService.calculatePassing(
                CATEGORY_ID, round, couplesToNextRound
        )).thenReturn(expected);

        List<ParticipantTournamentDto> result =
                service.selectParticipants(category, roundSettings, false);

        assertEquals(expected, result);

        verify(participantPassingService)
                .calculatePassing(CATEGORY_ID, round, couplesToNextRound);

        verifyNoInteractions(markRepository);
        verifyNoInteractions(participantTournamentRepository);
    }

}
