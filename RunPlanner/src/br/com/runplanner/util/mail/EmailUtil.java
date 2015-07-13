package br.com.runplanner.util.mail;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtil {
    
    private static final String CORPO_EMAIL = "<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/></head><body><center><div style=\"width: 621px; border: 1px solid #dedede; background-color: #EDEDED;\"><div style=\"background-image: url('http://runplanner.com.br/runplanner/images/rpTopoEmail.png') !important; width: 621px; height: 30px;\"></div><div style='text-align: left; width: 100%; min-height: 300px; font-family: tahoma,verdana,arial,sans-serif; font-size: 12px; padding: 10px;'><br/><br/>$1</div></div><div style='width: 621px;'><font size='1' face='tahoma,verdana,arial,sans-serif'>D&uacute;vidas? Acesse <a href='http://www.runplanner.com.br'>http://www.runplanner.com.br/</a><br/>Este &eacute; um e-mail automaticamente disparado pelo sistema. Favor n&atilde;o respond&ecirc;-lo, pois esta conta n&atilde;o &eacute; monitorada.</font></div></center></body></html>";
	private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=UTF-8";
	private static final String MESSAGE_STARTTLS  = "true";
    private static final String MESSAGE_AUTH      = "true";
    private static final String MESSAGE_PROTOCOL  = "smtp";
    private static final String MESSAGE_TEXT_TYPE = "html";
    private static final String MESSAGE_ENCONDING = "UTF-8";
    
    private static final String SMTP_HOST_NAME = "smtp.sendgrid.net";
    private static final int    SMTP_HOST_PORT = 587;  
    private static final String SMTP_AUTH_USER = "runplanner";
    private static final String SMTP_AUTH_PWD  = "m8iq1q2w";
    
    private static final String FROM = "nao-responda@runplanner.com.br";
     

    protected static void send(String messageSubject, String messageBody, String recipient) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.transport.protocol", MESSAGE_PROTOCOL);
        //props.put("mail.smtp.starttls.enable",MESSAGE_STARTTLS);
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", MESSAGE_AUTH);      

        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(false);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject(messageSubject);
        message.setHeader("Content-Type", TEXT_HTML_CHARSET_UTF_8);
        
        String msg = CORPO_EMAIL;
        msg = msg.replace("$1", messageBody);
        
        message.setText(msg, MESSAGE_ENCONDING, MESSAGE_TEXT_TYPE);
        message.setFrom(new InternetAddress(FROM));
        
        String[] recipients = recipient.split(",");
        if ( recipients==null || recipients.length==0 ) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
        }
        else {
            for(String r:recipients) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(r));
            }
        }

        transport.connect(SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);

        transport.sendMessage(message,message.getRecipients(Message.RecipientType.BCC));
        transport.close();
        
    }
    
    protected static void send(String messageSubject, String messageBody, String recipient, byte[] anexo) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.transport.protocol", MESSAGE_PROTOCOL);
        props.put("mail.smtp.starttls.enable",MESSAGE_STARTTLS);
        props.put("mail.smtps.host", SMTP_HOST_NAME);
        props.put("mail.smtps.auth", MESSAGE_AUTH);      

        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(false);
        Transport transport = mailSession.getTransport();

        // cria a mensagem
        MimeMessage message = new MimeMessage(mailSession);
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject(messageSubject);
        message.setFrom(new InternetAddress(FROM));
        message.setHeader("Content-Type", TEXT_HTML_CHARSET_UTF_8);

        // cria a primeira parte da mensagem
        String msg = CORPO_EMAIL;
        msg = msg.replace("$1", messageBody);
        
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(msg, MESSAGE_ENCONDING, MESSAGE_TEXT_TYPE);

        // cria a segunda parte da mensage
        MimeBodyPart mbp2 = new MimeBodyPart();

        // anexa o arquivo na mensagem        
        mbp2.setDataHandler(new DataHandler(anexo, "application/pdf"));
        mbp2.setFileName("planilha.pdf");

        // cria a Multipart
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);

        // adiciona a Multipart na mensagem
        message.setContent(mp);

        transport.connect(SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);
        transport.sendMessage(message,message.getRecipients(Message.RecipientType.TO));
        transport.close();
        
    }
    
    
    public static void main(String[] a) {
        try {
            EmailUtil.send("Teste", "RunPlanner", "pedgoncalves@gmail.com");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        
    }

}