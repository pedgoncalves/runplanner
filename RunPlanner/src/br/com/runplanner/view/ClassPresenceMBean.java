package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.PessoaPresenceReport;
import br.com.runplanner.domain.Presence;
import br.com.runplanner.domain.Team;
import br.com.runplanner.domain.TipoPresenca;
import br.com.runplanner.domain.WeekDays;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.PresenceService;
import br.com.runplanner.service.ReportService;
import br.com.runplanner.service.ReportServiceImpl;
import br.com.runplanner.service.TeamService;
import br.com.runplanner.view.util.Constants;

@Component("classPresenceMBean")
@ManagedBean
public class ClassPresenceMBean extends BasicMBean {
	private static final String PRESENCE_FORM_PAGE = "/pages/presence/presence";

	private TeamService teamService;
	private PresenceService presenceService;
	private ReportService reportService;

	private List<Team> teamList = new ArrayList<Team>();

	private Team selectedTeam;

	private Integer selectedMonth;
	private Integer selectedYear;
	private int selectedWeek;

	private String selectedWeekText;
	private List<SelectItem> weekList;

	private String[] weekDate = new String[7];
	private Integer activeIndex = new Integer(0);

	@Autowired
	public ClassPresenceMBean(TeamService teamService,
			PresenceService presenceService, ReportService reportService) {
		this.teamService = teamService;
		this.presenceService = presenceService;
		this.reportService = reportService;
	}

	@Override
	public String goToList() {
		Pessoa loged = getSessionUser();
		Long adviceId = loged.getAdvice().getId();
		teamList = teamService.getByAdvice(adviceId);

		GregorianCalendar gc = new GregorianCalendar();
		this.selectedYear = gc.get(Calendar.YEAR);
		this.selectedMonth = gc.get(Calendar.MONTH);
		this.selectedWeek = gc.get(Calendar.WEEK_OF_MONTH);

		this.prepareWeekList(gc.getActualMaximum(Calendar.WEEK_OF_MONTH));

		activeIndex = new Integer(0);

		setSelectedMenu(Constants.MENU_PRESENCE);

		return PRESENCE_FORM_PAGE;
	}

	public void previous() {
		if (selectedWeek > 0) {
			selectedWeek--;
			this.preparePresenceList();
		}
	}

	public void next() {
		if (selectedWeek < weekList.size()) {
			selectedWeek++;
			this.preparePresenceList();
		}
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

	public void preparePresenceList() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		this.selectedWeekText = weekList.get(selectedWeek - 1).getLabel();
		int dia = ((selectedWeek - 1) * 7) + 1;

		GregorianCalendar gc = new GregorianCalendar(this.selectedYear,
				this.selectedMonth, dia);

		int diaMes = gc.get(Calendar.DAY_OF_MONTH);
		int diaSemana = gc.get(Calendar.DAY_OF_WEEK);

		for (int i = 0; i < 7; i++) {
			GregorianCalendar aux = new GregorianCalendar(
					gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), diaMes
							- ((diaSemana - 1) - i));
			weekDate[i] = sdf.format(aux.getTime());
		}
		this.populatePresence();
	}

	private void populatePresence() {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		for (int x = 0; x < teamList.size(); x++) {
			Team team = teamList.get(x);
			for (int y = 0; y < team.getCustomers().size(); y++) {
				Pessoa aluno = team.getCustomers().get(y);
				if (aluno.isActive()) {
					this.setPresencesFalse(aluno);
					List<Presence> presences = presenceService.findByAlunoDias(
							aluno.getId(), weekDate[0], weekDate[6]);
					Presence pre = new Presence();
					pre.setCustomer(aluno);

					for (int i = 0; i <= 6; i++) {
						try {
							pre.setDtPresence(sdf.parse(weekDate[i]));
						} catch (ParseException e) {
							e.printStackTrace();
						}

						if (presences.contains(pre)) {
							switch (i) {
							case 0:
								aluno.getWeekPresences().setSunday(true);
								break;
							case 1:
								aluno.getWeekPresences().setMonday(true);
								break;
							case 2:
								aluno.getWeekPresences().setTuesday(true);
								break;
							case 3:
								aluno.getWeekPresences().setWednesday(true);
								break;
							case 4:
								aluno.getWeekPresences().setThursday(true);
								break;
							case 5:
								aluno.getWeekPresences().setFriday(true);
								break;
							case 6:
								aluno.getWeekPresences().setSaturday(true);
								break;
							}
						}
					}
				} else {
					team.getCustomers().remove(aluno);
					y--;
				}
			}
		}
	}

	public void prepareFirstWeekList() {
		GregorianCalendar gc = new GregorianCalendar(this.selectedYear,
				this.selectedMonth, 1);
		this.selectedWeek = 1;
		// this.preparePresenceList();
		this.prepareWeekList(gc.getActualMaximum(Calendar.WEEK_OF_MONTH));
	}

	private void prepareWeekList(int numWeeks) {
		weekList = new ArrayList<SelectItem>();
		for (int i = 1; i <= numWeeks; i++) {
			weekList.add(new SelectItem(i, i + "a. Semana"));
		}
		this.preparePresenceList();
	}

	private void setPresencesFalse(Pessoa aluno) {
		aluno.getWeekPresences().setSunday(false);
		aluno.getWeekPresences().setMonday(false);
		aluno.getWeekPresences().setTuesday(false);
		aluno.getWeekPresences().setWednesday(false);
		aluno.getWeekPresences().setThursday(false);
		aluno.getWeekPresences().setFriday(false);
		aluno.getWeekPresences().setSaturday(false);
	}

	public void goToReport() {
		Pessoa loged = getSessionUser();
		boolean usarLogoRunPlanner;

		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("ASSESSORIA", loged.getAdvice().getName());
		param.put("YEAR", selectedYear.intValue());
		param.put("MONTH", selectedMonth.intValue());
		param.put(ReportServiceImpl.NOME_ARQUIVO, "Lista de presenca "+loged.getAdvice().getName());
		
		if (loged.getAdvice() != null && loged.getAdvice().getLogo() != null) {
			param.put("LOGO", new ByteArrayInputStream(loged.getAdvice()
					.getLogo()));
			usarLogoRunPlanner = false;
		} else {
			usarLogoRunPlanner = true;
		}

		List<PessoaPresenceReport> pprList = new ArrayList<PessoaPresenceReport>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		SimpleDateFormat sdfDMA = new SimpleDateFormat("dd/MM/yyyy");
		GregorianCalendar aux;
		GregorianCalendar gc = new GregorianCalendar(this.selectedYear,
				this.selectedMonth, 1);

		int primeiroDia = gc.getActualMinimum(Calendar.DAY_OF_MONTH);
		aux = new GregorianCalendar(gc.get(Calendar.YEAR),
				gc.get(Calendar.MONTH), primeiroDia);
		String primeiraData = sdfDMA.format(aux.getTime());

		int ultimoDia = gc.getActualMaximum(Calendar.DAY_OF_MONTH);
		aux = new GregorianCalendar(gc.get(Calendar.YEAR),
				gc.get(Calendar.MONTH), ultimoDia);
		String ultimaData = sdfDMA.format(aux.getTime());

		for (Team team : teamList) {
			for (Pessoa aluno : team.getCustomers()) {
				if (aluno.isActive()) {
					double cont = 0;
					PessoaPresenceReport ppr = new PessoaPresenceReport();
					ppr.setCustomerName(aluno.getNome());
					ppr.setTeamName(team.getPlace());
					ArrayList<String> dias = new ArrayList<String>();
					ArrayList<String> allFalts = this.allFalt();
					List<Presence> presences = presenceService.findByAlunoDias(
							aluno.getId(), primeiraData, ultimaData);

					for (int i = 0; i < 31; i++) {
						for (Presence presence : presences) {
							if ((i + 1) == Integer.parseInt(sdf.format(presence
									.getDtPresence()))) {
								dias.add(i, "X");
								cont++;
								break;
							} else {
								dias.add(i, " ");
							}
						}
					}
					if (dias.size() != 0) {
						ppr.setDays(dias);
					} else {
						ppr.setDays(allFalts);
					}
					ppr.setQtdClasses(qtdTeamClass(team.getDays()));

					ppr.setPresencePercent(cont / ppr.getQtdClasses());
					pprList.add(ppr);
				}
			}
		}

		reportService.gerar(ReportServiceImpl.PLANILHA_PRESENCA_TURMA_REPORT,
				param, pprList, usarLogoRunPlanner);
	}

	private int qtdTeamClass(WeekDays teamWeekDays) {
		int result = 0;
		if (teamWeekDays.getSunday()) {
			result++;
		}
		if (teamWeekDays.getMonday()) {
			result++;
		}
		if (teamWeekDays.getTuesday()) {
			result++;
		}
		if (teamWeekDays.getWednesday()) {
			result++;
		}
		if (teamWeekDays.getThursday()) {
			result++;
		}
		if (teamWeekDays.getFriday()) {
			result++;
		}
		if (teamWeekDays.getSaturday()) {
			result++;
		}
		return result * weekList.size();
	}

	private ArrayList<String> allFalt() {
		ArrayList<String> dias = new ArrayList<String>();
		for (int i = 0; i < 31; i++) {
			dias.add(i, " ");
		}
		return dias;
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

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		for (Team team : teamList) {
			for (Pessoa aluno : team.getCustomers()) {
				List<Presence> presences = presenceService.findByAlunoDias(
						aluno.getId(), weekDate[0], weekDate[6]);
				for (int i = 0; i <= 6; i++) {
					try {
						Presence aux = new Presence();
						aux.setCustomer(aluno);
						aux.setDtPresence(sdf.parse(weekDate[i]));

						switch (i) {
						case 0:
							if (aluno.getWeekPresences().getSunday()) {
								if (!presences.contains(aux)) {
									Presence pre = new Presence();
									pre.setCustomer(aluno);
									pre.setDtPresence(sdf.parse(weekDate[i]));
									pre.setTipoPresenca(team.getDays()
											.getSunday() ? TipoPresenca.NORMAL
											: TipoPresenca.EXTRA);
									presenceService.persist(pre);
								}
							} else {
								if (presences.contains(aux)) {
									Presence pre = presenceService
											.findByAlunoDia(aluno.getId(),
													weekDate[i]);
									presenceService.deleteById(pre.getId());
								}
							}
							break;
						case 1:
							if (aluno.getWeekPresences().getMonday()) {
								if (!presences.contains(aux)) {
									Presence pre = new Presence();
									pre.setCustomer(aluno);
									pre.setDtPresence(sdf.parse(weekDate[i]));
									pre.setTipoPresenca(team.getDays()
											.getMonday() ? TipoPresenca.NORMAL
											: TipoPresenca.EXTRA);
									presenceService.persist(pre);
								}
							} else {
								if (presences.contains(aux)) {
									Presence pre = presenceService
											.findByAlunoDia(aluno.getId(),
													weekDate[i]);
									presenceService.deleteById(pre.getId());
								}
							}
							break;
						case 2:
							if (aluno.getWeekPresences().getTuesday()) {
								if (!presences.contains(aux)) {
									Presence pre = new Presence();
									pre.setCustomer(aluno);
									pre.setDtPresence(sdf.parse(weekDate[i]));
									pre.setTipoPresenca(team.getDays()
											.getTuesday() ? TipoPresenca.NORMAL
											: TipoPresenca.EXTRA);
									presenceService.persist(pre);
								}
							} else {
								if (presences.contains(aux)) {
									Presence pre = presenceService
											.findByAlunoDia(aluno.getId(),
													weekDate[i]);
									presenceService.deleteById(pre.getId());
								}
							}
							break;
						case 3:
							if (aluno.getWeekPresences().getWednesday()) {
								if (!presences.contains(aux)) {
									Presence pre = new Presence();
									pre.setCustomer(aluno);
									pre.setDtPresence(sdf.parse(weekDate[i]));
									pre.setTipoPresenca(team.getDays()
											.getWednesday() ? TipoPresenca.NORMAL
											: TipoPresenca.EXTRA);
									presenceService.persist(pre);
								}
							} else {
								if (presences.contains(aux)) {
									Presence pre = presenceService
											.findByAlunoDia(aluno.getId(),
													weekDate[i]);
									presenceService.deleteById(pre.getId());
								}
							}
							break;
						case 4:
							if (aluno.getWeekPresences().getThursday()) {
								if (!presences.contains(aux)) {
									Presence pre = new Presence();
									pre.setCustomer(aluno);
									pre.setDtPresence(sdf.parse(weekDate[i]));
									pre.setTipoPresenca(team.getDays()
											.getThursday() ? TipoPresenca.NORMAL
											: TipoPresenca.EXTRA);
									presenceService.persist(pre);
								}
							} else {
								if (presences.contains(aux)) {
									Presence pre = presenceService
											.findByAlunoDia(aluno.getId(),
													weekDate[i]);
									presenceService.deleteById(pre.getId());
								}
							}
							break;
						case 5:
							if (aluno.getWeekPresences().getFriday()) {
								if (!presences.contains(aux)) {
									Presence pre = new Presence();
									pre.setCustomer(aluno);
									pre.setDtPresence(sdf.parse(weekDate[i]));
									pre.setTipoPresenca(team.getDays()
											.getFriday() ? TipoPresenca.NORMAL
											: TipoPresenca.EXTRA);
									presenceService.persist(pre);
								}
							} else {
								if (presences.contains(aux)) {
									Presence pre = presenceService
											.findByAlunoDia(aluno.getId(),
													weekDate[i]);
									presenceService.deleteById(pre.getId());
								}
							}
							break;
						case 6:
							if (aluno.getWeekPresences().getSaturday()) {
								if (!presences.contains(aux)) {
									Presence pre = new Presence();
									pre.setCustomer(aluno);
									pre.setDtPresence(sdf.parse(weekDate[i]));
									pre.setTipoPresenca(team.getDays()
											.getSaturday() ? TipoPresenca.NORMAL
											: TipoPresenca.EXTRA);
									presenceService.persist(pre);
								}
							} else {
								if (presences.contains(aux)) {
									Presence pre = presenceService
											.findByAlunoDia(aluno.getId(),
													weekDate[i]);
									presenceService.deleteById(pre.getId());
								}
							}
							break;
						}
					} catch (EntityNotFoundException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.presence.save.ok");
		return null;
	}

	public Integer getSelectedMonth() {
		return selectedMonth;
	}

	public void setSelectedMonth(Integer selectedMonth) {
		this.selectedMonth = selectedMonth;
	}

	public TeamService getTeamService() {
		return teamService;
	}

	public void setTeamService(TeamService teamService) {
		this.teamService = teamService;
	}

	public List<Team> getTeamList() {
		return teamList;
	}

	public void setTeamList(List<Team> teamList) {
		this.teamList = teamList;
	}

	public Team getSelectedTeam() {
		return selectedTeam;
	}

	public void setSelectedTeam(Team selectedTeam) {
		this.selectedTeam = selectedTeam;
	}

	public PresenceService getPresenceService() {
		return presenceService;
	}

	public void setPresenceService(PresenceService presenceService) {
		this.presenceService = presenceService;
	}

	public String[] getWeekDate() {
		return weekDate;
	}

	public void setWeekDate(String[] weekDate) {
		this.weekDate = weekDate;
	}

	public int getSelectedWeek() {
		return selectedWeek;
	}

	public void setSelectedWeek(int selectedWeek) {
		this.selectedWeek = selectedWeek;
	}

	// Util Month List to Filter Combobox
	public List<SelectItem> getMonthList() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		String months[] = new DateFormatSymbols(new Locale("pt", "BR"))
				.getMonths();
		for (int i = 0; i < 12; i++) {
			result.add(new SelectItem(i, months[i]));
		}
		return result;
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

	public List<SelectItem> getWeekList() {
		return weekList;
	}

	public void setWeekList(List<SelectItem> weekList) {
		this.weekList = weekList;
	}

	public int getSelectedYear() {
		return selectedYear;
	}

	public void setSelectedYear(int selectedYear) {
		this.selectedYear = selectedYear;
	}

	public ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	public String getSelectedWeekText() {
		return selectedWeekText;
	}

	public void setSelectedWeekText(String selectedWeekText) {
		this.selectedWeekText = selectedWeekText;
	}

	public void setSelectedYear(Integer selectedYear) {
		this.selectedYear = selectedYear;
	}

	public Integer getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(Integer activeIndex) {
		this.activeIndex = activeIndex;
	}

}
