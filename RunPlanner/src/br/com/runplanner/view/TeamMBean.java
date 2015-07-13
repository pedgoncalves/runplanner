package br.com.runplanner.view;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Team;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.view.util.Constants;

@Component("teamMBean")
@Scope("session")
public class TeamMBean extends BasicMBean {

	private static final String TEAM_FORM_PAGE = "/pages/team/team";
	private static final String TEAM_LIST_PAGE = "/pages/team/teamList";
	
	private AdviceService adviceService;	
	private TeamService teamService;
	private PessoaService pessoaService;
	
	private Team team;
	private List<Team> teamList;
	
	private DualListModel<String> teachers;  

	@Autowired
	public TeamMBean(TeamService teamService, AdviceService adviceService, PessoaService pessoaService) {
		this.teamService = teamService;
		this.adviceService = adviceService;
		this.pessoaService = pessoaService;
		
		this.team = new Team();
		this.teamList = new ArrayList<Team>();
	}
	
	public String goToList() {
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		
		teamList = teamService.getByAdvice(advice.getId());
		
		setSelectedMenu(Constants.MENU_ADVICE);
		
		return TEAM_LIST_PAGE;
	}
	
	public String goToCreate() {
		team = new Team();
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		team.setAdvice(advice);
		
		List<Pessoa> teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.PROFESSOR, advice.getId());
		List<String> nameList = new ArrayList<String>(); 
		for(Pessoa teacher: teacherList) {
			nameList.add(teacher.getNome());
		}
		
		//Adicionar os Donos da assessoria
		teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ASSESSORIA, advice.getId());		 
		for(Pessoa teacher: teacherList) {
			nameList.add(teacher.getNome());
		}
		
		teachers = new DualListModel<String>(nameList,new ArrayList<String>());

		return TEAM_FORM_PAGE;
	}

	public String goToModify() {
		
		team = teamService.loadById(team.getId());
		
		if ( team==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		team.setAdvice(advice);
		
		List<Pessoa> teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.PROFESSOR, advice.getId());		
		List<String> nameList = new ArrayList<String>(); 
		for(Pessoa teacher: teacherList) {
			nameList.add(teacher.getNome());
		}
		
		//Adicionar os Donos da assessoria
		teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ASSESSORIA, advice.getId());		 
		for(Pessoa teacher: teacherList) {
			nameList.add(teacher.getNome());
		}
		
		List<Pessoa> target = team.getTeachers();		
		if ( target==null ) target = new ArrayList<Pessoa>();
		List<String> targetList = new ArrayList<String>();
		for (Pessoa pessoa:target) {	
			targetList.add(pessoa.getNome());
			nameList.remove(pessoa.getNome());
		}
		
		teachers = new DualListModel<String>(nameList,targetList);
		
		return TEAM_FORM_PAGE;
	}
	
	public String delete() {
		
		Team t = teamService.loadById(team.getId());
		if ( !t.getCustomers().isEmpty() ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"team.delete.error.customer");
			return null;
		}
		
		try {
			teamService.deleteById(team.getId());
			addMessage(FacesMessage.SEVERITY_INFO,"team.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");			
		} 
		
		team = new Team();
		return goToList();
	}
	
	public String save() {
		
		Advice advice = getSessionUser().getAdvice();
		
		List<Pessoa> teacherList = new ArrayList<Pessoa>();
		for (String name:teachers.getTarget()) {
			Pessoa teacher = pessoaService.getByNameTipoPessoaAdvice(name, TipoPessoa.PROFESSOR, advice.getId());			
			if ( teacher==null ) {
				teacher = pessoaService.getByNameTipoPessoaAdvice(name, TipoPessoa.ASSESSORIA, advice.getId());
			}
			teacherList.add(teacher);
		}			
		team.setTeachers(teacherList);
		
		if ( team.getId()==null ) {			
			team.setAdvice(advice);
			team = teamService.persist(team);
	        addMessage(FacesMessage.SEVERITY_INFO,"team.save.sucess");
		}
		else {
			
			try {
				teamService.update(team);
		        addMessage(FacesMessage.SEVERITY_INFO,"team.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return goToList();
			}  
		}
		return goToList();
	}
	
	//Gets and Sets
	
	public TeamService getTeamService() {
		return teamService;
	}

	public void setTeamService(TeamService teamService) {
		this.teamService = teamService;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public List<Team> getTeamList() {
		return teamList;
	}

	public void setTeamList(List<Team> teamList) {
		this.teamList = teamList;
	}

	public AdviceService getAdviceService() {
		return adviceService;
	}

	public void setAdviceService(AdviceService adviceService) {
		this.adviceService = adviceService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public DualListModel<String> getTeachers() {
		return teachers;
	}

	public void setTeachers(DualListModel<String> teachers) {
		this.teachers = teachers;
	}
	
	
}
