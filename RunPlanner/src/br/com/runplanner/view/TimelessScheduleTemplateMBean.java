package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.jsf.FacesContextUtils;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.RhythmTable;
import br.com.runplanner.domain.Route;
import br.com.runplanner.domain.Schedule;
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
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.service.TimelessScheduleTemplateService;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;

@Component("timelessScheduleTemplateMBean")
@Scope("session")
public class TimelessScheduleTemplateMBean extends BasicMBean {

	private static final String PAGES_TIMELESS_SCHEDULE_TEMPLATE = "/pages/schedule/timelessScheduleTemplate";
	private static final String PAGES_TIMELESS_SCHEDULE_SCHEDULE_TEMPLATE_DRISTRIBUTION = "/pages/schedule/timelessScheduleTemplateDistribution";
	private static final String OPERACAO_INCLUIR = "INCLUIR";
	private static final String OPERACAO_EDITAR = "EDITAR";

	private TimelessScheduleTemplateService timelessScheduleTemplateService;
	private List<TimelessScheduleTemplate> timelessScheduleTemplateList;

	private TimelessScheduleTemplate timelessScheduleTemplate;
	private TimelessScheduleWeek timelessScheduleWeek;
	private TimelessScheduleWeek timelessScheduleWeekBackUp;
	private int timelessScheduleWeekBackUpId;

	private List<SelectItem> routeList;
	private RouteService routeService;
	private TimelessScheduleWeek selectedRow;
	private String operacao;

	// Distribution
	private Pessoa customerDist;
	private Team teamDist;
	private RhythmTable rhythmDist;
	private Boolean sendEmail = Boolean.FALSE;
	private List<SelectItem> teamList = new ArrayList<SelectItem>();
	private List<SelectItem> customerList = new ArrayList<SelectItem>();
	private List<SelectItem> rhythmList = new ArrayList<SelectItem>();
	private Date initialDate;

	private RhythmService rhythmService;
	private TeamService teamService;
	private PessoaService pessoaService;
	private SpreadsheetService spreadsheetService;
	private ReportService reportService;
	private EmailThreadProductor emailThreadProductor;
	
	private int LINE_SIZE = 7;
	private int firstNumber = 1;
	private int secondNumber = 2;
	private int thirdNumber = 3;
	private int fourthNumber = 4;
	private int fifthNumber = 5;
	private int sixthNumber = 6;
	private int seventhNumber = 7;
	
	private Route route1 = new Route();
	private Route route2 = new Route();
	private Route route3 = new Route();
	private Route route4 = new Route();
	private Route route5 = new Route();
	private Route route6 = new Route();
	private Route route7 = new Route();
	
	private int selectedDay = 0;
	

	@Autowired
	public TimelessScheduleTemplateMBean(
			TimelessScheduleTemplateService timelessScheduleTemplateService,
			RouteService routeService, RhythmService rhythmService,
			TeamService teamService, PessoaService pessoaService,
			SpreadsheetService spreadsheetService,
			ReportService reportService,
			EmailThreadProductor emailThreadProductor) {

		this.timelessScheduleTemplateService = timelessScheduleTemplateService;
		this.routeService = routeService;
		this.timelessScheduleTemplate = new TimelessScheduleTemplate();
		this.rhythmService = rhythmService;
		this.teamService = teamService;
		this.pessoaService = pessoaService;
		this.spreadsheetService = spreadsheetService;
		this.reportService = reportService;
		this.emailThreadProductor = emailThreadProductor;
		
		route1 = new Route();
		route2 = new Route();
		route3 = new Route();
		route4 = new Route();
		route5 = new Route();
		route6 = new Route();
		route7 = new Route();
	}

	@Override
	public String goToList() {
		/*this.timelessScheduleTemplateList = this.timelessScheduleTemplateService
				.findByAdvice(getSessionUser().getAdvice().getId());
		this.operacao = OPERACAO_INCLUIR;
		
		setSelectedMenu(Constants.MENU_TIMELESS_TEMPLATE);
		return PAGES_TIMELESS_SCHEDULE_TEMPLATE_LIST;*/
		route1 = new Route();
		route2 = new Route();
		route3 = new Route();
		route4 = new Route();
		route5 = new Route();
		route6 = new Route();
		route7 = new Route();
		
		ScheduleTemplateMBean scheduleTemplateBean = (ScheduleTemplateMBean)FacesContextUtils.getWebApplicationContext( FacesContext.getCurrentInstance() ).getBean("scheduleTemplateBean");
		return scheduleTemplateBean.goToList();
	}
	
	public String cloneTemplate() {    	
    	this.timelessScheduleTemplate = timelessScheduleTemplateService.loadById(timelessScheduleTemplate.getId());
    	try {
			timelessScheduleTemplateService.cloneTemplate(timelessScheduleTemplate);
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
    	
		addMessage(FacesMessage.SEVERITY_INFO, "schedule.template.clone.sucess");
    	
    	return goToList();
    }

	@Override
	public String goToCreate() {
		this.timelessScheduleTemplate = new TimelessScheduleTemplate();
		this.timelessScheduleTemplate.setAdvice(this.getSessionUser().getAdvice());
		this.timelessScheduleWeek = new TimelessScheduleWeek();

		loadRoutes();
		
		firstNumber = 1;
		secondNumber = 2;
		thirdNumber = 3;
		fourthNumber = 4;
		fifthNumber = 5;
		sixthNumber = 6;
		seventhNumber = 7;
		
		route1 = new Route();
		route2 = new Route();
		route3 = new Route();
		route4 = new Route();
		route5 = new Route();
		route6 = new Route();
		route7 = new Route();
		
		this.operacao = OPERACAO_INCLUIR;

		return PAGES_TIMELESS_SCHEDULE_TEMPLATE;
	}

	@Override
	public String goToModify() {
		this.timelessScheduleTemplate = timelessScheduleTemplateService.loadById(this.timelessScheduleTemplate.getId());
		this.timelessScheduleWeek = new TimelessScheduleWeek();
		
		route1 = new Route();
		route2 = new Route();
		route3 = new Route();
		route4 = new Route();
		route5 = new Route();
		route6 = new Route();
		route7 = new Route();

		this.operacao = OPERACAO_INCLUIR;

		loadRoutes();
		
		calculateNumbers();
		

		return PAGES_TIMELESS_SCHEDULE_TEMPLATE;
	}
	
	public String goToDistribution() {
		this.timelessScheduleTemplate = timelessScheduleTemplateService
				.loadById(this.timelessScheduleTemplate.getId());

		Pessoa user = getSessionUser();
    	Advice advice = user.getAdvice();
    	
		customerDist = new Pessoa();
		teamDist = new Team();
		rhythmDist = new RhythmTable();
		initialDate = null;
				
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
    	
    	route1 = new Route();
		route2 = new Route();
		route3 = new Route();
		route4 = new Route();
		route5 = new Route();
		route6 = new Route();
		route7 = new Route();
		
		return PAGES_TIMELESS_SCHEDULE_SCHEDULE_TEMPLATE_DRISTRIBUTION;
	}
	
	public void goToReport() {
		Pessoa loged = getSessionUser();
    	boolean usarLogoRunPlanner;
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	    	
    	if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
    		param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
    				.getLogo()));
    		usarLogoRunPlanner = false;
    	} else {
    		usarLogoRunPlanner = true;
    	}
    	timelessScheduleTemplate = timelessScheduleTemplateService.loadById(timelessScheduleTemplate.getId());
    	param.put("TEMPLATE_ID", timelessScheduleTemplate.getId().intValue());
    	param.put(ReportServiceImpl.NOME_ARQUIVO, timelessScheduleTemplate.getTitle());
    	
    	if(!reportService.gerar(ReportServiceImpl.TIMELESS_TEMPLATES_ADVICE_REPORT_LISTAGEM, param, null, 
    			usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}
    }

	private void loadRoutes() {
		List<Route> routes = routeService.getByAdvice(this.getSessionUser().getAdvice().getId());
		routeList = new ArrayList<SelectItem>();
		for (Route route : routes) {
			routeList.add(new SelectItem(route.getId(), route.getName()));
		}
	}

	public String goToInclude() {
		
		if ( isAllNullWeek(timelessScheduleWeek) ) {
			addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.template.empty");
			return "";
		}
		
		updateWeek();
		
		timelessScheduleTemplate.getWeeks().add(timelessScheduleWeek);
		timelessScheduleWeek.setTimelessTemplate(timelessScheduleTemplate);
		timelessScheduleWeek = new TimelessScheduleWeek();
		
		route1 = new Route();
		route2 = new Route();
		route3 = new Route();
		route4 = new Route();
		route5 = new Route();
		route6 = new Route();
		route7 = new Route();

		calculateNumbers();
		
		this.operacao = OPERACAO_INCLUIR;
		return PAGES_TIMELESS_SCHEDULE_TEMPLATE;
	}

	public String goToEdit() {		
		updateWeek();
		
		for (TimelessScheduleWeek tw : timelessScheduleTemplate.getWeeks()) {
			if (tw.equals(timelessScheduleWeekBackUp)) {
				int index = timelessScheduleTemplate.getWeeks().indexOf(tw);
				timelessScheduleTemplate.getWeeks().set(index, timelessScheduleWeek.copy());
				/*timelessScheduleTemplate.getWeeks().remove(tw);
				timelessScheduleTemplate.getWeeks().add(timelessScheduleWeek);*/
				break;
			}
		}
		
		if ( isAllNullWeek(timelessScheduleWeek) ) {
			addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.template.empty");
			return "";
		}
		
		timelessScheduleWeek = new TimelessScheduleWeek();
		route1 = new Route();
		route2 = new Route();
		route3 = new Route();
		route4 = new Route();
		route5 = new Route();
		route6 = new Route();
		route7 = new Route();

		calculateNumbers();
		
		this.operacao = OPERACAO_INCLUIR;
		return PAGES_TIMELESS_SCHEDULE_TEMPLATE;
	}

	public String goToCancel() {
		timelessScheduleWeek = new TimelessScheduleWeek();
		this.operacao = OPERACAO_INCLUIR;
		this.timelessScheduleTemplate.getWeeks().set(timelessScheduleWeekBackUpId, timelessScheduleWeekBackUp);
		
		calculateNumbers();
		return PAGES_TIMELESS_SCHEDULE_TEMPLATE;
	}

	@Override
	public String delete() {
		try {
			timelessScheduleTemplateService.deleteById(timelessScheduleTemplate.getId());
			addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.delete.sucess");
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR,	"template.msg.entitynotfound.delete");
		}
		return goToList();
	}

	public void deleteWeek() {
		
		List<TimelessScheduleWeek> weeks = timelessScheduleTemplate.getWeeks();
		for(TimelessScheduleWeek week:weeks) {
			if ( week.equals(selectedRow) ) {
				weeks.remove(week);
				break;
			}
		}
		
		if (selectedRow.getId()!=null) { //Ja foi pro banco
			try {
				timelessScheduleTemplateService.deleteWeek(selectedRow);
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
				addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");
			}
			timelessScheduleTemplate = timelessScheduleTemplateService.loadById(this.timelessScheduleTemplate.getId());
		}

		timelessScheduleTemplate.setWeeks(weeks);
		
		calculateNumbers();
	}
		
	public void deleteDay1() {
		this.getTimelessScheduleWeek().setFirstDay(new Schedule());	
		this.route1 = new Route();
	}
	
	public void deleteDay2() {
		this.getTimelessScheduleWeek().setSecondDay(new Schedule());	
		this.route2 = new Route();
	}
	
	public void deleteDay3() {
		this.getTimelessScheduleWeek().setThirdDay(new Schedule());
		this.route3 = new Route();
	}
	
	public void deleteDay4() {
		this.getTimelessScheduleWeek().setFourthDay(new Schedule());
		this.route4 = new Route();
	}
	
	public void deleteDay5() {
		this.getTimelessScheduleWeek().setFifthDay(new Schedule());	
		this.route5 = new Route();
	}
	
	public void deleteDay6() {
		this.getTimelessScheduleWeek().setSixthDay(new Schedule());	
		this.route6 = new Route();
	}
	
	public void deleteDay7() {
		this.getTimelessScheduleWeek().setSeventhDay(new Schedule());
		this.route7 = new Route();
	}

	private void calculateNumbers() {
		int qtdSemanas = getTimelessScheduleTemplate().getWeeks().size()*LINE_SIZE;
		firstNumber = qtdSemanas + 1;
		secondNumber = qtdSemanas + 2;
		thirdNumber = qtdSemanas + 3;
		fourthNumber = qtdSemanas + 4;
		fifthNumber = qtdSemanas + 5;
		sixthNumber = qtdSemanas + 6;
		seventhNumber = qtdSemanas + 7;
	}

	public void modifyWeek() {
		this.operacao = OPERACAO_EDITAR;
		int count = 0;
		for (TimelessScheduleWeek tw : timelessScheduleTemplate.getWeeks()) {
			if (tw.equals(selectedRow)) {
				timelessScheduleWeek = tw.copy();
				timelessScheduleWeekBackUp = tw.copy();
				timelessScheduleWeekBackUpId = count;
				
				firstNumber = ((count*LINE_SIZE)) + 1;
				secondNumber = ((count*LINE_SIZE)) + 2;
				thirdNumber = ((count*LINE_SIZE)) + 3;
				fourthNumber = ((count*LINE_SIZE)) + 4;
				fifthNumber = ((count*LINE_SIZE)) + 5;
				sixthNumber = ((count*LINE_SIZE)) + 6;
				seventhNumber = ((count*LINE_SIZE)) + 7;
					
				route1 = applyRoute(timelessScheduleWeek.getFirstDay());
				route2 = applyRoute(timelessScheduleWeek.getSecondDay());
				route3 = applyRoute(timelessScheduleWeek.getThirdDay());
				route4 = applyRoute(timelessScheduleWeek.getFourthDay());
				route5 = applyRoute(timelessScheduleWeek.getFifthDay());
				route6 = applyRoute(timelessScheduleWeek.getSixthDay());
				route7 = applyRoute(timelessScheduleWeek.getSeventhDay());
				
				break;
			}
			count++;
		}
	}
	
	private Route applyRoute(Schedule schedule) {
		if (schedule.getRoute()!=null 
    			&& schedule.getRoute().getId()!=null 
    			&& schedule.getRoute().getId().longValue()!=0) {
    		return schedule.getRoute();
    	}
		else {
			return new Route();
		}
	}
	
	public void updateWeek() {
		
		if (route1.getId()!=null && route1.getId().longValue()!=0) {
			timelessScheduleWeek.getFirstDay().setRoute(route1);
    	}
		
		if (route2.getId()!=null && route2.getId().longValue()!=0) {
			timelessScheduleWeek.getSecondDay().setRoute(route2);
    	}
		
		if (route3.getId()!=null && route3.getId().longValue()!=0) {
			timelessScheduleWeek.getThirdDay().setRoute(route3);
    	}
		
		if (route4.getId()!=null && route4.getId().longValue()!=0) {
			timelessScheduleWeek.getFourthDay().setRoute(route4);
    	}
		
		if (route5.getId()!=null && route5.getId().longValue()!=0) {
			timelessScheduleWeek.getFifthDay().setRoute(route5);
    	}
		
		if (route6.getId()!=null && route6.getId().longValue()!=0) {
			timelessScheduleWeek.getSixthDay().setRoute(route6);
    	}
		
		if (route7.getId()!=null && route7.getId().longValue()!=0) {
			timelessScheduleWeek.getSeventhDay().setRoute(route7);
    	}
	}

	@Override
	public String save() {
		checkNullRoutesOnTimelessSchedule();
		checkNullSchedules();
		
		if ( isAllNullSchedules() ) {
			addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.template.empty");
			return "";
		}
		
		if (timelessScheduleTemplate.getId() == null) {
			timelessScheduleTemplateService.persist(timelessScheduleTemplate);
			addMessage(FacesMessage.SEVERITY_INFO, "spreadsheet.save.sucess");
		} else {
			try {
				timelessScheduleTemplateService.update(timelessScheduleTemplate);
				addMessage(FacesMessage.SEVERITY_INFO,"spreadsheet.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR,
						"template.msg.entitynotfound.delete");
			}
		}
		return goToDistribution();
	}
	
	private void checkNullSchedules() {
		for (TimelessScheduleWeek week : timelessScheduleTemplate.getWeeks()) {
			if(week.getFirstDay() != null) {
				if(((week.getFirstDay().getDescription() != null) && week.getFirstDay().getDescription().trim().equals(""))
						|| (week.getFirstDay().getDescription() == null)) {
					week.setFirstDay(null);
				}
			}
			if(week.getSecondDay() != null) {
				if(((week.getSecondDay().getDescription() != null) && week.getSecondDay().getDescription().trim().equals(""))
						|| (week.getSecondDay().getDescription() == null)) {
					week.setSecondDay(null);
				}
			}
			if(week.getThirdDay() != null) {
				if(((week.getThirdDay().getDescription() != null) && week.getThirdDay().getDescription().trim().equals(""))
						|| (week.getThirdDay().getDescription() == null)) {
					week.setThirdDay(null);
				}
			}
			if(week.getFourthDay() != null) {
				if(((week.getFourthDay().getDescription() != null) && week.getFourthDay().getDescription().trim().equals(""))
						|| (week.getFourthDay().getDescription() == null)) {
					week.setFourthDay(null);
				}
			}
			if(week.getFifthDay() != null) {
				if(((week.getFifthDay().getDescription() != null) && week.getFifthDay().getDescription().trim().equals(""))
						|| (week.getFifthDay().getDescription() == null)) {
					week.setFifthDay(null);
				}
			}
			if(week.getSixthDay() != null) {
				if(((week.getSixthDay().getDescription() != null) && week.getSixthDay().getDescription().trim().equals(""))
						|| (week.getSixthDay().getDescription() == null)) {
					week.setSixthDay(null);
				}
			}
			if(week.getSeventhDay() != null) {
				if(((week.getSeventhDay().getDescription() != null) && week.getSeventhDay().getDescription().trim().equals(""))
						|| (week.getSeventhDay().getDescription() == null)) {
					week.setSeventhDay(null);
				}
			}
		}
	}

	private boolean isAllNullSchedules() {
		for (TimelessScheduleWeek week : timelessScheduleTemplate.getWeeks()) {
			if ( !isAllNullWeek(week) ) return false;
		}
		return true;
	}

	private boolean isAllNullWeek(TimelessScheduleWeek week) {
		
		if(week.getFirstDay() != null) {
			if ( !(((week.getFirstDay().getDescription() != null) && week.getFirstDay().getDescription().trim().equals(""))
					|| (week.getFirstDay().getDescription() == null)) ) {
				return false;
			}
		}
		if(week.getSecondDay() != null) {
			if( !(((week.getSecondDay().getDescription() != null) && week.getSecondDay().getDescription().trim().equals(""))
					|| (week.getSecondDay().getDescription() == null)) ) {
				return false;
			}
		}
		if(week.getThirdDay() != null) {
			if( !(((week.getThirdDay().getDescription() != null) && week.getThirdDay().getDescription().trim().equals(""))
					|| (week.getThirdDay().getDescription() == null)) ) {
				return false;
			}
		}
		if(week.getFourthDay() != null) {
			if( !(((week.getFourthDay().getDescription() != null) && week.getFourthDay().getDescription().trim().equals(""))
					|| (week.getFourthDay().getDescription() == null)) ) {
				return false;
			}
		}
		if(week.getFifthDay() != null) {
			if( !(((week.getFifthDay().getDescription() != null) && week.getFifthDay().getDescription().trim().equals(""))
					|| (week.getFifthDay().getDescription() == null)) ) {
				return false;
			}
		}
		if(week.getSixthDay() != null) {
			if( !(((week.getSixthDay().getDescription() != null) && week.getSixthDay().getDescription().trim().equals(""))
					|| (week.getSixthDay().getDescription() == null)) ) {
				return false;
			}
		}
		if(week.getSeventhDay() != null) {
			if( !(((week.getSeventhDay().getDescription() != null) && week.getSeventhDay().getDescription().trim().equals(""))
					|| (week.getSeventhDay().getDescription() == null)) ) {
				return false;
			}
		}
		
		return true;
	}
	
	private void checkNullRoutesOnTimelessSchedule() {
		for (TimelessScheduleWeek week : timelessScheduleTemplate.getWeeks()) {
			if ((week.getFirstDay() != null) && (week.getFirstDay().getRoute() != null) && (week.getFirstDay().getRoute().getId()== null) ) {
				week.getFirstDay().setRoute(null);
			}
			if ((week.getSecondDay() != null) && (week.getSecondDay().getRoute() != null) && (week.getSecondDay().getRoute().getId()== null) ) {
				week.getSecondDay().setRoute(null);
			}
			if ((week.getThirdDay() != null) && (week.getThirdDay().getRoute() != null) && (week.getThirdDay().getRoute().getId()== null) ) {
				week.getThirdDay().setRoute(null);
			}
			if ((week.getFourthDay() != null) && (week.getFourthDay().getRoute() != null) && (week.getFourthDay().getRoute().getId()== null) ) {
				week.getFourthDay().setRoute(null);
			}
			if ((week.getFifthDay() != null) && (week.getFifthDay().getRoute() != null) && (week.getFifthDay().getRoute().getId()== null) ) {
				week.getFifthDay().setRoute(null);
			}
			if ((week.getSixthDay() != null) && (week.getSixthDay().getRoute() != null) && (week.getSixthDay().getRoute().getId()== null) ) {
				week.getSixthDay().setRoute(null);
			}
			if ((week.getSeventhDay() != null) && (week.getSeventhDay().getRoute() != null) && (week.getSeventhDay().getRoute().getId()== null) ) {
				week.getSeventhDay().setRoute(null);
			}
		}
	}
	
	/*private void checkNullRoutesOnSchedule(Spreadsheet spreadsheet) {
		for (Schedule schedule : spreadsheet.getSchedules()) {
			if((schedule.getRoute() != null) && (schedule.getRoute().getId() == -1)){
				schedule.setRoute(null);
			}
		}
	}*/

	public String distribution() {
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();

		List<Pessoa> customerList = new ArrayList<Pessoa>();

		// Alunos
		if (customerDist.getId().longValue() != -1) {

			if (customerDist.getId().longValue() == 0) {
				// Todos alunos
				customerList = pessoaService.getByTipoPessoaAdvice(
						TipoPessoa.ALUNO, advice.getId());
			} else {
				// Um aluno
				customerDist = pessoaService.loadById(customerDist.getId());
				customerList.add(customerDist);
			}
		} else if (teamDist.getId().longValue() != -1) {
			Team team = teamService.loadById(teamDist.getId());
			customerList = team.getCustomers();
		} else if (rhythmDist.getId().longValue() != -1) {
			customerList = pessoaService.getByTipoPessoaAdviceClassification(
					TipoPessoa.ALUNO, advice.getId(), rhythmDist.getId());
		}

		if (customerList.size() > 0) {
			applyTemplate(customerList);
			addMessage(FacesMessage.SEVERITY_INFO,
					"schedule.template.distribution.success");
		} else {
			// Planilha nao associada
			addMessage(FacesMessage.SEVERITY_INFO,
					"schedule.template.distribution.none");
		}

		return goToList();
	}

	private void applyTemplate(List<Pessoa> customerList) {

		for (Pessoa customer : customerList) {

			if (!customer.isActive())
				continue;

			Spreadsheet spreadsheet = new Spreadsheet();
			spreadsheet.setStudent(customer);

			RhythmTable rhythm = rhythmService.getByCustomer(customer.getId());
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
					iniDate.setTime(initialDate);
					iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
					schedule.setDate(iniDate.getTime());
					spreadsheet.getSchedules().add(schedule);
				}

				if (!week.getSecondDay().isEmpty()) {
					Schedule schedule = week.getSecondDay().copy();
					schedule.setId(null);
					
					GregorianCalendar iniDate = new GregorianCalendar();
					iniDate.setTime(initialDate);
					iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
					iniDate.add(Calendar.DATE, 1);
					schedule.setDate(iniDate.getTime());
					spreadsheet.getSchedules().add(schedule);
				}

				if (!week.getThirdDay().isEmpty()) {
					Schedule schedule = week.getThirdDay().copy();
					schedule.setId(null);
					
					GregorianCalendar iniDate = new GregorianCalendar();
					iniDate.setTime(initialDate);
					iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
					iniDate.add(Calendar.DATE, 2);
					schedule.setDate(iniDate.getTime());
					spreadsheet.getSchedules().add(schedule);
				}

				if (!week.getFourthDay().isEmpty()) {
					Schedule schedule = week.getFourthDay().copy();
					schedule.setId(null);
					
					GregorianCalendar iniDate = new GregorianCalendar();
					iniDate.setTime(initialDate);
					iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
					iniDate.add(Calendar.DATE, 3);
					schedule.setDate(iniDate.getTime());
					spreadsheet.getSchedules().add(schedule);
				}

				if (!week.getFifthDay().isEmpty()) {
					Schedule schedule = week.getFifthDay().copy();
					schedule.setId(null);
					
					GregorianCalendar iniDate = new GregorianCalendar();
					iniDate.setTime(initialDate);
					iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
					iniDate.add(Calendar.DATE, 4);
					schedule.setDate(iniDate.getTime());
					spreadsheet.getSchedules().add(schedule);
				}

				if (!week.getSixthDay().isEmpty()) {
					Schedule schedule = week.getSixthDay().copy();
					schedule.setId(null);
					
					GregorianCalendar iniDate = new GregorianCalendar();
					iniDate.setTime(initialDate);
					iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
					iniDate.add(Calendar.DATE, 5);
					schedule.setDate(iniDate.getTime());
					spreadsheet.getSchedules().add(schedule);
				}

				if (!week.getSeventhDay().isEmpty()) {
					Schedule schedule = week.getSeventhDay().copy();
					schedule.setId(null);
					
					GregorianCalendar iniDate = new GregorianCalendar();
					iniDate.setTime(initialDate);
					iniDate.add(Calendar.WEEK_OF_MONTH, weekCount);
					iniDate.add(Calendar.DATE, 6);
					schedule.setDate(iniDate.getTime());
					spreadsheet.getSchedules().add(schedule);
				}

				weekCount++;
			}

			GregorianCalendar iniDate = new GregorianCalendar();
			iniDate.setTime(initialDate);
			spreadsheet.setType(SpreadsheetType.MONTH);

			spreadsheet.setYear(iniDate.get(Calendar.YEAR));
			spreadsheet.setMonth(iniDate.get(Calendar.MONTH));
			spreadsheet.setDay(iniDate.get(Calendar.DAY_OF_MONTH));

			spreadsheet.setSent(false);
			
			//checkNullRoutesOnSchedule(spreadsheet);
			spreadsheet = spreadsheetService.persist(spreadsheet);

			// Enviar por email?
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

		emailThreadProductor.enviarMensagem(email,
				Constants.EMAIL_SPREADSHEET_TITLE, messageBody, spreadsheet);

		spreadSheet.setSent(true);
		try {
			spreadsheetService.update(spreadSheet);
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
		}

	}

	public void handleSelectChangeCustomer() {
		teamDist = new Team();
		rhythmDist = new RhythmTable();
	}

	public void handleSelectChangeRhythm() {
		customerDist = new Pessoa();
		teamDist = new Team();
	}

	public void handleSelectChangeTeam() {
		customerDist = new Pessoa();
		rhythmDist = new RhythmTable();
	}

	public TimelessScheduleTemplateService getTimelessScheduleTemplateService() {
		return this.timelessScheduleTemplateService;
	}

	public void setTimelessScheduleTemplateService(
			TimelessScheduleTemplateService timelessScheduleTemplateService) {
		this.timelessScheduleTemplateService = timelessScheduleTemplateService;
	}

	public List<TimelessScheduleTemplate> getTimelessScheduleTemplateList() {
		return this.timelessScheduleTemplateList;
	}

	public void setTimelessScheduleTemplateList(
			List<TimelessScheduleTemplate> timelessScheduleTemplateList) {
		this.timelessScheduleTemplateList = timelessScheduleTemplateList;
	}

	public TimelessScheduleTemplate getTimelessScheduleTemplate() {
		return this.timelessScheduleTemplate;
	}

	public void setTimelessScheduleTemplate(
			TimelessScheduleTemplate timelessScheduleTemplate) {
		this.timelessScheduleTemplate = timelessScheduleTemplate;
	}

	public TimelessScheduleWeek getTimelessScheduleWeek() {
		return this.timelessScheduleWeek;
	}

	public void setTimelessScheduleWeek(
			TimelessScheduleWeek timelessScheduleWeek) {
		this.timelessScheduleWeek = timelessScheduleWeek;
	}

	public List<SelectItem> getRouteList() {
		return routeList;
	}

	public void setRouteList(List<SelectItem> routeList) {
		this.routeList = routeList;
	}

	public RouteService getRouteService() {
		return routeService;
	}

	public void setRouteService(RouteService routeService) {
		this.routeService = routeService;
	}

	public TimelessScheduleWeek getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(TimelessScheduleWeek selectedRow) {
		this.selectedRow = selectedRow;
	}

	public String getOperacao() {
		return operacao;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
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

	public List<SelectItem> getRhythmList() {
		return rhythmList;
	}

	public void setRhythmList(List<SelectItem> rhythmList) {
		this.rhythmList = rhythmList;
	}

	public RhythmService getRhythmService() {
		return rhythmService;
	}

	public void setRhythmService(RhythmService rhythmService) {
		this.rhythmService = rhythmService;
	}

	public TeamService getTeamService() {
		return teamService;
	}

	public void setTeamService(TeamService teamService) {
		this.teamService = teamService;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public Date getInitialDate() {
		return initialDate;
	}

	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}

	public SpreadsheetService getSpreadsheetService() {
		return spreadsheetService;
	}

	public void setSpreadsheetService(SpreadsheetService spreadsheetService) {
		this.spreadsheetService = spreadsheetService;
	}

	public ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	public int getFirstNumber() {
		return firstNumber;
	}

	public int getSecondNumber() {
		return secondNumber;
	}

	public int getThirdNumber() {
		return thirdNumber;
	}

	public int getFourthNumber() {
		return fourthNumber;
	}

	public int getFifthNumber() {
		return fifthNumber;
	}

	public int getSixthNumber() {
		return sixthNumber;
	}

	public int getSeventhNumber() {
		return seventhNumber;
	}

	public Route getRoute1() {
		return route1;
	}

	public void setRoute1(Route route1) {
		this.route1 = route1;
	}

	public Route getRoute2() {
		return route2;
	}

	public void setRoute2(Route route2) {
		this.route2 = route2;
	}

	public Route getRoute3() {
		return route3;
	}

	public void setRoute3(Route route3) {
		this.route3 = route3;
	}

	public Route getRoute4() {
		return route4;
	}

	public void setRoute4(Route route4) {
		this.route4 = route4;
	}

	public Route getRoute5() {
		return route5;
	}

	public void setRoute5(Route route5) {
		this.route5 = route5;
	}

	public Route getRoute6() {
		return route6;
	}

	public void setRoute6(Route route6) {
		this.route6 = route6;
	}

	public Route getRoute7() {
		return route7;
	}

	public void setRoute7(Route route7) {
		this.route7 = route7;
	}

	public int getSelectedDay() {
		return selectedDay;
	}

	public void setSelectedDay(int selectedDay) {
		this.selectedDay = selectedDay;
	}

}
