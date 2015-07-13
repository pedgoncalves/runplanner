package br.com.runplanner.view.util;

import java.util.Date;

import org.primefaces.model.DefaultScheduleEvent;

/**
 * Essa classe se propoe a disponibilizar o ID gravado no banco no componente JSF
 */
public class CustonScheduleEvent extends DefaultScheduleEvent {

	private static final long serialVersionUID = 2647549666415734590L;
	
	/**
	 * Identificador gravado no banco
	 */
	private Long scheduleId;
	
	public CustonScheduleEvent(String description, Date date) {
		super(description,date,date);
	}

	public Long getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}
	
}
