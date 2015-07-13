package br.com.runplanner.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@NamedQueries({	
	@NamedQuery(name = "Notification.findByAdvice", query = "select o from Notification o "
		+ "where (o.advice.id = :adviceId) order by o.date desc, id desc "),
	@NamedQuery(name = "Notification.findByObject", query = "select o from Notification o "
		+ "where (o.objectId = :objectId) "),
	@NamedQuery(name = "Notification.findByUserObject", query = "select o from Notification o "
		+ "where (o.objectId = :objectId) and (o.customer.id = :userId) "),
	@NamedQuery(name = "Notification.findByUser", query = "select o from Notification o "
		+ "where (o.customer.id = :userId) order by o.date desc, id desc ")	
		})
public class Notification implements BasicEntity {
	
	private static final long serialVersionUID = 1329322707521296702L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Enumerated
	private NotificationType type;
		
	@Temporal(TemporalType.DATE)
	private Date date;
	
	@Basic
	private String title;
	
	@Basic
	private Long objectId;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Advice advice;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Pessoa customer;	
	
	@Transient
	private List<Comment> commentList;
	
	@Transient
	private Activity activity;
	
	@Transient
	private Event event;
	
	@Transient 
	private int commentNumber=0;
	
	//Get and Set
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date data) {
		this.date = data;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public Pessoa getCustomer() {
		return customer;
	}

	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Comment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<Comment> commentList) {
		this.commentList = commentList;
		if (commentList!=null) {
			commentNumber = commentList.size();
		}
	}

	public void setCommentNumber(int number) {
		this.commentNumber = number;
	}
	
    public int getCommentNumber() {
    	return commentNumber;
    }

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}
    
}
