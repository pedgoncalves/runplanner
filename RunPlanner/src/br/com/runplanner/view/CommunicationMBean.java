package br.com.runplanner.view;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Team;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;

@Component("communicationMBean")
@Scope("session")
public class CommunicationMBean extends BasicMBean {
	
	@Autowired
	private TeamService teamService;
	@Autowired
	private PessoaService pessoaService;
	@Autowired
	private EmailThreadProductor emailThreadProductor;
	@Autowired
	private AdviceService adviceService;

	private static final String COMMUNICATION_PAGE = "/pages/teacher/communication";
	private static final String COMMUNICATION_PAGE_ADM = "/pages/advice/communication";
	
	private List<Team> teamList;
	private List<Advice> adviceList;
	
	private Team team;
	private Advice adviceSelect;
	
	private String subject;
	private String message;
	
	public String goToList() {
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		
		teamList = teamService.getByAdvice(advice.getId());
		team = new Team();
		
		setSelectedMenu(Constants.MENU_CUSTOMER);
		return COMMUNICATION_PAGE;
	}
	
	public String goToListAdm() {
		
		adviceList = adviceService.loadAll();
		adviceSelect = new Advice();
		
		
		setSelectedMenu(Constants.MENU_COMMUNICATION);
		return COMMUNICATION_PAGE_ADM;
	}
	
	public void send() {
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		
		List<Pessoa> customerList = new ArrayList<Pessoa>();
		if ( team.getId().longValue()==-1 ) {
			customerList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ALUNO, advice.getId());
		} else {
			team = teamService.loadById(team.getId());
			customerList = team.getCustomers();
		}
		
		if( customerList==null || customerList.size()==0 ) {
			addMessage(FacesMessage.SEVERITY_WARN,"contact.advice.send.empty");
			return;
		}
		
		String recipient = "";
		for(Pessoa p:customerList) {
			recipient += ","+p.getEmail();
		}
		recipient = recipient.replaceFirst(",", "");		
		
		try {
			message = message.replaceAll("\n", "<br/>");
			message = message.replaceAll("\r", "<br/>");
			message = message.replaceAll("\t", "<br/>");
			
			
			emailThreadProductor.enviarMensagem(recipient, subject, message);
			
			team = new Team();
			subject="";
			message="";
			
			addMessage(FacesMessage.SEVERITY_INFO,"contact.advice.send.sucess");			
		}
		catch (Exception e) {
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_INFO,"contact.advice.send.error");
		}
		

	}
	
	public void sendAdm() {
				
		List<Pessoa> customerList = new ArrayList<Pessoa>();
		if ( adviceSelect.getId().longValue()==-1 ) {
			customerList = pessoaService.getByTipoPessoa(TipoPessoa.ASSESSORIA);
		} else {
			customerList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ASSESSORIA,adviceSelect.getId());
		}
		
		if( customerList==null || customerList.size()==0 ) return;
		
		String recipient = "";
		for(Pessoa p:customerList) {
			recipient += ","+p.getEmail();
		}
		recipient = recipient.replaceFirst(",", "");		
		
		try {
			message = message.replaceAll("\n", "<br/>");
			message = message.replaceAll("\r", "<br/>");
			message = message.replaceAll("\t", "<br/>");
			
			
			emailThreadProductor.enviarMensagem(recipient, subject, message);
			
			team = new Team();
			subject="";
			message="";
			
			addMessage(FacesMessage.SEVERITY_INFO,"contact.advice.send.sucess");			
		}
		catch (Exception e) {
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_INFO,"contact.advice.send.error");
		}
		

	}	
	
	public List<SelectItem> getTeamList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();		
		for (Team team:teamList) {
			result.add(new SelectItem(team.getId(),team.getPlace()));
		}
		return result;
	}
	
	public List<SelectItem> getAdviceList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();		
		for (Advice advice:adviceList) {
			result.add(new SelectItem(advice.getId(),advice.getName()));
		}
		return result;
	}
	
	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
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

	public Advice getAdviceSelect() {
		return adviceSelect;
	}

	public void setAdviceSelect(Advice adviceSelect) {
		this.adviceSelect = adviceSelect;
	}

	// ---
	@Override
	public String delete() {
		return null;
	}

	@Override
	public String goToCreate() {
		return null;
	}

	@Override
	public String goToModify() {
		return null;
	}

	@Override
	public String save() {
		return null;
	}
	
}
