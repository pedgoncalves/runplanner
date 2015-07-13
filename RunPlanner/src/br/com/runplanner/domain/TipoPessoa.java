package br.com.runplanner.domain;

public enum TipoPessoa {
	
	ADMINISTRADOR(0,"Administrador"),
	ASSESSORIA(1,"Resp. Assessoria"),
	PROFESSOR(2,"Professor"),
	ALUNO(3,"Aluno");
	
	
	private int id;
	private String label;
	
	private TipoPessoa(int id, String label){
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
