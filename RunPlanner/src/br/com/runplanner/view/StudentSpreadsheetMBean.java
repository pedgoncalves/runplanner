package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.xml.parsers.ParserConfigurationException;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
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
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Route;
import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.Spreadsheet;
import br.com.runplanner.domain.SpreadsheetType;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.BodyMeasurementsService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.service.RouteService;
import br.com.runplanner.service.ScheduleService;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.CustonScheduleEvent;
import br.com.runplanner.view.util.GarminTCXParser;

@Component("studentSpreadsheetMBean")
@Scope("session")
public class StudentSpreadsheetMBean extends BasicMBean {

	private static final String PAGES_STUDENT_SPREADSHEET = "/pages/student/spreadsheet";
	private static final String LEFT_HEADER_TEMPLATE_MULTI = "prev, next, today";

	private ScheduleModel eventModel;  
    
	private Schedule schedule = new Schedule();
    private Spreadsheet spreadsheet = new Spreadsheet();
    private List<Spreadsheet> spreadsheetList = new ArrayList<Spreadsheet>();
    private List<SelectItem> spreadsheetListSelect = new ArrayList<SelectItem>();
    
    private CustonScheduleEvent selectedEvent; 
    
    private Integer selectedMonth;
    private Integer selectedYear;
    private Integer selectedDay;
    
    private String leftHeaderTemplate = "";
    
    private Date initialDate = new Date();

	@Autowired
	private RouteService routeService;
    
    private ScheduleService scheduleService;
    private SpreadsheetService spreadsheetService;
    private ActivityService activityService;
    private ReportService reportService;
    
    private UploadedFile tcxFile;
    private Date fileDate;
    private Pessoa customer;
    

	private MapModel mapModel;
	private Double initLat;
    private Double initLon;
    
    
    @Autowired
    public StudentSpreadsheetMBean(ScheduleService scheduleService, 
    		SpreadsheetService spreadsheetService,
    		ActivityService activityService,
    		BodyMeasurementsService bodyMeasurementsService,
    		ReportService reportService) { 
		this.eventModel = new LazyScheduleModel() {

			private static final long serialVersionUID = 8796136580164685526L;

			@Override
			public void loadEvents(Date start, Date end) {
				clear();
				loadAllEvents();
			}
		};
		
		this.schedule = new Schedule();
		this.spreadsheet = new Spreadsheet();
		this.spreadsheetList = new ArrayList<Spreadsheet>();
		
		this.scheduleService = scheduleService;
		this.spreadsheetService = spreadsheetService;
		this.activityService=activityService;
		this.reportService=reportService;
		
		this.selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
		this.selectedYear = Calendar.getInstance().get(Calendar.YEAR);
		
    }
    
    @SuppressWarnings("deprecation")
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
    		
    		//TODO BUSCAR ATIVIDADES
    		/*if ( schedule.getActivity()!=null && schedule.getActivity().getId()!=null ) {
    			CustonScheduleEvent event = new CustonScheduleEvent("Activity!",schedule.getDate());
    			event.setScheduleId(schedule.getId());    		
    			eventModel.addEvent(event);
    		}*/
    	}
    }
    
    public void goToReport() {
    	Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;
		
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	param.put("PLANILHA_ID", spreadsheet.getId().intValue());
    	param.put(ReportServiceImpl.NOME_ARQUIVO, "Planilha de treino para "+spreadsheet.getStudent().getNome());
    	
    	if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}
    	
    	if(!reportService.gerar(ReportServiceImpl.PLANILHA_ALUNO_MES_LISTAGEM, param, null, usarLogoRunPlanner)){
    		addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
    	}
    }
        
    public String save() {
    	return null;
    }
    
    public String goToCreate() {    
    	return null;
    }
        
    public String goToList() {
		this.schedule = new Schedule();
		this.spreadsheet = new Spreadsheet();
    	    	
    	this.initialDate = new Date();
		this.selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
		this.selectedYear = Calendar.getInstance().get(Calendar.YEAR);
		this.selectedDay = Calendar.getInstance().get(Calendar.DATE);
		
		
		Pessoa user = getSessionUser();		
		spreadsheetList = spreadsheetService.findByStudent(user.getId());
		
		if ( spreadsheetList.size()>0 ) {
			
			boolean nowadaySpread = false;
			for( Spreadsheet spread:spreadsheetList ) {
				if ( spread.getMonth().intValue()==selectedMonth.intValue() 
						&& spread.getYear().intValue()==selectedYear.intValue() ) {
					
					this.spreadsheet = spread;
					nowadaySpread = true;
					break;
				}
			}
			
			if ( !nowadaySpread ) {
				this.spreadsheet =  spreadsheetList.get(spreadsheetList.size()-1);							
			}
			
			this.selectedMonth = spreadsheet.getMonth();
			this.selectedYear = spreadsheet.getYear();				
			if( spreadsheet.getType().equals(SpreadsheetType.WEEK) ) {
				this.selectedDay = spreadsheet.getDay();
			}
			else {
				this.selectedDay = 1;
			}
			
			GregorianCalendar spreadDate = new GregorianCalendar();
	    	spreadDate.set(GregorianCalendar.DAY_OF_MONTH, selectedDay);
	    	spreadDate.set(GregorianCalendar.MONTH, selectedMonth);
	    	spreadDate.set(GregorianCalendar.YEAR, selectedYear);
	    	this.initialDate = spreadDate.getTime();
		}

	    spreadsheetListSelect = new ArrayList<SelectItem>();
		for (Spreadsheet spread: spreadsheetList) {
			spreadsheetListSelect.add(new SelectItem(spread.getId(),spread.getTextMonth()+"/"+spread.getYear()));
		}
		
		customer = getSessionUser(); 
		
		setSelectedMenu(Constants.MENU_SPREADSHEET);
		
		verifyLeftHeader();
    	return PAGES_STUDENT_SPREADSHEET;
    }
    
    private void verifyLeftHeader() {
    	leftHeaderTemplate = "";
    	if ( spreadsheet.getSchedules()==null || spreadsheet.getSchedules().size()==0 ) return;
    	
    	int firstMonth = spreadsheet.getSchedules().get(0).getDate().getMonth();  
    	for(Schedule schedule:spreadsheet.getSchedules()) {
    		int month = schedule.getDate().getMonth();
    		if (month!=firstMonth) {
    			leftHeaderTemplate = LEFT_HEADER_TEMPLATE_MULTI;
    			return;
    		}
    	}
    }
    
    public void updateSpreadsheet() {
    	
    	this.spreadsheet =  spreadsheetService.loadById(spreadsheet.getId());		

    	if ( spreadsheet==null || spreadsheet.getId()==null ) {
    		spreadsheet = new Spreadsheet();
    		return;
    	}
    	
		this.selectedMonth = spreadsheet.getMonth();
		this.selectedYear = spreadsheet.getYear();
		if( spreadsheet.getType().equals(SpreadsheetType.WEEK) ) {
			this.selectedDay = spreadsheet.getDay();
		}
		else {
			this.selectedDay = 1;
		}
		
		GregorianCalendar spreadDate = new GregorianCalendar();
    	spreadDate.set(GregorianCalendar.DAY_OF_MONTH, selectedDay);
    	spreadDate.set(GregorianCalendar.MONTH, selectedMonth);
    	spreadDate.set(GregorianCalendar.YEAR, selectedYear);
    	this.initialDate = spreadDate.getTime();
    	
    	verifyLeftHeader();
    }
    
    public String goToView() {
    	updateSpreadsheet();
    	
    	Pessoa user = getSessionUser();		
		spreadsheetList = spreadsheetService.findByStudent(user.getId());
		
		spreadsheetListSelect = new ArrayList<SelectItem>();
		for (Spreadsheet spread: spreadsheetList) {
			spreadsheetListSelect.add(new SelectItem(spread.getId(),spread.getTextMonth()+"/"+spread.getYear()));
		}
		
		customer = getSessionUser(); 
		
		setSelectedMenu(Constants.MENU_SPREADSHEET);
    	
    	return PAGES_STUDENT_SPREADSHEET;
    }
    

	@SuppressWarnings("deprecation")
	public void handleFileUpload() {  
		
		if ( fileDate==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "student.spread.date.mandatory");
			return;
		}
		
		this.schedule = new Schedule();
		for(Schedule s: spreadsheet.getSchedules()) {
			if ( s.getDate().getDate() == fileDate.getDate() &&
				s.getDate().getMonth() == fileDate.getMonth() &&
				s.getDate().getYear() == fileDate.getYear() ) {
				this.schedule = s;
				break;
			}
		}		

		if ( this.schedule.getId()==null ) {
			schedule.setDate(fileDate);
			scheduleService.persist(schedule);
			
			spreadsheet.getSchedules().add(schedule);
			try {
				spreadsheetService.update(spreadsheet);
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}
		}
		
		
        byte[] file = tcxFile.getContents();
        File f = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath("arquivoTCX.xml"));
        FileOutputStream out;
        try {
            out = new FileOutputStream(f);
            out.write(file);
            out.flush();
           
            GarminTCXParser parser = new GarminTCXParser(f);
            
            Activity activity = parser.parse();
            activity.setDate(fileDate);
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
            } catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}
           
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");	
        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");	
        } catch (SAXException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");	
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");	
        } catch (ParseException e) {
        	e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "activity.file.process.erro");	
		}	
    }  
    
    //Eventos do Schedule
    public void addEvent(ActionEvent actionEvent) {   
    	if(spreadsheet.getId() == null ) {
             return;
    	}   	
    	
    	GregorianCalendar eventDate = new GregorianCalendar();
    	eventDate.setTime(schedule.getDate());
    	Integer eventMonth = eventDate.get(Calendar.MONTH);
    	Integer eventYear = eventDate.get(Calendar.YEAR);
    	
    	if ( !eventMonth.equals(selectedMonth) || !eventYear.equals(selectedYear) ) {
    		if ( spreadsheet.getSchedules().isEmpty() )  {
    			this.selectedMonth = eventMonth;
        		this.selectedYear = eventYear;
    		}
    		else {
    			addMessage(FacesMessage.SEVERITY_ERROR, "spreadsheet.template.err.diferent.month");
    			return;        		
        	}
    	}        
    	
    	
    	if(schedule.getId() == null) {
    		schedule = scheduleService.persist(schedule);    		    		
    		spreadsheet.getSchedules().add(schedule);
    		
    		spreadsheet.setMonth(selectedMonth);
    		spreadsheet.setYear(selectedYear);
    		try {
    			spreadsheetService.update(spreadsheet);            
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}
    	}
        else {
        	try {
        		scheduleService.update(schedule); 
        		spreadsheet.getSchedules().remove(schedule);   
        		spreadsheet.getSchedules().add(schedule); 
        	}
        	 catch (EntityNotFoundException e) {
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
    	
    	loadRoute(schedule.getRoute());
    	
    	//TODO BUSCAR ATIVIDADES
    	/*if ( schedule.getActivity()!=null ) {
	    	List<ActivityTack> trackList = activityService.findTracksById(schedule.getActivity().getId());
	    	  
	    	Polyline polyline = new Polyline();  
	    	for( ActivityTack track: trackList ) {
	    		LatLng coord = new LatLng(track.getLatitudeDegrees(), track.getLongitudeDegrees());
	    		polyline.getPaths().add(coord);  
	    	}
	    	
	    	polyline.setStrokeWeight(10);  
	        polyline.setStrokeColor("#FF9900");  
	        polyline.setStrokeOpacity(0.7);  
	          
	        polylineModel.addOverlay(polyline);  
    	}*/
          
    }
    
    public void loadRoute(Route route) {
    	
    	if (route == null || route.getId()==null || route.getId()<=0 ) {
    		mapModel = null;
    		return;
    	}
    	
    	route = routeService.getRoutePath(route.getId());
    	
    	List<br.com.runplanner.domain.LatLng> positions = route.getPositions();
		br.com.runplanner.domain.LatLng p0 = positions.get(0);
		
		initLat = p0.getLatitudeDegrees();
		initLon = p0.getLongitudeDegrees();
		mapModel = new DefaultMapModel();
		
		Polyline polyline = new Polyline();
		polyline.setStrokeWeight(5);  
		polyline.setStrokeColor("#6F0000");  
		polyline.setStrokeOpacity(0.7);  

		boolean first = true;
		for(br.com.runplanner.domain.LatLng pos:positions) {
			LatLng coord = new LatLng(pos.getLatitudeDegrees(), pos.getLongitudeDegrees());
			if ( first ) {
				Marker init = new Marker(coord, "Inicio");
				init.setIcon("/runplanner/images/icon_green.png"); 
				mapModel.addOverlay(init);
				first = false;
			}
			else {
				Marker end = new Marker(coord, "");
				mapModel.addOverlay(end);
			}
			polyline.getPaths().add(coord);
		}
				
		List<Marker> markers = mapModel.getMarkers();
		for (Marker m:markers ) {
			m.setIcon("/runplanner/images/icon_grey.png");
			m.setDraggable(false);
		}
		markers.get(0).setIcon("/runplanner/images/icon_green.png");
		markers.get(markers.size()-1).setIcon("/runplanner/images/icon_red.png");
		markers.get(markers.size()-1).setDraggable(true);
		
		mapModel.addOverlay(polyline);
    }

	@Override
	public String delete() {
		return null;
	}

	@Override
	public String goToModify() {
		return null;
	}  
	
	public String getType() {
		if ( spreadsheet==null || spreadsheet.getType().equals(SpreadsheetType.MONTH) ) {
			return "month";
		}
		else {
			return "basicWeek";
		}
	}

    //Getters and Setters  
	
	public StreamedContent getPhoto() {
		if ( customer.getPhoto()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(customer.getPhoto());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
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

	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	}

	public void setSpreadsheet(Spreadsheet spreadsheet) {
		this.spreadsheet = spreadsheet;
	}

	public List<SelectItem> getSpreadsheetListSelect() {
		return spreadsheetListSelect;
	}

	public void setSpreadsheetList(List<Spreadsheet> spreadsheetList) {
		this.spreadsheetList = spreadsheetList;
	}
	
	public UploadedFile getTcxFile() {
		return tcxFile;
	}

	public void setTcxFile(UploadedFile tcxFile) {
		this.tcxFile = tcxFile;
	}
	
	public Date getFileDate() {
		return fileDate;
	}

	public void setFileDate(Date fileDate) {
		this.fileDate = fileDate;
	}

	public Pessoa getCustomer() {
		return customer;
	}

	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}

	public Integer getSelectedDay() {
		return selectedDay;
	}

	public void setSelectedDay(Integer selectedDay) {
		this.selectedDay = selectedDay;
	}
	
	public double getDialogSize() {
		double result = 500d;
		
		if ( schedule!=null && schedule.getRoute()!=null && schedule.getRoute().getId()!=null ) {
			result = 900d;
		}
		
		return result;
	}

	public MapModel getMapModel() {
		return mapModel;
	}

	public Double getInitLat() {
		return initLat;
	}

	public Double getInitLon() {
		return initLon;
	}

	public String getLeftHeaderTemplate() {
		return leftHeaderTemplate;
	}

}
