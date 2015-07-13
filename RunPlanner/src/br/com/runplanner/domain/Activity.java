package br.com.runplanner.domain;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.runplanner.util.Utils;
import br.com.runplanner.view.util.Base64;

/**
 * @author d333247
 *
 */
@Entity
@NamedQueries({	
	@NamedQuery(name = "Activity.countByAdvice", query = "select count(*) from Activity a "
		+ "where (a.student.advice.id = :adviceId)"),
	@NamedQuery(name = "Activity.findTracksById", query = "select a from ActivityTack a "
		+ "where (a.activity.id = :activityId) order by a.id asc"),
	@NamedQuery(name = "Activity.findByUserId", query = "select a from Activity a "
		+ "where (a.student.id = :userId) order by a.date desc"),
	@NamedQuery(name = "Activity.findByUserIdAsc", query = "select a from Activity a "
		+ "where (a.student.id = :userId) order by a.date asc"),
	@NamedQuery(name = "Activity.findByUserIdDateAsc", query = "select a from Activity a "
		+ "where (a.student.id = :userId) and (a.date>=:initialDate) and (a.date<=:finalDate) " 
		+ "order by a.date asc"),
	@NamedQuery(name = "Activity.findByEquipment", query = "select a from Activity a "
		+ "where a.equipment.id=:equipmentId"),			
		})
public class Activity implements BasicEntity {

	private static final long serialVersionUID = -4888480994909974118L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
		
	@ManyToOne(cascade= {})
	private Pessoa student;

	@Temporal(TemporalType.DATE)
	private Date date;
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy="activity",cascade={CascadeType.MERGE,CascadeType.REMOVE})
	@OrderBy("id ASC")
	private List<ActivityLap> laps;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="activity",cascade={CascadeType.MERGE,CascadeType.REMOVE})
	@OrderBy("id ASC")
	private List<ActivityTack> tracks;	
	
	@Basic
	private String observation;
	
	@ManyToMany(fetch=FetchType.LAZY)
	private List<Equipment> equipment;
	
	@Transient
	private Double totalDistance;

	
	//Get and Set
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ActivityLap> getLaps() {
		return laps;
	}

	public void setLaps(List<ActivityLap> laps) {
		this.laps = laps;
	}
	
	public Double getTotalDistance() {
		
		if ( totalDistance != null && totalDistance!=0 ) return totalDistance;
		
		if ( laps==null ) return 0d;
		
		Double result = new Double(0);
		for( ActivityLap l:laps ) {
			result += l.getDistanceKm();
		}
		return result;
	}
	
	public void setTotalDistance(Double distance) {
		totalDistance = distance;
	}
	
	public Double getTotalCalories() {
		if ( laps==null ) return 0d;
		
		Double result = new Double(0);
		for( ActivityLap l:laps ) {
			result += l.getCalories();
		}
		return result;
	}
		
	public List<ActivityTack> getTracks() {
		return tracks;
	}

	public void setTracks(List<ActivityTack> tracks) {
		this.tracks = tracks;
	}

	public String getTotalTime() {
		if ( laps==null ) return "00:00:00";
		
		Double result = new Double(0);
		for( ActivityLap l:laps ) {
			result += l.getTotalTimeSeconds();
		}		
		return Utils.formataTempo(result.intValue());
	}
	
	public double getTotalTimeInSeconds() {
		if ( laps==null ) return 0d;
		
		Double result = new Double(0);
		for( ActivityLap l:laps ) {
			result += l.getTotalTimeSeconds();
		}		
		return result;
	}
	
	public Double getTotalAverageHeartRateBpm() {
		if ( laps==null ) return 0d;
		
		Double result = new Double(0);
		for( ActivityLap l:laps ) {
			if ( l.getAverageHeartRateBpm()!=null ) {
				result += l.getAverageHeartRateBpm();
			}
		}		
		return result/laps.size();
	}
	
	public String getAveragePace() {
		double paceValue = getAveragePaceMinutes();
		
		int resto = (int)(((double)paceValue - (int)paceValue)*100);
    	resto = (int)((resto*60)/100);
    	        	        	
    	return Utils.strzero((int)paceValue)+":"+Utils.strzero(resto);
	}
	
	public double getAveragePaceMinutes() {
		String time = getTotalTime();
		
		int first = time.indexOf(":");

		int hor = 0;
		int min = 0;
		int sec = 0;

		if ( time.length()>6 ) { //Possui hora 
			int second = time.indexOf(":", first+1);
			hor = Integer.parseInt( time.substring( 0,first) );
			min = Integer.parseInt( time.substring( first+1,second) );
			sec = Integer.parseInt( time.substring( time.length()-2 ) );
		}
		else if ( time.length()>3) { //Possui minutos
			min = Integer.parseInt( time.substring( 0,first) );
			sec = Integer.parseInt( time.substring( time.length()-2 ) );
		}

		return (((hor*60*60) + (min*60) + sec)/getTotalDistance())/60;
	}
	
	public Pessoa getStudent() {
		return student;
	}

	public void setStudent(Pessoa student) {
		this.student = student;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}
	
	public int getLapsSize() {
		if( laps==null ) return 0;
		else return laps.size();
	}
	
	@SuppressWarnings("deprecation")
	public String getDayIcon() {
		String icon = "icon-calendar-";		
		int day = date.getDate();
		
		icon += day;
		icon += ".png";
		return icon;
	}

	public List<Equipment> getEquipment() {
		return equipment;
	}

	public void setEquipment(List<Equipment> equipment) {
		this.equipment = equipment;
	}

	public String getCodedId() {	
		
		if ( this.id!=null ) {
			String idString = this.id.toString();
			while(idString.length()<10) {
				idString = "0"+idString;
			}
			return Base64.encodeBytes( idString.getBytes() );
		}			
		else {
			return "0";
		}
	}

	public String getShortenUrl() {
		StringBuffer urlString = new StringBuffer();

		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		urlString.append( context.getRequestScheme() );
		urlString.append( "://" );
		urlString.append( context.getRequestServerName() );
		urlString.append( context.getRequestContextPath() );
		urlString.append( "/public/" );
		urlString.append( getCodedId() );
		
		return urlString.toString();
	}
	
	public String getSocialTitle() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		
		String result = "Completei uma corrida de " + df.format(getTotalDistance()) +" km em " + getTotalTime() + ". Veja!";
		result += " #"+getStudent().getAdvice().getName();
		
		return result;
	}
	
	
	/**
	 * Usado apenas para a parte de notificacao
	 */
	private String time = "00:00:00";
	/**
	 * Usado apenas para a parte de notificacao
	 */
	public void setTime(String time) {
		this.time = time;
	}
	/**
	 * Usado apenas para a parte de notificacao
	 */
	public String getTime() {
		return time;
	}
}
