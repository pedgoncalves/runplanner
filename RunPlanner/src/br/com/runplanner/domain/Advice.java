package br.com.runplanner.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({	
	@NamedQuery(name = "Advice.loadAllActiveEager", query = "select a from Advice a "
		+ "where a.active = :active order by a.name"),
	@NamedQuery(name = "Advice.loadAllActiveEagerOrdenadoDiferente", query = "select a from Advice a "
			+ "where a.active = :active order by a.registerDate desc"),
	@NamedQuery(name = "Advice.loadByActiveName", query = "select a from Advice a "
			+ "where a.active = :active and a.name like :name order by a.name"),
	@NamedQuery(name = "Advice.loadByStatus", query = "select a from Advice a "
			+ "where a.status = :status order by a.name"),
	@NamedQuery(name = "Advice.loadAll", query = "select a from Advice a order by a.name")})
public class Advice implements BasicEntity {


	private static final long serialVersionUID = -8610441037167655986L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private String name;
	
	@Basic
	private String theme;
	
	@Basic
	private String cnpj;
	
	@Basic
	private Integer studendNumber;
	
	@Basic
	private Boolean active = true;
	
	@Temporal(value=TemporalType.DATE)
	private Date contractInit;
	
	@Temporal(value=TemporalType.DATE)
	private Date contractFinal;

	@OneToOne(fetch=FetchType.EAGER,cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Endereco adress;
	
	@Lob
	private byte[] logo;
	
	@Basic
	private String advicePhoto;
	
	@Basic
	private String adviceBanner;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="advice",cascade={CascadeType.REMOVE})
	private List<Pessoa> pessoas;
	
	/**
	 * Valor que a Assessoria paga ao Runplanner
	 */
	@Basic
	private Double value;
	
	/**
	 * Dia base de pagamentos da ASSESSORIA
	 */
	@Basic
	private Integer advicePaymentDay;
	
	/**
	 * Dia base de pagamentos dos ALUNOS
	 */
	@Basic
	private Integer customerPaymentDay;
	
	/**
	 * Valor da mensalidade que a Assessoria cobra
	 */
	@Basic
	private Double customerPaymentValue;
	
	@Temporal(value=TemporalType.DATE)
	private Date registerDate;
	
	@Temporal(value=TemporalType.DATE)
	private Date endTestsDate;
	
	@Enumerated
	private StatusAdvice status;
	
	@Basic
	private Boolean paymentNotificationDialog;

	@Basic
	private Boolean paymentNotificationMail;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="advice",cascade={CascadeType.REMOVE})
	private List<Partner> partners;
	
	
	public Advice() {
		adress = new Endereco();
	}
	
	//Gets and Sets
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public Integer getStudendNumber() {
		return studendNumber;
	}

	public void setStudendNumber(Integer studendNumber) {
		this.studendNumber = studendNumber;
	}

	public Date getContractInit() {
		return contractInit;
	}

	public void setContractInit(Date contractInit) {
		this.contractInit = contractInit;
	}

	public Date getContractFinal() {
		return contractFinal;
	}

	public void setContractFinal(Date contractFinal) {
		this.contractFinal = contractFinal;
	}

	public Endereco getAdress() {
		return adress;
	}

	public void setAdress(Endereco adress) {
		this.adress = adress;
	}
	
    public byte[] getLogo() {
    	
    	if ( hasAdvicePhoto() ) {
			File f = new File(advicePhoto);
			logo = new byte[(int)f.length()];
			
			FileInputStream in;
			try {
				in = new FileInputStream(f);
				in.read(logo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
    	
		return logo;
    }

    public void setLogo(byte[] logo) {
    	this.logo = logo;        
    }

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}
	
	public int getActiveUserNumber() {
		int result = 0;
		for( Pessoa p:pessoas ) {
			if ( p.isActive() && p.getTipoPessoa().equals(TipoPessoa.ALUNO) ) result++;
		}
		return result;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Integer getCustomerPaymentDay() {
		
		if ( customerPaymentDay == null ) {
			customerPaymentDay = 1;
		}
		
		return customerPaymentDay;
	}

	public void setCustomerPaymentDay(Integer customerPaymentDay) {
		this.customerPaymentDay = customerPaymentDay;
	}

	public Integer getAdvicePaymentDay() {
		return advicePaymentDay;
	}

	public void setAdvicePaymentDay(Integer advicePaymentDay) {
		this.advicePaymentDay = advicePaymentDay;
	}

	public Double getCustomerPaymentValue() {
		
		if ( customerPaymentValue == null ) {
			customerPaymentValue = 0d;
		}
		
		return customerPaymentValue;
	}

	public void setCustomerPaymentValue(Double customerPaymentValue) {
		this.customerPaymentValue = customerPaymentValue;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	

	public void setAdvicePhoto(String advicePhoto) {
		this.advicePhoto = advicePhoto;
	}


	public boolean hasAdvicePhoto() {
		return advicePhoto!=null && advicePhoto.trim().length()>0;
	}

	public Date getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}

	public Boolean getPaymentNotificationDialog() {
		if(paymentNotificationDialog==null) {
			paymentNotificationDialog=false;
		}
		return paymentNotificationDialog;
	}

	public void setPaymentNotificationDialog(Boolean paymentNotificationDialog) {
		this.paymentNotificationDialog = paymentNotificationDialog;
	}

	public Boolean getPaymentNotificationMail() {
		if(paymentNotificationMail==null) {
			paymentNotificationMail=false;
		}
		return paymentNotificationMail;
	}

	public void setPaymentNotificationMail(Boolean paymentNotificationMail) {
		this.paymentNotificationMail = paymentNotificationMail;
	}

	public Date getEndTestsDate() {
		return endTestsDate;
	}

	public void setEndTestsDate(Date endTestsDate) {
		this.endTestsDate = endTestsDate;
	}

	public StatusAdvice getStatus() {
		return status;
	}

	public void setStatus(StatusAdvice status) {
		this.status = status;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public List<Partner> getPartners() {
		return partners;
	}
	
	public List<Partner> getPartners(int n) {
		if ( partners==null ) return new ArrayList<Partner>();
		else if ( partners.size()<=n ) return partners;
		else {
			List<Partner> returnList = new ArrayList<Partner>(n);
			Set<Integer> selected = new LinkedHashSet<Integer>();
			Random r = new Random();

			while (returnList.size()<n) {
				int index = r.nextInt(partners.size());
				boolean novo = selected.add(index);
				
				if (novo) {
					returnList.add( partners.get(index) );
				}
			}
			
			return returnList;
		}
	}

	public void setPartners(List<Partner> partners) {
		this.partners = partners;
	}

	public String getAdviceBanner() {
		return adviceBanner;
	}

	public void setAdviceBanner(String adviceBanner) {
		this.adviceBanner = adviceBanner;
	}

}
