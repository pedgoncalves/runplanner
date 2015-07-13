package br.com.runplanner.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@NamedQueries({	
	@NamedQuery(name = "Event.findByAdvice", query = "select o from Event o "
		+ "where (o.advice.id = :adviceId) and o.date >= current_date() order by o.date desc"),	
	@NamedQuery(name = "Event.countByAdvice", query = "select count(o) from Event o "
		+ "where (o.advice.id = :adviceId) and o.date >= current_date() order by o.date desc"),			
	@NamedQuery(name = "Event.findByAdviceInactive", query = "select o from Event o "
		+ "where (o.advice.id = :adviceId) and o.date <= current_date() order by o.date desc")
		})
public class Event implements BasicEntity {

	private static final long serialVersionUID = 6294950975443909897L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	    
    @Basic
	private String name;
    
    @Basic
	private String location;
    
    @Temporal(TemporalType.DATE)
	private Date date;
    
    @Temporal(TemporalType.DATE)
	private Date finalDate;
    
	@Temporal(value=TemporalType.TIME)
	private Date time;
    
    @Basic
    private String distance;

    @Basic
    private String description;
    
    @Basic
    private String website;
     
	@ManyToOne(fetch=FetchType.EAGER)
	private Advice advice;
    
	@ManyToMany(fetch = FetchType.LAZY, cascade={CascadeType.REMOVE,CascadeType.MERGE})
	private List<EventPessoaActivity> associations;
	
	@Basic
	private String value;
	
	/**
	 * Apenas para guardar o tempo total, quando tiver atividade associada
	 */
	@Transient
	private String totalTime;
	
	@Transient
	private Activity activity;
	
    //Get and Set
    
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public List<Pessoa> getCustomers() {
		
		List<Pessoa> customers = new ArrayList<Pessoa>();
		if ( associations!=null ) {
			for(EventPessoaActivity association:associations) {
				customers.add(association.getCustomer());
			}			
		}
		
		return customers;
	}

	public int getCustomersSize() {		
		if ( associations==null ) return 0;
		else return associations.size();
	}
	
	public void addAssociation(EventPessoaActivity association) {
		if ( associations==null ) {
			associations = new ArrayList<EventPessoaActivity>();			
		}
		associations.add(association);
	}
	
	public void removeAssociation(EventPessoaActivity association) {
		if ( associations==null ) {
			return;			
		}
		
		for ( int i=0; i<associations.size(); i++) {
			EventPessoaActivity a = associations.get(i);
			if ( a.getId().longValue()==association.getId().longValue() ) {
				associations.remove(i);
				break;
			}
		}
		
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getFinalDate() {
		return finalDate;
	}

	public void setFinalDate(Date finalDate) {
		this.finalDate = finalDate;
	}

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public List<EventPessoaActivity> getAssociations() {
		return associations;
	}
	
}
