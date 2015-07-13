package br.com.runplanner.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class WeekDays implements BasicEntity {

	private static final long serialVersionUID = 3402567693942743977L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private Boolean sunday;

	@Basic
	private Boolean monday;

	@Basic
	private Boolean tuesday;

	@Basic
	private Boolean wednesday;

	@Basic
	private Boolean thursday;

	@Basic
	private Boolean friday;

	@Basic
	private Boolean saturday;

	
	//Gets and Sets
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getSunday() {
		return sunday;
	}

	public void setSunday(Boolean sunday) {
		this.sunday = sunday;
	}

	public Boolean getMonday() {
		return monday;
	}

	public void setMonday(Boolean monday) {
		this.monday = monday;
	}

	public Boolean getTuesday() {
		return tuesday;
	}

	public void setTuesday(Boolean tuesday) {
		this.tuesday = tuesday;
	}

	public Boolean getWednesday() {
		return wednesday;
	}

	public void setWednesday(Boolean wednesday) {
		this.wednesday = wednesday;
	}

	public Boolean getThursday() {
		return thursday;
	}

	public void setThursday(Boolean thursday) {
		this.thursday = thursday;
	}

	public Boolean getFriday() {
		return friday;
	}

	public void setFriday(Boolean friday) {
		this.friday = friday;
	}

	public Boolean getSaturday() {
		return saturday;
	}

	public void setSaturday(Boolean saturday) {
		this.saturday = saturday;
	}
	
}
