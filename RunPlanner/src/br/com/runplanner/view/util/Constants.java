package br.com.runplanner.view.util;

import java.io.File;

public class Constants {
	
	//Payment
	public static final String PAGSEGURO_EMAIL = "pedgoncalves@gmail.com";
	public static final String PAGSEGURO_TOKEN = "222E080F35A042948B4EEFAC5A4E8239";
	
	//PHOTO
	public static final long PHOTO_MAX_SIZE = 1024000; //1 mega
	public static final long BANNER_MAX_SIZE = 1024000; //1 mega
	public static final String PHOTO_PATH = System.getProperty("user.home")+File.separator+"userImage/"; 
	public static final String BANNER_PATH = System.getProperty("user.home")+File.separator+"partnerBanners/";
	public static final String PHOTO_USER_NAME = "USER";
	public static final String PHOTO_ADVICE_NAME = "ADVICE"; 
	public static final String PHOTO_ADVICE_BANNER_NAME = "ADVICEBANNER";
	public static final String[] PHOTO_TYPES = {".jpg",".jpeg",".png",".gif"};
	public static final String DEFAULT_PHOTO = File.separator + "images" + File.separator+ "runnerico.jpg";
	public static final String DEFAULT_BANNER_ADVICE = File.separator + "images" + File.separator+ "defaultBanner.jpg";

	//MENU
	public static final String SELECTED_MENU = "selectedMenu";
	public static final String MENU_NONE = "none";
	public static final String MENU_CUSTOMER = "customer";
	public static final String MENU_TEACHER = "teacher";
	public static final String MENU_TEAM = "team";
	public static final String MENU_EVENT = "event";
	public static final String MENU_PAYMENT = "payment";
	public static final String MENU_PRESENCE = "presence";
	public static final String MENU_RHYTHM = "rhythm";
	public static final String MENU_TEMPLATE = "template";
	public static final String MENU_SPREADSHEET = "spread";
	public static final String MENU_ACTIVITY = "activity";
	public static final String MENU_PROFILE = "profile";
	public static final String MENU_ADVICE = "advice";
	public static final String MENU_ADVICE_OWNER = "adviceOwner";
	public static final String MENU_GRAPH_PROGRESS = "graphProgress";
	public static final String MENU_GRAPH_ACTIVITY = "graphActivity";
	public static final String MENU_COMMUNICATION = "communication";
	public static final String MENU_EQUIPMENT = "equipment";
	public static final String MENU_ROUTE = "route";
	public static final String MENU_TIMELESS_TEMPLATE="templateBase";
	
	//EMAIL	
	public static final String RUNPLANNER_EMAIL="contato@runplanner.com.br";
	
	public static final String USER_SESSION = "AUTHENTICATED_USER";
	
	public static final String EMAIL_REGISTRATION_STUDENT_TITLE = "[RunPlanner.com.br] Bem-Vindo ao RunPlanner!";
	public static final String EMAIL_REGISTRATION_TEACHER_TITLE = "[RunPlanner.com.br] Bem-Vindo ao RunPlanner!";
	public static final String EMAIL_REGISTRATION_ADVICE_TITLE = "[RunPlanner.com.br] Bem-Vindo ao RunPlanner!";
	public static final String EMAIL_SPREADSHEET_TITLE = "[RunPlanner.com.br] Nova Planilha de Treino Cadastrada!";
	public static final String EMAIL_RESTART_PASS_TITLE = "[RunPlanner.com.br] Dados de acesso";
	public static final String EMAIL_COMMENT_ACTIVITY_TITLE = "[RunPlanner.com.br] Novo comentário em sua Atividade!";
	public static final String EMAIL_COMMENT_ACTIVITY_OTHERS_TITLE = "[RunPlanner.com.br] Novo comentário na Atividade do(a) $1!";
	
	public static final String EMAIL_REGISTRATION_STUDENT_BODY = "Parabéns, você foi registrado no <b>RunPlanner.com.br</b> pela $1" +
			"<br> Para utilizar o sistema utilize os seguintes dados:" +
			"<br> Usuário: $2" +
			"<br> Senha: $3" +
			"<br><br> Você agora faz parte do sistema RunPlanner. " +
				"Um ambiente inovador e colaborativo que permitirá o seu acompanhamento nos treinos realizados. " +
				"Para começar a usar o sistema siga os passos abaixo:" +
			"<br> 1. Entre no sistema pela página da assessoria ou pelo endereço <a href='www.runplanner.com.br'>http://www.runplanner.com.br</a>;" +
			"<br> 2. Complete os dados do seu perfil e altere sua foto para que você possa ser identificado pelo seus colegas;" +
			"<br> 3. Acesse sua planilha de treinos;" +
			"<br> 4. Cadastre suas atividades realizadas, seja manualmente ou automaticamente através de seu dispositivo eletrônico (Garmin/Celular);" +
			"<br> 5. Acompanhe seus treinos e dos seus colegas e faça comentários à vontade;" +
			"<br> 6. Analise seus resultados e progresso via gráficos." +
			"<br> A equipe <a href='www.runplanner.com.br'>RunPlanner</a> lhe deseja bons treinos!" +
			"<br><br><font color='#ccc'>--</font> " +
			"<br><font color='#ccc'>Equipe RunPlanner</font>";
	
	public static final String EMAIL_SPREADSHEET = "Uma nova Planilha de Treino foi cadastrada para você no <b>RunPlanner</b> pela $1" +
			"<br> A sua planilha segue em anexo, mas você também pode consulta-la através do <a href='www.runplanner.com.br'>RunPlanner.com.br</a>" +
			"<br> Consulte sempre sua planilha!" +
			"<br> A equipe <a href='www.runplanner.com.br'>RunPlanner</a> lhe deseja bons treinos!" +
			"<br><br><font color='#ccc'>--</font> " +
			"<br><font color='#ccc'>Equipe RunPlanner</font>";
	
	public static final String EMAIL_REGISTRATION_TEACHER_BODY = "Parabéns, você foi registrado no <b>RunPlanner.com.br</b> pela $1" +
			"<br> Para utilizar o sistema utilize os seguintes dados:" +
			"<br> Usuário: $2" +
			"<br> Senha: $3" +
			"<br><br> Você agora faz parte do sistema RunPlanner. " +
				"Um ambiente inovador e colaborativo que permitirá o completo acompanhamento dos treinos dos seus alunos. " +
				"Para começar a usar o sistema siga os passos abaixo:" +
			"<br> 1. Entre no sistema pela página da assessoria ou pelo endereço <a href='www.runplanner.com.br'>http://www.runplanner.com.br</a>;" +
			"<br> 2. Complete os dados do seu perfil e altere sua foto para que você possa ser identificado pelo seus colegas;" +
			"<br> 3. Crie as Planilhas de treinos para os seus alunos;" +
			"<br> 4. Acompanhe os treinos realizadas pelo seus alunos e faça comentários à vontade;" +			
			"<br> 5. Analise os treinos e progresso dos alunos via gráficos." +
			"<br> Bem-vindo ao <a href='www.runplanner.com.br'>RunPlanner</a>!" +
			"<br><br><font color='#ccc'>--</font> " +
			"<br><font color='#ccc'>Equipe RunPlanner</font>";
	
	public static final String EMAIL_REGISTRATION_ADVICE = "Parabéns, você foi registrado no <b>RunPlanner.com.br</b>!!!" +
			"<br> Para utilizar o sistema utilize os seguintes dados:" +
			"<br> Usuário: $1" +
			"<br> Senha: $2" +
			"<br><br> Você agora faz parte do sistema RunPlanner. " +
				"Um ambiente inovador e colaborativo que permitirá o completo acompanhamento dos treinos dos seus alunos. " +
				"Para começar a usar o sistema siga os passos abaixo:" +
			"<br> 1. Entre no sistema pela página da assessoria ou pelo endereço <a href='www.runplanner.com.br'>http://www.runplanner.com.br</a>;" +
			"<br> 2. Complete os dados do seu perfil e altere sua logomarca para que ela possa ser visualizada pelos seus alunos;" +
			"<br> 4. Cadastre as categorias da assessoria com seus respectivos ritmos;" +
			"<br> 5. Cadastre os professores que trabalham em sua assessoria;" +
			"<br> 6. Cadastre as turmas existentes;" +
			"<br> 7. A partir deste momento você estará apto a utilizar as outras funcionalidades do sistema, como:" +
			"<br>    * Cadastrar os alunos da assessoria;" +
			"<br>    * Elaborar e enviar planilha de treinos dos alunos de forma rápida e intuitiva;" +
			"<br>    * Controlar pagamento das mensalidades dos alunos;" +
			"<br>    * Controlar frequência dos alunos nos treinos;" +
			"<br>    * Cadastrar eventos esportivos (corridas);" +
			"<br>    * Enviar e-mails de comunicação para os professores, alunos ou turmas;" +
			"<br>    * Acompanhar os treinos realizados pelos seus alunos, com opção de	realizar comentários;" +
			"<br>    * Acompanhar graficamente as atividades e o progresso dos alunos." +
			"<br> Bem-vindo ao <a href='www.runplanner.com.br'>RunPlanner</a>!" +
			"<br><br><font color='#ccc'>--</font> " +
			"<br><font color='#ccc'>Equipe RunPlanner</font>";
	
	public static final String EMAIL_RESTART_PASS_BODY = "Sua senha de acesso ao <b>RunPlanner.com.br</b> foi reinicializada!" +
			"<br> Para utilizar o sistema utilize os seguintes dados:" +
			"<br> Usuário: $1" +
			"<br> Senha: $2" +
			"<br> Bem-vindo ao <a href='www.runplanner.com.br'>RunPlanner</a> e bons treinos!" +
			"<br><br><font color='#ccc'>--</font> " +
			"<br><font color='#ccc'>Equipe RunPlanner</font>";
		
	public static final String EMAIL_COMMENT_ACTIVITY = "<b>$1</b> fez um novo comentário na sua atividade de <b>$2</b>: " +
			"<br><i>' $3 '</i> " +
			"<br><br><font color='#ccc'>--</font> " +
			"<br><font color='#ccc'>Equipe RunPlanner</font>";
	
	public static final String EMAIL_COMMENT_ACTIVITY_OTHERS = "<b>$1</b> também fez um comentário na atividade do(a) $2 de <b>$3</b>: " +
			"<br><i>' $4 '</i> " +
			"<br><br><font color='#ccc'>--</font> " +
			"<br><font color='#ccc'>Equipe RunPlanner</font>";	
}
