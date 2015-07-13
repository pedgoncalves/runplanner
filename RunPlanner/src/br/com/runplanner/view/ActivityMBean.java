package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.xml.parsers.ParserConfigurationException;

import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.event.map.StateChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.ActivityLap;
import br.com.runplanner.domain.ActivityTack;
import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Comment;
import br.com.runplanner.domain.Equipment;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.EventPessoaActivity;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.Spreadsheet;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.CommentService;
import br.com.runplanner.service.EquipmentService;
import br.com.runplanner.service.EventPessoaActivityService;
import br.com.runplanner.service.EventService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.ScheduleService;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.ActivityDisplay;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.FITParser;
import br.com.runplanner.view.util.GPSParser;
import br.com.runplanner.view.util.GPXParser;
import br.com.runplanner.view.util.GarminTCXParser;


@Component("activityMBean")
@Scope("session")
public class ActivityMBean extends BasicMBean {

	private static final int PACE_GRAPH_DISTANCE = 200;

	private static final String DEFAULT_PHOTO = File.separator + "images" + File.separator+ "runnerico.jpg";

	private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
	
    private static final String PAGE_STUDENT_ACTIVITY = "/pages/student/activity";
    private static final String PAGE_TEACHER_ACTIVITY = "/pages/teacher/activity";
    private static final String PAGE_STUDENT_NEW_ACTIVITY = "/pages/student/newActivity";
    private static final String PAGE_STUDENT_EDIT_ACTIVITY = "/pages/student/editActivity";
    private static final String PAGE_VIEW_ACTIVITY = "/pages/viewActivity";
    private static final String PAGE_STUDENT_NEW_ACTIVITY_MAP = "/pages/student/newActivityManualMap";
    private static final String PAGE_STUDENT_NEW_ACTIVITY_MAP_DATA = "/pages/student/newActivityManualMapData";
    
	private ScheduleService scheduleService;
    private SpreadsheetService spreadsheetService;
    private ActivityService activityService;
	private EventService eventService;	
	private EventPessoaActivityService eventPessoaActivityService;
	private PessoaService pessoaService;
	private CommentService commentService;
	private EmailThreadProductor emailThreadProductor;
	private EquipmentService equipmentService;
    
    private Activity activity = new Activity();
    private File activityFile;
    private ActivityLap lap;
    private String stringTime;
    private String observation;
    private String equipmentText; //Texto com os equipamentos utilizados

	private CartesianChartModel paceGraph;
	private CartesianChartModel altitudeGraph;
	private double maxAltitude = 0;
    
    private Pessoa user;
    private Event event;
    
    private List<Activity> activityList;
    private List<ActivityDisplay> activityDisplayList;
	private List<Pessoa> customerList;
	private List<SelectItem> customerListSI;
	private List<SelectItem> userEvents;
    
    private UploadedFile tcxFile;
    
    private Double initLat;
    private Double initLon;
    private int mapZoom;
    private String activeIndex = new String("0");
	private String activeIndexCreate = new String("0");
	private double manualMapDistance;
	private List<ActivityTack> tracks;
        
    private MapModel polylineModel;  
    
	private Comment comment;
	private List<Comment> commentList;
	
	private Long idNext;
	private Long idPrev;
	
	private DualListModel<String> equipments;  
    
    @Autowired
    public ActivityMBean(ScheduleService scheduleService, 
    		SpreadsheetService spreadsheetService,
    		ActivityService activityService,
    		EventService eventService,
    		EventPessoaActivityService eventPessoaActivityService,
    		PessoaService pessoaService,
    		CommentService commentService,
    		EmailThreadProductor emailThreadProductor,
    		EquipmentService equipmentService) { 
				
		this.scheduleService = scheduleService;
		this.spreadsheetService = spreadsheetService;	
		this.activityService = activityService;
		this.eventService = eventService;
		this.eventPessoaActivityService = eventPessoaActivityService;
		this.pessoaService = pessoaService;
		this.commentService = commentService;
		this.emailThreadProductor = emailThreadProductor;
		this.equipmentService = equipmentService;
    }

    /**
     * Salva uma atividade a partir do formulario "Manual"
     */
    public String save() {
		Pessoa user = getSessionUser();
    	
		activity.setStudent(user);
		if ( activity.getDate()==null ) {
			activity.setDate( new Date() );
		}
		
		//Equipamentos
		List<Equipment> equipmentList = new ArrayList<Equipment>();
		for (String name: equipments.getTarget() ) {
			Equipment equipment = equipmentService.loadByName(name, user.getId());
			equipmentList.add(equipment);
		}			
		activity.setEquipment(equipmentList);
		
    	lap.setStartTime(activity.getDate());
    	lap.setDistanceMeters( lap.getDistanceMeters()*1000 );
    	lap.setTotalTimeSeconds( stringTimeToDouble(stringTime) );
    	
    	List<ActivityLap> laps = new ArrayList<ActivityLap>();
    	laps.add(lap);    	
    	activity.setLaps(laps);  
    
    	if(tracks!=null && tracks.size()>2) {
        	activity.setTracks(tracks);
    	}
    	
	    if ( activity.getId()==null ) {	    	
	    	activity = activityService.persist(activity); 
	    	
	    	lap.setActivity(activity);
	    	if(tracks!=null && tracks.size()>2) {
	    		for(ActivityTack track:tracks) {
	    			track.setActivity(activity);
	    		}
	    	}
	    	
	    	try {
				activityService.update(activity);
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}
	    	
	    	if ( event.getId()!=null && event.getId()!=-1 ) {
	    		try {
	    			event = eventService.loadById(event.getId());
					eventService.setEventActivity(event, user, activity);
				} catch (EntityNotFoundException e) {
					e.printStackTrace();
				}
	    	}
	    	
	    	addMessage(FacesMessage.SEVERITY_INFO, "activity.save.sucess");
	    }
	    else {
	    	try {
				activityService.update(activity);
				
				
				if ( event.getId()!=null && event.getId()!=-1 ) {	    		
	    			event = eventService.loadById(event.getId());
					eventService.setEventActivity(event, user, activity);					
		    	} else {
		    		eventService.removeEventActivity(activity);
		    	}
				
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}   	
	    	addMessage(FacesMessage.SEVERITY_INFO, "activity.edit.sucess");
	    }
    	return goToList();
    }
    
   
    
    /**                                     01234567
     * Faz o parse de uma string no formato 00:00:00 para double
     * @param time String no formato 00:00:00
     * @return Valor em segundos
     */
    private double stringTimeToDouble(String time) {
    	
    	int hour = Integer.valueOf( time.substring(0, 2) );
    	int min = Integer.valueOf( time.substring(3, 5) );
    	int sec = Integer.valueOf( time.substring(6) );
    	
    	return sec + (min*60) + (hour*60*60);
    }
    
	public void onChange(TabChangeEvent event) {
		Tab newTab = event.getTab();
		String tabIndex = newTab.getId().replaceAll("tab", "");
		
		activeIndexCreate = tabIndex;
	}
    
    public String goToCreate() {
    	activity = new Activity();
    	lap = new ActivityLap();
    	event = new Event();
    	tracks = new ArrayList<ActivityTack>();
    	observation = "";
    	stringTime = "";
    	manualMapDistance = 0d;
    	
    	//Buscar Eventos do usuario
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		
		//Buscar Eventos
		populateUserEvents(user, advice);
		
		//Buscar equipamentos
		List<Equipment> equipmentList = equipmentService.findByStudent(user.getId(), Boolean.TRUE);
		List<String> nameList = new ArrayList<String>(); 
		for(Equipment equipment: equipmentList) {
			nameList.add( equipment.getName() );
		}
		
		equipments = new DualListModel<String>(nameList,new ArrayList<String>());
    	
    	activeIndexCreate = "0";
    	setSelectedMenu(Constants.MENU_ACTIVITY);
    	
    	return PAGE_STUDENT_NEW_ACTIVITY;
    }
        
	public String goToList() {
    	
    	user = getSessionUser();
    	Long userId = user.getId();
    	
    	activityList = activityService.findByUserId(userId);
    	polylineModel = new DefaultMapModel();

    	loadActivity();
    	
    	setSelectedMenu(Constants.MENU_ACTIVITY);
    	
    	return PAGE_STUDENT_ACTIVITY;
    }
	
	public String goToInit() {
		event = new Event();
		
    	user = getSessionUser();    	
    	Long userId = user.getId();
    	
    	activityList = activityService.findByUserId(userId);
    	polylineModel = new DefaultMapModel();
    	
    	if ( activityList!=null && activityList.size()>0 ) {
    		activity = activityList.get(0);
    		loadActivity();
    	}
    	else { 
    		activity = new Activity();
    	}
    	
    	setSelectedMenu(Constants.MENU_ACTIVITY);
    	
    	return PAGE_STUDENT_ACTIVITY;
    }
		
	
	public String goToListTeacher() {
    	
		Pessoa teacher = getSessionUser();
    	Advice advice = teacher.getAdvice();
    	
    	customerList = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO, advice.getId(), true);
    	if ( customerList==null || customerList.size()<=0 ) {
        	return PAGE_TEACHER_ACTIVITY;
    	}
    	
    	customerListSI = new ArrayList<SelectItem>();
    	
		for (Pessoa pessoa:customerList) {
			customerListSI.add(new SelectItem(pessoa.getId(),pessoa.getNome()));
		}	
    	
		user = customerList.get(0);
    	Long userId = user.getId();
    	
    	activityList = activityService.findByUserId(userId);
    	polylineModel = new DefaultMapModel();
    	
    	if ( activityList!=null && activityList.size()>0 ) {
    		activity = activityList.get(0);
    		loadActivityTeacher();
    	}
    	else { 
        	addMessage(FacesMessage.SEVERITY_WARN, "activity.list.empty.teacher");
    		activity = new Activity();
    	}
    	
    	setSelectedMenu(Constants.MENU_ACTIVITY);
    	
    	return PAGE_TEACHER_ACTIVITY;
    }
	
	public String findByUser() {
    	
		user = pessoaService.loadById(user.getId());
    	Long userId = user.getId();
    	
    	activityList = activityService.findByUserId(userId);
    	polylineModel = new DefaultMapModel();
    	
    	if ( activityList!=null && activityList.size()>0 ) {
    		activity = activityList.get(0);
    		loadActivityTeacher();
    	}
    	else { 
        	addMessage(FacesMessage.SEVERITY_WARN, "activity.list.empty.teacher");
    		activity = new Activity();
    	}
    	
    	setSelectedMenu(Constants.MENU_ACTIVITY);
    	
    	return PAGE_TEACHER_ACTIVITY;
    }

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	private void prepareActivityList() {
		
		String[] months = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths();
		Hashtable activityTable = new Hashtable();
		
		int key = Integer.MAX_VALUE;
		int actualMonth = 0;
		
		if ( activityList!=null && activityList.size()>0 ) {
			
			actualMonth = activityList.get(0).getDate().getMonth();
			
			for(Activity act:activityList) {
				
				int month = act.getDate().getMonth();
				int year = act.getDate().getYear()+1900;
				
				if ( month!=actualMonth ) {
					key--;
					actualMonth=month;
				}
				
				if ( activityTable.containsKey(key) ) {
					ActivityDisplay actDisplay = (ActivityDisplay)activityTable.get(key);
					actDisplay.getActivityList().add(act);
				}
				else {
					ActivityDisplay actDisplay = new ActivityDisplay();
					actDisplay.setMonth(months[month]);
					actDisplay.setYear(year);
					actDisplay.getActivityList().add(act);
					
					activityTable.put(key,actDisplay);
				}
				
				if ( activity!=null && activity.getId().longValue() == act.getId().longValue() ) {
					activeIndex=  String.valueOf( Integer.MAX_VALUE-key );
				}
				
			}
		}
		
		activityDisplayList = new ArrayList<ActivityDisplay>();
		
		for(int i=Integer.MAX_VALUE; i>=key; i--) {
			ActivityDisplay actDisplay = (ActivityDisplay)activityTable.get(i);
			activityDisplayList.add(actDisplay);
			
		}
		
		/*Enumeration<ActivityDisplay> elements = activityTable.elements();
		while (elements.hasMoreElements()) {
			activityDisplayList.add(elements.nextElement());
		}*/
	}
    
    public String loadActivity() {
    	
    	prepareActivity();
		prepareActivityList();
		
		if (isUserStudent()) 	
			return PAGE_STUDENT_ACTIVITY;
		else
	    	return PAGE_TEACHER_ACTIVITY;

    }
    
    public String loadActivityTeacher() {
    	
    	prepareActivity();
		prepareActivityList();
		
    	return PAGE_TEACHER_ACTIVITY;
    	
    }

	public void prepareActivity() {
		activity = activityService.loadById(activity.getId());
		
		
		//Links para proximo e anterior
		idNext = null;
		idPrev = null;
		if ( activity!=null && activityList!=null && activityList.size()>1 ) {
			int index = 0;
			for( int i=0;i<activityList.size();i++ ) {
				Activity a = activityList.get(i);
				if ( a.getId().longValue()==activity.getId().longValue() ) {
					index = i;
					break;
				}
			}
			
			if ( index == 0 ) {
				idNext = null; //Ja e a ultima
			}
			else {
				idNext = activityList.get(index-1).getId();
			}
			
			if ( index==activityList.size()-1) {
				idPrev = null; //Ja e o primeiro
			}
			else {
				idPrev = activityList.get(index+1).getId();
			}
		}
		
		
    	if ( activity.getObservation()!=null ) {
    		String obs = activity.getObservation();
    		obs = obs.replaceAll("\n", "<br/>");
    		activity.setObservation(obs);
    	}
		
    	//Buscar Comentarios
    	comment = new Comment();
    	commentList = commentService.loadByActivityId(activity.getId());
    	for (Comment c: commentList) {
    		if ( c.getComment()!=null ) {
        		String comm = c.getComment();
        		comm = comm.replaceAll("\n", "<br/>");
        		c.setComment(comm);
        	}
    	}
    	
    	//Buscar Equipamentos
    	equipmentText = new String();
    	List<Equipment> equipments = equipmentService.findByActivity(activity.getId());
    	if ( equipments!=null && equipments.size()>0 ) {    		
    		for(Equipment equipment: equipments) {
    			equipmentText += equipment.getName() + ", ";
    		}
    		
    		equipmentText = equipmentText.substring( 0, equipmentText.length()-2 );    		
    	}
    	else {
    		equipmentText = null;
    	}
    	
    	loadMap(activity);
    	createAltitudeGraph();
    	//createPaceGraph();
    	
    	EventPessoaActivity epa = eventPessoaActivityService.findByActivity(activity.getId());
    	if (epa!=null) {
    		event = epa.getEvent();
    	} 
    	else {
    		event = new Event();
    	}
	}

	private void createAltitudeGraph() {
		
		List<ActivityTack> tracks = activity.getTracks();
		if ( tracks==null || tracks.size()==0) {
			altitudeGraph = null;
			return;
		}
		
    	LineChartSeries altitudeSeries = new LineChartSeries();
    	altitudeSeries.setLabel("Altitude");
    	
    	maxAltitude = 0;    	
    	double distance = 0;
    	ActivityTack lastTrack = null;
    	for(ActivityTack track: tracks) {
    		
    		//Anteriormente nao tinhamos os dados para o grafico (de pace),
    		//cancela para manter compatibilidade com os dados antigos, so exibe se nao tiver de pace
    		/*if ( track.getTime() != null ) {
    			altitudeGraph = null;
    			return;
    		}*/
    		
    		if ( track.getAltitudeMeters() == null ||
    				track.getLatitudeDegrees() == null || 
    				track.getLongitudeDegrees() ==null ) continue;
    		
    		if ( lastTrack==null ) {
    			lastTrack = track;
    		}
    		else {    		
    			distance += Utils.distance(track.getLatitudeDegrees(),track.getLongitudeDegrees(),
    				lastTrack.getLatitudeDegrees(), lastTrack.getLongitudeDegrees());
    		}
    		
    		double altitude = track.getAltitudeMeters();
    		if ( altitude>maxAltitude ) {
    			maxAltitude = altitude;
    		}
    		altitudeSeries.set( distance,  altitude );
    		
    		lastTrack = track;
    	}
    	
    	//Mantido para compatibilidade com coisas antigas, o if abaixo
    	//tambem é necessário
    	if ( distance == 0 && maxAltitude==0 ) {
			altitudeGraph = null;
    		return;
    	}
    	
    	if (  maxAltitude==0 ) {
			altitudeGraph = null;
    		return;
    	}
    	
    	altitudeGraph = new CartesianChartModel();
    	altitudeGraph.addSeries(altitudeSeries);
	}
	
	public String getPaceGraphTick() {		
		StringBuffer tick = new StringBuffer("[");
		
		if ( activity==null ) return null;
		Double distance = activity.getTotalDistance();
		
		int value = 0;
		for( int i=1; value<(distance*1000); i++ ) {
			value = i*PACE_GRAPH_DISTANCE;
			tick.append(value);
			tick.append(",");
		}		

		tick.append("]");
		return tick.toString();
	}
	
	public String getPaceGraphSerie() {
		
		List<ActivityTack> tracks = activity.getTracks();
		if ( tracks==null || tracks.size()==0) {			
			return null;
		}
		
		StringBuffer serie = new StringBuffer("[[0,0],");
    	
    	double distance = 0;
    	long timeSeconds = 0;
    	int volta = 1;
    	Date firstTime = null;
    	
    	for(ActivityTack track: tracks) {
    		
    		//Anteriormente nao tinhamos os dados para o grafico,
    		//cancela para manter compatibilidade com os dados antigos
    		if ( track.getTime() == null ) {
    			return null;
    		}
    		
    		if ( firstTime==null ) {
    			firstTime = track.getTime();
    		}
    		
    		if ( track.getDistanceMeters() == null ) {
    			continue;
    		}
    		
    		distance = track.getDistanceMeters();
    		
    		if ( distance/volta >= PACE_GRAPH_DISTANCE ) {
    			timeSeconds = (track.getTime().getTime() - firstTime.getTime() )/1000; 	
    			
    			String pace = getPace(timeSeconds,PACE_GRAPH_DISTANCE);
    			String data = pace.replaceAll(":", ".");
    			
    			serie.append("["+distance+","+data+"]");
    			//serie.append(data);
    			serie.append(",");
    			
    			firstTime = null;
    			volta++;
			}
    		
    		
    	}
    	
    	if ( distance == 0 ) {
    		return null;
    	}

    	serie.append("]");
    	return serie.toString();
	}	
	
/*	private void createPaceGraph() {
		
		List<ActivityTack> tracks = activity.getTracks();
		if ( tracks==null || tracks.size()==0) {
			paceGraph = null;
			return;
		}
		
    	LineChartSeries paceSerie = new LineChartSeries();
    	paceSerie.setLabel("Ritmo");
    	
    	
    	double distance = 0;
    	long timeSeconds = 0;
    	int volta = 1;
    	Date firstTime = null;
    	for(ActivityTack track: tracks) {
    		
    		//Anteriormente nao tinhamos os dados para o grafico,
    		//cancela para manter compatibilidade com os dados antigos
    		if ( track.getTime() == null ) {
    			paceGraph = null;
    			return;
    		}
    		
    		if ( firstTime==null ) {
    			firstTime = track.getTime();
    		}
    		
    		distance = track.getDistanceMeters();
    		
    		if ( distance/volta >= 1000 ) {
    			timeSeconds = (track.getTime().getTime() - firstTime.getTime() )/1000; 	
    			paceSerie.set( 1000*volta,  getPace(timeSeconds,1000) );
    			firstTime = null;
    			volta++;
			}
    		
    	}
    	
    	if ( distance == 0 ) {
    		paceGraph = null;
    		return;
    	}
    	
    	paceGraph = new CartesianChartModel();
    	paceGraph.addSeries(paceSerie);
	}*/	
	
	private String getPace(long timeSeconds, double distanceMeters) {
		
		long ss = timeSeconds % 60;  
		timeSeconds /= 60;  
		long min = timeSeconds % 60;  
		timeSeconds /= 60;  
		long hh = timeSeconds % 24;  
		String tempo = strzero(hh) + ":" + strzero(min) + ":" + strzero(ss);  
		
		int first = tempo.indexOf(":");


		if ( tempo.length()>6 ) { //Possui hora 
			int second = tempo.indexOf(":", first+1);
			hh = Integer.parseInt( tempo.substring( 0,first) );
			min = Integer.parseInt( tempo.substring( first+1,second) );
			ss = Integer.parseInt( tempo.substring( tempo.length()-2 ) );
		}
		else if ( tempo.length()>3) { //Possui minutos
			min = Integer.parseInt( tempo.substring( 0,first) );
			ss = Integer.parseInt( tempo.substring( tempo.length()-2 ) );
		}

		double paceValue = (((hh*60*60) + (min*60) + ss)/ (distanceMeters/1000) )/60;
		
		int resto = (int)(((double)paceValue - (int)paceValue)*100);
    	resto = (int)((resto*60)/100);
    	        	        
    	return (int)paceValue+":"+strzero(resto);
	}
	
	private String strzero(long n) {  
		if(n < 10)  
			return "0" + String.valueOf(n);  
		return String.valueOf(n);  
	}
	
	
    
    public String goToView() {
    	
    	prepareActivity();
    	
    	return PAGE_VIEW_ACTIVITY;
    }
    
    public String goToCreateManualMap() {
    	Pessoa student = getSessionUser();
    	ActivityTack activityTack = activityService.getLastActivityPosition(student.getId());
    	
    	if ( activityTack!=null ) {
    		
    		if( activityTack.getLatitudeDegrees()!=null ) {
    			initLat = activityTack.getLatitudeDegrees();
    		}
    		else {
    			initLat = -14.127675;
    		}
    		
    		if( activityTack.getLongitudeDegrees()!=null ) {
    			initLon = activityTack.getLongitudeDegrees();
    		}
    		else {
    			initLon = -45.139964;
    		}
        	
        	mapZoom = 15;
    	}
    	else {
    		initLat = -14.127675;
    		initLon = -45.139964;
        	
        	mapZoom = 5;
    	}
    	polylineModel = new DefaultMapModel();
    	
    	return PAGE_STUDENT_NEW_ACTIVITY_MAP;
    }
    
    
    public String goToCreateManualMapData() {
    	
    	tracks = new ArrayList<ActivityTack>();
    	List<Marker> markers = polylineModel.getMarkers();
		if (markers!=null && markers.size()>=2) {
			for (Marker m:markers ) {

				ActivityTack t = new ActivityTack();
				t.setAltitudeMeters(0d);
				t.setLatitudeDegrees(m.getLatlng().getLat());
				t.setLongitudeDegrees(m.getLatlng().getLng());
				
			}
		}
    	
		double distance = 0d;
		boolean first = true;
		double p1Lat=0;
		double p1Lon=0;
		
    	if (markers!=null && markers.size()>=2) {
			for (Marker m:markers ) {
				
				if (first) {
					p1Lat = m.getLatlng().getLat();
					p1Lon = m.getLatlng().getLng();
					
					first = false;
					
					ActivityTack t = new ActivityTack();
					t.setAltitudeMeters(0d);
					t.setLatitudeDegrees(m.getLatlng().getLat());
					t.setLongitudeDegrees(m.getLatlng().getLng());
					t.setDistanceMeters(0d);
					tracks.add(t);
					
					continue;
				}
				
				double p2Lat = m.getLatlng().getLat();
				double p2Lon = m.getLatlng().getLng();
				
				distance = Utils.distance(p1Lat, p1Lon, p2Lat, p2Lon);
				
				ActivityTack t = new ActivityTack();
				t.setAltitudeMeters(0d);
				t.setLatitudeDegrees(m.getLatlng().getLat());
				t.setLongitudeDegrees(m.getLatlng().getLng());
				t.setDistanceMeters(distance);
				tracks.add(t);
				
				p1Lat = p2Lat;
				p1Lon = p2Lon;
			}
    	}
    	
    	lap.setDistanceMeters(manualMapDistance/1000);
    	
    	return PAGE_STUDENT_NEW_ACTIVITY_MAP_DATA;
    }

    public void onStateChange(StateChangeEvent event) {  
        mapZoom = event.getZoomLevel();
        initLat = event.getCenter().getLat();
        initLon = event.getCenter().getLng();
    }
    
    public void onMarkerSelect(OverlaySelectEvent event) {  
    	Marker marker = (Marker) event.getOverlay();  
    	
    	List<Polyline> lines = polylineModel.getPolylines();
    	Polyline polyline = lines.get(0);
    	
    	//Remove da linha
    	polyline.getPaths().remove( marker.getLatlng() );
    	
    	//Remove marcador
    	polylineModel.getMarkers().remove(marker);
    	
    	List<Marker> markers = polylineModel.getMarkers();
		if (markers!=null && markers.size()>=2) {
			for (Marker m:markers ) {
				m.setIcon("/runplanner/images/icon_grey.png");
				m.setDraggable(false);
			}
			markers.get(0).setIcon("/runplanner/images/icon_green.png");
			markers.get(markers.size()-1).setIcon("/runplanner/images/icon_red.png");
			markers.get(markers.size()-1).setDraggable(true);
		}
    	updateDistanceMap(markers);
    	polylineModel.addOverlay(polyline);
    }  
    
    public void onMarkerDrag(MarkerDragEvent event) {
    	Marker marker = event.getMarker(); 
    	
    	List<Polyline> lines = polylineModel.getPolylines();
    	Polyline polyline = lines.get(0);
    	polyline.getPaths().remove( polyline.getPaths().size()-1 );
    	polyline.getPaths().add(marker.getLatlng());
    	
    	List<Marker> markers = polylineModel.getMarkers();
    	markers.remove( markers.size()-1 );    	
    	polylineModel.addOverlay(marker);
    	
    	updateDistanceMap(polylineModel.getMarkers());
        
    }
    
    public void onPointSelect(PointSelectEvent event) {  
    	
    	if (polylineModel==null) {
    		polylineModel = new DefaultMapModel();
    	}
    	
    	boolean first = true;
    	List<Polyline> lines = polylineModel.getPolylines();
    	Polyline polyline = new Polyline();
    	
    	if ( lines.size()>0 ) {
    		polyline = lines.get(0);
    		first=false;
    	}
    	else {
    		polyline.setStrokeWeight(5);  
    		polyline.setStrokeColor("#6F0000");  
    		polyline.setStrokeOpacity(0.7);  
    	}
    	
        LatLng coord = event.getLatLng(); 
        if ( first ) {
			Marker init = new Marker(coord, "Inicio");
			init.setIcon("/runplanner/images/icon_green.png"); 
			polylineModel.addOverlay(init);
			first = false;
		}
		else {
			Marker end = new Marker(coord, "");
			polylineModel.addOverlay(end);
		}
		polyline.getPaths().add(coord);	
		
				
		List<Marker> markers = polylineModel.getMarkers();
		if (markers!=null && markers.size()>=2) {
			for (Marker m:markers ) {
				m.setIcon("/runplanner/images/icon_grey.png");
				m.setDraggable(false);
			}
			markers.get(0).setIcon("/runplanner/images/icon_green.png");
			markers.get(markers.size()-1).setIcon("/runplanner/images/icon_red.png");
			markers.get(markers.size()-1).setDraggable(true);
		}
		updateDistanceMap(markers);
		
		polylineModel.addOverlay(polyline);
    } 
    
    public void clearPath() {
    	polylineModel = new DefaultMapModel();
		updateDistanceMap(polylineModel.getMarkers());
	}
    
    private void updateDistanceMap(List<Marker> markers) {
    	manualMapDistance = 0d;
		boolean first = true;
		double p1Lat=0;
		double p1Lon=0;
		
    	if (markers!=null && markers.size()>=2) {
			for (Marker m:markers ) {
				
				if (first) {
					p1Lat = m.getLatlng().getLat();
					p1Lon = m.getLatlng().getLng();
					
					first = false;
					continue;
				}
				
				double p2Lat = m.getLatlng().getLat();
				double p2Lon = m.getLatlng().getLng();
				
				manualMapDistance += Utils.distance(p1Lat, p1Lon, p2Lat, p2Lon);
				
				p1Lat = p2Lat;
				p1Lon = p2Lon;
			}
    	}
    }
     
    
	private void loadMap(Activity activity) {

    	polylineModel = new DefaultMapModel();
		List<ActivityTack> trackList = activityService.findTracksById(activity.getId());
		activity.setTracks(trackList);
		  
		if ( trackList==null || trackList.size()==0 )  {
			polylineModel = null;
			return;
		}
		
		Polyline polyline = new Polyline();  
		boolean first = true;
		LatLng coord = null;
		
		ActivityTack lastTrack = null;
		double distance = 0d;
		int volta = 1;
		
		for( ActivityTack track: trackList ) {
			
			if( track==null ||
				track.getLatitudeDegrees()==null ||
				track.getLongitudeDegrees()==null ) continue;
			
			coord = new LatLng(track.getLatitudeDegrees(), track.getLongitudeDegrees());
			
			if ( first ) {
				Marker init = new Marker(coord, "Inicio");
				init.setIcon("/runplanner/images/icon_green.png"); 
				polylineModel.addOverlay(init);
				first = false;
			}
			polyline.getPaths().add(coord);	
			
			if ( lastTrack==null ) {
    			lastTrack = track;
    		}
    		else {    		
    			distance += Utils.distance(track.getLatitudeDegrees(),track.getLongitudeDegrees(),
    				lastTrack.getLatitudeDegrees(), lastTrack.getLongitudeDegrees());
    		}
			
			if ( distance/volta >= 1000 ) {
				Marker km = new Marker(coord, "KM "+volta);
				km.setIcon("/runplanner/images/number/number_"+volta+".png"); 
				polylineModel.addOverlay(km);
				
				volta++;
			}
			
			lastTrack = track;			
		}
		
		if ( coord!=null ) {
			Marker end = new Marker(coord, "Fim");
			end.setIcon("/runplanner/images/icon_red.png"); 
			polylineModel.addOverlay(end);
		}
		
		
		
		LatLng init = polyline.getPaths().get(0);
		this.initLat=init.getLat();
		this.initLon=init.getLng();
		
		polyline.setStrokeWeight(5);  
		polyline.setStrokeColor("#6F0000");  
		polyline.setStrokeOpacity(0.7);  
		  
		polylineModel.addOverlay(polyline);
	}
	
	
	public String handleFileUpload() {
		
		activeIndexCreate = "1";
		
		String type = "tcx";
		String fileName = tcxFile.getFileName();
		fileName = fileName.toLowerCase();
		
		if ( fileName==null || (!fileName.endsWith(".tcx") && !fileName.endsWith(".gpx") && !fileName.endsWith(".fit")) ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.erro");			
			return null;
		} else { 
			if ( fileName.endsWith(".tcx") ) {
				type = "tcx";
			} else {
				if ( fileName.endsWith(".gpx") ) {
					type = "gpx";
				} else {
					if ( fileName.endsWith(".fit") ) {
						type = "fit";
					}
				}
			}
        }
		
		
		//Importar arquivo
		byte[] file = tcxFile.getContents();
		activityFile = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath(fileName+".xml"));
		
		FileOutputStream out;
		try {
			out = new FileOutputStream(activityFile);
	        out.write(file);
	        out.flush();

	        processFile(type);	
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (IOException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		}

		addMessage(FacesMessage.SEVERITY_INFO, "activity.file.sucess");
    	return goToList();
        
    }
	
	public String handleSyncFile() {
		
		try {
			
			if ( activityFile.getName().endsWith(".fit") ) {
				processFile("fit");
			}
			else {
				processFile("tcx");
			}
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (IOException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");			
			return null;
		}

		addMessage(FacesMessage.SEVERITY_INFO, "activity.file.sucess");
    	return goToList();
		
	}

	@SuppressWarnings("deprecation")
	private void processFile(String tipo) throws SAXException, IOException, ParserConfigurationException, ParseException {
    	Pessoa user = getSessionUser();
		Long adviceId = user.getAdvice().getId();
		Date activityDate = new Date();
       
        GPSParser parser = null;

        if ( tipo.equals("tcx") ) {
        	parser = new GarminTCXParser(activityFile);
        } else if ( tipo.equals("gpx") ) {        
        	parser = new GPXParser(activityFile);
        } else if ( tipo.equals("fit")){
        	parser = new FITParser(activityFile);
        }

        
         
        activity = parser.parse();
        
        if ( activityFile.exists() ) activityFile.delete();
        
        //Pega a data da primeira volta
        ActivityLap lap1 = activity.getLaps().get(0);
        activityDate = lap1.getStartTime();
        activity.setDate(activityDate);            
        activity.setStudent(user);
        
        if  ( observation !=null ) {
			activity.setObservation( new String (observation.getBytes ("iso-8859-1"), "UTF-8") );
		}
        
        //Equipamentos
		List<Equipment> equipmentList = new ArrayList<Equipment>();
		for (String name: equipments.getTarget() ) {
			Equipment equipment = equipmentService.loadByName(name, user.getId());
			equipmentList.add(equipment);
		}			
		activity.setEquipment(equipmentList);
        
		//TODO UrlUtil.getShortenUrl(activity);
		
        activityService.persist(activity);            
        
        //Setar o ID
        for( ActivityLap lap: activity.getLaps() ) {
        	lap.setActivity(activity);
        }
        for( ActivityTack track: activity.getTracks() ) {
        	track.setActivity(activity);
        }
        
        
        try {
        	activityService.update(activity); 
        }
        catch (Exception e) {
			// TODO: handle exception
		}
        
        if ( event.getId()!=null && event.getId()!=-1 ) {
    		try {
    			event = eventService.loadById(event.getId());
				eventService.setEventActivity(event, user, activity);
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}
    	}


		Schedule schedule = new Schedule();
		List<Spreadsheet> spreadList = spreadsheetService.findByCompetence(activityDate.getMonth(), activityDate.getYear(), adviceId);
		
		if ( spreadList!=null && spreadList.size()>0 ) {
			Spreadsheet spreadsheet = spreadList.get(0);
		
			for(Schedule s: spreadsheet.getSchedules()) {
				if ( s.getDate().getDate() == activityDate.getDate() &&
					s.getDate().getMonth() == activityDate.getMonth() &&
					s.getDate().getYear() == activityDate.getYear() ) {
					schedule = s;
					break;
				}
			}		
	
			if ( schedule.getId()==null ) {
				schedule.setDate(activityDate);
				schedule = scheduleService.persist(schedule);
				
				spreadsheet.getSchedules().add(schedule);
				try {
					spreadsheetService.update(spreadsheet);
				} catch (EntityNotFoundException e) {
					//TODO
				} 
			}
		}
	}  
	
	
	public List<SelectItem> getUserEvents() {

		return userEvents;
	}	
	
    
	@Override
	public String delete() {
		
		try {
			activityService.deleteById(activity.getId());
			addMessage(FacesMessage.SEVERITY_INFO,"activity.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");	
		}
		
		return goToInit();
	}

	@Override
	public String goToModify() {
		
		if ( activity.getLaps()!=null && activity.getLaps().size()>0 ) {
			lap = activity.getLaps().get(0);
			stringTime = lap.getTotalTimeString();
			lap.setDistanceMeters(lap.getDistanceKm());
		}	
		
    	if ( activity.getObservation()!=null ) {
    		String obs = activity.getObservation();
    		obs = obs.replaceAll("<br/>", "\n");
    		activity.setObservation(obs);
    	}
    	
    	//Buscar Eventos do usuario
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		
		populateUserEvents(user, advice);
    	
    	//Buscar equipamentos
		List<Equipment> equipmentList = equipmentService.findByStudent(user.getId(), Boolean.TRUE);
		List<String> nameList = new ArrayList<String>(); 
		for(Equipment equipment: equipmentList) {
			nameList.add( equipment.getName() );
		}
				
		List<Equipment> target = equipmentService.findByActivity(activity.getId());
		if ( target==null ) target = new ArrayList<Equipment>();
		List<String> targetList = new ArrayList<String>();
		for (Equipment equipment:target) {				
			targetList.add(equipment.getName());
			nameList.remove(equipment.getName());
		}		
		equipments = new DualListModel<String>(nameList,targetList);
		
    	activeIndexCreate = "0";
    	
    	return PAGE_STUDENT_EDIT_ACTIVITY;
	}

	private void populateUserEvents(Pessoa user, Advice advice) {
		List<Event> events = eventService.getByUserInactive(advice.getId(),user.getId());		
		userEvents = new ArrayList<SelectItem>();
		for (Event e:events) {
			
			EventPessoaActivity epa = eventPessoaActivityService.findByEventUserFull(e.getId(), user.getId());
			
			if (epa == null) continue;
			
			if ( epa.getActivity()==null || epa.getActivity().getId()==null ) {			
				userEvents.add(new SelectItem(e.getId(),e.getName()));
			}
		}
	}  
	
	public void removeComment() {
		try {
			
			commentService.deleteById(comment.getId());
			
			for( Comment c:commentList ) {
				if ( c.getId().longValue() == comment.getId().longValue() ) {
					commentList.remove(c);
					break;
				}
			}
			
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");	
		}
	}
	
	public void addComment() {
		
		if( comment.getComment()==null || 
				comment.getComment().trim().equals("") ) {
			return;
		}
		
		Pessoa user = getSessionUser();
		
		Comment comentario = new Comment();
		comentario.setActivity(activity);
		comentario.setPessoa(user);
		comentario.setDate(new Date());
		comentario.setComment( comment.getComment() );
		
		commentService.persist(comentario);
		
		if ( comentario.getComment()!=null ) {
    		String comm = comentario.getComment();
    		comm = comm.replaceAll("\n", "<br/>");
    		comentario.setComment(comm);
    	}
		commentList.add(comentario);
		
		//Nao avisar se for ele mesmo
		if ( activity.getStudent().getId().longValue() != user.getId().longValue() ) {
			
			//Enviar email avisando do comentario
			String email = activity.getStudent().getEmail();
			String messageBody = Constants.EMAIL_COMMENT_ACTIVITY;
			messageBody = messageBody.replace("$1", user.getNome());
			messageBody = messageBody.replace("$2", sdf.format(activity.getDate()));
			messageBody = messageBody.replace("$3", comentario.getComment());	
			emailThreadProductor.enviarMensagem(email, Constants.EMAIL_COMMENT_ACTIVITY_TITLE, messageBody);
			
		}
				
						
		//Procurar os outros que tambem comentaram para avisar
		ArrayList<String> alreadySent = new ArrayList<String>();
		for( Comment c:commentList ) {
			
			if ( c.getPessoa().getId().longValue() != user.getId().longValue() && 
					c.getPessoa().getId().longValue() != activity.getStudent().getId().longValue() ) {
				
				String email = c.getPessoa().getEmail();
				
				if ( !alreadySent.contains(email) ) {
					
					alreadySent.add(email);
					
					String messageBody = Constants.EMAIL_COMMENT_ACTIVITY_OTHERS;
					messageBody = messageBody.replace("$1", user.getNome());
					messageBody = messageBody.replace("$2", activity.getStudent().getNome());
					messageBody = messageBody.replace("$3", sdf.format(activity.getDate()));
					messageBody = messageBody.replace("$4", comentario.getComment());	
					
					String messageTitle = Constants.EMAIL_COMMENT_ACTIVITY_OTHERS_TITLE;
					messageTitle = messageTitle.replace("$1", activity.getStudent().getNome());

					emailThreadProductor.enviarMensagem(c.getPessoa().getEmail(), messageTitle, messageBody);
				}
				
			}
		}
		
		comment = new Comment();		
	}
	
	public StreamedContent getStreamedUserPhoto() {
		
		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("client_id");
				
		if( id==null || id.equals("") ) {
			return getDefaultPhoto();
		}
		
		Long clientId = Long.parseLong(id);
		
		Pessoa pessoa = pessoaService.loadById(clientId);
		
		if ( pessoa.getPhoto()!= null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(pessoa.getPhoto());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		else {
			return getDefaultPhoto();
		}
	}
	
	private DefaultStreamedContent getDefaultPhoto() {
		String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+DEFAULT_PHOTO;
		
		try {
			FileInputStream is = new FileInputStream(new File(photoPath));
			byte[] photo = new byte[is.available()];
			is.read(photo);
			
			ByteArrayInputStream stream = new ByteArrayInputStream(photo);

			return new DefaultStreamedContent(stream, "image/jpeg");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    //Getters and Setters  
	public ScheduleService getScheduleService() {
		return scheduleService;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public SpreadsheetService getSpreadsheetService() {
		return spreadsheetService;
	}

	public void setSpreadsheetService(SpreadsheetService spreadsheetService) {
		this.spreadsheetService = spreadsheetService;
	}

	public UploadedFile getTcxFile() {
		return tcxFile;
	}

	public void setTcxFile(UploadedFile tcxFile) {
		this.tcxFile = tcxFile;
	}

	public MapModel getPolylineModel() {
		return polylineModel;
	}

	public void setPolylineModel(MapModel polylineModel) {
		this.polylineModel = polylineModel;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public ActivityService getActivityService() {
		return activityService;
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	public List<Activity> getActivityList() {
		return activityList;
	}

	public void setActivityList(List<Activity> activityList) {
		this.activityList = activityList;
	}

	public Double getInitLat() {
		return initLat;
	}

	public void setInitLat(Double initLat) {
		this.initLat = initLat;
	}

	public Double getInitLon() {
		return initLon;
	}

	public void setInitLon(Double initLon) {
		this.initLon = initLon;
	}

	public List<ActivityDisplay> getActivityDisplayList() {
		return activityDisplayList;
	}

	public void setActivityDisplayList(List<ActivityDisplay> activityDisplayList) {
		this.activityDisplayList = activityDisplayList;
	}

	public ActivityLap getLap() {
		return lap;
	}

	public void setLap(ActivityLap lap) {
		this.lap = lap;
	}

	public String getStringTime() {
		return stringTime;
	}

	public void setStringTime(String stringTime) {
		this.stringTime = stringTime;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}
	public List<SelectItem> getCustomerList() {   
		return customerListSI;
    }

	public Pessoa getUser() {
		return user;
	}

	public String getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(String activeIndex) {
		this.activeIndex = activeIndex;
	}

	public String getActiveIndexCreate() {
		return activeIndexCreate;
	}

	public void setActiveIndexCreate(String activeIndexCreate) {
		this.activeIndexCreate = activeIndexCreate;
	}

	public CartesianChartModel getAltitudeGraph() {
		return altitudeGraph;
	}

	public void setAltitudeGraph(CartesianChartModel altitudeGraph) {
		this.altitudeGraph = altitudeGraph;
	}

	public double getMaxAltitude() {
		return maxAltitude;
	}

	public void setMaxAltitude(double maxAltitude) {
		this.maxAltitude = maxAltitude;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public List<Comment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<Comment> commentList) {
		this.commentList = commentList;
	}

	public Long getIdNext() {
		return idNext;
	}

	public void setIdNext(Long idNext) {
		this.idNext = idNext;
	}

	public Long getIdPrev() {
		return idPrev;
	}

	public void setIdPrev(Long idPrev) {
		this.idPrev = idPrev;
	}

	public CartesianChartModel getPaceGraph() {
		return paceGraph;
	}

	public DualListModel<String> getEquipments() {
		return equipments;
	}

	public void setEquipments(DualListModel<String> equipments) {
		this.equipments = equipments;
	}

	public String getEquipmentText() {
		return equipmentText;
	}

	public void setEquipmentText(String equipmentText) {
		this.equipmentText = equipmentText;
	}

	public File getActivityFile() {
		return activityFile;
	}

	public void setActivityFile(File activityFile) {
		this.activityFile = activityFile;
	}

	public int getMapZoom() {
		return mapZoom;
	}

	public void setMapZoom(int mapZoom) {
		this.mapZoom = mapZoom;
	}

	public double getManualMapDistance() {
		return manualMapDistance;
	}
	
	
}
