package br.com.runplanner.domain;

public enum Sexo {
	MASCULINO(0,"Masculino"), 
	FEMININO(1,"Feminino");
	
	public int id;
	public String label;
	
	private Sexo(int id, String label) {
		this.id=id;
		this.label=label;
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
