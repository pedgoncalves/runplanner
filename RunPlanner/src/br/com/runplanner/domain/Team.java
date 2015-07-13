package br.com.runplanner.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@NamedQueries({	
	@NamedQuery(name = "Team.findByAdvice", query = "select o from Team o "
		+ "where (o.advice.id = :adviceId) ")	
		})
public class Team implements BasicEntity {

	private static final long serialVersionUID = 8409878303927514751L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Advice advice;
	
	@Basic
	private String description;
	
	@Basic
	private String place;
	
	@Temporal(value=TemporalType.TIME)
	private Date initialHour;
	
	@OneToOne(cascade={CascadeType.ALL})
	private WeekDays days;
	
	@ManyToMany(fetch = FetchType.EAGER)
	private List<Pessoa> teachers;
	
	@OneToMany(mappedBy="team", fetch = FetchType.EAGER)
	@OrderBy("nome")
	private List<Pessoa> customers;
	
	@Transient
	private List<String> presenceColumns;

	public Team() {
		days = new WeekDays();
	}

	//Gets and sets
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public Date getInitialHour() {
		return initialHour;
	}

	public void setInitialHour(Date initialHour) {
		this.initialHour = initialHour;
	}

	public WeekDays getDays() {
		return days;
	}

	public void setDays(WeekDays days) {
		this.days = days;
	}

	public List<Pessoa> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<Pessoa> teachers) {
		this.teachers = teachers;
	}

	public List<Pessoa> getCustomers() {
		return customers;
	}

	public void setCustomers(List<Pessoa> customers) {
		this.customers = customers;
	}

	public List<String> getPresenceColumns() {
		return presenceColumns;
	}

	public void setPresenceColumns(List<String> presenceColumns) {
		this.presenceColumns = presenceColumns;
	}

	
}
