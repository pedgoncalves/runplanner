package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.primefaces.component.tabview.Tab;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.BodyMeasurements;
import br.com.runplanner.domain.Endereco;
import br.com.runplanner.domain.Notification;
import br.com.runplanner.domain.PaymentData;
import br.com.runplanner.domain.PaymentMonthsAdvice;
import br.com.runplanner.domain.PaymentType;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Sexo;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.BodyMeasurementsService;
import br.com.runplanner.service.NotificationService;
import br.com.runplanner.service.PaymentAdviceService;
import br.com.runplanner.service.PaymentDataService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.util.Utils;
import br.com.runplanner.view.util.Constants;
import br.com.uol.pagseguro.domain.AccountCredentials;
import br.com.uol.pagseguro.domain.Currency;
import br.com.uol.pagseguro.domain.PaymentRequest;
import br.com.uol.pagseguro.domain.ShippingType;
import br.com.uol.pagseguro.exception.PagSeguroServiceException;

@Component("profileMBean")
@Scope("session")
public class ProfileMBean extends BasicMBean {
	

	private static final String PROFILE_STUDENT_PAGE = "/pages/student/profile";
	private static final String PROFILE_TEACHER_PAGE = "/pages/teacher/profile";
	private static final String PROFILE_TEACHER_PAGE_USER = "/pages/teacher/profileUser";
	private static final String PROFILE_PUBLIC_PAGE = "/pages/viewProfile";

	@Autowired
	private PessoaService pessoaService;
	@Autowired
	private BodyMeasurementsService bodyMeasurementsService;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private AdviceService adviceService;
	@Autowired
	private LoginMBean loginBean;
	@Autowired
	private PaymentAdviceService paymentAdviceService;
	@Autowired
	private PaymentDataService paymentDataService;
	@Autowired
	private ThemeSwitcherBean themeSwitcherBean;
	
	private Advice advice;
	private BodyMeasurements bodyMeasurements;
	private Pessoa customer = new Pessoa();
	private Endereco endereco;
	private Endereco enderecoAdvice;
	
	private Integer activeIndex = new Integer(0);
    private Integer selectedMonth;
    private Integer selectedYear;
    private PaymentMonthsAdvice payment;
    private String paymentLink;
	
	private List<Activity> activityList;
	private List<Notification> notificationList;
	private List<PaymentMonthsAdvice> paymentList;
	

	private int page = 1;
	private final int PAGE_SIZE = 10;
	
	@Override
	public String goToList() {
		page=1;
		customer = getSessionUser();
		
		customer = pessoaService.loadById(customer.getId());		
		activityList = activityService.findByUserId(customer.getId());
		notificationList = notificationService.findByUser(customer.getId(), 0, PAGE_SIZE);
		paymentLink = null;
		
		endereco = customer.getEndereco();
		if ( endereco == null ) {
			endereco = new Endereco();
		}
		
		List<BodyMeasurements> bodyMeasurementsList = bodyMeasurementsService.findByCustomerId(customer.getId());
		if ( bodyMeasurementsList!=null && bodyMeasurementsList.size()>0 ) {
			bodyMeasurements = bodyMeasurementsList.get(0);
		} else {
			bodyMeasurements = new BodyMeasurements();
		}
		
		activeIndex=new Integer(0);
		setSelectedMenu(Constants.MENU_CUSTOMER);
		
		return PROFILE_STUDENT_PAGE;
	}
	
	public String goToListTeacher() {
		setSelectedMenu(Constants.MENU_PROFILE);
		page=1;
		
		customer = getSessionUser();
		advice = customer.getAdvice();
		paymentLink = null;
		
		customer = pessoaService.loadById(customer.getId());		
		notificationList = notificationService.findByUser(customer.getId(),0, PAGE_SIZE);
		
		endereco = customer.getEndereco();
		if ( endereco == null ) {
			endereco = new Endereco();
		}
		
		/*advice = adviceService.loadById(advice.getId());
		enderecoAdvice = advice.getAdress();
		if ( enderecoAdvice == null ) {
			enderecoAdvice = new Endereco();
		}*/
		
		
		/*if ( customer.getTipoPessoa()==TipoPessoa.ASSESSORIA ) {
			
			selectedYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
			selectedMonth = new GregorianCalendar().get(GregorianCalendar.MONTH);
			
			String year = String.valueOf(selectedYear);		
			
			
			payment = paymentAdviceService.findByAdvice(advice.getId(), year);
			
			if ( payment==null ) {
				payment = new PaymentMonthsAdvice();
				payment.setYear(year);
			}
			
			
			this.paymentList = new ArrayList<PaymentMonthsAdvice>();
			this.paymentList.add(payment);
		}*/
		

		activeIndex=new Integer(0);
		
		return PROFILE_TEACHER_PAGE_USER;
	}
	
	public String goToListAdvice() {
		setSelectedMenu(Constants.MENU_ADVICE);
		page=1;
		
		customer = getSessionUser();
		advice = customer.getAdvice();
		paymentLink = null;
		
		advice = adviceService.loadById(advice.getId());
		enderecoAdvice = advice.getAdress();
		if ( enderecoAdvice == null ) {
			enderecoAdvice = new Endereco();
		}
		
		
		/*if ( customer.getTipoPessoa()==TipoPessoa.ASSESSORIA ) {
			
			selectedYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
			selectedMonth = new GregorianCalendar().get(GregorianCalendar.MONTH);
			
			String year = String.valueOf(selectedYear);		
			
			
			payment = paymentAdviceService.findByAdvice(advice.getId(), year);
			
			if ( payment==null ) {
				payment = new PaymentMonthsAdvice();
				payment.setYear(year);
			}
			
			
			this.paymentList = new ArrayList<PaymentMonthsAdvice>();
			this.paymentList.add(payment);
		}*/
		

		activeIndex=new Integer(0);
		
		return PROFILE_TEACHER_PAGE;
	}
	
	public void payment() {
		
		activeIndex=new Integer(4);
		
		String reference = selectedMonth+"/"+selectedYear+"/"+advice.getId(); 
		String description = "Mensalidade RunPlanner - "+advice.getName();
		String transactionCode = null;
		double value = advice.getValue();
			
		
		PaymentRequest paymentRequest = new PaymentRequest();  
	    paymentRequest.setCurrency(Currency.BRL);  
	    
	    BigDecimal valor = new BigDecimal(value);
	    valor = valor.setScale(2);
	    
	    paymentRequest.addItem(  
	    	    reference,  
	    	    description,   
	    	    new Integer(1),   
	    	    valor,   
	    	    new Long(0),   
	    	    null  
	    	);  
	    
	    paymentRequest.setShippingType(ShippingType.NOT_SPECIFIED);
	    
	    try {
			URL paymentURL = paymentRequest.register(new  
				    AccountCredentials(  
			    		Constants.PAGSEGURO_EMAIL,   
				    	Constants.PAGSEGURO_TOKEN)  
				    );
			
			String query = paymentURL.getQuery();
			String[] params = query.split("&");
			
			for ( String p:params ) {
				if ( p.startsWith("code") ) {
					int index = p.indexOf("=");
					transactionCode = p.substring(index+1);
					break;
				}
			}
			
			
			if ( transactionCode==null ) {
				//TODO Lancar erro
			}
			
			//Salvar no Banco a operacao
			PaymentData paymentData = new PaymentData();
			paymentData.setAdvice(advice);
			paymentData.setDate( new Date() );
			paymentData.setDescription(description);
			paymentData.setReference(reference);
			paymentData.setTransactionCode(transactionCode);
			paymentData.setValue(value);
			paymentData.setMonth( selectedMonth );
			paymentData.setYear( selectedYear );
			paymentData.setValidated(false);
			paymentData.setType(PaymentType.PAGSEGURO);
			paymentData.setPaymentUrl(paymentURL.toString());
			
			//Buscar os pagamentos mensais e atualizar
			if ( payment.getId()==null ) {
				payment = new PaymentMonthsAdvice();
				payment.setYear(selectedYear.toString());
				payment.setAdvice(advice);
				payment.setPaymentDataList(new ArrayList<PaymentData>());
				payment = paymentAdviceService.persist(payment);
				
			}
			
			paymentData = paymentDataService.persist(paymentData);
			payment.getPaymentDataList().add(paymentData);
			
			try {
				paymentAdviceService.update(payment);
			} catch (EntityNotFoundException e) {
				// TODO Tratar erro
			}
			
			
			//Redirecionar o usuario para pagamento
			paymentLink = paymentURL.toString();
			
		} catch (PagSeguroServiceException e) {
			e.printStackTrace();
			//TODO Tratar erro
		}  
	    
	}
	
	public String goToView() {	

		page=1;
		
		customer = pessoaService.loadById(customer.getId());		
		activityList = activityService.findByUserId(customer.getId());
		notificationList = notificationService.findByUser(customer.getId(), 0, PAGE_SIZE);
		
		endereco = customer.getEndereco();
		if ( endereco == null ) {
			endereco = new Endereco();
		}
	
		setSelectedMenu(Constants.MENU_NONE);
		
		return PROFILE_PUBLIC_PAGE;
	}
	
	 public void loadMore() {			
		 	notificationList.addAll(notificationService.findByUser(customer.getId(), page*PAGE_SIZE, PAGE_SIZE));

			page++;
	    }

	@Override
	public String goToCreate() {
		return null;
	}

	@Override
	public String goToModify() {
		return null;
	}

	@Override
	public String delete() {
		return null;
	}

	@Override
	public String save() {
		
		if ( customer.getNome().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.nome.mandatory");
			return null;
		}
		
		if ( customer.getEmail().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.email.mandatory");
			return null;
		}
		
		try {
			customer.setNome(  new String (customer.getNome().getBytes ("iso-8859-1"), "UTF-8") );			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		//Verificar se o email ja nao e cadastrado
		Pessoa pessoaEmail = pessoaService.loadByEmailActive(customer.getEmail(),true);
		if ( pessoaEmail !=null ) {
			if ( customer.getId()==null ) {
				addMessage(FacesMessage.SEVERITY_ERROR,"alunos.duplicated.email");
				return null;
			}
			if( pessoaEmail.getId()!=null && !customer.getId().equals(pessoaEmail.getId()) ) {
				addMessage(FacesMessage.SEVERITY_ERROR,"alunos.duplicated.email");
				return null;
			}
		}
		
		//Se tiver foto em disco, apagar do banco
		if ( customer.hasUserPhoto() ) customer.setPhoto(null);
		
		try {
			pessoaService.update(customer);
			
			if ( customer.getPhoto()!=null ) {
				loginBean.setPhoto(customer.getPhoto());
			}
			loginBean.setName(customer.getNome());
			
			addMessage(FacesMessage.SEVERITY_INFO,"profile.edit.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			return goToList();
		} 
		return null;
	}

	public String saveAdress() {
		
		try {			
			endereco.setBairro(  new String (endereco.getBairro().getBytes ("iso-8859-1"), "UTF-8") );
			endereco.setComplemento(  new String (endereco.getComplemento().getBytes ("iso-8859-1"), "UTF-8") );
			endereco.setLogradouro(  new String (endereco.getLogradouro().getBytes ("iso-8859-1"), "UTF-8") );
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		customer = pessoaService.loadById(customer.getId());
		customer.setEndereco(endereco);
		
		try {
			pessoaService.update(customer);
			addMessage(FacesMessage.SEVERITY_INFO,"profile.edit.sucess");
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			e.printStackTrace();
			return goToList();
		} 
		return null;
	}
	
	public String saveBodyMeasurements() {
		
		customer = pessoaService.loadById(customer.getId());
		bodyMeasurements.setCustomer(customer);

		bodyMeasurements.setId(null);
		bodyMeasurements.setDtData(new Date());		
		bodyMeasurementsService.persist(bodyMeasurements);

		addMessage(FacesMessage.SEVERITY_INFO,"profile.edit.sucess");
		
		return null;
	}	
	
	public String saveAdvice() {
		advice.setAdress(enderecoAdvice);
		
		if ( advice.getName().trim().equals("") ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"template.nome.mandatory");
			return null;
		}
		
		try {
			
			//Se tiver foto em disco, apagar do banco
			if ( advice.hasAdvicePhoto() ) advice.setLogo(null);
			
			advice.setTheme(themeSwitcherBean.getTheme());
			adviceService.update(advice);
			
			if ( advice.getLogo()!=null ) {
				loginBean.setAdviceLogo(advice.getLogo());
			}
			loginBean.setAdviceName(advice.getName());
			loginBean.setAdviceBanner(advice.getAdviceBanner());
			getSessionUser().setAdvice(advice);
			
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
			e.printStackTrace();
		}
		
		addMessage(FacesMessage.SEVERITY_INFO,"profile.edit.sucess");
		
		return goToListAdvice();
	}
	
	
	public void clearImage() {
		customer.setPhoto(null);
	}
	
	public void handleFileUpload(FileUploadEvent event) {  
		
		Pessoa user = getSessionUser();
		
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
		
		String fotoFileName = Constants.PHOTO_PATH+Constants.PHOTO_USER_NAME+user.getId()+extencao;
		
		byte[] foto = event.getFile().getContents();
		
		try {			
			File fileFoto = new File(fotoFileName);		
			
			fileFoto.createNewFile();
			
			FileOutputStream out = new FileOutputStream(fileFoto);
			out.write(foto);
			
			out.flush();
			out.close();

			fileFoto.getAbsolutePath();
			
		} catch (FileNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			e.printStackTrace();
		} catch (IOException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			e.printStackTrace();
		}
		
		customer.setUserPhoto(fotoFileName);
        customer.setPhoto( foto ); 
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
    } 
	
	public void handleFileUploadAdviceBanner(FileUploadEvent event) { 

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
		
		if ( !Utils.verifyAdviceBannerPhotoPath() ) {
			addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.foto.erro");
			System.err.println("Erro ao criar diretório para Fotos");
			
			return;
		}
		
		String fotoFileName = Constants.PHOTO_PATH+Constants.PHOTO_ADVICE_BANNER_NAME+advice.getId()+extencao;
		
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
		
		advice.setAdviceBanner(fotoFileName);
    } 
	
	public StreamedContent getPhoto() {
		if ( customer.getPhoto()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(customer.getPhoto());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}
	
	public StreamedContent getPhotoAdvice() {
		if ( advice.getLogo()!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(advice.getLogo());
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}
	
	public void onChange(TabChangeEvent event) {
		Tab newTab = event.getTab();
		String tabIndex = newTab.getId().replaceAll("tab", "");
		
		activeIndex = Integer.valueOf(tabIndex);
	}
	
	//-- Gets and Sets
	
	public int getActivityNumber() {
		if (activityList==null) return 0;
		else return activityList.size();
	}
	
	public double getActivityDistance() {
		if (activityList==null) return 0;
		else {
			double distance = 0;
			for(Activity act:activityList) {
				distance += act.getTotalDistance();
			}
			return distance;
		}
	}
	
	public String getActivityTime() {
		if (activityList==null) return "00:00:00";
		else {
			double time = 0;
			for(Activity act:activityList) {
				time += act.getTotalTimeInSeconds();
			}
			return Utils.formataTempo( (long)time );
		}
	}
		
	public BodyMeasurements getBodyMeasurements() {
		return bodyMeasurements;
	}

	public void setBodyMeasurements(BodyMeasurements bodyMeasurements) {
		this.bodyMeasurements = bodyMeasurements;
	}

	public Pessoa getCustomer() {
		return customer;
	}

	public void setCustomer(Pessoa customer) {
		this.customer = customer;
	}
	
	public List<SelectItem> getListaSexo() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		Sexo[] sexos = Sexo.values();
		for (int i = 0; i < sexos.length; i++) {
			result.add(new SelectItem(sexos[i],sexos[i].label));
		}
		return result;
	}

    // Util Month List to Filter Combobox    
    public List<SelectItem> getMonthList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();
    	String months[] = new DateFormatSymbols(new Locale("pt","BR")).getMonths();
    	for (int i = 0; i < 12; i++) {
			result.add( new SelectItem(i,months[i]) );
		}
    	return result;
    }
    
    public List<SelectItem> getYearList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();
    	
    	GregorianCalendar date = new GregorianCalendar();
    	Integer year = date.get(GregorianCalendar.YEAR);
    	
    	result.add( new SelectItem(year,year.toString()) );
    	year++;
    	result.add( new SelectItem(year,year.toString()) );
    	
    	return result;
    }     
	
	public List<Notification> getNotificationList() {
		return notificationList;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
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

	public Integer getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(Integer activeIndex) {
		this.activeIndex = activeIndex;
	}

	public List<PaymentMonthsAdvice> getPaymentList() {
		return paymentList;
	}

	public void setPaymentList(List<PaymentMonthsAdvice> paymentList) {
		this.paymentList = paymentList;
	}

	public Integer getSelectedMonth() {
		return selectedMonth;
	}

	public void setSelectedMonth(Integer selectedMonth) {
		this.selectedMonth = selectedMonth;
	}

	public Integer getSelectedYear() {
		return selectedYear;
	}

	public void setSelectedYear(Integer selectedYear) {
		this.selectedYear = selectedYear;
	}

	public String getPaymentLink() {
		return paymentLink;
	}

	public void setPaymentLink(String paymentLink) {
		this.paymentLink = paymentLink;
	}
	

}
