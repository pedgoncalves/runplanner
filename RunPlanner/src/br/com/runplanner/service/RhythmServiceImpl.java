package br.com.runplanner.service;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.RhythmTable;

@Service("rhythmService")
public class RhythmServiceImpl extends GenericServiceImpl<RhythmTable, Long> implements RhythmService {

	@SuppressWarnings("unchecked")
	public List<RhythmTable> getByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("RhythmTable.findByAdvice");
		query.setParameter("adviceId", adviceId);
		return (List<RhythmTable>)query.getResultList();
	}
	
	public RhythmTable getByAdviceClassification(Long adviceId, String classification) {
		Query query = entityManager.createNamedQuery("RhythmTable.findByAdviceClassification");
		query.setParameter("adviceId", adviceId);
		query.setParameter("classification", classification);
		
		try {
			return (RhythmTable)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}
	}

	public RhythmTable getByCustomer(Long customerId) {
		Query query = entityManager.createNamedQuery("RhythmTable.findByCustomer");
		query.setParameter("customerId", customerId);
		
		try {
			return (RhythmTable)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}
	}
	
}