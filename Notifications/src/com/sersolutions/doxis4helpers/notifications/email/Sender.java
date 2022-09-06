package com.sersolutions.doxis4helpers.notifications.email;

import com.ser.blueline.ISession;
import com.sersolutions.doxis4helpers.commons.types.ContentFile;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.*;

/**
 * Class for sending emails
 */
public class Sender {

    /**
     * Send email
     * @param to list of addresses where need to send email
     * @param subject subject of email
     * @param message HTML-based message for email
     * @param attachments list of attachments for the email
     * @throws Exception if couldn't send email
     */
    public static void sendMail(List<String> to, String subject, String message, List<ContentFile> attachments) throws Exception{
        sendMail(null, null, to,  subject, message, attachments);
    }

    /**
     * Send email
     * @param doxis4Session Doxis4 Session Object
     *                      @see com.ser.blueline.ISession
     * @param smtpConfigGVL Name of global value list with information of connection to SMTP+IMAP
     *                      @see com.sersolutions.doxis4helpers.notifications.email.Connector
     * @param to list of addresses where need to send email
     * @param subject subject of email
     * @param message HTML-based message for email
     * @param attachments list of attachments for the email
     * @throws Exception if couldn't send email
     */
    public static void sendMail(ISession doxis4Session, String smtpConfigGVL, List<String> to, String subject, String message, List<ContentFile> attachments) throws Exception{
        Connector mailConnector = null;

        if (doxis4Session != null && smtpConfigGVL != null && !"".equals(smtpConfigGVL)) {
            mailConnector = Connector.init(doxis4Session, smtpConfigGVL);
        }

        Properties props = new Properties();
        if (mailConnector == null) {
            throw new Exception("Can't send email message without SMTP Configuration!");
        }

        props.put("mail.transport.protocol", mailConnector.getTransportProtocol());
        props.put("mail.smtp.port", mailConnector.getPort());

        // Set properties indicating that we want to use STARTTLS to encrypt the
        // connection.
        // The SMTP session will begin on an unencrypted connection, and then
        // the client
        // will issue a STARTTLS command to upgrade to an encrypted connection.
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", mailConnector.getStartTlsEnable());
        props.put("mail.smtp.starttls.required", mailConnector.getStartTlsRequired());
        props.put("mail.smtp.connectiontimeout", mailConnector.getTimeOut());
        props.put("mail.smtp.timeout", mailConnector.getTimeOut());

        props.put("mail.smtp.host", mailConnector.getHost());
        props.put("mail.from", mailConnector.getFromAddress());

        Session mailSession = Session.getInstance(props, null);

        String errorMessage = "";

            //set and validate from address
            MimeMessage msg = new MimeMessage(mailSession);
            InternetAddress addressFrom = new InternetAddress(mailConnector.getFromAddress());
            addressFrom.validate();
            msg.setFrom(addressFrom);

            //validate and convert recipient addresses
            List<Address> addresses = new ArrayList<Address>();
            for (String address : to) {
                Address inetAddress = new InternetAddress(address);
                // inetAddress.validate();
                addresses.add(inetAddress);
            }

            Address[] sendTo = addresses.toArray(new Address[addresses.size()]);

            msg.setRecipients(Message.RecipientType.TO, sendTo);
            msg.setSubject(subject, "UTF-8");
            msg.setSentDate(new Date());
            // If the desired charset is known, you can use
            // setText(text, charset)
            // msg.setText(message, "UTF-8");
            // msg.setContent(message, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();

            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setContent(message, "text/html; charset=utf-8");
            multipart.addBodyPart(textBodyPart);  // add the text part

            if (attachments != null) {
                for (ContentFile file: attachments) {
                    byte[] bytearray = file.GetContent();
                    if (bytearray != null) {
                            MimeBodyPart attachmentBodyPart= new MimeBodyPart();
                            ByteArrayDataSource bds = new ByteArrayDataSource(bytearray, file.GetMimeType());
                            attachmentBodyPart.setDataHandler(new DataHandler(bds));
                            attachmentBodyPart.setFileName(file.GetFileName());

                            multipart.addBodyPart(attachmentBodyPart);

                    }
                }
            }

            msg.setContent(multipart);

            //send mail

            Transport transport = mailSession.getTransport();
            if (mailConnector.getPort() == null || "".equals(mailConnector.getPort())){
                transport.connect(mailConnector.getHost(), mailConnector.getLogin(), mailConnector.getPass());
            } else {
                transport.connect(mailConnector.getHost(),  mailConnector.getPortInt(), mailConnector.getLogin(), mailConnector.getPass());

            }

            transport.sendMessage(msg, msg.getAllRecipients());

        if (mailConnector.getImaphost() != null && mailConnector.getSentFolderName() != null) {

            Store store = mailSession.getStore(mailConnector.getImapProtocol());
            store.connect(mailConnector.getImaphost(), mailConnector.getImapPortInt(), mailConnector.getLogin(), mailConnector.getPass());
            Folder sent = store.getFolder(mailConnector.getSentFolderName());
            sent.open(Folder.READ_WRITE);
            sent.appendMessages(new Message[] {msg});
            sent.close();
        }
    }
}
