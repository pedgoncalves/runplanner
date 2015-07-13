package br.com.runplanner.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.primefaces.model.SelectableDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.StatusAdvice;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.RhythmService;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.util.mail.EmailThreadProductor;

@Component("managementInformationMBean")
@Scope("session")
public class ManagementInformationMBean extends BasicMBean {
	
	private static final String MANAGEMENT_INFO_INDEX = "/pages/advice/index";
	private AdviceService adviceService;
	private ActivityService activityService;
	private PessoaService pessoaService;
	private SpreadsheetService spreadsheetService;
	private RhythmService rhythmService;
	private TeamService teamService;
	private EmailThreadProductor emailThreadProductor;

	private ManagementInformationDataModel managementInformationListDataModel;
	private ManagementInformation[] selectedManagementInformationList;
	private Boolean activeAdvice;
	private StatusAdvice selectedStatus;
	
	private String subject = null;
	private String message = null;
	
	@Autowired
	public ManagementInformationMBean(AdviceService adviceService, 
			ActivityService activityService,
			PessoaService pessoaService,
			RhythmService rhythmService,
			TeamService teamService,
			SpreadsheetService spreadsheetService,
			EmailThreadProductor emailThreadProductor) {
		this.adviceService = adviceService;
		this.activityService = activityService;
		this.pessoaService = pessoaService;
		this.spreadsheetService = spreadsheetService;
		this.rhythmService = rhythmService;
		this.teamService = teamService;
		this.emailThreadProductor = emailThreadProductor;
	}

	@Override
	public String goToList() {
		List<ManagementInformation> managementInformationList = new ArrayList<ManagementInformation>();
		List<Advice> advices = adviceService.loadAllActiveEager(activeAdvice);
		
		for(Advice a:advices){
			if((selectedStatus == null) || a.getStatus().equals(selectedStatus)){
				ManagementInformation mi = new ManagementInformation();
				mi.setId(a.getId());
				mi.setAdvice(a.getName());
				mi.setQtdActivitByMonth(activityService.countByAdvice(a.getId()).intValue());
				mi.setQtdActiveRunners(pessoaService.getSumByAdviceActiveTipoPessoa(a.getId(), true, TipoPessoa.ALUNO).intValue());
				mi.setQtdSpreadSheetByMonth(spreadsheetService.countByAdvice(a.getId()).intValue());
				/*Hashtable<String, Object> lastLogin = lastLogin(a);
				mi.setLastLoginDate((lastLogin != null)?(Date)lastLogin.get("date"):null);
				mi.setLastLoginType((lastLogin != null)?(String)lastLogin.get("type"):"");*/
				mi.setStatus(a.getStatus() != null ? a.getStatus().getLabel():"");
				mi.setEndTestes(a.getEndTestsDate());
				mi.setRegisterDate(a.getRegisterDate());
				mi.setWizardComplete(wizardComplete(a)?"Sim":"Não");
			
				managementInformationList.add(mi);
			}
		}
		managementInformationListDataModel = new ManagementInformationDataModel(managementInformationList);
		return MANAGEMENT_INFO_INDEX;
	}
	
	public List<SelectItem> getListaStatus() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		result.add(new SelectItem(null, "Todos"));
		StatusAdvice[] status = StatusAdvice.values();
		for (int i = 0; i < status.length; i++) {
			result.add(new SelectItem(status[i],status[i].getLabel()));
		}
		return result;
	} 
	
	public String sendEmail() {
		if ( selectedManagementInformationList==null || selectedManagementInformationList.length<=0 ) {
    		addMessage(FacesMessage.SEVERITY_ERROR, "template.select");
    		return null;
    	}
    	
    	for (int i = 0; i < selectedManagementInformationList.length; i++) {    		
    		ManagementInformation mi = selectedManagementInformationList[i];
    		List<Pessoa> responsaveis = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ASSESSORIA, mi.getId());
    		
    		for(Pessoa p: responsaveis) {
    			emailThreadProductor.enviarMensagem(p.getEmail(), subject, message);
    		}
		}
    	
    	subject="";
    	message="";
    	selectedManagementInformationList=null;
        addMessage(FacesMessage.SEVERITY_INFO, "template.msg.email.sucess");
        
		return null;
	}
	
	private boolean wizardComplete(Advice a) {
		int teacherNumber=0;
		int teamsNumber=0;
		int rhythmNumber=0;
		
		teacherNumber = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.PROFESSOR, a.getId(), true).size();
		teacherNumber += pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ASSESSORIA, a.getId(), true).size();
		teamsNumber = teamService.getByAdvice(a.getId()).size();
		rhythmNumber = rhythmService.getByAdvice(a.getId()).size();
		
		if(teacherNumber > 0 && teamsNumber > 0 && rhythmNumber > 0) {
			return true;
		} else {		
			return false;
		}
	}
	
	private Hashtable<String, Object> lastLogin(Advice a){
		Hashtable<String, Object> result = null;
		int cont = 0;
		Date date = null;
		List<Pessoa> pessoas = a.getPessoas();
		if((pessoas != null) && (pessoas.size() > 0)) {
			
			while (date == null && cont < pessoas.size()) {
				date = pessoas.get(cont).getLastLoginDate();
				cont++;
			}
			
			if(date != null) {
				result = new Hashtable<String, Object>();
				result.put("date", pessoas.get(cont).getLastLoginDate());				
				result.put("type", pessoas.get(cont).getTipoPessoa().getLabel());
				
				for(int i=0;i<pessoas.size();i++){
					if(a.getActive() && pessoas.get(i).getLastLoginDate() != null 
							&& (date.compareTo(pessoas.get(i).getLastLoginDate())<0)){
						date = pessoas.get(i).getLastLoginDate();
						result.put("date", pessoas.get(i).getLastLoginDate());
						result.put("type", pessoas.get(i).getTipoPessoa().getLabel());
					}
				}
			}
		}
		return result;
	}

	@Override
	public String goToCreate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String goToModify() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String delete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String save() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class ManagementInformationDataModel extends
			ListDataModel<ManagementInformation> implements
			SelectableDataModel<ManagementInformation> {
		
		private List<ManagementInformation> managementInformationList;

		public ManagementInformationDataModel(List<ManagementInformation> managementInformationList) {
			super(managementInformationList);
			this.managementInformationList = managementInformationList;
		}
		
		public ManagementInformation getRowData(String arg0) {
			Long id = Long.parseLong(arg0);
			
			ManagementInformation selected = null;
			for(ManagementInformation mi: managementInformationList) {
				if ( mi.getId().longValue()==id.longValue() ) {
					selected = mi;
					break;
				}
			}
			
			return selected;       
		}

		public Object getRowKey(ManagementInformation managementInformation) {
			return managementInformation.getId();
		}

	}

	public class ManagementInformation {
		private Long id;
		private String advice;
		private String email;
		private int qtdActivitByMonth;
		private int qtdSpreadSheetByMonth;
		private int qtdActiveRunners;
		private Date lastLoginDate;
		private String lastLoginType;
		private String status;
		private Date endTestes;
		private Date registerDate;
		private String wizardComplete;
		
		public String getAdvice() {
			return advice;
		}
		public void setAdvice(String advice) {
			this.advice = advice;
		}
		public int getQtdActivitByMonth() {
			return qtdActivitByMonth;
		}
		public void setQtdActivitByMonth(int qtdActivitByMonth) {
			this.qtdActivitByMonth = qtdActivitByMonth;
		}
		public int getQtdSpreadSheetByMonth() {
			return qtdSpreadSheetByMonth;
		}
		public void setQtdSpreadSheetByMonth(int qtdSpreadSheetByMonth) {
			this.qtdSpreadSheetByMonth = qtdSpreadSheetByMonth;
		}
		public int getQtdActiveRunners() {
			return qtdActiveRunners;
		}
		public void setQtdActiveRunners(int qtdActiveRunners) {
			this.qtdActiveRunners = qtdActiveRunners;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getWizardComplete() {
			return wizardComplete;
		}
		public void setWizardComplete(String wizardComplete) {
			this.wizardComplete = wizardComplete;
		}
		public Date getLastLoginDate() {
			return lastLoginDate;
		}
		public void setLastLoginDate(Date lastLogin) {
			this.lastLoginDate = lastLogin;
		}
		public Date getEndTestes() {
			return endTestes;
		}
		public void setEndTestes(Date endTestes) {
			this.endTestes = endTestes;
		}
		public Date getRegisterDate() {
			return registerDate;
		}
		public void setRegisterDate(Date registerDate) {
			this.registerDate = registerDate;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getLastLoginType() {
			return lastLoginType;
		}
		public void setLastLoginType(String lastType) {
			this.lastLoginType = lastType;
		}

	}

	public Boolean getActiveAdvice() {
		return activeAdvice;
	}

	public void setActiveAdvice(Boolean activeAdvice) {
		this.activeAdvice = activeAdvice;
	}

	public void setSelectedManagementInformationList(
			ManagementInformation[] selectedManagementInformationList) {
		this.selectedManagementInformationList = selectedManagementInformationList;
	}
	
	public ManagementInformation[] getSelectedManagementInformationList() {
		return this.selectedManagementInformationList;
	}

	public ManagementInformationDataModel getManagementInformationListDataModel() {
		return managementInformationListDataModel;
	}

	public void setManagementInformationListDataModel(
			ManagementInformationDataModel managementInformationListDataModel) {
		this.managementInformationListDataModel = managementInformationListDataModel;
	}
	
	public StatusAdvice getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(StatusAdvice selectedStatus) {
		this.selectedStatus = selectedStatus;
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
}
