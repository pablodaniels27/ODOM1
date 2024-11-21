package controllers;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;


public class MailController {

    public static class EmailSender {
        public static void sendEmail(String to, String subject, String body) {
            // Configuración del servidor SMTP de Gmail
            final String username = "utigi78@gmail.com"; // Cambia esto por tu correo de Gmail
            final String password = "cwlt khpk qhhm ytgo"; // Cambia esto por tu contraseña de aplicación

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                System.out.println("Correo enviado exitosamente.");

            } catch (MessagingException e) {
                throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
            }
        }

        static void enviarCorreoConContrasena(String correo, String mensaje) {
            String asunto = "Recuperación de contraseña";
            try {
                MailController.EmailSender.sendEmail(correo, asunto, mensaje);
                System.out.println("Correo enviado exitosamente a " + correo);
            } catch (RuntimeException e) {
                System.err.println("Error al enviar el correo: " + e.getMessage());
            }
        }

    }
}
