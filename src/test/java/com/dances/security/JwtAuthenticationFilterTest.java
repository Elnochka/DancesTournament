package com.dances.security;

import com.dances.entity.Users;
import com.dances.model.RoleEnum;
import com.dances.repository.UsersRepository;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsersRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_withoutAuthorizationHeader_shouldContinueFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);

        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertTrue(chainCalled.get());

        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void doFilter_withInvalidAuthorizationHeader_shouldContinueFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic some-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertTrue(chainCalled.get());
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void doFilter_withInvalidToken_shouldClearSecurityContextAndContinueFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new MalformedJwtException("Invalid token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertTrue(chainCalled.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUsername("invalid-token");
        verifyNoInteractions(userRepository);
    }

    @Test
    void doFilter_withValidToken_shouldSetAuthentication() throws Exception {
        Users user = new Users();
        user.setEmail("admin@example.com");
        user.setRole(RoleEnum.ADMIN);

        when(jwtService.extractUsername("valid-token"))
                .thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(jwtService.isValid("valid-token", user))
                .thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertTrue(chainCalled.get());
        assertNotNull(authentication);
        assertEquals(user, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        ));

        verify(jwtService).extractUsername("valid-token");
        verify(userRepository).findByEmail(user.getEmail());
        verify(jwtService).isValid("valid-token", user);
    }

    @Test
    void doFilter_withUnknownUser_shouldNotSetAuthentication() throws Exception {
        when(jwtService.extractUsername("valid-token"))
                .thenReturn("deleted@example.com");

        when(userRepository.findByEmail("deleted@example.com"))
                .thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertTrue(chainCalled.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUsername("valid-token");
        verify(userRepository).findByEmail("deleted@example.com");
        verify(jwtService, never()).isValid(anyString(), any());
    }

    @Test
    void doFilter_withInvalidUserToken_shouldNotSetAuthentication() throws Exception {
        Users user = new Users();
        user.setEmail("admin@example.com");
        user.setRole(RoleEnum.ADMIN);

        when(jwtService.extractUsername("valid-token"))
                .thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(jwtService.isValid("valid-token", user))
                .thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertTrue(chainCalled.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).isValid("valid-token", user);
    }

    @Test
    void doFilter_withExistingAuthentication_shouldNotAuthenticateAgain() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "existing-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertTrue(chainCalled.get());
        assertEquals("existing-user", authentication.getPrincipal());

        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void doFilter_withEmptyBearerToken_shouldContinueFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");

        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (req, res) -> chainCalled.set(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertTrue(chainCalled.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService, userRepository);
    }
}
