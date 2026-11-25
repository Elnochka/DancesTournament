package com.dances.entity.dto;

import com.dances.entity.CategoryParticipant;
import com.dances.entity.CategoryTournament;
import com.dances.entity.KindOfDance;
import com.dances.entity.Klass;
import com.dances.model.SoloCoupleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CategoryTournamentDtoTest {

    @Test
    void fromEntity_shouldReturnNullWhenEntityIsNull() {
        CategoryTournamentDto.fromEntity(null);
        CategoryTournamentDto result = null;

        assertNull(result);
    }

    @Test
    void fromEntity_shouldMapAllFields() {
        CategoryTournament entity = getCategoryTournament();

        CategoryTournamentDto result = CategoryTournamentDto.fromEntity(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Adult", result.getNameCategory());
        assertEquals(SoloCoupleEnum.COUPLE, result.getSoloCouple());
        assertEquals(2L, result.getKlassId());
        assertEquals("A", result.getKlassName());
        assertEquals(3L, result.getCategoryParticipantId());
        assertEquals("Adults", result.getCategoryParticipantName());
        assertEquals(4L, result.getKindOfDanceId());
        assertEquals("Standard", result.getKindOfDanceName());
    }

    private static CategoryTournament getCategoryTournament() {
        CategoryTournament entity = new CategoryTournament();
        entity.setId(1L);
        entity.setNameCategory("Adult");
        entity.setSoloCouple(SoloCoupleEnum.COUPLE);

        Klass klass = new Klass();
        klass.setId(2L);
        klass.setNameKlass("A");

        CategoryParticipant categoryParticipant = new CategoryParticipant();
        categoryParticipant.setId(3L);
        categoryParticipant.setNameCategory("Adults");

        KindOfDance kindOfDance = new KindOfDance();
        kindOfDance.setId(4L);
        kindOfDance.setNameKindOfDance("Standard");

        entity.setKlass(klass);
        entity.setCategoryParticipant(categoryParticipant);
        entity.setKindOfDance(kindOfDance);
        return entity;
    }

    @Test
    void fromEntity_shouldMapNullRelatedEntities() {
        CategoryTournament entity = new CategoryTournament();
        entity.setId(1L);
        entity.setNameCategory("Adult");
        entity.setSoloCouple(SoloCoupleEnum.COUPLE);

        entity.setKlass(null);
        entity.setCategoryParticipant(null);
        entity.setKindOfDance(null);

        CategoryTournamentDto result = CategoryTournamentDto.fromEntity(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Adult", result.getNameCategory());
        assertEquals(SoloCoupleEnum.COUPLE, result.getSoloCouple());

        assertNull(result.getKlassId());
        assertNull(result.getKlassName());
        assertNull(result.getCategoryParticipantId());
        assertNull(result.getCategoryParticipantName());
        assertNull(result.getKindOfDanceId());
        assertNull(result.getKindOfDanceName());
    }
}
