package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Route;


public interface RouteService extends GenericService<Route, Long>{
	
	List<Route> getByAdvice(Long adviceId);
	Route getRoutePath(Long routeId);
	
}
