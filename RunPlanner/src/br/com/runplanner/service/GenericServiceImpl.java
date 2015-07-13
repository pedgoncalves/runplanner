package br.com.runplanner.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.BasicEntity;
import br.com.runplanner.exception.EntityNotFoundException;

public abstract class GenericServiceImpl<T extends BasicEntity, ID extends Serializable> implements GenericService<T, ID> {

	 	private Class<T> persistentClass;
	 	
	 	@PersistenceContext
	 	protected EntityManager entityManager;

	 	@SuppressWarnings("unchecked")
	    public GenericServiceImpl() {
	        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
	                                .getGenericSuperclass()).getActualTypeArguments()[0];
	    }

	    public Class<T> getPersistentClass() {
	        return persistentClass;
	    }

	    public T loadById(ID id) {
	       return entityManager.find(persistentClass, id);
	    }
	    
	    private T loadByIdSerial(Serializable id) {
		    return entityManager.find(persistentClass, id);
		}

	    @Transactional
	    public T persist(T entity) {
	        entityManager.persist(entity);
	        return entity;
	    }
	    
	    @Transactional
	    public void update(T entity) throws EntityNotFoundException {
	    	T exist = loadByIdSerial(entity.getId());	    	
	    	if ( exist == null ) {
	    		throw new EntityNotFoundException();
	    	}
	        entityManager.merge(entity);
	    }
	    	    
	    @Transactional
	    public void deleteById(ID id) throws EntityNotFoundException {
	    	T entity = loadById(id);	    	
	    	if ( entity == null ) {
	    		throw new EntityNotFoundException();
	    	}
	        entityManager.remove(entity);
	    }
	    
	    @SuppressWarnings("unchecked")
	    @Transactional
	    public List<T> loadAll() {
	        return entityManager.createQuery("Select t from " + persistentClass.getSimpleName() + " t").getResultList();
	    }
}
