package br.com.runplanner.to;

import java.util.Date;

import br.com.runplanner.util.Utils;

public class TopActivityTO {

	private Double totalDistance;
	private Double totalTime;
	private String name;
	private Long activityId;
	private Date date;
	private int position;
	
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public double getTotalDistance() {
		return totalDistance;
	}
	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}
	public String getTotalTime() {
		return Utils.formataTempo( totalTime.intValue() );
	}
	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getActivityId() {
		return activityId;
	}
	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}	
}
