package br.com.runplanner.service;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.EventPessoaActivity;

@Service("eventPessoaActivityService")
public class EventPessoaActivityServiceImpl extends GenericServiceImpl<EventPessoaActivity, Long> implements EventPessoaActivityService {
	

	@Transactional
	public EventPessoaActivity persist(EventPessoaActivity entity) {
		return super.persist(entity);
	}
	
	public EventPessoaActivity findByEventUser(Long eventId, Long customerId) {
		Query query = entityManager.createNamedQuery("EventPessoaActivity.findByEventUser");
		query.setParameter("eventId", eventId);
		query.setParameter("customerId", customerId);		
		
		try {
			return (EventPessoaActivity)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}		
	}

	@Transactional
	public EventPessoaActivity findByEventUserFull(Long eventId, Long customerId) {
		Query query = entityManager.createNamedQuery("EventPessoaActivity.findByEventUser");
		query.setParameter("eventId", eventId);
		query.setParameter("customerId", customerId);		
		
		try {
			EventPessoaActivity epa = (EventPessoaActivity)query.getSingleResult();		
			if ( epa.getActivity()!=null ) {
				epa.getActivity().getId();
			}
			if ( epa.getEvent()!=null ) {
				epa.getEvent().getId();
			}
			return epa;
		}
		catch (NoResultException  e) {
			return null;
		}	
	}

	@SuppressWarnings("unchecked")
	public List<EventPessoaActivity> findByEvent(Long eventId) {
		Query query = entityManager.createNamedQuery("EventPessoaActivity.findByEvent");
		query.setParameter("eventId", eventId);
		
		return (List<EventPessoaActivity>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public List<EventPessoaActivity> findByEventFull(Long eventId) {
		Query query = entityManager.createNamedQuery("EventPessoaActivity.findByEvent");
		query.setParameter("eventId", eventId);
				
		List<EventPessoaActivity> result = (List<EventPessoaActivity>)query.getResultList();
		
		if ( result!=null ) {
			for( EventPessoaActivity epa: result) {			
				if ( epa.getActivity()!=null ) {
					epa.getActivity().getId();
				}
				if ( epa.getEvent()!=null ) {
					epa.getEvent().getId();
				}
			}
		}
		
		return result;
	}
	
		
	public EventPessoaActivity findByActivity(Long activityId){
		Query query = entityManager.createNamedQuery("EventPessoaActivity.findByActivity");
		query.setParameter("activityId", activityId);
		
		try {
			return (EventPessoaActivity) query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}	
	}
	
}
