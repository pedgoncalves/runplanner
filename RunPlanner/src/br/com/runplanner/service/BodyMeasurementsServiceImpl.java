package br.com.runplanner.service;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.BodyMeasurements;

@Service("bodyMeasurementsServiceImpl")
public class BodyMeasurementsServiceImpl extends GenericServiceImpl<BodyMeasurements, Long> implements
		BodyMeasurementsService {

	@SuppressWarnings("unchecked")
	public List<BodyMeasurements> findByCustomerId(Long id) {
		Query query = entityManager.createNamedQuery("BodyMeasurements.findByCustomerId");
		query.setParameter("customerId", id);

		return (List<BodyMeasurements>) query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<BodyMeasurements> findByCustomerIdAsc(Long id) {
		Query query = entityManager.createNamedQuery("BodyMeasurements.findByCustomerIdAsc");
		query.setParameter("customerId", id);

		return (List<BodyMeasurements>) query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<BodyMeasurements> findByCustomerIdIntervalAsc(Long id, Date initialDate, Date finalDate) {
		Query query = entityManager.createNamedQuery("BodyMeasurements.findByCustomerIdIntervalAsc");
		query.setParameter("customerId", id);
		query.setParameter("initialDate", initialDate);
		query.setParameter("finalDate", finalDate);

		return (List<BodyMeasurements>) query.getResultList();
	}
	
		
}
