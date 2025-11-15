package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.dto.RegisterRequestDto;
import com.DocumentQ.ASystem.entity.Users;
import com.DocumentQ.ASystem.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class UsersService {


        @Autowired
        private UsersRepository usersRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;


        @Transactional
        public Users saveUser(RegisterRequestDto registerRequest) {

            Users user1 = usersRepository.findByEmail(registerRequest.getEmail());

            if(user1 == null)
            {
                Users user = new Users();
                user.setName(registerRequest.getName());
                user.setEmail(registerRequest.getEmail());
                user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

                return usersRepository.save(user);
            }else
            {
                throw new UsernameNotFoundException("User with user name already present");
            }
        }


    public Users findByEmail(String email) {

            Users user = usersRepository.findByEmail(email);

            return user;
    }
}
