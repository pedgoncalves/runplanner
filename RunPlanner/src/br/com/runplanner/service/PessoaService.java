package br.com.runplanner.service;

import java.util.Date;
import java.util.List;

import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;

public interface PessoaService extends GenericService<Pessoa, Long>{
	
	List<Pessoa> getByTipoPessoa(TipoPessoa tipoPessoa);
	List<Pessoa> getByTipoPessoaAdvice(TipoPessoa tipoPessoa, Long adviceId);
	List<Pessoa> getByAdvice(Long adviceId, boolean active);
	Long getSumByAdviceActiveTipoPessoa(Long adviceId, boolean active, TipoPessoa tipoPessoa);
	List<Pessoa> getByTipoPessoaAdviceClassification(TipoPessoa tipoPessoa, Long adviceId, Long rhythmId);
	Pessoa getByNameTipoPessoaAdvice(String nome, TipoPessoa tipoPessoa, Long adviceId);
	List<Pessoa> getByTipoPessoaAdviceAniversario(TipoPessoa tipoPessoa, Long adviceId, int birthMonth);
	List<Pessoa> loadByEmail(String email);
	Pessoa loadByEmailActive(String email, boolean active);
	List<Pessoa> getByTipoPessoaAdviceActive(TipoPessoa tipoPessoa, Long adviceId, boolean active);
	List<Pessoa> getByNameTipoPessoaAdviceActive(String name, TipoPessoa tipoPessoa, Long adviceId, boolean active);
	List<Pessoa> getByNameAdviceActive(String name, Long adviceId, boolean active);
	List<Pessoa> getByTipoPessoaAdviceActiveTeam(TipoPessoa tipoPessoa, Long adviceId, boolean active, Long teamId);
	List<Pessoa> getByNameTipoPessoaAdviceActiveTeam(String name, TipoPessoa tipoPessoa, Long adviceId, boolean active, Long teamId);
	List<Pessoa> getByBitNameTipoPessoaAdvice(String nome, TipoPessoa tipoPessoa, Long adviceId);
	Pessoa loadByIdBodyMeasurements(Long id);
	Pessoa loadByIdFull(Long id);
	Pessoa loadByIdClassification(Long id);
	void deleteById(Long id) throws EntityNotFoundException;	
	void updateLoginTime(Long id, Date time);
	List<Pessoa> getByTipoPessoaAdviceAniversario(TipoPessoa tipoPessoa, Long adviceId, int birthMonth, int day);
}
