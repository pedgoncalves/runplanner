package br.com.runplanner.view;

import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Endereco;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.StatusAdvice;
import br.com.runplanner.domain.SystemRoles;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.MessagesResources;
import br.com.runplanner.view.util.PasswordUtil;

@Component("contactMBean")
@Scope("session")
public class ContactMBean {

	private EmailThreadProductor emailThreadProductor;
	
	private String name;
	private String email;
	private String subject;
	private String message;
	private String company;
	private String phone;
	
	private String notification;
	private AdviceService adviceService;
	private PessoaService pessoaService;

	@Autowired
	public ContactMBean(EmailThreadProductor emailThreadProductor,
			AdviceService adviceService,
			PessoaService pessoaService){
		this.emailThreadProductor = emailThreadProductor;
		this.adviceService = adviceService;
		this.pessoaService = pessoaService;
	}
	
	public void send() {
		
		message += "\n FROM: "+email;
		
		emailThreadProductor.enviarMensagem(Constants.RUNPLANNER_EMAIL, subject, message);
		
		clear();
		addMessage(FacesMessage.SEVERITY_INFO,"contact.send.sucess");
	}
	
	public String sendContactInit() {
		
		Pessoa jaCadastrado = pessoaService.loadByEmailActive(email,true);
		
		
		if(jaCadastrado == null) {
			subject = "Novo Contato pelo Site RunPlanner.com.br";
			
			message += "<br/><br/>------------------<br/><br/>";
			message += "Nome: "+name;
			message += "<br/> Email: "+email;
			message += "<br/> company: "+company;
			message += "<br/> Telefone: "+phone;
		
			
			emailThreadProductor.enviarMensagem(Constants.RUNPLANNER_EMAIL, subject, message);
			
			//Cadastrar assessoria
			Advice adviceOwner = new Advice();
			adviceOwner.setAdvicePaymentDay(5);
			adviceOwner.setContractInit(new Date());
			adviceOwner.setName(this.company);
			adviceOwner.setStudendNumber(10);
			adviceOwner.setValue(0D);
			adviceOwner.setRegisterDate(new Date());
			adviceOwner.setStatus(StatusAdvice.GRATUITO);
			
			adviceOwner = adviceService.persist(adviceOwner);
			
			//Dono da assessoria
			Pessoa owner = new Pessoa();
			owner.setNome(this.name);
			owner.setActive(true);
			owner.setDtEntrada(new Date());
			owner.setEmail(email);
			owner.setTipoPessoa(TipoPessoa.ASSESSORIA);
			owner.setRole(SystemRoles.ADVICE);
			
			Endereco tel = new Endereco();
			tel.setFoneComercial(phone);
			
			owner.setEndereco(tel);
			owner.setAdvice(adviceOwner);
			
			String generatedPass = PasswordUtil.gerarSenha();
			owner.setPassword( MD5Util.crypt(generatedPass) );
			
			owner = pessoaService.persist(owner);
	
			String messageBody = Constants.EMAIL_REGISTRATION_ADVICE;
			messageBody = messageBody.replace("$1", owner.getEmail());
			messageBody = messageBody.replace("$2", generatedPass);
			
			emailThreadProductor.enviarMensagem(owner.getEmail(), Constants.EMAIL_REGISTRATION_ADVICE_TITLE, messageBody);
			
			clear();
			
			return "/pages/registrationok";
		} else {
			addMessage(FacesMessage.SEVERITY_ERROR,"contact.duplicated.email");
			return null;
		}
	}
	
	public void clear() {
		name="";
		email="";
		subject="";
		message="";
		company="";
		phone="";
	}
	
    private void addMessage(Severity severity, String sumaryKey) { 
    	FacesMessage message = new FacesMessage(severity, 
    			MessagesResources.getStringFromBundle(sumaryKey,""),"");  
    	
        FacesContext.getCurrentInstance().addMessage(null, message);  
    }	
	
	//-- Get and Set
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

	public AdviceService getAdviceService() {
		return adviceService;
	}

	public void setAdviceService(AdviceService adviceService) {
		this.adviceService = adviceService;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

}
