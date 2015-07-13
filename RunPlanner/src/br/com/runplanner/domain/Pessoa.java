package br.com.runplanner.domain;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.imgscalr.Scalr;

/**
 * @author d333247
 *
 */
@Entity
@NamedQueries({	
	@NamedQuery(name = "Pessoa.findByTipo", query = "select o from Pessoa o "
		+ "where (o.tipoPessoa = :tipoPessoa) and (o.active = true) " +
				"order by o.nome"),
	@NamedQuery(name = "Pessoa.findByTipoAdvice", query = "select o from Pessoa o "
		+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.active = true) " +
				"order by o.nome"),
	@NamedQuery(name = "Pessoa.findByAdvice", query = "select o from Pessoa o "
			+ "where (o.advice.id = :adviceId) and (o.active = :active)  " +
					"order by o.nome"),
	@NamedQuery(name = "Pessoa.sumByAdviceActiveTipoPessoa", query = "select COUNT(o) from Pessoa o "
		+ "where (o.advice.id = :adviceId) and (o.active = :active) " +
				"and (o.tipoPessoa = :tipoPessoa) order by o.nome"),			
	@NamedQuery(name = "Pessoa.findByTipoAdviceActive", query = "select o from Pessoa o "
		+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.active = :active) " +
				"order by o.nome"),		
	@NamedQuery(name = "Pessoa.findByNameTipoAdviceActive", query = "select o from Pessoa o "
		+ "where (o.nome like :nome ) and (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.active = :active) " +
				"order by o.nome"),					
	@NamedQuery(name = "Pessoa.findByNameAdviceActive", query = "select o from Pessoa o "
		+ "where (o.nome like :nome ) and (o.advice.id = :adviceId) and (o.active = :active) " +
				"order by o.nome"),					
	@NamedQuery(name = "Pessoa.findByTipoAdviceActiveTeam", query = "select o from Pessoa o "
		+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.active = :active) and (o.team.id = :teamId) " +
				"order by o.nome"),		
	@NamedQuery(name = "Pessoa.findByNameTipoAdviceActiveTeam", query = "select o from Pessoa o "
		+ "where (o.nome like :nome ) and (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.active = :active) and (o.team.id = :teamId) " +
				"order by o.nome"),						
	@NamedQuery(name = "Pessoa.findByTipoAdviceAniversario", query = "select o from Pessoa o "
		+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (month(o.dtNascimento) = :month) " +
				"and (o.active = :active) order by day(o.dtNascimento)"),
	@NamedQuery(name = "Pessoa.findByTipoAdviceAniversarioDay", query = "select o from Pessoa o "
		+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (month(o.dtNascimento) = :month) and (day(o.dtNascimento) >= :day) " +
				"and (o.active = :active) order by day(o.dtNascimento)"),
	@NamedQuery(name = "Pessoa.findByNomeTipoAdvice", query = "select o from Pessoa o "
		+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.nome = :nome) " +
				"order by o.nome"),
	@NamedQuery(name = "Pessoa.findByBitNomeTipoAdvice", query = "select o from Pessoa o "
			+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.nome like :nome) " 
			+ "and (o.active = true) order by o.nome"),
	@NamedQuery(name = "Pessoa.getByTipoPessoaAdviceClassification", query = "select o from Pessoa o "
			+ "where (o.tipoPessoa = :tipoPessoa) and (o.advice.id = :adviceId) and (o.classification.id = :rhythmId) and (o.active = true) " 
			+ "order by o.nome"),	
	@NamedQuery(name = "Pessoa.findByEmailActive", query = "select o from Pessoa o "
			+ "where (o.email = :email) and (o.active = :active) " 
			+ "order by o.nome"),	
	@NamedQuery(name = "Pessoa.findByEmail", query = "select o from Pessoa o "
		+ "where (o.email = :email) " 
		+ "order by o.nome")			
		})
public class Pessoa implements BasicEntity {

	private static final long serialVersionUID = -3491162524294697452L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
    @Enumerated
	private TipoPessoa tipoPessoa;
    
    @Basic
	private String nome;

    @Basic
    private String shortName;
    
    @Basic
	private String email;
    
    @Enumerated
	private Sexo sexo;
    
    @Basic
	private String rg;
    
    @Basic
	private String cpf;
    
    @Basic
	private String ocupacao;
    
    @Basic
	private String plano;
    
    @Basic
	private String password;
    
    @Basic
	private Boolean active;
	    
    @Basic
	private String objetivoPessoal;
    
	@Temporal(TemporalType.DATE)
	private Date dtNascimento;
	
	@Temporal(TemporalType.DATE)
	private Date dtEntrada;
	
	@Temporal(TemporalType.DATE)
	private Date dtSaida;
	
	@OneToOne(fetch=FetchType.EAGER,cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private Endereco endereco;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private RhythmTable classification;
	
	@OneToOne(fetch=FetchType.LAZY, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private MedicalData medicalData;

	@OneToMany(mappedBy="customer", fetch=FetchType.LAZY, cascade={CascadeType.REMOVE,CascadeType.MERGE,CascadeType.PERSIST})
	private List<BodyMeasurements> bodyMeasurements;
	
	@OneToMany(mappedBy="customer", fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	private List<Presence> presences;
	
	@OneToMany(mappedBy="student", fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	private List<Spreadsheet> spreadsheet;
	
	@OneToMany(mappedBy="customer", fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	private List<PaymentMonths> payments;
	
	@Enumerated
	private SystemRoles role;
		
	@Lob
	private byte[] photo;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Advice advice;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Team team;
	
	@Transient
	private PaymentMonths paymentMonths;
	
	@Transient
	private WeekDays weekPresences = new WeekDays();
	
	/**
	 * Dia base de pagamentos dos ALUNOS
	 */
	@Basic
	private Integer customerPaymentDay;
	
	@Basic
	private Double paymentValue;
	
	@Basic
	private String userPhoto;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLoginDate;
	
	public Pessoa() {
		this.sexo = Sexo.MASCULINO;
		this.active = true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( obj == null || !(obj instanceof Pessoa) ) return false;
		
		Pessoa pessoa = (Pessoa)obj;
		if ( pessoa.getId() == null ) return false;
		
		if ( pessoa.getId().equals(this.getId()) ) return true;
		else return false;
	}
	
	@Override
	public int hashCode() {
		if( nome==null ) nome="";
		if( email==null ) email="";
		
		int hash = this.nome.hashCode() *7;
		hash += this.email.hashCode() *13;
		//hash += this.role.hashCode() * 17;
		return hash;
	}
	
	//Get and Set
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}
	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}
	public Sexo getSexo() {
		return sexo;
	}
	public void setSexo(Sexo sexo) {
		this.sexo = sexo;
	}
	public String getRg() {
		return rg;
	}
	public void setRg(String rg) {
		this.rg = rg;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getOcupacao() {
		return ocupacao;
	}
	public void setOcupacao(String ocupacao) {
		this.ocupacao = ocupacao;
	}
	public String getPlano() {
		return plano;
	}
	public void setPlano(String plano) {
		this.plano = plano;
	}
	public Date getDtNascimento() {
		return dtNascimento;
	}
	public void setDtNascimento(Date dtNascimento) {
		this.dtNascimento = dtNascimento;
	}
	public Date getDtEntrada() {
		return dtEntrada;
	}
	public void setDtEntrada(Date dtEntrada) {
		this.dtEntrada = dtEntrada;
	}
	public Date getDtSaida() {
		return dtSaida;
	}
	public void setDtSaida(Date dtSaida) {
		this.dtSaida = dtSaida;
	}

	public Integer getIdade(){
		
		if (this.getDtNascimento() == null ){
			return 0;
		}
				
		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.setTime(this.getDtNascimento());

		Calendar today = Calendar.getInstance();

		Integer idade = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
		dateOfBirth.add(Calendar.YEAR, idade);
		
		if (today.before(dateOfBirth)) {
		idade--;
		}

		return idade; 
	}
	
	public Endereco getEndereco() {
		//if ( endereco ==null ) return new Endereco();
		return endereco;
	}
	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public byte[] getPhoto() {
		
		if ( hasUserPhoto() ) {
			File f = new File(userPhoto);
			photo = new byte[(int)f.length()];
			
			FileInputStream in;
			try {
				in = new FileInputStream(f);
				in.read(photo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return photo;
	}
	
	public byte[] getThumb() {
		
		if ( hasUserPhoto() ) {
			
			String extencao = userPhoto.substring( userPhoto.indexOf(".") );
			String name =  userPhoto.substring( 0, userPhoto.indexOf(".") );
			String thumbFileName = name+"_thumb"+extencao;
			
			File f = new File(thumbFileName);
			photo = new byte[(int)f.length()];
			
			FileInputStream in;
			try {
				in = new FileInputStream(f);
				in.read(photo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public boolean getActive() {
		return active;
	}

	public SystemRoles getRole() {
		return role;
	}

	public void setRole(SystemRoles role) {
		this.role = role;
	}
	
	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public List<BodyMeasurements> getBodyMeasurements() {
		return bodyMeasurements;
	}

	public void setBodyMeasurements(List<BodyMeasurements> bodyMeasurements) {
		this.bodyMeasurements = bodyMeasurements;
	}

	public MedicalData getMedicalData() {
		return medicalData;
	}

	public void setMedicalData(MedicalData medicalData) {
		this.medicalData = medicalData;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public PaymentMonths getPaymentMonths() {
		return paymentMonths;
	}

	public void setPaymentMonths(PaymentMonths paymentMonths) {
		this.paymentMonths = paymentMonths;
	}

	public List<Spreadsheet> getSpreadsheet() {
		return spreadsheet;
	}

	public void setSpreadsheet(List<Spreadsheet> spreadsheet) {
		this.spreadsheet = spreadsheet;
	}

	public List<PaymentMonths> getPayments() {
		return payments;
	}

	public void setPayments(List<PaymentMonths> payments) {
		this.payments = payments;
	}

	public List<Presence> getPresences() {
		return presences;
	}

	public void setPresences(List<Presence> presences) {
		this.presences = presences;
	}

	public WeekDays getWeekPresences() {
		return weekPresences;
	}

	public void setWeekPresences(WeekDays weekPresences) {
		this.weekPresences = weekPresences;
	}
	public String getObjetivoPessoal() {
		return objetivoPessoal;
	}

	public void setObjetivoPessoal(String objetivoPessoal) {
		this.objetivoPessoal = objetivoPessoal;
	}

	public RhythmTable getClassification() {
		return classification;
	}

	public void setClassification(RhythmTable classification) {
		this.classification = classification;
	}

	public String getShortName() {
		String result = "";
		if(shortName != null && !shortName.equals("")){
			result = shortName;
		} else {			
			String[] names = nome.split(" ");
			
			if ( names!=null && names.length>2 ) {
				result = names[0];
				result += " "+names[names.length-1];
			}
			else {
				result = nome;
			}
		}
		
		return result;
	}

	public Integer getCustomerPaymentDay() {
		
		if ( customerPaymentDay==null ) {
			customerPaymentDay = advice.getCustomerPaymentDay();
		}
		
		return customerPaymentDay;
	}

	public void setCustomerPaymentDay(Integer customerPaymentDay) {
		this.customerPaymentDay = customerPaymentDay;
	}

	public Double getPaymentValue() {
		
		if ( paymentValue==null ) {
			paymentValue = advice.getCustomerPaymentValue();
		}
		
		return paymentValue;
	}

	public void setPaymentValue(Double paymentValue) {
		this.paymentValue = paymentValue;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public void setUserPhoto(String userPhoto) {
		this.userPhoto = userPhoto;
		
		//Salvar Thumb
		if ( userPhoto != null  ) {
			File f = new File(userPhoto);
			photo = new byte[(int)f.length()];
			
			FileInputStream in;
			try {
				in = new FileInputStream(f);
				in.read(photo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if ( photo != null ) {
				InputStream bIn = new ByteArrayInputStream(photo);
				BufferedImage img;
				try {
					img = ImageIO.read(bIn);
					img = Scalr.resize(img, Scalr.Mode.FIT_TO_WIDTH, 45, Scalr.THRESHOLD_QUALITY_BALANCED);
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write( img, "jpg", baos );
					baos.flush();
					photo = baos.toByteArray();
					baos.close();
					
					String extencao = userPhoto.substring( userPhoto.indexOf(".") );
					String name =  userPhoto.substring( 0, userPhoto.indexOf(".") );
					String thumbFileName = name+"_thumb"+extencao;
					
					
					File thumbFile = new File(thumbFileName);
					if (thumbFile.exists()) {
						thumbFile.delete();
						thumbFile.createNewFile();
					}
					
					FileOutputStream out = new FileOutputStream(thumbFile);
					out.write(photo);
					
					out.flush();
					out.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
		
	}


	public boolean hasUserPhoto() {
		return userPhoto!=null && userPhoto.trim().length()>0;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getUserPhoto() {
		return userPhoto;
	}
	
	
}
