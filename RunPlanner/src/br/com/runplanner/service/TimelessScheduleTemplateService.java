package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.TimelessScheduleTemplate;
import br.com.runplanner.domain.TimelessScheduleWeek;
import br.com.runplanner.exception.EntityNotFoundException;

public interface TimelessScheduleTemplateService extends GenericService<TimelessScheduleTemplate, Long> {
	List<TimelessScheduleTemplate> findByAdvice(Long adviceId);
	TimelessScheduleTemplate cloneTemplate(TimelessScheduleTemplate template) throws EntityNotFoundException;
	void deleteWeek(TimelessScheduleWeek week) throws EntityNotFoundException;
}
