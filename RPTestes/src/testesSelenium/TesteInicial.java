package testesSelenium;

import arquitetura.Massa;
import arquitetura.SeleniumBaseTest;


public class TesteInicial {

	public static void main(String args[]) throws InterruptedException{

		//Limpar Base
		//SeleniumBaseTest.prepararBase();

		Massa massaPerfilAssessoria = new Massa("resources/massa_perfil_assessoria.properties");
		
		SeleniumBaseTest.logInfo("Iniciando testes...");
		
		// Realizar o login do sistema
		EfetuarLogin.efetuarLogin(Usuario.ADMINISTRADOR);
		
					
		//Executar passos
		Perfil.adicionarDadosPessoais(massaPerfilAssessoria);
		Perfil.alterarSenha(massaPerfilAssessoria);
	
		SeleniumBaseTest.logInfo("Finalizando testes...");
		
		// Desconectar e fechar o browser
		//EfetuarLogin.desconectar();
	}
	
}
