package com.example.MailingServiceLoginSystem.email;

import com.example.MailingServiceLoginSystem.appuser.AppUser;
import com.example.MailingServiceLoginSystem.email.EmailReceiver;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(
            nullable = false,
            name = "app_user_id"
    )
    private AppUser appUser;
    @Column(
            name = "text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String text;
    private String subject;
    private String receiver;

    public Email(AppUser appUser, String text, String subject, String receiver) {
        this.appUser = appUser;
        this.text = text;
        this.subject = subject;
        this.receiver = receiver;
    }
}
