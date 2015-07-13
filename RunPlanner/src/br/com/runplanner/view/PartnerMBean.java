package br.com.runplanner.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.FileUploadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Partner;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.PartnerService;
import br.com.runplanner.view.util.Constants;

@Component("partnerMBean")
@ViewScoped
public class PartnerMBean extends BasicMBean {

	private static final String PARTNER_FORM_PAGE = "/pages/partner/partner";
	private static final String PARTNER_LIST_PAGE = "/pages/partner/partnerList";
	
	private List<Partner> partnerList;
	private Partner partner;
	
	
	@Autowired
	private PartnerService partnerService;	
	
	@Override
	public String goToList() {
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		
		partnerList = partnerService.findByAdvice(advice.getId()); 
		partner = new Partner();
		
    	setSelectedMenu(Constants.MENU_ADVICE);		
		return PARTNER_LIST_PAGE;
	}
	
	@Override
	public String goToCreate() {
		this.partner = new Partner();
		return PARTNER_FORM_PAGE;
	}
	
	@Override
	public String goToModify() {
		this.partner = partnerService.loadById(partner.getId());
    	
		if ( partner==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		return PARTNER_FORM_PAGE;
	}
	
	@Override
	public String delete() {
		if ( partner!=null && partner.getId()!=null ) {			
			
			partner = partnerService.loadById(partner.getId());
			try{
				//Apagar Imagem
				if ( partner.getBanner()!=null ) {
					String fileName = partner.getBanner().replace(getContextName(), "");
					String fotoFileName = getRealPath()+File.separator+Constants.BANNER_PATH+fileName;
					File f = new File(fotoFileName);
					f.delete();
				}
				
				partnerService.deleteById(partner.getId());
	    		addMessage(FacesMessage.SEVERITY_INFO, "partner.delete.sucess");
			}
			catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
			} 
		}
		
		return goToList();
	}
	
	@Override
	public String save() {
		
    	if( partner.getId()==null ) {    	
    		Pessoa user = getSessionUser();
    		partner.setAdvice(user.getAdvice());
    		partner = partnerService.persist(partner);
            addMessage(FacesMessage.SEVERITY_INFO, "partner.save.sucess");
    	}
    	else {
    		
    		try {
    			partnerService.update(partner);
    	    	addMessage(FacesMessage.SEVERITY_INFO, "partner.edit.sucess");
    		} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			}
            
    	}   
    	
        return goToList();
	}
	
	public void handleFileUpload(FileUploadEvent event) {  
	
		//Validar FOTO - Formato e Tamanho
		String fileName = event.getFile().getFileName().toLowerCase();
		Long fileSize = event.getFile().getSize();
		
		//Validar extensao do arquivo
		boolean valid = false;
		for( String ext: Constants.PHOTO_TYPES ) {
			if ( fileName.endsWith(ext) ) {
				valid = true;
				break;
			}
		}
		
		if ( valid ) {
			if ( fileSize>Constants.BANNER_MAX_SIZE ) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.sizeExceedMessage");
				return;
			}
		}
		else {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.invalidFileMessage");
			return;
		}
		
		File f = new File(Constants.BANNER_PATH);
		if (!f.exists()) {
			f.mkdir();
		}
		
		String fotoFileName = Constants.BANNER_PATH+fileName;
		
		byte[] foto = event.getFile().getContents();
		
		try {
			File fileFoto = new File(fotoFileName);		
			fileFoto.createNewFile();
			
			FileOutputStream out = new FileOutputStream(fileFoto);
			out.write(foto);
			
			out.flush();
			out.close();
			
			
		} catch (FileNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			e.printStackTrace();
		} catch (IOException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			e.printStackTrace();
		}
		
		partner.setBanner(fotoFileName);
    }  

	public List<Partner> getPartnerList() {
		return partnerList;
	}

	public void setPartnerList(List<Partner> partnerList) {
		this.partnerList = partnerList;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public PartnerService getPartnerService() {
		return partnerService;
	}

	public void setPartnerService(PartnerService partnerService) {
		this.partnerService = partnerService;
	}

}
