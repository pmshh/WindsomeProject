package com.windsome.service.mail;

import javax.mail.MessagingException;

public interface EmailService {

    void sendEmail(EmailMessageDto emailMessageDto) throws MessagingException;
}
