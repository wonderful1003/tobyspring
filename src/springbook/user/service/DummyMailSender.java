package user.service;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class DummyMailSender implements MailSender{
	private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {  // Ensure this setter exists and is public
        this.host = host;
    }
	public void send(SimpleMailMessage mailMessage) throws MailException {
	}
	
	public void send(SimpleMailMessage[] mailMessage) throws MailException {
	}
}
