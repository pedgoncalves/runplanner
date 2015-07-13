package br.com.runplanner.view.util;

import java.util.ArrayList;
import java.util.List;

import br.com.runplanner.domain.Activity;

public class ActivityDisplay {
	private String month;
	private int year;
	private List<Activity> activityList;
	
	public ActivityDisplay() {
		activityList = new ArrayList<Activity>();
	}
	
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public List<Activity> getActivityList() {
		return activityList;
	}
	public void setActivityList(List<Activity> activityList) {
		this.activityList = activityList;
	}

	public int getSize() {
		return activityList.size();
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

}