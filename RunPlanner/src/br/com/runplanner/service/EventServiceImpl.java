package br.com.runplanner.service;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.EventPessoaActivity;
import br.com.runplanner.domain.Notification;
import br.com.runplanner.domain.NotificationType;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("eventService")
public class EventServiceImpl extends GenericServiceImpl<Event, Long> implements EventService {

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private PessoaService pessoaService;
	
	@Autowired
	private EventPessoaActivityService eventPessoaActivityService;
	
	@SuppressWarnings("unchecked")
	public List<Event> getByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("Event.findByAdvice");
		query.setParameter("adviceId", adviceId);
		return (List<Event>)query.getResultList();
	}
	
	public Long countByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("Event.countByAdvice");
		query.setParameter("adviceId", adviceId);
		try {
			return (Long)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}	
	}	
	
	@Transactional
	@SuppressWarnings("unchecked")
	public List<Event> getByUserInactive(Long adviceId, Long userId) {
		Query query = entityManager.createNamedQuery("Event.findByAdviceInactive");
		query.setParameter("adviceId", adviceId);
		
		List<Event> events = (List<Event>)query.getResultList();
		boolean remove = true;
		for(int i=0; i<events.size(); i++) {
			Event e = events.get(i);			
			remove = true;
			for( Pessoa p:e.getCustomers() ) {
				if(p.getId().longValue()==userId) {
					remove = false;
				}
			}
			
			if ( remove ) {
				events.remove(i);
				i--;
			}
		}
		
		return events;
	}
	
	public List<Event> getByUserActive(Long adviceId, Long userId) {		
		List<Event> events = getByAdvice(adviceId);
		boolean remove = true;
		for(int i=0; i<events.size(); i++) {
			Event e = events.get(i);			
			remove = true;
			for( Pessoa p:e.getCustomers() ) {
				if(p.getId().longValue()==userId) {
					remove = false;
				}
			}
			
			if ( remove ) {
				events.remove(i);
				i--;
			}
		}
		
		return events;
	}
	
	@Override
	@Transactional
	public Event persist(Event entity) {
		
		entity = super.persist(entity);
		
		Notification notification = new Notification();
		notification.setAdvice(entity.getAdvice());
		notification.setObjectId(entity.getId());
		notification.setTitle(entity.getName());
		notification.setType(NotificationType.EVENT);
		notification.setDate(new Date());
		notification.setCustomer(null);
		
		notificationService.persist(notification);
		
		return entity;
	}
	
	@Override
	@Transactional
	public void deleteById(Long id) throws EntityNotFoundException {	
		super.deleteById(id);
		notificationService.deleteByObject(id);
	}
	
	@Transactional
	public Event addParticipant(Event entity, Pessoa participant) throws EntityNotFoundException {
		
		entity = loadById(entity.getId());
		participant = pessoaService.loadById(participant.getId());
		
		if ( eventPessoaActivityService.findByEventUser(entity.getId(), participant.getId())!=null ) {
			//Ja esta cadastrado
			return entity;
		}
		
		
		EventPessoaActivity association = new EventPessoaActivity();
		association.setEvent(entity);
		association.setCustomer(participant);
		
		association = eventPessoaActivityService.persist(association);
		
		entity.addAssociation(association);
		update(entity);
		
		Notification notification = new Notification();
		notification.setAdvice(entity.getAdvice());
		notification.setObjectId(entity.getId());
		notification.setTitle(entity.getName());
		notification.setType(NotificationType.EVENT_PARTICIPANT);
		notification.setDate(new Date());
		notification.setCustomer(participant);
		
		notificationService.persist(notification);
		
		return entity;
	}
	
	@Transactional
	public Event removeParticipant(Event event, Pessoa participant) throws EntityNotFoundException {
		
		event = loadById(event.getId());
		participant = pessoaService.loadById(participant.getId());
		
		
		EventPessoaActivity association = eventPessoaActivityService.findByEventUser(event.getId(), participant.getId());
		if ( association==null ) {
			//Ja nao esta cadastrado
			return event;
		}
		
		//Retira ligacao com o evento
		event.removeAssociation(association);
		update(event);
		
		//Retira notificacao
		notificationService.deleteByUserObject(participant.getId(), event.getId());
		
		//Apaga associacao
		eventPessoaActivityService.deleteById(association.getId());
		
		
		return event;
	}

	public void removeParticipantAllEvents(Long adviceId, Pessoa pessoa) throws EntityNotFoundException {
		List<Event> events = getByAdvice(adviceId);
		events.addAll( getByAdviceInactive(adviceId) );
		
		for(Event e:events) {
			removeParticipant(e,pessoa);
		}
	}
	
	public void setEventActivity(Event event, Pessoa user, Activity activity) throws EntityNotFoundException {
		EventPessoaActivity association = eventPessoaActivityService.findByEventUser(event.getId(), user.getId());
		if ( association!=null ) {
			association.setActivity(activity);
			eventPessoaActivityService.update(association);			
		}
		else {
			association = new EventPessoaActivity();
			association.setEvent(event);
			association.setCustomer(user);
			
			eventPessoaActivityService.persist(association);
			
			event.addAssociation(association);
			update(event);
		}
		
	}
		
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@SuppressWarnings("unchecked")
	public List<Event> getByAdviceInactive(Long adviceId) {
		Query query = entityManager.createNamedQuery("Event.findByAdviceInactive");
		query.setParameter("adviceId", adviceId);
		return (List<Event>)query.getResultList();
	}

	@Transactional
	public void removeEventActivity(Activity activity)
			throws EntityNotFoundException {
		EventPessoaActivity association = eventPessoaActivityService.findByActivity(activity.getId());
		if ( association!=null ) {
			association.setEvent(null);
			eventPessoaActivityService.update(association);			
		}
		
	}
	
	@Transactional
	public Event loadByIdEager(Long eventId) {
		Event e = super.loadById(eventId);
		e.getAssociations().size();
		
		return e;
	}
	
	@Transactional
	public List<Event> loadByAdviceIdEager(Long adviceId) {
		List<Event> events = getByAdvice(adviceId);
		for(int i=0;i<events.size();i++){
			events.get(i).getAssociations().size();
		}
		
		return events;
	}
	
	
}