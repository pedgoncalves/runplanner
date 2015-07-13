package br.com.runplanner.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * Esta classe representa um template de planilha atemporal.
 * Ela possui N atividades que podem ser carregadas para as planilhas de alunos.
 */
@Entity
@NamedQueries({	
	@NamedQuery(name = "TimelessScheduleTemplate.findByAdvice", query = "select o from TimelessScheduleTemplate o "
		+ "where (o.advice.id = :adviceId)")
		})
public class TimelessScheduleTemplate implements BasicEntity {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Basic
	private String title;
	
	@Basic
	private String observations;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Advice advice;
	
	/**
	 * Lista das atividades do Template
	 */
	@OneToMany(mappedBy="timelessTemplate",fetch = FetchType.EAGER,cascade={CascadeType.ALL})
	@OrderBy("id asc")
	private List<TimelessScheduleWeek> weeks;
	
	public TimelessScheduleTemplate() {
		weeks = new ArrayList<TimelessScheduleWeek>();
	}	

	public Long getId() {
		return id;
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

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public List<TimelessScheduleWeek> getWeeks() {
		if(weeks == null)
			weeks = new ArrayList<TimelessScheduleWeek>();
		
		return weeks;
	}

	public void setWeeks(List<TimelessScheduleWeek> weeks) {
		this.weeks = weeks;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
