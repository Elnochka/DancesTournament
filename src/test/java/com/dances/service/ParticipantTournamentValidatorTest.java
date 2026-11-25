package com.dances.service;

import com.dances.entity.*;
import com.dances.model.SoloCoupleEnum;
import com.dances.service.impl.ParticipantTournamentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantTournamentValidatorTest {

    private ParticipantTournamentValidator validator;

    @Mock
    private Participant participant;

    @Mock
    private Participant male;

    @Mock
    private Participant female;

    @Mock
    private Couple couple;

    @Mock
    private CategoryTournament category;

    @Mock
    private CategoryParticipant categoryParticipant;

    @Mock
    private Klass klass;

    @Mock
    private KindOfDance kindOfDance;

    @BeforeEach
    void setUp() {
        validator = new ParticipantTournamentValidator();
    }

    // =========================
    // validateParticipantMatchesCategory()
    // =========================

    @Test
    void validateParticipantMatchesCategory_shouldPass() {
        mockValidParticipant(participant);

        assertDoesNotThrow(() ->
                validator.validateParticipantMatchesCategory(participant, category)
        );
    }

    @Test
    void validateParticipantMatchesCategory_shouldThrowWhenKlassDoesNotMatch() {
        Klass anotherKlass = mock(Klass.class);

        when(participant.getKlass()).thenReturn(klass);
        when(category.getKlass()).thenReturn(anotherKlass);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateParticipantMatchesCategory(participant, category)
        );
    }

    @Test
    void validateParticipantMatchesCategory_shouldThrowWhenAgeIsBelowRange() {
        mockParticipantKlass(participant);
        mockParticipantAge(participant, 30);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateParticipantMatchesCategory(participant, category)
        );
    }

    @Test
    void validateParticipantMatchesCategory_shouldThrowWhenAgeIsAboveRange() {
        mockParticipantKlass(participant);
        mockParticipantAge(participant, 30);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateParticipantMatchesCategory(participant, category)
        );
    }

    @Test
    void validateParticipantMatchesCategory_shouldAcceptMinimumAge() {
        mockParticipantKlass(participant);
        mockParticipantAge(participant, 18);
        mockParticipantDance(participant);

        assertDoesNotThrow(() ->
                validator.validateParticipantMatchesCategory(participant, category)
        );
    }

    @Test
    void validateParticipantMatchesCategory_shouldAcceptMaximumAge() {
        mockParticipantKlass(participant);
        mockParticipantAge(participant, 25);
        mockParticipantDance(participant);

        assertDoesNotThrow(() ->
                validator.validateParticipantMatchesCategory(participant, category)
        );
    }

    @Test
    void validateParticipantMatchesCategory_shouldThrowWhenDanceDoesNotMatch() {
        KindOfDance anotherDance = mock(KindOfDance.class);

        when(participant.getKlass()).thenReturn(klass);
        when(category.getKlass()).thenReturn(klass);

        when(participant.getAge()).thenReturn(20);
        mockAgeCategory();

        when(participant.getKindOfDances())
                .thenReturn(Collections.singleton(kindOfDance));
        when(category.getKindOfDance()).thenReturn(anotherDance);
        when(anotherDance.getNameKindOfDance()).thenReturn("Another dance");

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateParticipantMatchesCategory(participant, category)
        );
    }

    // =========================
    // validateCoupleMatchesCategory()
    // =========================

    @Test
    void validateCoupleMatchesCategory_shouldPassWhenBothParticipantsMatch() {
        when(couple.getParticipantMale()).thenReturn(male);
        when(couple.getParticipantFemale()).thenReturn(female);

        mockValidParticipant(male);
        mockValidParticipant(female);

        assertDoesNotThrow(() ->
                validator.validateCoupleMatchesCategory(couple, category)
        );
    }

    @Test
    void validateCoupleMatchesCategory_shouldThrowWhenMaleDoesNotMatch() {
        when(couple.getParticipantMale()).thenReturn(male);

        mockParticipantKlass(male);
        mockParticipantAge(male, 30);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateCoupleMatchesCategory(couple, category)
        );
    }

    @Test
    void validateCoupleMatchesCategory_shouldThrowWhenFemaleDoesNotMatch() {
        when(couple.getParticipantMale()).thenReturn(male);
        when(couple.getParticipantFemale()).thenReturn(female);

        mockValidParticipant(male);

        mockParticipantKlass(female);
        mockParticipantAge(female, 30);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateCoupleMatchesCategory(couple, category)
        );
    }

    // =========================
    // validateSoloCouple()
    // =========================

    @Test
    void validateSoloCouple_shouldPassForSoloCategory() {
        when(category.getSoloCouple()).thenReturn(SoloCoupleEnum.SOLO);

        assertDoesNotThrow(() ->
                validator.validateSoloCouple(SoloCoupleEnum.SOLO, category)
        );
    }

    @Test
    void validateSoloCouple_shouldPassForCoupleCategory() {
        when(category.getSoloCouple()).thenReturn(SoloCoupleEnum.COUPLE);

        assertDoesNotThrow(() ->
                validator.validateSoloCouple(SoloCoupleEnum.COUPLE, category)
        );
    }

    @Test
    void validateSoloCouple_shouldThrowWhenTypesDoNotMatch() {
        when(category.getSoloCouple()).thenReturn(SoloCoupleEnum.SOLO);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateSoloCouple(SoloCoupleEnum.COUPLE, category)
        );
    }

    @Test
    void validateSoloCouple_shouldThrowWhenRegistrationTypeIsNull() {

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateSoloCouple(null, category)
        );

    }

    // =========================
    // Test helpers
    // =========================

    private void mockValidParticipant(Participant participant) {
        mockParticipantKlass(participant);
        mockParticipantAge(participant, 25);
        mockParticipantDance(participant);
    }

    private void mockParticipantKlass(Participant participant) {
        when(participant.getKlass()).thenReturn(klass);
        when(category.getKlass()).thenReturn(klass);
    }

    private void mockParticipantAge(Participant participant, int age) {
        when(participant.getAge()).thenReturn(age);
        mockAgeCategory();
    }

    private void mockParticipantDance(Participant participant) {
        when(participant.getKindOfDances())
                .thenReturn(Collections.singleton(kindOfDance));
        when(category.getKindOfDance()).thenReturn(kindOfDance);
    }

    private void mockAgeCategory() {

        when(category.getCategoryParticipant()).thenReturn(categoryParticipant);
        when(categoryParticipant.getAge()).thenReturn(18);
        when(categoryParticipant.getAgeon()).thenReturn(25);

    }

}
