package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Comment;


public interface CommentService extends GenericService<Comment, Long>{
	
	List<Comment> loadByActivityId(Long activityId);
	Integer countByActivityId(Long activityId);
	void deleteByActivityId(Long activityId);
	
	List<Comment> loadByPessoaId(Long pessoaId);
	void deleteByPessoaId(Long pessoaId);
	
}
