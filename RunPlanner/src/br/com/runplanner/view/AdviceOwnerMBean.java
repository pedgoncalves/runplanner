package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import br.com.runplanner.domain.Sexo;
import br.com.runplanner.domain.SystemRoles;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.PasswordUtil;

@Component("adviceOwnerMBean")
@Scope("session")
public class AdviceOwnerMBean extends BasicMBean {
	
	private static final String ADVICE_OWNER_FORM_PAGE = "/pages/advice/adviceOwner";
	private static final String ADVICE_OWNER_LIST_PAGE = "/pages/advice/adviceOwnerList";
	
	private PessoaService pessoaService;
	private AdviceService adviceService;
	private EmailThreadProductor emailThreadProductor;
	
	private Pessoa owner;
	private Advice advice;

	private List<Advice> adviceList;
	private List<SelectItem> adviceListSI;
	private List<Pessoa> ownerList = new ArrayList<Pessoa>();
 

	@Autowired
	public AdviceOwnerMBean(PessoaService pessoaService, 
			AdviceService adviceService, 
			EmailThreadProductor emailThreadProductor) {
		this.pessoaService = pessoaService;
		this.adviceService = adviceService;
		this.emailThreadProductor = emailThreadProductor;
		this.adviceList = new ArrayList<Advice>();
	}
	
	public String goToList() {
		owner = new Pessoa();		
		
		adviceList = adviceService.loadAll();
		adviceListSI = new ArrayList<SelectItem>();
		for (Advice advice:adviceList) {
			adviceListSI.add(new SelectItem(advice.getId(),advice.getName()));
		}		
		
		advice = adviceList.get(0);		
		ownerList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ASSESSORIA, advice.getId());
		
		setSelectedMenu(Constants.MENU_ADVICE_OWNER);
		
		return ADVICE_OWNER_LIST_PAGE;
	}
	
	public String find() {
		
		ownerList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ASSESSORIA, advice.getId());
		
		setSelectedMenu(Constants.MENU_ADVICE_OWNER);
		
		return ADVICE_OWNER_LIST_PAGE;
	}
	
	public String goToCreate() {
		owner = new Pessoa();
		if(owner.getEndereco() == null) {
			owner.setEndereco(new Endereco());
		}
		if(owner.getAdvice() == null) {
			owner.setAdvice(new Advice());
		}
		return ADVICE_OWNER_FORM_PAGE;
	}
	
	public String goToModify() {
		owner = pessoaService.loadById(owner.getId());
		
		if ( owner==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		if(owner.getEndereco() == null) {
			owner.setEndereco(new Endereco());
		}
		
		if(owner.getAdvice() == null) {
			owner.setAdvice(new Advice());
		}
		
		return ADVICE_OWNER_FORM_PAGE;
	}
	
	public String save() {
		
		//changeEnconding();
		//Se tiver foto em disco, apagar do banco
		if ( owner.hasUserPhoto() ) owner.setPhoto(null);
		
		if(owner.getId() == null) {
			Pessoa test = pessoaService.loadByEmailActive(owner.getEmail(),true);
			
			if(test == null) {				
				owner.setRole(SystemRoles.ADVICE);
				owner.setTipoPessoa(TipoPessoa.ASSESSORIA);
				
				String generatedPass = PasswordUtil.gerarSenha();
				owner.setPassword( MD5Util.crypt(generatedPass) );

				
				pessoaService.persist(owner);
	
				String messageBody = Constants.EMAIL_REGISTRATION_ADVICE;
				messageBody = messageBody.replace("$1", owner.getEmail());
				messageBody = messageBody.replace("$2", generatedPass);
				
				emailThreadProductor.enviarMensagem(owner.getEmail(), Constants.EMAIL_REGISTRATION_ADVICE_TITLE, messageBody);
				
				addMessage(FacesMessage.SEVERITY_INFO, "advice.owner.save.sucess");
			}else {
				addMessage(FacesMessage.SEVERITY_ERROR,"contact.duplicated.email");
				return ADVICE_OWNER_FORM_PAGE;
			}
		} else {
			try {
				
				pessoaService.update(owner);
				addMessage(FacesMessage.SEVERITY_INFO, "advice.owner.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return goToList();
			} 
		}
		return goToList();
	}
	
	public void restartPassword() {
		
		owner = pessoaService.loadById(owner.getId());
		
		String generatedPass = PasswordUtil.gerarSenha();
		owner.setPassword( MD5Util.crypt(generatedPass) );
		
		try {
			pessoaService.update(owner);
			
			String messageBody = Constants.EMAIL_RESTART_PASS_BODY;
			messageBody = messageBody.replace("$1", owner.getEmail());
			messageBody = messageBody.replace("$2", generatedPass);
			
			emailThreadProductor.enviarMensagem(owner.getEmail(), Constants.EMAIL_RESTART_PASS_TITLE, messageBody);

			addMessage(FacesMessage.SEVERITY_INFO, "template.msg.changepass.sucess");
			
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			e.printStackTrace();
		}		
	}
	


	public String delete() {
		try {
			pessoaService.deleteById(owner.getId());
			addMessage(FacesMessage.SEVERITY_INFO, "advice.owner.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.delete");			
		}  
		
		return goToList();
	}
	
	public List<SelectItem> getListaSexo() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		Sexo[] sexos = Sexo.values();
		for (int i = 0; i < sexos.length; i++) {
			result.add(new SelectItem(sexos[i],sexos[i].label));
		}
		return result;
	}
	
	public List<SelectItem> getAdviceList() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		List<Advice> adviceList = adviceService.loadAll();
		for (Advice advice:adviceList) {
			result.add(new SelectItem(advice.getId(),advice.getName()));
		}
		return result;
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
		String fotoFileName = Constants.PHOTO_PATH+Constants.PHOTO_USER_NAME+owner.getId()+extencao;
		
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
		
		owner.setUserPhoto(fotoFileName);
		owner.setPhoto( foto ); 		
}  
	
	public StreamedContent getPhoto() {
		if ( owner.getPhoto()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(owner.getPhoto());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}
	
	public void clearImage() {
		owner.setUserPhoto(null);
		owner.setPhoto(null);
	}

	
	public List<SelectItem> getAdviceListSI() {

		return adviceListSI;
	}
	
	//Get and Set

	
	public Pessoa getOwner() {
		return owner;
	}

	public void setOwner(Pessoa owner) {
		this.owner = owner;
	}

	public List<Pessoa> getOwnerList() {
		return ownerList;
	}

	public void setOwnerList(List<Pessoa> ownerList) {
		this.ownerList = ownerList;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public void setAdviceListSI(List<SelectItem> adviceListSI) {
		this.adviceListSI = adviceListSI;
	}
	
	
}
