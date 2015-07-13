package br.com.runplanner.service;

import java.io.Serializable;

import br.com.runplanner.exception.EntityNotFoundException;

public interface GenericService<T, ID extends Serializable> {
	  
	T loadById(ID id);
	
	T persist(T entity);
	
	void update(T entity) throws EntityNotFoundException;
		
	void deleteById(ID id)  throws EntityNotFoundException;	

}
