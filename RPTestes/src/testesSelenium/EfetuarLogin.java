package testesSelenium;

import arquitetura.DriverFactory;
import arquitetura.SeleniumBaseTest;

public class EfetuarLogin {
	

	public static void efetuarLogin(Usuario usuario){

		SeleniumBaseTest.navegarTela(SeleniumBaseTest.URL);
		SeleniumBaseTest.escreverPorId("inUsuario",usuario.getEmail());
		SeleniumBaseTest.escreverPorId("inSenha",usuario.getSenha());
		SeleniumBaseTest.clicarBotaoPorId("btEntrar");
		
	}
	
	public static void desconectar(){
		DriverFactory.fecharBrowser();
	}
	

}
