package com.DocumentQ.ASystem.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class RegisterRequestDto {


    private String password;
    private String email;
    private String name;

}