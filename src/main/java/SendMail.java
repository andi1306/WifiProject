import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

    public static void main(String[] args) throws SQLException, URISyntaxException, IOException {

        List<Map<String, Object>> mapList = loadData();

        // Recipient's email ID needs to be mentioned.
        String to = "andreasfreidl1306@gmail.com";

        // Sender's email ID needs to be mentioned
        String from = "karlgruberjava@gmail.com";

        // Assuming you are sending email from through gmails smtp
        String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication("karlgruberjava@gmail.com", "Pa55w.rd");

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Support Ticket");

            // Now set the actual message
            message.setText(mapList.toString());

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }

    public static SupportEmailConfig readProperties() throws URISyntaxException, IOException {
        URL resource = SendMail.class.getClassLoader()
                .getResource("properties.json");
        byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
        SupportEmailConfig supportEmailConfig = new Gson().fromJson(new String(bytes), SupportEmailConfig.class);
        return supportEmailConfig;
    }

    public static List<Map<String, Object>> loadData() throws SQLException, URISyntaxException, IOException {

        SupportEmailConfig supportEmailConfigResult = readProperties();
        try (Connection con = DriverManager.getConnection(supportEmailConfigResult.getDbUrl(), supportEmailConfigResult.getDbUser(), supportEmailConfigResult.getDbPassword());
             PreparedStatement pst = con.prepareStatement(supportEmailConfigResult.getQuery())) {
            ResultSet resultSet = pst.executeQuery();
            System.out.println(resultSet);
            return resultSetToList(resultSet);
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> resMap = new HashMap<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                resMap.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
            }
            result.add(resMap);
        }

        return result;
    }

}