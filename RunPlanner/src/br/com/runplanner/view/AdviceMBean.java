package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Endereco;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.StatusAdvice;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.AdviceCantBeDeletedException;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.view.util.Constants;

@Component("adviceMBean")
@Scope("session")
public class AdviceMBean extends BasicMBean {

	private static final String ADVICE_FORM_PAGE = "/pages/advice/advice";
	private static final String ADVICE_LIST_PAGE = "/pages/advice/adviceList";
	private static final String ADVICE_CUSTOMER_PAGE = "/pages/advice/customerList";
	
	private AdviceService adviceService;
	private PessoaService pessoaService;
	private SpreadsheetService spreadsheetService;
	private ActivityService activityService;
	private Advice advice;
	
	private List<Advice> adviceList;
	private List<Pessoa> customerList;
	
	private List<SelectItem> adviceListSI;
	
	private Long spreadCount;
	private Long activityCount;
	
	private Boolean activeAdvice;
	private String findAdviceName;
	
	@Autowired
	public AdviceMBean(AdviceService adviceService,PessoaService pessoaService, 
			SpreadsheetService spreadsheetService, ActivityService activityService) {
		this.adviceService = adviceService;
		this.pessoaService = pessoaService;
		this.spreadsheetService = spreadsheetService;
		this.activityService = activityService;
		
		this.advice = new Advice();
		this.adviceList = new ArrayList<Advice>();
		this.activeAdvice = true;
	}
	

	public String goToList() {
		adviceList = adviceService.loadAllActiveEager(true);
		customerList = new ArrayList<Pessoa>();
		
		setSelectedMenu(Constants.MENU_ADVICE);
		
		return ADVICE_LIST_PAGE;
	}
	
	public String findManagementInformation() {
		if(findAdviceName != null && !findAdviceName.equals("")) {
			if (findAdviceName.indexOf("@") > 0) {
				Pessoa p = pessoaService.loadByEmailActive(findAdviceName,true);
				if (p != null) {
					adviceList = new ArrayList<Advice>();
					Advice a = adviceService.loadByIdEager(p.getAdvice().getId());
					a.getPessoas().size();
					adviceList.add(a);
				} else {
					adviceList = new ArrayList<Advice>();
				}
			} else {
				adviceList = adviceService.loadByActiveName(activeAdvice,findAdviceName);
			}
		} else {
			adviceList = adviceService.loadAllActiveEager(activeAdvice);
		}
		customerList = new ArrayList<Pessoa>();
		
		return ADVICE_LIST_PAGE;
	}
	
	public String findActive() {
		adviceList = adviceService.loadAllActiveEager(activeAdvice);
		customerList = new ArrayList<Pessoa>();
		
		setSelectedMenu(Constants.MENU_ADVICE);
		
		return ADVICE_LIST_PAGE;
	}
	
	public String goToCustomerList() {
		adviceList = adviceService.loadAllEager();		
		advice = adviceList.get(0);
		
		customerList = advice.getPessoas();
		spreadCount = spreadsheetService.countByAdvice(advice.getId());
		activityCount = activityService.countByAdvice(advice.getId());
		
		setSelectedMenu(Constants.MENU_CUSTOMER);
		
		return ADVICE_CUSTOMER_PAGE;
	}
	
	public String find() {
		
		advice = adviceService.loadByIdEager(advice.getId());

		customerList = advice.getPessoas();
		spreadCount = spreadsheetService.countByAdvice(advice.getId());
		activityCount = activityService.countByAdvice(advice.getId());
		
		setSelectedMenu(Constants.MENU_CUSTOMER);
		
		return ADVICE_CUSTOMER_PAGE;
	}
	
	public String goToCreate() {
		advice = new Advice();
		customerList = new ArrayList<Pessoa>();
		return ADVICE_FORM_PAGE;
	}

	public String goToModify() {
		advice = adviceService.loadById(advice.getId());
		
		if ( advice==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		if ( advice.getAdress()==null ) {
			advice.setAdress(new Endereco());
		}
		
		customerList = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO, advice.getId(), true);
		
		return ADVICE_FORM_PAGE;
	}
	
	public String delete() {
		try {
			adviceService.deleteById(advice.getId());
			addMessage(FacesMessage.SEVERITY_INFO, "advice.delete.sucess");
		} catch (AdviceCantBeDeletedException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "advice.cant.be.deleted");			
		} catch (EntityNotFoundException e) {
		addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
	}        		
		
		advice = new Advice();
		return goToList();
	}
	
	public String deactivate() {
		advice = adviceService.loadById(advice.getId());
		adviceService.desativarAssessoriaResponsaveis(advice);
		addMessage(FacesMessage.SEVERITY_INFO, "advice.deactivate.sucess");
		
		advice = new Advice();
		return findActive();
	}
	
	public String activate() {
		advice = adviceService.loadById(advice.getId());
		adviceService.ativarAssessoriaResponsaveis(advice);
		addMessage(FacesMessage.SEVERITY_INFO, "advice.activate.sucess");
		
		advice = new Advice();
		return findActive();
	}
	
	public String save() {

		changeEnconding();


		//Se tiver foto em disco, apagar do banco
		if ( advice.hasAdvicePhoto() ) advice.setLogo(null);
		
		if ( advice.getId()==null ) {
			advice.setRegisterDate(new Date());
			advice.setStatus(StatusAdvice.GRATUITO);
			advice = adviceService.persist(advice);
            addMessage(FacesMessage.SEVERITY_INFO, "advice.save.sucess");
		}
		else {
			try { 
				
				adviceService.update(advice);
				addMessage(FacesMessage.SEVERITY_INFO, "advice.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return goToList();
			}  
		}
		return goToList();
	}
	
	private void changeEnconding() {
		try {
			advice.setName(  new String (advice.getName().getBytes ("iso-8859-1"), "UTF-8") );
			
			Endereco endereco = advice.getAdress();			
			endereco.setBairro(  new String (endereco.getBairro().getBytes ("iso-8859-1"), "UTF-8") );
			endereco.setComplemento(  new String (endereco.getComplemento().getBytes ("iso-8859-1"), "UTF-8") );
			endereco.setLogradouro(  new String (endereco.getLogradouro().getBytes ("iso-8859-1"), "UTF-8") );			
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
			if ( fileSize>Constants.PHOTO_MAX_SIZE ) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.sizeExceedMessage");
				return;
			}
		}
		else {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.invalidFileMessage");
			return;
		}
		
		String extencao = fileName.substring( fileName.lastIndexOf(".") );
		String fotoFileName = Constants.PHOTO_PATH+Constants.PHOTO_ADVICE_NAME+advice.getId()+extencao;
		
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
		
		advice.setAdvicePhoto(fotoFileName);		
        advice.setLogo( foto ); 

    }  
	
	public StreamedContent getAdviceLogo() {
		if ( advice.getLogo()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(advice.getLogo());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}
	
	public List<SelectItem> getListaStatus() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		StatusAdvice[] status = StatusAdvice.values();
		for (int i = 0; i < status.length; i++) {
			result.add(new SelectItem(status[i],status[i].getLabel()));
		}
		return result;
	} 
	
	public void clearImage() {
		advice.setAdvicePhoto(null);
		advice.setLogo(null);
	}
	
	
	public List<SelectItem> getAdviceListSI() {
		adviceListSI = new ArrayList<SelectItem>();
		for (Advice advice:adviceList) {
			adviceListSI.add(new SelectItem(advice.getId(),advice.getName()));
		}
		return adviceListSI;
	}
	
	
	//Gets and Sets
	
	public AdviceService getAdviceService() {
		return adviceService;
	}

	public void setAdviceService(AdviceService adviceService) {
		this.adviceService = adviceService;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public List<Advice> getAdviceList() {
		return adviceList;
	}

	public void setAdviceList(List<Advice> adviceList) {
		this.adviceList = adviceList;
	}

	public List<Pessoa> getCustomerList() {
		return customerList;
	}

	public void setCustomerList(List<Pessoa> customerList) {
		this.customerList = customerList;
	}

	public Long getSpreadCount() {
		return spreadCount;
	}
	
	public Integer getPessoasCount() {
		return customerList.size();
	}

	public Long getActivityCount() {
		return activityCount;
	}


	public Boolean getActiveAdvice() {
		return activeAdvice;
	}


	public void setActiveAdvice(Boolean activeAdvice) {
		this.activeAdvice = activeAdvice;
	}


	public String getFindAdviceName() {
		return findAdviceName;
	}


	public void setFindAdviceName(String findAdviceName) {
		this.findAdviceName = findAdviceName;
	}


}
