package br.com.runplanner.domain;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class InternalMessage implements BasicEntity {

	private static final long serialVersionUID = 6294950975443909897L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private String subject;
	
	@Basic
	private String bodyMessage;
    
    @Temporal(TemporalType.TIMESTAMP)
	private Date date;
    
    @Temporal(TemporalType.TIME)
	private Date hour;
    
    @OneToOne(fetch=FetchType.EAGER)
    private Pessoa sender;
    	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBodyMessage() {
		return bodyMessage;
	}

	public void setBodyMessage(String bodyMessage) {
		this.bodyMessage = bodyMessage;
	}

	public Pessoa getSender() {
		return sender;
	}

	public void setSender(Pessoa sender) {
		this.sender = sender;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getHour() {
		return hour;
	}

	public void setHour(Date hour) {
		this.hour = hour;
	}

}
