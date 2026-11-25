package com.dances.integration;

import com.dances.entity.Users;
import com.dances.model.RoleEnum;
import com.dances.repository.UsersRepository;
import com.dances.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityConfigurationIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void putWithoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/active-dance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerEndpoint_withoutToken_shouldNotReturn401() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void optionsRequest_withoutToken_shouldReturn200() throws Exception {
        mockMvc.perform(options("/api/organizations")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:5173"
                ));
    }

    @Test
    void cors_withDisallowedMethod_shouldNotAllowTrace() throws Exception {
        mockMvc.perform(options("/api/organizations")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "TRACE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cors_withDisallowedOrigin_shouldNotAllowAccess() throws Exception {
        mockMvc.perform(options("/api/organizations")
                        .header("Origin", "http://evil-example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withAdminToken_shouldNotReturn401() throws Exception {
        Users admin = createAdminUser();
        String token = jwtService.generateAccessToken(admin);

        mockMvc.perform(get("/api/organizations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withJudgeToken_shouldNotReturn401() throws Exception {
        Users judge = createJudgeUser();
        String token = jwtService.generateAccessToken(judge);

        mockMvc.perform(get("/api/organizations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminOnlyEndpoint_withJudgeToken_shouldReturn403() throws Exception {
        Users judge = createJudgeUser();
        String token = jwtService.generateAccessToken(judge);

        mockMvc.perform(post("/api/skating/final")
                        .param("categoryId", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOnlyEndpoint_withAdminToken_shouldNotReturn403() throws Exception {
        Users admin = createAdminUser();
        String token = jwtService.generateAccessToken(admin);

        mockMvc.perform(post("/api/skating/final")
                        .param("categoryId", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result ->
                        assertNotEquals(403, result.getResponse().getStatus())
                );
    }

    @Test
    void cors_withDisallowedHeader_shouldReturn403() throws Exception {
        mockMvc.perform(options("/api/organizations")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Custom-Header"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cors_withAllowedOrigin_shouldAllowCredentials() throws Exception {
        mockMvc.perform(options("/api/organizations")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Credentials", "true"
                ));
    }

    @Test
    void authenticatedRequest_shouldNotCreateHttpSession() throws Exception {
        Users admin = createAdminUser();
        String token = jwtService.generateAccessToken(admin);

        mockMvc.perform(get("/api/organizations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result ->
                        assertNull(result.getRequest().getSession(false))
        );
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