package br.com.runplanner.domain;


public enum SpreadsheetType {
	
	MONTH(0,"Mensal"),
	WEEK(1,"Semanal"),
	TIMELESS(2,"Atemporal");
		
	private int id;
	private String label;
	
	private SpreadsheetType(int id, String label){
		this.id = id;
		this.label = label;
	}
	
	public static SpreadsheetType getById(int id) {
		switch (id) {
			case 0:
				return SpreadsheetType.MONTH;
			case 1:
				return SpreadsheetType.WEEK;
			case 2:
				return SpreadsheetType.TIMELESS;
			default:
				return SpreadsheetType.MONTH;
		}
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

}
