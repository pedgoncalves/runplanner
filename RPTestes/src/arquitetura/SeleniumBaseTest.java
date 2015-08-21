package arquitetura;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumBaseTest {

	//public final static String URL = "http:\\ead.aulasdeprogramacao.com.br";
	public final static String URL = "http:\\localhost:8080/runplanner";
	
	
	private final static Integer TIMEOUT = 30000; 
	public final static Integer WAIT_TIME = 2000; 
	
	
	public static WebDriver getDriver(){
		return DriverFactory.getDriver();
	}
	

	public static void prepararBase(){
	}
	
	
	public static void selecionarItemMenu(ItensMenu URLItem){
		
		logInfo(URLItem.name());
		SeleniumBaseTest.clicarLinkPorIdJS(URLItem.getURLItem());		
	}
	
	public static void navegarLinks(String... links) {
		
		Actions actions = new Actions(SeleniumBaseTest.getDriver());
		for(String link: links) {
			WebElement refLink = SeleniumBaseTest.getDriver().findElement(By.xpath("//a[contains(text(),'" + link + "')]"));
			actions.moveToElement(refLink);
			actions.click();
		}
		actions.perform();
	}

	public static void navegarLinks(By... bys) {
		Actions actions = new Actions(SeleniumBaseTest.getDriver());
		for(By by: bys) {
			WebElement refLink = SeleniumBaseTest.getDriver().findElement(by);
			actions.moveToElement(refLink);
			actions.click();
		}
		actions.perform();
	}
	
	public static void clicarLinkPorLocator(By by) {
		WebElement elemento = getDriver().findElement(by);
		
		//FIXME Trecho de código para fazer o elemento aparecer... usando o "navegarLinks" não está funcionando
		JavascriptExecutor js = (JavascriptExecutor) SeleniumBaseTest.getDriver();  
		String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";  
		js.executeScript(mouseOverScript, elemento);
		
		elemento.click();
	}
	
	//Escrita em textField
	
	public static void escrever(String locator, String valor){
		WebElement campo = getDriver().findElement(By.xpath(locator));
		campo.clear();
		campo.sendKeys(valor);
	}
	
	public static void escreverPorId(String id, String valor){
		logInfo(id);
		String fullId = (String) jsExec("return $('[id$=\"" + id + "\"]').prop('id')");
		WebElement campo = getDriver().findElement(By.id(fullId));
		campo.clear();
		campo.sendKeys(valor);
	}
	
	public static void escreverPorName(String name, String valor){
		logInfo(name);
		WebElement campo = getDriver().findElement(By.name(name));
		campo.clear();
		campo.sendKeys(valor);
	}
	
	public static void escreverPorLocator(String locator, String valor){
		logInfo(locator);
		WebElement campo = getDriver().findElement(By.xpath(locator));
		campo.clear();
		campo.sendKeys(valor);
	}
	
	//Escrita em textArea
	
	public static void escreverTextareaPorId(String id, String valor){
		logInfo(id);
		escreverPorId(id, valor);
	}
	
	//Botões
	
	public static void clicarBotaoPorLocator(String locator){
		logInfo(locator);
		getDriver().findElement(By.xpath(locator)).click();
	}
	
	public static void clicarBotaoPorId(String id){
		logInfo(id);
		getDriver().findElement(By.id(id)).click();
	}
	
	public static void clicarBotaoPorIdComEspera(String id){
		while(!getDriver().findElement(By.id(id)).isDisplayed());
		getDriver().findElement(By.id(id)).click();
	}
	
	public static void clicarBotao(String texto){
		logInfo(texto);
		getDriver().findElement(By.xpath("//input[@value='" + texto + "']")).click();
	}
	
	//Links
	
	public static void clicarLinkPorLocator(String locator){
		getDriver().findElement(By.xpath(locator)).click();
	}
	
	public static void clicarLinkPorId(String locator){
		getDriver().findElement(By.id(locator)).click();
	}
	
	public static void clicarLink(String texto){
		logInfo(texto);
		getDriver().findElement(By.linkText(texto)).click();
	}
	
	//Imagens
//
//	public static void clicarImagemPorId(String locator){
//		getBrowser().<DomLink>find("//INPUT[@id='" + locator + "']").select();
//	}
	
	//Radio
		
	public static void clicarRadioPorIdPrime(String id) {
		String fullId = (String) jsExec("return $('[id$=\"" + id + "\"]').prop('id')");
		getDriver().findElement(By.cssSelector("label[for='" + fullId +"']")).click();
	}
	
	public static void clicarBotaoPorIdPrime(String id) {
//		getDriver().findElement(By.name(id)).click();
		getDriver().findElement(By.xpath("//button[@id='" + id + "']")).click();
	}
		
	public static void clicarRadioPorLocator(String locator){
		getDriver().findElement(By.xpath(locator)).click();
	}
	
	public static void clicarRadioPorId(String id){
		getDriver().findElement(By.id(id)).click();
	}
	
	public static void garantirRadioCheckedPorId(String id){
		WebElement radio = getDriver().findElement(By.id(id));
		while(!radio.isSelected()) {
			radio.click();
		}
	}
	
	public static boolean isRadioEnabled(String locator){
		return getDriver().findElement(By.xpath(locator)).isEnabled();
	}
	
	public static boolean isRadioSelected(String locator){
		return getDriver().findElement(By.xpath(locator)).isSelected();
	}
	
	// Click genérico
	
	public static void clicarGenerico(String locator){
		getDriver().findElement(By.xpath(locator)).click();
	}
	
	//Check-box	
	
	public static void checar(String locator){
		getDriver().findElement(By.xpath(locator)).click();
	}
	
	public static void clicarCheckBoxPorId(String id, boolean valor){
		WebElement elemento = getDriver().findElement(By.id(id));
		boolean selecionado = elemento.isSelected();
		if (!valor && selecionado) {
			elemento.click();
		} else if (valor && !selecionado) {
			elemento.click();
		}
	
	}
	
	public static void clicarCheckBoxPorName(String name, boolean valor){
		WebElement elemento = getDriver().findElement(By.name(name));
		boolean selecionado = elemento.isSelected();
		if (!valor && selecionado) {
			elemento.click();
		} else if (valor && !selecionado) {
			elemento.click();
		}
		
	}

	
	public static void clicarBotaoPorIdJS(String id) {
		logInfo(id);
		jsExec("$('[id$=\"" + id + "\"]').click()");
	}
	
	public static void clicarLinkPorIdJS(String id) {
		logInfo(id);
		jsExec("$('[id$=\"" + id + "\"]').click()");
	}
	
	public static void clicarCheckBoxPorTextoJS(String texto, boolean valor) {
		logInfo(texto);
		jsExec("$('[value=\"" + texto + "\"]').prop('checked', " + valor + ")");
	}
	
	public static void escreverTextAreaPorNameJS(String name, String valor) {
		logInfo(name);
		jsExec("$('[name=\""+ name +"\"]').val(\""+ valor +"\")");
	}
	
	public static void escreverPorIdJS(String name, String valor) {
		logInfo(name);
		jsExec("$('[id$=\""+ name +"\"]').val(\""+ valor +"\")");
	}
	
	public static void marcarCheckBoxPorIdJS(String id, boolean valor) {
		logInfo(id);
		jsExec("$('[id=\"" + id + "\"]').prop('checked', " + valor + ")");
	}
	
	public static void clicarCheckBoxPorIdJS(String id) {
		logInfo(id);
		jsExec("$('[id=\"" + id + "\"]').click()");
	}
	
	public static void clicarCheckBoxPorNameJS(String name, boolean valor) {
		logInfo(name);
		jsExec("$('[name=\"" + name + "\"]').prop('checked', " + valor + ")");
	}
	
	//Combo-box
	
	public static void selecionarComboPorLocator(String locator, String valor){
		Select select = new Select(getDriver().findElement(By.xpath(locator)));
		select.selectByVisibleText(valor);
	}
	
	public static void selecionarComboPorId(String locator, String valor){
		Select select = new Select(getDriver().findElement(By.id(locator)));
		select.selectByVisibleText(valor);
	}
	
	public static void selecionarComboPorName(String locator, String valor){
		logInfo(locator);
		Select select = new Select(getDriver().findElement(By.name(locator)));
		select.selectByVisibleText(valor);
	}

	public static void selecionarComboPorIdPrime(String idPrefix, String valor){
        getDriver().findElement(By.id(idPrefix + "_label")).click();
        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), 15);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='" + idPrefix + "_panel']/div/ul/li[text()='" + valor + "']")));
        getDriver().findElement(By.xpath("//div[@id='" + idPrefix + "_panel']/div/ul/li[text()='" + valor + "']")).click();
	}
	
	public static void selecionarComboComBuscaPorIdPrime(String idPrefix, String valor){
		getDriver().findElement(By.id(idPrefix + "_label")).click();
		WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), 15);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='" + idPrefix + "_panel']/div[2]/ul/li[text()='" + valor + "']")));
		getDriver().findElement(By.xpath("//div[@id='" + idPrefix + "_panel']/div[2]/ul/li[text()='" + valor + "']")).click();
	}
	
	//Buscas
	
	public static boolean existeElemento(String locator){
		try {
			return getDriver().findElement(By.xpath(locator)) != null;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public static boolean existeElemento(By by){
		try {
			return getDriver().findElement(by) != null;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	//Textos
	
	public static String obterTextoSpan(String id){
		return getDriver().findElement(By.xpath("span="+id)).getText();
	}
	
	public static String obterTextoPorLocator(String locator){
		return getDriver().findElement(By.xpath(locator)).getText();
	}
	
	public static String obterTextoPorCss(String css){
		return getDriver().findElement(By.cssSelector(css)).getText();
	}
	
	public static boolean isTextoPresente(String texto){
		return getDriver().getPageSource().contains(texto);
	}
	
	public static void verificarTextoPresente(String... textos){
		for(String texto: textos){
			Assert.assertTrue("Texto não encontrado na página: ", isTextoPresente(texto));
		}
	}
	
	//Navegação
	
	public static void navegarTela(String url){
		logInfo(url);
		getDriver().navigate().to(url);
	}
	
	// Tabela
	
	/**
	 * Busca um registro na tabela que possua os pares coluna e valor da coluna correspondente aos passados
	 * 	pelo <b>paresColunaValor</b> e clica no botão <b>textoBotão</b>
	 *
	 * É recomendado enviar no Map uma instância de LinkedHashMap para garantir que os pares serão
	 * 	buscados na ordem que os mesmos forem adicionados ao Map
	 *
	 * @author Fco. Wagner Costa Aquino (wagner.aquino@previdencia.gov.br) - UDCE
	 * @since 19/05/2009 13:29:36
	 *
	 * @param paresColunaValor map contendo os pares de colunas e valores a serem procurados na tabela
	 * @param textoBotao Nome da coluna do botão que deve ser clicado
	 * @param botaoProximo Id do botão de próximo para paginação. (Caso a tabela não possua paginação, enviar <code>null</code>
	 * @param idTabela Id da tabela a ser buscada
	 * @param headerLevel nível dos dados do cabeçalho. Nível inicial = 0
	 */
	public static void clicarBotaoTabela(String coluna, String valor, String textoBotao, String idTabela){
//		int[] coordenadas = getCoordenadas(paresColunaValor, textoBotao, idTabela);
		SeleniumTable tabela = new SeleniumTable(idTabela);
		
		//descobrir a coluna para varrer
		int numColuna = getColuna(tabela, coluna);
		
		//descobrir a linha correta
		int contador = 1;
		boolean encontrado = false;
		while(true) {
			String celula = tabela.getCelula(contador, numColuna).getText();
			if(celula.equals(valor)) {
				encontrado = true;
				break;
			}
			contador++;
		}
		
		if(!encontrado) {
			throw new RuntimeException("Valor não encontrado");
		}
		
		//descobrir a coluna do botao
		numColuna = getColuna(tabela, textoBotao);
		
		//clicar
		tabela.getCelula(contador, numColuna).click();
	}
	
	public static void clicarBotaoTabela(int linha, String textoBotao, String idTabela){
//		int[] coordenadas = getCoordenadas(paresColunaValor, textoBotao, idTabela);
		SeleniumTable tabela = new SeleniumTable(idTabela);
		
		
		//descobrir a coluna do botao
		int numColuna = getColuna(tabela, textoBotao);
		
		//clicar
		tabela.getCelula(linha, numColuna).click();
	}
	
	private static int getColuna(SeleniumTable tabela, String coluna) {
		int contador = 1;
		try { 
			while(true) {
				String nomeColuna = tabela.getCelulaCabecalho(contador).getText();
				if(nomeColuna.equals(coluna)) {
					return contador;
				}
				contador++;
			}
		} catch(NoSuchElementException ex) {
			System.err.println("Coluna: " + coluna + " não encontrada.");
		}
		return -1;
	}
	
	// Waits
	
	public static void waitForBody(int timeout){
		waitForElement("//BODY", timeout);		
	}

	public static void waitForElement(String locator, int timeout){
		int trys = Math.round(timeout / 500);
		while(trys-- > 0) {
			try {
				Thread.sleep(500);
				if(existeElemento(locator)){
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}			
	}

	public static void waitForElement(By by, int timeout){
		int trys = Math.round(timeout / 500);
		while(trys-- > 0) {
			try {
				Thread.sleep(500);
				if(existeElemento(by)){
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}			
	}
	
	public static void waitForAjax(){
		int trys = Math.round(TIMEOUT / 500);
		while(trys-- > 0) {
			try {
				Thread.sleep(500);
				if(isAjaxComplete()){
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}			
		waitTime();
	}

	
	// Waits
	public static void waitTime(){
		wait(WAIT_TIME);
	}
	
	public static void wait(int timeMili){
		try {
			Thread.sleep(timeMili);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void waitPorNome(String name) {
		waitPorNome(name, 20);
	}
	
	public static void waitPorTexto(String texto) {
		waitPorTexto(texto, 20);
	}
	
	public static void waitPorId(String id) {
		waitPorId(id, 20);
	}
	
	public static void waitPorNome(String name, long timeoutInSeconds) {
		WebDriverWait waiting = new WebDriverWait(getDriver(), timeoutInSeconds);
		waiting.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
	}

	public static void waitPorId(String id, long timeoutInSeconds) {
		WebDriverWait waiting = new WebDriverWait(getDriver(), timeoutInSeconds);
		waiting.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
	}
	
	public static void waitPorTexto(String texto, long timeoutInSeconds) {
		WebDriverWait waiting = new WebDriverWait(getDriver(), timeoutInSeconds);
		waiting.until(ExpectedConditions.presenceOfElementLocated(By.linkText(texto)));
	}
	
	public static void waitPorClasse(String className, long timeout) {
		WebDriverWait waiting = new WebDriverWait(getDriver(), timeout);
		waiting.until(ExpectedConditions.presenceOfElementLocated(By.className(className)));
	}
	
	public static boolean isAjaxComplete() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
	    Boolean ajaxIsComplete = (Boolean) jse.executeScript("return PrimeFaces.ajax.Queue.isEmpty();");
	    return ajaxIsComplete;
	}

	//Logs
	
	public static void logInfo(String msg) {
//		getDesktop().logInfo(msg);
		System.out.println("#INFO: Procurando elemento " + msg + "...");
	}
	
	public static void logErro(String msg) {
//		getDesktop().logError(msg);
		System.out.println("#ERRO: " + msg);
	}
	
	public static void logWarm(String msg) {
//		getDesktop().logWarning(msg);
		System.out.println("#WARM: " + msg);
	}
	
	public static Object jsExec(String executeString, Object... elements) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		return js.executeScript(executeString, elements);
	}
	
}


