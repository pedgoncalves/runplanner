package br.com.runplanner.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity

public class MedicalData implements BasicEntity {
	
	private static final long serialVersionUID = 5440230333260033719L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String bloodType;	
	private double height;
	private String sportsHistory;
	private String healthHistory;
	private String objective;
	private String observation;
	
	/*@OneToOne(fetch = FetchType.LAZY)
	private Pessoa customer;*/

	
	@Override
	public boolean equals(Object obj) {
		if ( obj == null || !(obj instanceof MedicalData) ) return false;
		
		MedicalData medicalData = (MedicalData)obj;
		if ( medicalData.getId() == null ) return false;
		
		if ( medicalData.getId().equals(this.getId()) ) return true;
		else return false;
	}
	
	@Override
	public int hashCode() {	
		return super.hashCode();
	}
	
	//Get and Set
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public String getSportsHistory() {
		return sportsHistory;
	}
	public void setSportsHistory(String sportsHistory) {
		this.sportsHistory = sportsHistory;
	}
	public String getHealthHistory() {
		return healthHistory;
	}
	public void setHealthHistory(String healthHistory) {
		this.healthHistory = healthHistory;
	}
	public String getBloodType() {
		return bloodType;
	}
	public void setBloodType(String bloodType) {
		this.bloodType = bloodType;
	}
	public String getObjective() {
		return objective;
	}
	public void setObjective(String objective) {
		this.objective = objective;
	}
	public String getObservation() {
		return observation;
	}
	public void setObservation(String observation) {
		this.observation = observation;
	}
	/*public Pessoa getCustomer() {
		return customer;
	}
	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}*/
	
}
