package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Equipment;


public interface EquipmentService extends GenericService<Equipment, Long>{
	
	List<Equipment> findByStudent(Long studentId, boolean active);
	List<Equipment> findByActivity(Long activityId);
	Double findDistance(Long equipmentId);
	Equipment loadByName(String name, Long studentId);	
	List<Activity> findActivityByEquipment(Long equipmentId);
	
}
