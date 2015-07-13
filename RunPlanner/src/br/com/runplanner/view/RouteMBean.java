package br.com.runplanner.view;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.event.map.StateChangeEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Route;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.RouteService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.view.util.Constants;

@Component("routeMBean")
@Scope("session")
public class RouteMBean extends BasicMBean {
	
	private final String ROUTE_LIST_PAGE = "/pages/route/routeList";
	private final String ROUTE_FORM_PAGE = "/pages/route/route";
	
	@Autowired
	private RouteService routeService;
	
	private List<Route> routeList;
	
	private Route route;
	
	private MapModel mapModel;  
	
	private Double initLat;
    private Double initLon;
    private int mapZoom;
    private Double mapDistance;

	@Override
	public String goToList() {
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		route = new Route();
		
		routeList = routeService.getByAdvice(advice.getId());

		setSelectedMenu(Constants.MENU_SPREADSHEET);
		return ROUTE_LIST_PAGE;
	}

	@Override
	public String goToCreate() {
		mapModel = new DefaultMapModel();
		initLat = -14.127675;
		initLon = -45.139964;
		mapZoom = 5;
		mapDistance=0d;
		route = new Route();
		
		setSelectedMenu(Constants.MENU_SPREADSHEET);
		return ROUTE_FORM_PAGE;
	}

	@Override
	public String goToModify() {
		this.route = routeService.getRoutePath(route.getId());
    	
		if ( route==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		List<br.com.runplanner.domain.LatLng> positions = route.getPositions();
		br.com.runplanner.domain.LatLng p0 = positions.get(0);
		
		initLat = p0.getLatitudeDegrees();
		initLon = p0.getLongitudeDegrees();
		mapZoom = 15;
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
		
		updateDistanceMap(markers);		
		mapModel.addOverlay(polyline);
		
		setSelectedMenu(Constants.MENU_SPREADSHEET);
		return ROUTE_FORM_PAGE;
	}

	@Override
	public String delete() {
		if ( route!=null && route.getId()!=null ) {			
			
			try{
				routeService.deleteById(route.getId());
	    		addMessage(FacesMessage.SEVERITY_INFO, "route.delete.sucess");
			}
			catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
			} 
		}
		
		return goToList();
	}

	@Override
	public String save() {
		
		List<Marker> markers = mapModel.getMarkers();
		if (markers==null || markers.size()<2) {
			addMessage(FacesMessage.SEVERITY_INFO, "route.save.path.error");
			return null;
		}
		
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		

	
		
		if( route.getId()==null ) {
			route.setAdvice(advice);
			route = routeService.persist(route);
            addMessage(FacesMessage.SEVERITY_INFO, "route.save.sucess");
		}
		else {
			try {
				routeService.update(route);
    	    	addMessage(FacesMessage.SEVERITY_INFO, "route.edit.sucess");
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
		}
		
		
		//Salvar o mapa
		List<br.com.runplanner.domain.LatLng> positions = new ArrayList<br.com.runplanner.domain.LatLng>();    	
		for (Marker m:markers ) {
			br.com.runplanner.domain.LatLng position = new br.com.runplanner.domain.LatLng();
			position.setLatitudeDegrees(m.getLatlng().getLat());
			position.setLongitudeDegrees(m.getLatlng().getLng());
			position.setRoute(route);
			positions.add(position);
		}
		route.setPositions(positions);
		try {
			routeService.update(route);
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
		}
		
		return goToList();
	}
	
	public void clearPath() {
		mapModel = new DefaultMapModel();
		updateDistanceMap(mapModel.getMarkers());
	}
	
    public void onPointSelect(PointSelectEvent event) {  
    	
    	if (mapModel==null) {
    		mapModel = new DefaultMapModel();
    	}
    	
    	boolean first = true;
    	List<Polyline> lines = mapModel.getPolylines();
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
			mapModel.addOverlay(init);
			first = false;
		}
		else {
			Marker end = new Marker(coord, "");
			mapModel.addOverlay(end);
		}
		polyline.getPaths().add(coord);	
		
				
		List<Marker> markers = mapModel.getMarkers();
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
		
		mapModel.addOverlay(polyline);
    } 
    
    public void onMarkerDrag(MarkerDragEvent event) {
    	Marker marker = event.getMarker(); 
    	
    	List<Polyline> lines = mapModel.getPolylines();
    	Polyline polyline = lines.get(0);
    	polyline.getPaths().remove( polyline.getPaths().size()-1 );
    	polyline.getPaths().add(marker.getLatlng());
    	
    	List<Marker> markers = mapModel.getMarkers();
    	markers.remove( markers.size()-1 );    	
    	mapModel.addOverlay(marker);
    	
    	updateDistanceMap(mapModel.getMarkers());
    }
    
    public void onStateChange(StateChangeEvent event) {  
        mapZoom = event.getZoomLevel();
        initLat = event.getCenter().getLat();
        initLon = event.getCenter().getLng();
    }
    
    public void onMarkerSelect(OverlaySelectEvent event) {  
    	
    	if ( !(event.getOverlay() instanceof Marker) ) return;
    	
    	Marker marker = (Marker) event.getOverlay();  
    	
    	List<Polyline> lines = mapModel.getPolylines();
    	Polyline polyline = lines.get(0);
    	
    	//Remove da linha
    	polyline.getPaths().remove( marker.getLatlng() );
    	
    	//Remove marcador
    	mapModel.getMarkers().remove(marker);
    	
    	List<Marker> markers = mapModel.getMarkers();
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
    	mapModel.addOverlay(polyline);
    } 

    private void updateDistanceMap(List<Marker> markers) {
    	mapDistance = 0d;
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
				
				mapDistance += Utils.distance(p1Lat, p1Lon, p2Lat, p2Lon);
				
				p1Lat = p2Lat;
				p1Lon = p2Lon;
			}
    	}
    }


    //Get e set
    
	public MapModel getMapModel() {
		return mapModel;
	}

	public void setMapModel(MapModel mapModel) {
		this.mapModel = mapModel;
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

	public int getMapZoom() {
		return mapZoom;
	}

	public void setMapZoom(int mapZoom) {
		this.mapZoom = mapZoom;
	}

	public Double getMapDistance() {
		return mapDistance;
	}

	public void setMapDistance(Double mapDistance) {
		this.mapDistance = mapDistance;
	}

	public void setRouteService(RouteService routeService) {
		this.routeService = routeService;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public List<Route> getRouteList() {
		return routeList;
	}

}
