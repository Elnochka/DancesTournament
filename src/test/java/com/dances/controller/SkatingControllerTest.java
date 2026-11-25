package com.dances.controller;

import com.dances.entity.dto.SkatingResultDto;
import com.dances.repository.UsersRepository;
import com.dances.security.JwtService;
import com.dances.security.SecurityConfiguration;
import com.dances.service.impl.SkatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(
        controllers = SkatingController.class,
        excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class
)
@Import(SecurityConfiguration.class)
class SkatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkatingService skatingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(roles = "ADMIN")
    void calculate_shouldReturnResults() throws Exception {
        Long categoryId = 1L;

        List<SkatingResultDto> results = List.of();

        when(skatingService.calculateFinalPlaces(categoryId))
                .thenReturn(results);

        mockMvc.perform(post("/api/skating/final")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(skatingService).calculateFinalPlaces(categoryId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getResults_shouldReturnResults() throws Exception {
        Long categoryId = 1L;

        List<SkatingResultDto> results = List.of();

        when(skatingService.getResults(categoryId))
                .thenReturn(results);

        mockMvc.perform(get("/api/skating/results")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(skatingService).getResults(categoryId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void moveToNextRound_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/skating/move-next-round")
                        .param("categoryId", "1")
                        .param("roundSettingsId", "2")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(skatingService)
                .createMarksOrFinalMarksForCategoryAndRound(1L, 2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void moveToNextDance_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/skating/move-next-dance")
                        .param("categoryId", "1")
                        .param("roundSettingsId", "2")
                        .param("danceId", "3")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(skatingService)
                .lockDance(1L, 2L, 3L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void calculate_shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/skating/final")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(skatingService);
    }

    @Test
    void calculate_shouldReturnUnauthorizedForUnauthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/skating/final")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(skatingService);
    }
}
