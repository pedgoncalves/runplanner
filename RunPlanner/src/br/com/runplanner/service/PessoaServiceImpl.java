package br.com.runplanner.service;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("pessoaService")
public class PessoaServiceImpl extends GenericServiceImpl<Pessoa, Long> implements PessoaService {
	
	@Autowired
	private EventService eventService;	
	@Autowired
	private ActivityService activityService;
	@Autowired
	private TeamService teamService;	
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private CommentService commentService;
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByAdvice(Long adviceId, boolean active) {
		Query query = entityManager.createNamedQuery("Pessoa.findByAdvice");
		query.setParameter("adviceId", adviceId);
		query.setParameter("active", active);
		return (List<Pessoa>)query.getResultList();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByTipoPessoa(TipoPessoa tipoPessoa) {
		Query query = entityManager.createNamedQuery("Pessoa.findByTipo");
		query.setParameter("tipoPessoa", tipoPessoa);
		return (List<Pessoa>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByTipoPessoaAdvice(TipoPessoa tipoPessoa, Long adviceId) {
		Query query = entityManager.createNamedQuery("Pessoa.findByTipoAdvice");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		return (List<Pessoa>)query.getResultList();
	}

	public Long getSumByAdviceActiveTipoPessoa(Long adviceId, boolean active, TipoPessoa tipoPessoa) {
		Query query = entityManager.createNamedQuery("Pessoa.sumByAdviceActiveTipoPessoa");
		query.setParameter("adviceId", adviceId);
		query.setParameter("active", active);
		query.setParameter("tipoPessoa", tipoPessoa);
		try {
			return (Long)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}	
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByTipoPessoaAdviceActive(TipoPessoa tipoPessoa, Long adviceId, boolean active) {
		Query query = entityManager.createNamedQuery("Pessoa.findByTipoAdviceActive");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		query.setParameter("active", active);
		return (List<Pessoa>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByNameTipoPessoaAdviceActive(String name, TipoPessoa tipoPessoa, Long adviceId, boolean active) {
		Query query = entityManager.createNamedQuery("Pessoa.findByNameTipoAdviceActive");
		query.setParameter("nome", "%" + name + "%");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		query.setParameter("active", active);
		return (List<Pessoa>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByNameAdviceActive(String name, Long adviceId, boolean active) {
		Query query = entityManager.createNamedQuery("Pessoa.findByNameAdviceActive");
		query.setParameter("nome", "%" + name + "%");
		query.setParameter("adviceId", adviceId);
		query.setParameter("active", active);
		return (List<Pessoa>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByTipoPessoaAdviceActiveTeam(TipoPessoa tipoPessoa, Long adviceId, boolean active, Long teamId) {
		Query query = entityManager.createNamedQuery("Pessoa.findByTipoAdviceActiveTeam");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		query.setParameter("active", active);
		query.setParameter("teamId", teamId);
		return (List<Pessoa>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByNameTipoPessoaAdviceActiveTeam(String name, TipoPessoa tipoPessoa, Long adviceId, boolean active, Long teamId) {
		Query query = entityManager.createNamedQuery("Pessoa.findByNameTipoAdviceActiveTeam");
		query.setParameter("nome", "%" + name + "%");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		query.setParameter("active", active);
		query.setParameter("teamId", teamId);
		return (List<Pessoa>)query.getResultList();
	}
	
	public Pessoa getByNameTipoPessoaAdvice(String nome, TipoPessoa tipoPessoa, Long adviceId) {
		Query query = entityManager.createNamedQuery("Pessoa.findByNomeTipoAdvice");
		query.setParameter("nome", nome);
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		
		try {
			return (Pessoa)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}		
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByBitNameTipoPessoaAdvice(String nome, TipoPessoa tipoPessoa, Long adviceId) {
		Query query = entityManager.createNamedQuery("Pessoa.findByBitNomeTipoAdvice");
		nome = "%"+nome+"%";
		query.setParameter("nome", nome);
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		
		return (List<Pessoa>)query.getResultList();
				
	}
		
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByTipoPessoaAdviceAniversario(TipoPessoa tipoPessoa, Long adviceId, int birthMonth) {
		Query query = entityManager.createNamedQuery("Pessoa.findByTipoAdviceAniversario");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		query.setParameter("month", birthMonth+1);
		query.setParameter("active", true);
		
		return (List<Pessoa>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByTipoPessoaAdviceAniversario(TipoPessoa tipoPessoa, Long adviceId, int birthMonth, int day) {
		Query query = entityManager.createNamedQuery("Pessoa.findByTipoAdviceAniversarioDay");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		query.setParameter("day", day);
		query.setParameter("month", birthMonth+1);
		query.setParameter("active", true);
		
		query.setMaxResults(5);
		
		return (List<Pessoa>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> loadByEmail(String email) {
		
		Query query = entityManager.createNamedQuery("Pessoa.findByEmail");
		query.setParameter("email", email);
		List<Pessoa> resultList = ((List<Pessoa>)query.getResultList());
		return resultList;
	}
	
	public Pessoa loadByEmailActive(String email, boolean active) {
		
		Query query = entityManager.createNamedQuery("Pessoa.findByEmailActive");
		query.setParameter("email", email);
		query.setParameter("active", active);
		
		
		try {
			return (Pessoa)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}	
		
	}

	public Pessoa loadByIdBodyMeasurements(Long id) {
		Pessoa pessoa = super.loadById(id);
		pessoa.getBodyMeasurements();
		
		return pessoa;
	}
	
	@Transactional
	public Pessoa loadByIdFull(Long id) {
		Pessoa pessoa = super.loadById(id);
		pessoa.getClassification().toString();
		pessoa.getEndereco().getLogradouro();
		pessoa.getMedicalData().getHeight();
		
		return pessoa;
	}
	
	@Transactional
	public Pessoa loadByIdClassification(Long id) {
		Pessoa pessoa = super.loadById(id);
		pessoa.getClassification().toString();
		
		return pessoa;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> getByTipoPessoaAdviceClassification(TipoPessoa tipoPessoa, Long adviceId, Long rhythmId) {
		Query query = entityManager.createNamedQuery("Pessoa.getByTipoPessoaAdviceClassification");
		query.setParameter("tipoPessoa", tipoPessoa);
		query.setParameter("adviceId", adviceId);
		query.setParameter("rhythmId", rhythmId);
		
		return (List<Pessoa>)query.getResultList();
	}

	@Override
	@Transactional
	public void deleteById(Long id) throws EntityNotFoundException {
		
		Pessoa pessoa = this.loadById(id);
		Advice advice = pessoa.getAdvice();
		
		//Limpar Atividades
		List<Activity> activities = activityService.findByUserId(pessoa.getId());
		for(Activity a:activities) {
			activityService.deleteById(a.getId());
		}
		
		//Limpar Eventos
		eventService.removeParticipantAllEvents(advice.getId(), pessoa);
		
		//Limpar Turmas
		if ( pessoa.getTipoPessoa().getId() == TipoPessoa.PROFESSOR.getId() ) {
			teamService.removeTeacher(advice.getId(), pessoa.getId());
		}
		else if ( pessoa.getTipoPessoa().getId() == TipoPessoa.ALUNO.getId() ) {
			teamService.removeUser(advice.getId(), pessoa.getId());
		}
		
		//Limpar notificacoes
		notificationService.deleteByUser(pessoa.getId());
		

		//Apagar comentarios
		commentService.deleteByPessoaId(id);
		
		
		super.deleteById(id);
	}
	
	@Transactional
	public void updateLoginTime(Long id, Date time) {
		String sql = "update Pessoa set lastLoginDate = ? where id = ?";
		entityManager.createNativeQuery(sql).setParameter(1, time).setParameter(2, id).executeUpdate();
	}
}
 