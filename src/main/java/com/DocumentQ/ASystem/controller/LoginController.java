package com.DocumentQ.ASystem.controller;

import com.DocumentQ.ASystem.dto.LoginRequestDto;
import com.DocumentQ.ASystem.service.SecurityCustomDetailService;
import com.DocumentQ.ASystem.service.UsersService;
import com.DocumentQ.ASystem.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class LoginController {

    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private SecurityCustomDetailService securityCustomService;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsersService usersService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response)
    {

        String email	 = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        try {


            Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));

            String jwt = "";


            UserDetails userDetails = securityCustomService.loadUserByUsername(email);

            jwt = jwtUtil.generateToken(userDetails.getUsername());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);


            Map<String,Object> map = new HashMap<>();
            map.put("token",jwt);

            return new ResponseEntity<>(map, HttpStatus.OK);

        }catch(Exception e)
        {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

    }


}
