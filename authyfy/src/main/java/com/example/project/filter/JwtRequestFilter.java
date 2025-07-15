package com.example.project.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.project.service.AppUserDetailsService;
import com.example.project.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_URLS = List.of(
        "/auth/login",
        "/api/v1/register",
        "/send-reset-otp",
        "/reset-password"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Allow public endpoints
        if (PUBLIC_URLS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;

        // 1️⃣ Authorization header
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 2️⃣ Fallback to cookies
        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // 3️⃣ Validate and set context
        if (jwt != null) {
            try {
                String email = jwtUtil.extractEmail(jwt);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // 🔍 Show error if token fails
            }
        }

        filterChain.doFilter(request, response);
    }
}
