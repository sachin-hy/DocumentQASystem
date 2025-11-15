package com.DocumentQ.ASystem.dto;


import com.DocumentQ.ASystem.entity.ChatMessage;
import lombok.*;

import java.util.List;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentChatResponseDto {
    Long id;
    String fileName;
    List<ChatMessageDto> message;
}
