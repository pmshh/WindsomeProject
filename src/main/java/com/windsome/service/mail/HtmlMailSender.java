package com.windsome.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Profile({"dev", "prd"})
@Component
@RequiredArgsConstructor
public class HtmlMailSender implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailMessageDto emailMessageDto) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "utf-8");
        mimeMessageHelper.setTo(emailMessageDto.getTo());
        mimeMessageHelper.setSubject(emailMessageDto.getSubject());
        mimeMessageHelper.setText(emailMessageDto.getMessage(), true);
        javaMailSender.send(mimeMessage);
        log.info("sent email: {}", emailMessageDto.getMessage());
    }
}
