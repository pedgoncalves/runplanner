package br.com.runplanner.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Comment;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("commentService")
public class CommentServiceImpl extends GenericServiceImpl<Comment, Long> implements CommentService {

	@SuppressWarnings("unchecked")
	public List<Comment> loadByActivityId(Long activityId) {
		Query query = entityManager.createNamedQuery("Comment.loadByActivityId");
		query.setParameter("activityId", activityId);
		return (List<Comment>)query.getResultList();
	}
	
	public Integer countByActivityId(Long activityId) {
		Query query = entityManager.createNamedQuery("Comment.countByActivityId");
		query.setParameter("activityId", activityId);

		Long count = (Long) query.getSingleResult();
		
		if ( count == null ) {
			count = 0l;
		}
		return count.intValue();
	}

	@Transactional
	public void deleteByActivityId(Long activityId) {
		List<Comment> list = loadByActivityId(activityId);
		for(Comment comment:list) {
			try {
				deleteById(comment.getId());
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Transactional
	public void deleteByPessoaId(Long pessoaId) {
		List<Comment> list = loadByPessoaId(pessoaId);
		for(Comment comment:list) {
			try {
				deleteById(comment.getId());
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<Comment> loadByPessoaId(Long pessoaId) {
		Query query = entityManager.createNamedQuery("Comment.loadByPessoaId");
		query.setParameter("pessoaId", pessoaId);
		return (List<Comment>)query.getResultList();
	}

}