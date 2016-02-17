package testesSelenium;

public enum Usuario {

	ADMINISTRADOR("assessoria@runplanner.com.br","adm"),
	ALUNO("barbosagrodrigo@gmail.com","161078");
	
	private String email;
	private String senha;
	
	private Usuario(String email, String senha) {
		this.email = email;
		this.senha = senha;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getSenha() {
		return senha;
	}
	
}
