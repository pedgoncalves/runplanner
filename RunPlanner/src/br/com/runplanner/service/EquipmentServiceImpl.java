package br.com.runplanner.service;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Equipment;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("equipmentService")
public class EquipmentServiceImpl extends GenericServiceImpl<Equipment, Long> implements EquipmentService {
	
	@Autowired
	private ActivityService activityService;

	@SuppressWarnings("unchecked")
	public List<Equipment> findByStudent(Long studentId, boolean active) {
		Query query = entityManager.createNamedQuery("Equipment.findByStudent");
		query.setParameter("studentId", studentId);
		query.setParameter("active", active);
		return (List<Equipment>)query.getResultList();
	}

	public Equipment loadByName(String name, Long studentId) {
		Query query = entityManager.createNamedQuery("Equipment.loadByName");
		query.setParameter("studentId", studentId);
		query.setParameter("name", name);
		
		try {
			return (Equipment)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Equipment> findByActivity(Long activityId) {
		Query query = entityManager.createNamedQuery("Equipment.findByActivity");
		query.setParameter("activityId", activityId);
		return (List<Equipment>)query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public Double findDistance(Long equipmentId) {
		Query query = entityManager.createNamedQuery("Activity.findByEquipment");
		query.setParameter("equipmentId", equipmentId);
		
		List<Activity> activityList = (List<Activity>)query.getResultList();
		
		double distance = 0;
		for( Activity act:activityList ) {
			distance += act.getTotalDistance();
		}
		
		return distance;
	}	
	
	@SuppressWarnings("unchecked")	
	@Transactional
	public List<Activity> findActivityByEquipment(Long equipmentId) {
		Query query = entityManager.createNamedQuery("Equipment.findActivityByEquipment");
		query.setParameter("equipmentId", equipmentId);
		
		return (List<Activity>)query.getResultList();
	}
	

	@Transactional
	public void deleteById(Long id) throws EntityNotFoundException {		
		
		//Remover ligacao com as atividades
		List<Activity> activityList = findActivityByEquipment(id);
		for(Activity a:activityList) {
			List<Equipment> equipmentList = a.getEquipment();
			
			for(Equipment e:equipmentList) {
				if ( e.getId().longValue() == id.longValue() ) {
					
					equipmentList.remove(e);					
					a.setEquipment(equipmentList);

					activityService.update(a);
					break;					
				}
			}
		}
		
		
		//Deleta equipamento
		super.deleteById(id);
	}

}