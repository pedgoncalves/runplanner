package br.com.runplanner.domain;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
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
	@NamedQuery(name = "PaymentData.findByYear", query = "select o from PaymentData o "
		+ "where (o.advice.id=:adviceId) and (o.year = :year) "),
	@NamedQuery(name = "PaymentData.findByMonthYear", query = "select o from PaymentData o "
		+ "where (o.advice.id=:adviceId) and (o.month=:month) and (o.year = :year) ")
		})
public class PaymentData implements BasicEntity  {
	
	private static final long serialVersionUID = 3936093600619710673L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	private Advice advice;

	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	@Basic
	private String transactionCode;
	
	@Enumerated
	private PaymentType type;
	
	@Basic
	private String reference;
	
	@Basic	
	private String description;
	
	@Basic
	private double value;
	
	@Basic
	private int month;
	
	@Basic
	private int year;
	
	@Basic
	private String paymentUrl;
	
	@Basic
	private boolean validated = false;	
	
	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTransactionCode() {
		return transactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public PaymentType getType() {
		return type;
	}

	public void setType(PaymentType type) {
		this.type = type;
	}

	public String getObservation() {
		return description;
	}

	public void setObservation(String observation) {
		this.description = observation;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}
	
}
