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

import br.com.runplanner.util.Utils;

@Entity
public class ActivityLap implements BasicEntity {

	private static final long serialVersionUID = -8378074459767506922L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	@Basic
	private Double totalTimeSeconds;

	@Basic
	private Double distanceMeters;

	@Basic
	private Double maximunSpeed;

	@Basic
	private Double averageHeartRateBpm;

	@Basic
	private Double maximumHeartRateBpm;
	
	@Basic
	private Double calories;

	@ManyToOne
	private Activity activity;

	//Get and Set
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Double getTotalTimeSeconds() {
		return totalTimeSeconds;
	}

	public void setTotalTimeSeconds(Double totalTimeSeconds) {
		this.totalTimeSeconds = totalTimeSeconds;
	}

	public Double getDistanceMeters() {
		return distanceMeters;
	}

	public void setDistanceMeters(Double distanceMeters) {
		this.distanceMeters = distanceMeters;
	}

	public Double getMaximunSpeed() {
		return maximunSpeed;
	}

	public void setMaximunSpeed(Double maximunSpeed) {
		this.maximunSpeed = maximunSpeed;
	}

	public Double getAverageHeartRateBpm() {
		return averageHeartRateBpm;
	}

	public void setAverageHeartRateBpm(Double averageHeartRateBpm) {
		this.averageHeartRateBpm = averageHeartRateBpm;
	}

	public Double getMaximumHeartRateBpm() {
		return maximumHeartRateBpm;
	}

	public void setMaximumHeartRateBpm(Double maximumHeartRateBpm) {
		this.maximumHeartRateBpm = maximumHeartRateBpm;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public String getPace() {
		String tempo = getTotalTimeString();
		int first = tempo.indexOf(":");

		int hor = 0;
		int min = 0;
		int sec = 0;

		if ( tempo.length()>6 ) { //Possui hora 
			int second = tempo.indexOf(":", first+1);
			hor = Integer.parseInt( tempo.substring( 0,first) );
			min = Integer.parseInt( tempo.substring( first+1,second) );
			sec = Integer.parseInt( tempo.substring( tempo.length()-2 ) );
		}
		else if ( tempo.length()>3) { //Possui minutos
			min = Integer.parseInt( tempo.substring( 0,first) );
			sec = Integer.parseInt( tempo.substring( tempo.length()-2 ) );
		}

		double paceValue = (((hor*60*60) + (min*60) + sec)/getDistanceKm())/60;
		
		int resto = (int)(((double)paceValue - (int)paceValue)*100);
    	resto = (int)((resto*60)/100);
    	        	        	
    	return Utils.strzero((int)paceValue)+":"+Utils.strzero(resto);
	}

	public Double getDistanceKm() {
		if ( getDistanceMeters() == null ) return 0d;
		return getDistanceMeters()/1000;
	}

	public String getTotalTimeString() {
		return Utils.formataTempo( getTotalTimeSeconds().intValue() );
	}


	public Double getCalories() {
		if ( calories == null ) {
			calories = 0d;
		}
		return calories;
	}

	public void setCalories(Double calories) {
		this.calories = calories;
	}
	
	
}
