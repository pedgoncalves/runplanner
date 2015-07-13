package br.com.runplanner.view;

import java.util.List;

import javax.faces.application.FacesMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Equipment;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.EquipmentService;
import br.com.runplanner.view.util.Constants;

@Component("equipmentMBean")
@Scope("session")
public class EquipmentMBean extends BasicMBean {

	private static final String EQUIPMENT_FORM_PAGE = "/pages/equipment/equipment";
	private static final String EQUIPMENT_LIST_PAGE = "/pages/equipment/equipmentList";
	
	private List<Equipment> equipmentList;
	private Equipment equipment;
	
	private boolean findActive = Boolean.TRUE;
	
	@Autowired
	private EquipmentService equipmentService;	
	
	@Override
	public String goToList() {
		Pessoa user = getSessionUser();
				
		findActive = Boolean.TRUE;
		equipmentList = equipmentService.findByStudent(user.getId(), findActive); 
		
    	for( Equipment e:equipmentList ) {
    		double distance = e.getKilometers();
    		distance += equipmentService.findDistance(e.getId());
    		e.setKilometers( distance );
    	}
		
		equipment = new Equipment();
		
    	setSelectedMenu(Constants.MENU_EQUIPMENT);		
		return EQUIPMENT_LIST_PAGE;
	}
	
	public String find() {
		Pessoa user = getSessionUser();
		equipmentList = equipmentService.findByStudent(user.getId(), findActive); 
		
		return EQUIPMENT_LIST_PAGE;
	}	
	
	@Override
	public String goToCreate() {
		this.equipment = new Equipment();
		equipment.setKilometers(0d);
		
		return EQUIPMENT_FORM_PAGE;
	}
	
	@Override
	public String goToModify() {
		this.equipment = equipmentService.loadById(equipment.getId());
    	
		if ( equipment==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		return EQUIPMENT_FORM_PAGE;
	}
	
	@Override
	public String delete() {
		if ( equipment!=null && equipment.getId()!=null ) {			
			
			try{
				equipmentService.deleteById(equipment.getId());
	    		addMessage(FacesMessage.SEVERITY_INFO, "equipment.delete.sucess");
			}
			catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
			} 
		}
		
		return goToList();
	}
	
	@Override
	public String save() {
		
    	if( equipment.getId()==null ) {    	
    		Pessoa user = getSessionUser();
    		
    		equipment.setStudent(user);
    		equipment.setActive(Boolean.TRUE);
    		equipment = equipmentService.persist(equipment);
            addMessage(FacesMessage.SEVERITY_INFO, "equipment.save.sucess");
    	}
    	else {
    		
    		try {
    			equipmentService.update(equipment);
    	    	addMessage(FacesMessage.SEVERITY_INFO, "equipment.edit.sucess");
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
            
    	}   
    	
        return goToList();
	}
	
	public String deactivate() {
		
		if ( equipment!=null && equipment.getId()!=null ) {			
			equipment = equipmentService.loadById(equipment.getId());    		
    		try {
    			equipment.setActive(Boolean.FALSE);
    			equipmentService.update(equipment);
    	    	addMessage(FacesMessage.SEVERITY_INFO, "equipment.deactivate.sucess");
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
            
    	}   
    	
        return find();
	}
	
	public String activate() {
		
		if ( equipment!=null && equipment.getId()!=null ) {			
			equipment = equipmentService.loadById(equipment.getId());
    		try {
    			equipment.setActive(Boolean.TRUE);
    			equipmentService.update(equipment);
    	    	addMessage(FacesMessage.SEVERITY_INFO, "equipment.activate.sucess");
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
            
    	}   
    	
        return find();
	}

	
	//Gets e Sets
	
	public List<Equipment> getEquipmentList() {
		return equipmentList;
	}

	public void setEquipmentList(List<Equipment> equipmentList) {
		this.equipmentList = equipmentList;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public void setEquipment(Equipment equipment) {
		this.equipment = equipment;
	}

	public boolean isFindActive() {
		return findActive;
	}

	public void setFindActive(boolean findActive) {
		this.findActive = findActive;
	}

}
