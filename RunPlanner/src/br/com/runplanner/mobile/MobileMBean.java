package br.com.runplanner.mobile;

import java.io.ByteArrayInputStream;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.ActivityTack;
import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Equipment;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.RhythmTable;
import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.Spreadsheet;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.EquipmentService;
import br.com.runplanner.service.EventService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.view.util.ActivityDisplay;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.MessagesResources;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;


@Component("mobileMBean")
@Scope("session")
public class MobileMBean {

	@Autowired
	private SpreadsheetService spreadsheetService;
	@Autowired
	private EventService eventService;
	@Autowired
	private EquipmentService equipmentService;
	@Autowired
    private ActivityService activityService;
	@Autowired
	private PessoaService pessoaService;
	@Autowired
    private ReportService reportService;
	
	//Login
	private String login;
	private String password;
	private String adviceName;
	
	//Proximo Treino
	private Schedule proximoTreino;
	private Spreadsheet spreadsheet;
	private List<Spreadsheet> spreadsheetList = new ArrayList<Spreadsheet>();
	//Ritmos
	private String easyPace;
	private String normalPace;
	private String mediumPace;
	private String moderatePace;
	private String strongPace;
	private String shootingPace;
	
	
	//Eventos
	private List<Event> eventList;
	private long eventDetailID = 0l;
	private Event selectedEvent;
	private Activity selectedActivity;
	
	//Equipamentos
	private List<Equipment> equipmentList;
	
	//Atividades
	private Activity activity;
	private List<Activity> activityList = new ArrayList<Activity>();
    private List<ActivityDisplay> activityDisplayList;
    private Integer selectedDisplay;
    private Hashtable activityTable = new Hashtable();
    private Double initLat;
    private Double initLon;
    private MapModel polylineModel; 
    private String selectedMonth; 
	
	public String doLogout() {
		login = null;
		password = null;
		
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		session.invalidate();
		
		return "pm:main";
	}
    
    
	public String doLogin() {
		Pessoa user = pessoaService.loadByEmailActive(login,true);
		
		if( user==null ) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					MessagesResources.getStringFromBundle("template.login.bad.credentials",""),"");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
			
			return "pm:main";
		}
		
		
		String crypted = MD5Util.crypt(password);
		if ( !user.getPassword().equals(crypted) ) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					MessagesResources.getStringFromBundle("template.login.bad.credentials",""),"");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
			
			return "pm:main";
		}
		else {
			user = pessoaService.loadById(user.getId());
		}
		
		Advice advice = user.getAdvice();		
		
		adviceName = advice.getName();
		
		//Carregar dados
		//Eventos		
		eventList = eventService.getByAdvice(advice.getId());
		
		//Equipamentos
		equipmentList = equipmentService.findByStudent(user.getId(), Boolean.TRUE);
		for( Equipment e:equipmentList ) {
    		double distance = e.getKilometers();
    		distance += equipmentService.findDistance(e.getId());
    		e.setKilometers( distance );
    	}
		
		//Treino
		proximoTreino = nextTraining(user);
		
		//Atividades
		activityList = activityService.findByUserId(user.getId());		
		if ( activityList!=null ) {
			activity = activityList.get(0);
			//loadMap(activity);
			loadActivityList();
		}
		
		return "pm:init";
	}	
	
    public void printSpreadsheet() {
    	
    	if( spreadsheet==null ) {
    		return;// "pm:init";
    	}
    	
    	Pessoa user = pessoaService.loadByEmailActive(login,true);
    	Advice advice = user.getAdvice();	
    	
		boolean usarLogoRunPlanner;
		
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("PLANILHA_ID", spreadsheet.getId().intValue());
    	
    	if (advice != null && advice.getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(advice.getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}
    	param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de treino");
    	
    	reportService.gerar(ReportServiceImpl.PLANILHA_ALUNO_MES_LISTAGEM, param, null, usarLogoRunPlanner);

		//return "pm:init";
    }
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	private void loadActivityList() {		
		String[] months = new DateFormatSymbols(new Locale("pt","BR")).getMonths();		
		
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
				
			}
		}
		
		activityDisplayList = new ArrayList<ActivityDisplay>();
		Enumeration<ActivityDisplay> elements = activityTable.elements();		
		ActivityDisplay display = elements.nextElement();
		setSelectedMonth(display);
		
		activityDisplayList.add(display);
	}
	
	@SuppressWarnings("unused")
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

	private void setSelectedMonth(ActivityDisplay display) {
		selectedMonth = display.getMonth() + "/" + display.getYear() + " ("+display.getSize()+" Atividades)";
	}
	
	@SuppressWarnings("unchecked")
	public void filterActivityList(ActionEvent actionEvent) {
		activityDisplayList = new ArrayList<ActivityDisplay>();
		
						
		Integer count = 0;
		Enumeration<ActivityDisplay> elements = activityTable.elements();
		while (elements.hasMoreElements()) {
			ActivityDisplay display = elements.nextElement();
			
			if ( count.intValue() == selectedDisplay.intValue() ) {
				activityDisplayList.add(display);
				setSelectedMonth(display);
				break;
			}
			
			count++;
		}
	}
	
	public List<Event> getEventList() {
		return eventList;
	}
	
	public List<Equipment> getEquipList() {
		return equipmentList; 
	}
	
	public Schedule getNextTraining() {
		return proximoTreino;
	}
	
	private Schedule nextTraining(Pessoa customer) {
				
		Schedule result = null;
		
		//Planilhas
		spreadsheetList = spreadsheetService.findByStudentDesc(customer.getId());
		if ( spreadsheetList!=null && spreadsheetList.size()>0 ) {
			spreadsheet = spreadsheetList.get(0);
		}
		
		//Buscar proximo treino
		result = spreadsheetService.findByStudentActual(customer.getId());
		
		setRhythmTable(customer.getClassification());
		
		return result;
	}
	
	private void setRhythmTable(RhythmTable rhythmTabel) {
		if(rhythmTabel==null) return;
		easyPace = rhythmTabel.getEasyPace();
		normalPace = rhythmTabel.getNormalPace();
		moderatePace = rhythmTabel.getModeratePace();
		mediumPace = rhythmTabel.getMediumPace();
		shootingPace = rhythmTabel.getShootingPace();
		strongPace = rhythmTabel.getStrongPace();
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
	
	
	//--Get and Set

	public long getEventDetailID() {
		return eventDetailID;
	}

	public void setEventDetailID(long eventDetailID) {
		this.eventDetailID = eventDetailID;
	}

	public Event getSelectedEvent() {
		return selectedEvent;
	}

	public void setSelectedEvent(Event selectedEvent) {
		this.selectedEvent = selectedEvent;
	}

	public Double getInitLat() {
		return initLat;
	}

	public Double getInitLon() {
		return initLon;
	}

	public MapModel getPolylineModel() {
		return polylineModel;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Activity getActivity() {
		return activity;
	}
	
	public List<ActivityDisplay> getActivityDisplayList() {
		return activityDisplayList;
	}

	public String getSelectedMonth() {
		return selectedMonth;
	}

	@SuppressWarnings("unchecked")
	public List<SelectItem> getMonthList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();

		Enumeration<ActivityDisplay> elements = activityTable.elements();
		Integer count = 0;
		while (elements.hasMoreElements()) {
			ActivityDisplay display = elements.nextElement();
			result.add(new SelectItem(count,display.getMonth()+"/"+display.getYear()));
			count++;
		}
		return result;
	}

	public Integer getSelectedDisplay() {
		return selectedDisplay;
	}

	public void setSelectedDisplay(Integer selectedDisplay) {
		this.selectedDisplay = selectedDisplay;
	}

	public String getAdviceName() {
		return adviceName;
	}

	public String getEasyPace() {
		return easyPace;
	}

	public String getNormalPace() {
		return normalPace;
	}

	public String getMediumPace() {
		return mediumPace;
	}

	public String getModeratePace() {
		return moderatePace;
	}

	public String getStrongPace() {
		return strongPace;
	}

	public String getShootingPace() {
		return shootingPace;
	}


	public Activity getSelectedActivity() {
		return selectedActivity;
	}


	public void setSelectedActivity(Activity selectedActivity) {
		this.selectedActivity = selectedActivity;
	}
	
}
