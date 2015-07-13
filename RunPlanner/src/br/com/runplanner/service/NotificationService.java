package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Notification;
import br.com.runplanner.exception.EntityNotFoundException;

public interface NotificationService extends GenericService<Notification, Long>{
	List<Notification> getByAdvice(Long adviceId);
	List<Notification> getByAdvice(Long adviceId, int firstPosition, int numberOfRecords);
	List<Notification> findByObject(Long objectId);
	List<Notification> findByUser(Long userId, int firstPosition, int numberOfRecord);
	void deleteByObject(Long objectId);
	void deleteByUserObject(Long userId, Long objectId);
	void deleteByUser(Long userId)  throws EntityNotFoundException;
}
