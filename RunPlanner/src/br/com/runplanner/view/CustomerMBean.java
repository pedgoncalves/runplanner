package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
import br.com.runplanner.domain.BodyMeasurements;
import br.com.runplanner.domain.Endereco;
import br.com.runplanner.domain.MedicalData;
import br.com.runplanner.domain.PaymentMonths;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.RhythmTable;
import br.com.runplanner.domain.Sexo;
import br.com.runplanner.domain.SystemRoles;
import br.com.runplanner.domain.Team;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.BodyMeasurementsService;
import br.com.runplanner.service.PaymentService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.service.RhythmService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.to.PlanilhaListagemAlunosTO;
import br.com.runplanner.util.Utils;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.PasswordUtil;

@Component("customerMBean")
@Scope("session")
public class CustomerMBean extends BasicMBean {

	private static final String CUSTOMER_FORM_PAGE = "/pages/customer/customer";
	private static final String CUSTOMER_LIST_PAGE = "/pages/customer/customerList";
	private static final String BODYMEASUREMENTS_LIST_PAGE = "/pages/customer/customerBodyMeasurementsList";
	private static final String BODYMEASUREMENTS_FORM_PAGE = "/pages/customer/customerBodyMeasurements";

	private PessoaService pessoaService;
	private BodyMeasurementsService bodyMeasurementsService;
	private TeamService teamService;
	private ReportService reportService;
	private RhythmService rhythmService;
	private PaymentService paymentService;

	private BodyMeasurements bodyMeasurements;
	private Pessoa customer = new Pessoa();

	private Boolean findActive;
	private Long findTeamId;
	private String findName;

	private List<Team> teamList;
	private List<RhythmTable> rhythmList;

	private List<BodyMeasurements> bodyMeasurementsList;
	private List<Pessoa> customerList = new ArrayList<Pessoa>();

	private EmailThreadProductor emailThreadProductor;

	private String subject;
	private String message;
	
	@Autowired
	public CustomerMBean(PessoaService pessoaService,
			BodyMeasurementsService bodyMeasurementsService,
			TeamService teamService, EmailThreadProductor emailThreadProductor,
			ReportService reportService, RhythmService rhythmService,
			PaymentService paymentService) {
		this.pessoaService = pessoaService;
		this.bodyMeasurementsService = bodyMeasurementsService;
		this.teamService = teamService;
		this.emailThreadProductor = emailThreadProductor;
		this.reportService = reportService;
		this.rhythmService = rhythmService;
		this.paymentService = paymentService;
	}

	public void sendEmail() {
		customer = pessoaService.loadById(customer.getId());

		if (customer == null) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
			return;
		}

		emailThreadProductor.enviarMensagem(customer.getEmail(), subject,
				message);
		clearEmail();
		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.email.sucess");
	}

	public void clearEmail() {
		subject = "";
		message = "";
	}

	public String goToList() {
		customer = new Pessoa();
		findTeamId = -1l;
		findActive = true;
		findName = "";

		Pessoa loged = getSessionUser();
		Long adviceId = loged.getAdvice().getId();
		teamList = teamService.getByAdvice(adviceId);

		customerList = pessoaService.getByTipoPessoaAdviceActive(
				TipoPessoa.ALUNO, adviceId, true);

		setSelectedMenu(Constants.MENU_CUSTOMER);
		clearEmail();
		return CUSTOMER_LIST_PAGE;
	}

	public String find() {
		Pessoa loged = getSessionUser();
		Long adviceId = loged.getAdvice().getId();

		if (findTeamId != -1l) {

			if (findName != null && !findName.trim().equals("")) {
				customerList = pessoaService
						.getByNameTipoPessoaAdviceActiveTeam(findName,
								TipoPessoa.ALUNO, adviceId, findActive,
								findTeamId);
			} else {
				customerList = pessoaService.getByTipoPessoaAdviceActiveTeam(
						TipoPessoa.ALUNO, adviceId, findActive, findTeamId);
			}

		} else {
			if (findName != null && !findName.trim().equals("")) {
				customerList = pessoaService.getByNameTipoPessoaAdviceActive(
						findName, TipoPessoa.ALUNO, adviceId, findActive);
			} else {
				customerList = pessoaService.getByTipoPessoaAdviceActive(
						TipoPessoa.ALUNO, adviceId, findActive);
			}
		}
		clearEmail();
		return CUSTOMER_LIST_PAGE;
	}

	public String goToBodyMeasurementsList() {
		this.bodyMeasurements = new BodyMeasurements();

		customer = pessoaService.loadById(customer.getId());
		bodyMeasurementsList = bodyMeasurementsService
				.findByCustomerId(customer.getId());

		return BODYMEASUREMENTS_LIST_PAGE;
	}

	public String goToCreate() {

		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();
		Long adviceId = advice.getId();

		boolean canInsert = canInsertCustomer(advice);
		if (!canInsert) {
			addMessage(FacesMessage.SEVERITY_WARN, "aluno.include.exceed");
			return CUSTOMER_LIST_PAGE;
		}

		rhythmList = rhythmService.getByAdvice(adviceId);

		customer = new Pessoa();

		if (customer.getEndereco() == null) {
			customer.setEndereco(new Endereco());
		}
		if (customer.getMedicalData() == null) {
			customer.setMedicalData(new MedicalData());
		}
		if (customer.getTeam() == null) {
			customer.setTeam(new Team());
		}
		if (customer.getClassification() == null) {
			customer.setClassification(new RhythmTable());
		}
		customer.setAdvice(advice);
		return CUSTOMER_FORM_PAGE;
	}

	private boolean canInsertCustomer(Advice advice) {
		Long totalAlunos = pessoaService.getSumByAdviceActiveTipoPessoa(
				advice.getId(), Boolean.TRUE, TipoPessoa.ALUNO);

		if (totalAlunos >= advice.getStudendNumber()) {
			return false;
		}

		return true;
	}

	public void goToListaAlunoReport() {
		Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;
		
		HashMap<String, Object> param = new HashMap<String, Object>();

		if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}
		param.put("ATIVO", findActive);
		param.put(ReportServiceImpl.NOME_ARQUIVO, "Lista de Alunos RunPlanner");
		
		if (!reportService.gerar(
				ReportServiceImpl.PLANILHA_LISTA_ALUNOS_REPORT, param, montarRelatorioBean(),
				usarLogoRunPlanner)) {
			addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
		}
	}

	private List<PlanilhaListagemAlunosTO> montarRelatorioBean() {
		List<PlanilhaListagemAlunosTO> listaBean = new ArrayList<PlanilhaListagemAlunosTO>();
		customerList = recarregarLista(customerList);
		for (Pessoa p : customerList) {
			PlanilhaListagemAlunosTO to = new PlanilhaListagemAlunosTO();
			to.setNome(p.getNome());
			to.setEmail(p.getEmail());
			to.setRg(p.getRg());
			to.setIdade(p.getIdade());
			to.setTurma(p.getTeam().getDescription());
			to.setAssessoria(p.getAdvice().getName());
			to.setClassificacao(p.getClassification().getClassification());
			
			if(p.getEndereco() != null){
				to.setFoneCelular(p.getEndereco().getFoneCelular());
				to.setFoneResidencial(p.getEndereco().getFoneResidencial());				
			}
			
			listaBean.add(to);
		}
		Collections.sort(listaBean);
		return listaBean;
	}
	
	private List<Pessoa> recarregarLista(List<Pessoa> old) {
		List<Pessoa> result = new ArrayList<Pessoa>();
		
		for(Pessoa p: old) {
			result.add(pessoaService.loadByIdClassification(p.getId()));
		}
		return result;
	}

	public String goToCreateBodyMeasurements() {
		bodyMeasurements = new BodyMeasurements();

		return BODYMEASUREMENTS_FORM_PAGE;
	}

	public String goToModify() {
		Pessoa loged = getSessionUser();
		Long adviceId = loged.getAdvice().getId();
		rhythmList = rhythmService.getByAdvice(adviceId);

		customer = pessoaService.loadByIdFull(customer.getId());

		if (customer == null) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.msg.entitynotfound.edit");
			return goToList();
		}

		if (customer.getEndereco() == null) {
			customer.setEndereco(new Endereco());
		}
		if (customer.getMedicalData() == null) {
			customer.setMedicalData(new MedicalData());
		}
		if (customer.getTeam() == null) {
			customer.setTeam(new Team());
		}
		if (customer.getClassification() == null) {
			customer.setClassification(new RhythmTable());
		}
		return CUSTOMER_FORM_PAGE;
	}

	public String goToModifyBodyMeasurements() {
		bodyMeasurements = bodyMeasurementsService.loadById(bodyMeasurements
				.getId());

		if (bodyMeasurements == null) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
			return goToList();
		}

		return BODYMEASUREMENTS_FORM_PAGE;
	}

	public String save() {

		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();

		boolean canInsert = canInsertCustomer(advice);
		if (customer.getId() == null && !canInsert) {
			addMessage(FacesMessage.SEVERITY_WARN, "aluno.include.exceed");
			return null;
		}

		customer.setAdvice(advice);

		changeEnconding();

		if (customer.getTeam() != null && customer.getTeam().getId() != null
				&& customer.getTeam().getId() != -1) {
			Team team = teamService.loadById(customer.getTeam().getId());
			customer.setTeam(team);
		} else {
			// Lancar erro, nao pode haver aluno sem turma
			addMessage(FacesMessage.SEVERITY_ERROR, "alunos.empty.team");
			return null;
		}

		if (customer.getClassification() != null
				&& customer.getClassification().getId() != null
				&& customer.getClassification().getId() != -1) {
			RhythmTable table = rhythmService.loadById(customer
					.getClassification().getId());
			customer.setClassification(table);
		} else {
			// Lancar erro, nao pode haver aluno sem classificacao
			addMessage(FacesMessage.SEVERITY_ERROR,
					"alunos.empty.classification");
			return null;
		}

		// Verificar se o email ja nao e cadastrado
		Pessoa pessoaEmail = pessoaService.loadByEmailActive(
				customer.getEmail(), true);
		if (pessoaEmail != null) {

			if (customer.getId() == null) { // Novo usuario

				if (pessoaEmail.getAdvice().getId().longValue() == advice
						.getId().longValue()) {
					// Mesma assessoria
					addMessage(FacesMessage.SEVERITY_ERROR,
							"alunos.duplicated.email");
					return null;
				} else {
					// Nova assessoria - desativar na antiga
					try {
						pessoaEmail.setActive(false);
						pessoaService.update(pessoaEmail);
					} catch (EntityNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			// Alteracao de usuario
			else if (pessoaEmail.getId() != null
					&& !customer.getId().equals(pessoaEmail.getId())) {
				if (pessoaEmail.getAdvice().getId().longValue() == advice
						.getId().longValue()) {
					// Mesma assessoria
					addMessage(FacesMessage.SEVERITY_ERROR,
							"alunos.duplicated.email");
					return null;
				} else {
					// Nova assessoria - desativar na antiga
					try {

						pessoaEmail.setActive(false);
						pessoaService.update(pessoaEmail);

						// Reinicia senha
						String generatedPass = PasswordUtil.gerarSenha();
						customer.setPassword(MD5Util.crypt(generatedPass));

						String messageBody = Constants.EMAIL_REGISTRATION_STUDENT_BODY;
						messageBody = messageBody.replace("$1",
								advice.getName());
						messageBody = messageBody.replace("$2",
								customer.getEmail());
						messageBody = messageBody.replace("$3", generatedPass);

						emailThreadProductor.enviarMensagem(
								customer.getEmail(),
								Constants.EMAIL_REGISTRATION_STUDENT_TITLE,
								messageBody);

					} catch (EntityNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// Se tiver foto em disco, apagar do banco
		if (customer.hasUserPhoto())
			customer.setPhoto(null);

		if (customer.getId() == null) {

			if (customer.getDtEntrada() == null) {
				customer.setDtEntrada(new Date());
			}

			customer.setActive(true);
			customer.setRole(SystemRoles.USER);
			customer.setTipoPessoa(TipoPessoa.ALUNO);

			String generatedPass = PasswordUtil.gerarSenha();
			customer.setPassword(MD5Util.crypt(generatedPass));

			customer = pessoaService.persist(customer);

			PaymentMonths payment = new PaymentMonths();
			payment.setCustomer(customer);
			payment.setYear(String.valueOf(new GregorianCalendar()
					.get(Calendar.YEAR)));
			paymentService.persist(payment);

			String messageBody = Constants.EMAIL_REGISTRATION_STUDENT_BODY;
			messageBody = messageBody.replace("$1", advice.getName());
			messageBody = messageBody.replace("$2", customer.getEmail());
			messageBody = messageBody.replace("$3", generatedPass);

			emailThreadProductor.enviarMensagem(customer.getEmail(),
					Constants.EMAIL_REGISTRATION_STUDENT_TITLE, messageBody);

			addMessage(FacesMessage.SEVERITY_INFO, "alunos.save.sucess");

		} else {
			try {
				pessoaService.update(customer);
				addMessage(FacesMessage.SEVERITY_INFO, "alunos.edit.sucess");
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR,
						"template.msg.entitynotfound.edit");
				return goToList();
			}
		}

		return goToList();
	}

	public void restartPassword() {

		customer = pessoaService.loadById(customer.getId());

		String generatedPass = PasswordUtil.gerarSenha();
		customer.setPassword(MD5Util.crypt(generatedPass));

		try {
			pessoaService.update(customer);

			String messageBody = Constants.EMAIL_RESTART_PASS_BODY;
			messageBody = messageBody.replace("$1", customer.getEmail());
			messageBody = messageBody.replace("$2", generatedPass);

			emailThreadProductor.enviarMensagem(customer.getEmail(),
					Constants.EMAIL_RESTART_PASS_TITLE, messageBody);

			addMessage(FacesMessage.SEVERITY_INFO,
					"template.msg.changepass.sucess");

		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
			e.printStackTrace();
		}
	}

	private void changeEnconding() {
		try {
			customer.setNome(new String(customer.getNome().getBytes(
					"iso-8859-1"), "UTF-8"));

			Endereco endereco = customer.getEndereco();
			if (endereco != null) {
				if (endereco.getBairro() != null)
					endereco.setBairro(new String(endereco.getBairro()
							.getBytes("iso-8859-1"), "UTF-8"));
				if (endereco.getComplemento() != null)
					endereco.setComplemento(new String(endereco
							.getComplemento().getBytes("iso-8859-1"), "UTF-8"));
				if (endereco.getLogradouro() != null)
					endereco.setLogradouro(new String(endereco.getLogradouro()
							.getBytes("iso-8859-1"), "UTF-8"));
			}

			MedicalData medical = customer.getMedicalData();
			medical.setHealthHistory(new String(medical.getHealthHistory()
					.getBytes("iso-8859-1"), "UTF-8"));
			medical.setObjective(new String(medical.getObjective().getBytes(
					"iso-8859-1"), "UTF-8"));
			medical.setSportsHistory(new String(medical.getSportsHistory()
					.getBytes("iso-8859-1"), "UTF-8"));
			medical.setObservation(new String(medical.getObservation()
					.getBytes("iso-8859-1"), "UTF-8"));

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String saveBodyMeasurements() {

		bodyMeasurements.setCustomer(customer);

		if (bodyMeasurements.getId() == null) {
			bodyMeasurements.setDtData(new Date());
			bodyMeasurementsService.persist(bodyMeasurements);
		} else {
			try {
				bodyMeasurementsService.update(bodyMeasurements);
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR,
						"template.msg.entitynotfound.edit");
				return goToList();
			}
		}

		addMessage(FacesMessage.SEVERITY_INFO, "alunos.medicaldata.save.sucess");
		return goToBodyMeasurementsList();
	}

	public String delete() {
		try {
			pessoaService.deleteById(customer.getId());
			addMessage(FacesMessage.SEVERITY_INFO, "alunos.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.delete");
		}

		return goToList();
	}

	public String deactive() {
		customer = pessoaService.loadById(customer.getId());
		customer.setActive(false);
		customer.setDtSaida(new Date());
		try {
			pessoaService.update(customer);
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
			return goToList();
		}

		addMessage(FacesMessage.SEVERITY_INFO, "alunos.deactive.sucess");
		return goToList();
	}

	public String reactive() {

		Pessoa loged = getSessionUser();
		Advice advice = loged.getAdvice();

		boolean canInsert = canInsertCustomer(advice);
		if (!canInsert) {
			addMessage(FacesMessage.SEVERITY_WARN, "aluno.include.exceed");
			return goToList();
		}

		customer = pessoaService.loadById(customer.getId());

		// Verificar se o email ja nao e cadastrado
		Pessoa pessoaEmail = pessoaService.loadByEmailActive(
				customer.getEmail(), true);
		if (pessoaEmail != null) {

			if (pessoaEmail.getId() != null
					&& !customer.getId().equals(pessoaEmail.getId())) {
				if (pessoaEmail.getAdvice().getId().longValue() == advice
						.getId().longValue()) {
					// Mesma assessoria
					addMessage(FacesMessage.SEVERITY_ERROR,
							"alunos.duplicated.email");
					return null;
				} else {
					// Nova assessoria - desativar na antiga
					try {
						pessoaEmail.setActive(false);
						pessoaService.update(pessoaEmail);
					} catch (EntityNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		customer.setActive(true);
		customer.setDtSaida(null);

		String generatedPass = PasswordUtil.gerarSenha();
		customer.setPassword(MD5Util.crypt(generatedPass));

		try {
			pessoaService.update(customer);
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
			return goToList();
		}

		// Enviar email
		String messageBody = Constants.EMAIL_REGISTRATION_STUDENT_BODY;
		messageBody = messageBody.replace("$1", advice.getName());
		messageBody = messageBody.replace("$2", customer.getEmail());
		messageBody = messageBody.replace("$3", generatedPass);

		emailThreadProductor.enviarMensagem(customer.getEmail(),
				Constants.EMAIL_REGISTRATION_STUDENT_TITLE, messageBody);

		addMessage(FacesMessage.SEVERITY_INFO, "alunos.active.sucess");
		return goToList();
	}

	public String deleteBodyMeasurements() {
		try {
			bodyMeasurementsService.deleteById(bodyMeasurements.getId());
			addMessage(FacesMessage.SEVERITY_INFO,
					"alunos.medicaldata.delete.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.delete");
		}

		return goToBodyMeasurementsList();
	}

	public List<SelectItem> getListaSexo() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		Sexo[] sexos = Sexo.values();
		for (int i = 0; i < sexos.length; i++) {
			result.add(new SelectItem(sexos[i], sexos[i].label));
		}
		return result;
	}

	public List<SelectItem> getRolesList() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		SystemRoles[] systemRoles = SystemRoles.values();
		for (int i = 0; i < systemRoles.length; i++) {
			result.add(new SelectItem(systemRoles[i], systemRoles[i]
					.getDescription()));
		}
		return result;
	}

	public void handleFileUpload(FileUploadEvent event) {

		// Validar FOTO - Formato e Tamanho
		String fileName = event.getFile().getFileName().toLowerCase();
		Long fileSize = event.getFile().getSize();

		// Validar extensao do arquivo
		boolean valid = false;
		for (String ext : Constants.PHOTO_TYPES) {
			if (fileName.endsWith(ext)) {
				valid = true;
				break;
			}
		}

		if (valid) {
			if (fileSize > Constants.PHOTO_MAX_SIZE) {
				addMessage(FacesMessage.SEVERITY_ERROR,
						"template.msg.sizeExceedMessage");
				return;
			}
		} else {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.invalidFileMessage");
			return;
		}

		String extencao = fileName.substring(fileName.lastIndexOf("."));
		
		if ( !Utils.verifyUserPhotoPath() ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			System.err.println("Erro ao criar diretório para Fotos");
			
			return;
		}
		
		String fotoFileName = Constants.PHOTO_PATH + Constants.PHOTO_USER_NAME
				+ customer.getId() + extencao;

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

		customer.setUserPhoto(fotoFileName);
		customer.setPhoto(foto);
	}

	public StreamedContent getPhoto() {
		if (customer.getPhoto() != null) {
			ByteArrayInputStream stream = new ByteArrayInputStream(
					customer.getPhoto());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}

	public void clearImage() {
		customer.setUserPhoto(null);
		customer.setPhoto(null);
	}

	// -- Get and Set
	public List<SelectItem> getTeamList() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		for (Team team : teamList) {
			result.add(new SelectItem(team.getId(), team.getPlace()));
		}
		return result;
	}

	public List<Pessoa> getCustomerList() {
		return customerList;
	}

	public void setCustomerList(List<Pessoa> customerList) {
		this.customerList = customerList;
	}

	public Pessoa getCustomer() {
		return customer;
	}

	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}

	public PessoaService getPessoaService() {
		return pessoaService;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

	public BodyMeasurementsService getBodyMeasurementsService() {
		return bodyMeasurementsService;
	}

	public void setBodyMeasurementsService(
			BodyMeasurementsService bodyMeasurementsService) {
		this.bodyMeasurementsService = bodyMeasurementsService;
	}

	public BodyMeasurements getBodyMeasurements() {
		return bodyMeasurements;
	}

	public void setBodyMeasurements(BodyMeasurements bodyMeasurements) {
		this.bodyMeasurements = bodyMeasurements;
	}

	public List<BodyMeasurements> getBodyMeasurementsList() {
		return bodyMeasurementsList;
	}

	public void setBodyMeasurementsList(
			List<BodyMeasurements> bodyMeasurementsList) {
		this.bodyMeasurementsList = bodyMeasurementsList;
	}

	public TeamService getTeamService() {
		return teamService;
	}

	public void setTeamService(TeamService teamService) {
		this.teamService = teamService;
	}

	public Boolean getFindActive() {
		return findActive;
	}

	public void setFindActive(Boolean findActive) {
		this.findActive = findActive;
	}

	public Long getFindTeamId() {
		return findTeamId;
	}

	public void setFindTeamId(Long findTeamId) {
		this.findTeamId = findTeamId;
	}

	public void setTeamList(List<Team> teamList) {
		this.teamList = teamList;
	}

	public List<SelectItem> getRhythmList() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		for (RhythmTable table : rhythmList) {
			result.add(new SelectItem(table.getId(), table.getClassification()));
		}
		return result;
	}

	public void setRhythmList(List<RhythmTable> rhythmList) {
		this.rhythmList = rhythmList;
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

	public String getFindName() {
		return findName;
	}

	public void setFindName(String findName) {
		this.findName = findName;
	}

}
