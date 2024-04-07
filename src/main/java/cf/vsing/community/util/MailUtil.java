package cf.vsing.community.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {
    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
    private static JavaMailSender sender;
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    MailUtil(JavaMailSender mailSender ){
        MailUtil.sender = mailSender;
    }

    public boolean send(String to, String subject, String text) {
        MimeMessage mail = sender.createMimeMessage();
        try {
            mail.setFrom(from);
            mail.setRecipients(Message.RecipientType.TO, to);
            mail.setSubject(subject);
            mail.setContent(text, "text/html;charset=UTF-8");
            sender.send(mail);
            return true;
        } catch (MessagingException e) {
            logger.error("【邮件发送出错】\n" + e.getMessage());
            return false;
        }
    }
}
