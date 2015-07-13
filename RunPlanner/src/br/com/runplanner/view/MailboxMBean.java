package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.InternalMessage;
import br.com.runplanner.domain.Mailbox;
import br.com.runplanner.domain.MailboxGroup;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.MailboxService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.view.util.Constants;

@Component("mailboxMBean")
@Scope("session")
public class MailboxMBean extends BasicMBean {

	private static final String PAGE_MAILBOX = "/pages/mailbox/mailbox";
	private static final String PAGE_MAILBOX_VIEW = "/pages/mailbox/mailboxView";

	private MailboxService mailboxService;
	private PessoaService pessoaService;
	
	private Mailbox mailbox;
	private InternalMessage message;
	private List<Mailbox> mailboxMessages;
	private List<MailboxGroup> groupMessages;
	private MailboxGroup group;
	private Pessoa user;
	private Advice advice;
	private Pessoa selectedReceiver;
	private String bodyMessage;
	

	private List<SelectItem> customerList = new ArrayList<SelectItem>();
	
	private Hashtable<Long,Object> photoCache = new Hashtable<Long,Object>();
	
	@Autowired
	public MailboxMBean(MailboxService mailboxService, PessoaService pessoaService) {
		user = getSessionUser();
		advice = user.getAdvice();
		
		this.mailboxService = mailboxService;
		this.pessoaService = pessoaService;

		this.selectedReceiver = new Pessoa();
		this.message = new InternalMessage();
		this.mailbox = new Mailbox();
	}

	@Override
	public String goToList() {
		user = getSessionUser();
		
		//mailboxMessages = mailboxService.findByUserId(user.getId());
		groupMessages = mailboxService.findByUserIdGrouped(user.getId());
		message = new InternalMessage();
		mailbox = new Mailbox();
		selectedReceiver = new Pessoa();
		
		customerList = new ArrayList<SelectItem>();
    	for( Pessoa pessoa: pessoaService.getByAdvice(advice.getId(),true) ) {
    		if( pessoa.getId().longValue() == user.getId().longValue() ) continue;
    		customerList.add(new SelectItem(pessoa.getId(),pessoa.getNome()));	
		}
    	
    	setSelectedMenu(Constants.MENU_NONE);
		return PAGE_MAILBOX;
	}

	public String send() {		
		
		if ( message.getBodyMessage().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"mailbox.message.mandatory");
			return goToList();
		}
		
		Date date = new Date();		
		
		selectedReceiver = pessoaService.loadById(selectedReceiver.getId());
		
		String msg = message.getBodyMessage();
		
		message = new InternalMessage();
		message.setBodyMessage(msg);
		message.setSender(user);
		message.setDate(date);
		message.setHour(date);
		
		mailbox = new Mailbox();
		mailbox.setMessage(message);
		mailbox.setMessageRead(false);
		mailbox.setReceiver(selectedReceiver);
		
		mailboxService.persist(mailbox);
		addMessage(FacesMessage.SEVERITY_INFO,"mailbox.message.send.ok");
		
		return goToList();
	}
	
	public List<Pessoa> complete(String value) {		
		List<Pessoa> result = pessoaService.getByNameAdviceActive(value, advice.getId(), true);
		return result;
	}
	
	public void readGroup() {
		Pessoa user = getSessionUser();
		List<Mailbox> list = group.getMailboxMessages();
		for(Mailbox mb:list) {
			
			if ( !mb.isMessageRead() && mb.getMessage().getSender().getId().longValue()!=user.getId().longValue() ) {
				mb.setReadDate(new Date());
				mb.setMessageRead(Boolean.TRUE);
				
				try {
					mailboxService.update(mb);
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if ( mb.getMessage().getSender().getId().longValue()!=user.getId().longValue() ) {
				selectedReceiver = mb.getMessage().getSender(); 
			}
			else if ( mb.getReceiver().getId().longValue()!=user.getId().longValue() ) {
				selectedReceiver = mb.getReceiver(); 
			}
		}
	}
	
	
	@Override
	public String goToModify() {
		mailbox = mailboxService.loadById(mailbox.getId());
		if(!mailbox.isMessageRead()) {
			mailbox.setMessageRead(true);
			mailbox.setReadDate(new Date());
			try {
				mailboxService.update(mailbox);
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if( mailbox!=null && mailbox.getMessage()!=null && mailbox.getMessage().getBodyMessage()!=null ) {			
			String message = mailbox.getMessage().getBodyMessage();
    		message = message.replaceAll("\n", "<br/>");
    		mailbox.getMessage().setBodyMessage(message);
		}

    	setSelectedMenu(Constants.MENU_NONE);
		return PAGE_MAILBOX_VIEW;
	}

	@Override
	public String delete() {
		mailbox = mailboxService.loadById(mailbox.getId());
		try {
			mailboxService.deleteById(mailbox.getId());
			addMessage(FacesMessage.SEVERITY_INFO,"mailbox.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");
		}
		return goToList();
	}
	
	@Override
	public String goToCreate() {
		return null;
	}
	
	private DefaultStreamedContent getDefaultPhoto() {
		String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_PHOTO;
		
		try {
			FileInputStream is = new FileInputStream(new File(photoPath));
			byte[] photo = new byte[is.available()];
			is.read(photo);
			
			ByteArrayInputStream stream = new ByteArrayInputStream(photo);

			return new DefaultStreamedContent(stream, "image/jpeg");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public StreamedContent getStreamedUserPhoto() {
		
		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("client_id");
				
		if( id==null || id.equals("") ) {
			return getDefaultPhoto();
		}		
		
		Long clientId = Long.parseLong(id);
		
		byte[] photo = (byte[]) photoCache.get(clientId);				
		if ( photo!=null ) {
			ByteArrayInputStream photoStream = new ByteArrayInputStream(photo);
			return new DefaultStreamedContent(photoStream, "image/jpeg");
		}
		else {
			Pessoa pessoa = pessoaService.loadById(clientId);
			
			if ( pessoa.getPhoto()!= null ) {
				ByteArrayInputStream photoStream = new ByteArrayInputStream(pessoa.getPhoto());
				
				photoCache.put( clientId, pessoa.getPhoto().clone() );
				return new DefaultStreamedContent(photoStream, "image/jpeg");
			}
			else {
				return getDefaultPhoto();
			}
		}
		
	}

	@Override
	public String save() {
		// TODO Auto-generated method stub
		return null;
	}

	public MailboxService getMailboxService() {
		return mailboxService;
	}

	public void setMailboxService(MailboxService mailboxService) {
		this.mailboxService = mailboxService;
	}

	public List<Mailbox> getMailboxMessages() {
		return mailboxMessages;
	}

	public void setMailboxMessages(List<Mailbox> mailboxMessages) {
		this.mailboxMessages = mailboxMessages;
	}

	public Mailbox getMailbox() {
		return mailbox;
	}

	public void setMailbox(Mailbox mailbox) {
		this.mailbox = mailbox;
	}

	public InternalMessage getMessage() {
		return message;
	}

	public void setMessage(InternalMessage message) {
		this.message = message;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public Pessoa getSelectedReceiver() {
		return selectedReceiver;
	}

	public void setSelectedReceiver(Pessoa selectedReceiver) {
		this.selectedReceiver = selectedReceiver;
	}

	public String getBodyMessage() {
		return bodyMessage;
	}

	public void setBodyMessage(String bodyMessage) {
		this.bodyMessage = bodyMessage;
	}

	public List<MailboxGroup> getGroupMessages() {
		return groupMessages;
	}

	public void setGroupMessages(List<MailboxGroup> groupMessages) {
		this.groupMessages = groupMessages;
	}

	public MailboxGroup getGroup() {
		return group;
	}

	public void setGroup(MailboxGroup group) {
		this.group = group;
	}

	public List<SelectItem> getCustomerList() {
		return customerList;
	}
	
	
}
