package br.com.runplanner.service;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.TimelessScheduleWeek;

@Service("timelessScheduleWeekService")
public class TimelessScheduleWeekServiceImpl extends
	GenericServiceImpl<TimelessScheduleWeek, Long> implements TimelessScheduleWeekService {

}
