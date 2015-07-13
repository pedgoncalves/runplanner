package br.com.runplanner.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;

import org.springframework.stereotype.Service;

import br.com.runplanner.util.JasperReportUtil;

@Service("reportService")
public class ReportServiceImpl extends ConnectionServiceMySqlImpl implements
		ReportService {
	public final static int PLANILHA_REPORT = 1;
	public final static int PLANILHA_TURMA_REPORT = 2;
	public final static int PLANILHA_PRESENCA_TURMA_REPORT = 3;
	public final static int PLANILHA_INSCRICAO_EVENTO_REPORT = 4;
	public final static int PLANILHA_LISTA_ALUNOS_REPORT = 5;
	public final static int PLANILHA_LISTA_RITMOS_REPORT = 6;
	public final static int TEMPLATES_ADVICE_MES_REPORT = 7;
	public final static int TEMPLATE_ADVICE_REPORT = 8;
	public final static int PLANILHA_PAGAMENTO_TURMA_REPORT = 9;
	public final static int PLANILHA_PAGAMENTO_TURMA_DETALHE_REPORT = 10;
	public final static int PLANILHA_ALUNO_MES_LISTAGEM = 11;
	public final static int PLANILHA_TURMA_LISTAGEM = 12;
	public final static int TEMPLATES_ADVICE_MES_REPORT_LISTAGEM = 13;
	public final static int TIMELESS_TEMPLATES_ADVICE_REPORT_LISTAGEM = 14;
	public static final String NOME_ARQUIVO = "NOME_ARQUIVO";
	
	private String imagemRodape = "images"+File.separator+"logo_pb.png";
	private String timbrado = "images"+File.separator+"timbradoRUN.jpg";
	private String timbradoPaisagem = "images"+File.separator+"timbradoRUNPaisagem.jpg";
	private String logo = "images"+File.separator+"logo2.png";
	private String planilhaReport = "relatorios"+File.separator+"planilhaAlunoMes.jasper";
	private String planilhaReportListagem = "relatorios"+File.separator+"planilhaAlunoMesListagem.jasper";
	private String planilhaTurmaReport = "relatorios"+File.separator+"planilhaTurmaMes.jasper";
	private String planilhaPresencaTurmaReport = "relatorios"+File.separator+"planilhaPresencaTurma.jasper";
	private String planilhaInscricaoEventoReport = "relatorios"+File.separator+"planilhaAlunosInscritosEvento.jasper";
	private String planilhaListaAlunosReport = "relatorios"+File.separator+"planilhaListagemAlunos.jasper";
	private String planilhaListaRitmosReport = "relatorios"+File.separator+"planilhaListagemRitmos.jasper";
	private String templatesAdviceMesReport = "relatorios"+File.separator+"templatesAdviceMes.jasper";
	private String templatesAdviceMesReportListagem = "relatorios"+File.separator+"templatesAdviceMesListagem.jasper";
	private String templateAdviceReportListagem = "relatorios"+File.separator+"templateAdviceListagem.jasper";
	private String planilhaPagamentoTurmaReport = "relatorios"+File.separator+"planilhaPagamentoTurma.jasper";
	private String planilhaPagamentoTurmaDetalheReport = "relatorios"+File.separator+"planilhaPagamentoTurmaDetalhe.jasper";
	private String planilhaTurmaReportListagem = "relatorios"+File.separator+"planilhaTurmaMesListagem.jasper";
	private String planilhaTimelessReportListagem = "relatorios"+File.separator+"templateTimelessAdviceListagem.jasper";
	
	private Connection conn;

	@SuppressWarnings("rawtypes")
	public boolean gerar(int tipoRelatorio, Map<String, Object> param, List beanList, boolean usarLogoRunPlanner) {
		try {
			if (usarLogoRunPlanner) {
				param.put("LOGO", new FileInputStream(JasperReportUtil.getRealPath() + logo));
			}			
			return gerarPlanilha(tipoRelatorio, param, beanList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean gerarPlanilha(int tipoRelatorio, Map<String, Object> param,
			List beanList) {
		Locale locale = new Locale("pt", "BR");
		param.put(JRParameter.REPORT_LOCALE, locale);

		conn = getConnection();
		
		String nomeArquivo = recuperaNomeArquivo(param);
		
		switch (tipoRelatorio) {
		case PLANILHA_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaReport, param, beanList,
					nomeArquivo, conn);
		case PLANILHA_ALUNO_MES_LISTAGEM:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaReportListagem, param, beanList,
					nomeArquivo, conn);				
		case PLANILHA_TURMA_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaTurmaReport, param, beanList,
					nomeArquivo, conn);
		case PLANILHA_TURMA_LISTAGEM:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaTurmaReportListagem, param, beanList,
					nomeArquivo, conn);
		case PLANILHA_PRESENCA_TURMA_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbradoPaisagem);
			return JasperReportUtil.gerarPDF(planilhaPresencaTurmaReport, param, beanList,
					nomeArquivo, conn);
		case PLANILHA_INSCRICAO_EVENTO_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaInscricaoEventoReport, param, beanList,
					nomeArquivo, conn);				
		case PLANILHA_LISTA_ALUNOS_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaListaAlunosReport, param, beanList,
					nomeArquivo, conn);
		case PLANILHA_LISTA_RITMOS_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaListaRitmosReport, param, beanList,
					nomeArquivo, conn);
			
		case TEMPLATES_ADVICE_MES_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(templatesAdviceMesReport, param, beanList,
					nomeArquivo, conn);
		case TEMPLATES_ADVICE_MES_REPORT_LISTAGEM:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(templatesAdviceMesReportListagem, param, beanList,
					nomeArquivo, conn);
			
		case TIMELESS_TEMPLATES_ADVICE_REPORT_LISTAGEM:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(planilhaTimelessReportListagem, param, beanList,
					nomeArquivo, conn);
			
		case TEMPLATE_ADVICE_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			return JasperReportUtil.gerarPDF(templateAdviceReportListagem, param, beanList,
					nomeArquivo, conn);
			
		case PLANILHA_PAGAMENTO_TURMA_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbradoPaisagem);
			return JasperReportUtil.gerarPDF(planilhaPagamentoTurmaReport, param, beanList,
					nomeArquivo, conn);
		case PLANILHA_PAGAMENTO_TURMA_DETALHE_REPORT:
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbradoPaisagem);
			return JasperReportUtil.gerarPDF(planilhaPagamentoTurmaDetalheReport, param, beanList,
					nomeArquivo, conn);
		default:
			return false;
		}
	}

	private String recuperaNomeArquivo(Map<String, Object> param) {
		String nomeArquivo = (String)param.get(NOME_ARQUIVO);
		if (nomeArquivo == null || nomeArquivo.equals("")) {
			nomeArquivo = "relatorioRunPlanner";
		}
		return nomeArquivo;
	}
	
	public byte[] getSpreadsheet(Map<String, Object> param) {

    	byte[] result = null;
    			
		try {
			conn = getConnection();						
			Locale locale = new Locale("pt", "BR");
			param.put("IMG_RODAPE", JasperReportUtil.getRealPath() + imagemRodape);
			param.put("TIMBRADO", JasperReportUtil.getRealPath() + timbrado);
			param.put(JRParameter.REPORT_LOCALE, locale);
			
			String nomeArquivo = recuperaNomeArquivo(param);
			
			result = JasperReportUtil.getPDF(planilhaReportListagem, param, nomeArquivo, conn);
		} 
		finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
}
