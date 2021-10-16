package com.example.MailingServiceLoginSystem.Controllers;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import com.example.MailingServiceLoginSystem.appuser.AppUserRepository;
import com.example.MailingServiceLoginSystem.appuser.AppUserService;
import com.example.MailingServiceLoginSystem.email.Email;
import com.example.MailingServiceLoginSystem.email.EmailRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@Controller
public class EmailController {
    private final AppUserRepository appUserRepository;
    private final EmailRepository emailRepository;

    @GetMapping("/emails")
    public String getEmails(Model model, Principal principal) {
        AppUser appUser = appUserRepository.findByEmail(principal.getName());
        List<Email> emailsList = emailRepository.findByAppUser(appUser);
        for(int i = 0; i < emailsList.size(); i++) {
            System.out.println(emailsList.get(i).getText());
        }
        return "emails";
    }
}
