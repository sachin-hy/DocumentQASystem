package com.DocumentQ.ASystem.repository;

import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.DocumentQ.ASystem.dto.DocumentResponseDto;

@Repository
public interface DocumentDetailsRepository extends JpaRepository<DocumentDetails,Long> {
    List<DocumentDetails> findByUserDetails(Users user);
}
