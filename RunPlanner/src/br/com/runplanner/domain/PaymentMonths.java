package br.com.runplanner.domain;

import java.util.List;

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
	@NamedQuery(name = "PaymentMonths.findByCustomerYear", query = "select o from PaymentMonths o "
		+ "where (o.customer.id = :customerId) and (o.year = :yearStr) "),
	@NamedQuery(name = "PaymentMonths.findByAdvice", query = "select o from PaymentMonths o "
		+ "where (o.customer.advice.id = :adviceId) and (o.year = :yearStr) ")	
		})
public class PaymentMonths implements BasicEntity {
	
	private static final long serialVersionUID = -6260239232959666640L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String year;
	private boolean jan;
	private boolean feb;
	private boolean mar;
	private boolean apr;
	private boolean may;
	private boolean jun;
	private boolean jul;
	private boolean aug;
	private boolean sep;
	private boolean oct;
	private boolean nov;
	private boolean dez;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Pessoa customer;
	
	@OneToMany(fetch=FetchType.EAGER,cascade={CascadeType.MERGE,CascadeType.REMOVE})
	private List<PaymentMonthsDetail> detailList;
	
	public boolean isPaid(int month) {
		
		boolean result=false;
		switch (month) {
			case 0:
				result = isJan();
			break;
			case 1:
				result = isFeb();
			break;
			case 2:
				result = isMar();
			break;
			case 3:
				result = isApr();
			break;
			case 4:
				result = isMay();
			break;
			case 5:
				result = isJun();
			break;
			case 6:
				result = isJul();
			break;
			case 7:
				result = isAug();
			break;
			case 8:
				result = isSep();
			break;
			case 9:
				result = isOct();
			break;
			case 10:
				result = isNov();
			break;
			case 11:
				result = isDez();
			break;

		}
		
		return result;
	}
	
	public void setPaid(int month, boolean paid) {
		
		switch (month) {
			case 0:
				setJan(paid);
			break;
			case 1:
				setFeb(paid);
			break;
			case 2:
				setMar(paid);
			break;
			case 3:
				setApr(paid);
			break;
			case 4:
				setMay(paid);
			break;
			case 5:
				setJun(paid);
			break;
			case 6:
				setJul(paid);
			break;
			case 7:
				setAug(paid);
			break;
			case 8:
				setSep(paid);
			break;
			case 9:
				setOct(paid);
			break;
			case 10:
				setNov(paid);
			break;
			case 11:
				setDez(paid);
			break;

		}
	}
	
	public boolean isJan() {
		return jan;
	}
	public void setJan(boolean jan) {
		this.jan = jan;
	}
	public boolean isFeb() {
		return feb;
	}
	public void setFeb(boolean feb) {
		this.feb = feb;
	}
	public boolean isMar() {
		return mar;
	}
	public void setMar(boolean mar) {
		this.mar = mar;
	}
	public boolean isApr() {
		return apr;
	}
	public void setApr(boolean apr) {
		this.apr = apr;
	}
	public boolean isMay() {
		return may;
	}
	public void setMay(boolean may) {
		this.may = may;
	}
	public boolean isJun() {
		return jun;
	}
	public void setJun(boolean jun) {
		this.jun = jun;
	}
	public boolean isJul() {
		return jul;
	}
	public void setJul(boolean jul) {
		this.jul = jul;
	}
	public boolean isAug() {
		return aug;
	}
	public void setAug(boolean aug) {
		this.aug = aug;
	}
	public boolean isSep() {
		return sep;
	}
	public void setSep(boolean sep) {
		this.sep = sep;
	}
	public boolean isOct() {
		return oct;
	}
	public void setOct(boolean oct) {
		this.oct = oct;
	}
	public boolean isNov() {
		return nov;
	}
	public void setNov(boolean nov) {
		this.nov = nov;
	}
	public boolean isDez() {
		return dez;
	}
	public void setDez(boolean dez) {
		this.dez = dez;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public Pessoa getCustomer() {
		return customer;
	}
	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}

	public List<PaymentMonthsDetail> getDetailList() {
		return detailList;
	}

	public void setDetailList(List<PaymentMonthsDetail> detailList) {
		this.detailList = detailList;
	}
	
}
