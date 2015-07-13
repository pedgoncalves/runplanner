package br.com.runplanner.domain;

public enum TipoPresenca {
	
	NORMAL(0, "Normal"),
	EXTRA(1,"Extra");
	
	private int id;
	private String label;
	
	private TipoPresenca(int id, String label){
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
