package br.com.runplanner.domain;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity 
public class ActivityTack implements BasicEntity {

	private static final long serialVersionUID = 2135323665179307206L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;
	
	@Basic
	private Double latitudeDegrees;
	
	@Basic
	private Double longitudeDegrees;
	
	@Basic
	private Double altitudeMeters;
	
	@Basic
	private Double distanceMeters;
	
	@ManyToOne
	@OrderBy("id ASC")
	private Activity activity;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getLatitudeDegrees() {
		return latitudeDegrees;
	}

	public void setLatitudeDegrees(Double latitudeDegrees) {
		this.latitudeDegrees = latitudeDegrees;
	}

	public Double getLongitudeDegrees() {
		return longitudeDegrees;
	}

	public void setLongitudeDegrees(Double longitudeDegrees) {
		this.longitudeDegrees = longitudeDegrees;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Double getAltitudeMeters() {
		return altitudeMeters;
	}

	public void setAltitudeMeters(Double altitudeMeters) {
		this.altitudeMeters = altitudeMeters;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Double getDistanceMeters() {
		return distanceMeters;
	}

	public void setDistanceMeters(Double distanceMeters) {
		this.distanceMeters = distanceMeters;
	}
	
}
