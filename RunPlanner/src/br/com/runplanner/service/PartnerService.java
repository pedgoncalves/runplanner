package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Partner;


public interface PartnerService extends GenericService<Partner, Long> {
	
	List<Partner> findByAdvice(Long adviceId);
	
}
