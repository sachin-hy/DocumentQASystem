package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.entity.Users;
import com.DocumentQ.ASystem.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityCustomDetailService implements UserDetailsService {


    @Autowired
    private UsersRepository recruiterRepo;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

       Users user = recruiterRepo.findByEmail(email);

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();
    }

}