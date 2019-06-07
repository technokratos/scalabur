package org.repocrud.service;

import org.repocrud.domain.Company;
import org.repocrud.domain.SmtpSettings;
import org.repocrud.repository.SmtpSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.repocrud.text.LocalText.text;

/**
 * @author Denis B. Kulikov<br/>
 * date: 07.11.2018:17:13<br/>
 */

@Slf4j
@Service
public class SmtpFactoryService {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<Long, Long> lastSentTimeByCompany = new ConcurrentHashMap<>();

    @Autowired
    private SmtpSettingsRepository smtpSettingsRepository;


    @Value("${smtpWarningTimeoutMin:60}")
    private int smtpWarningTimeoutMin;

    public void setSmtpSettingsRepository(SmtpSettingsRepository smtpSettingsRepository) {
        this.smtpSettingsRepository = smtpSettingsRepository;
    }

    public JavaMailSender getJavaMailSender(SmtpSettings smtpSettings) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpSettings.getSmptServer());
        mailSender.setPort(smtpSettings.getSmptPort());

        mailSender.setUsername(smtpSettings.getSmptUser());
        mailSender.setPassword(smtpSettings.getSmptPassword());

        Properties props = mailSender.getJavaMailProperties();

        //props.put("mail.debug", "true");

        props.put("mail.smtp.host", smtpSettings.getSmptServer());
        props.put("mail.smtp.socketFactory.port", smtpSettings.getSmptPort().toString());
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", smtpSettings.getSmptPort().toString());

        props.put("mail.smtp.timeout", "10000");

        return mailSender;
    }

    public void sendWarningWithLimit(String warningMessage) {
        log.info("Sending warning message {}", warningMessage);

        List<SmtpSettings> settingsList = smtpSettingsRepository.findByCompany(null);
        if (settingsList.size() < 1) {
            log.error("Wrong count of settings for null company " + settingsList.size());
            return;
        }

        SmtpSettings settings = settingsList.iterator().next();
        log.info("Load smpt settings {}", settings);
        Company company = settings.getCompany();

        synchronized (lastSentTimeByCompany) {
            Long companyKey = (company != null) ? company.getId() : -1L;
            Long lastSent = lastSentTimeByCompany.getOrDefault(companyKey, 0L);
            long dif = System.currentTimeMillis() - lastSent;
            if (TimeUnit.MILLISECONDS.toMinutes(dif) > smtpWarningTimeoutMin) {
                executor.execute(() -> sendWarning(warningMessage, settings));
                lastSentTimeByCompany.put(companyKey, System.currentTimeMillis());
            } else {
                log.info("Timeout isn't exceed for send next warning ", dif);
            }
        }
    }



    public boolean sendWarning(String warningMessage, SmtpSettings smtpSettings) {
        try {

            JavaMailSender sender = getJavaMailSender(smtpSettings);
            @NotNull String warningMail = smtpSettings.getWarningMail();
            return Stream.of(warningMail.split(";")).allMatch(mail -> sendSingleWarning(warningMessage, smtpSettings, sender, mail));
        } catch (Exception e) {
            log.error("Error sent warning message " + warningMessage, e);
            return false;
        }

    }

    public boolean sendWarning(String warningMessage, String warningMail) {
        try {

            SmtpSettings smtpSettings = getSmptSettings();
            if (smtpSettings == null) {
                return false;
            }
            JavaMailSender sender = getJavaMailSender(smtpSettings);
            return Stream.of(warningMail.split(";")).allMatch(mail -> sendSingleWarning(warningMessage, smtpSettings, sender, mail));
        } catch (Exception e) {
            log.error("Error sent warning message " + warningMessage, e);
            return false;
        }

    }

    private boolean sendSingleWarning(String warningMessage, SmtpSettings smtpSettings, JavaMailSender sender, @NotNull String warningMail) {
        @NotNull String smptUser = smtpSettings.getSmptUser();
        return sendSingleWarning(warningMessage, sender, warningMail, smptUser);
    }

    private boolean sendSingleWarning(String warningMessage, JavaMailSender sender, @NotNull String warningMail, @NotNull String smptUser) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("warningEmail");
            message.setText(warningMessage);
            message.setTo(warningMail);
            message.setFrom(smptUser);
            sender.send(message);
            log.info("Sent mail {}, {}", smptUser, warningMail);
            return true;
        } catch (Exception e) {
            log.error("Error sent warning message " + warningMessage, e);
            return false;
        }
    }

    public SmtpSettings getSmptSettings() {
        List<SmtpSettings> settingsList = smtpSettingsRepository.findByCompany(null);
        if (settingsList.size() < 1) {
            log.error("Wrong count of settings for null company " + settingsList.size());
            return null;
        }

        return settingsList.iterator().next();
    }

    public void sendMailWithAttachment(String email, String subject, InputStreamSource inputStreamSource, String filename) {


        SmtpSettings smtpSettings = getSmptSettings();
        if (smtpSettings == null) {
            return;
        }
        JavaMailSender mailSender =  getJavaMailSender(smtpSettings);
        MimeMessage message = mailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(smtpSettings.getSmptUser());
            helper.setTo(email);
            helper.setSubject(subject );
            helper.setText("See attachment!");

            helper.addAttachment(filename, inputStreamSource);

        }catch (MessagingException e) {
            throw new MailParseException(e);
        }
        mailSender.send(message);
    }
}

