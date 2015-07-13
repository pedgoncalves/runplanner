package br.com.runplanner.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Route;

@Service("routeService")
public class RouteServiceImpl extends GenericServiceImpl<Route, Long> implements RouteService {

	@SuppressWarnings("unchecked")
	public List<Route> getByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("Route.findByAdvice");
		query.setParameter("adviceId", adviceId);
		return (List<Route>)query.getResultList();
	}

	@Transactional
	public Route getRoutePath(Long routeId) {
		Route result = loadById(routeId);
		if (result!=null)
			result.getPositions().size();
		return result;
	}
	

}