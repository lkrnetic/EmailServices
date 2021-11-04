package com.example.MailingServiceLoginSystem.Controllers;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import com.example.MailingServiceLoginSystem.appuser.AppUserRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Controller
public class EmailController {
    private final AppUserRepository appUserRepository;
    private final EmailRepository emailRepository;
    private final EmailSender emailSender;

    @GetMapping("/emails")
    public String getEmails(@RequestParam("message") Optional<String> message, Model model, Principal principal) {
        AppUser appUser = appUserRepository.findByEmail(principal.getName());
        List<Email> emailsList = emailRepository.findByAppUser(appUser);
        Collections.reverse(emailsList);
        model.addAttribute("emails" , emailsList);
        if(!message.isEmpty()) {
            if (message.equals(Optional.of("emailWasSent"))) {
                model.addAttribute("emailWasSent", "Email was successfully sent.");
            }
        }
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
    public RedirectView sendNewEmail(Principal principal, @RequestParam String receiver, String subject, String text) {
        AppUser appUser = appUserRepository.findByEmail(principal.getName());
        Email email = new Email(appUser, text, subject, receiver);
        int count = StringUtils.countOccurrencesOf(receiver, ";");
        if (count == 0) {
            emailSender.send(receiver, text, subject, appUser.getEmail());
        } else {
            String[] list_of_receivers = receiver.split(";");
            for (int i = 0; i < list_of_receivers.length; i++) {
                emailSender.send(
                        list_of_receivers[i], text, subject, appUser.getEmail());
            }
        }
        emailRepository.save(email);
        return new RedirectView("/emails?message=emailWasSent");
    }

    @GetMapping("/newemail")
    public String newEmail()
    {
        return "newemail";
    }

    @PostMapping("/deleteEmail")
    public RedirectView deleteEmail(@RequestParam Long id)
    {
        emailRepository.deleteById(id);
        return new RedirectView("/emails");
    }

}
