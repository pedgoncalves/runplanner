package br.com.runplanner.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Esta classe representa uma semana na planilha atemporal.
 * Ela possui N atividades que podem ser carregadas para as planilhas de alunos.
 */
@Entity
public class TimelessScheduleWeek implements BasicEntity {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	private TimelessScheduleTemplate timelessTemplate;
	
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Schedule firstDay;
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Schedule secondDay;
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Schedule thirdDay;
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Schedule fourthDay;
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Schedule fifthDay;
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Schedule sixthDay;
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Schedule seventhDay;
	
	public TimelessScheduleWeek() {
		firstDay = new Schedule();
		secondDay = new Schedule();
		thirdDay  = new Schedule();
		fourthDay = new Schedule();
		fifthDay = new Schedule();
		sixthDay = new Schedule();
		seventhDay = new Schedule();		
	}
	
	public void setId(Long id){
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public TimelessScheduleTemplate getTimelessTemplate() {
		return timelessTemplate;
	}

	public void setTimelessTemplate(TimelessScheduleTemplate timelessTemplate) {
		this.timelessTemplate = timelessTemplate;
	}
	
	public TimelessScheduleWeek copy() {
		TimelessScheduleWeek copy = new TimelessScheduleWeek();
		copy.setId(this.getId());
		copy.setTimelessTemplate(this.getTimelessTemplate());
		
		copy.setFirstDay(this.getFirstDay().copy());
		copy.setSecondDay(this.getSecondDay().copy());
		copy.setThirdDay(this.getThirdDay().copy());
		copy.setFourthDay(this.getFourthDay().copy());
		copy.setFifthDay(this.getFifthDay().copy());
		copy.setSixthDay(this.getSixthDay().copy());
		copy.setSeventhDay(this.getSeventhDay().copy());
		
		return copy;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( obj == null || !(obj instanceof TimelessScheduleWeek) ) return false;
		
		TimelessScheduleWeek temp = (TimelessScheduleWeek)obj;
		
		if ( (temp.getId()==null && this.getId()!=null)
				|| (temp.getId()!=null && this.getId()==null) ) {
			return false;
		}
		
		boolean equalsId = false;
		if ( (temp.getId()==null && this.getId()==null) 
				|| (temp.getId().equals(this.getId())) ) {
			equalsId = true;
		}
		
		if ( temp.getFirstDay().equals(this.getFirstDay()) && 
				temp.getSecondDay().equals(this.getSecondDay()) &&
				temp.getThirdDay().equals(this.getThirdDay()) &&
				temp.getFourthDay().equals(this.getFourthDay()) &&
				temp.getFifthDay().equals(this.getFifthDay()) &&
				temp.getSixthDay().equals(this.getSixthDay()) &&
				temp.getSeventhDay().equals(this.getSeventhDay())
				&& equalsId) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		
		return firstDay.hashCode() + secondDay.hashCode() + 
				thirdDay.hashCode() + fourthDay.hashCode() +
				fifthDay.hashCode() + sixthDay.hashCode() +
				seventhDay.hashCode();
	}

	public Schedule getFirstDay() {
		firstDay = firstDay!=null?firstDay:new Schedule();
		return firstDay;
	}

	public void setFirstDay(Schedule firstDay) {
		this.firstDay = firstDay;
	}

	public Schedule getSecondDay() {
		secondDay = secondDay!=null?secondDay:new Schedule();
		return secondDay;
	}

	public void setSecondDay(Schedule secondDay) {
		this.secondDay = secondDay;
	}

	public Schedule getThirdDay() {
		thirdDay = thirdDay!=null?thirdDay:new Schedule();
		return thirdDay;
	}

	public void setThirdDay(Schedule thirdDay) {
		this.thirdDay = thirdDay;
	}

	public Schedule getFourthDay() {
		fourthDay = fourthDay!=null?fourthDay:new Schedule();
		return fourthDay;
	}

	public void setFourthDay(Schedule fourthDay) {
		this.fourthDay = fourthDay;
	}

	public Schedule getFifthDay() {
		fifthDay = fifthDay!=null?fifthDay:new Schedule();
		return fifthDay;
	}

	public void setFifthDay(Schedule fifthDay) {
		this.fifthDay = fifthDay;
	}

	public Schedule getSixthDay() {
		sixthDay = sixthDay!=null?sixthDay:new Schedule();
		return sixthDay;
	}

	public void setSixthDay(Schedule sixthDay) {
		this.sixthDay = sixthDay;
	}

	public Schedule getSeventhDay() {
		seventhDay = seventhDay!=null?seventhDay:new Schedule();
		return seventhDay;
	}

	public void setSeventhDay(Schedule seventhDay) {
		this.seventhDay = seventhDay;
	}

}
