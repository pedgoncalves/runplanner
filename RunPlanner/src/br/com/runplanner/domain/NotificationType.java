package br.com.runplanner.domain;

public enum NotificationType {
	
	EVENT(0,"Evento"),
	EVENT_PARTICIPANT(1,"Participante de Evento"),
	ACTIVITY(2,"Atividade"),
	PAYMENT(3,"Pagamento");
	
	
	private int id;
	private String label;
	
	private NotificationType(int id, String label){
		this.id = id;
		this.label = label;
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
