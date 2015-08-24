package testesSelenium;

import arquitetura.ItensMenu;
import arquitetura.Massa;
import arquitetura.SeleniumBaseTest;

public class PerfilPessoa{

	protected static ItensMenu item = ItensMenu.PERFIL;
	
	static void before(){
		SeleniumBaseTest.waitPorId(item.getURLItem());
	} 
		
	public static void adicionarDadosPessoais(Massa massa){
	
		before();
		SeleniumBaseTest.waitPorId(item.getURLItem());
		SeleniumBaseTest.selecionarItemMenu(item);
		SeleniumBaseTest.clicarLinkPorIdJS("tab0");
		SeleniumBaseTest.escreverPorIdJS("inNome", massa.getString("nome"));
		SeleniumBaseTest.escreverPorIdJS("inEmail", massa.getString("email"));
		SeleniumBaseTest.escreverPorIdJS("inCpf", massa.getString("cpf"));
		SeleniumBaseTest.escreverPorIdJS("inRg", massa.getString("rg"));
		SeleniumBaseTest.escreverPorId("dtNascimento", massa.getString("dtNascimento"));		
		SeleniumBaseTest.clicarRadioPorIdPrime("rdSexo:"+massa.getString("sexo"));
		SeleniumBaseTest.clicarBotaoPorIdJS("btSalvarDadosPessoais");
		
	}
	
	public static void alterarSenha(Massa massa){
		
		before();
		SeleniumBaseTest.selecionarItemMenu(item);
		SeleniumBaseTest.clicarLinkPorIdJS("tab2");
		SeleniumBaseTest.escreverPorIdJS("inSenhaAtual", massa.getString("senha"));
		SeleniumBaseTest.escreverPorIdJS("inNovaSenha", massa.getString("novaSenha"));
		SeleniumBaseTest.escreverPorIdJS("inConfirmacaoSenha", massa.getString("novaSenha"));
		SeleniumBaseTest.clicarBotaoPorIdJS("btSalvarSenha");
		
	}

}
