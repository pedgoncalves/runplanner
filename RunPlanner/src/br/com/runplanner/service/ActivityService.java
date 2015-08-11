package br.com.runplanner.service;

import java.util.Date;
import java.util.List;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.ActivityTack;
import br.com.runplanner.to.TopActivityTO;


public interface ActivityService extends GenericService<Activity, Long>{
	
	List<ActivityTack> findTracksById(Long activityId);
	List<Activity> findByUserId(Long userId);
	List<Activity> findByUserIdAsc(Long userId);
	List<Activity> findByUserIdDateAsc(Long userId, Date initialDate, Date finalDate);
	Long countByAdvice(Long adviceId);
	ActivityTack getLastActivityPosition(Long studentId);
	Double getActivityDistance(Long activityId);
	String getActivityTime(Long activityId);
	Activity getLongestActivityKm(Long studentId);
	Activity getLongestActivityTime(Long studentId);
	Activity getLongestActivityPace(Long studentId);
	List<TopActivityTO> getTopActivity5k(Long adviceId, int sex);
	List<TopActivityTO> getTopActivity10k(Long adviceId, int sex);
	
}
