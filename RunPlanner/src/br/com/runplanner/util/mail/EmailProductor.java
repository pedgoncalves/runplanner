package br.com.runplanner.util.mail;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class EmailProductor {
	
	private JmsTemplate jmsTemplate;
	
	private Destination destination;	
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public void enviarMensagem(final String recipient, final String messageSubject, final String messageBody) {		

		MessageCreator messageCreator =	new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				MapMessage message = session.createMapMessage();
				
				message.setString("recipient", recipient);
				message.setString("messageSubject", messageSubject);
				message.setString("messageBody", messageBody);
				message.setBoolean("hasAnexo", false);
				
				return message;
			}
		};

		jmsTemplate.send(destination, messageCreator);
	}
	
	public void enviarMensagem(final String recipient, final String messageSubject, final String messageBody, final byte[] anexo) {		

		MessageCreator messageCreator =	new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				MapMessage message = session.createMapMessage();
				
				message.setString("recipient", recipient);
				message.setString("messageSubject", messageSubject);
				message.setString("messageBody", messageBody);
				message.setBoolean("hasAnexo", true);
				message.setBytes("anexo", anexo);
				
				return message;
			}
		};

		jmsTemplate.send(destination, messageCreator);
	}	
	
}
