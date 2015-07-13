package br.com.runplanner.domain;

import java.util.List;

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
	@NamedQuery(name = "PaymentMonthsAdvice.findByAdvice", query = "select o from PaymentMonthsAdvice o "
		+ "where (o.advice.id = :adviceId) and (o.year = :yearStr) ")	
		})
public class PaymentMonthsAdvice implements BasicEntity {
	
	private static final long serialVersionUID = 8167790929493626723L;

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
	
	@ManyToOne
	private Advice advice;
	
	@OneToMany(fetch=FetchType.EAGER)
	private List<PaymentData> paymentDataList;
	
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

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public List<PaymentData> getPaymentDataList() {
		return paymentDataList;
	}

	public void setPaymentDataList(List<PaymentData> paymentDataList) {
		this.paymentDataList = paymentDataList;
	}
	
}
