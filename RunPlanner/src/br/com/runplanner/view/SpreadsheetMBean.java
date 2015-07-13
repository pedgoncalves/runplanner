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
import br.com.runplanner.domain.TimelessScheduleWeek;
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
import br.com.runplanner.util.DateUtils;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.CustomerDataModel;
import br.com.runplanner.view.util.CustonScheduleEvent;
import br.com.runplanner.view.util.SpreadSheetDataModel;

@Component("spreadsheetBean")
@Scope("session")
public class SpreadsheetMBean extends BasicMBean {

	private static final String PAGES_SCHEDULE_SPREADSHEET = "/pages/schedule/spreadsheet";
	private static final String PAGES_SCHEDULE_SPREADSHEET_LIST = "/pages/schedule/spreadsheetList";
	private static final String LEFT_HEADER_TEMPLATE_MULTI = "prev, next, today";

	private ScheduleModel eventModel;  
    
	private Schedule schedule = new Schedule();
    private ScheduleTemplate template = new ScheduleTemplate();
    private TimelessScheduleTemplate timelessScheduleTemplate = new TimelessScheduleTemplate();
    private Spreadsheet spreadsheet = new Spreadsheet();
    private Team selectedTeam;
    private Team selectedTeamFind = new Team();
    private Route route = new Route();
    
    private CustonScheduleEvent selectedEvent; 
    private Spreadsheet[] selectedSpreadsheetList; 
    private Pessoa[] selectedSpreadsheetListLess; 
    
    private SpreadSheetDataModel spreadSheetDataModel;
    private CustomerDataModel customerDataModel;
    
    private List<Spreadsheet> spreadsheetList = new ArrayList<Spreadsheet>();
    private List<Pessoa> spreadlessList = new ArrayList<Pessoa>();
    
    private Integer selectedMonth;
    private Integer selectedYear;
    private Integer selectedDay;
    private int selectedType = SpreadsheetType.MONTH.getId();
    private String leftHeaderTemplate = "";
    
    private Date initialDate = new Date();
    private Date applyDate = new Date();
    
    private ScheduleService scheduleService;
    private ScheduleTemplateService scheduleTemplateService;
    private SpreadsheetService spreadsheetService;
    private PessoaService pessoaService;
    private RhythmService rhythmService;
    private ReportService reportService;
    private TeamService teamService;
	private EmailThreadProductor emailThreadProductor;
	private RouteService routeService;
	private TimelessScheduleTemplateService timelessScheduleTemplateService;
	
	private List<SelectItem> teamList = new ArrayList<SelectItem>();
	private List<SelectItem> customerList = new ArrayList<SelectItem>();
	private List<SelectItem> templateList = new ArrayList<SelectItem>();
    private List<SelectItem> typeList = new ArrayList<SelectItem>();
	private List<SelectItem> routeList = new ArrayList<SelectItem>();
	
	@SuppressWarnings("rawtypes")
	private List templateFinalList = new ArrayList();
    
    @Autowired
    public SpreadsheetMBean(ScheduleService scheduleService, 
    		ScheduleTemplateService scheduleTemplateService,
    		SpreadsheetService spreadsheetService,
    		PessoaService pessoaService,
    		RhythmService rhythmService,
    		ReportService reportService,
    		TeamService teamService,
    		EmailThreadProductor emailThreadProductor,
    		RouteService routeService,
    		TimelessScheduleTemplateService timelessScheduleTemplateService) { 
    	
		this.eventModel = new LazyScheduleModel() {

			private static final long serialVersionUID = 8796136580164685526L;

			@Override
			public void loadEvents(Date start, Date end) {
				clear();
				loadAllEvents();
			}
		};
		
		this.schedule = new Schedule();
		this.route = new Route();
		this.template = new ScheduleTemplate();
		this.spreadsheet = new Spreadsheet();
		this.spreadsheetList = new ArrayList<Spreadsheet>();
		
		this.scheduleService = scheduleService;
		this.scheduleTemplateService = scheduleTemplateService;
		this.spreadsheetService = spreadsheetService;
		this.pessoaService  = pessoaService;
		this.rhythmService  = rhythmService;
		this.reportService  = reportService;
		this.teamService    = teamService;
		this.emailThreadProductor = emailThreadProductor;
		this.routeService = routeService;
		this.timelessScheduleTemplateService = timelessScheduleTemplateService;
		
		this.selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
		this.selectedYear  = Calendar.getInstance().get(Calendar.YEAR);
		this.selectedDay   = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		
		typeList = new ArrayList<SelectItem>();
		typeList.add(new SelectItem(SpreadsheetType.MONTH.getId(), SpreadsheetType.MONTH.getLabel()));	
		typeList.add(new SelectItem(SpreadsheetType.WEEK.getId(), SpreadsheetType.WEEK.getLabel()));	
		
    }
    
    public void loadAllEvents() {
    	for(Schedule schedule:spreadsheet.getSchedules()) {
    		
    		String text = "";
    		
    		if ( schedule.getWarmUp()!=null && !schedule.getWarmUp().trim().equals("") ) {
	    		text += "Aquecimento: "+schedule.getWarmUp() + "\n";
    		}
    		
    		if ( schedule.getDescription()!=null && !schedule.getDescription().trim().equals("") ) {
    			String detail = "Treino: " + schedule.getDescription().replaceAll("\n", " ");
    			detail = detail.replaceAll("\r", " ");
    			detail = detail.replaceAll("\t", " ");
    			
    			text += detail + "\n";
    		}
    		
    		if ( schedule.getCoolDown()!=null && !schedule.getCoolDown().trim().equals("") ) {
	    		text += "Desaquecimento: "+schedule.getCoolDown();
    		}

    		if ( text.length()>0 ) {
	    		CustonScheduleEvent event = new CustonScheduleEvent(text,schedule.getDate());
	    		event.setScheduleId(schedule.getId()); 
	    		event.setAllDay(true);   		
	    		eventModel.addEvent(event);
    		}
    	}
    }
    
    private void populateRhythm() {
    	if ( spreadsheet.getStudent().getId()!=null ) {
    		RhythmTable rhythm = spreadsheet.getStudent().getClassification();
    		    		
    		if ( rhythm!=null ) {
    			spreadsheet.setEasyPace(rhythm.getEasyPace());
    			spreadsheet.setNormalPace(rhythm.getNormalPace());
    			spreadsheet.setMediumPace(rhythm.getMediumPace());
    			spreadsheet.setModeratePace(rhythm.getModeratePace());
    			spreadsheet.setStrongPace(rhythm.getStrongPace());
    			spreadsheet.setShootingPace(rhythm.getShootingPace());
    		}
    	}
    }
    
    private void aplicarTemplateTimeless(Pessoa customer) {

    	if (spreadsheet.getStudent()==null || spreadsheet.getStudent().getId()==null) {
    		addMessage(FacesMessage.SEVERITY_ERROR, "spreadsheet.err.aluno"); 
            return;
    	} 
    	
    	customer = pessoaService.loadByIdClassification(spreadsheet.getStudent().getId());    	
    	spreadsheet.setStudent(customer);
    	spreadsheet.setSent(false);    	

    	RhythmTable rhythm = customer.getClassification();
    	if (rhythm != null) {
    		spreadsheet.setEasyPace(rhythm.getEasyPace());
    		spreadsheet.setNormalPace(rhythm.getNormalPace());
    		spreadsheet.setMediumPace(rhythm.getMediumPace());
    		spreadsheet.setModeratePace(rhythm.getModeratePace());
    		spreadsheet.setStrongPace(rhythm.getStrongPace());
    		spreadsheet.setShootingPace(rhythm.getShootingPace());
    	}

    	int weekCount = 0;
    	for (TimelessScheduleWeek week : timelessScheduleTemplate.getWeeks()) {				

    		if (!week.getFirstDay().isEmpty()) {
    			Schedule schedule = week.getFirstDay().copy();
    			schedule.setId(null);

    			GregorianCalendar iniDate = new GregorianCalendar();
    			iniDate.setTime(applyDate);
    			iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
    			schedule.setDate(iniDate.getTime());
    			spreadsheet.getSchedules().add(schedule);
    		}

    		if (!week.getSecondDay().isEmpty()) {
    			Schedule schedule = week.getSecondDay().copy();
    			schedule.setId(null);

    			GregorianCalendar iniDate = new GregorianCalendar();
    			iniDate.setTime(applyDate);
    			iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
    			iniDate.add(Calendar.DATE, 1);
    			schedule.setDate(iniDate.getTime());
    			spreadsheet.getSchedules().add(schedule);
    		}

    		if (!week.getThirdDay().isEmpty()) {
    			Schedule schedule = week.getThirdDay().copy();
    			schedule.setId(null);

    			GregorianCalendar iniDate = new GregorianCalendar();
    			iniDate.setTime(applyDate);
    			iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
    			iniDate.add(Calendar.DATE, 2);
    			schedule.setDate(iniDate.getTime());
    			spreadsheet.getSchedules().add(schedule);
    		}

    		if (!week.getFourthDay().isEmpty()) {
    			Schedule schedule = week.getFourthDay().copy();
    			schedule.setId(null);

    			GregorianCalendar iniDate = new GregorianCalendar();
    			iniDate.setTime(applyDate);
    			iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
    			iniDate.add(Calendar.DATE, 3);
    			schedule.setDate(iniDate.getTime());
    			spreadsheet.getSchedules().add(schedule);
    		}

    		if (!week.getFifthDay().isEmpty()) {
    			Schedule schedule = week.getFifthDay().copy();
    			schedule.setId(null);

    			GregorianCalendar iniDate = new GregorianCalendar();
    			iniDate.setTime(applyDate);
    			iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
    			iniDate.add(Calendar.DATE, 4);
    			schedule.setDate(iniDate.getTime());
    			spreadsheet.getSchedules().add(schedule);
    		}

    		if (!week.getSixthDay().isEmpty()) {
    			Schedule schedule = week.getSixthDay().copy();
    			schedule.setId(null);

    			GregorianCalendar iniDate = new GregorianCalendar();
    			iniDate.setTime(applyDate);
    			iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
    			iniDate.add(Calendar.DATE, 5);
    			schedule.setDate(iniDate.getTime());
    			spreadsheet.getSchedules().add(schedule);
    		}

    		if (!week.getSeventhDay().isEmpty()) {
    			Schedule schedule = week.getSeventhDay().copy();
    			schedule.setId(null);

    			GregorianCalendar iniDate = new GregorianCalendar();
    			iniDate.setTime(applyDate);
    			iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
    			iniDate.add(Calendar.DATE, 6);
    			schedule.setDate(iniDate.getTime());
    			spreadsheet.getSchedules().add(schedule);
    		}

    		weekCount++;
    	}

    	GregorianCalendar iniDate = new GregorianCalendar();
    	iniDate.setTime(applyDate);
    	spreadsheet.setType(SpreadsheetType.MONTH);

    	spreadsheet.setYear(iniDate.get(Calendar.YEAR));
    	spreadsheet.setMonth(iniDate.get(Calendar.MONTH));
    	spreadsheet.setDay(iniDate.get(Calendar.DAY_OF_MONTH));

    	spreadsheet.setSent(false);

    	//checkNullRoutesOnSchedule(spreadsheet);
    	spreadsheet = spreadsheetService.persist(spreadsheet);

    }
    
    public void saveStepOne() {
    	if (spreadsheet.getStudent()==null || spreadsheet.getStudent().getId()==null) {
    		addMessage(FacesMessage.SEVERITY_ERROR, "spreadsheet.err.aluno"); 
            return;
    	} 
    	
    	Pessoa customer = pessoaService.loadByIdClassification(spreadsheet.getStudent().getId());    	
    	spreadsheet.setStudent(customer);
    	spreadsheet.setSent(false);
    	
    	spreadsheet.setType( SpreadsheetType.getById(selectedType) );
    	
    	populateRhythm();
    	
    	if (template.getId()!=null && template.getId().intValue()!=-1) {
    		
    		template = scheduleTemplateService.loadById(template.getId());
    		List<Schedule> schedules = template.getSchedules();
    		for(Schedule schedule:schedules) {
    			schedule.setId(null);
    			spreadsheet.getSchedules().add(schedule);
    		}
    		
    		spreadsheet.setType( template.getType() );
        	spreadsheet.setDay(template.getDay());
	    	spreadsheet.setMonth(template.getMonth());
	    	spreadsheet.setYear(template.getYear());
    	}
    	else {
    		if( spreadsheet.getType().equals(SpreadsheetType.MONTH) ) {
    			spreadsheet.setMonth(selectedMonth);
    			spreadsheet.setYear(selectedYear);
    			spreadsheet.setDay(1);
    		}
    		else if( spreadsheet.getType().equals(SpreadsheetType.WEEK) ) {
    			GregorianCalendar eventDate = new GregorianCalendar();
    	    	eventDate.setTime(initialDate);
    	    	Integer eventMonth = eventDate.get(Calendar.MONTH);
    	    	Integer eventYear = eventDate.get(Calendar.YEAR);
    	    	Integer eventDay =  eventDate.get(Calendar.DAY_OF_MONTH);
    	    	
    	    	spreadsheet.setDay(eventDay);
    	    	spreadsheet.setMonth(eventMonth);
    	    	spreadsheet.setYear(eventYear);
    		}    	
    	}
		
		selectedMonth = spreadsheet.getMonth();
		selectedYear = spreadsheet.getYear();
		selectedDay = spreadsheet.getDay();
    	
    	
		GregorianCalendar templateDate = new GregorianCalendar();
    	templateDate.set(GregorianCalendar.DAY_OF_MONTH, spreadsheet.getDay());
    	templateDate.set(GregorianCalendar.MONTH, spreadsheet.getMonth());
    	templateDate.set(GregorianCalendar.YEAR, spreadsheet.getYear());
    	this.initialDate = templateDate.getTime();
    	
    	spreadsheet = spreadsheetService.persist(spreadsheet);    	
    }
    
    public String sendEmails() {
    	if ( selectedSpreadsheetList==null || selectedSpreadsheetList.length<=0 ) {
    		addMessage(FacesMessage.SEVERITY_ERROR, "spreadsheet.email.send.empty");
    		return null;
    	}
    	
    	for (int i = 0; i < selectedSpreadsheetList.length; i++) {
    		Spreadsheet spread = selectedSpreadsheetList[i];    		
    		String email = spread.getStudent().getEmail();
    		
    		sendSpreadsheetByEmail(spread,email);
		}
    	
    	selectedSpreadsheetList=null;
        addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.email.sucess");
        
        return find();
    }
    
    public String goToAtribuirTemplate() {
    	if ( selectedSpreadsheetListLess==null || selectedSpreadsheetListLess.length<=0 ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "spreadsheet.template.apply.empty");
    		return null;
    	}
    	
    	
    	Long pos = template.getId();
    	Object obj = templateFinalList.get(pos.intValue());
    	boolean timeless = false;
    	if ( obj instanceof ScheduleTemplate ) {
    		template = (ScheduleTemplate)obj;
    	}
    	else if ( obj instanceof TimelessScheduleTemplate ) {
    		timelessScheduleTemplate = (TimelessScheduleTemplate)obj;
    		timeless=true;
    	}
    	
    	for (int i = 0; i < selectedSpreadsheetListLess.length; i++) {
    		Pessoa pessoa = selectedSpreadsheetListLess[i];  
    		spreadsheet = new Spreadsheet();
    		spreadsheet.setStudent(pessoa);
    		
    		if (!timeless) {
    			this.saveStepOne();
    		}
    		else {
    			this.aplicarTemplateTimeless(pessoa);
    		}
    		
    	}
    	
    	addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.save.sucess");
    	
    	selectedSpreadsheetListLess = null;
    	
    	return goToList();
    }
    
    public String sendByEmail() {
    	 Long id = spreadsheet.getId();
    	 
    	 spreadsheet = spreadsheetService.loadById(id);
    	 
    	 String email = spreadsheet.getStudent().getEmail();
    	 sendSpreadsheetByEmail(spreadsheet,email);
    	 

         addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.email.sucess");
         return find();
    }
    
    private void sendSpreadsheetByEmail(Spreadsheet spreadSheet, String email) {
    	
    	Pessoa user = getSessionUser();
    	
    	String messageBody = Constants.EMAIL_SPREADSHEET;
		messageBody = messageBody.replace("$1", user.getAdvice().getName());
		
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("PLANILHA_ID", spreadSheet.getId().intValue());
    	param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de treino de "
    	    	+spreadsheet.getStudent().getNome()+" - "
    	    	+DateUtils.mesPorExtenso(selectedMonth));
    	
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
    
    public String save() {
    	if( spreadsheet.getId()==null ) {    	
    		spreadsheet = spreadsheetService.persist(spreadsheet);
            addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.save.sucess");
            
            return null;
    	}
    	else {
    		try {
    			this.spreadsheet.setSent(false);
    			spreadsheetService.update(spreadsheet);
    	    	addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.edit.sucess");
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return goToList();
			}
            
    	}   
    	
        return goToList();
    }
    
    public String goToCreate() {    	
		this.schedule = new Schedule();
		this.template = new ScheduleTemplate();
		this.spreadsheet = new Spreadsheet();
		this.route = new Route();
		selectedType = SpreadsheetType.MONTH.getId();
		spreadsheet.setType( SpreadsheetType.MONTH );
		
		
		//this.initialDate = new Date();
		//this.selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
		//this.selectedYear = Calendar.getInstance().get(Calendar.YEAR);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, selectedMonth);
		calendar.set(Calendar.YEAR, selectedYear);
		this.initialDate = new Date(calendar.getTimeInMillis());
		
		
    	Pessoa logUser = getSessionUser();
    	Long adviceId = logUser.getAdvice().getId();
    	
    	
    	//Lista de Aluno
		List<Pessoa> list = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO,adviceId,true);
		customerList = new ArrayList<SelectItem>();
		for (Pessoa pessoa:list) {
			customerList.add(new SelectItem(pessoa.getId(),pessoa.getNome()));
		}
		
		if ( (spreadsheet.getStudent()==null || spreadsheet.getStudent().getId()==null)
				&& list.size()>0 ) {
			spreadsheet.setStudent(list.get(0));
		}
		
    
		//Lista de Template		
		Integer month = Calendar.getInstance().get(Calendar.MONTH);
		Integer year = Calendar.getInstance().get(Calendar.YEAR);

    	populateTemplateList(adviceId, month, year);
		
		//Lista de Rotas
		List<Route> routes = routeService.getByAdvice(logUser.getAdvice().getId());
		routeList = new ArrayList<SelectItem>();
    	for( Route route: routes ) {
    		routeList.add(new SelectItem(route.getId(),route.getName()));	
		} 
		
    	verifyLeftHeader();
    	return PAGES_SCHEDULE_SPREADSHEET;
    }
    
   /* public void goToPrepareTemplateList() {
    	List<ScheduleTemplate> scheduleTemplateList = scheduleTemplateService.findByCompetenceNext(selectedMonth, selectedYear, getSessionUser().getAdvice().getId());
    	templateList = new ArrayList<SelectItem>();
		for (ScheduleTemplate template:scheduleTemplateList) {
			templateList.add(new SelectItem(template.getId(),template.getTitle()+"-"+template.getTextMonth()+"/"+template.getYear()));
		}
    }*/
    
    public void updateClassification() {
    	Pessoa aluno = pessoaService.loadById(spreadsheet.getStudent().getId());
    	spreadsheet.setStudent(aluno);
    }
    
    public String goToList() {
		this.schedule = new Schedule();
		this.template = new ScheduleTemplate();
		this.spreadsheet = new Spreadsheet();
		this.route = new Route();
		
		Pessoa user = getSessionUser();		
		this.spreadsheetList = spreadsheetService.findByCompetenceOrdered(selectedMonth, selectedYear, user.getAdvice().getId());
		this.spreadSheetDataModel = new SpreadSheetDataModel(spreadsheetList);
				
    	Pessoa pessoa = getSessionUser();
    	Advice advice = pessoa.getAdvice();
    	
		List<Team> teams = teamService.getByAdvice(advice.getId());
		teamList = new ArrayList<SelectItem>();
		for (Team team:teams) {
			teamList.add(new SelectItem(team.getId(),team.getPlace()));
		}
		
		
		Long adviceId = user.getAdvice().getId();			
		this.spreadlessList = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO, adviceId, true);
		this.customerDataModel = new CustomerDataModel(spreadlessList);
		
    	for(Spreadsheet spread:spreadsheetList) {
    		spreadlessList.remove(spread.getStudent());
		}
    	
    	selectedSpreadsheetList = null;
		 
    	
    	//Lista de Template		
    	populateTemplateList(adviceId, selectedMonth, selectedYear);
    	
		setSelectedMenu(Constants.MENU_SPREADSHEET);
		
    	return PAGES_SCHEDULE_SPREADSHEET_LIST;
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateTemplateList(Long adviceId, int selectedMonth, int selectedYear) {
		List<ScheduleTemplate> scheduleTemplateList = scheduleTemplateService.findByCompetenceNext(selectedMonth, selectedYear, adviceId);
    	List<TimelessScheduleTemplate> timelessScheduleTemplateList = timelessScheduleTemplateService.findByAdvice(adviceId);
    	
    	templateList = new ArrayList<SelectItem>();
    	templateFinalList = new ArrayList();
    	int position = 0;
		for (ScheduleTemplate template:scheduleTemplateList) {
			templateList.add(new SelectItem(position,template.getTitle()+"-"+template.getTextMonth()+"/"+template.getYear()));
			templateFinalList.add(template);
			position++;
		}
		for (TimelessScheduleTemplate template: timelessScheduleTemplateList) {
			templateList.add(new SelectItem(position,template.getTitle()+" (Atemporal)"));
			templateFinalList.add(template);
			position++;
		}
	}
    
    public String goToInit() {
		
    	this.initialDate = new Date();
		this.selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
		this.selectedYear = Calendar.getInstance().get(Calendar.YEAR);
		
		if( spreadsheet.getType().equals(SpreadsheetType.WEEK) ) {
			this.selectedDay = spreadsheet.getDay();
		}
		else {
			this.selectedDay = 1;
		}

    	selectedTeam = new Team();
    	selectedTeamFind = new Team();
		
		setSelectedMenu(Constants.MENU_SPREADSHEET);
		
		verifyLeftHeader();
    	return goToList();
    }    
    
    public String find() {
		Pessoa user = getSessionUser();
		Long adviceId = user.getAdvice().getId();			
				
		if(selectedTeamFind.getId() == 0) {
			spreadsheetList = spreadsheetService.findByCompetenceOrdered(selectedMonth, selectedYear, user.getAdvice().getId());
			
			spreadlessList = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO, adviceId, true);
	    	for(Spreadsheet spread:spreadsheetList) {
	    		spreadlessList.remove(spread.getStudent());
			}
		} else {
			spreadsheetList = spreadsheetService.findByCompetenceTeam(selectedMonth, selectedYear, user.getAdvice().getId(), selectedTeamFind.getId());
			
			spreadlessList = pessoaService.getByTipoPessoaAdviceActiveTeam(TipoPessoa.ALUNO, adviceId, true,selectedTeamFind.getId());
	    	for(Spreadsheet spread:spreadsheetList) {
	    		spreadlessList.remove(spread.getStudent());
			}
		}
		this.customerDataModel = new CustomerDataModel(spreadlessList);		
		this.spreadSheetDataModel = new SpreadSheetDataModel(spreadsheetList);
		
		//Lista de Template		    	
		populateTemplateList(adviceId, selectedMonth, selectedYear);
    	
    	return PAGES_SCHEDULE_SPREADSHEET_LIST;
    }
    
    public List<Pessoa> getSpreadless() {
    	return spreadlessList;
    }
    
    public String goToModify() {
    	Pessoa logUser = getSessionUser();
    	
    	this.spreadsheet = spreadsheetService.loadById(spreadsheet.getId());
    	
		if ( spreadsheet==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
    	
    	this.selectedMonth = spreadsheet.getMonth();
    	this.selectedYear = spreadsheet.getYear();    	
		this.selectedDay = spreadsheet.getDay();
		this.selectedType = spreadsheet.getType().getId();
    	
    	this.schedule = new Schedule();
    	this.route = new Route();
    	
    	GregorianCalendar templateDate = new GregorianCalendar();
    	templateDate.set(GregorianCalendar.DAY_OF_MONTH, 1);
    	templateDate.set(GregorianCalendar.MONTH, selectedMonth);
    	templateDate.set(GregorianCalendar.YEAR, selectedYear);
    	templateDate.setTimeZone( TimeZone.getTimeZone("America/Sao_Paulo") );
    	if( spreadsheet.getType().equals(SpreadsheetType.WEEK) ) {
    		templateDate.set(Calendar.DAY_OF_MONTH, selectedDay);
		}
		else {
			templateDate.set(Calendar.DAY_OF_MONTH, 1);
		}
    	
    	this.initialDate = templateDate.getTime();

		//Lista de Rotas
		List<Route> routes = routeService.getByAdvice(logUser.getAdvice().getId());
		routeList = new ArrayList<SelectItem>();
    	for( Route route: routes ) {
    		routeList.add(new SelectItem(route.getId(),route.getName()));	
		} 
    	
    	verifyLeftHeader();
    	return PAGES_SCHEDULE_SPREADSHEET;
    }
    
    public void goToReportCalendar() {
    	goToReport(ReportServiceImpl.PLANILHA_REPORT);
    }
    
    public void goToReportList() {
    	goToReport(ReportServiceImpl.PLANILHA_ALUNO_MES_LISTAGEM);
    }
    
    private void goToReport(int planilha) {
    	Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;
		spreadsheet = spreadsheetService.loadById(spreadsheet.getId());
		
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("PLANILHA_ID", spreadsheet.getId().intValue());
    	param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de treino de "
    	+spreadsheet.getStudent().getNome()+" - "
    	+DateUtils.mesPorExtenso(selectedMonth));
    	
    	if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}
    	
    	if(!reportService.gerar(planilha, param, null, usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}
    }
    
    public void goToPrintReportTurmaCalendar() {
    	goToPrintReportTurma(ReportServiceImpl.PLANILHA_TURMA_REPORT);
    }
    
    public void goToPrintReportTurmaList() {
    	goToPrintReportTurma(ReportServiceImpl.PLANILHA_TURMA_LISTAGEM);
    }
    
    private void goToPrintReportTurma(int planilha) {
    	Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("TURMA_ID", selectedTeam.getId().intValue());
    	param.put("MES", selectedMonth.toString());
    	param.put("ANO", selectedYear.toString());
    	
    	if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de Treino Turma "+selectedTeam.getDescription());
			usarLogoRunPlanner = false;
		} else {
			param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de Treino RunPlanner.pfd");
			usarLogoRunPlanner = true;
		}
    	
    	if(!reportService.gerar(planilha, param, null, usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}
    }
    
    public String delete() {
    	try { spreadsheetService.deleteById(spreadsheet.getId());
    		addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.delete.sucess");
    	} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
		} 
    	
    	return goToList();
    }
    
    
    //Eventos do Schedule
    public void addEvent(ActionEvent actionEvent) {   
    	if(spreadsheet.getId() == null ) {
             return;
    	}   	
    	   

    	if (route.getId()!=null && route.getId().longValue()!=0) {
    		schedule.setRoute(route);
    	}
    	
    	if(schedule.getId() == null) {
    		schedule = scheduleService.persist(schedule);    		    		
    		spreadsheet.getSchedules().add(schedule);
    		
    		try {
    			this.spreadsheet.setSent(false);
    			spreadsheetService.update(spreadsheet);     
            	
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}
    	}
        else {
        	try {
        		scheduleService.update(schedule); 
        		
        		for (Schedule s: spreadsheet.getSchedules() ) {
        			if (s.getId().longValue()==schedule.getId().longValue()) {
                		spreadsheet.getSchedules().remove(s);   
                		spreadsheet.getSchedules().add(schedule);
        				break;
        			}
        		}
        	} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}
        	
        }  
    	verifyLeftHeader();
    	schedule = new Schedule();  
    	route = new Route();
    }
      
    public void deleteEvent() {    	    	
    	if(schedule.getId() != null) {        	
    		spreadsheet = spreadsheetService.removeSchedule(spreadsheet, schedule);    	
    		
    		try {
    			this.spreadsheet.setSent(false);
    			spreadsheetService.update(spreadsheet);            
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}
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
    	calendar.setTime(selectedDate);
    	calendar.setTimeZone( TimeZone.getTimeZone("America/Sao_Paulo") );
    	calendar.set(Calendar.HOUR_OF_DAY, 10);
    	
    	if ( spreadsheet.getSchedules()!=null ) {
    		for(Schedule s:spreadsheet.getSchedules()) {
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
    	calendar.add(GregorianCalendar.DAY_OF_MONTH, dayDelta);
    	calendar.set(Calendar.HOUR_OF_DAY, 10);
    	
    	Integer eventMonth = calendar.get(Calendar.MONTH);
    	Integer eventYear = calendar.get(Calendar.YEAR);
    	
    	if ( eventMonth.equals(selectedMonth) && eventYear.equals(selectedYear) ) {
    		schedule.setDate(calendar.getTime());
        	try {
        		scheduleService.update(schedule); 
        		
        		try {
        			this.spreadsheet.setSent(false);
        			spreadsheetService.update(spreadsheet);            
        		} catch (EntityNotFoundException e) {
    				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
    				return;
    			}
        		
        	} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}
        	
    	}
    	else {
    		addMessage(FacesMessage.SEVERITY_ERROR, "spreadsheet.template.err.diferent.month");
    	}

    	this.spreadsheet = spreadsheetService.loadById(spreadsheet.getId());
    	
    	verifyLeftHeader();
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

    public void handleSelectChangeType() {

    	spreadsheet.setType( SpreadsheetType.getById(selectedType) );
    	
    }
    
    //Getters and Setters 
    public List<SelectItem> getListaAluno() {
		return customerList;
    }
    
    public List<SelectItem> getTemplateList() {

		return templateList;
    }	
    
	public List<SelectItem> getTeams() {	
		return teamList;
	}
    
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

	public CustonScheduleEvent getSelectedEvent() {
		return selectedEvent;
	}

	public void setSelectedEvent(CustonScheduleEvent selectedEvent) {
		this.selectedEvent = selectedEvent;
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

	public Date getInitialDate() {
		return initialDate;
	}

	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}

	public ScheduleService getScheduleService() {
		return scheduleService;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public ScheduleTemplateService getScheduleTemplateService() {
		return scheduleTemplateService;
	}

	public void setScheduleTemplateService(
			ScheduleTemplateService scheduleTemplateService) {
		this.scheduleTemplateService = scheduleTemplateService;
	}

	public SpreadsheetService getSpreadsheetService() {
		return spreadsheetService;
	}

	public void setSpreadsheetService(SpreadsheetService spreadsheetService) {
		this.spreadsheetService = spreadsheetService;
	}

	public Spreadsheet getSpreadSheet() {
		return spreadsheet;
	}

	public void setSpreadSheet(Spreadsheet spreadSheet) {
		this.spreadsheet = spreadSheet;
	}

	public Date getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(Date applyDate) {
		this.applyDate = applyDate;
	}

	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	}

	public void setSpreadsheet(Spreadsheet spreadsheet) {
		this.spreadsheet = spreadsheet;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public List<Spreadsheet> getSpreadsheetList() {
		return spreadsheetList;
	}

	public void setSpreadsheetList(List<Spreadsheet> spreadsheetList) {
		this.spreadsheetList = spreadsheetList;
	}

	public Team getSelectedTeam() {
		return selectedTeam;
	}

	public void setSelectedTeam(Team selectedTeam) {
		this.selectedTeam = selectedTeam;
	}

	public RhythmService getRhythmService() {
		return rhythmService;
	}

	public void setRhythmService(RhythmService rhythmService) {
		this.rhythmService = rhythmService;
	}

	public ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	public TeamService getTeamService() {
		return teamService;
	}

	public void setTeamService(TeamService teamService) {
		this.teamService = teamService;
	}

	public Team getSelectedTeamFind() {
		return selectedTeamFind;
	}

	public void setSelectedTeamFind(Team selectedTeamFind) {
		this.selectedTeamFind = selectedTeamFind;
	}

	public Spreadsheet[] getSelectedSpreadsheetList() {
		return selectedSpreadsheetList;
	}

	public void setSelectedSpreadsheetList(Spreadsheet[] selectedSpreadsheetList) {
		this.selectedSpreadsheetList = selectedSpreadsheetList;
	}

	public SpreadSheetDataModel getSpreadSheetDataModel() {
		return spreadSheetDataModel;
	}

	public void setSpreadSheetDataModel(SpreadSheetDataModel spreadSheetDataModel) {
		this.spreadSheetDataModel = spreadSheetDataModel;
	}

	public List<Pessoa> getSpreadlessList() {
		return spreadlessList;
	}

	public void setSpreadlessList(List<Pessoa> spreadlessList) {
		this.spreadlessList = spreadlessList;
	}

	public CustomerDataModel getCustomerDataModel() {
		return customerDataModel;
	}

	public void setCustomerDataModel(CustomerDataModel customerDataModel) {
		this.customerDataModel = customerDataModel;
	}

	public Pessoa[] getSelectedSpreadsheetListLess() {
		return selectedSpreadsheetListLess;
	}

	public void setSelectedSpreadsheetListLess(Pessoa[] selectedSpreadsheetListLess) {
		this.selectedSpreadsheetListLess = selectedSpreadsheetListLess;
	}

	public Integer getSelectedDay() {
		return selectedDay;
	}

	public void setSelectedDay(Integer selectedDay) {
		this.selectedDay = selectedDay;
	}

	public List<SelectItem> getTypeList() {
		return typeList;
	}

	public String getType() {
		if ( spreadsheet==null || selectedType==SpreadsheetType.MONTH.getId() ) {
			return "month";
		}
		else {
			return "basicWeek";
		}
	}
	
	 @SuppressWarnings("deprecation")
	private void verifyLeftHeader() {
	    	leftHeaderTemplate = "";
	    	if ( spreadsheet.getSchedules()==null || spreadsheet.getSchedules().size()==0 ) return;
	    	
	    	if (spreadsheet.getSchedules().get(0).getDate()==null) return;
	    	
	    	int firstMonth = spreadsheet.getSchedules().get(0).getDate().getMonth();  
	    	for(Schedule schedule:spreadsheet.getSchedules()) {
	    		int month = schedule.getDate().getMonth();
	    		if (month!=firstMonth) {
	    			leftHeaderTemplate = LEFT_HEADER_TEMPLATE_MULTI;
	    			return;
	    		}
	    	}
	    }
	
	public String getLeftHeaderTemplate() {
		
		return leftHeaderTemplate;
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

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}
	
	/*private void checkNullRoutesOnSchedule(Spreadsheet spreadsheet) {
		for (Schedule schedule : spreadsheet.getSchedules()) {
			if((schedule.getRoute() != null) && (schedule.getRoute().getId() == -1)){
				schedule.setRoute(null);
			}
		}
	}*/
}
