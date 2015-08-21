package testesSelenium;

import arquitetura.ItensMenu;
import arquitetura.Massa;
import arquitetura.SeleniumBaseTest;

public class Perfil {

	
	public static void criarUsuario(Massa massa){
		
		SeleniumBaseTest.clicarBotaoPorLocator("//*[@id='extr-page-header-space']/a");
		//SeleniumBaseTest.clicarLink("Criar Conta");
		SeleniumBaseTest.escreverPorName("name",massa.getString("nome"));
		SeleniumBaseTest.escreverPorName("email",massa.getString("email"));
		SeleniumBaseTest.escreverPorName("password1",massa.getString("senha"));
		SeleniumBaseTest.escreverPorName("password2",massa.getString("confirmSenha"));
		SeleniumBaseTest.marcarCheckBoxPorIdJS("terms", massa.getBoolean("aceitarTermos"));
		SeleniumBaseTest.clicarBotaoPorLocator("//*[@id='signup']/footer/button");
	}
	
	public static void adicionarDadosPessoais(Massa massa){
		
		SeleniumBaseTest.selecionarItemMenu(ItensMenu.PERFIL);
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
		
		SeleniumBaseTest.selecionarItemMenu(ItensMenu.PERFIL);
		SeleniumBaseTest.clicarLinkPorIdJS("tab2");
		SeleniumBaseTest.escreverPorIdJS("inSenhaAtual", massa.getString("senha"));
		SeleniumBaseTest.escreverPorIdJS("inNovaSenha", massa.getString("novaSenha"));
		SeleniumBaseTest.escreverPorIdJS("inConfirmacaoSenha", massa.getString("novaSenha"));
		SeleniumBaseTest.clicarBotaoPorIdJS("btSalvarSenha");
		
	}


	
}
