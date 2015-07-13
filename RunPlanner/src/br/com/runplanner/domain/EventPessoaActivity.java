package br.com.runplanner.domain;

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
	@NamedQuery(name = "EventPessoaActivity.findByEventUser", query = "select o from EventPessoaActivity o "
		+ "where o.event.id=:eventId and o.customer.id=:customerId"),
	@NamedQuery(name = "EventPessoaActivity.findByEvent", query = "select o from EventPessoaActivity o "
		+ "where (o.event.id = :eventId)"),
	@NamedQuery(name = "EventPessoaActivity.findByActivity", query = "select o from EventPessoaActivity o "
		+ "where (o.activity.id = :activityId)")	
		})
public class EventPessoaActivity implements BasicEntity{

	private static final long serialVersionUID = -1762381192563639365L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	    
	@ManyToOne(fetch=FetchType.EAGER)
	private Pessoa customer;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Event event;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Activity activity;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Pessoa getCustomer() {
		return customer;
	}

	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
}
