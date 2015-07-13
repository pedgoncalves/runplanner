package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.jsf.FacesContextUtils;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.RhythmTable;
import br.com.runplanner.domain.Route;
import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.ScheduleTemplate;
import br.com.runplanner.domain.Spreadsheet;
import br.com.runplanner.domain.SpreadsheetType;
import br.com.runplanner.domain.Team;
import br.com.runplanner.domain.TimelessScheduleTemplate;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.service.RhythmService;
import br.com.runplanner.service.RouteService;
import br.com.runplanner.service.ScheduleService;
import br.com.runplanner.service.ScheduleTemplateService;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.service.TimelessScheduleTemplateService;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.CustonScheduleEvent;

@Component("scheduleTemplateBean")
@Scope("session")
public class ScheduleTemplateMBean extends BasicMBean {
	
	private static final String PAGES_SCHEDULE_SCHEDULE_TEMPLATE_LIST = "/pages/schedule/scheduleTemplateList";
	private static final String PAGES_SCHEDULE_SCHEDULE_TEMPLATE = "/pages/schedule/scheduleTemplate";
	private static final String PAGES_SCHEDULE_SCHEDULE_TEMPLATE_DRISTRIBUTION = "/pages/schedule/scheduleTemplateDistribution";
	
	private ScheduleModel eventModel;  
    private Schedule schedule = new Schedule();
    private ScheduleTemplate template = new ScheduleTemplate();
    private RhythmTable rhythmTable;
    private Route route = new Route();
    
    private CustonScheduleEvent selectedEvent; 
    
    private Integer selectedMonth;
    private Integer selectedYear;
    private Integer selectedDay;
    private int selectedType = SpreadsheetType.MONTH.getId();
    
    private Date initialDate = new Date();
    
    private List<ScheduleTemplate> templateList;
	private List<TimelessScheduleTemplate> timelessScheduleTemplateList;	
    private List<SelectItem> typeList = new ArrayList<SelectItem>();
	private List<SelectItem> rhythmList = new ArrayList<SelectItem>();
        
    private ScheduleService scheduleService;
    private ScheduleTemplateService scheduleTemplateService;
    private SpreadsheetService spreadsheetService;
    private ReportService reportService;
	private RhythmService rhythmService;
    private TeamService teamService;
    private PessoaService pessoaService;
	private EmailThreadProductor emailThreadProductor;
	private RouteService routeService;
	private TimelessScheduleTemplateService timelessScheduleTemplateService;
	
	private List<SelectItem> teamList = new ArrayList<SelectItem>();
	private List<SelectItem> customerList = new ArrayList<SelectItem>();
	private List<SelectItem> routeList = new ArrayList<SelectItem>();
	
	//Distribution
	private Pessoa customerDist;
	private Team teamDist;
	private RhythmTable rhythmDist;
	private Boolean sendEmail = Boolean.FALSE;
	
    
    @Autowired
    public ScheduleTemplateMBean(ScheduleService scheduleService, 
    		ScheduleTemplateService scheduleTemplateService,
    		ReportService reportService,
    		RhythmService rhythmService,
    		TeamService teamService,
    		PessoaService pessoaService,
    		SpreadsheetService spreadsheetService,
    		EmailThreadProductor emailThreadProductor,
    		RouteService routeService,
    		TimelessScheduleTemplateService timelessScheduleTemplateService) {
    	
    	this.reportService = reportService;
    	this.rhythmService = rhythmService;
    	this.teamService = teamService;
    	this.pessoaService = pessoaService;
    	this.spreadsheetService = spreadsheetService;
    	this.emailThreadProductor = emailThreadProductor;
    	this.routeService = routeService;
    	this.timelessScheduleTemplateService = timelessScheduleTemplateService;
    	
		this.eventModel = new LazyScheduleModel() {

			private static final long serialVersionUID = 8796136580164685526L;

			@Override
			public void loadEvents(Date start, Date end) {
				clear();
				loadAllEvents();
			}
		};
		
		this.schedule = new Schedule();
		this.template = new ScheduleTemplate();	
		this.route = new Route();	
		this.scheduleService = scheduleService;
		this.scheduleTemplateService = scheduleTemplateService;
		this.selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
		this.selectedYear = Calendar.getInstance().get(Calendar.YEAR);
		this.selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		
		customerDist = new Pessoa();
		teamDist = new Team();
		rhythmDist = new RhythmTable();
		
		typeList = new ArrayList<SelectItem>();
    	for( SpreadsheetType type: SpreadsheetType.values() ) {
			typeList.add(new SelectItem(type.getId(),type.getLabel()));	
		}   	    	
    	
    }  
    
    public String goToList() {
    	
		Pessoa user = getSessionUser();		
    	templateList = scheduleTemplateService.findByCompetence(selectedMonth, selectedYear, user.getAdvice().getId());
    	timelessScheduleTemplateList = timelessScheduleTemplateService.findByAdvice(user.getAdvice().getId());

		this.route = new Route();
    	
		setSelectedMenu(Constants.MENU_SPREADSHEET);
    	
    	return PAGES_SCHEDULE_SCHEDULE_TEMPLATE_LIST;
    }
    
    public String goToInit() {
    	this.schedule = new Schedule();
		this.template = new ScheduleTemplate();	
		
		this.selectedMonth = template.getMonth();
		this.selectedYear = template.getYear();
		
		if( template.getType().equals(SpreadsheetType.WEEK) ) {
			this.selectedDay = template.getDay();
		}
		else {
			this.selectedDay = 1;
		}
		setSelectedMenu(Constants.MENU_SPREADSHEET);
    	return goToList();
    }
      
    public String goToCreate() {   
    	Pessoa user = getSessionUser();

		GregorianCalendar calendar = new GregorianCalendar();
		this.initialDate = new Date(calendar.getTimeInMillis());
    	
		this.schedule = new Schedule();
		this.template = new ScheduleTemplate();
		this.rhythmTable = new RhythmTable();
		this.route = new Route();
		

		selectedType = SpreadsheetType.MONTH.getId();
		
		
		List<Route> routes = routeService.getByAdvice(user.getAdvice().getId());
		routeList = new ArrayList<SelectItem>();
    	for( Route route: routes ) {
    		routeList.add(new SelectItem(route.getId(),route.getName()));	
		} 

		initDistribuition();
    	
    	return PAGES_SCHEDULE_SCHEDULE_TEMPLATE;
    }

	private void initDistribuition() {

    	Pessoa user = getSessionUser();
    	Advice advice = user.getAdvice();
    	
		customerDist = new Pessoa();
		teamDist = new Team();
		rhythmDist = new RhythmTable();
				
		rhythmList = new ArrayList<SelectItem>();
		for( RhythmTable rhythm: rhythmService.getByAdvice(advice.getId()) ) {
			rhythmList.add(new SelectItem(rhythm.getId(),rhythm.getClassification()));	
		}
   	    	
    	teamList = new ArrayList<SelectItem>();
    	for( Team team: teamService.getByAdvice(advice.getId()) ) {
    		teamList.add(new SelectItem(team.getId(),team.getDescription()));	
		}
		
    	customerList = new ArrayList<SelectItem>();
    	for( Pessoa pessoa: pessoaService.getByTipoPessoaAdvice(TipoPessoa.ALUNO, advice.getId()) ) {
    		customerList.add(new SelectItem(pessoa.getId(),pessoa.getNome()));	
		}
    	

    	customerDist = new Pessoa();
		teamDist = new Team();
		rhythmDist = new RhythmTable();
	}
    
    public void goToReport() {
    	Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("ASSESSORIA_ID", loged.getAdvice().getId().intValue());
    	param.put("MES", selectedMonth.toString());
    	param.put("ANO", selectedYear.toString());
    	param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de treino para o mes "+(selectedMonth+1));
    	
    	if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice().getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}
    	
    	if(!reportService.gerar(ReportServiceImpl.TEMPLATES_ADVICE_MES_REPORT_LISTAGEM, param, null, usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}
    }
    
    public void goToOneReport() {
    	Pessoa loged = getSessionUser();
    	boolean usarLogoRunPlanner;
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("TEMPLATE_ID", template.getId().intValue());
    	    	
    	if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
    		param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
    				.getLogo()));
    		param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de Treino "+loged.getAdvice().getName());
    		usarLogoRunPlanner = false;
    	} else {
    		param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de Treino RunPlanner.pfd");
    		usarLogoRunPlanner = true;
    	}
    	
    	if(!reportService.gerar(ReportServiceImpl.TEMPLATE_ADVICE_REPORT, param, null, usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}
    }
    
    public void loadAllEvents() {
    	for(Schedule schedule:template.getSchedules()) {
    		
    		if ( schedule.getWarmUp()!=null && !schedule.getWarmUp().trim().equals("") ) {
	    		CustonScheduleEvent event = new CustonScheduleEvent(schedule.getWarmUp(),schedule.getDate());
	    		event.setScheduleId(schedule.getId());  
	    		event.setAllDay(true);
	    		eventModel.addEvent(event);
    		}
    		
    		if ( schedule.getDescription()!=null && !schedule.getDescription().trim().equals("") ) {
    			String detail = schedule.getDescription().replaceAll("\n", " ");
    			detail = detail.replaceAll("\r", " ");
    			detail = detail.replaceAll("\t", " ");
    			
	    		CustonScheduleEvent event = new CustonScheduleEvent(detail,schedule.getDate());
	    		event.setScheduleId(schedule.getId());  
	    		event.setAllDay(true);
	    		eventModel.addEvent(event);
    		}
    		
    		if ( schedule.getCoolDown()!=null && !schedule.getCoolDown().trim().equals("") ) {
    			CustonScheduleEvent event = new CustonScheduleEvent(schedule.getCoolDown(),schedule.getDate());
    			event.setScheduleId(schedule.getId());   
    			event.setAllDay(true);
    			eventModel.addEvent(event);
    		}
    	}
    }
    
    public String save() {
    	if( template.getTitle() == null || template.getTitle().equals("") ) {    		   
            addMessage(FacesMessage.SEVERITY_ERROR,"schedule.template.err.title"); 
            return null;
    	}
    	
    	if( template.getId()==null ) { 
    		
    		if ( template.getType().equals(SpreadsheetType.TIMELESS) ) {
    			
    			TimelessScheduleTemplateMBean timelessMBean = (TimelessScheduleTemplateMBean)FacesContextUtils.getWebApplicationContext( FacesContext.getCurrentInstance() ).getBean("timelessScheduleTemplateMBean");
    			String page = timelessMBean.goToCreate();
    			
    			TimelessScheduleTemplate timelessScheduleTemplate = new TimelessScheduleTemplate();
    			timelessScheduleTemplate.setTitle( template.getTitle() );
    			timelessScheduleTemplate.setObservations( template.getObservations() );
    			timelessScheduleTemplate.setAdvice(this.getSessionUser().getAdvice());
    			timelessMBean.setTimelessScheduleTemplate(timelessScheduleTemplate);
    			
    			return page;
    		}
    		
    		Pessoa user = getSessionUser();
    		template.setAdvice(user.getAdvice());
    	
    		template.setType( SpreadsheetType.getById(selectedType) );
    		
    		if( template.getType().equals(SpreadsheetType.MONTH) ) {
    			template.setMonth(selectedMonth);
    			template.setYear(selectedYear);
    			template.setDay(1);
    		}
    		else if( template.getType().equals(SpreadsheetType.WEEK) ) {
    			GregorianCalendar eventDate = new GregorianCalendar();
    	    	eventDate.setTime(initialDate);
    	    	Integer eventMonth = eventDate.get(Calendar.MONTH);
    	    	Integer eventYear = eventDate.get(Calendar.YEAR);
    	    	Integer eventDay =  eventDate.get(Calendar.DAY_OF_MONTH);
    	    	
    	    	template.setDay(eventDay);
    	    	template.setMonth(eventMonth);
    	    	template.setYear(eventYear);
    		}
    		
    		
    		GregorianCalendar templateDate = new GregorianCalendar();
        	templateDate.set(GregorianCalendar.DAY_OF_MONTH, template.getDay());
        	templateDate.set(GregorianCalendar.MONTH, template.getMonth());
        	templateDate.set(GregorianCalendar.YEAR, template.getYear());
        	this.initialDate = templateDate.getTime();
        	
    		template = scheduleTemplateService.persist(template);
            //addMessage(FacesMessage.SEVERITY_INFO, "schedule.template.save.sucess");
            return null;
    	}
    	else {
    		try {
    			scheduleTemplateService.update(template);
    			//addMessage(FacesMessage.SEVERITY_INFO, "schedule.template.edit.sucess");
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");				
			}
            
    		
    		customerDist = new Pessoa();
    		teamDist = new Team();
    		rhythmDist = new RhythmTable();
            return PAGES_SCHEDULE_SCHEDULE_TEMPLATE_DRISTRIBUTION;
            //return goToList();
    	}    	
    	
    }
    
    public String goToDistribution() {
    	template = scheduleTemplateService.loadById(template.getId());

		if ( template==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		initDistribuition();
        return PAGES_SCHEDULE_SCHEDULE_TEMPLATE_DRISTRIBUTION;
    }
    
    public String distribution() {
    	
    	Pessoa user = getSessionUser();
    	Advice advice = user.getAdvice();

		List<Pessoa> customerList = new ArrayList<Pessoa>();
    	
    	//Alunos
    	if ( customerDist.getId().longValue()!=-1 ) {
    		
    		if ( customerDist.getId().longValue()==0 ) {
        		//Todos alunos
    			customerList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ALUNO, advice.getId());
    		}
    		else {
    			//Um aluno    			
    			customerDist = pessoaService.loadById(customerDist.getId());
    			customerList.add(customerDist);
    		}
    	}
    	else if ( teamDist.getId().longValue()!=-1 ) {
    		Team team = teamService.loadById( teamDist.getId() );
    		customerList = team.getCustomers();
    	}
    	else if ( rhythmDist.getId().longValue()!=-1 ) {
    		customerList = pessoaService.getByTipoPessoaAdviceClassification(TipoPessoa.ALUNO, advice.getId(), rhythmDist.getId());
    	}
    	
    	if ( customerList.size()>0 ) {
    		applyTemplate(customerList);
			addMessage(FacesMessage.SEVERITY_INFO, "schedule.template.distribution.success");
    	}
    	else {
    		//Planilha nao associada
    		addMessage(FacesMessage.SEVERITY_INFO, "schedule.template.distribution.none");
    	}
    	
    	return goToList();
    }

	private void applyTemplate(List<Pessoa> customerList) {
		
		for(Pessoa customer:customerList) {
			
			if ( !customer.isActive() ) continue;
			
			Spreadsheet spreadsheet = new Spreadsheet();
			spreadsheet.setStudent(customer);    	
			
			RhythmTable rhythm = rhythmService.getByCustomer(customer.getId());
			if ( rhythm!=null ) {
				spreadsheet.setEasyPace(rhythm.getEasyPace());
				spreadsheet.setNormalPace(rhythm.getNormalPace());
				spreadsheet.setMediumPace(rhythm.getMediumPace());
				spreadsheet.setModeratePace(rhythm.getModeratePace());
				spreadsheet.setStrongPace(rhythm.getStrongPace());
				spreadsheet.setShootingPace(rhythm.getShootingPace());
			}
			
			List<Schedule> schedules = template.getSchedules();
			for(Schedule schedule:schedules) {
				schedule.setId(null);
				spreadsheet.getSchedules().add(schedule);
			}

			spreadsheet.setType( SpreadsheetType.getById( template.getType().getId() ) );
			
			spreadsheet.setMonth(selectedMonth);
			spreadsheet.setYear(selectedYear);
			if( spreadsheet.getType().equals(SpreadsheetType.WEEK) ) {
				spreadsheet.setDay(template.getDay());
			}
			else {
				spreadsheet.setDay(1);
			}
			
			
			
			spreadsheet.setSent(false);

			spreadsheet = spreadsheetService.persist(spreadsheet);
			
			//Enviar por email?
			if( sendEmail ) {
				sendSpreadsheetByEmail(spreadsheet, customer.getEmail());
			}
		}
	}
	
	 private void sendSpreadsheetByEmail(Spreadsheet spreadSheet, String email) {
	    	
	    	Pessoa user = getSessionUser();
	    	
	    	String messageBody = Constants.EMAIL_SPREADSHEET;
			messageBody = messageBody.replace("$1", user.getAdvice().getName());
			
	    	HashMap<String, Object> param = new HashMap<String, Object>();
	    	param.put("PLANILHA_ID", spreadSheet.getId().intValue());
			byte[] spreadsheet = reportService.getSpreadsheet(param);
			
			emailThreadProductor.enviarMensagem(email, Constants.EMAIL_SPREADSHEET_TITLE, messageBody, spreadsheet);
					
			spreadSheet.setSent(true);
			try {
				spreadsheetService.update(spreadSheet);
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
	    	
	    }
    
    public void find() {
    	Pessoa user = getSessionUser();		
    	templateList = scheduleTemplateService.findByCompetence(selectedMonth, selectedYear, user.getAdvice().getId());
    }
    
    public String delete() {
    	try {
    		scheduleTemplateService.deleteScheduleTemplate(template);
    		addMessage(FacesMessage.SEVERITY_INFO, "schedule.template.delete.sucess");
    	}
    	catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
		} 
    	
    	return goToList();
    }
    
    public String goToModify() {
    	Pessoa user = getSessionUser();
    	template = scheduleTemplateService.loadById(template.getId());

		if ( template==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
    	
		this.selectedMonth = template.getMonth();
    	this.selectedYear = template.getYear();
    	this.selectedDay = template.getDay();
    	this.selectedType = template.getType().getId();
    	
    	GregorianCalendar templateDate = new GregorianCalendar();
    	templateDate.set(GregorianCalendar.DAY_OF_MONTH, 1);
    	templateDate.set(GregorianCalendar.MONTH, selectedMonth);
    	templateDate.set(GregorianCalendar.YEAR, selectedYear);
    	if( template.getType().equals(SpreadsheetType.WEEK) ) {
    		templateDate.set(Calendar.DAY_OF_MONTH, selectedDay);
		}
		else {
			templateDate.set(Calendar.DAY_OF_MONTH, 1);
		}
    	this.initialDate = templateDate.getTime();
    	
    	List<Route> routes = routeService.getByAdvice(user.getAdvice().getId());
		routeList = new ArrayList<SelectItem>();
    	for( Route route: routes ) {
    		routeList.add(new SelectItem(route.getId(),route.getName()));	
		} 
		this.route = new Route();
    	
    	initDistribuition();
    	
    	return PAGES_SCHEDULE_SCHEDULE_TEMPLATE;
    }    
    
    public void addEvent(ActionEvent actionEvent) {   
    	if(template.getId() == null || (template.getTitle() == null || template.getTitle().equals("") ) ) {    		   
             addMessage(FacesMessage.SEVERITY_ERROR, "schedule.template.err.title"); 
             return;
    	}   	
    	
    	
    	GregorianCalendar eventDate = new GregorianCalendar();
    	eventDate.setTime(schedule.getDate());
    	Integer eventMonth = eventDate.get(Calendar.MONTH);
    	Integer eventYear = eventDate.get(Calendar.YEAR);
    	Integer eventDay =  eventDate.get(Calendar.DAY_OF_MONTH);
    	
    	/*if ( !eventMonth.equals(selectedMonth) || !eventYear.equals(selectedYear) ) {
    		
    		if ( template.getSchedules().isEmpty() )  {
    			this.selectedMonth = eventMonth;
        		this.selectedYear = eventYear;        		
    		}
    		else {
    			addMessage(FacesMessage.SEVERITY_ERROR, "schedule.template.err.diferent.month");
    			return;        		
        	}
    	}   */
    	
    	if (route.getId()!=null && route.getId().longValue()!=0) {
    		schedule.setRoute(route);
    	}
    	
    	if(schedule.getId() == null) {
    		
    		this.selectedMonth = eventMonth;
    		this.selectedYear = eventYear; 
    		if( template.getType().equals(SpreadsheetType.WEEK) ) {
    			this.selectedDay = eventDay;
    		}
    		else {
    			this.selectedDay = 1;
    		}
    		
    		
    		schedule = scheduleService.persist(schedule);    		    		
    		template.getSchedules().add(schedule);
    		
    		/*template.setMonth(selectedMonth);
    		template.setYear(selectedYear);
    		template.setDay(selectedDay);*/
    		
    		try {
    			scheduleTemplateService.update(template);    
    			

        		//Setar nova data inicial
        		/*GregorianCalendar templateDate = new GregorianCalendar();
            	templateDate.set(GregorianCalendar.DAY_OF_MONTH, 1);
            	templateDate.set(GregorianCalendar.MONTH, selectedMonth);
            	templateDate.set(GregorianCalendar.YEAR, selectedYear);
            	if( template.getType().equals(SpreadsheetType.WEEK) ) {
            		templateDate.set(Calendar.DAY_OF_MONTH, selectedDay);
        		}
        		else {
        			templateDate.set(Calendar.DAY_OF_MONTH, 1);
        		}
            	this.initialDate = templateDate.getTime();*/
            	
    		}
    		catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
    	}
        else {
        	try {
        		scheduleService.update(schedule);
        		
        		for(Schedule s:template.getSchedules()) {
        			if (s.getId().longValue()==schedule.getId().longValue()) {
        				template.getSchedules().remove(s);
        				break;
        			}
        		}
        		   
            	template.getSchedules().add(schedule);
        	} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
        	}
        	
        }          
    	schedule = new Schedule();  
    	route = new Route();
    }
      
    public void deleteEvent() {    	    	
    	if(schedule.getId() != null) {        	
    		template = scheduleTemplateService.removeSchedule(template, schedule);    		 
        }          
    	schedule = new Schedule();
    }  
    
    public void onEventSelect(SelectEvent selectEvent) { 

    	ScheduleEvent event = (ScheduleEvent) selectEvent.getObject();  
    	selectedEvent = (CustonScheduleEvent) event;
    	schedule = scheduleService.loadById(selectedEvent.getScheduleId());
    	
    	route = new Route();
    	if (schedule.getRoute()!=null && schedule.getRoute().getId()!=null && schedule.getRoute().getId().longValue()!=0) {
    		route = schedule.getRoute();
    	} 
    }  
      
    public void onDateSelect(SelectEvent selectEvent) {  
    	
    	Date selectedDate = (Date) selectEvent.getObject();
    	
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTimeZone( TimeZone.getTimeZone("America/Sao_Paulo") );
    	calendar.setTime(selectedDate);
    	calendar.set(Calendar.HOUR_OF_DAY, 10);
    	
    	if ( template.getSchedules()!=null ) {
    		for(Schedule s:template.getSchedules()) {
    			if ( s.getDate().compareTo(selectedDate) == 0 ) {
    				schedule = s;
    				return;
    			}
    		}
    	}
    	
    	schedule = new Schedule();
    	schedule.setDate( calendar.getTime() );   
    	route = new Route();
    }  
      
    public void onEventMove(ScheduleEntryMoveEvent event) {  
        Integer dayDelta = event.getDayDelta();  
        
        selectedEvent = (CustonScheduleEvent)event.getScheduleEvent();
    	schedule = scheduleService.loadById(selectedEvent.getScheduleId());
    	
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTimeZone( TimeZone.getTimeZone("America/Sao_Paulo") );
    	calendar.setTime(schedule.getDate());
    	calendar.set(Calendar.HOUR_OF_DAY, 10);
    	calendar.add(GregorianCalendar.DAY_OF_MONTH, dayDelta);
    	
    	Integer eventMonth = calendar.get(Calendar.MONTH);
    	Integer eventYear = calendar.get(Calendar.YEAR);
    	
    	if ( eventMonth.equals(selectedMonth) && eventYear.equals(selectedYear) ) {
    		schedule.setDate(calendar.getTime());
        	try {
        		scheduleService.update(schedule); 
        	} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
    	}
    	else {
    		addMessage(FacesMessage.SEVERITY_ERROR, "spreadsheet.template.err.diferent.month");
    	}

    	this.template = scheduleTemplateService.loadById(template.getId());
    }      
    
    // Util Month List to Filter Combobox    
    public List<SelectItem> getMonthList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();
    	String months[] = new DateFormatSymbols(new Locale("pt","BR")).getMonths();
    	for (int i = 0; i < 12; i++) {
			result.add( new SelectItem(i,months[i]) );
		}
    	return result;
    }
    
    public List<SelectItem> getYearList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();
    	
    	GregorianCalendar date = new GregorianCalendar();
    	Integer year = date.get(GregorianCalendar.YEAR);
    	    	
    	year--;
    	result.add( new SelectItem(year,year.toString()) );
    	year++;
    	result.add( new SelectItem(year,year.toString()) );
    	year++;
    	result.add( new SelectItem(year,year.toString()) );
    	
    	return result;
    }
    	
    public String cloneTemplate() {    	
    	this.template = scheduleTemplateService.loadById(template.getId());
    	scheduleTemplateService.cloneTemplate(template);
    	
		addMessage(FacesMessage.SEVERITY_INFO, "schedule.template.clone.sucess");
    	
    	return goToList();
    }
    
    public void handleSelectChangeCustomer() {
		teamDist = new Team();
		rhythmDist = new RhythmTable();
    }
    
    public void handleSelectChangeTeam() {
    	customerDist = new Pessoa();
		rhythmDist = new RhythmTable();
    }

	public void handleSelectChangeRhythm() {
		customerDist = new Pessoa();
		teamDist = new Team();
	}
	
	public void handleSelectChangeType() {
    	template.setType( SpreadsheetType.getById(selectedType) );
	}
    
    //Getters and Setters 

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	public void setEventModel(ScheduleModel eventModel) {
		this.eventModel = eventModel;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public ScheduleTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ScheduleTemplate template) {
		this.template = template;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public void setScheduleTemplateService(ScheduleTemplateService scheduleTemplateService) {
		this.scheduleTemplateService = scheduleTemplateService;
	}

	public Date getInitialDate() {
		return initialDate;
	}

	public List<ScheduleTemplate> getTemplateList() {
		return templateList;
	}

	public Integer getSelectedMonth() {
		return selectedMonth;
	}

	public void setSelectedMonth(Integer selectedMonth) {
		this.selectedMonth = selectedMonth;
	}

	public Integer getSelectedYear() {
		return selectedYear;
	}

	public void setSelectedYear(Integer selectedYear) {
		this.selectedYear = selectedYear;
	}	

	public String getType() {
		if ( template==null || selectedType == SpreadsheetType.MONTH.getId() ) {
			return "month";
		}
		else if ( selectedType == SpreadsheetType.TIMELESS.getId() ) {
			return "timeless";
		}
		else if ( selectedType == SpreadsheetType.WEEK.getId() ) {
			return "basicWeek";
		}
		else {
			return "month";
		}
	}

	public List<SelectItem> getTypeList() {
		return typeList;
	}

	public String getLeftHeaderTemplate() {
		//String result = "prev, next, today";
		//Nao pode alterar
		String result = "";
		
		if ( !template.getSchedules().isEmpty() ) {
			result = "";
		}
		
		return result;
	}

	public RhythmService getRhythmService() {
		return rhythmService;
	}

	public void setRhythmService(RhythmService rhythmService) {
		this.rhythmService = rhythmService;
	}
	
	public List<SelectItem> getRhythmList() {		
		return rhythmList;
	}

	public RhythmTable getRhythmTable() {
		return rhythmTable;
	}

	public void setRhythmTable(RhythmTable rhythmTable) {
		this.rhythmTable = rhythmTable;
	}

	public List<SelectItem> getTeamList() {
		return teamList;
	}

	public void setTeamList(List<SelectItem> teamList) {
		this.teamList = teamList;
	}

	public List<SelectItem> getCustomerList() {
		return customerList;
	}

	public void setCustomerList(List<SelectItem> customerList) {
		this.customerList = customerList;
	}

	public Pessoa getCustomerDist() {
		return customerDist;
	}

	public void setCustomerDist(Pessoa customerDist) {
		this.customerDist = customerDist;
	}

	public Team getTeamDist() {
		return teamDist;
	}

	public void setTeamDist(Team teamDist) {
		this.teamDist = teamDist;
	}

	public RhythmTable getRhythmDist() {
		return rhythmDist;
	}

	public void setRhythmDist(RhythmTable rhythmDist) {
		this.rhythmDist = rhythmDist;
	}

	public Boolean getSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(Boolean sendEmail) {
		this.sendEmail = sendEmail;
	}

	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}

	public int getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(int selectedType) {
		this.selectedType = selectedType;
	}

	public List<SelectItem> getRouteList() {
		return routeList;
	}

	public void setRouteList(List<SelectItem> routeList) {
		this.routeList = routeList;
	}

	public List<TimelessScheduleTemplate> getTimelessScheduleTemplateList() {
		return timelessScheduleTemplateList;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	
}
