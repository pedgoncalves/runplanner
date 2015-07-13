package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Presence;

public interface PresenceService extends GenericService<Presence, Long> {

	public List<Presence> findByAlunoMes(Long id, int month);
	public Presence findByAlunoDia(Long id, String date1);
	public List<Presence> findByAlunoDias(Long id, String date1, String date2);
}
