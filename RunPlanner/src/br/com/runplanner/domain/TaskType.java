package br.com.runplanner.domain;

public enum TaskType {

	VencimentoMensalidadeAluno(0, "Vencimento da mensalidade do Aluno");
	
	private int id;
	private String label;
	
	private TaskType(int id, String label) {
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