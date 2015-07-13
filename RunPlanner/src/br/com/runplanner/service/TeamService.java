package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Team;
import br.com.runplanner.exception.EntityNotFoundException;

public interface TeamService extends GenericService<Team, Long>{
	
	List<Team> getByAdvice(Long adviceId);
	void removeTeacher(Long adviceId, Long teacherId) throws EntityNotFoundException;
	void removeUser(Long adviceId, Long teacherId) throws EntityNotFoundException;
	
}
