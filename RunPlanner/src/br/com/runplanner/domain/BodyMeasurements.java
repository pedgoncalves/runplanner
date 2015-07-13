package br.com.runplanner.domain;

import java.util.Date;

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
		@NamedQuery(name = "BodyMeasurements.findByCustomerId", 
				query = "select o from BodyMeasurements o where (o.customer.id = :customerId) order by o.dtData desc"),	
		@NamedQuery(name = "BodyMeasurements.findByCustomerIdAsc", 
				query = "select o from BodyMeasurements o where (o.customer.id = :customerId) order by o.dtData asc"),
		@NamedQuery(name = "BodyMeasurements.findByCustomerIdIntervalAsc", 
				query = "select o from BodyMeasurements o where (o.customer.id = :customerId) " 
					+ "and (o.dtData>=:initialDate) and (o.dtData<=:finalDate)" 
					+ "order by o.dtData asc")
	})
public class BodyMeasurements implements BasicEntity {

	private static final long serialVersionUID = 4663332263637705549L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Temporal(TemporalType.DATE)
	private Date dtData;
	
	private double weight;
	private double vo2Max;
	private int heartRateRest;
	private int heartRateMax;
	private int threshold1;
	private int threshold2;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Pessoa customer;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDtData() {
		return dtData;
	}

	public void setDtData(Date dtData) {
		this.dtData = dtData;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getVo2Max() {
		return vo2Max;
	}

	public void setVo2Max(double vo2Max) {
		this.vo2Max = vo2Max;
	}

	public int getHeartRateRest() {
		return heartRateRest;
	}

	public void setHeartRateRest(int heartRateRest) {
		this.heartRateRest = heartRateRest;
	}

	public int getHeartRateMax() {
		return heartRateMax;
	}

	public void setHeartRateMax(int heartRateMax) {
		this.heartRateMax = heartRateMax;
	}

	public int getThreshold1() {
		return threshold1;
	}

	public void setThreshold1(int threshold1) {
		this.threshold1 = threshold1;
	}

	public int getThreshold2() {
		return threshold2;
	}

	public void setThreshold2(int threshold2) {
		this.threshold2 = threshold2;
	}

	public Pessoa getCustomer() {
		return customer;
	}

	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}
	
	
}
