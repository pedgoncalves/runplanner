package br.com.runplanner.domain;

import java.util.Date;
import java.util.List;

public class MailboxGroup {
	private List<Mailbox> mailboxMessages;
	private Pessoa sender;
	private Date lastMessageDate;
	private Date lastMessageHour;
	private InternalMessage lastMessage;
	private boolean newMessage = false;

	public MailboxGroup(List<Mailbox> mailboxMessages, Pessoa sender) {
		this.mailboxMessages = mailboxMessages;
		this.sender = sender;
		
		if (mailboxMessages.size()>0) {
			Mailbox mail = mailboxMessages.get(0);			
			
			lastMessageDate = mail.getMessage().getDate();
			lastMessageHour = mail.getMessage().getHour();
			lastMessage = mail.getMessage();
			newMessage = !mail.isMessageRead();
			
			for(Mailbox m:mailboxMessages) {
				
				if ( m.getMessage().getDate().compareTo(lastMessageDate)>0 ){
					lastMessageDate = m.getMessage().getDate();
					lastMessageHour = m.getMessage().getHour();
					lastMessage = m.getMessage();
					newMessage = !m.isMessageRead();
				}
				else if ( m.getMessage().getDate().compareTo(lastMessageDate)==0
						&& m.getMessage().getHour().compareTo(lastMessageHour)>0){
					lastMessageDate = m.getMessage().getDate();
					lastMessageHour = m.getMessage().getHour();
					lastMessage = m.getMessage();
					newMessage = !m.isMessageRead();
				}
			}
		}
	}
	
	public List<Mailbox> getMailboxMessages() {
		return mailboxMessages;
	}

	public void setMailboxMessages(List<Mailbox> mailboxMessages) {
		this.mailboxMessages = mailboxMessages;
	}
	
	public String getName() {
		return sender.getNome();
	}

	public Date getLastMessageDate() {
		return lastMessageDate;
	}
	
	public String getLastMessageIntro() {
		String intro = lastMessage.getBodyMessage();
		if ( intro!=null && intro.length()>50 ) {
			intro = intro.substring(0, 47);
			intro += "...";
		}
		return intro;
	}

	public Pessoa getSender() {
		return sender;
	}

	public boolean getNewMessage() {
		return newMessage;
	}

	public Date getLastMessageHour() {
		return lastMessageHour;
	}

	public void setLastMessageHour(Date lastMessageHour) {
		this.lastMessageHour = lastMessageHour;
	}

	public InternalMessage getLastMessage() {
		return lastMessage;
	}
	
	
}