package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.ScheduleTemplate;
import br.com.runplanner.exception.EntityNotFoundException;

public interface ScheduleTemplateService extends GenericService<ScheduleTemplate, Long>{
	
	void deleteScheduleTemplate(ScheduleTemplate entity) throws EntityNotFoundException;
	ScheduleTemplate removeSchedule(ScheduleTemplate template, Schedule schedule);
	List<ScheduleTemplate> findByCompetence(Integer month, Integer year, Long adviceId);
	List<ScheduleTemplate> findByAdvice(Long adviceId);	
	List<ScheduleTemplate> findByCompetenceNext(Integer month, Integer year, Long adviceId);
	ScheduleTemplate cloneTemplate(ScheduleTemplate template);
	List<ScheduleTemplate> findByAdviceNameTitle(Long adviceId, String title);
	
}
