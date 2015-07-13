package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;

public interface EventService extends GenericService<Event, Long>{
	
	List<Event> getByAdvice(Long adviceId);
	Long countByAdvice(Long adviceId);
	List<Event> getByAdviceInactive(Long adviceId);
	List<Event> getByUserInactive(Long adviceId, Long userId);
	List<Event> getByUserActive(Long adviceId, Long userId);
	Event addParticipant(Event entity, Pessoa participant) throws EntityNotFoundException;
	Event removeParticipant(Event event, Pessoa participant) throws EntityNotFoundException;
	void removeParticipantAllEvents(Long adviceId, Pessoa participant) throws EntityNotFoundException;
	void setEventActivity(Event event, Pessoa user, Activity activity) throws EntityNotFoundException;
	void removeEventActivity(Activity activity) throws EntityNotFoundException;
	Event loadByIdEager(Long eventId);
	List<Event> loadByAdviceIdEager(Long adviceId);
	
}
