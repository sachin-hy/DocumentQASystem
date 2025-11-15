package com.DocumentQ.ASystem.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageDto {

    Long id;
    String query;
    String message;
    LocalDateTime createdAt;
}
