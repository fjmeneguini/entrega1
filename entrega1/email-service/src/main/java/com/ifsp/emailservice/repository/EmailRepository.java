package com.ifsp.emailservice.repository;

import com.ifsp.emailservice.model.EmailModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<EmailModel, Long> {
}
