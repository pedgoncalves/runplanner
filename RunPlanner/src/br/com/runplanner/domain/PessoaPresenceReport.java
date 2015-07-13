package br.com.runplanner.domain;

import java.util.ArrayList;
import java.util.List;

public class PessoaPresenceReport {
	private String teamName;
	private String customerName;
	private List<String> days = new ArrayList<String>();
	private Integer qtdPresence;
	private Integer qtdClasses;
	private Double presencePercent;
	
	public String getTeamName() {
		return teamName;
	}
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public List<String> getDays() {
		return days;
	}
	public void setDays(List<String> days) {
		this.days = days;
	}
	public Integer getQtdPresence() {
		return qtdPresence;
	}
	public void setQtdPresence(Integer qtdPresence) {
		this.qtdPresence = qtdPresence;
	}
	public Integer getQtdClasses() {
		return qtdClasses;
	}
	public void setQtdClasses(Integer qtdClasses) {
		this.qtdClasses = qtdClasses;
	}
	public Double getPresencePercent() {
		return presencePercent;
	}
	public void setPresencePercent(Double presencePercent) {
		this.presencePercent = presencePercent;
	}

}
