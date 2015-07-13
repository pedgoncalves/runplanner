package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
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

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.ActivityLap;
import br.com.runplanner.domain.ActivityTack;
import br.com.runplanner.domain.Equipment;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.EventPessoaActivity;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.EquipmentService;
import br.com.runplanner.service.EventPessoaActivityService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.view.util.Constants;

@Component("publicMBean")
@Scope("session")
public class PublicActivityMBean {

	private static final int PACE_GRAPH_DISTANCE = 200;

	private ActivityService activityService;
	private EventPessoaActivityService eventPessoaActivityService;
	private EquipmentService equipmentService;
    
    private Activity activity = new Activity();
    private ActivityLap lap;
    private String stringTime;
    private String observation;
    private String equipmentText; //Texto com os equipamentos utilizados

	private CartesianChartModel paceGraph;
	private CartesianChartModel altitudeGraph;
	private double maxAltitude = 0;
    
    private Pessoa user;
    private Event event;
    
    private Double initLat;
    private Double initLon;
        
    private MapModel polylineModel;  
    
    private Long activityId;
        
    @Autowired
    public PublicActivityMBean(ActivityService activityService,
    		EventPessoaActivityService eventPessoaActivityService,
    		EquipmentService equipmentService) { 
				
		this.activityService = activityService;
		this.eventPessoaActivityService = eventPessoaActivityService;
		this.equipmentService = equipmentService;
    }
       
    

	public void prepareActivity() throws Exception {
		activity = activityService.loadById(activityId);
		
		
		if ( activity==null ) {
			throw new Exception("Atividade nao encontrada!");
		}
		
		if ( activity.getObservation()!=null ) {
    		String obs = activity.getObservation();
    		obs = obs.replaceAll("\n", "<br/>");
    		activity.setObservation(obs);
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
    		if ( track.getTime() != null ) {
    			altitudeGraph = null;
    			return;
    		}
    		
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
    	
    	if ( distance == 0 && maxAltitude==0 ) {
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
		
		for( int i=1; i<distance; i++ ) {
			tick.append(i*1000);
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
	
	public String getDescription() {
		String result = "Sistema WEB voltado para treinadores e assessorias esportivas, que visa a organizacao da empresa, a elaboracao dos treinos e o acompanhamento do desempenho dos seus atletas";
		
		if (activity!=null && activity.getId()!=null) {
			result = activity.getSocialTitle();
			
		}
		
		return result;
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
	
	public StreamedContent getStreamedUserPhoto() {
				
		if( activity.getStudent().getId()==null ) {
			return getDefaultPhoto();
		}		
		
		Pessoa pessoa = activity.getStudent();		
		if ( pessoa.getPhoto()!= null ) {
			ByteArrayInputStream photoStream = new ByteArrayInputStream(pessoa.getPhoto());
			return new DefaultStreamedContent(photoStream, "image/jpeg");
		}
		else {
			return getDefaultPhoto();
		}
	}
	
	public StreamedContent getAdviceLogo() throws IOException {

		byte[] adviceLogo = activity.getStudent().getAdvice().getLogo();
		
		if ( adviceLogo==null ) {							
			String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_PHOTO;
			FileInputStream is = new FileInputStream(new File(photoPath));
			adviceLogo = new byte[is.available()];
			is.read(adviceLogo);
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(adviceLogo);
		return new DefaultStreamedContent(stream, "image/jpeg");
	}
	
	private DefaultStreamedContent getDefaultPhoto() {
		String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_PHOTO;
		
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

	public Pessoa getUser() {
		return user;
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

	public CartesianChartModel getPaceGraph() {
		return paceGraph;
	}

	public String getEquipmentText() {
		return equipmentText;
	}

	public void setEquipmentText(String equipmentText) {
		this.equipmentText = equipmentText;
	}


	public Long getActivityId() {
		return activityId;
	}


	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}
	
}
