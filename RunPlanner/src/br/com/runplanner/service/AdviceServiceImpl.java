package br.com.runplanner.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.StatusAdvice;
import br.com.runplanner.exception.AdviceCantBeDeletedException;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("adviceService")
public class AdviceServiceImpl extends GenericServiceImpl<Advice, Long>
		implements AdviceService {

	@Autowired
	private PessoaService pessoaService;

	@Transactional
	public List<Advice> loadAllEager() {
		List<Advice> list = this.loadAll();
		for (Advice a : list) {
			a.getPessoas().size();
		}
		return list;
	}

	@Transactional
	public Advice loadByIdEager(Long id) {
		Advice a = super.loadById(id);
		a.getPessoas().size();
		return a;
	}

	@Transactional
	@SuppressWarnings("unchecked")
	public List<Advice> loadByActiveName(boolean active, String name) {
		Query query = entityManager
				.createNamedQuery("Advice.loadByActiveName");
		query.setParameter("active", active);
		query.setParameter("name", "%"+name+"%");
		
		List<Advice> list = (List<Advice>) query.getResultList();
		for (Advice a : list) {
			a.getPessoas().size();
		}
		return list;
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	public List<Advice> loadAllActiveEager(boolean active) {
		Query query = entityManager
				.createNamedQuery("Advice.loadAllActiveEager");
		query.setParameter("active", active);
		List<Advice> list = (List<Advice>) query.getResultList();
		for (Advice a : list) {
			a.getPessoas().size();
		}
		return list;
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	public List<Advice> loadAll() {
		Query query = entityManager
				.createNamedQuery("Advice.loadAll");
		List<Advice> list = (List<Advice>) query.getResultList();
		for (Advice a : list) {
			a.getPessoas().size();
		}
		return list;
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	public List<Advice> loadByStatus(StatusAdvice status) {
		Query query = entityManager
				.createNamedQuery("Advice.loadByStatus");
		query.setParameter("status", status);
		List<Advice> list = (List<Advice>) query.getResultList();
		for (Advice a : list) {
			a.getPessoas().size();
		}
		return list;
	}
	
	@Transactional
	public void deleteById(Long id) throws EntityNotFoundException, AdviceCantBeDeletedException {
		Advice a = super.loadById(id);
		if(!a.getStatus().equals(StatusAdvice.PAGANDO)) {
			super.deleteById(id);
		} else {
			throw new AdviceCantBeDeletedException();
		}
	}

	@Transactional
	public void desativarAssessoriaResponsaveis(Advice advice) {
		try {
			List<Pessoa> customers = pessoaService.getByAdvice(advice.getId(), true);
			for (Pessoa owner : customers) {
				owner.setActive(false);
				pessoaService.update(owner);
			}

			advice.setActive(false);
			this.update(advice);
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Transactional
	public void ativarAssessoriaResponsaveis(Advice advice) {
		try {
			List<Pessoa> customers = pessoaService.getByAdvice(advice.getId(), false);
			for (Pessoa owner : customers) {
				owner.setActive(true);
				pessoaService.update(owner);
			}

			advice.setActive(true);
			this.update(advice);
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

}