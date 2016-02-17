package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.PasswordUtil;

@Component("teacherMBean")
@Scope("session")
public class TeacherMBean extends BasicMBean {
	
	private static final String TEACHER_FORM_PAGE = "/pages/teacher/teacher";
	private static final String TEACHER_LIST_PAGE = "/pages/teacher/teacherList";
	
	private PessoaService pessoaService;
	private EmailThreadProductor emailThreadProductor;
	
	private Pessoa teacher;
	private List<Pessoa> teacherList = new ArrayList<Pessoa>();
	
	private String subject;
	private String message;

	@Autowired
	public TeacherMBean(PessoaService pessoaService, EmailThreadProductor emailThreadProductor) {
		this.pessoaService = pessoaService;
		this.emailThreadProductor = emailThreadProductor;
	}
	
	public void sendEmail() {
		teacher = pessoaService.loadById(teacher.getId());
		
		if ( teacher==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return;
		}
		
		emailThreadProductor.enviarMensagem(teacher.getEmail(), subject, message);
		clearEmail();
		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.email.sucess");		
	}	
	
	public void clearEmail() {
		subject="";
		message="";
	}
	
	public String goToList() {
		teacher = new Pessoa();
		
		Pessoa loged = getSessionUser();
		Long adviceId = loged.getAdvice().getId();		
		teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.PROFESSOR,adviceId);
		
		setSelectedMenu(Constants.MENU_ADVICE);
		
		return TEACHER_LIST_PAGE;
	}
	
	public String goToCreate() {
		teacher = new Pessoa();
		if(teacher.getEndereco() == null) {
			teacher.setEndereco(new Endereco());
		}
		
		Pessoa loged = getSessionUser();
		teacher.setAdvice(loged.getAdvice());
		
		return TEACHER_FORM_PAGE;
	}
	
	public String goToModify() {
		teacher = pessoaService.loadById(teacher.getId());
		
		if ( teacher==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}
		
		if(teacher.getEndereco() == null) {
			teacher.setEndereco(new Endereco());
		}
		
		if(teacher.getAdvice() == null) {
			teacher.setAdvice(new Advice());
		}
		
		return TEACHER_FORM_PAGE;
	}
	
	public String save() {

		changeEnconding();		
		
		Pessoa user = getSessionUser();
		Advice advice = user.getAdvice();
		
		//Verificar se o email ja nao e cadastrado
		Pessoa pessoaEmail = pessoaService.loadByEmailActive(teacher.getEmail(),true);
		if ( pessoaEmail !=null ) {
			if ( teacher.getId()==null ) {
				addMessage(FacesMessage.SEVERITY_ERROR,"alunos.duplicated.email");
				return null;
			}
			if( pessoaEmail.getId()!=null && !teacher.getId().equals(pessoaEmail.getId()) ) {
				addMessage(FacesMessage.SEVERITY_ERROR,"alunos.duplicated.email");
				return null;
			}
		}
		
		//Se tiver foto em disco, apagar do banco
		if ( teacher.hasUserPhoto() ) teacher.setPhoto(null);
				
		
		if(teacher.getId() == null) {	
			
			Pessoa existTeacher = pessoaService.getByNameTipoPessoaAdvice(teacher.getNome(), 
					TipoPessoa.PROFESSOR, teacher.getAdvice().getId());
			
			if ( existTeacher!=null ) {
				addMessage(FacesMessage.SEVERITY_INFO,"teacher.save.name.error");
				return null;
			}
			
			teacher.setRole(SystemRoles.TEACHER);
			teacher.setTipoPessoa(TipoPessoa.PROFESSOR);
			String generatedPass = PasswordUtil.gerarSenha();
			teacher.setPassword( MD5Util.crypt(generatedPass) );
			
			pessoaService.persist(teacher);
			
			String messageBody = Constants.EMAIL_REGISTRATION_TEACHER_BODY;
			messageBody = messageBody.replace("$1", advice.getName());
			messageBody = messageBody.replace("$2", teacher.getEmail());
			messageBody = messageBody.replace("$3", generatedPass);
			
			emailThreadProductor.enviarMensagem(teacher.getEmail(), Constants.EMAIL_REGISTRATION_TEACHER_TITLE, messageBody);
			
			addMessage(FacesMessage.SEVERITY_INFO,"teacher.save.sucess");
		} else {
			
			Pessoa existTeacher = pessoaService.getByNameTipoPessoaAdvice(teacher.getNome(), 
					TipoPessoa.PROFESSOR, teacher.getAdvice().getId());
			
			if ( existTeacher!=null && !existTeacher.getId().equals(teacher.getId()) ) {
				addMessage(FacesMessage.SEVERITY_INFO,"teacher.save.name.error");
				return null;
			}
			
			try {
				pessoaService.update(teacher);
				addMessage(FacesMessage.SEVERITY_INFO,"teacher.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return goToList();
			}  
			
		}
		return goToList();
	}
	
	public void restartPassword() {
		
		teacher = pessoaService.loadById(teacher.getId());
		
		String generatedPass = PasswordUtil.gerarSenha();
		teacher.setPassword( MD5Util.crypt(generatedPass) );
		
		try {
			pessoaService.update(teacher);
			
			String messageBody = Constants.EMAIL_RESTART_PASS_BODY;
			messageBody = messageBody.replace("$1", teacher.getEmail());
			messageBody = messageBody.replace("$2", generatedPass);
			
			emailThreadProductor.enviarMensagem(teacher.getEmail(), Constants.EMAIL_RESTART_PASS_TITLE, messageBody);

			addMessage(FacesMessage.SEVERITY_INFO, "template.msg.changepass.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			e.printStackTrace();
		}		
	}
	
	private void changeEnconding() {
		try {
			teacher.setNome(  new String (teacher.getNome().getBytes ("iso-8859-1"), "UTF-8") );
			
			Endereco endereco = teacher.getEndereco();			
			if (endereco!=null) {
				if (endereco.getBairro()!=null)
					endereco.setBairro(  new String (endereco.getBairro().getBytes ("iso-8859-1"), "UTF-8") );
				if (endereco.getComplemento()!=null)
					endereco.setComplemento(  new String (endereco.getComplemento().getBytes ("iso-8859-1"), "UTF-8") );
				if (endereco.getLogradouro()!=null)
					endereco.setLogradouro(  new String (endereco.getLogradouro().getBytes ("iso-8859-1"), "UTF-8") );
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String delete() {
		try {
			pessoaService.deleteById(teacher.getId());
			addMessage(FacesMessage.SEVERITY_INFO,"teacher.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.delete");			
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
		
		if ( !Utils.verifyUserPhotoPath() ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			System.err.println("Erro ao criar diretório para Fotos");
			
			return;
		}
		
		String fotoFileName = Constants.PHOTO_PATH+Constants.PHOTO_USER_NAME+teacher.getId()+extencao;
		
		
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
		
		teacher.setUserPhoto(fotoFileName);
		teacher.setPhoto( foto ); 
    }  
	
	public StreamedContent getPhoto() {
		if ( teacher.getPhoto()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(teacher.getPhoto());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}
	

	//Get and Set
	public void clearImage() {
		teacher.setUserPhoto(null);
		teacher.setPhoto(null);
	}

	public Pessoa getTeacher() {
		return teacher;
	}

	public void setTeacher(Pessoa teacher) {
		this.teacher = teacher;
	}

	public List<Pessoa> getTeacherList() {
		return teacherList;
	}

	public void setTeacherList(List<Pessoa> teacherList) {
		this.teacherList = teacherList;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
