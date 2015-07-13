package br.com.runplanner.util.mail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("emailThreadProductor")
@Scope("singleton")
public class EmailThreadProductor {

	private final ExecutorService executor = Executors.newCachedThreadPool();

	public void enviarMensagem(final String recipient, final String messageSubject, final String messageBody) {		
		executor.execute(new Runnable() {
			public void run() {
				try {
					EmailUtil.send(messageSubject, messageBody, recipient);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void enviarMensagem(final String recipient, final String messageSubject, final String messageBody, final byte[] anexo) {		
		executor.execute(new Runnable() {
			public void run() {
				try {
					EmailUtil.send(messageSubject, messageBody, recipient, anexo);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		});
	}	


}
