package com.example.MailingServiceLoginSystem.email;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.lang.model.util.ElementScanner7;
import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findByAppUser(AppUser appUser);
}

