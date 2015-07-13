package br.com.runplanner.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.Presence;

@Service("presenceService")
public class PresenceServiceImpl extends GenericServiceImpl<Presence, Long>
		implements PresenceService {

	@SuppressWarnings("unchecked")
	public List<Presence> findByAlunoMes(Long id, int month) {
		Query query = entityManager.createNamedQuery("Presence.findByAlunoMes");
		query.setParameter("aluno", id);
		query.setParameter("month", month);
		return (List<Presence>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Presence> findByAlunoDias(Long id, String date1, String date2) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		
		Query query = entityManager.createNamedQuery("Presence.findByAlunoDias");
		query.setParameter("aluno", id);
		try {
			query.setParameter("date1",	sdf.parse(date1));
			query.setParameter("date2", sdf.parse(date2));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return (List<Presence>)query.getResultList();
	}
	
	public Presence findByAlunoDia(Long id, String date1) {
		Query query = entityManager.createNamedQuery("Presence.findByAlunoDia");
		query.setParameter("aluno", id);
		try {
			query.setParameter("date1", DateFormat.getDateInstance().parse(date1));
			return (Presence)query.getSingleResult();
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}

	
}
