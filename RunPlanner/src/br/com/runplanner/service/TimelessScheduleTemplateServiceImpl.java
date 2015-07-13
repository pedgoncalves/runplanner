package br.com.runplanner.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.TimelessScheduleTemplate;
import br.com.runplanner.domain.TimelessScheduleWeek;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("timelessScheduleTemplateService")
public class TimelessScheduleTemplateServiceImpl extends
		GenericServiceImpl<TimelessScheduleTemplate, Long> implements TimelessScheduleTemplateService {

	private TimelessScheduleWeekService timelessScheduleWeekService;
	
	@Autowired
	public TimelessScheduleTemplateServiceImpl(
			TimelessScheduleWeekService timelessScheduleWeekService) {
		this.timelessScheduleWeekService = timelessScheduleWeekService;
	}
	
	@SuppressWarnings("unchecked")
	public List<TimelessScheduleTemplate> findByAdvice(Long adviceId){
		Query query = entityManager.createNamedQuery("TimelessScheduleTemplate.findByAdvice");
		query.setParameter("adviceId", adviceId);
		return (List<TimelessScheduleTemplate>)query.getResultList();
	}
	
	@Transactional
	public TimelessScheduleTemplate cloneTemplate(TimelessScheduleTemplate template) throws EntityNotFoundException {
				
		TimelessScheduleTemplate newTemplate = new TimelessScheduleTemplate();
		newTemplate.setAdvice( template.getAdvice() );
		newTemplate.setTitle( template.getTitle()+" - cópia" );	
		newTemplate.setObservations( template.getObservations() );
		
		newTemplate = persist(newTemplate);
		
		List<TimelessScheduleWeek> weeks = template.getWeeks();
		List<TimelessScheduleWeek> newWeeks = new ArrayList<TimelessScheduleWeek>();
		for(TimelessScheduleWeek week: weeks) {
			TimelessScheduleWeek newWeek = week.copy();
			newWeek.setId(null);
			newWeek.setTimelessTemplate(newTemplate);
			newWeeks.add(newWeek);
		}
		
		update(newTemplate);
		
		newTemplate.setWeeks(newWeeks);
		checkNullSchedules(newTemplate);
		
		return newTemplate;
	}
	
	private void checkNullSchedules(TimelessScheduleTemplate timelessScheduleTemplate) {
		for (TimelessScheduleWeek week : timelessScheduleTemplate.getWeeks()) {
			if(week.getFirstDay() != null) {
				week.getFirstDay().setId(null);
				if(((week.getFirstDay().getDescription() != null) && week.getFirstDay().getDescription().trim().equals(""))
						|| (week.getFirstDay().getDescription() == null)) {
					week.setFirstDay(null);
				}
			}
			if(week.getSecondDay() != null) {
				week.getSecondDay().setId(null);
				if(((week.getSecondDay().getDescription() != null) && week.getSecondDay().getDescription().trim().equals(""))
						|| (week.getSecondDay().getDescription() == null)) {
					week.setSecondDay(null);
				}
			}
			if(week.getThirdDay() != null) {
				week.getThirdDay().setId(null);
				if(((week.getThirdDay().getDescription() != null) && week.getThirdDay().getDescription().trim().equals(""))
						|| (week.getThirdDay().getDescription() == null)) {
					week.setThirdDay(null);
				}
			}
			if(week.getFourthDay() != null) {
				week.getFourthDay().setId(null);
				if(((week.getFourthDay().getDescription() != null) && week.getFourthDay().getDescription().trim().equals(""))
						|| (week.getFourthDay().getDescription() == null)) {
					week.setFourthDay(null);
				}
			}
			if(week.getFifthDay() != null) {
				week.getFifthDay().setId(null);
				if(((week.getFifthDay().getDescription() != null) && week.getFifthDay().getDescription().trim().equals(""))
						|| (week.getFifthDay().getDescription() == null)) {
					week.setFifthDay(null);
				}
			}
			if(week.getSixthDay() != null) {
				week.getSixthDay().setId(null);
				if(((week.getSixthDay().getDescription() != null) && week.getSixthDay().getDescription().trim().equals(""))
						|| (week.getSixthDay().getDescription() == null)) {
					week.setSixthDay(null);
				}
			}
			if(week.getSeventhDay() != null) {
				week.getSeventhDay().setId(null);
				if(((week.getSeventhDay().getDescription() != null) && week.getSeventhDay().getDescription().trim().equals(""))
						|| (week.getSeventhDay().getDescription() == null)) {
					week.setSeventhDay(null);
				}
			}
		}
	}
	
	@Transactional
	public void deleteWeek(TimelessScheduleWeek week) throws EntityNotFoundException {
		TimelessScheduleTemplate timelessScheduleTemplate = this.loadById(week.getTimelessTemplate().getId());
		
		timelessScheduleWeekService.deleteById(week.getId());
		timelessScheduleTemplate.getWeeks().remove(week);
		update(timelessScheduleTemplate);
	}
}
