package com.dances.integration;

import com.dances.entity.Club;
import com.dances.repository.ClubRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

class ClubRepositoryIT extends IntegrationTestBase {

    @Autowired
    private ClubRepository clubRepository;

    @Test
    @Transactional
    void shouldSaveAndFindClub() {
        // Arrange
        Club club = Club.builder()
                .nameClub("Integration Test Club")
                .build();

        // Act
        Club savedClub = clubRepository.save(club);

        // Assert
        assertNotNull(savedClub.getId());
        assertEquals("Integration Test Club", savedClub.getNameClub());

        Club foundClub = clubRepository.findById(savedClub.getId())
                .orElseThrow();

        assertEquals(savedClub.getId(), foundClub.getId());
        assertEquals("Integration Test Club", foundClub.getNameClub());

        assertTrue(clubRepository.existsByNameClub("Integration Test Club"));
    }

    @Test
    void shouldReturnFalseWhenClubDoesNotExist() {
        assertFalse(clubRepository.existsByNameClub("Nonexistent Club"));
    }
}
