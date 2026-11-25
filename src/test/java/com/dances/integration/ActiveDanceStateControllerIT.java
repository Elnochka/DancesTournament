package com.dances.integration;

import com.dances.entity.ActiveDanceState;
import com.dances.entity.Users;
import com.dances.entity.dto.ActiveDanceStateRequestDto;
import com.dances.model.RoleEnum;
import com.dances.repository.ActiveDanceStateRepository;
import com.dances.repository.UsersRepository;
import com.dances.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class ActiveDanceStateControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ActiveDanceStateRepository activeDanceStateRepository;

    @Test
    @Transactional
    void getCurrent_asAdmin_shouldReturn200() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("active-dance-admin@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        // Act & Assert
        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void getCurrent_asJudge_shouldReturn403() throws Exception {
        Users user = new Users();
        user.setEmail("active-dance-judge@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Judge");
        user.setActive(true);
        user.setRole(RoleEnum.JUDGE);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrent_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/active-dance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void update_asAdmin_shouldReturn200() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("active-dance-put-admin@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        ActiveDanceStateRequestDto dto = ActiveDanceStateRequestDto.builder()
                .categoryTournamentId(1L)
                .roundSettingsId(2L)
                .danceId(3L)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/active-dance")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void update_asJudge_shouldReturn403() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("active-dance-put-judge@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Judge");
        user.setActive(true);
        user.setRole(RoleEnum.JUDGE);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        ActiveDanceStateRequestDto dto = ActiveDanceStateRequestDto.builder()
                .categoryTournamentId(1L)
                .roundSettingsId(2L)
                .danceId(3L)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/active-dance")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_withoutToken_shouldReturn401() throws Exception {
        ActiveDanceStateRequestDto dto = ActiveDanceStateRequestDto.builder()
                .categoryTournamentId(1L)
                .roundSettingsId(2L)
                .danceId(3L)
                .build();

        mockMvc.perform(put("/api/active-dance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void update_asAdmin_shouldSaveAndReturnUpdatedState() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("active-dance-put-admin@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        ActiveDanceStateRequestDto dto = ActiveDanceStateRequestDto.builder()
                .categoryTournamentId(1L)
                .roundSettingsId(2L)
                .danceId(3L)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/active-dance")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryTournamentId").value(1))
                .andExpect(jsonPath("$.roundSettingsId").value(2))
                .andExpect(jsonPath("$.danceId").value(3));

        ActiveDanceState savedState = activeDanceStateRepository.findById(1L)
                .orElseThrow();

        assertEquals(1L, savedState.getCategoryTournamentId());
        assertEquals(2L, savedState.getRoundSettingsId());
        assertEquals(3L, savedState.getDanceId());
    }

    @Test
    @Transactional
    void getCurrent_asAdmin_shouldReturnCurrentState() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("active-dance-get-admin@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        ActiveDanceState state = new ActiveDanceState();
        state.setId(1L);
        state.setCategoryTournamentId(1L);
        state.setRoundSettingsId(2L);
        state.setDanceId(3L);

        activeDanceStateRepository.save(state);

        // Act & Assert
        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryTournamentId").value(1))
                .andExpect(jsonPath("$.roundSettingsId").value(2))
                .andExpect(jsonPath("$.danceId").value(3));
    }

    @Test
    @Transactional
    void getCurrent_asAdmin_whenStateNotExists_shouldReturnEmptyBody() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("active-dance-empty-admin@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        user = usersRepository.save(user);

        String token = jwtService.generateAccessToken(user);

        // Act & Assert
        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryTournamentId").value((Object) null))
                .andExpect(jsonPath("$.roundSettingsId").value((Object) null))
                .andExpect(jsonPath("$.danceId").value((Object) null));
    }

    @Test
    void getCurrent_withInvalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrent_withExpiredToken_shouldReturn401() throws Exception {
        // Arrange
        Users user = new Users();
        user.setEmail("active-dance-empty-admin@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        user = usersRepository.save(user);

        String secret = ReflectionTestUtils.getField(jwtService, "secret")
                .toString();

        String expiredToken = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(
                        Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();

        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrent_withJudgeToken_shouldReturn403() throws Exception {
        Users judge = createJudgeUser();

        String token = jwtService.generateAccessToken(judge);

        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrent_withDeletedUserToken_shouldReturn401() throws Exception {
        Users admin = createAdminUser();
        String token = jwtService.generateAccessToken(admin);

        usersRepository.deleteById(admin.getId());
        usersRepository.flush();

        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrent_withValidAdminToken_shouldReturn200() throws Exception {
        Users admin = createAdminUser();
        String token = jwtService.generateAccessToken(admin);

        mockMvc.perform(get("/api/active-dance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void optionsRequest_shouldNotReturn401() throws Exception {
        mockMvc.perform(options("/api/organizations")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk());
    }

    private Users createAdminUser() {
        Users user = new Users();
        user.setEmail("club-test-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setName("Test");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(RoleEnum.ADMIN);

        return usersRepository.save(user);
    }

    private Users createJudgeUser() {
        Users user = new Users();
        user.setEmail("club-test1-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setName("Test1");
        user.setLastName("Judge");
        user.setActive(true);
        user.setRole(RoleEnum.JUDGE);

        return usersRepository.save(user);
    }
}
