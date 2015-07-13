package br.com.runplanner.domain;

public enum StatusAdvice {
	PAGANDO(0,"Está Pagando"),
	TESTANDO(1,"Está Testando"),
	GRATUITO(2,"Uso Gratuito");

	private int id;
	private String label;
	
	private StatusAdvice(int id, String label){
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
