package arquitetura;

public enum ItensMenu {

	
	
	PERFIL("lkPerfil"),
	ASSESSORIA("lkAssessoria"),
	ALUNOS("lkAlunos"),
	PLANILHAS("lkPlanilhas"),
	PRESENCA("lkPresenca"),
	EVENTOS("lkEventos"),
	ATIVIDADES("lkAtividades"),
	DESEMPENHO("lkDesempenho"),
	SAIR("lkSair");
	
	private String URLItem;

	private ItensMenu(String uRLItem) {
		URLItem = uRLItem;
	}
	
	public String getURLItem(){
		return URLItem;
	}
	
	
}
