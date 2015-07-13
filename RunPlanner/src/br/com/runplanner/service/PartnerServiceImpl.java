package br.com.runplanner.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.Partner;

@Service("partnerService")
public class PartnerServiceImpl extends GenericServiceImpl<Partner, Long> implements PartnerService {

	@SuppressWarnings("unchecked")
	public List<Partner> findByAdvice(Long adviceId) {
		Query query = entityManager.createNamedQuery("Partner.findByAdvice");
		query.setParameter("adviceId", adviceId);
		return (List<Partner>)query.getResultList();
	}

}
