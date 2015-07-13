package br.com.runplanner.service;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.ActivityTack;
import br.com.runplanner.domain.EventPessoaActivity;
import br.com.runplanner.domain.Notification;
import br.com.runplanner.domain.NotificationType;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.util.Utils;

@Service("activityService")
public class ActivityServiceImpl extends GenericServiceImpl<Activity, Long> implements ActivityService {

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private EventPessoaActivityService eventPessoaActivityService;
	
	@Autowired
	private CommentService commentService;
	
	@SuppressWarnings("unchecked")
	public List<ActivityTack> findTracksById(Long activityId) {
		Query query = entityManager.createNamedQuery("Activity.findTracksById");
		query.setParameter("activityId", activityId);
		return (List<ActivityTack>)query.getResultList();
	}	
	
	@SuppressWarnings("unchecked")
	public List<Activity> findByUserId(Long userId) {
		Query query = entityManager.createNamedQuery("Activity.findByUserId");
		query.setParameter("userId", userId);
		return (List<Activity>)query.getResultList();
	}
	
	@Override
	@Transactional
	public Activity persist(Activity entity) {
		
		entity = super.persist(entity);
		
		Pessoa user = entity.getStudent();
		
		Notification notification = new Notification();
		notification.setAdvice(user.getAdvice());
		notification.setObjectId(entity.getId());
		notification.setTitle(null);
		notification.setType(NotificationType.ACTIVITY);
		notification.setDate(new Date());
		notification.setCustomer(user);
		
		notificationService.persist(notification);
		
		return entity;
	}
	
	@Override
	@Transactional
	public void deleteById(Long id) throws EntityNotFoundException {	
		super.deleteById(id);
		
		//Apagar notificacoes
		notificationService.deleteByObject(id);
		
		//Apagar ligacao com evento
		EventPessoaActivity eventPessoaActivity = eventPessoaActivityService.findByActivity(id);
		if ( eventPessoaActivity!=null ) {
			eventPessoaActivity.setActivity(null);
			eventPessoaActivityService.update(eventPessoaActivity);
		}
		
		//Apagar comentarios
		commentService.deleteByActivityId(id);
		
	}

	@SuppressWarnings("unchecked")
	public List<Activity> findByUserIdAsc(Long userId) {
		Query query = entityManager.createNamedQuery("Activity.findByUserIdAsc");
		query.setParameter("userId", userId);
		return (List<Activity>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Activity> findByUserIdDateAsc(Long userId, Date initialDate, Date finalDate) {
		Query query = entityManager.createNamedQuery("Activity.findByUserIdDateAsc");
		query.setParameter("userId", userId);
		query.setParameter("initialDate", initialDate);
		query.setParameter("finalDate", finalDate);
		return (List<Activity>)query.getResultList();
	}
	
	public Long countByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("Activity.countByAdvice");
		query.setParameter("adviceId", adviceId);
		
		Long count = (Long) query.getSingleResult();
		
		if ( count == null ) {
			count = 0l;
		}
		return count;
	}
	
	public ActivityTack getLastActivityPosition(Long studentId) {
		
		Query nativeQuery = entityManager.createNativeQuery( "SELECT al.* " +
				"FROM Activity a, ActivityTack al "+
				"where a.id = al.activity_id and student_id=:studentId order by time desc LIMIT 1", ActivityTack.class);
		nativeQuery.setParameter("studentId", studentId);
		
		
		ActivityTack track = null;
		try { 
			track = (ActivityTack)nativeQuery.getSingleResult();
		}
		catch (NoResultException e) {
			//Nao possuia atividades antes
		}
		
		return track;
		
	}
	
	public Activity getLongestActivityKm(Long studentId) {
		Query nativeQuery = entityManager.createNativeQuery( "select * from Activity where id = ( select activity_id from ( select sum(distanceMeters) as totalDistance, activity_id from ActivityLap "
				+ "where activity_id in ( select id from Activity where student_id = :studentId ) group by activity_id order by totalDistance desc LIMIT 1 ) as TempTable )", Activity.class);
		nativeQuery.setParameter("studentId", studentId);
		
		
		Activity activity = null;
		try { 
			activity = (Activity)nativeQuery.getSingleResult();
		}
		catch (NoResultException e) {
			//Nao possuia atividades antes
		}
		
		return activity;
	}
	
	public Activity getLongestActivityTime(Long studentId) {
		Query nativeQuery = entityManager.createNativeQuery( "select * from Activity where id = ( select activity_id from ( select sum(totalTimeSeconds) as totalTime, activity_id from ActivityLap "
				+ "where activity_id in ( select id from Activity where student_id = :studentId ) group by activity_id order by totalTime desc LIMIT 1 ) as TempTable )", Activity.class);
		nativeQuery.setParameter("studentId", studentId);
		
		
		Activity activity = null;
		try { 
			activity = (Activity)nativeQuery.getSingleResult();
		}
		catch (NoResultException e) {
			//Nao possuia atividades antes
		}
		
		return activity;
	}
	
	public Activity getLongestActivityPace(Long studentId) {
		Query nativeQuery = entityManager.createNativeQuery( "select * from Activity where id = ( select activity_id from ( select (sum(distanceMeters)/sum(totalTimeSeconds)) as pace, activity_id from ActivityLap "
				+ "where activity_id in ( select id from Activity where student_id = :studentId ) group by activity_id order by pace desc LIMIT 1 ) as TempTable )", Activity.class);
		nativeQuery.setParameter("studentId", studentId);
		
		
		Activity activity = null;
		try { 
			activity = (Activity)nativeQuery.getSingleResult();
		}
		catch (NoResultException e) {
			//Nao possuia atividades antes
		}
		
		return activity;
	}
	
	public Double getActivityDistance(Long activityId) {
		
		Query nativeQuery = entityManager.createNativeQuery( "SELECT sum(distanceMeters)/1000 "+
				"FROM ActivityLap lap "+
				"where lap.activity_id=:activityId");
		nativeQuery.setParameter("activityId", activityId);
		
		
		Double distance = null;
		try { 
			distance = (Double)nativeQuery.getSingleResult();
		}
		catch (NoResultException e) {
			//Nao possuia atividades antes
		}
		
		return distance;
		
	}
	
	public String getActivityTime(Long activityId) {
		
		Query nativeQuery = entityManager.createNativeQuery( "SELECT sum(totalTimeSeconds) "+
				"FROM ActivityLap lap "+
				"where lap.activity_id=:activityId");
		nativeQuery.setParameter("activityId", activityId);
		
		String tempo = "00:00:00";
		Double result = null;
		try { 
			result = (Double)nativeQuery.getSingleResult();
			tempo = Utils.formataTempo(result.intValue());
		}
		catch (NoResultException e) {
			//Nao possuia atividades antes
		}
		
		return tempo;
		
	}
	
}