package com.dances.integration;

import com.dances.entity.Club;
import com.dances.entity.Users;
import com.dances.model.RoleEnum;
import com.dances.repository.UsersRepository;
import com.dances.security.JwtService;
import com.dances.service.ClubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ClubControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClubService clubService;

    @Test
    @Transactional
    void shouldCreateClub() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("club-test@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        Club club = Club.builder()
                .nameClub("Controller Test Club")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/clubs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(club)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nameClub").value("Controller Test Club"));
    }

    @Test
    @Transactional
    void shouldReturnAllClubs() throws Exception {
        // Arrange
        Users user = createAdminUser();

        String token = jwtService.generateAccessToken(user);

        clubService.createClub(
                Club.builder().nameClub("First Club").build()
        );

        clubService.createClub(
                Club.builder().nameClub("Second Club").build()
        );

        // Act & Assert
        mockMvc.perform(get("/api/clubs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nameClub == 'First Club')]").exists())
                .andExpect(jsonPath("$[?(@.nameClub == 'Second Club')]").exists());
    }

    @Test
    @Transactional
    void shouldReturnClubById() throws Exception {
        // Arrange
        Users user = createAdminUser();
        String token = jwtService.generateAccessToken(user);

        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Club By Id")
                        .build()
        );

        // Act & Assert
        mockMvc.perform(get("/api/clubs/{id}", club.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(club.getId()))
                .andExpect(jsonPath("$.nameClub").value("Club By Id"));
    }

    @Test
    @Transactional
    void shouldReturnNotFoundWhenClubDoesNotExist() throws Exception {
        // Arrange
        Users user = createAdminUser();
        String token = jwtService.generateAccessToken(user);

        // Act & Assert
        mockMvc.perform(get("/api/clubs/{id}", -1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private Users createAdminUser() {
        Users user = new Users();
        user.setEmail("club-test@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        return usersRepository.save(user);
    }

    @Test
    @Transactional
    void shouldUpdateClub() throws Exception {
        // Arrange
        Users user = createAdminUser();
        String token = jwtService.generateAccessToken(user);

        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Old Club Name")
                        .build()
        );

        Club updatedClub = Club.builder()
                .nameClub("New Club Name")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/clubs/{id}", club.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedClub)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(club.getId()))
                .andExpect(jsonPath("$.nameClub").value("New Club Name"));
    }

    @Test
    @Transactional
    void shouldDeleteClub() throws Exception {
        // Arrange
        Users user = createAdminUser();
        String token = jwtService.generateAccessToken(user);

        Club club = clubService.createClub(
                Club.builder()
                        .nameClub("Club To Delete")
                        .build()
        );

        // Act & Assert
        mockMvc.perform(delete("/api/clubs/{id}", club.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        assertTrue(clubService.getClubById(club.getId()).isEmpty());
    }
}
