package com.example.MailingServiceLoginSystem.email;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import com.example.MailingServiceLoginSystem.appuser.AppUserRepository;
import com.example.MailingServiceLoginSystem.appuser.AppUserRole;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
@Configuration
public class EmaiConfig {
    private AppUserRole appUserRole;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Bean
    CommandLineRunner commandLineRunner(AppUserRepository appUserRepository, EmailRepository emailRepository) {
        return args -> {
            AppUser john = new AppUser( "John", "Johnny" ,"john.johnny@gmail.com", bCryptPasswordEncoder.encode("abc"), AppUserRole.USER, true);
            appUserRepository.saveAll(
                    List.of(john)
            );
            Email email1 = new Email(john, "blabla", "subject");
            Email email2 = new Email(john, "blabla2", "subject2");
            Email email3 = new Email(john, "blabla3", "subject3");
            emailRepository.saveAll(List.of(email1, email2, email3));
        };
    }
}
