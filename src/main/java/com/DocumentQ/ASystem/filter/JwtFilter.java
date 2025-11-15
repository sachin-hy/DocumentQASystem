package com.DocumentQ.ASystem.filter;


import com.DocumentQ.ASystem.service.SecurityCustomDetailService;
import com.DocumentQ.ASystem.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import java.io.IOException;
import java.util.Set;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private SecurityCustomDetailService securityCustomDetailService;
    @Autowired
    private JwtUtil jwtUtil;



    private Set<String> paths = Set.of("/login","/signup");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();

        log.info("path : {}",path);
        AntPathMatcher matcher = new AntPathMatcher();

        for(String s:paths)
        {
            if(matcher.match(s,path))
            {

                filterChain.doFilter(request, response);
                return;
            }

        }

        try {

            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if(authHeader != null && authHeader.startsWith("Bearer"))
            {
                token = authHeader.substring(7);
                username=jwtUtil.extractUsername(token);
            }else
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing or invalid Authorization header");
                return;
            }

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null)
            {
                UserDetails userDetails = securityCustomDetailService.loadUserByUsername(username);

                if(jwtUtil.validateToken(token, userDetails))
                {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }else {

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid token");
                    return;
                }


            }

            log.info("valide token of user {} ", username);

            filterChain.doFilter(request, response);

        }catch(ExpiredJwtException e)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token has expired\"}");
        }
        catch(SignatureException | MalformedJwtException e)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token\"}");
        }catch(Exception e)
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"An unexpected error occurred\"}");
        }




    }


}

