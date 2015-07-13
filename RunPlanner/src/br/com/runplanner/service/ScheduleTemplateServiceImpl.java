package br.com.runplanner.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.ScheduleTemplate;
import br.com.runplanner.domain.SpreadsheetType;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("scheduleTemplateService")
public class ScheduleTemplateServiceImpl extends GenericServiceImpl<ScheduleTemplate, Long> implements ScheduleTemplateService {
	
	@Autowired
	private ScheduleService scheduleService;

    @Transactional
	public void deleteScheduleTemplate(ScheduleTemplate entity) throws EntityNotFoundException {		
        this.deleteById(entity.getId());
	}
    
    @Transactional
	public ScheduleTemplate removeSchedule(ScheduleTemplate template, Schedule schedule) {
    	
    	template = this.loadById(template.getId());
    	template.getSchedules().remove(schedule);
		
    	try {
			scheduleService.deleteById(schedule.getId());
	        this.update(template);
		} catch (EntityNotFoundException e) {
			//Ja foi removido antes
		}    	
    	
        
        return template;
	}

	@SuppressWarnings("unchecked")
	public List<ScheduleTemplate> findByCompetence(Integer month, Integer year, Long adviceId) {
		Query query = entityManager.createNamedQuery("ScheduleTemplate.findByCompetence");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("adviceId", adviceId);
		return (List<ScheduleTemplate>)query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<ScheduleTemplate> findByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("ScheduleTemplate.findByAdvice");
		query.setParameter("adviceId", adviceId);
		return (List<ScheduleTemplate>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<ScheduleTemplate> findByAdviceNameTitle(Long adviceId, String title) {
		Query query = entityManager.createNamedQuery("ScheduleTemplate.findByAdviceTitle");
		query.setParameter("adviceId", adviceId);
		query.setParameter("title", title);
		return (List<ScheduleTemplate>)query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<ScheduleTemplate> findByCompetenceNext(Integer month,
			Integer year, Long adviceId) {
		
		List<ScheduleTemplate> result = new ArrayList<ScheduleTemplate>();
		Query query = entityManager.createNamedQuery("ScheduleTemplate.findByCompetence");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("adviceId", adviceId);
		
		result.addAll((List<ScheduleTemplate>)query.getResultList());
		
		month++;
		if ( month==12 ) {
			month=0;
			year++;
		}
		query = entityManager.createNamedQuery("ScheduleTemplate.findByCompetence");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("adviceId", adviceId);
		
		result.addAll((List<ScheduleTemplate>)query.getResultList());
		
		return result;
	}

	@Transactional
	public ScheduleTemplate cloneTemplate(ScheduleTemplate template) {
				
		int count = 1;
		String title = template.getTitle()+" - cópia";
		String newTitle = title;
		while ( this.findByAdviceNameTitle(template.getAdvice().getId(), newTitle).size() != 0 ) {
			count++;
			newTitle = title+" "+count;
		}
		
		ScheduleTemplate newTemplate = new ScheduleTemplate();
		newTemplate.setAdvice( template.getAdvice() );
		newTemplate.setTitle( newTitle );		
		newTemplate.setMonth( template.getMonth() );
		newTemplate.setYear( template.getYear() );
		newTemplate.setObservations( template.getObservations() );
		newTemplate.setType( template.getType() );
		
		if( newTemplate.getType().equals(SpreadsheetType.WEEK) ) {
			newTemplate.setDay( template.getDay() );
		}
		else {
			newTemplate.setDay( 1 );
		}
		
		
		List<Schedule> scheduleList = template.getSchedules();
		List<Schedule> newScheduleList = new ArrayList<Schedule>();		
		for(Schedule schedule:scheduleList) {
			
			Schedule newSchedule = new Schedule();
			newSchedule.setCoolDown( schedule.getCoolDown() );			
			newSchedule.setDate( schedule.getDate() );
			newSchedule.setDescription( schedule.getDescription() );
			newSchedule.setObservations( schedule.getObservations() );
			newSchedule.setWarmUp( schedule.getWarmUp() );
			
			if (schedule.getRoute() != null) {
				newSchedule.setRoute(schedule.getRoute().getId() != null?schedule.getRoute():null );
			} else {
				newSchedule.setRoute(null);
			}
			
			newSchedule = scheduleService.persist(newSchedule);			
			newScheduleList.add(newSchedule);			
		}
		
		newTemplate.setSchedules(newScheduleList);
		newTemplate = persist(newTemplate);
		
		return newTemplate;
	}
	
}
