package com.dances.integration;

import com.dances.entity.Club;
import com.dances.exception.DuplicateException;
import com.dances.exception.NotFoundException;
import com.dances.service.ClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClubServiceIT extends IntegrationTestBase {

    @Autowired
    private ClubService clubService;

    @Test
    @Transactional
    void shouldCreateClub() {
        // Arrange
        Club club = Club.builder()
                .nameClub("Integration Service Club")
                .build();

        // Act
        Club createdClub = clubService.createClub(club);

        // Assert
        assertNotNull(createdClub.getId());
        assertEquals("Integration Service Club", createdClub.getNameClub());
    }

    @Test
    @Transactional
    void shouldThrowExceptionWhenClubNameAlreadyExists() {
        // Arrange
        Club club = Club.builder()
                .nameClub("Existing Club")
                .build();

        clubService.createClub(club);

        Club duplicateClub = Club.builder()
                .nameClub("Existing Club")
                .build();

        // Act & Assert
        DuplicateException exception = assertThrows(
                DuplicateException.class,
                () -> clubService.createClub(duplicateClub)
        );

        assertEquals(
                "Club with this name already exists",
                exception.getMessage()
        );
    }

    @Test
    @Transactional
    void shouldReturnAllClubs() {
        // Arrange
        clubService.createClub(
                Club.builder().nameClub("Club One").build()
        );

        clubService.createClub(
                Club.builder().nameClub("Club Two").build()
        );

        // Act
        List<Club> clubs = clubService.getAllClubs();

        // Assert
        assertTrue(clubs.stream()
                .anyMatch(club -> club.getNameClub().equals("Club One")));

        assertTrue(clubs.stream()
                .anyMatch(club -> club.getNameClub().equals("Club Two")));
    }

    @Test
    @Transactional
    void shouldReturnClubById() {
        // Arrange
        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Club By Id")
                        .build()
        );

        // Act
        Optional<Club> result = clubService.getClubById(club.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(club.getId(), result.get().getId());
        assertEquals("Club By Id", result.get().getNameClub());
    }

    @Test
    @Transactional
    void shouldUpdateClub() {
        // Arrange
        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Old Club Name")
                        .build()
        );

        Club updatedClub = Club.builder()
                .nameClub("New Club Name")
                .build();

        // Act
        Club result = clubService.updateClub(club.getId(), updatedClub);

        // Assert
        assertEquals(club.getId(), result.getId());
        assertEquals("New Club Name", result.getNameClub());

        Club fromDatabase = clubService.getClubById(club.getId())
                .orElseThrow();

        assertEquals("New Club Name", fromDatabase.getNameClub());
    }

    @Test
    @Transactional
    void shouldThrowExceptionWhenUpdatingNonexistentClub() {
        // Arrange
        Club updatedClub = Club.builder()
                .nameClub("Updated Club")
                .build();

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> clubService.updateClub(-1L, updatedClub)
        );

        assertEquals("Club not found", exception.getMessage());
    }

    @Test
    @Transactional
    void shouldDeleteClub() {
        // Arrange
        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Club To Delete")
                        .build()
        );

        Long clubId = club.getId();

        // Act
        clubService.deleteClub(clubId);

        // Assert
        assertTrue(clubService.getClubById(clubId).isEmpty());
    }

    @Test
    @Transactional
    void shouldUpdateClubWithSameName() {
        // Arrange
        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Club One")
                        .build()
        );

        // Act
        Club updatedClub = clubService.updateClub(
                club.getId(),
                Club.builder()
                        .nameClub("Club One")
                        .build()
        );

        // Assert
        assertEquals("Club One", updatedClub.getNameClub());
    }

    @Test
    @Transactional
    void shouldThrowExceptionWhenUpdatingClubWithExistingName() {
        // Arrange
        clubService.createClub(
                Club.builder()
                        .nameClub("Club One")
                        .build()
        );

        Club secondClub = clubService.createClub(
                Club.builder()
                        .nameClub("Club Two")
                        .build()
        );

        Club updatedClub = Club.builder()
                .nameClub("Club One")
                .build();

        // Act & Assert
        DuplicateException exception = assertThrows(
                DuplicateException.class,
                () -> clubService.updateClub(secondClub.getId(), updatedClub)
        );

        assertEquals(
                "Club with this name already exists",
                exception.getMessage()
        );
    }

    @Test
    @Transactional
    void shouldUpdateClubWithNewUniqueName() {
        // Arrange
        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Old Club Name")
                        .build()
        );

        Club updatedClub = Club.builder()
                .nameClub("New Unique Name")
                .build();

        // Act
        Club result = clubService.updateClub(club.getId(), updatedClub);

        // Assert
        assertEquals("New Unique Name", result.getNameClub());
        assertEquals(
                "New Unique Name",
                clubService.getClubById(club.getId())
                        .orElseThrow()
                        .getNameClub()
        );
    }
}
