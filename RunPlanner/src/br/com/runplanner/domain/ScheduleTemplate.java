package br.com.runplanner.domain;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * Esta classe representa um template de planilha mensal.
 * Ela possui N atividades que podem ser carregadas para as planilhas de alunos.
 */
@Entity
@NamedQueries({	
	@NamedQuery(name = "ScheduleTemplate.findByCompetence", query = "select o from ScheduleTemplate o "
		+ "where (o.month = :month) and (o.year = :year) and (o.advice.id = :adviceId)"),
	@NamedQuery(name = "ScheduleTemplate.findByAdvice", query = "select o from ScheduleTemplate o "
		+ "where (o.advice.id = :adviceId)"),
	@NamedQuery(name = "ScheduleTemplate.findByAdviceTitle", query = "select o from ScheduleTemplate o "
			+ "where (o.advice.id = :adviceId) and (o.title = :title)")
		})
public class ScheduleTemplate implements BasicEntity {

	private static final long serialVersionUID = 1089362368396195862L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Basic
	private String title;
	
	@Basic
	private String observations;
	
	/**
	 * Mes ao qual se aplica o Template
	 */
	@Basic
	private Integer month;
	
	/**
	 * Ano ao qual se aplica o Template
	 */
	@Basic
	private Integer year;
	
	/**
	 * Dia ao qual se aplica o Template, para o caso semanal
	 */
	@Basic
	private Integer day;
	
	/**
	 * Se e mensal ou semanal
	 */
	@Enumerated
	private SpreadsheetType type;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Advice advice;
	
	/**
	 * Lista das atividades do Template
	 */
	@OneToMany(fetch = FetchType.EAGER,cascade={CascadeType.REMOVE,CascadeType.MERGE})
	private List<Schedule> schedules;
	
	public ScheduleTemplate() {
		schedules = new ArrayList<Schedule>();
		month = Calendar.getInstance().get(Calendar.MONTH);
		year = Calendar.getInstance().get(Calendar.YEAR);
		type = SpreadsheetType.MONTH;
	}
			
	// -- Get and Set

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
	
	public String getTextMonth() {
		return new DateFormatSymbols(new Locale("pt","BR")).getMonths()[this.month];
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public SpreadsheetType getType() {
		if ( type==null ) return SpreadsheetType.MONTH;
		return type;
	}

	public void setType(SpreadsheetType type) {
		this.type = type;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

}
