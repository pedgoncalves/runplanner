package testesSelenium;

import arquitetura.Massa;
import arquitetura.SeleniumBaseTest;


public class TesteInicial {

	public static void main(String args[]) throws InterruptedException{

		//Limpar Base
		//SeleniumBaseTest.prepararBase();

		Massa massaPessoaPerfil = new Massa("resources/massa_pessoa_perfil.properties");
		Massa massaAssessoriaPerfil = new Massa("resources/massa_assessoria_perfil.properties");
		Massa massaAssessoriaTurma = new Massa("resources/massa_assessoria_turma.properties");
		
		SeleniumBaseTest.logInfo("Iniciando testes...");
		
		try {
			// Realizar o login do sistema
			EfetuarLogin.efetuarLogin(Usuario.ADMINISTRADOR);

			//Executar passos
			
			// Perfil
			PerfilPessoa.adicionarDadosPessoais(massaPessoaPerfil);
			PerfilPessoa.alterarSenha(massaPessoaPerfil);

			// Assessoria
			Assessoria.adicionarDadosPerfilAssessoria(massaAssessoriaPerfil);
			Assessoria.adicionarTurma(massaAssessoriaTurma);  	
			
			SeleniumBaseTest.logInfo("Finalizando testes...");
			
			//Desconectar e fechar o browser
			EfetuarLogin.desconectar();
			
		} catch (Exception e) {
			// Desconectar e fechar o browser
			//EfetuarLogin.desconectar();
		}
		
	
	}
	
}
