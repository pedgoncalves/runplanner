package br.com.runplanner.domain;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({	
	@NamedQuery(name = "Mailbox.sumByInternalMessage", query = "select COUNT(o) from Mailbox o "
			+ "where (o.message.id = :msgId)"),
	@NamedQuery(name = "Mailbox.findByUserId", query = "select mb from Mailbox mb "
		+ "where (mb.receiver.id = :userId) order by messageRead, id desc"),
	@NamedQuery(name = "Mailbox.countNewByUserId", query = "select COUNT(mb) from Mailbox mb "
		+ "where (mb.receiver.id = :userId) and messageRead = false")})
public class Mailbox implements BasicEntity {

	private static final long serialVersionUID = 6294950975443909897L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date readDate;

	@Basic
	private boolean messageRead = false;

	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.PERSIST})
	private InternalMessage message;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Pessoa receiver;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public InternalMessage getMessage() {
		return message;
	}

	public void setMessage(InternalMessage message) {
		this.message = message;
	}

	public Pessoa getReceiver() {
		return receiver;
	}

	public void setReceiver(Pessoa receiver) {
		this.receiver = receiver;
	}

	public Date getReadDate() {
		return readDate;
	}

	public void setReadDate(Date readDate) {
		this.readDate = readDate;
	}

	public boolean isMessageRead() {
		return messageRead;
	}
	
	public boolean getMessageRead() {
		return messageRead;
	}

	public void setMessageRead(boolean messageRead) {
		this.messageRead = messageRead;
	}

}
