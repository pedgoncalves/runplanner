package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.StatusAdvice;


public interface AdviceService extends GenericService<Advice, Long>{
	
	List<Advice> loadAllEager();
	List<Advice> loadAllActiveEager(boolean active);
	List<Advice> loadAll();
	Advice loadByIdEager(Long id);
	List<Advice> loadByActiveName(boolean active, String name);
	void desativarAssessoriaResponsaveis(Advice advice);
	void ativarAssessoriaResponsaveis(Advice advice);
	List<Advice> loadByStatus(StatusAdvice status);
}
