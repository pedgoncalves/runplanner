package br.com.runplanner.service;

import java.util.Date;
import java.util.List;

import br.com.runplanner.domain.BodyMeasurements;

public interface BodyMeasurementsService extends GenericService<BodyMeasurements, Long> {
	List<BodyMeasurements> findByCustomerId(Long id);
	List<BodyMeasurements> findByCustomerIdAsc(Long id);
	List<BodyMeasurements> findByCustomerIdIntervalAsc(Long id, Date initialDate, Date finalDate);
}
