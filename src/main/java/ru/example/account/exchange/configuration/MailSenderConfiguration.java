package ru.example.account.exchange.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class MailSenderConfiguration {

    @Value("${EMAIL_PORT}")
    private String port;

    @Value("${EMAIL_USERNAME}")
    private String username;

    @Value("${EMAIL_PASSWORD}")
    private String password;

    @Value("${EMAIL_HOST}")
    private String host;

    @Bean
    public JavaMailSender getJavaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(465);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.debug", "true");
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.user", username);
        props.put("mail.password", password);
        props.put("mail.smtps.ssl.checkserveridentity", true);
        props.put("mail.smtps.ssl.trust", "*");

        return mailSender;
    }
}
