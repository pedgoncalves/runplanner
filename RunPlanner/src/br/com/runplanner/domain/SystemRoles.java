package br.com.runplanner.domain;

public enum SystemRoles {

	ADMIN(0,"ROLE_ADM"),
	ADVICE(1,"ROLE_ASSESSOR"),
	TEACHER(2,"ROLE_TEACHER"),
	USER(3,"ROLE_USER");
	
	private Integer id;
	private String description;
	
	private SystemRoles(Integer id, String desc) {
		this.id=id;
		this.description=desc;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
