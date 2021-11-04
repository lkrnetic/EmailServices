package com.example.MailingServiceLoginSystem.Controllers;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import com.example.MailingServiceLoginSystem.appuser.AppUserRepository;
import com.example.MailingServiceLoginSystem.appuser.AppUserRole;
import com.example.MailingServiceLoginSystem.appuser.AppUserService;
import com.example.MailingServiceLoginSystem.email.EmailSender;
import com.example.MailingServiceLoginSystem.email.EmailValidator;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Controller
public class AppUserController {
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailSender emailSender;

    @GetMapping("/signup")
    public String getSignupPage(){
        return "signup" ;
    }

    @PostMapping("/signup")
    public RedirectView createNewAppUser(@RequestParam String firstName , @RequestParam String lastName,
                                         @RequestParam String email , @RequestParam String password, Model model){
        boolean isValidEmail = emailValidator.test(email);
        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }
        AppUser appUser = appUserRepository.findByEmail(email);
        String accountActivationToken = UUID.randomUUID().toString();
        if (appUser != null) {
            throw new IllegalStateException("email already taken");
        } else {
            appUser =
                    new AppUser(
                            firstName,
                            lastName,
                            email,
                            password,
                            AppUserRole.USER,
                            accountActivationToken
                    );
        }
        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);
        appUserRepository.save(appUser);
        String link = "http://localhost:8080/accountActivation?token=" + accountActivationToken;
        emailSender.send(
                email,
                buildEmail(firstName, link), "Activation link","EmailServices.com");
        //set the login token session
        Authentication authentication = new UsernamePasswordAuthenticationToken(appUser, null , appUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        model.addAttribute("successfulAccountCreation", "Account was successfully created. In order to login, you must activate account.");
        return new RedirectView("/login?message=accountWasCreated");
    }

    @GetMapping("/forgotpassword")
    public String getForgotPassword(){
        return "forgotpassword" ;
    }

    @PostMapping("/forgotpassword")
    public RedirectView createNewAppUser(@RequestParam String email){
        AppUser appUser = appUserRepository.findByEmail(email);
        String forgetPasswordToken = UUID.randomUUID().toString();
        String link = "http://localhost:8080/confirmchangepasswordtoken?forgetPasswordToken=" + forgetPasswordToken;
        emailSender.send(
                email,
                "In order to change your password you must visit this link http://localhost:8080/confirmchangepasswordtoken?forgetPasswordToken=" + forgetPasswordToken,
                "Forget password", "EmailServices.com");
        appUserRepository.updateForgetPasswordToken(email, forgetPasswordToken);
        return new RedirectView("/login?message=emailForForgetPasswordWasSent");
    }

    @GetMapping("/confirmchangepasswordtoken")
    public RedirectView confirmChangePasswordToken(@RequestParam String forgetPasswordToken) {
        AppUser appUser = appUserRepository.findByForgetPasswordToken(forgetPasswordToken);
        Authentication authentication = new UsernamePasswordAuthenticationToken(appUser, null , appUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new RedirectView("/changepassword");
    }

    @GetMapping("/changepassword")
    public String getChangePassword(){
        return "changepassword" ;
    }

    @PostMapping("/changepassword")
    public RedirectView postChangepassword(@RequestParam String password, Principal principal) {
        AppUser appUser = appUserRepository.findByEmail(principal.getName());
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        appUser.setPassword(encodedPassword);
        appUserRepository.updatePassword(appUser.getEmail(), appUser.getPassword());
        return new RedirectView("/emails");
    }

    @GetMapping("/login")
    public String getLoginPage(@RequestParam("message") Optional<String> message, Model model){
        if(!message.isEmpty()) {
            if (message.equals(Optional.of("accountWasCreated"))) {
                model.addAttribute("successfulAccountCreation", "Account was successfully created. In order to login, account must be activated. Email with activation link was sent to your email.");
            }
            if (message.equals(Optional.of("emailForForgetPasswordWasSent"))) {
                model.addAttribute("emailForForgetPasswordWasSent", "In order to change password, please visit link that was sent to your email.");
            }
        }
        return "login" ;
    }


    @GetMapping("/logout")
    public String getLogoutPage(){
        return "logout" ;
    }



    @GetMapping("/login-error")
    public String login(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }
        model.addAttribute("errorMessage", errorMessage);
        return "login";
    }


    @GetMapping("/accountActivation")
    public RedirectView  confirm(@RequestParam("token") String token) {
        AppUser appUser = appUserRepository.findByAccountActivationToken(token);
        if (appUser == null) {
            throw new IllegalStateException("Requested token doesn't exist");
        }
        appUserRepository.enableAppUser(appUser.getEmail());
        return new RedirectView("/emails");
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
