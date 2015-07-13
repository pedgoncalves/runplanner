package br.com.runplanner.domain;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
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

@Entity
@NamedQueries({
	@NamedQuery(name = "Spreadsheet.countByAdvice", query = "select count(*) from Spreadsheet o "
		+ "where (o.student.advice.id = :adviceId)"),
	@NamedQuery(name = "Spreadsheet.findByCompetence", query = "select o from Spreadsheet o "
		+ "where (o.month = :month) and (o.year = :year) and (o.student.advice.id = :adviceId)"),
	@NamedQuery(name = "Spreadsheet.findByCompetenceOrdered", query = "select o from Spreadsheet o "
		+ "where (o.month = :month) and (o.year = :year) and (o.student.advice.id = :adviceId) " 
		+ "order by o.sent asc, o.student.nome"),
	@NamedQuery(name = "Spreadsheet.findByCompetenceTeam", query = "select o from Spreadsheet o "
			+ "where (o.month = :month) and (o.year = :year) and (o.student.advice.id = :adviceId) and (o.student.team.id = :teamId) " 
			+ "order by o.sent asc, o.student.nome"),
	@NamedQuery(name = "Spreadsheet.findByStudent", query = "select o from Spreadsheet o "
		+ "where (o.student.id = :studentId) order by o.year, o.month asc"),
	@NamedQuery(name = "Spreadsheet.findByStudentDesc", query = "select o from Spreadsheet o "
				+ "where (o.student.id = :studentId) order by o.year desc, o.month desc"),
	@NamedQuery(name = "Spreadsheet.findByCompetenceAdvice", query = "select o from Spreadsheet o "
		+ "where (o.month = :month) and (o.year = :year) and (o.student.advice.id = :adviceId)"),
	@NamedQuery(name = "Spreadsheet.findByStudentInterval", query = "select o from Spreadsheet o "
		+ "where (o.student.id = :studentId) "
		+ "and ( o.year > :initialYear or ( o.month >= :initialMonth and o.year = :initialYear ) ) "
		+ "and ( o.year < :finalYear or ( o.month <= :finalMonth and o.year = :finalYear) ) "
		+ "order by o.year, o.month asc")
		})
public class Spreadsheet implements BasicEntity {

	private static final long serialVersionUID = 3262181016080019926L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private String observations;
	
	/**
	 * Mes ao qual se aplica a Planilha
	 */
	@Basic
	private Integer month;
	
	/**
	 * Ano ao qual se aplica a Planilha
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
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Pessoa student;
	
	/**
	 * Indica se a planilha ja foi enviada por email
	 */
	@Basic
	private Boolean sent;	
	
	/**
	 * Lista das atividades do Template
	 */
	@OneToMany(fetch = FetchType.EAGER,cascade={CascadeType.REMOVE,CascadeType.PERSIST})
	private List<Schedule> schedules;
	
	//Informacoes sobre Ritmo
	private String easyPace;
	
	private String normalPace;
	
	private String moderatePace;
	
	private String mediumPace;
	
	private String strongPace;
	
	private String shootingPace;
	
	
	public Spreadsheet() {
		this.student = new Pessoa();
		this.schedules = new ArrayList<Schedule>();
		type = SpreadsheetType.MONTH;
	}

	//Gets and Sets
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
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

	public Pessoa getStudent() {
		return student;
	}

	public void setStudent(Pessoa student) {
		this.student = student;
	}

	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}
	
	public String getTextMonth() {
		return new DateFormatSymbols(new Locale("pt","BR")).getMonths()[this.month];
	}

	public String getEasyPace() {
		return easyPace;
	}

	public void setEasyPace(String easyPace) {
		this.easyPace = easyPace;
	}

	public String getNormalPace() {
		return normalPace;
	}

	public void setNormalPace(String normalPace) {
		this.normalPace = normalPace;
	}

	public String getModeratePace() {
		return moderatePace;
	}

	public void setModeratePace(String moderatePace) {
		this.moderatePace = moderatePace;
	}

	public String getMediumPace() {
		return mediumPace;
	}

	public void setMediumPace(String mediumPace) {
		this.mediumPace = mediumPace;
	}

	public String getStrongPace() {
		return strongPace;
	}

	public void setStrongPace(String strongPace) {
		this.strongPace = strongPace;
	}

	public String getShootingPace() {
		return shootingPace;
	}

	public void setShootingPace(String shootingPace) {
		this.shootingPace = shootingPace;
	}

	public Boolean getSent() {
		if ( sent==null ) {
			sent = false;
		}
		return sent;
	}

	public void setSent(Boolean sent) {
		this.sent = sent;
	}

	public Schedule getNextTraining(Date date) {
		
		Schedule result = null;
		List<Schedule> scheduleList = getSchedules();
		
		for(Schedule schedule:scheduleList) {
			
			if ( schedule.isSameDate(date) ) {
				result = schedule;
				break;
			}
			
			if ( schedule.getDate().after(date) ) {
				if ( result!=null ) {					
					if ( schedule.getDate().before(result.getDate()) ) {
						result = schedule;
					}
				}
				else {
					result = schedule;
				}
			}
		}	
		
		return result;
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
