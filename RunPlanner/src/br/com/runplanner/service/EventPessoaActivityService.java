package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.EventPessoaActivity;

public interface EventPessoaActivityService  extends GenericService<EventPessoaActivity, Long> {

	EventPessoaActivity findByEventUser(Long eventId, Long customerId);
	EventPessoaActivity findByActivity(Long activityId);
	EventPessoaActivity findByEventUserFull(Long eventId, Long customerId);
	List<EventPessoaActivity> findByEvent(Long eventId);
	List<EventPessoaActivity> findByEventFull(Long eventId);
}
