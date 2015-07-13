package br.com.runplanner.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
	@NamedQuery(name = "Equipment.findByStudent", query = "select o from Equipment o "
		+ "where (o.student.id = :studentId) and (o.active = :active) order by o.name"),			
	@NamedQuery(name = "Equipment.findByActivity", query = "select o from Equipment o "
		+ "where o.activity.id=:activityId"),	
	@NamedQuery(name = "Equipment.findActivityByEquipment", query = "select o from Activity o "
			+ "where o.equipment.id=:equipmentId"),			
	@NamedQuery(name = "Equipment.loadByName", query = "select o from Equipment o "
		+ "where (o.student.id = :studentId) and (o.name = :name)")		
		})
public class Equipment implements BasicEntity  {

	private static final long serialVersionUID = -7219808783680856687L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private String name;
	
	@Basic
	private String description;
	
	@Temporal(value=TemporalType.DATE)
	private Date date;
	
	@Basic
	private Double kilometers;
	
	@Basic
	private Boolean active;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Pessoa student;
	
	@ManyToMany(fetch=FetchType.LAZY, mappedBy="equipment", cascade={})
	private List<Activity> activity;	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getKilometers() {
		return kilometers;
	}

	public void setKilometers(Double kilometers) {
		this.kilometers = kilometers;
	}

	public List<Activity> getActivity() {
		return activity;
	}

	public void setActivity(List<Activity> activity) {
		this.activity = activity;
	}

	public Pessoa getStudent() {
		return student;
	}

	public void setStudent(Pessoa student) {
		this.student = student;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	
}
