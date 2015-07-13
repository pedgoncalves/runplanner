package br.com.runplanner.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Team;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("teamService")
public class TeamServiceImpl extends GenericServiceImpl<Team, Long> implements TeamService {

	@SuppressWarnings("unchecked")
	public List<Team> getByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("Team.findByAdvice");
		query.setParameter("adviceId", adviceId);
		return (List<Team>)query.getResultList();
	}

	public void removeTeacher(Long adviceId, Long teacherId) throws EntityNotFoundException {
		List<Team> teams = getByAdvice(adviceId);
		
		for(Team t:teams) {
			
			List<Pessoa> teachers = t.getTeachers();
			if( teachers!=null && teachers.size()>0 ) {			
				for(int i=0; i<teachers.size(); i++) {
					Pessoa p = teachers.get(i);
					if ( p.getId().longValue() == teacherId.longValue() ) {
						teachers.remove(i);
						break;
					}
				}
			}
			
			t.setTeachers(teachers);
			update(t);
		}
		
	}
	

	public void removeUser(Long adviceId, Long teacherId) throws EntityNotFoundException {
		List<Team> teams = getByAdvice(adviceId);
		
		for(Team t:teams) {
			
			List<Pessoa> customers = t.getCustomers();
			
			if( customers!=null && customers.size()>0 ) {			
				for(int i=0; i<customers.size(); i++) {
					Pessoa p = customers.get(i);
					if ( p.getId().longValue() == teacherId.longValue() ) {
						customers.remove(i);
						break;
					}
				}
			}
			
			t.setCustomers(customers);
			update(t);
		}
		
	}
	
}