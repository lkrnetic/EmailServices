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
import org.springframework.util.StringUtils;
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
        model.addAttribute("emails" , emailsList);
        return "emails";
    }

    @GetMapping("/email")
    public String showEmail(Model model, @RequestParam Long id)
    {
        Email email = emailRepository.getById(id);
        model.addAttribute("email" , email);
        return "email";
    }

    @PostMapping("/email")
    public String sendNewEmail(Principal principal, Model model, @RequestParam String receiver, String subject, String text)
    {
        AppUser appUser = appUserRepository.findByEmail(principal.getName());
        Email email = new Email(appUser, text,  subject, receiver);
        int count = StringUtils.countOccurrencesOf(receiver, ";");
        if (count == 0) {
            emailSender.send(
                    receiver,
                    text);
        } else {
            String[] list_of_receivers = receiver.split(";");
            for(int i = 0; i < list_of_receivers.length; i++) {
                emailSender.send(
                        list_of_receivers[i],
                        text);
            }
        }
        emailRepository.save(email);
        List<Email> emailsList = emailRepository.findByAppUser(appUser);
        model.addAttribute("emails" , emailsList);
        return "emails";
    }

    @GetMapping("/newEmail")
    public String newEmail()
    {
        return "newemail";
    }

}
