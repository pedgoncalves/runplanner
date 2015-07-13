package br.com.runplanner.util.mail;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.mail.MessagingException;

public class EmailListener implements MessageListener {

	public void onMessage(Message message) {
		
		MapMessage mapMessage = (MapMessage) message;		
		try {

			String recipient = mapMessage.getString("recipient");
			String messageSubject = mapMessage.getString("messageSubject");
			String messageBody = mapMessage.getString("messageBody");
			boolean hasAnexo = mapMessage.getBoolean("hasAnexo");
			

			try {
				
				if ( !hasAnexo ) {
					EmailUtil.send(messageSubject, messageBody, recipient);
				}
				else {
					byte[] anexo = mapMessage.getBytes("anexo");
					EmailUtil.send(messageSubject, messageBody, recipient, anexo);
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
