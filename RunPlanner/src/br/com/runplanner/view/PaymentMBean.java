package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.PaymentMonthDetailReport;
import br.com.runplanner.domain.PaymentMonths;
import br.com.runplanner.domain.PaymentMonthsDetail;
import br.com.runplanner.domain.PaymentType;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.Team;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.PaymentService;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.view.util.Constants;

@Component("paymentMBean")
@Scope("session")
public class PaymentMBean extends BasicMBean {

	private static final String PAYMENT_FORM_PAGE = "/pages/payment/payment";

	private ReportService reportService;
	private TeamService teamService;
	private PaymentService paymentService;
	private PessoaService pessoaService;

	private List<Team> teamList;
	private List<PaymentMonths> paymentList;

	private String year;
	private Integer activeIndex = new Integer(0);

	@Autowired
	public PaymentMBean(TeamService teamService, PaymentService paymentService,
			ReportService reportService, PessoaService pessoaService) {
		this.teamService = teamService;
		this.paymentService = paymentService;
		this.reportService = reportService;
		this.pessoaService = pessoaService;

		SimpleDateFormat sd = new SimpleDateFormat("yyyy");
		this.year = sd.format(new Date());
	}

	@Override
	public String goToList() {
		Pessoa loged = getSessionUser();
		Long adviceId = loged.getAdvice().getId();
		teamList = teamService.getByAdvice(adviceId);

		this.year = String.valueOf(new GregorianCalendar()
				.get(GregorianCalendar.YEAR));

		// Detalhes do Pagamento
		clearDetail();
		// --

		preparePaymentList();

		activeIndex = 0;

		setSelectedMenu(Constants.MENU_CUSTOMER);

		return PAYMENT_FORM_PAGE;
	}

	public void preparePaymentList() {
		for (int i = 0; i < teamList.size(); i++) {
			for (int j = 0; j < teamList.get(i).getCustomers().size(); j++) {
				if (teamList.get(i).getCustomers().get(j).getActive()) {
					PaymentMonths pm = paymentService.findByCustomerYear(
							teamList.get(i).getCustomers().get(j).getId(),
							this.year);
					if (pm == null)
						pm = new PaymentMonths();
					teamList.get(i).getCustomers().get(j).setPaymentMonths(pm);
				} else {
					teamList.get(i).getCustomers().remove(j);
					j--;
				}
			}
		}
	}

	// Campos para detalhes do pagamento
	private int month;
	private Date paymentDateDetail = new Date();
	private Pessoa customerDetail = new Pessoa();
	private PaymentMonths paymentMontDetail;// = new PaymentMonths();
	private PaymentMonthsDetail paymentDetail = new PaymentMonthsDetail();

	public String saveNewPayment() {

		paymentDetail.setMonth(month);
		paymentDetail.setDate(paymentDateDetail);

		// Buscar PaymentMonth para atualizar
		if (paymentMontDetail == null || paymentMontDetail.getId() == null
				|| paymentMontDetail.getId().longValue() == 0) {
			// Criar um novo
			paymentMontDetail = new PaymentMonths();
			paymentMontDetail.setCustomer(customerDetail);
			paymentMontDetail.setYear(year);

			paymentMontDetail = paymentService.persist(paymentMontDetail);
		}

		paymentMontDetail = paymentService.loadById(paymentMontDetail.getId());

		paymentMontDetail.setPaid(month, true);
		List<PaymentMonthsDetail> detailList = paymentMontDetail
				.getDetailList();

		if (detailList == null) {
			detailList = new ArrayList<PaymentMonthsDetail>();
		}

		detailList.add(paymentDetail);
		paymentMontDetail.setDetailList(detailList);

		// Cascade Merge para incluir tambem os detalhes
		try {
			paymentService.update(paymentMontDetail);
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
		}
		clearDetail();

		preparePaymentList();

		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.payment.save.ok");
		return PAYMENT_FORM_PAGE;
	}

	public void updatePayment() {

		paymentDetail.setDate(paymentDateDetail);
		List<PaymentMonthsDetail> detailList = paymentMontDetail
				.getDetailList();

		for (PaymentMonthsDetail detail : detailList) {
			if (detail.getId().longValue() == paymentDetail.getId().longValue()) {
				detailList.remove(detail);
				break;
			}
		}
		detailList.add(paymentDetail);
		paymentMontDetail.setDetailList(detailList);

		// Cascade Merge para incluir tambem os detalhes
		try {
			paymentService.update(paymentMontDetail);
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
		}
		clearDetail();

		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.payment.edit.ok");

	}

	public String removePayment() {

		List<PaymentMonthsDetail> detailList = paymentMontDetail
				.getDetailList();

		for (PaymentMonthsDetail detail : detailList) {
			if (detail.getId().longValue() == paymentDetail.getId().longValue()) {
				detailList.remove(detail);
				break;
			}
		}
		;
		paymentMontDetail.setDetailList(detailList);
		paymentMontDetail.setPaid(month, false);

		// Cascade Merge para incluir tambem os detalhes
		try {
			paymentService.update(paymentMontDetail);
		} catch (EntityNotFoundException e) {
			addMessage(FacesMessage.SEVERITY_ERROR,
					"template.msg.entitynotfound.edit");
		}
		clearDetail();
		preparePaymentList();

		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.payment.remove.ok");
		return PAYMENT_FORM_PAGE;

	}

	public void getDetailData() {

		// Buscar PaymentMonth
		paymentMontDetail = paymentService.loadById(paymentMontDetail.getId());
		List<PaymentMonthsDetail> detailList = paymentMontDetail
				.getDetailList();

		for (PaymentMonthsDetail detail : detailList) {
			if (detail.getMonth() == month) {
				paymentDetail = detail;
				paymentDateDetail = paymentDetail.getDate();
				break;
			}
		}

	}

	public void prepareDetail() {
		paymentDateDetail = new Date();
		paymentDetail = new PaymentMonthsDetail();

		if (customerDetail.getId() != null) {
			customerDetail = pessoaService.loadById(customerDetail.getId());

			paymentDetail.setValue(customerDetail.getPaymentValue());
		}
	}

	public void clearDetail() {
		paymentDateDetail = new Date();
		customerDetail = new Pessoa();
		paymentMontDetail = new PaymentMonths();
		paymentDetail = new PaymentMonthsDetail();
	}

	public String getPaymentDateStringDetail() {
		return new DateFormatSymbols(new Locale("pt", "BR")).getMonths()[month]
				+ "/" + year;
	}

	public List<SelectItem> getPaymentTypeList() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		PaymentType[] paymentType = PaymentType.values();
		for (int i = 0; i < paymentType.length; i++) {
			result.add(new SelectItem(paymentType[i], paymentType[i].getLabel()));
		}
		return result;
	}

	public String getCustomerNameDetail() {
		if (customerDetail.getId() != null) {
			customerDetail = pessoaService.loadById(customerDetail.getId());
			return customerDetail.getNome();
		}
		return "";
	}

	public void onChange(TabChangeEvent event) {
		Tab newTab = event.getTab();
		String tabIndex = newTab.getId().replaceAll("tab", "");

		Long teamId = Long.valueOf(tabIndex);

		for (int i = 0; i < teamList.size(); i++) {
			Team t = teamList.get(i);

			if (t.getId().longValue() == teamId.longValue()) {
				activeIndex = i;
				break;
			}
		}
	}

	@Override
	public String goToCreate() {
		return null;
	}

	public void goToReport() {
		Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;

		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("ASSESSORIA", loged.getAdvice().getId().intValue());
		param.put("YEAR", year);
		param.put(ReportServiceImpl.NOME_ARQUIVO, "Lista de pagamentos "+loged.getAdvice().getName());
		
		if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}

		if (!reportService.gerar(
				ReportServiceImpl.PLANILHA_PAGAMENTO_TURMA_REPORT, param, null,
				usarLogoRunPlanner)) {
			addMessage(FacesMessage.SEVERITY_WARN, "template.empty.report");
		}
	}

	public void goToReportDetail() {
		Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;

		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("ASSESSORIA", loged.getAdvice().getName());
		param.put("YEAR", year);

		if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			param.put(ReportServiceImpl.NOME_ARQUIVO, "Relatorio de pagamentos "+loged.getAdvice().getName());
			usarLogoRunPlanner = false;
		} else {
			param.put(ReportServiceImpl.NOME_ARQUIVO, "Relatorio de pagamentos RunPlanner");
			usarLogoRunPlanner = true;
		}

		ArrayList<PaymentMonthDetailReport> paymentList = new ArrayList<PaymentMonthDetailReport>();
		for (Team t : teamList) {
			for (Pessoa p : t.getCustomers()) {
				if (p.isActive()) {
					PaymentMonthDetailReport payment = new PaymentMonthDetailReport();
					paymentList.add(payment);
					payment.setYear(this.year);
					payment.setAdvice(loged.getAdvice().getName());
					payment.setCustomer(p.getNome());
					payment.setTeam(t.getPlace());
					for (int i = 0; i < 12; i++) {
						switch (i) {
						case 0:
							if (p.getPaymentMonths().isJan()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setJan(new Double(0));
							}
							break;
						case 1:
							if (p.getPaymentMonths().isFeb()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setFeb(new Double(0));
							}
							break;
						case 2:
							if (p.getPaymentMonths().isMar()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setMar(new Double(0));
							}
							break;
						case 3:
							if (p.getPaymentMonths().isApr()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setApr(new Double(0));
							}
							break;
						case 4:
							if (p.getPaymentMonths().isMay()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setMay(new Double(0));
							}
							break;
						case 5:
							if (p.getPaymentMonths().isJun()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setJun(new Double(0));
							}
							break;
						case 6:
							if (p.getPaymentMonths().isJul()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setJul(new Double(0));
							}
							break;
						case 7:
							if (p.getPaymentMonths().isAug()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setAug(new Double(0));
							}
							break;
						case 8:
							if (p.getPaymentMonths().isSep()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setSep(new Double(0));
							}
							break;
						case 9:
							if (p.getPaymentMonths().isOct()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setOct(new Double(0));
							}
							break;
						case 10:
							if (p.getPaymentMonths().isNov()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setNov(new Double(0));
							}
							break;
						case 11:
							if (p.getPaymentMonths().isDez()) {
								findMonth(i, p.getPaymentMonths(), payment);
							} else {
								payment.setDez(new Double(0));
							}
							break;
						}
					}
				}
			}
		}

		reportService.gerar(
				ReportServiceImpl.PLANILHA_PAGAMENTO_TURMA_DETALHE_REPORT,
				param, paymentList, usarLogoRunPlanner);
	}

	private void findMonth(int month, PaymentMonths paymentMonths,
			PaymentMonthDetailReport paymentMonthReport) {
		boolean achou = false;
		for (PaymentMonthsDetail pm : paymentMonths.getDetailList()) {
			if (pm.getMonth() == month) {
				achou = true;
				switch (month) {
				case 0:
					paymentMonthReport.setJan(pm.getValue());
					paymentMonthReport.setTpJan(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtJan(pm.getDate());
					break;
				case 1:
					paymentMonthReport.setFeb(pm.getValue());
					paymentMonthReport.setTpFeb(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtFeb(pm.getDate());
					break;
				case 2:
					paymentMonthReport.setMar(pm.getValue());
					paymentMonthReport.setTpMar(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtMar(pm.getDate());
					break;
				case 3:
					paymentMonthReport.setApr(pm.getValue());
					paymentMonthReport.setTpApr(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtApr(pm.getDate());
					break;
				case 4:
					paymentMonthReport.setMay(pm.getValue());
					paymentMonthReport.setTpMay(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtMay(pm.getDate());
					break;
				case 5:
					paymentMonthReport.setJun(pm.getValue());
					paymentMonthReport.setTpJun(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtJun(pm.getDate());
					break;
				case 6:
					paymentMonthReport.setJul(pm.getValue());
					paymentMonthReport.setTpJul(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtJul(pm.getDate());
					break;
				case 7:
					paymentMonthReport.setAug(pm.getValue());
					paymentMonthReport.setTpAug(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtAug(pm.getDate());
					break;
				case 8:
					paymentMonthReport.setSep(pm.getValue());
					paymentMonthReport.setTpSep(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtSep(pm.getDate());
					break;
				case 9:
					paymentMonthReport.setOct(pm.getValue());
					paymentMonthReport.setTpOct(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtOct(pm.getDate());
					break;
				case 10:
					paymentMonthReport.setNov(pm.getValue());
					paymentMonthReport.setTpNov(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtNov(pm.getDate());
					break;
				case 11:
					paymentMonthReport.setDez(pm.getValue());
					paymentMonthReport.setTpDez(" ("
							+ pm.getType().getAbbreviation() + ")");
					paymentMonthReport.setDtDez(pm.getDate());
					break;
				}
			}
			if (achou)
				break;
		}
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
		for (int i = 0; i < teamList.size(); i++) {
			for (int j = 0; j < teamList.get(i).getCustomers().size(); j++) {
				PaymentMonths pm = teamList.get(i).getCustomers().get(j)
						.getPaymentMonths();
				if (pm.getId() == null) {
					pm.setCustomer(teamList.get(i).getCustomers().get(j));
					pm.setYear(year);
					paymentService.persist(pm);
				} else {
					try {
						paymentService.update(pm);
					} catch (EntityNotFoundException e) {
						addMessage(FacesMessage.SEVERITY_ERROR,
								"template.msg.entitynotfound.edit");
					}
				}
			}
		}
		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.payment.save.ok");
		return PAYMENT_FORM_PAGE;
	}

	public List<SelectItem> getYearList() {
		List<SelectItem> result = new ArrayList<SelectItem>();

		GregorianCalendar date = new GregorianCalendar();
		Integer year = date.get(GregorianCalendar.YEAR);

		year--;
		result.add(new SelectItem(year, year.toString()));
		year++;
		result.add(new SelectItem(year, year.toString()));
		year++;
		result.add(new SelectItem(year, year.toString()));

		return result;
	}

	public List<Team> getTeamList() {
		return teamList;
	}

	public void setTeamList(List<Team> teamList) {
		this.teamList = teamList;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public List<PaymentMonths> getPaymentList() {
		return paymentList;
	}

	public void setPaymentList(List<PaymentMonths> paymentList) {
		this.paymentList = paymentList;
	}

	public PaymentService getPaymentService() {
		return paymentService;
	}

	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	public int getYearNum() {
		return Integer.parseInt(year);
	}

	public void setYearNum(int yearNum) {
		this.year = String.valueOf(yearNum);
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public Pessoa getCustomerDetail() {
		return customerDetail;
	}

	public void setCustomerDetail(Pessoa customerDetail) {
		this.customerDetail = customerDetail;
	}

	public PaymentMonths getPaymentMontDetail() {
		return paymentMontDetail;
	}

	public void setPaymentMontDetail(PaymentMonths paymentMontDetail) {
		this.paymentMontDetail = paymentMontDetail;
	}

	public Date getPaymentDateDetail() {
		return paymentDateDetail;
	}

	public void setPaymentDateDetail(Date paymentDateDetail) {
		this.paymentDateDetail = paymentDateDetail;
	}

	public PaymentMonthsDetail getPaymentDetail() {
		return paymentDetail;
	}

	public void setPaymentDetail(PaymentMonthsDetail paymentDetail) {
		this.paymentDetail = paymentDetail;
	}

	public Integer getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(Integer activeIndex) {
		this.activeIndex = activeIndex;
	}

}
