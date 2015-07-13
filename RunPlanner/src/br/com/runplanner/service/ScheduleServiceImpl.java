package br.com.runplanner.service;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.Schedule;

@Service("scheduleService")
public class ScheduleServiceImpl extends GenericServiceImpl<Schedule, Long> implements ScheduleService {

}
