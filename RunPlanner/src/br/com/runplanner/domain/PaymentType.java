package br.com.runplanner.domain;

public enum PaymentType {
	
	
	DINHEIRO (0,"Dinheiro", "DI"),
	CHEQUE (1,"Cheque", "CH"),
	CARTAO (2,"Cartão", "CA"),
	PAGSEGURO (3,"Pag Seguro", "PS"),
	OUTRO (9,"Outro", "OT");
	
	private int id;
	private String label;
	private String abbreviation;
	
	private PaymentType(int id, String label, String abbreviation){
		this.id = id;
		this.label = label;
		this.abbreviation = abbreviation;
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


	public String getAbbreviation() {
		return abbreviation;
	}


	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}
}
