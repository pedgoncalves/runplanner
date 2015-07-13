package br.com.runplanner.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.EventPessoaActivity;
import br.com.runplanner.domain.Notification;
import br.com.runplanner.domain.NotificationType;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("notificationService")
public class NotificationServiceImpl extends GenericServiceImpl<Notification, Long> implements NotificationService {

	@Autowired
	private CommentService commentService;
	
	@Autowired
	private ActivityService activityService;
	
	@Autowired
	private EventPessoaActivityService eventPessoaActivityService;
	
	
	@SuppressWarnings("unchecked")
	public List<Notification> getByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("Notification.findByAdvice");
		query.setParameter("adviceId", adviceId);
		
		List<Notification> result = (List<Notification>)query.getResultList();
		for( Notification notification:result ) {
			
			if ( notification.getType()==NotificationType.ACTIVITY ) {
				int commentSize = commentService.countByActivityId(notification.getObjectId());
				notification.setCommentNumber(commentSize);
				
				Activity activity = new Activity();
				activity.setId(notification.getObjectId());
				activity.setTotalDistance(activityService.getActivityDistance(activity.getId()));
				notification.setActivity(activity);
				
				getEventPessoaActivity(notification, activity);
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Notification> getByAdvice(Long adviceId, int firstPosition, int numberOfRecords) {
		Query query = entityManager.createNamedQuery("Notification.findByAdvice");
		query.setParameter("adviceId", adviceId);
		query.setFirstResult(firstPosition);
		query.setMaxResults(numberOfRecords);
		
		List<Notification> result = (List<Notification>)query.getResultList();
		for( Notification notification:result ) {
			
			if ( notification.getType()==NotificationType.ACTIVITY ) {
				int commentSize = commentService.countByActivityId(notification.getObjectId());
				notification.setCommentNumber(commentSize);
				
				Activity activity = new Activity();
				activity.setId(notification.getObjectId());
				activity.setTotalDistance(activityService.getActivityDistance(activity.getId()));
				activity.setTime(activityService.getActivityTime(activity.getId())); 
				notification.setActivity(activity);
				
				getEventPessoaActivity(notification, activity);
			}
		}
		
		return result;
	}	

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Notification> findByObject(Long objectId) {
		Query query = entityManager.createNamedQuery("Notification.findByObject");
		query.setParameter("objectId", objectId);
		
		List<Notification> result = (List<Notification>)query.getResultList();
		for( Notification notification:result ) {
			if ( notification.getType()==NotificationType.ACTIVITY ) {

				int commentSize = commentService.countByActivityId(notification.getObjectId());
				notification.setCommentNumber(commentSize);
				
				Activity activity = activityService.loadById(notification.getObjectId());
				notification.setActivity(activity);
				
				getEventPessoaActivity(notification, activity);
			}
		}
		
		return result;
	}

	private void getEventPessoaActivity(Notification notification,
			Activity activity) {
		EventPessoaActivity epa = eventPessoaActivityService.findByActivity(activity.getId());
		if ( epa!=null && epa.getActivity()!=null ) {
			Event event = epa.getEvent();
			notification.setEvent(event);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public void deleteByObject(Long objectId) {
		Query query = entityManager.createNamedQuery("Notification.findByObject");
		query.setParameter("objectId", objectId);
		List<Notification> list = (List<Notification>)query.getResultList();
		for(Notification n:list) {
			try {
				deleteById(n.getId());
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void deleteByUserObject(Long userId, Long objectId) {
		Query query = entityManager.createNamedQuery("Notification.findByUserObject");
		query.setParameter("objectId", objectId);
		query.setParameter("userId", userId);
		List<Notification> list = (List<Notification>)query.getResultList();
		for(Notification n:list) {
			try {
				deleteById(n.getId());
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	public List<Notification> findByUser(Long userId, int firstPosition, int numberOfRecords) {
		Query query = entityManager.createNamedQuery("Notification.findByUser");
		query.setParameter("userId", userId);
		query.setFirstResult(firstPosition);
		query.setMaxResults(numberOfRecords);

		List<Notification> result = (List<Notification>)query.getResultList();
		for( Notification notification:result ) {
			if ( notification.getType()==NotificationType.ACTIVITY ) {

				int commentSize = commentService.countByActivityId(notification.getObjectId());
				notification.setCommentNumber(commentSize);
				
				Activity activity = activityService.loadById(notification.getObjectId());
				notification.setActivity(activity);
				
				getEventPessoaActivity(notification, activity);
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void deleteByUser(Long userId) throws EntityNotFoundException {
		Query query = entityManager.createNamedQuery("Notification.findByUser");
		query.setParameter("userId", userId);

		List<Notification> result = (List<Notification>)query.getResultList();
		for(Notification n:result) {
			this.deleteById(n.getId());
		}
	}
	
}