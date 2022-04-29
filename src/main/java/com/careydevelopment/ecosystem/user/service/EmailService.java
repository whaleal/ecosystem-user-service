package com.careydevelopment.ecosystem.user.service;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 *email业务层
 */
@Service
public class EmailService {

    //日志
    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private static final String NOREPLY_ADDRESS = "noreply@careydevelopment.us";

    @Autowired
    private JavaMailSender emailSender;

    // @Value("classpath:/mail-logo.png")
    // private Resource resourceFile;

    /**
     * 发送消息
     * @param to 收件地址
     * @param subject 邮件主题
     * @param text 邮件内容
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            //创建邮件
            SimpleMailMessage message = new SimpleMailMessage();
            //设置发件人地址
            message.setFrom(NOREPLY_ADDRESS);
            //设置收件人地址
            message.setTo(to);
            //设置邮件主题
            message.setSubject(subject);
            //设置邮件内容
            message.setText(text);

            //日志输出
            LOG.debug("Sending email " + message);

            //发送
            emailSender.send(message);
        } catch (MailException exception) {
            //出现异常日志输出
            LOG.error("Problem sending email", exception);
        }
    }

    /**
     * 发送消息
     * @param from 发送地址
     * @param to 收件地址
     * @param subject 邮件主题
     * @param text 邮件内容
     */
    public void sendSimpleMessage(String from, String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            LOG.debug("Sending email " + message);

            emailSender.send(message);
        } catch (MailException exception) {
            LOG.error("Problem sending email", exception);
        }
    }

//    public void sendSimpleMessageUsingTemplate(String to,
//                                               String subject,
//                                               String ...templateModel) {
//        String text = String.format(template.getText(), templateModel);  
//        sendSimpleMessage(to, subject, text);
//    }

    /**
     * 发送带有附件的邮件
     * @param to 收件地址
     * @param subject 邮件主题
     * @param text 邮件内容
     * @param pathToAttachment 连接路径
     */
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            // pass 'true' to the constructor to create a multipart message 将“true”传递给构造函数以创建多部分消息
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            //设置内容
            helper.setFrom(NOREPLY_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment("Invoice", file);

            emailSender.send(message);
        } catch (MessagingException e) {
            LOG.error("Problem sending email", e);
        }
    }

//    public void sendMessageUsingThymeleafTemplate(
//        String to, String subject, Map<String, Object> templateModel)
//            throws MessagingException {
//                
//        Context thymeleafContext = new Context();
//        thymeleafContext.setVariables(templateModel);
//        
//        String htmlBody = thymeleafTemplateEngine.process("template-thymeleaf.html", thymeleafContext);
//
//        sendHtmlMessage(to, subject, htmlBody);
//    }

    /**
     * 发送浏览器消息
     * @param to 发送地址
     * @param subject 主题
     * @param htmlBody 浏览器页面
     * @throws MessagingException
     */
    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        //设置内容
        helper.setFrom(NOREPLY_ADDRESS);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        // helper.addInline("attachment.png", resourceFile);
        emailSender.send(message);
    }
}
