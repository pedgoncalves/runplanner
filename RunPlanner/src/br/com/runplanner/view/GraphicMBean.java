package br.com.runplanner.view;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.model.chart.CartesianChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.BodyMeasurements;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Spreadsheet;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.BodyMeasurementsService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MessagesResources;

@Component("graphMBean")
@Scope("session")
@SuppressWarnings("deprecation")
public class GraphicMBean {

	//Numero de meses para tras a serem exibidos de inicio no grafico
	private static final int PAST_MONTHS = -6;
	
	//Constants
    private static final String PAGES_STUDENT_PROGRESS_REPORT = "/pages/student/progressReport";
    private static final String PAGES_TEACHER_PROGRESS_REPORT = "/pages/teacher/progressReport";
    
    private static final String PAGES_STUDENT_ACTIVITY_REPORT = "/pages/student/activityReport";
    private static final String PAGES_TEACHER_ACTIVITY_REPORT = "/pages/teacher/activityReport";    
    private static final String PAGES_STUDENT_ACTIVITY_REPORT_WEEK = "/pages/student/activityReportWeek";
    private static final String PAGES_TEACHER_ACTIVITY_REPORT_WEEK = "/pages/teacher/activityReportWeek";
    
    //EvolutionGraphs
	private CartesianChartModel rhythmGraph;
    private CartesianChartModel medicalGraph;  
    private CartesianChartModel weightGraph;
    //ActivityGraphs
    private CartesianChartModel activityGraph;
    private CartesianChartModel activityNumberGraph;
    private CartesianChartModel activityTimeGraph;
    private CartesianChartModel activityDayGraph;
    
    //Services
    private BodyMeasurementsService bodyMeasurementsService;
    private SpreadsheetService spreadsheetService;
	private ActivityService activityService;
	private PessoaService pessoaService;
    
    private List<Spreadsheet> spreadsheetList;
    private List<BodyMeasurements> bodymeasurementsList;
	private List<Activity> activityList;
	private List<Pessoa> customerList;
	private List<SelectItem> customerListSI;
	
	private Date initialDate;
	private Date finalDate;
	private int type = 1; //1 para mensal, 2 para semanal
	private Pessoa student;
	
    
    @Autowired
    public GraphicMBean(SpreadsheetService spreadsheetService,
    		BodyMeasurementsService bodyMeasurementsService,
    		ActivityService activityService,
    		PessoaService pessoaService) {
		this.bodyMeasurementsService=bodyMeasurementsService;
		this.spreadsheetService = spreadsheetService;
		this.activityService = activityService;
		this.pessoaService = pessoaService;
    }
    
    protected void addMessage(Severity severity, String sumaryKey) {  
	    	
    	FacesMessage message = new FacesMessage(severity, 
    			MessagesResources.getStringFromBundle(sumaryKey,""),"");  
    	
        FacesContext.getCurrentInstance().addMessage(null, message);  
    }
    
    public String goToProgress(){

    	Pessoa user = getSessionUser();
    	
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.add(Calendar.MONTH, PAST_MONTHS);
    	
    	initialDate = calendar.getTime();
    	finalDate = new Date();
    	
    	int initialMonth = initialDate.getMonth();
    	int finalMonth = finalDate.getMonth();
    	int initialYear = initialDate.getYear()+1900;
    	int finalYear = finalDate.getYear()+1900;
    	
    	//Progress Tab
    	spreadsheetList = spreadsheetService.findByStudentInterval(user.getId(),initialMonth,finalMonth,initialYear,finalYear);    	
    	bodymeasurementsList = bodyMeasurementsService.findByCustomerIdIntervalAsc(user.getId(),initialDate,finalDate);
    	
    
    	setSelectedMenu(Constants.MENU_GRAPH_PROGRESS);
    	
    	return PAGES_STUDENT_PROGRESS_REPORT;
    }
    
	public String goToProgressTeacher(){
    	Pessoa teacher = getSessionUser();
    	Advice advice = teacher.getAdvice();
    	
    	customerList = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO, advice.getId(), true);
    	if ( customerList==null || customerList.size()<=0 ) {
        	return PAGES_TEACHER_PROGRESS_REPORT;
    	}
    	
    	customerListSI = new ArrayList<SelectItem>();
    	
		for (Pessoa pessoa:customerList) {
			customerListSI.add(new SelectItem(pessoa.getId(),pessoa.getNome()));
		}	
    	
    	student = customerList.get(0);
    	
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.add(Calendar.MONTH, PAST_MONTHS);
    	
    	initialDate = calendar.getTime();
    	finalDate = new Date();
    	
    	int initialMonth = initialDate.getMonth();
    	int finalMonth = finalDate.getMonth();
    	int initialYear = initialDate.getYear()+1900;
    	int finalYear = finalDate.getYear()+1900;
    	
    	//Progress Tab
    	spreadsheetList = spreadsheetService.findByStudentInterval(student.getId(),initialMonth,finalMonth,initialYear,finalYear);	
    	bodymeasurementsList = bodyMeasurementsService.findByCustomerIdIntervalAsc(student.getId(),initialDate,finalDate);
    	
    	setSelectedMenu(Constants.MENU_GRAPH_ACTIVITY);
    	
    	return PAGES_TEACHER_PROGRESS_REPORT;
    }    
    
    public String goToActivity() {

    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.add(Calendar.MONTH, PAST_MONTHS);
    	
    	initialDate = calendar.getTime();
    	finalDate = new Date();
    	type = 1;
    	
    	Pessoa user = getSessionUser();
		activityList = activityService.findByUserIdDateAsc(user.getId(),initialDate,finalDate);
		
    	setSelectedMenu(Constants.MENU_GRAPH_ACTIVITY);
		
    	return getReturnPageActivity(false);
    }
    
    public String goToActivityTeacher() {
    	Pessoa teacher = getSessionUser();
    	Advice advice = teacher.getAdvice();
    	
    	customerList = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO, advice.getId(), true);
    	if ( customerList==null || customerList.size()<=0 ) {
        	return getReturnPageActivity(true);
    	}
    	
    	customerListSI = new ArrayList<SelectItem>();
    	
		for (Pessoa pessoa:customerList) {
			customerListSI.add(new SelectItem(pessoa.getId(),pessoa.getNome()));
		}	
    	
    	student = customerList.get(0);
    	
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.add(Calendar.MONTH, PAST_MONTHS);
    	
    	initialDate = calendar.getTime();
    	finalDate = new Date();
    	type = 1;
    	
		activityList = activityService.findByUserIdDateAsc(student.getId(),initialDate,finalDate);
    	setSelectedMenu(Constants.MENU_GRAPH_ACTIVITY);
		
    	return getReturnPageActivity(true);
    }
    
    public String filterActivity() {
    
    	Pessoa user = getSessionUser();
		activityList = activityService.findByUserIdDateAsc(user.getId(),initialDate,finalDate);
				
    	return getReturnPageActivity(false);
    }
    
    public String filterActivityTeacher() {
    	
		activityList = activityService.findByUserIdDateAsc(student.getId(),initialDate,finalDate);
		
    	return getReturnPageActivity(true);
    }
    
    public String filterProgress() {
        
    	Pessoa user = getSessionUser();
    	
    	int initialMonth = initialDate.getMonth();
    	int finalMonth = finalDate.getMonth();
    	int initialYear = initialDate.getYear()+1900;
    	int finalYear = finalDate.getYear()+1900;
    	
    	//Progress Tab
    	spreadsheetList = spreadsheetService.findByStudentInterval(user.getId(),initialMonth,finalMonth,initialYear,finalYear);  	
    	bodymeasurementsList = bodyMeasurementsService.findByCustomerIdIntervalAsc(user.getId(),initialDate,finalDate);
      	
    	return PAGES_STUDENT_PROGRESS_REPORT;
    }    
    
    public String filterProgressTeacher(){
    	
    	int initialMonth = initialDate.getMonth();
    	int finalMonth = finalDate.getMonth();
    	int initialYear = initialDate.getYear()+1900;
    	int finalYear = finalDate.getYear()+1900;
    	
    	//Progress Tab
    	spreadsheetList = spreadsheetService.findByStudentInterval(student.getId(),initialMonth,finalMonth,initialYear,finalYear);
    	bodymeasurementsList = bodyMeasurementsService.findByCustomerIdIntervalAsc(student.getId(),initialDate,finalDate);
    
    	
    	return PAGES_TEACHER_PROGRESS_REPORT;
    }     
    
    public String getReturnPageActivity(boolean teacher) {
    	
    	if ( type==1 ) {    		
    		if ( teacher ) {
    			return PAGES_TEACHER_ACTIVITY_REPORT;
    		}
    		else {
    			return PAGES_STUDENT_ACTIVITY_REPORT;
    		}    		
    	}
    	else if ( type==2 ) {
    		if ( teacher ) {
    			return PAGES_TEACHER_ACTIVITY_REPORT_WEEK;
    		}
    		else {
    			return PAGES_STUDENT_ACTIVITY_REPORT_WEEK;
    		}
    	}
    	return null;
    	
    }
    
    
    
    
    //Descricao do eixo X para o grafico de pace
  	public String getRhythmGraphTick() {
  		StringBuffer ticks = new StringBuffer("[");
  		
  		if ( spreadsheetList==null || spreadsheetList.size()<=0 ) {      		
          	return "[]";
  		}
 
        
        for ( Spreadsheet spread:spreadsheetList ) {
        	ticks.append( "'"+spread.getTextMonth()+"/"+spread.getYear()+"'" );
        	ticks.append(",");
        }       
  		
  		
      	ticks.deleteCharAt( ticks.length()-1 );
  		ticks.append("]");
  		
  		return ticks.toString();
  	}
  	
	public String getRhythmGraphSerie() {		
		StringBuffer result = new StringBuffer("[");
  		
  		if ( spreadsheetList==null || spreadsheetList.size()<=0 ) {      		
          	return null;
  		}

		StringBuffer easyPace = new StringBuffer("[");
		StringBuffer normalPace = new StringBuffer("[");
		StringBuffer moderatePace = new StringBuffer("[");
		StringBuffer mediumPace = new StringBuffer("[");
		StringBuffer strongPace = new StringBuffer("[");
		StringBuffer shootingPace = new StringBuffer("[");

  		
		for ( Spreadsheet spread:spreadsheetList ) {
        	easyPace.append( stringTimeToNumber(spread.getEasyPace()) + ",");
        	normalPace.append( stringTimeToNumber(spread.getNormalPace()) + ",");
        	moderatePace.append( stringTimeToNumber(spread.getModeratePace()) + ",");
        	mediumPace.append( stringTimeToNumber(spread.getMediumPace()) + ",");
        	strongPace.append( stringTimeToNumber(spread.getStrongPace()) + ",");
        	shootingPace.append( stringTimeToNumber(spread.getShootingPace()) + ",");
        }
		
		easyPace.deleteCharAt( easyPace.length()-1 );
		normalPace.deleteCharAt( normalPace.length()-1 );
		moderatePace.deleteCharAt( moderatePace.length()-1 );
		mediumPace.deleteCharAt( mediumPace.length()-1 );
		strongPace.deleteCharAt( strongPace.length()-1 );
		shootingPace.deleteCharAt( shootingPace.length()-1 );
		
		easyPace.append("]");
    	normalPace.append("]");
    	moderatePace.append("]");
    	mediumPace.append("]");
    	strongPace.append("]");
    	shootingPace.append("]");
		
		result.append( easyPace );
		result.append( "," );
		result.append( normalPace );
		result.append( "," );
		result.append( moderatePace );
		result.append( "," );
		result.append( mediumPace );
		result.append( "," );
		result.append( strongPace );
		result.append( "," );
		result.append( shootingPace );
		
		result.append("]");  		
  		return result.toString();
	}
		
	//Descricao do eixo X para o grafico de peso					 
  	public String getWeightGraphTick() {
  		  		
  		if ( bodymeasurementsList==null || bodymeasurementsList.size()<=0 ) {      		
          	return "[]";
  		}
  		
  		StringBuffer ticks = new StringBuffer("[");
  				
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		for ( BodyMeasurements body:bodymeasurementsList ) {
			ticks.append( "'"+sdf.format(body.getDtData())+"'" );
			ticks.append( "," );
		}  			
		
      	ticks.deleteCharAt( ticks.length()-1 );
  		ticks.append("]");  		
  		return ticks.toString();
  	}
  	

	public String getWeightGraphSerie() {		
		 		
  		if ( bodymeasurementsList==null || bodymeasurementsList.size()<=0 ) {      		
          	return null;
  		}

  		StringBuffer result = new StringBuffer("[");

  		 for ( BodyMeasurements body:bodymeasurementsList ) {
  			result.append( body.getWeight() );
  			result.append(",");
         }
		
  		result.deleteCharAt( result.length()-1 );
		result.append("]");  		
  		return result.toString();
	}  	
	
	//Descricao do eixo X para o grafico medico					 
  	public String getMedicalGraphTick() {
  		  		
  		if ( bodymeasurementsList==null || bodymeasurementsList.size()<=0 ) {      		
          	return "[]";
  		}
  		
  		StringBuffer ticks = new StringBuffer("[");
  				
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		for ( BodyMeasurements body:bodymeasurementsList ) {
			ticks.append( "'"+sdf.format(body.getDtData())+"'" );
			ticks.append( "," );
		}  			
		
      	ticks.deleteCharAt( ticks.length()-1 );
  		ticks.append("]");  		
  		return ticks.toString();
  	}
  	
	public String getMedicalGraphSerie() {		
 		
  		if ( bodymeasurementsList==null || bodymeasurementsList.size()<=0 ) {      		
          	return null;
  		}

  		StringBuffer result = new StringBuffer("[");
  		
  		StringBuffer vo2Max = new StringBuffer("[");
  		StringBuffer heartRateRest = new StringBuffer("[");
  		StringBuffer heartRateMax = new StringBuffer("[");

  		 for ( BodyMeasurements body:bodymeasurementsList ) {

  			vo2Max.append( body.getVo2Max() );
  			vo2Max.append(",");
        	heartRateRest.append( body.getHeartRateRest() );
        	heartRateRest.append(",");
        	heartRateMax.append( body.getHeartRateMax() );
        	heartRateMax.append(",");
         }
  		 
  		vo2Max.deleteCharAt( vo2Max.length()-1 );
  		heartRateRest.deleteCharAt( heartRateRest.length()-1 );
  		heartRateMax.deleteCharAt( heartRateMax.length()-1 );
		
		vo2Max.append("]");
		heartRateRest.append("]");
		heartRateMax.append("]");
		
		result.append( vo2Max );
		result.append( "," );
		result.append( heartRateRest );
		result.append( "," );
		result.append( heartRateMax );
		
  		
		result.append("]");  		
  		return result.toString();
	}  
    
	
	
	//Descricao do eixo X para os graficos de atividade (SEMANAS)
	public String getActivityWeekGraphTick() {
		StringBuffer ticks = new StringBuffer("[");
		
		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
        	return "[]";
        }   	
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM. yy");		
		
		//Monta lista de "Domingos"
    	Date d0 = (Date)initialDate.clone();
    	Calendar calendar = new GregorianCalendar();    		
		calendar.setTime(d0);
		

		if ( calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY ) {
			ticks.append( "'" + sdf.format(d0) + "'");
			ticks.append(",");	
			
			int days = 7-(calendar.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY);
    		calendar.add(Calendar.DAY_OF_MONTH,days); 
    		d0=calendar.getTime();    
		}
		
    	while ( d0.before(finalDate) ) {    			
    		calendar = new GregorianCalendar();    		
    		calendar.setTime(d0);	
    		    		
    		ticks.append( "'" + sdf.format(d0) + "'");
    		ticks.append(",");
    			
    		calendar.add(Calendar.DAY_OF_MONTH,7);
    		   		
    		d0=calendar.getTime();    		
    	} 		
		
    	ticks.deleteCharAt( ticks.length()-1 );
		ticks.append("]");
		
		return ticks.toString();
	}
	
	
	//Descricao do eixo X para os graficos de atividade
	public String getActivityGraphTick() {
		StringBuffer ticks = new StringBuffer("[");
		
		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
        	return "[]";
        }
    	    	    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {
    		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;
    		
    		ticks.append( "'" + new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year + "'");
    		ticks.append(",");
    		
    		GregorianCalendar calendar = new GregorianCalendar();    		
    		calendar.setTime(d0);
    		calendar.set(Calendar.DAY_OF_MONTH,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    	
		
    	ticks.deleteCharAt( ticks.length()-1 );
		ticks.append("]");
		
		return ticks.toString();
	}
	
	// Grafico de Tempo de Atividades por Mes
	public String getActivityTimeGraphSerie() {
        

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
		
		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setMinimumIntegerDigits(2);
		
		Hashtable<String, Double> values = new Hashtable<String, Double>();	
    	    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {
    		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;

    		values.put( new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year , 0d );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    	
    	
    	int month = activityList.get(0).getDate().getMonth();    	
    	int year  = activityList.get(0).getDate().getYear()+1900;
    	
    	double monthValue = 0;
    	for(Activity act:activityList) {    		
    		
    		if ( month == act.getDate().getMonth() ) {    			
    			monthValue += act.getTotalTimeInSeconds();    			
    		} else {    			
        		
    			String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    			values.put(key, (monthValue/60)/60);
    			
    			month = act.getDate().getMonth();
    			year = act.getDate().getYear()+1900;
    			
    			monthValue = act.getTotalTimeInSeconds();    			    			
    		}
    	}    
    	String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    	values.put(key, (double)((double)monthValue/60)/60);
		
		StringBuffer serie = new StringBuffer("[");
		d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {    		
    		        		
    		month = d0.getMonth();
    		year  = d0.getYear()+1900;
    		
    		key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    		
    		Double v = values.get(key);
    		serie.append( v );
			serie.append( "," );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    
				
    	serie.append("]");
    	return serie.toString();   
	}
	
	// Grafico de Tempo de Atividades por Semana
	public String getActivityTimeWeekGraphSerie() {

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Double> values = new Hashtable<String, Double>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		
		List<String> weekList = getWeekList();		
		for(String week:weekList) {
			values.put(week, 0d);
		}   
		
	   	//Buscar atividades a partir das datas
    	Object[] keys = weekList.toArray();
    	int iStart = 0;
    	int iFinal = 1;
    	for ( iStart=0; iStart<keys.length-1; iStart++, iFinal++ ) {
    		    		
    		try {
    			Date startDate = sdf.parse( (String)keys[iStart] );
    			Date finalDate = sdf.parse( (String)keys[iFinal] );

    			double weekValue = 0;
        		for(Activity activity:activityList) {		
        			
        			Date date = activity.getDate();
        			
        			if ( date.equals(startDate) || ( date.after(startDate) && date.before(finalDate) ) ) {
        				weekValue += activity.getTotalTimeInSeconds();
        			}        			
        		}

    			values.put((String)keys[iStart], weekValue/60/60);        		
    			
			} catch (ParseException e) {
				e.printStackTrace();
			}    		
    	}
    	    	
    	try {
    		iFinal = keys.length-1;
    		Date finalDate = sdf.parse( (String)keys[iFinal] );
    		
    		double lastWeekValue = 0;    		
    		for(Activity activity:activityList) {		

    			Date date = activity.getDate();			
    			if ( date.equals(finalDate) || date.after(finalDate) ) {
    				lastWeekValue += activity.getTotalTimeInSeconds();
    			}        			
    		}
    		values.put((String)keys[iFinal], lastWeekValue/60/60);
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    	
    	    			    	
		//Montar lista
		StringBuffer serie = new StringBuffer("[");
		for(int i=0; i<keys.length; i++) {
			String key = (String)keys[i];
			
			Double v = values.get(key);
    		serie.append( v );
			serie.append( "," );
		}
		

		serie.deleteCharAt( serie.length()-1 );
    	serie.append("]");	
    	return serie.toString();  		
		
	}
	// FIM Grafico de Tempo de Atividades por Semana		
	
	
	// Grafico de Distancia de Atividades por Mes
	public String getActivityDistanceGraphSerie() {

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Double> values = new Hashtable<String, Double>();	
    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {
    		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;
    		
    		values.put( new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year , 0d );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}
    	
    	
    	int month = activityList.get(0).getDate().getMonth();    	
    	int year  = activityList.get(0).getDate().getYear()+1900;
    	
    	double monthValue = 0;
    	for(Activity act:activityList) {
    		
    		if ( month == act.getDate().getMonth() && year == act.getDate().getYear()+1900 ) {    			
    			monthValue += act.getTotalDistance();    			
    		} else {    			
    			String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    			values.put(key, monthValue);
    			
    			month = act.getDate().getMonth();
    			year = act.getDate().getYear()+1900;
    			monthValue = act.getTotalDistance();    			    			
    		}
    	}    	
    	String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		values.put(key, monthValue);
    	
		StringBuffer serie = new StringBuffer("[");
		d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {    		
    		        		
    		month = d0.getMonth();
    		year  = d0.getYear()+1900;
    		
    		key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    		
    		Double v = values.get(key);
    		serie.append( v );
			serie.append( "," );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    
    	key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		
		Double v = values.get(key);
		serie.append( v );
				
    	serie.append("]");
    	return serie.toString();  		
		
	}
	// FIM Grafico de Distancia de Atividades por Mes
	
	// Grafico de Calorias de Atividades por Mes
	public String getActivityEnergyGraphSerie() {

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Double> values = new Hashtable<String, Double>();	
    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {
    		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;
    		
    		values.put( new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year , 0d );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}
    	
    	
    	int month = activityList.get(0).getDate().getMonth();    	
    	int year  = activityList.get(0).getDate().getYear()+1900;
    	
    	double monthValue = 0;
    	for(Activity act:activityList) {
    		
    		if ( month == act.getDate().getMonth() && year == act.getDate().getYear()+1900 ) {    			
    			monthValue += act.getTotalCalories();    			
    		} else {    			
    			String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    			values.put(key, monthValue);
    			
    			month = act.getDate().getMonth();
    			year = act.getDate().getYear()+1900;
    			monthValue = act.getTotalCalories();    			    			
    		}
    	}    	
    	String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		values.put(key, monthValue);
    	
		StringBuffer serie = new StringBuffer("[");
		d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {    		
    		        		
    		month = d0.getMonth();
    		year  = d0.getYear()+1900;
    		
    		key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    		
    		Double v = values.get(key);
    		serie.append( v );
			serie.append( "," );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    
    	key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		
		Double v = values.get(key);
		serie.append( v );
				
    	serie.append("]");
    	return serie.toString();  		
		
	}
	// FIM Grafico de Calorias de Atividades por Mes	
	
	// Grafico de Distancia de Atividades por Semana
	public String getActivityDistanceWeekGraphSerie() {

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Double> values = new Hashtable<String, Double>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		
		List<String> weekList = getWeekList();		
		for(String week:weekList) {
			values.put(week, 0d);
		}   
		
	   	//Buscar atividades a partir das datas
    	Object[] keys = weekList.toArray();
    	int iStart = 0;
    	int iFinal = 1;
    	for ( iStart=0; iStart<keys.length-1; iStart++, iFinal++ ) {
    		    		
    		try {
    			Date startDate = sdf.parse( (String)keys[iStart] );
    			Date finalDate = sdf.parse( (String)keys[iFinal] );

    			double weekValue = 0;
        		for(Activity activity:activityList) {		
        			
        			Date date = activity.getDate();
        			
        			if ( date.equals(startDate) || ( date.after(startDate) && date.before(finalDate) ) ) {
        				weekValue += activity.getTotalDistance();
        			}        			
        		}

    			values.put((String)keys[iStart], weekValue);        		
    			
			} catch (ParseException e) {
				e.printStackTrace();
			}    		
    	}
    	    	
    	try {
    		iFinal = keys.length-1;
    		Date finalDate = sdf.parse( (String)keys[iFinal] );
    		
    		double lastWeekValue = 0;    		
    		for(Activity activity:activityList) {		

    			Date date = activity.getDate();			
    			if ( date.equals(finalDate) || date.after(finalDate) ) {
    				lastWeekValue += activity.getTotalDistance();
    			}        			
    		}
    		values.put((String)keys[iFinal], lastWeekValue);
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    	
    	    			    	
		//Montar lista
		StringBuffer serie = new StringBuffer("[");
		for(int i=0; i<keys.length; i++) {
			String key = (String)keys[i];
			
			Double v = values.get(key);
    		serie.append( v );
			serie.append( "," );
		}
		

		serie.deleteCharAt( serie.length()-1 );
    	serie.append("]");	
    	return serie.toString();  		
		
	}
	// FIM Grafico de Distancia de Atividades por Semana	
	
	// Grafico de Calorias de Atividades por Semana
	public String getActivityEnergyWeekGraphSerie() {

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Double> values = new Hashtable<String, Double>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		
		List<String> weekList = getWeekList();		
		for(String week:weekList) {
			values.put(week, 0d);
		}   
		
	   	//Buscar atividades a partir das datas
    	Object[] keys = weekList.toArray();
    	int iStart = 0;
    	int iFinal = 1;
    	for ( iStart=0; iStart<keys.length-1; iStart++, iFinal++ ) {
    		    		
    		try {
    			Date startDate = sdf.parse( (String)keys[iStart] );
    			Date finalDate = sdf.parse( (String)keys[iFinal] );

    			double weekValue = 0;
        		for(Activity activity:activityList) {		
        			
        			Date date = activity.getDate();
        			
        			if ( date.equals(startDate) || ( date.after(startDate) && date.before(finalDate) ) ) {
        				weekValue += activity.getTotalCalories();
        			}        			
        		}

    			values.put((String)keys[iStart], weekValue);        		
    			
			} catch (ParseException e) {
				e.printStackTrace();
			}    		
    	}
    	    	
    	try {
    		iFinal = keys.length-1;
    		Date finalDate = sdf.parse( (String)keys[iFinal] );
    		
    		double lastWeekValue = 0;    		
    		for(Activity activity:activityList) {		

    			Date date = activity.getDate();			
    			if ( date.equals(finalDate) || date.after(finalDate) ) {
    				lastWeekValue += activity.getTotalCalories();
    			}        			
    		}
    		values.put((String)keys[iFinal], lastWeekValue);
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    	
    	    			    	
		//Montar lista
		StringBuffer serie = new StringBuffer("[");
		for(int i=0; i<keys.length; i++) {
			String key = (String)keys[i];
			
			Double v = values.get(key);
    		serie.append( v );
			serie.append( "," );
		}
		

		serie.deleteCharAt( serie.length()-1 );
    	serie.append("]");	
    	return serie.toString();  		
		
	}
	// FIM Grafico de Calorias de Atividades por Semana		
	
	// Grafico de Numero de Atividades por SEMANA	                 
	public String getActivityNumberWeekGraphSerie() {		
		
    	if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Integer> values = new Hashtable<String, Integer>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		
		List<String> weekList = getWeekList();		
		for(String week:weekList) {
			values.put(week, 0);
		}    	
    	
    	//Buscar atividades a partir das datas
    	Object[] keys = weekList.toArray();
    	int iStart = 0;
    	int iFinal = 1;
    	for ( iStart=0; iStart<keys.length-1; iStart++, iFinal++ ) {
    		    		
    		try {
    			Date startDate = sdf.parse( (String)keys[iStart] );
    			Date finalDate = sdf.parse( (String)keys[iFinal] );

    			int weekValue = 0;
        		for(Activity activity:activityList) {		
        			
        			Date date = activity.getDate();
        			
        			if ( date.equals(startDate) || ( date.after(startDate) && date.before(finalDate) ) ) {
        				weekValue++;
        			}        			
        		}

    			values.put((String)keys[iStart], weekValue);        		
    			
			} catch (ParseException e) {
				e.printStackTrace();
			}    		
    	}
    	    	
    	try {
    		iFinal = keys.length-1;
    		Date finalDate = sdf.parse( (String)keys[iFinal] );
    		
    		int lastWeekValue = 0;    		
    		for(Activity activity:activityList) {		

    			Date date = activity.getDate();			
    			if ( date.equals(finalDate) || date.after(finalDate) ) {
    				lastWeekValue++;
    			}        			
    		}
    		values.put((String)keys[iFinal], lastWeekValue);
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    	
    	    			    	
		//Montar lista
		StringBuffer serie = new StringBuffer("[");
		for(int i=0; i<keys.length; i++) {
			String key = (String)keys[i];
			
			Integer v = values.get(key);
    		serie.append( v );
			serie.append( "," );
		}
		

		serie.deleteCharAt( serie.length()-1 );
    	serie.append("]");
    	return serie.toString();
	}
	// FIM Grafico de Numero de Atividades por SEamana

	/**
	 * Busca a lista de semanas a partir da initialDate
	 * @return
	 */
	private List<String> getWeekList() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		List<String> keysList = new ArrayList<String>();
		
		//Monta lista de "domingos"
    	Date d0 = (Date)initialDate.clone();
		Calendar calendar = new GregorianCalendar();    		
		calendar.setTime(d0);
    	
		if ( calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY ) {			
			keysList.add(sdf.format(d0));
			
			int days = 7-(calendar.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY);
    		calendar.add(Calendar.DAY_OF_MONTH,days);   
    		d0=calendar.getTime();    
		}
		
    	while ( d0.before(finalDate) ) {    			
    		calendar = new GregorianCalendar();    		
    		calendar.setTime(d0);    	
    					
			keysList.add(sdf.format(d0));
			
			calendar.add(Calendar.DAY_OF_MONTH,7);		
    		   		
    		d0=calendar.getTime();    		
    	}
		return keysList;
	}
	
	
	// Grafico de Numero de Atividades por Mes
	public String getActivityNumberGraphSerie() {		
		
    	if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Integer> values = new Hashtable<String, Integer>();	
    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {    		
    		        		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;
    		
    		values.put(new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year, Integer.valueOf(0) );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    	
    	
    	int month = activityList.get(0).getDate().getMonth();  
    	int year  = activityList.get(0).getDate().getYear()+1900;  	
    	
    	int monthValue = 0;
    	for(Activity act:activityList) {
    		
    		if ( month == act.getDate().getMonth() && year == act.getDate().getYear()+1900 ) {    			
    			monthValue ++;    			
    		} else {    			
    			
    			String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    			values.put(key, monthValue);
    			
    			month = act.getDate().getMonth();
    			year = act.getDate().getYear()+1900;
    			monthValue = 1;    			    			
    		}
    	}    	
    	String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		values.put(key, monthValue);
		    	
		
		StringBuffer serie = new StringBuffer("[");
		d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {    		
    		        		
    		month = d0.getMonth();
    		year  = d0.getYear()+1900;
    		
    		key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    		
    		Integer v = values.get(key);
    		serie.append( v );
			serie.append( "," );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    
    	key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		
		Integer v = values.get(key);
		serie.append( v );
				
    	serie.append("]");
    	return serie.toString();
	}
	// FIM Grafico de Numero de Atividades por Mes
	
	// Grafico de Pace de Atividades por Mes
	public String getActivityPaceGraphSerie() {
        

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setMinimumIntegerDigits(2);
		
		Hashtable<String, String> values = new Hashtable<String, String>();	
    	    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {
    		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;

    		values.put( new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year , "2000-01-01 00:00:00" );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    	
    	
    	int month = activityList.get(0).getDate().getMonth();    	
    	int year  = activityList.get(0).getDate().getYear()+1900;
    	
    	double monthTimeSeconds = 0;
    	double monthDistanceKm = 0;
    	for(Activity act:activityList) {    		
    		
    		if ( month == act.getDate().getMonth() ) {    			
    			monthTimeSeconds += act.getTotalTimeInSeconds();
    			monthDistanceKm += act.getTotalDistance();
    		} else {    			
  
   				int ss = (int)monthTimeSeconds % 60;  
   				monthTimeSeconds /= 60;  
   				int mm = (int)monthTimeSeconds % 60;  
   				monthTimeSeconds /= 60;  
   				int hh = (int)monthTimeSeconds % 24;     		
    			
   				double value = ((hh*60*60) + (mm*60) + ss)/monthDistanceKm; //Em segundos    
   				ss = (int)value % 60;  
   				value /= 60;  
   				mm = (int)value % 60;  
   				value /= 60;  
   				hh = (int)value % 24;   			
    			
    			String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    			values.put(key, "2000-01-01 "+decFormat.format(mm)+":"+decFormat.format(ss)+":00");
    			
    			month = act.getDate().getMonth();
    			year = act.getDate().getYear()+1900;
    			
    			monthTimeSeconds = act.getTotalTimeInSeconds();  
    			monthDistanceKm = act.getTotalDistance();
    		}
    	}    
    	int ss = (int)monthTimeSeconds % 60;  
		monthTimeSeconds /= 60;  
		int mm = (int)monthTimeSeconds % 60;  
		monthTimeSeconds /= 60;  
		int hh = (int)monthTimeSeconds % 24;     		
		
		double value = ((hh*60*60) + (mm*60) + ss)/monthDistanceKm; //Em segundos    
		ss = (int)value % 60;  
		value /= 60;  
		mm = (int)value % 60;  
		value /= 60;  
		hh = (int)value % 24; 
		
		String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		values.put(key, "2000-01-01 "+decFormat.format(mm)+":"+decFormat.format(ss)+":00");
		
		StringBuffer serie = new StringBuffer("[");
		d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {    		
    		        		
    		month = d0.getMonth();
    		year  = d0.getYear()+1900;
    		
    		key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    		
    		String v = values.get(key);
    		serie.append( "'"+v+"'" );
			serie.append( "," );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    
    	/*key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		
		String v = values.get(key);
		serie.append( "'"+v+"'" );*/
				
    	serie.append("]");
    	return serie.toString();   
	}
	
	// Grafico de Pace de Atividades por Semana
	public String getActivityPaceWeekGraphSerie() {
        

		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setMinimumIntegerDigits(2);
		
		Hashtable<String, String> values = new Hashtable<String, String>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		
		List<String> weekList = getWeekList();		
		for(String week:weekList) {
			values.put(week, "2000-01-01 00:00:00");
		}    	
    	
    	//Buscar atividades a partir das datas
    	Object[] keys = weekList.toArray();
    	int iStart = 0;
    	int iFinal = 1;
    	for ( iStart=0; iStart<keys.length-1; iStart++, iFinal++ ) {
    		    		
    		try {
    			Date startDate = sdf.parse( (String)keys[iStart] );
    			Date finalDate = sdf.parse( (String)keys[iFinal] );

    			double monthTimeSeconds = 0;
    	    	double monthDistanceKm = 0;
        		for(Activity activity:activityList) {		
        			
        			Date date = activity.getDate();
        			
        			if ( date.equals(startDate) || ( date.after(startDate) && date.before(finalDate) ) ) {
        				monthTimeSeconds += activity.getTotalTimeInSeconds();
            			monthDistanceKm += activity.getTotalDistance();
        			}        			
        		}

        		int ss = (int)monthTimeSeconds % 60;  
   				monthTimeSeconds /= 60;  
   				int mm = (int)monthTimeSeconds % 60;  
   				monthTimeSeconds /= 60;  
   				int hh = (int)monthTimeSeconds % 24;     		
    			
   				double value = ((hh*60*60) + (mm*60) + ss)/monthDistanceKm; //Em segundos    
   				ss = (int)value % 60;  
   				value /= 60;  
   				mm = (int)value % 60;  
   				value /= 60;  
   				hh = (int)value % 24;   			
    			
    			
    			String weekValue = "2000-01-01 "+decFormat.format(mm)+":"+decFormat.format(ss)+":00";        		
    			values.put((String)keys[iStart], weekValue);        		
    			
			} catch (ParseException e) {
				e.printStackTrace();
			}    		
    	}
    	    	
    	try {
    		iFinal = keys.length-1;
    		Date finalDate = sdf.parse( (String)keys[iFinal] );
    		
    		double monthTimeSeconds = 0;
	    	double monthDistanceKm = 0; 		
    		for(Activity activity:activityList) {		

    			Date date = activity.getDate();			
    			if ( date.equals(finalDate) || date.after(finalDate) ) {
    				monthTimeSeconds += activity.getTotalTimeInSeconds();
        			monthDistanceKm += activity.getTotalDistance();
    			}        			
    		}
    		
    		int ss = (int)monthTimeSeconds % 60;  
			monthTimeSeconds /= 60;  
			int mm = (int)monthTimeSeconds % 60;  
			monthTimeSeconds /= 60;  
			int hh = (int)monthTimeSeconds % 24;     		
		
			double value = ((hh*60*60) + (mm*60) + ss)/monthDistanceKm; //Em segundos    
			ss = (int)value % 60;  
			value /= 60;  
			mm = (int)value % 60;  
			value /= 60;  
			hh = (int)value % 24;   			
			
			
			String lastWeekValue = "2000-01-01 "+decFormat.format(mm)+":"+decFormat.format(ss)+":00";   
    		values.put((String)keys[iFinal], lastWeekValue);
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    	
    	    			    	
		//Montar lista
		StringBuffer serie = new StringBuffer("[");
		for(int i=0; i<keys.length; i++) {
			String key = (String)keys[i];
			
			String v = values.get(key);
    		serie.append( "'"+v+"'" );
			serie.append( "," );
		}
		
		serie.deleteCharAt( serie.length()-1 );
    	serie.append("]");
    	return serie.toString();   
	}

    
    //-- Util
    
    protected void setSelectedMenu(String menu) {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		session.setAttribute(Constants.SELECTED_MENU, menu);
	}
    
    private Double stringTimeToNumber(String time) {
    	if ( time==null || time.length()<5 ) return 0d;
    	
    	int first = time.indexOf(":");
	
		int min = Integer.parseInt( time.substring( 0,first) );
		int sec = (Integer.parseInt( time.substring( time.length()-2 ) )*10)/6;
		
		return Double.valueOf(min+"."+sec);
    } 
    
    protected Pessoa getSessionUser() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		return ((Pessoa) session.getAttribute(Constants.USER_SESSION));
	}
	

    //Getters and Setters
	
    public List<SelectItem> getCustomerList() {   
		return customerListSI;
    }	
	
	public int getActivityNumber() {
		if (activityList==null) return 0;
		else return activityList.size();
	}
	
	public double getActivityDistance() {
		if (activityList==null) return 0;
		else {
			double distance = 0;
			for(Activity act:activityList) {
				distance += act.getTotalDistance();
			}
			return distance;
		}
	}
	
	public String getActivityTime() {
		if (activityList==null) return "00:00:00";
		else {
			double time = 0;
			for(Activity act:activityList) {
				time += act.getTotalTimeInSeconds();
			}
			return Utils.formataTempo( (long)time );
		}
	}	
	
	public CartesianChartModel getRhythmGraph() {
		return rhythmGraph;
	}

	public void setRhythmGraph(CartesianChartModel rhythmGraph) {
		this.rhythmGraph = rhythmGraph;
	}

	public CartesianChartModel getMedicalGraph() {
		return medicalGraph;
	}

	public void setMedicalGraph(CartesianChartModel medicalGraph) {
		this.medicalGraph = medicalGraph;
	}

	public CartesianChartModel getWeightGraph() {
		return weightGraph;
	}

	public void setWeightGraph(CartesianChartModel weightGraph) {
		this.weightGraph = weightGraph;
	}

	public CartesianChartModel getActivityGraph() {
		return activityGraph;
	}

	public void setActivityGraph(CartesianChartModel activityGraph) {
		this.activityGraph = activityGraph;
	}

	public CartesianChartModel getActivityNumberGraph() {
		return activityNumberGraph;
	}

	public void setActivityNumberGraph(CartesianChartModel activityNumberGraph) {
		this.activityNumberGraph = activityNumberGraph;
	}

	public CartesianChartModel getActivityTimeGraph() {
		return activityTimeGraph;
	}

	public void setActivityTimeGraph(CartesianChartModel activityTimeGraph) {
		this.activityTimeGraph = activityTimeGraph;
	}

	public CartesianChartModel getActivityDayGraph() {
		return activityDayGraph;
	}

	public void setActivityDayGraph(CartesianChartModel activityDayGraph) {
		this.activityDayGraph = activityDayGraph;
	}

	public Date getInitialDate() {
		return initialDate;
	}

	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}

	public Date getFinalDate() {
		return finalDate;
	}

	public void setFinalDate(Date finalDate) {
		this.finalDate = finalDate;
	}

	public Pessoa getStudent() {
		return student;
	}

	public void setStudent(Pessoa student) {
		this.student = student;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
