package br.com.runplanner.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

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

@Entity
@NamedQueries({			
	@NamedQuery(name = "Presence.findByAlunoMes", query = "select o from Presence o "
		+ "where (o.customer.id = :aluno) and (month(o.dtPresence) = :month) " +
				"order by o.dtPresence"),
	@NamedQuery(name = "Presence.findByAlunoDias", query = "select o from Presence o "
		+ "where (o.customer.id = :aluno) and o.dtPresence between :date1 and :date2) " +
			"order by o.dtPresence"),
	@NamedQuery(name = "Presence.findByAlunoDia", query = "select o from Presence o "
		+ "where (o.customer.id = :aluno) and o.dtPresence = :date1) " +
		"order by o.dtPresence")
		})
public class Presence implements BasicEntity {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Temporal(TemporalType.DATE)
	private Date dtPresence;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Pessoa customer;
	
	@Enumerated
	private TipoPresenca tipoPresenca;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDtPresence() {
		return dtPresence;
	}

	public void setDtPresence(Date dtPresence) {
		this.dtPresence = dtPresence;
	}

	public TipoPresenca getTipoPresenca() {
		return tipoPresenca;
	}

	public void setTipoPresenca(TipoPresenca tipoPresenca) {
		this.tipoPresenca = tipoPresenca;
	}

	public Pessoa getCustomer() {
		return customer;
	}

	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}

	public boolean equals(Object o) {
		if(o == null){
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Presence aux = (Presence)o;
		
		return ( this.getCustomer().getId().equals(aux.getCustomer().getId()) && sdf.format(this.getDtPresence()).equals(sdf.format(aux.getDtPresence())) );
	}

}
