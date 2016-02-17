
package testesSelenium;

import arquitetura.ItensMenu;
import arquitetura.Massa;
import arquitetura.SeleniumBaseTest;

public class Assessoria {
	
	protected static ItensMenu item = ItensMenu.ASSESSORIA;
	
	static void before(){
		SeleniumBaseTest.waitPorId(item.getURLItem());
	} 
	
	public static void adicionarDadosPerfilAssessoria(Massa massa){
		
		before();
		SeleniumBaseTest.selecionarItemMenu(item);
		SeleniumBaseTest.clicarLinkPorIdJS("mPerfil");
		SeleniumBaseTest.wait(SeleniumBaseTest.WAIT_SHORT);
		SeleniumBaseTest.escreverPorId("inNome", massa.getString("nome"));
		SeleniumBaseTest.escreverPorId("inMensalidade", massa.getString("mensalidade"));
		SeleniumBaseTest.selecionarComboPorIdPrimeJS("inDiaPagamento", massa.getString("diaPagamento"));
		if (massa.getBoolean("atrasoMensagem")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chAtrasoMensagem");
		if (massa.getBoolean("atrasoEmail"))SeleniumBaseTest.clicarCheckPorIdPrimeJS("chAtrasoEmail");
		SeleniumBaseTest.clicarBotaoPorIdJS("btSalvar");
	}
	
	public static void adicionarTurma(Massa massa){

		before();
		SeleniumBaseTest.selecionarItemMenu(item);
		SeleniumBaseTest.clicarLinkPorIdJS("mTurmas");
		SeleniumBaseTest.wait(SeleniumBaseTest.WAIT_SHORT);
		SeleniumBaseTest.clicarBotaoPorIdJS("btIncluir");
		SeleniumBaseTest.escreverPorId("inLocal", massa.getString("local"));
		SeleniumBaseTest.escreverPorId("inHorario", massa.getString("horario"));
		SeleniumBaseTest.escreverPorId("inDescricao", massa.getString("descricao"));
		SeleniumBaseTest.clicarDuploListBoxPorIdPrimeJS("first");
		if(massa.getBoolean("diaSegunda")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chSegunda");
		if(massa.getBoolean("diaTerca")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chTerca");
		if(massa.getBoolean("diaQuarta")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chQuarta");
		if(massa.getBoolean("diaQuinta")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chQuinta");
		if(massa.getBoolean("diaSexta")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chSexta");
		if(massa.getBoolean("diaSabado")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chSabado");
		if(massa.getBoolean("diaDomingo")) SeleniumBaseTest.clicarCheckPorIdPrimeJS("chDomingo");
		SeleniumBaseTest.clicarBotaoPorIdJS("btSalvar");
		
	}
	
}
