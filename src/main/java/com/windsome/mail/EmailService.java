package com.windsome.mail;

import javax.mail.MessagingException;

public interface EmailService {

    void sendEmail(EmailMessage emailMessage) throws MessagingException;
}
