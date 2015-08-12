package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.CartesianChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Event;
import br.com.runplanner.domain.Notification;
import br.com.runplanner.domain.Partner;
import br.com.runplanner.domain.PaymentMonths;
import br.com.runplanner.domain.PaymentMonthsAdvice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.RhythmTable;
import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.Spreadsheet;
import br.com.runplanner.domain.StatusAdvice;
import br.com.runplanner.domain.Team;
import br.com.runplanner.domain.TipoPessoa;
import br.com.runplanner.service.ActivityService;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.EventService;
import br.com.runplanner.service.MailboxService;
import br.com.runplanner.service.NotificationService;
import br.com.runplanner.service.PartnerService;
import br.com.runplanner.service.PaymentAdviceService;
import br.com.runplanner.service.PaymentService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.RhythmService;
import br.com.runplanner.service.SpreadsheetService;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.to.TopActivityTO;
import br.com.runplanner.util.Utils;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MessagesResources;

@Component("dashboardMBean")
@Scope("session")
public class DashboardMBean implements Serializable {

	private static final long serialVersionUID = -9111549063923619689L;
	
	
	@Autowired
	private PessoaService pessoaService;	
	@Autowired
	private PaymentService paymentService;	
	@Autowired
	private PaymentAdviceService paymentAdviceService;	
	@Autowired
	private NotificationService notificationService;	
	@Autowired
	private SpreadsheetService spreadsheetService;
	@Autowired
	private EventService eventService;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private RhythmService rhythmService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private MailboxService mailboxService;
	@Autowired
	private AdviceService adviceService;
	@Autowired
	private PartnerService partnerService;
	
	private List<Notification> notifications = new ArrayList<Notification>();
	private List<Pessoa> birthdayList = new ArrayList<Pessoa>();
	private List<Pessoa> paymentList = new ArrayList<Pessoa>();
	private List<Pessoa> spreadlessList = new ArrayList<Pessoa>();	
	private List<Activity> activityList = new ArrayList<Activity>();
	private List<Spreadsheet> spreadsheetList = new ArrayList<Spreadsheet>();
	private List<Event> eventList = new ArrayList<Event>();	
	private List<TopActivityTO> topList = new ArrayList<TopActivityTO>();
	@SuppressWarnings("unused")
	private List<Advice> adviceList = new ArrayList<Advice>();
	private Schedule proximoTreino;
	private Event nextEvent;
	
	private Long eventNumber = 0l;
	private Long activeTeacherNumber = 0l;
	private Long activeCustomerNumber = 0l;
	private Integer studendNumber = 0;

	private Long longestActivityKmId = 0l;
	private Double longestActivityKm = 0d;
	private Date longestActivityKmDate = null;
	
	private Long longestActivityTimeId = 0l;
	private Double longestActivityTimeSecond = 0d;
	private String longestActivityTime = "00:00:00";
	private Date longestActivityTimeDate = null;
	
	private Long longestActivityPaceId = 0l;
	private Double longestActivityPaceMin = Double.MAX_VALUE;
	private String longestActivityPace = "00:00:00";
	private Date longestActivityPaceDate = null;

	private Long newMessageNumber = 0l;
	
    private CartesianChartModel activityNumberGraph;
	
	//Wizard
	private boolean wizard = false;
	private List<RhythmTable> rhythmList;
	private List<Pessoa> teacherList;
	private List<Team> teamList;
	
	//Notificacao de Pagamento
	private boolean paymentNotification = false;
	
	private int page = 1;
	private final int PAGE_SIZE = 10;
	
	private Hashtable<Long,Object> photoCache = new Hashtable<Long,Object>();
	
	//Publicidade
	private int partnerListSize = 2;
	private List<Partner> partnerList = new ArrayList<Partner>(partnerListSize);
	
	//Titulos para TopActivity 
	private String topActivityTitle = "";
	private String topActivityTitle5 = "5KM ";
	private String topActivityTitle10 = "10KM ";
	private String topActivityTitle21 = "21KM ";
	private String topActivityTitle42 = "42KM ";
	private String topActivityTitleMas = "Masculino";
	private String topActivityTitleFem = "Feminino";
	private String topActivitySelected = "";
	
    public DashboardMBean() {  
              
    }
    
    //Metodo chamado para carregar os dados
    public String getData() {
    	HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		session.setAttribute(Constants.SELECTED_MENU, Constants.MENU_NONE);
		
		Pessoa customer = getSessionUser();
		Advice advice = customer.getAdvice();
		studendNumber = advice.getStudendNumber();
		List<Partner> partners = partnerService.findByAdvice(advice.getId());
		advice.setPartners(partners);
		
		page = 1;
		
		GregorianCalendar calendar = new GregorianCalendar();
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		//int year = calendar.get(Calendar.YEAR);	
		
		if ( advice.getStatus().equals(StatusAdvice.PAGANDO) ) {
			partnerList = advice.getPartners(partnerListSize);
		}
		

		//Eventos
		eventList = eventService.getByAdvice(advice.getId());
		if (eventList!=null && eventList.size()>0) {
			nextEvent = eventList.get(eventList.size()-1);
		}
		
		//Melhores tempos de 5KM
		topList = activityService.getTopActivity5k(advice.getId(), 0);
		topActivitySelected = "5M";
		topActivityTitle = topActivityTitle5+topActivityTitleMas;
		
		if ( customer.getTipoPessoa() == TipoPessoa.PROFESSOR || customer.getTipoPessoa() == TipoPessoa.ASSESSORIA ) {	
			
			//Notificacoes	    	
	    	notifications = notificationService.getByAdvice(customer.getAdvice().getId(), 0, PAGE_SIZE);	
	    	
	    	//Aniversariantes
			birthdayList = pessoaService.getByTipoPessoaAdviceAniversario(TipoPessoa.ALUNO,advice.getId(),month,day);
			
			//Mensagens
			newMessageNumber = mailboxService.getNewByUserId(customer.getId());

			//Numeros - Alunos					
			activeCustomerNumber = pessoaService.getSumByAdviceActiveTipoPessoa(customer.getAdvice().getId(), true, TipoPessoa.ALUNO);
		}
		else if ( customer.getTipoPessoa() == TipoPessoa.ALUNO ) {
			
			//Notificacoes	    	
			notifications = notificationService.getByAdvice(customer.getAdvice().getId(), 0, PAGE_SIZE);		
			
			//Lista de atividades
			/*activityList = activityService.findByUserId(customer.getId());	
			
			for( Activity act:activityList ) {
				
				if (act.getTotalDistance()>longestActivityKm) {
					longestActivityKm = act.getTotalDistance();
					longestActivityKmDate = act.getDate();
					longestActivityKmId = act.getId();
				}
				
				if (act.getTotalTimeInSeconds()>longestActivityTimeSecond) {
					longestActivityTimeSecond = act.getTotalTimeInSeconds();
					longestActivityTime = act.getTotalTime();
					longestActivityTimeDate = act.getDate();
					longestActivityTimeId = act.getId();
				}
				
				if (act.getAveragePaceMinutes()<longestActivityPaceMin) {
					longestActivityPaceMin = act.getAveragePaceMinutes();
					longestActivityPace = act.getAveragePace();
					longestActivityPaceDate = act.getDate();
					longestActivityPaceId = act.getId();
				}
				
			}*/
			
			//Atividade de maior distancia
			Activity act = activityService.getLongestActivityKm(customer.getId());
			if ( act!=null ) {
				longestActivityKm = act.getTotalDistance();
				longestActivityKmDate = act.getDate();
				longestActivityKmId = act.getId();
			}
			

			//Atividade de maior tempo
			act = activityService.getLongestActivityTime(customer.getId());
			if ( act!=null ) {
				longestActivityTimeSecond = act.getTotalTimeInSeconds();
				longestActivityTime = act.getTotalTime();
				longestActivityTimeDate = act.getDate();
				longestActivityTimeId = act.getId();
			}
			
			//Atividade de maior pace
			act = activityService.getLongestActivityPace(customer.getId());
			if ( act!=null ) {
				longestActivityPaceMin = act.getAveragePaceMinutes();
				longestActivityPace = act.getAveragePace();
				longestActivityPaceDate = act.getDate();
				longestActivityPaceId = act.getId();
			}
			
			//Buscar proximo treino
			proximoTreino = spreadsheetService.findByStudentActual(customer.getId());
			
			//Aniversariantes
			birthdayList = pessoaService.getByTipoPessoaAdviceAniversario(TipoPessoa.ALUNO,advice.getId(),month);
			
			//Notificacao de pagamento
			if ( customer.getAdvice().getPaymentNotificationDialog() ) {
				
				int paymentDay = customer.getCustomerPaymentDay();
				
				if ( paymentDay >= calendar.get(Calendar.DAY_OF_MONTH)  ) {
					paymentNotification=false;
				}
				else {
					PaymentMonths payments = paymentService.findByCustomerYear(customer.getId(), String.valueOf(calendar.get(Calendar.YEAR)) );
					paymentNotification = !payments.isPaid( calendar.get(Calendar.MONTH) );
				}
				
			}
			else {
				paymentNotification=false;
			}
		}   
		else if ( customer.getTipoPessoa() == TipoPessoa.ADMINISTRADOR ) {
			
			adviceList = adviceService.loadAllActiveEager(true);
		}
		
		
		if ( customer.getTipoPessoa() == TipoPessoa.ASSESSORIA ) {		
			
			//Pagamento		
			paymentList = new ArrayList<Pessoa>();
			List<Pessoa> customerList = pessoaService.getByTipoPessoaAdviceActive(TipoPessoa.ALUNO,advice.getId(),true);
			List<PaymentMonths> list = paymentService.findByAdvice(advice.getId(), String.valueOf(calendar.get(Calendar.YEAR)) );
			for(PaymentMonths payment:list) {
				if ( payment.isPaid(month) ) {
					customerList.remove(payment.getCustomer());
				}
			}
			paymentList.addAll(customerList);
				
			//VERIFICAR NECESSIDADE DE WIZARD
			/* 1. Exitem Tabelas de Ritmo?
			 * 2. Existem Turmas?
			 */
			rhythmList = rhythmService.getByAdvice(advice.getId());
			teamList = teamService.getByAdvice(advice.getId());
			if( rhythmList.size()<=0 || teamList.size()<=0 ) {
				wizard = true;
			} 
			else {
				wizard = false;
			}
			
			//Notificacao de pagamento			
			if (advice.getAdvicePaymentDay()==null) {
				paymentNotification=false;
			}
			else {
				int advicePaymentDay = advice.getAdvicePaymentDay();
	
				if (advice.getStatus().equals(StatusAdvice.PAGANDO)) {
					if ( advicePaymentDay >= calendar.get(Calendar.DAY_OF_MONTH)  ) {
						paymentNotification=false;
					} else {
						PaymentMonthsAdvice payments = paymentAdviceService.findByAdvice(advice.getId(), String.valueOf(calendar.get(Calendar.YEAR)) );
						if (payments == null) {
							//Se retornou null, é pq nenhum pagamento neste ano solicitado foi realizado.
							//Só deve acontecer no primeiro mês da assessoria a cada ano.
							paymentNotification = true;
						} else {
							paymentNotification = !payments.isPaid( calendar.get(Calendar.MONTH) );
						}
					}
				} else {
					paymentNotification=false;
				}
			}
		}
		
		
		return "";
    }
    
    public void handleChangeTopActivity() {
    	Pessoa customer = getSessionUser();
		Advice advice = customer.getAdvice();		
		
		if (topActivitySelected.equals("5M")) {
			topList = activityService.getTopActivity5k(advice.getId(), 0);
			topActivityTitle = topActivityTitle5+topActivityTitleMas;
		} else if (topActivitySelected.equals("5F")) {
			topList = activityService.getTopActivity5k(advice.getId(), 1);
			topActivityTitle = topActivityTitle5+topActivityTitleFem;
		} else if (topActivitySelected.equals("10M")) {
			topList = activityService.getTopActivity10k(advice.getId(), 0);
			topActivityTitle = topActivityTitle10+topActivityTitleMas;
		} else if (topActivitySelected.equals("10F")) {
			topList = activityService.getTopActivity10k(advice.getId(), 1);
			topActivityTitle = topActivityTitle10+topActivityTitleFem;
		} else if (topActivitySelected.equals("21M")) {
			topList = activityService.getTopActivity21k(advice.getId(), 0);
			topActivityTitle = topActivityTitle21+topActivityTitleMas;
		} else if (topActivitySelected.equals("21F")) {
			topList = activityService.getTopActivity21k(advice.getId(), 1);
			topActivityTitle = topActivityTitle21+topActivityTitleFem;
		} else if (topActivitySelected.equals("42M")) {
			topList = activityService.getTopActivity42k(advice.getId(), 0);
			topActivityTitle = topActivityTitle42+topActivityTitleMas;
		} else if (topActivitySelected.equals("42F")) {
			topList = activityService.getTopActivity42k(advice.getId(), 1);
			topActivityTitle = topActivityTitle42+topActivityTitleFem;
		}
		
	}

	
	public String goToIndex() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		session.setAttribute(Constants.SELECTED_MENU, Constants.MENU_NONE);
		
		Pessoa customer = getSessionUser();
		
		
		if ( customer.getTipoPessoa() == TipoPessoa.PROFESSOR ) {							
			return "/pages/teacher/indexV2";
		}
		else if ( customer.getTipoPessoa() == TipoPessoa.ASSESSORIA ) {				
			return "/pages/teacher/indexV2";
		}
		else if ( customer.getTipoPessoa() == TipoPessoa.ALUNO ) {	
			return "/pages/student/indexV2";
		}
		
		return "/pages/advice/index";
	}
    
    public List<Notification> getNotifications() {
    	return notifications;
    }
    
    public void loadMore() {
		Pessoa customer = getSessionUser();
		
		notifications.addAll(notificationService.getByAdvice(customer.getAdvice().getId(), page*PAGE_SIZE, PAGE_SIZE));

		page++;
    }
       
    
	public StreamedContent getStreamedUserPhoto() {
		
		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("client_id");
				
		if( id==null || id.equals("") ) {
			return getDefaultPhoto();
		}		
		
		Long clientId = Long.parseLong(id);
		
		byte[] photo = (byte[]) photoCache.get(clientId);				
		if ( photo!=null ) {
			ByteArrayInputStream photoStream = new ByteArrayInputStream(photo);
			return new DefaultStreamedContent(photoStream, "image/jpeg");
		}
		else {
			Pessoa pessoa = pessoaService.loadById(clientId);
			
			if ( pessoa.getPhoto()!= null ) {
				ByteArrayInputStream photoStream = new ByteArrayInputStream(pessoa.getPhoto());
				
				photoCache.put( clientId, pessoa.getPhoto().clone() );
				return new DefaultStreamedContent(photoStream, "image/jpeg");
			}
			else {
				return getDefaultPhoto();
			}
		}
		
	}

	private DefaultStreamedContent getDefaultPhoto() {
		String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_PHOTO;
		
		try {
			FileInputStream is = new FileInputStream(new File(photoPath));
			byte[] photo = new byte[is.available()];
			is.read(photo);
			
			ByteArrayInputStream stream = new ByteArrayInputStream(photo);

			is.close();
			return new DefaultStreamedContent(stream, "image/jpeg");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Grafico de Distancia de Atividades por Mes
	@SuppressWarnings("deprecation")
	public String getActivityDistanceGraphSerie() {

		GregorianCalendar calendarInit = new GregorianCalendar();
    	calendarInit.add(Calendar.MONTH, -6);
    	
    	Date initialDate = calendarInit.getTime();
    	Date finalDate = new Date();
    	
		
		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
    		return null;
        }
        
		Hashtable<String, Double> values = new Hashtable<String, Double>();	
    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {
    		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;
    		
    		values.put( new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year , 0d );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}
    	
    	
    	int month = activityList.get(0).getDate().getMonth();    	
    	int year  = activityList.get(0).getDate().getYear()+1900;
    	
    	double monthValue = 0;
    	for(Activity act:activityList) {
    		
    		if ( month == act.getDate().getMonth() && year == act.getDate().getYear()+1900 ) {    			
    			monthValue += act.getTotalDistance();    			
    		} else {    			
    			String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    			values.put(key, monthValue);
    			
    			month = act.getDate().getMonth();
    			year = act.getDate().getYear()+1900;
    			monthValue = act.getTotalDistance();    			    			
    		}
    	}    	
    	String key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		values.put(key, monthValue);
    	
		StringBuffer serie = new StringBuffer("[");
		d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {    		
    		        		
    		month = d0.getMonth();
    		year  = d0.getYear()+1900;
    		
    		key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
    		
    		Double v = values.get(key);
    		serie.append( v );
			serie.append( "," );
    		
    		GregorianCalendar calendar = new GregorianCalendar();
    		calendar.setTime(d0);
    		calendar.set(Calendar.DATE,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    
    	key = new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year;
		
		Double v = values.get(key);
		serie.append( v );
				
    	serie.append("]");
    	return serie.toString();  		
		
	}
	
	//Descricao do eixo X para os graficos de atividade
	@SuppressWarnings("deprecation")
	public String getActivityGraphTick() {
		GregorianCalendar calendarInit = new GregorianCalendar();
    	calendarInit.add(Calendar.MONTH, -6);
    	
    	Date initialDate = calendarInit.getTime();
    	Date finalDate = new Date();
		
		StringBuffer ticks = new StringBuffer("[");
		
		if ( activityList==null || activityList.size()<=0 ) {
    		activityNumberGraph = null;
        	return "[]";
        }
    	    	    	
    	Date d0 = (Date)initialDate.clone();
    	while ( d0.before(finalDate) ) {
    		
    		int month = d0.getMonth();
    		int year  = d0.getYear()+1900;
    		
    		ticks.append( "'" + new DateFormatSymbols(new Locale("pt","BR")).getShortMonths()[month]+"/"+year + "'");
    		ticks.append(",");
    		
    		GregorianCalendar calendar = new GregorianCalendar();    		
    		calendar.setTime(d0);
    		calendar.set(Calendar.DAY_OF_MONTH,1);
    		calendar.add(Calendar.MONTH, 1);
    		
    		d0=calendar.getTime();    		
    	}    	
		
    	ticks.deleteCharAt( ticks.length()-1 );
		ticks.append("]");
		
		return ticks.toString();
	}	
	// FIM Grafico de Distancia de Atividades por Mes
    
    public List<Pessoa> getBirthdayList() {
		return birthdayList;
	}
	
	public List<Pessoa> getPaymentList() {	
		return paymentList;
	}	
	
	public List<Pessoa> getSpreadless() {
		return spreadlessList;
	}
	
	public Long getActiveTeacherNumber() {
		return activeTeacherNumber;
	}

	public Long getActiveCustomerNumber() {
		return activeCustomerNumber;
	}

	public long getEventNumber() {
		return eventNumber;
	}
	
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

	
	//------------
    protected void addMessage(Severity severity, String sumaryKey) {  
	    	
    	FacesMessage message = new FacesMessage(severity, 
    			MessagesResources.getStringFromBundle(sumaryKey,""),"");  
    	
        FacesContext.getCurrentInstance().addMessage(null, message);  
    }
    
	protected Pessoa getSessionUser() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		return ((Pessoa) session.getAttribute(Constants.USER_SESSION));
	}

	public String getSelectedMenu() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		
		Object menu = session.getAttribute(Constants.SELECTED_MENU);
		
		if ( menu==null ) {
			menu = Constants.MENU_NONE;
		}
		
		return menu.toString();
	}
	
	public String getAdviceName() {
		Pessoa loged = getSessionUser();   
		return loged.getAdvice().getName();
	}
    	
	public List<Spreadsheet> getSpreadsheetList() {
		return spreadsheetList;
	}

	public List<Activity> getActivityList() {
		return activityList;
	}

	public List<Event> getEventList() {
		return eventList;
	}

	public void setEventList(List<Event> eventList) {
		this.eventList = eventList;
	}

	public Schedule getProximoTreino() {
		return proximoTreino;
	}

	public void setProximoTreino(Schedule proximoTreino) {
		this.proximoTreino = proximoTreino;
	}

	public boolean getWizard() {
		return wizard;
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

	public boolean getPaymentNotification() {
		return paymentNotification;
	}

	public CartesianChartModel getActivityNumberGraph() {
		return activityNumberGraph;
	}

	public void setActivityNumberGraph(CartesianChartModel activityNumberGraph) {
		this.activityNumberGraph = activityNumberGraph;
	}

	public Double getLongestActivityKm() {
		return longestActivityKm;
	}

	public void setLongestActivityKm(Double longestActivityKm) {
		this.longestActivityKm = longestActivityKm;
	}

	public Date getLongestActivityKmDate() {
		return longestActivityKmDate;
	}

	public void setLongestActivityKmDate(Date longestActivityKmDate) {
		this.longestActivityKmDate = longestActivityKmDate;
	}

	public String getLongestActivityTime() {
		return longestActivityTime;
	}

	public void setLongestActivityTime(String longestActivityTime) {
		this.longestActivityTime = longestActivityTime;
	}

	public Date getLongestActivityTimeDate() {
		return longestActivityTimeDate;
	}

	public void setLongestActivityTimeDate(Date longestActivityTimeDate) {
		this.longestActivityTimeDate = longestActivityTimeDate;
	}

	public Double getLongestActivitySecond() {
		return longestActivityTimeSecond;
	}

	public void setLongestActivitySecond(Double longestActivitySecond) {
		this.longestActivityTimeSecond = longestActivitySecond;
	}

	public Double getLongestActivityTimeSecond() {
		return longestActivityTimeSecond;
	}

	public void setLongestActivityTimeSecond(Double longestActivityTimeSecond) {
		this.longestActivityTimeSecond = longestActivityTimeSecond;
	}

	public Double getLongestActivityPaceMin() {
		return longestActivityPaceMin;
	}

	public void setLongestActivityPaceMin(Double longestActivityPaceMin) {
		this.longestActivityPaceMin = longestActivityPaceMin;
	}

	public String getLongestActivityPace() {
		return longestActivityPace;
	}

	public void setLongestActivityPace(String longestActivityPace) {
		this.longestActivityPace = longestActivityPace;
	}

	public Date getLongestActivityPaceDate() {
		return longestActivityPaceDate;
	}

	public void setLongestActivityPaceDate(Date longestActivityPaceDate) {
		this.longestActivityPaceDate = longestActivityPaceDate;
	}

	public Long getLongestActivityKmId() {
		return longestActivityKmId;
	}

	public void setLongestActivityKmId(Long longestActivityKmId) {
		this.longestActivityKmId = longestActivityKmId;
	}

	public Long getLongestActivityTimeId() {
		return longestActivityTimeId;
	}

	public void setLongestActivityTimeId(Long longestActivityTimeId) {
		this.longestActivityTimeId = longestActivityTimeId;
	}

	public Long getLongestActivityPaceId() {
		return longestActivityPaceId;
	}

	public void setLongestActivityPaceId(Long longestActivityPaceId) {
		this.longestActivityPaceId = longestActivityPaceId;
	}

	public Event getNextEvent() {
		return nextEvent;
	}

	public void setNextEvent(Event nextEvent) {
		this.nextEvent = nextEvent;
	}

	public Integer getStudendNumber() {
		return studendNumber;
	}

	public void setStudendNumber(Integer studendNumber) {
		this.studendNumber = studendNumber;
	}

	public void setNewMessageNumber(Long newMessageNumber) {
		this.newMessageNumber = newMessageNumber;
	}
	
	public Long getNewMessageNumber() {
		return this.newMessageNumber;
	}

	public List<Partner> getPartnerList() {
		return partnerList;
	}

	public void setPartnerList(List<Partner> partnerList) {
		this.partnerList = partnerList;
	}

	public List<TopActivityTO> getTopList() {
		return topList;
	}

	public String getTopActivitySelected() {
		return topActivitySelected;
	}

	public void setTopActivitySelected(String topActivitySelected) {
		this.topActivitySelected = topActivitySelected;
	}

	public String getTopActivityTitle() {
		return topActivityTitle;
	}
	
}