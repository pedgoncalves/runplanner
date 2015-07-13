package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.EventPessoaActivity;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.EventPessoaActivityService;
import br.com.runplanner.service.EventService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.view.util.Constants;

@Component("eventMBean")
@Scope("session")
public class EventMBean extends BasicMBean {

	private static final String EVENT_FORM_PAGE = "/pages/event/event";
	private static final String EVENT_LIST_PAGE = "/pages/event/eventList";
	private static final String EVENT_CUSTOMER_EVENT_PAGE = "/pages/event/addCustomerToEvent";
	
	private static final String EVENT_STUDENT_FORM_PAGE = "/pages/student/event";
	private static final String EVENT_STUDENT_LIST_PAGE = "/pages/student/eventList";
	
	private EventService eventService;
	private EventPessoaActivityService eventPessoaActivityService;
	private ReportService reportService;
	private PessoaService pessoaService;
	
	private Event event;
	private List<Event> eventList;	  
	private boolean findActive;
	
	private Pessoa selectedCustomer = new Pessoa();
	private Pessoa deleteCustomer = new Pessoa();
	private List<Pessoa> alunosInscritos;
	private List<Event> userEvents ;
	private List<EventPessoaActivity> eventPessoaList;
	

	@Autowired
	public EventMBean(EventService teamService, 
			ReportService reportService,
			EventPessoaActivityService eventPessoaActivityService,
			PessoaService pessoaService) {
		this.eventService = teamService;
		this.reportService = reportService;
		this.eventPessoaActivityService = eventPessoaActivityService;
		this.pessoaService = pessoaService;
		
		this.event = new Event();
		this.eventList = new ArrayList<Event>();
	}
	
	public String goToList() {		
		findActive=true;
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		
		eventList = eventService.getByAdvice(advice.getId());
		
		setSelectedMenu(Constants.MENU_EVENT);
		
		return EVENT_LIST_PAGE;
	}
	
	public String find() {		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		
		if ( findActive ) {
			eventList = eventService.getByAdvice(advice.getId());
		}
		else {
			eventList = eventService.getByAdviceInactive(advice.getId());
		}
		
		return EVENT_LIST_PAGE;
	}
	
	

	public String goToStudentList() {		
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		
	
		//eventList = eventService.getByAdvice(advice.getId());
		eventList = eventService.loadByAdviceIdEager(advice.getId());
		
		//Buscar eventos antigos		
		userEvents = eventService.getByUserInactive(advice.getId(),user.getId()); 
		
		for(Event e:userEvents) {
			EventPessoaActivity epa = eventPessoaActivityService.findByEventUserFull(e.getId(), user.getId());
			
			if ( epa!=null && epa.getActivity()!=null ) {				
				e.setTotalTime( epa.getActivity().getTotalTime() );
				e.setActivity(epa.getActivity());
			}
			else {
				e.setTotalTime("");
			}
		}
		
		setSelectedMenu(Constants.MENU_EVENT);
		
		return EVENT_STUDENT_LIST_PAGE;
	}
	
	public String goToCustomerEvent() {		
				
		event = eventService.loadByIdEager(event.getId());
		selectedCustomer=new Pessoa();
		deleteCustomer = new Pessoa();
		
		return EVENT_CUSTOMER_EVENT_PAGE;
	}	
	
	public List<Pessoa> completeCustomer(String value) {
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		
		List<Pessoa> result = pessoaService.getByBitNameTipoPessoaAdvice(value, TipoPessoa.ALUNO, advice.getId());
		//TODO verificar se ja nao esta inscrito ? Avaliar custoxbeneficio
		
		return result;
	}
	
	public String goToCreate() {
		event = new Event();
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		event.setAdvice(advice);
		
		return EVENT_FORM_PAGE;
	}

	public String goToModify() {
		
		event = eventService.loadByIdEager(event.getId());
		
		if ( event==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		eventPessoaList = eventPessoaActivityService.findByEventFull(event.getId());	
		setSelectedMenu(Constants.MENU_EVENT);
				
		return EVENT_FORM_PAGE;
	}
	
	@SuppressWarnings("deprecation")
	public String goToView() {
		
		event = eventService.loadByIdEager(event.getId());
		
		if ( event==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		eventPessoaList = eventPessoaActivityService.findByEventFull(event.getId());	
		
		Pessoa user = getSessionUser();
		
		if ( user.getTipoPessoa().getId() == TipoPessoa.ALUNO.getId() ) {
			Date now = new Date();
			now.setHours(01);
			now.setMinutes(00);		
			Date eventFinalDate = new Date(event.getFinalDate().getTime());
			if ( eventFinalDate!=null ) {
				eventFinalDate.setHours(23);
				eventFinalDate.setMinutes(59);
				
				if ( eventFinalDate.before(now)) {
					addMessage(FacesMessage.SEVERITY_WARN,"event.msg.inscription.late");
				}
			}
		}
		
		setSelectedMenu(Constants.MENU_EVENT);
				
		return EVENT_STUDENT_FORM_PAGE;
	}
	
	public void goToReport() {
		Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;
		
		HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("EVENTO_ID", event.getId().intValue());
    	param.put(ReportServiceImpl.NOME_ARQUIVO, "Lista de inscritos no evento "+event.getName());
    	
		if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}
		
    	if(!reportService.gerar(ReportServiceImpl.PLANILHA_INSCRICAO_EVENTO_REPORT, param, null, usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}
	}
	
	public String delete() {
				
		try {
			eventService.deleteById(event.getId());
			addMessage(FacesMessage.SEVERITY_INFO,"event.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");			
		} 
		
		event = new Event();
		return goToList();
	}
	
	public String addCustomerEvento() {
		try {
			event = eventService.addParticipant(event, selectedCustomer);
			
			selectedCustomer=new Pessoa();
			addMessage(FacesMessage.SEVERITY_INFO,"event.inscription.advice.sucess");
			
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");
		}

		return goToCustomerEvent();
	}
	
	public String removeCustomerEvento() {
		try {
			event = eventService.removeParticipant(event, deleteCustomer);
			
			selectedCustomer=new Pessoa();
			addMessage(FacesMessage.SEVERITY_INFO,"event.subinscription.advice.sucess");
			
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");
		}
		
		return goToCustomerEvent();
	}
	
	public String addParticipant() { 
		
		if (!getCanParticipate() ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "event.msg.participant.already");
			return goToView();
		}
		
		
		Pessoa customer = getSessionUser();
		event = eventService.loadByIdEager(event.getId());	
		
		if ( event==null ) {			
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
		}
		else {
			try {
				event = eventService.addParticipant(event, customer);				
				addMessage(FacesMessage.SEVERITY_INFO,"event.inscription.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");
			}
		}
		return goToView();
	}
	
	public String removeParticipant() {
		
		Pessoa customer = getSessionUser();
		event = eventService.loadByIdEager(event.getId());	
		if ( event==null ) {			
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
		}
		else {
			try {
				event = eventService.removeParticipant(event, customer);				
				addMessage(FacesMessage.SEVERITY_INFO,"event.subinscription.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");
			}
		}
		
		return goToView();
	}
	
	public String save() {
				
		Advice advice = getSessionUser().getAdvice();
		
		if ( event.getDate().before(new Date()) ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "event.date.before.now");
			return null;
		}
		
		if ( event.getFinalDate().after( event.getDate() ) ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "event.limit.afer.date");
			return null;
		}
			
		if ( event.getId()==null ) {			
			event.setAdvice(advice);
			event = eventService.persist(event);
	        addMessage(FacesMessage.SEVERITY_INFO,"event.save.sucess");
		}
		else {
			
			try {
				eventService.update(event);
		        addMessage(FacesMessage.SEVERITY_INFO,"event.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return goToList();
			}  
		}
		return goToList();
	}
	
	/**
	 * Verifica se o usuario pode se inscrever em um evento 
	 * @return False caso ele ja esteja inscrito, True caso contrario
	 */
	@SuppressWarnings("deprecation")
	public boolean getCanParticipate() {
		Pessoa user = getSessionUser();
		
		if ( user.getTipoPessoa().getId()!=TipoPessoa.ALUNO.getId() ) {
			return false;
		}
		
		Date now = new Date();
		now.setHours(01);
		now.setMinutes(00);		
		Date eventFinalDate = new Date(event.getFinalDate().getTime());
		if ( eventFinalDate!=null ) {
			eventFinalDate.setHours(23);
			eventFinalDate.setMinutes(59);
			if ( eventFinalDate.before(now)) {
				return false;
			}
		}
		
		if ( getIsParticipate() ) return false;
		
		return true;
	}
	
	public boolean getIsParticipate() {
		Pessoa user = getSessionUser();
		
		if ( user.getTipoPessoa().getId()!=TipoPessoa.ALUNO.getId() ) {
			return false;
		}
		
		List<Pessoa> participants = event.getCustomers();
		for ( Pessoa p:participants ) {
			if ( p.getId().longValue()==user.getId().longValue() ) return true;
		}
		return false;
	}
	
	public List<Event> getUserEvents() {
		
		return userEvents;
	}

	//Gets and Sets
	
	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public List<Event> getEventList() {
		return eventList;
	}

	public void setEventList(List<Event> eventList) {
		this.eventList = eventList;
	}

	public ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	public boolean isFindActive() {
		return findActive;
	}

	public void setFindActive(boolean findActive) {
		this.findActive = findActive;
	}

	public Pessoa getSelectedCustomer() {
		return selectedCustomer;
	}

	public void setSelectedCustomer(Pessoa selectedCustomer) {
		this.selectedCustomer = selectedCustomer;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public EventPessoaActivityService getEventPessoaActivityService() {
		return eventPessoaActivityService;
	}

	public void setEventPessoaActivityService(
			EventPessoaActivityService eventPessoaActivityService) {
		this.eventPessoaActivityService = eventPessoaActivityService;
	}

	public List<Pessoa> getAlunosInscritos() {
		return alunosInscritos;
	}

	public void setAlunosInscritos(List<Pessoa> alunosInscritos) {
		this.alunosInscritos = alunosInscritos;
	}

	public Pessoa getDeleteCustomer() {
		return deleteCustomer;
	}

	public void setDeleteCustomer(Pessoa deleteCustomer) {
		this.deleteCustomer = deleteCustomer;
	}

	public List<EventPessoaActivity> getEventPessoaList() {
		return eventPessoaList;
	}

	public void setEventPessoaList(List<EventPessoaActivity> eventPessoaList) {
		this.eventPessoaList = eventPessoaList;
	}

	
}
