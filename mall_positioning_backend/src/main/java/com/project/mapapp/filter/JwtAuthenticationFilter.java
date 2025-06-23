package com.project.mapapp.filter;

import com.project.mapapp.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String path = request.getServletPath();
        // 1. 放行公开接口
        if (path.startsWith("/app/auth/") || path.startsWith("/public/") || path.startsWith("/msm/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 获取并验证Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "缺少有效Token");
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "无效Token");
                return;
            }

            // 3. 🔥 关键修复：设置认证信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 默认角色
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 调试日志
            System.out.println("JWT验证通过，用户ID: " + userId);
            System.out.println("SecurityContext内容: " +
                    SecurityContextHolder.getContext().getAuthentication());

            chain.doFilter(request, response);

        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token验证失败");
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"status\":%d, \"message\":\"%s\"}", status, message));
    }
}