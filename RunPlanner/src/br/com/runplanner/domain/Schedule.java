package br.com.runplanner.domain;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Schedule implements BasicEntity {
	
	private static final long serialVersionUID = 7890886297205396293L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Basic
	private String warmUp;
	
	@Basic
	private String description;
	
	@Basic
	private String coolDown;
	
	@Basic
	private String observations;
	
	@Temporal(TemporalType.DATE)
	private Date date;
	
	@Basic
	private double rating;
	
	@ManyToOne
	private Route route;
	
	
	@Override
	public boolean equals(Object obj) {
		if ( obj == null || !(obj instanceof Schedule) ) return false;
		
		Schedule schedule = (Schedule)obj;
		
		if ( !equalObj(schedule.getId(),this.getId()) ) return false;
		
		if ( !equalObj(schedule.getDate(),this.getDate()) ) return false;
		if ( !equalObj(schedule.getWarmUp(),this.getWarmUp()) ) return false;
		if ( !equalObj(schedule.getDescription(),this.getDescription()) ) return false;
		if ( !equalObj(schedule.getCoolDown(),this.getCoolDown()) ) return false;
		if ( !equalObj(schedule.getObservations(),this.getObservations()) ) return false;
		if ( !equalObj(schedule.getRating(),this.getRating()) ) return false;
				
		if ( schedule.getRoute()==null && this.getRoute()!=null ) return false;
		if ( schedule.getRoute()!=null && this.getRoute()==null ) return false;
		if ( schedule.getRoute()!=null &&
				this.getRoute()!=null &&
				!schedule.getRoute().getId().equals(this.getRoute().getId()) ) return false;

		return true;
	}
	
	private boolean equalObj(Object obj1, Object obj2) {
		if ( obj1==null && obj2!=null ) return false;
		if ( obj1!=null && obj2==null ) return false;
		if ( obj1!=null && obj2!=null && !obj1.equals(obj2) ) return false;
		return true;
	}
	
	@Override
	public int hashCode() {	
		return (description.hashCode()*7) + (date.hashCode()*13);
	}
	
	// -- Get and Set
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getWarmUp() {
		return warmUp;
	}

	public void setWarmUp(String warmUp) {
		this.warmUp = warmUp;
	}

	public String getCoolDown() {
		return coolDown;
	}

	public void setCoolDown(String coolDown) {
		this.coolDown = coolDown;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isSameDate(Date date) {
		
		Date d = this.getDate();
		
		if ( d.getDate() == date.getDate() &&
				d.getMonth() == date.getMonth() &&
				d.getYear() == date.getYear() ) {
			return true;
		}
		
		return false;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}
	
	/**
	 * Como os get desta classe nunca retornam null, esse metodo foi criado para informar 
	 * se o schedule foi ou não preenchido.
	 * @return <code>true</code> se não estiver preenchido e <code>false</code> do contrário.
	 */
	public boolean isEmpty() {
		boolean result = false;
		
		if ((description == null || description.equals("")) && (warmUp == null || warmUp.equals("")) 
				&& (coolDown == null || coolDown.equals("")) && (observations == null || observations.equals("")))
			result = true;
		
		return result;
	}
	
	public Schedule copy() {
		Schedule copy = new Schedule();
		copy.setCoolDown(this.getCoolDown());
		copy.setDate(this.getDate());
		copy.setDescription(this.getDescription());
		copy.setId(this.getId());
		copy.setObservations(this.getObservations());
		copy.setRating(this.getRating());
		copy.setRoute(this.getRoute());
		copy.setWarmUp(this.getWarmUp());
		
		return copy;
	}
}
