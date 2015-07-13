package br.com.runplanner.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity 
public class LatLng implements BasicEntity {

	private static final long serialVersionUID = -6855025459588558272L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Basic
	private Double latitudeDegrees;
	
	@Basic
	private Double longitudeDegrees;
	
	@Basic
	private Double altitudeMeters;
	
	@ManyToOne
	private Route route;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getLongitudeDegrees() {
		return longitudeDegrees;
	}

	public void setLongitudeDegrees(Double longitudeDegrees) {
		this.longitudeDegrees = longitudeDegrees;
	}

	public Double getAltitudeMeters() {
		return altitudeMeters;
	}

	public void setAltitudeMeters(Double altitudeMeters) {
		this.altitudeMeters = altitudeMeters;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public Double getLatitudeDegrees() {
		return latitudeDegrees;
	}

	public void setLatitudeDegrees(Double latitudeDegrees) {
		this.latitudeDegrees = latitudeDegrees;
	}
	
	
}
