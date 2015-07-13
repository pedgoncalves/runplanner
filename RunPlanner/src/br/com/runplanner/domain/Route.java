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

@Entity
@NamedQueries({	
	@NamedQuery(name = "Route.findByAdvice", query = "select o from Route o "
		+ "where (o.advice.id = :adviceId) ")
		})
public class Route implements BasicEntity {

	private static final long serialVersionUID = -1811961262590354608L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private String name;
	
	@Basic 
	private String description;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Advice advice;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="route", cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
	private List<LatLng> positions;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public String getDescriptionText() {
		String intro = description;
		if ( intro!=null && intro.length()>70 ) {
			intro = intro.substring(0, 67);
			intro += "...";
		}
		
		return intro;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<LatLng> getPositions() {
		if (positions==null) {
			positions = new ArrayList<LatLng>();
		}
		return positions;
	}

	public void setPositions(List<LatLng> positions) {
		this.positions = positions;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}
	
}
