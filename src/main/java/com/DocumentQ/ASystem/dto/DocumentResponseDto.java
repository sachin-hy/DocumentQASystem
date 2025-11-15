package com.DocumentQ.ASystem.dto;


import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDto {
    Long id;
    String fileName;

    Long fileSize;
    LocalDateTime createdAt;

}
