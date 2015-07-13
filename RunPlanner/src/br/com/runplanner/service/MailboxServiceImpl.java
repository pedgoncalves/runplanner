package br.com.runplanner.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Mailbox;
import br.com.runplanner.domain.MailboxGroup;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("mailboxService")
public class MailboxServiceImpl extends GenericServiceImpl<Mailbox, Long>
		implements MailboxService {

	@Autowired
	private InternalMessageService internalMessage;

	@SuppressWarnings("unchecked")
	public List<Mailbox> findByUserId(Long userId) {
		Query query = entityManager.createNamedQuery("Mailbox.findByUserId");
		query.setParameter("userId", userId);
		return (List<Mailbox>) query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<MailboxGroup> findByUserIdGrouped(Long userId) {
		Query query = entityManager.createNamedQuery("Mailbox.findByUserId");		 
		query.setParameter("userId", userId);
		List<Mailbox> list = (List<Mailbox>)query.getResultList();
					
		Hashtable<Long, List<Mailbox>> grouped = new Hashtable<Long, List<Mailbox>>();
		for(Mailbox mail:list) {
			Long senderId = mail.getMessage().getSender().getId();
			List<Mailbox> groupList = grouped.get(senderId);
			
			if ( groupList==null ) {
				groupList = new ArrayList<Mailbox>();
			}
			
			groupList.add(mail);
			grouped.put(senderId, groupList);
		}
	
		List<MailboxGroup> result = new ArrayList<MailboxGroup>();	
		Enumeration<List<Mailbox>> enumertation = grouped.elements();		
		while( enumertation.hasMoreElements() ) {
			List<Mailbox> groupList = enumertation.nextElement();
			
			Mailbox mb = groupList.get(0);			
			Long receiver = mb.getReceiver().getId();
			Long sender = mb.getMessage().getSender().getId();
			
			//Buscar por toda a conversar, dos 2 lados
			Query nativeQuery = entityManager.createNativeQuery("SELECT m.* " +
					"FROM Mailbox m, InternalMessage i " +
					"where i.id=m.message_id and ((m.receiver_id=:receiver1 and i.sender_id=:sender) " +
					"or (m.receiver_id=:receiver2 and i.sender_id=:sender2)) order by i.date, i.hour asc", 
					Mailbox.class);		
			nativeQuery.setParameter("receiver1", receiver);
			nativeQuery.setParameter("receiver2", sender);
			nativeQuery.setParameter("sender", sender);
			nativeQuery.setParameter("sender2", receiver);
			groupList = (List<Mailbox>)nativeQuery.getResultList();
			
			MailboxGroup group = new MailboxGroup(groupList, mb.getMessage().getSender());
			result.add(group);			
		}
		
		//Buscar mensagens enviadas sem resposta
		Query nativeQuery = entityManager.createNativeQuery("SELECT m.* " +
				"FROM Mailbox m, InternalMessage i " +
				"where i.id=m.message_id and i.sender_id=:userId1 and m.receiver_id not in ( " +
				"select im.sender_id from Mailbox mb, InternalMessage im " +
				"where im.id=mb.message_id and im.sender_id=m.receiver_id and mb.receiver_id=:userId2) " +
				"order by i.date, i.hour desc", 
				Mailbox.class);	
		nativeQuery.setParameter("userId1", userId);
		nativeQuery.setParameter("userId2", userId);
		List<Mailbox> mailboxList = (List<Mailbox>)nativeQuery.getResultList();
		
		Hashtable<Long, List<Mailbox>> grouped2 = new Hashtable<Long, List<Mailbox>>();
		for (Mailbox mail:mailboxList) {
			Long receiverId = mail.getReceiver().getId();
			
			List<Mailbox> groupList = grouped2.get(receiverId);
			
			if ( groupList==null ) {
				groupList = new ArrayList<Mailbox>();
			}
			
			groupList.add(mail);
			grouped2.put(receiverId, groupList);
		}	
		
		enumertation = grouped2.elements();
		while( enumertation.hasMoreElements() ) {
			List<Mailbox> groupList = enumertation.nextElement();			
			Mailbox mb = groupList.get(0);
			MailboxGroup group = new MailboxGroup(groupList, mb.getReceiver());
			result.add(group);			
		}
		
		
					
		
		return result;		
	}

	private Long sumByInternalMessage(Long msgId) {
		Query query = entityManager.createNamedQuery("Mailbox.sumByInternalMessage");
		query.setParameter("msgId", msgId);
		return (Long) query.getSingleResult();
	}

	@Transactional
	public void deleteById(Long id) throws EntityNotFoundException {
		Mailbox mb = this.loadById(id);
		Long messageId = mb.getMessage().getId();
		
		if (sumByInternalMessage(messageId).intValue() == 1) {
			super.deleteById(id);
			internalMessage.deleteById(messageId);
		} else {
			super.deleteById(id);
		}
	}

	public InternalMessageService getInternalMessage() {
		return internalMessage;
	}

	public void setInternalMessage(InternalMessageService internalMessage) {
		this.internalMessage = internalMessage;
	}

	public Long getNewByUserId(Long userId) {
		Query query = entityManager.createNamedQuery("Mailbox.countNewByUserId");
		query.setParameter("userId", userId);
		return (Long) query.getSingleResult();
	}

}
