package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.RhythmTable;

public interface RhythmService extends GenericService<RhythmTable, Long>{
	List<RhythmTable> getByAdvice(Long adviceId);
	RhythmTable getByAdviceClassification(Long adviceId, String classification);
	RhythmTable getByCustomer(Long customerId);
}
