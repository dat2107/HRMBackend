package com.toto.backend.security;

import com.toto.backend.service.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        System.out.println(">>> Request path: " + path);

        if (path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/forgot-password")
                || path.startsWith("/api/auth/verify-forgot-password-otp")
                || path.startsWith("/login/oauth2")
                || path.equals("/login")
                || path.equals("/register")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/assets")
                || path.startsWith("/favicon.ico")
                || path.endsWith(".jsp")
                || path.startsWith("/WEB-INF")) {

            chain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);
        System.out.println(">>> Token lấy ra: " + token);

        if (token != null && jwtUtil.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String username = jwtUtil.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails instanceof CustomUserDetails customUserDetails) {

                Integer tokenVersionInToken = jwtUtil.getTokenVersion(token);
                Integer tokenVersionInDb = customUserDetails.getTokenVersion();

                if (!tokenVersionInToken.equals(tokenVersionInDb)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token has been invalidated");
                    return;
                }

                // optional: chặn user bị xóa
                if (!customUserDetails.isEnabled()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("User is disabled");
                    return;
                }
            }

            System.out.println(">>> Authorities từ userDetails: " + userDetails.getAuthorities());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            auth.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println(">>> AUTH SET: " + auth);
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

