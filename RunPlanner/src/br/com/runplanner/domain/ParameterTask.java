package br.com.runplanner.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ParameterTask implements BasicEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private String description;
	
	@Enumerated
	private TaskType taskType;
	
	@Basic
	private int daysAfter;

	@Basic
	private String messageSubject;
	
	@Basic
	private String messageBody;
	
	@Basic
	private Boolean active = true;

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessageSubject() {
		return messageSubject;
	}

	public void setMessageSubject(String messageSubject) {
		this.messageSubject = messageSubject;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public int getDaysAfter() {
		return daysAfter;
	}

	public void setDaysAfter(int daysAfter) {
		this.daysAfter = daysAfter;
	}

}
