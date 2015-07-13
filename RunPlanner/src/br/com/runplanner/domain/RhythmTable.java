package br.com.runplanner.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({	
	@NamedQuery(name = "RhythmTable.findByAdvice", query = "select o from RhythmTable o "
		+ "where (o.advice.id = :adviceId) "),
	@NamedQuery(name = "RhythmTable.findByCustomer", query = "select p.classification from Pessoa p "
			+ "where (p.id = :customerId) "),
	@NamedQuery(name = "RhythmTable.findByAdviceClassification", query = "select o from RhythmTable o "
		+ "where (o.advice.id = :adviceId) and (o.classification = :classification) ")
		})
public class RhythmTable implements BasicEntity {

	private static final long serialVersionUID = 8373720569270653932L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Advice advice;
	
	@Basic
	private String classification;
	
	//Informacoes sobre Ritmo
	@Basic
	private String easyPace;
	
	@Basic
	private String normalPace;
	
	@Basic
	private String moderatePace;
	
	@Basic
	private String mediumPace;
	
	@Basic
	private String strongPace;
	
	@Basic
	private String shootingPace;

	
	//Get and Set
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
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

}
