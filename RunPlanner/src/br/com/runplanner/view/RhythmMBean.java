package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.RhythmTable;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.service.RhythmService;
import br.com.runplanner.view.util.Constants;

@Component("rhythmMBean")
@Scope("session")
public class RhythmMBean extends BasicMBean {

	private static final String RHYTHM_FORM_PAGE = "/pages/rhythm/rhythm";
	private static final String RHYTHM_LIST_PAGE = "/pages/rhythm/rhythmList";
	
	private RhythmService rhythmService;
	private PessoaService pessoaService;
	private ReportService reportService;
	
	private RhythmTable rhythm;
	private List<RhythmTable> rhythmList;
	private List<Pessoa> customerList;

	@Autowired
	public RhythmMBean(RhythmService rhythmService,
			PessoaService pessoaService,
			ReportService reportService) {
		this.rhythmService = rhythmService;
		this.pessoaService = pessoaService;
		this.reportService = reportService;
		
		this.rhythm = new RhythmTable();
		this.rhythmList = new ArrayList<RhythmTable>();
	}
	
	public String goToList() {
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		
		rhythmList = rhythmService.getByAdvice(advice.getId());
		customerList = new ArrayList<Pessoa>();
		
		setSelectedMenu(Constants.MENU_ADVICE);
		
		return RHYTHM_LIST_PAGE;
	}
	
	public String goToCreate() {
		rhythm = new RhythmTable();

		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		rhythm.setAdvice(advice);

		return RHYTHM_FORM_PAGE;
	}

	public String goToModify() {
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		
		rhythm = rhythmService.loadById(rhythm.getId());
		if ( rhythm==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		customerList = pessoaService.getByTipoPessoaAdviceClassification(TipoPessoa.ALUNO, advice.getId(), rhythm.getId());
		
		return RHYTHM_FORM_PAGE;
	}
	
	public void goToReport() {
		Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("ASSESSORIA_ID", loged.getAdvice().getId().intValue());
		
		if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {			
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			param.put(ReportServiceImpl.NOME_ARQUIVO, "Lista de ritimos "+loged.getAdvice().getName());
			usarLogoRunPlanner = false;
		} else {
			param.put(ReportServiceImpl.NOME_ARQUIVO, "Lista de ritimos RunPlanner.pfd");
			usarLogoRunPlanner = true;
		}
		
		if(!reportService.gerar(ReportServiceImpl.PLANILHA_LISTA_RITMOS_REPORT, param, null, usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}		
	}
	
	public String delete() {
		try {
			Pessoa loged = getSessionUser();
			Advice advice = loged.getAdvice();
			
			List<Pessoa> customers = pessoaService.getByTipoPessoaAdviceClassification(TipoPessoa.ALUNO, advice.getId(), rhythm.getId());
			if ( customers!=null && customers.size()>0 ) {
				addMessage(FacesMessage.SEVERITY_ERROR,"rhythm.delete.error.customer");
				return null;
			}
			
			rhythmService.deleteById(rhythm.getId());		
			addMessage(FacesMessage.SEVERITY_INFO,"rhythm.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
		} 
		
		rhythm = new RhythmTable();
		return goToList();
	}
	
	public String save() {
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		
		//Verificar se esta tudo zerado
		if ( rhythm.getEasyPace().equals("") 
				&& rhythm.getNormalPace().equals("")
				&& rhythm.getMediumPace().equals("")
				&& rhythm.getModeratePace().equals("")
				&& rhythm.getStrongPace().equals("")
				&& rhythm.getShootingPace().equals("")
				) {
			addMessage(FacesMessage.SEVERITY_ERROR,"rhythm.save.error.empty");
			return null;
		}
		
		RhythmTable existentRhythm = rhythmService.getByAdviceClassification(
				advice.getId(), rhythm.getClassification());
		
		if ( existentRhythm!=null && (
				rhythm.getId()==null || 
				!rhythm.getId().equals(existentRhythm.getId()) ) ) {
			
			addMessage(FacesMessage.SEVERITY_ERROR,"rhythm.save.error.exist");
			return null;
		}
		
		if ( rhythm.getId()==null ) {			
			rhythm = rhythmService.persist(rhythm); 
	        addMessage(FacesMessage.SEVERITY_INFO,"rhythm.save.sucess");           
		}
		else {
			try {
				rhythmService.update(rhythm);
		        addMessage(FacesMessage.SEVERITY_INFO,"rhythm.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return goToList();
			}  			
		}
		return goToList();
	}

	
	//Gets and Sets
	
	public RhythmService getRhythmService() {
		return rhythmService;
	}

	public void setRhythmService(RhythmService rhythmService) {
		this.rhythmService = rhythmService;
	}

	public RhythmTable getRhythm() {
		return rhythm;
	}

	public void setRhythm(RhythmTable rhythm) {
		this.rhythm = rhythm;
	}

	public List<RhythmTable> getRhythmList() {
		return rhythmList;
	}

	public void setRhythmList(List<RhythmTable> rhythmList) {
		this.rhythmList = rhythmList;
	}

	public List<Pessoa> getCustomerList() {
		return customerList;
	}

	public void setCustomerList(List<Pessoa> customerList) {
		this.customerList = customerList;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	
}
