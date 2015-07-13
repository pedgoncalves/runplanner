package br.com.runplanner.domain;

import java.util.Date;

import javax.persistence.Basic;
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
	@NamedQuery(name = "Comment.loadByActivityId", query = "select c from Comment c "
		+ "where c.activity.id = :activityId order by c.date, c.id"),
	@NamedQuery(name = "Comment.countByActivityId", query = "select count(*) from Comment c "
		+ "where c.activity.id = :activityId order by c.date, c.id"),
	@NamedQuery(name = "Comment.loadByPessoaId", query = "select c from Comment c "
		+ "where c.pessoa.id = :pessoaId order by c.date, c.id")	
	})				
public class Comment implements BasicEntity {

	private static final long serialVersionUID = 6872214641033901205L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Pessoa pessoa;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Activity activity;
	
	@Basic
	private String comment;
	
	@Temporal(TemporalType.DATE)
	private Date date;
	
	
	public Pessoa getPessoa() {
		return pessoa;
	}
	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	public Activity getActivity() {
		return activity;
	}
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
