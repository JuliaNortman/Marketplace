package com.ncgroup.marketplaceserver.service.impl;

import javax.mail.MessagingException;

import com.ncgroup.marketplaceserver.model.dto.EmailParamsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.ncgroup.marketplaceserver.constants.EmailParam;
import com.ncgroup.marketplaceserver.constants.MailConstants;
import com.ncgroup.marketplaceserver.service.EmailSenderService;


@Service
public class EmailSenderServiceImpl implements EmailSenderService {

    @Value("${url.confirm-account}")
    private String confirmAccountUrl;

    @Value("${url.reset-password}")
    private String resetPasswordUrl;

    @Value("${url.create-password}")
    private String createPasswordUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;


    @Override
    public String sendSimpleEmailValidate(String toEmail, String name) throws MessagingException {
        EmailParamsDto params = new EmailParamsDto(
                MailConstants.ACTIVATE_ACCOUNT_SUBJECT,
                MailConstants.REGISTRATION_MESSAGE,
                toEmail,
                name,
                confirmAccountUrl
        );
        return sendEmail(params);
    }

    @Override
    public String sendSimpleEmailPasswordRecovery(String toEmail, String name) throws MessagingException {
        EmailParamsDto params = new EmailParamsDto(
                MailConstants.PASSWORD_RECOVERY_SUBJECT,
                MailConstants.PASSWORD_RECOVERY_MESSAGE,
                toEmail,
                name,
                resetPasswordUrl
        );
        return sendEmail(params);
    }

    @Override
    public String sendSimpleEmailPasswordCreation(String toEmail, String name) throws MessagingException {
        EmailParamsDto params = new EmailParamsDto(
                MailConstants.PASSWORD_CREATION_SUBJECT,
                MailConstants.PASSWORD_CREATION_MESSAGE,
                toEmail,
                name,
                createPasswordUrl
        );
        return sendEmail(params);
    }

    private String sendEmail(EmailParamsDto params) throws MessagingException {
        javax.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
        EmailParam emailParam = new EmailParam();

        message.setFrom(MailConstants.SENDER_EMAIL);
        message.setTo(params.getReceiver());
        message.setSubject(params.getSubject());

        emailParam.setMess(String.format(params.getMessage()));
        emailParam.setLink(String.format(params.getRedirectUrl(), emailParam.getToken()));
        emailParam.setName(params.getName());

        Context context = new Context();
        context.setVariable("EmailParam", emailParam);

        String html = templateEngine.process("EmailValidation", context);
        message.setText(html, true);
        mailSender.send(mimeMessage);
        return emailParam.getToken();
    }


}
