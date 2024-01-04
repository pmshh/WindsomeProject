package com.windsome.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("test")
@Component
public class ConsoleEmailService implements EmailService{

    @Override
    public void sendEmail(EmailMessageDto emailMessageDto) {
        log.info("sent email: {}", emailMessageDto.getMessage());
    }
}
