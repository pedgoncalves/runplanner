package br.com.runplanner.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.util.JRProperties;
import br.com.runplanner.view.util.MessagesResources;
 
/**
 * Classe utilitaria que reune mrtodos para geracao de relatorio JasperReport.
 * 
 * @author vinicius.souto
 * 
 */
public class JasperReportUtil {
	
	static {        
        /*
         * Ignorar fontes faltantes.
         */
        JRProperties.setProperty("net.sf.jasperreports.awt.ignore.missing.font", true);
    }
	
	public static String getRealPath(){
		// recuperando o contexto da aplicacao web
        ExternalContext eContext = ((ExternalContext) FacesContext.getCurrentInstance().getExternalContext());
		ServletContext sContext = (ServletContext) eContext.getContext();
		
        return sContext.getRealPath("/");
	}
 
    /**
     * Metodo para gerar uma saida em PDF, no qual ira solicitar para o usuario
     * salvar o arquivo gerado.
     * 
     * @param reportPath String - caminho do relatorio
     * @param parametros Map - mapa com os parametros
     * @param beanList List - lista com os dados a serem impressos na listagem
     * @param nomeDoArquivo String - nome do arquivo a ser gerado erro do sistema
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean gerarPDF(String reportPath, Map parametros,
            List beanList, String nomeDoArquivo, Connection conn) {
        
        try {
             // recupera o contexto do JSF
            FacesContext context = FacesContext.getCurrentInstance();
 
            // recuperando o contexto da aplicacao web
            ExternalContext eContext = ((ExternalContext) FacesContext
                    .getCurrentInstance().getExternalContext());
            // recuperando o response da aplicacao
            HttpServletResponse response = (HttpServletResponse) eContext
                    .getResponse();
 
            // definindo o exportador, para PDF
            JRExporter exporter = new JRPdfExporter();
            ServletContext sContext = (ServletContext) eContext.getContext();
            InputStream is = new FileInputStream(sContext.getRealPath("/")
                    + reportPath);
            // RelatorioPDF.class.getResourceAsStream( reportPath );
            
            parametros.put(JRParameter.REPORT_RESOURCE_BUNDLE, MessagesResources.getBundleApplication());
 
            JasperPrint printer;
 
            if (beanList == null) {
 
                // gera o relatorio em memoria
                printer = JasperFillManager.fillReport(
                // local que esta o arquivo do modelo do relatorio
                        is,
                        // parametros a serem enviados para o relatorio
                        parametros, 
                        // conexaco com banco de dados
                        conn);
 
            } else {
 
                // gera o relatorio em memoria
                printer = JasperFillManager.fillReport(
                // local que esta o arquivo do modelo do relatorio
                        is,
                        // parametros a serem enviados para o relatorio
                        parametros,
                        // lista de beans a serem impressos no relatorio
                        new JRBeanCollectionDataSource(beanList));
 
            }
 
            if(printer.getPages().isEmpty()) {
            	return false;
            }
            
            // lista de bytes do arquivo carregado em memoria
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
            // seta o objeto 'baos' como parametro de streamming de saida no
            // objeto 'exporter'
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            // seta o objeto 'printer' como parametro de impressao no objeto
            // 'exporter'
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, printer);
            // seta a codificacao dos caracteres como parametro de impressao no
            // objeto 'exporter'
            exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING,
                    "ISO-8859-9");
            // exporta o relatorio com a saida escolha em memoria (formato
            // Streamming)
            exporter.exportReport();
 
            // definindo o cabecalho do retornos
            response.addHeader("Content-Type", "application/force-download");
            response.addHeader("Content-Disposition", "attachment; filename="
                    + nomeDoArquivo + ".pdf");
 
            // informa o tamanho total do arquivo
            response.setContentLength(baos.size());
            // escreve o relatorio na memoria do objeto 'response'
            response.getOutputStream().write(baos.toByteArray());
            // fechando o array de bytes
            response.getOutputStream().flush();
 
            // libera a memoria
            baos.flush();
            // fecha o arquivo em memoria
            baos.close();
 
            // indica para o contexto do JSF que o response esta
            // completo/finalizado
            context.responseComplete();
 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
 
    /**
     * Metodo para gerar uma saida em RTF, no qual ira solicitar para o usuario
     * salvar o arquivo gerado.
     * 
     * @param reportPath
     *            String - caminho do relatorio
     * @param parametros
     *            Map - mapa com os parametros
     * @param beanList
     *            List - lista com os dados a serem impressos na listagem
     * @param nomeDoArquivo
     *            String - nome do arquivo a ser gerado erro do sistema
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void gerarRTF(String reportPath, Map parametros,
            List beanList, String nomeDoArquivo){
        
        try {
 
            // recupera o contexto do JSF
            FacesContext context = FacesContext.getCurrentInstance();
 
            // recuperando o contexto da aplicacao web
            ExternalContext eContext = ((ExternalContext) FacesContext
                    .getCurrentInstance().getExternalContext());
            // recuperando o response da aplicacao
            HttpServletResponse response = (HttpServletResponse) eContext
                    .getResponse();
 
            ServletContext sContext = (ServletContext) eContext.getContext();
            InputStream is = new FileInputStream(sContext.getRealPath("/")
                    + reportPath);
            // RelatorioPDF.class.getResourceAsStream( reportPath );
            
            parametros.put(JRParameter.REPORT_RESOURCE_BUNDLE, MessagesResources.getBundleApplication());
 
            JasperPrint printer;
 
            // gera o relatorio em memoria
            printer = JasperFillManager.fillReport(
            // local que esta o arquivo do modelo do relatorio
                    is,
                    // parametros a serem enviados para o relatorio
                    parametros,
                    // lista de beans a serem impressos no relatorio
                    new JRBeanCollectionDataSource(beanList));
 
            // lista de bytes do arquivo carregado em memoria
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
            JRRtfExporter rtf = new JRRtfExporter();
            rtf.setParameter(JRExporterParameter.JASPER_PRINT, printer);
            rtf.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            rtf.exportReport();
 
            // definindo o cabecalho do retornos
            response.addHeader("Content-Type", "application/force-download");
            response.addHeader("Content-Disposition", "attachment; filename="
                    + nomeDoArquivo + ".rtf");
 
            // informa o tamanho total do arquivo
            response.setContentLength(baos.size());
            // escreve o relatorio na memoria do objeto 'response'
            response.getOutputStream().write(baos.toByteArray());
            // fechando o array de bytes
            response.getOutputStream().flush();
 
            // libera a memoria
            baos.flush();
            // fecha o arquivo em memoria
            baos.close();
 
            // indica para o contexto do JSF que o response esta
            // completo/finalizado
            context.responseComplete();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * Metodo para gerar uma saida em XLS, no qual ira solicitar para o usuario
     * salvar o arquivo gerado.
     * 
     * @param reportPath
     *            String - caminho do relatorio
     * @param parametros
     *            Map - mapa com os parametros
     * @param beanList
     *            List - lista com os dados a serem impressos na listagem
     * @param nomeDoArquivo
     *            String - nome do arquivo a ser gerado erro do sistema
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void gerarXLS(String reportPath, Map parametros,
            List beanList, String nomeDoArquivo)  {
        
        try {
 
            // recupera o contexto do JSF
            FacesContext context = FacesContext.getCurrentInstance();
 
            // recuperando o contexto da aplicacao web
            ExternalContext eContext = ((ExternalContext) FacesContext
                    .getCurrentInstance().getExternalContext());
            // recuperando o response da aplicacao
            HttpServletResponse response = (HttpServletResponse) eContext
                    .getResponse();
 
            ServletContext sContext = (ServletContext) eContext.getContext();
            InputStream is = new FileInputStream(sContext.getRealPath("/")
                    + reportPath);
            // RelatorioPDF.class.getResourceAsStream( reportPath );
            
            parametros.put(JRParameter.REPORT_RESOURCE_BUNDLE, MessagesResources.getBundleApplication());
 
            JasperPrint printer;
 
            // gera o relatorio em memoria
            printer = JasperFillManager.fillReport(
            // local que esta o arquivo do modelo do relatorio
                    is,
                    // parametros a serem enviados para o relatorio
                    parametros,
                    // lista de beans a serem impressos no relatorio
                    new JRBeanCollectionDataSource(beanList));
             
 
            // lista de bytes do arquivo carregado em memoria
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
            // JRRtfExporter xls = new JRRtfExporter();
            JExcelApiExporter xls = new JExcelApiExporter();
            xls.setParameter(JExcelApiExporterParameter.JASPER_PRINT, printer);
            xls.setParameter(JExcelApiExporterParameter.OUTPUT_STREAM, baos);
            xls
                    .setParameter(
                            JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
                            Boolean.TRUE);
            xls.setParameter(
                    JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                    Boolean.TRUE);
            xls.setParameter(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET,
                    Boolean.TRUE);
            xls.exportReport();
 
            // definindo o cabecalho do retornos
            response.addHeader("Content-Type", "application/vnd.ms-excel");
            response.addHeader("Content-Disposition", "attachment; filename="
                    + nomeDoArquivo + ".xls");
            // informa o tamanho total do arquivo
            response.setContentLength(baos.size());
            // escreve o relatorio na memoria do objeto 'response'
            response.getOutputStream().write(baos.toByteArray());
            // fechando o array de bytes
            response.getOutputStream().flush();
 
            // libera a memoria
            baos.flush();
            // fecha o arquivo em memoria
            baos.close();
 
            // indica para o contexto do JSF que o response esta
            // completo/finalizado
            context.responseComplete();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static byte[] getPDF(String reportPath, Map parametros, String nomeDoArquivo, Connection conn) {
        
    	byte[] result = null;
    	
        try {
             // recupera o contexto do JSF            
            ExternalContext eContext = ((ExternalContext) FacesContext.getCurrentInstance().getExternalContext());
            ServletContext sContext = (ServletContext) eContext.getContext();
            
            parametros.put(JRParameter.REPORT_RESOURCE_BUNDLE, MessagesResources.getBundleApplication());
 
            // definindo o exportador, para PDF
            JRExporter exporter = new JRPdfExporter();
            InputStream is = new FileInputStream(sContext.getRealPath("/") + reportPath);
 
            JasperPrint printer = JasperFillManager.fillReport(is,parametros,conn);
 
            // lista de bytes do arquivo carregado em memoria
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, printer);
            exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING,"ISO-8859-9");
            // exporta o relatorio com a saida escolha em memoria (formato Streamming)
            exporter.exportReport();
 

            result = baos.toByteArray();
            
            baos.flush();            
            baos.close();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
 
} 
