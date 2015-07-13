package br.com.runplanner.to;


public class PlanilhaListagemAlunosTO implements Comparable<PlanilhaListagemAlunosTO> {
	
	private String nome;
	private String email;
	private String rg;
	private String assessoria;
	private String turma;
	private String foneResidencial;
	private String foneCelular;
	private Integer idade;
	private String classificacao;
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRg() {
		return rg;
	}
	public void setRg(String rg) {
		this.rg = rg;
	}
	public String getAssessoria() {
		return assessoria;
	}
	public void setAssessoria(String assessoria) {
		this.assessoria = assessoria;
	}
	public String getTurma() {
		return turma;
	}
	public void setTurma(String turma) {
		this.turma = turma;
	}
	public String getFoneResidencial() {
		return foneResidencial;
	}
	public void setFoneResidencial(String foneResidencial) {
		this.foneResidencial = foneResidencial;
	}
	public String getFoneCelular() {
		return foneCelular;
	}
	public void setFoneCelular(String foneCelular) {
		this.foneCelular = foneCelular;
	}
	public Integer getIdade() {
		return idade;
	}
	public void setIdade(Integer idade) {
		this.idade = idade;
	}
	public String getClassificacao() {
		return classificacao;
	}
	public void setClassificacao(String classificacao) {
		this.classificacao = classificacao;
	}
	public int compareTo(PlanilhaListagemAlunosTO arg0) {
		int compTurma = this.turma.compareTo(arg0.getTurma());
		return  compTurma == 0 ? this.getNome().compareTo(arg0.getNome()): compTurma;
	}
}
