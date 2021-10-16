package com.example.MailingServiceLoginSystem.Controllers;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import com.example.MailingServiceLoginSystem.appuser.AppUserRepository;
import com.example.MailingServiceLoginSystem.appuser.AppUserService;
import com.example.MailingServiceLoginSystem.email.Email;
import com.example.MailingServiceLoginSystem.email.EmailRepository;
import com.example.MailingServiceLoginSystem.email.EmailSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@Controller
public class EmailController {
    private final AppUserRepository appUserRepository;
    private final EmailRepository emailRepository;
    private final EmailSender emailSender;

    @GetMapping("/emails")
    public String getEmails(Model model, Principal principal) {
        AppUser appUser = appUserRepository.findByEmail(principal.getName());
        List<Email> emailsList = emailRepository.findByAppUser(appUser);
        for(int i = 0; i < emailsList.size(); i++) {
            System.out.println(emailsList.get(i).getText());
        }
        return "emails";
    }

    @GetMapping("/email")
    public String showEmail(@RequestParam Long id)
    {
        Email email = emailRepository.getById(id);
        return "email";
    }

    @PostMapping("/email")
    public String sendNewEmail(Principal principal, @RequestParam String receiver, String subject, String text)
    {
        AppUser appUser = appUserRepository.findByEmail(principal.getName());
        emailSender.send(
                receiver,
                text);
        Email email = new Email(appUser, text,  subject, receiver);
        emailRepository.save(email);
        return "emails";
    }

    @GetMapping("/newEmail")
    public String newEmail()
    {
        return "newemail";
    }

}