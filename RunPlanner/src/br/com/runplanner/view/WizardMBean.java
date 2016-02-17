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
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Endereco;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.RhythmTable;
import br.com.runplanner.domain.Sexo;
import br.com.runplanner.domain.SystemRoles;
import br.com.runplanner.domain.Team;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.RhythmService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.MessagesResources;
import br.com.runplanner.view.util.PasswordUtil;

@Component("wizardMBean")
@Scope("session")
public class WizardMBean {

	@Autowired
	private PessoaService pessoaService;	
	@Autowired
	private RhythmService rhythmService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private AdviceService adviceService;
	@Autowired
	private LoginMBean loginBean;
	@Autowired
	private EmailThreadProductor emailThreadProductor;
	
	//Listas
	private List<RhythmTable> rhythmList;
	private List<Pessoa> teacherList;
	private List<Team> teamList;
	
	//Formularios
	private RhythmTable rhythm;
	private Advice advice;
	private Endereco enderecoAdvice;
	private Pessoa teacher;
	private Team team;
	private DualListModel<String> teachers;
	
	//Controle Wizard
	private int stepsNumber = 5;
	private int stepNow = 0;
	
	//Dados Wizard
	private String stepTitles[] = {
		"Bem-vindo ao RunPlanner!",
		"Dados da Assessoria",
		"Classifica&ccedil;&otilde;es",
		"Professores",
		"Turmas",
		"Pronto para usar!"
	};	
	private String stepSubTitles[] = {
		"",
		"Passo 1 de 5",
		"Passo 2 de 5",
		"Passo 3 de 5",
		"Passo 4 de 5",
		"Passo 5 de 5"
	};
	
	public static final int STEP_INITIAL = 0;
	public static final int STEP_ADVICE_DATA = 1;
	public static final int STEP_RHYTHM_TABLE = 2;
	public static final int STEP_TEACHER = 3;
	public static final int STEP_TEAM = 4;
	public static final int STEP_FINAL = 5;
	
	public void next() {
		saveWizard();
		stepNow++;
		moveWizard();
	}
	
	public void back() {
		saveWizard();
		stepNow--;
		moveWizard();
	}
	
	public void finalize() {
		stepNow = 0;
	}

	private void moveWizard() {

		Pessoa customer = getSessionUser();
		Advice advice = customer.getAdvice();
		
		switch (stepNow) {
			case STEP_INITIAL:
				
			break;
			case STEP_ADVICE_DATA:
				this.advice = advice;
				if ( enderecoAdvice==null ) {
					enderecoAdvice = new Endereco();
				}
			break;			
			case STEP_RHYTHM_TABLE:
				rhythmList = rhythmService.getByAdvice(advice.getId());
				rhythm = new RhythmTable();
			break;
			case STEP_TEACHER:
				teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.PROFESSOR, advice.getId());
				teacherList.add(customer);
				teacher = new Pessoa();
				teacher.setEndereco( new Endereco() );
			break;
			case STEP_TEAM:
				teamList = teamService.getByAdvice(advice.getId());
				
				//Picklist
				List<Pessoa> teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.PROFESSOR, advice.getId());
				List<String> nameList = new ArrayList<String>(); 
				for(Pessoa teacher: teacherList) {
					nameList.add(teacher.getNome());
				}
				//Adicionar os Donos da assessoria
				teacherList = pessoaService.getByTipoPessoaAdvice(TipoPessoa.ASSESSORIA, advice.getId());		 
				for(Pessoa teacher: teacherList) {
					nameList.add(teacher.getNome());
				}				
				teachers = new DualListModel<String>(nameList,new ArrayList<String>());
				
				team = new Team();
			break;
			case STEP_FINAL:
			break;
		}
	}
	
	private void saveWizard() {

		switch (stepNow) {
			case STEP_ADVICE_DATA:
				saveAdvice();
			break;			
			case STEP_RHYTHM_TABLE:				
				rhythm = new RhythmTable();
			break;
			case STEP_TEACHER:
				teacher = new Pessoa();
				teacher.setEndereco( new Endereco() );
			break;
			case STEP_TEAM:
				
			break;
			case STEP_FINAL:
				
			break;
		}
	}
	
	private void saveAdvice() {
		advice.setAdress(enderecoAdvice);
		
		//Se tiver foto em disco, apagar do banco
		if ( advice.hasAdvicePhoto() ) advice.setLogo(null);
		
		try {
			adviceService.update(advice);
			
			if ( advice.getLogo()!=null ) {
				loginBean.setAdviceLogo(advice.getLogo());
			}
			loginBean.setAdviceName(advice.getName());
			
			getSessionUser().setAdvice(advice);
			
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			e.printStackTrace();
		}		
	}
	
	public void saveRhythm() {
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		rhythm.setAdvice(advice);
		
		if ( rhythm.getClassification().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"rhythm.classification.mandatory");
			return;
		}
		
		//Verificar se esta tudo zerado
		if ( rhythm.getEasyPace().equals("") 
				&& rhythm.getNormalPace().equals("")
				&& rhythm.getMediumPace().equals("")
				&& rhythm.getModeratePace().equals("")
				&& rhythm.getStrongPace().equals("")
				&& rhythm.getShootingPace().equals("")
				) {
			addMessage(FacesMessage.SEVERITY_ERROR,"rhythm.save.error.empty");
			return;
		}
		
		RhythmTable existentRhythm = rhythmService.getByAdviceClassification(
				advice.getId(), rhythm.getClassification());
		
		if ( existentRhythm!=null && (
				rhythm.getId()==null || 
				!rhythm.getId().equals(existentRhythm.getId()) ) ) {
			
			addMessage(FacesMessage.SEVERITY_ERROR,"rhythm.save.error.exist");
			return;
		}
		
		if ( rhythm.getId()==null ) {			
			rhythm = rhythmService.persist(rhythm);          
		}
		else {
			try {
				rhythmService.update(rhythm);
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return;
			}  			
		}
		
		moveWizard();
	}
	
	public void saveTeacher() {
		
		//Verificar nome e email		
		if ( teacher.getNome().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.name.mandatory");
			return;
		}
		
		if ( teacher.getEmail().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.email.mandatory");
			return;
		}
		
		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		teacher.setAdvice(advice);
		
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
		
		//Verificar se o email ja nao e cadastrado
		Pessoa pessoaEmail = pessoaService.loadByEmailActive(teacher.getEmail(),true);
		if ( pessoaEmail !=null ) {
			if ( teacher.getId()==null ) {
				addMessage(FacesMessage.SEVERITY_ERROR,"alunos.duplicated.email");
				return;
			}
			if( pessoaEmail.getId()!=null && !teacher.getId().equals(pessoaEmail.getId()) ) {
				addMessage(FacesMessage.SEVERITY_ERROR,"alunos.duplicated.email");
				return;
			}
		}
		
		//Se tiver foto em disco, apagar do banco
		if ( teacher.hasUserPhoto() ) teacher.setPhoto(null);
		
		
		if(teacher.getId() == null) {	
			
			Pessoa existTeacher = pessoaService.getByNameTipoPessoaAdvice(teacher.getNome(), 
					TipoPessoa.PROFESSOR, teacher.getAdvice().getId());
			
			if ( existTeacher!=null ) {
				addMessage(FacesMessage.SEVERITY_INFO,"teacher.save.name.error");
				return;
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
		}
		
		moveWizard();
	}	
	
	public void saveTeam() {
		
		if ( team.getPlace().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"team.place.mandatory");
			return;
		}
		
		if ( team.getDescription().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"team.description.mandatory");
			return;
		}
		
		if ( team.getInitialHour()==null ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"team.time.mandatory");
			return;
		}
		
		Advice advice = getSessionUser().getAdvice();
		
		List<Pessoa> teacherList = new ArrayList<Pessoa>();
		for (String name:teachers.getTarget()) {
			Pessoa teacher = pessoaService.getByNameTipoPessoaAdvice(name, TipoPessoa.PROFESSOR, advice.getId());			
			if ( teacher==null ) {
				teacher = pessoaService.getByNameTipoPessoaAdvice(name, TipoPessoa.ASSESSORIA, advice.getId());
			}
			teacherList.add(teacher);
		}			
		team.setTeachers(teacherList);
		
		if ( team.getId()==null ) {			
			team.setAdvice(advice);
			team = teamService.persist(team);
		}
		
		moveWizard();
	}
	
	
	public StreamedContent getPhotoAdvice() {
		if ( advice.getLogo()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(advice.getLogo());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}
	
	public void handleFileUploadAdvice(FileUploadEvent event) { 

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
		
		if ( !Utils.verifyAdvicePhotoPath() ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			System.err.println("Erro ao criar diretório para Fotos");
			
			return;
		}		
		
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
		        
        Pessoa customer = getSessionUser();
        customer.setAdvice(advice);
    } 
	
	public void handleFileUploadTeacher(FileUploadEvent event) {
		
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
		
	public StreamedContent getTeacherPhoto() {
		if ( teacher.getPhoto()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(teacher.getPhoto());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}	
	
	public void clearTeacherImage() {
		teacher.setUserPhoto(null);
		teacher.setPhoto(null);
	}
	
	protected Pessoa getSessionUser() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		return ((Pessoa) session.getAttribute(Constants.USER_SESSION));
	}
	
	protected void addMessage(Severity severity, String sumaryKey) { 
		FacesMessage message = new FacesMessage(severity, MessagesResources.getStringFromBundle(sumaryKey,""),"");
		FacesContext.getCurrentInstance().addMessage(null, message);  
	}
	
	
	//Gets e Sets
	public boolean getNextDisabled() {
		boolean result = false;
		
		switch (stepNow) {
			case STEP_ADVICE_DATA:
				result = false;
			break;			
			case STEP_RHYTHM_TABLE:				
				if ( rhythmList==null || rhythmList.size()==0 ) {
					result = true;
				}
			break;
			case STEP_TEACHER:
				result = false;
			break;
			case STEP_TEAM:
				if ( teamList==null || teamList.size()==0 ) {
					result = true;
				}
			break;
			case STEP_FINAL:
				result = false;				
			break;
		}
		
		return result;
	}
	
	public List<SelectItem> getListaSexo() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		Sexo[] sexos = Sexo.values();
		for (int i = 0; i < sexos.length; i++) {
			result.add(new SelectItem(sexos[i],sexos[i].label));
		}
		return result;
	}
	
	public String getStepTitle() {
		return stepTitles[stepNow];
	}
	
	public String getStepSubTitle() {
		return stepSubTitles[stepNow];
	}
	
	public int getStepsNumber() {
		return stepsNumber;
	}

	public int getStepNow() {
		return stepNow;
	}

	public String[] getStepTitles() {
		return stepTitles;
	}

	public String[] getStepSubTitles() {
		return stepSubTitles;
	}

	public RhythmTable getRhythm() {
		return rhythm;
	}

	public void setRhythm(RhythmTable rhythm) {
		this.rhythm = rhythm;
	}

	public List<RhythmTable> getRhythmList() {
		return rhythmList;
	}

	public List<Pessoa> getTeacherList() {
		return teacherList;
	}

	public List<Team> getTeamList() {
		return teamList;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public Endereco getEnderecoAdvice() {
		return enderecoAdvice;
	}

	public void setEnderecoAdvice(Endereco enderecoAdvice) {
		this.enderecoAdvice = enderecoAdvice;
	}

	public Pessoa getTeacher() {
		return teacher;
	}

	public void setTeacher(Pessoa teacher) {
		this.teacher = teacher;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public DualListModel<String> getTeachers() {
		return teachers;
	}

	public void setTeachers(DualListModel<String> teachers) {
		this.teachers = teachers;
	}

	public void setTeacherList(List<Pessoa> teacherList) {
		this.teacherList = teacherList;
	}
	
}
