package com.DocumentQ.ASystem.controller;

import com.DocumentQ.ASystem.dto.RegisterRequestDto;
import com.DocumentQ.ASystem.entity.Users;
import com.DocumentQ.ASystem.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SignUpController {

    @Autowired
    private UsersService usersService;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequestDto registerRequest)
    {

        try {

            Users user = usersService.saveUser(registerRequest);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }catch(Exception ex)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
