package br.com.plator.test.engine;

import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestHelper {

	private static final int QTDE_MAX_TENTATIVAS = 5;
	private static WebDriver driver;

	private TestHelper() {
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------- Browsers Methods
	// ------------------------------------------------------------------------

	public static WebDriver getDefaultBrowser() {
		return getDefaultBrowser(1);
	}

	private static WebDriver getDefaultBrowser(int tentativa) {
		try {
			if (driver == null) {
				driver = new FirefoxDriver();
			}

			driver.getCurrentUrl();
		} catch (Exception e) {
			try {
				driver.quit();
			} catch (Exception e1) {
			}

			driver = null;

			if (tentativa <= QTDE_MAX_TENTATIVAS) {
				return getDefaultBrowser(tentativa + 1);
			}
		}

		return driver;
	}

	public static void closeBrowser() {
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception e) {
			}
			driver = null;
		}
	}

	// ------------------------------------------------------------------------
	// ----------------------------------------------------- Navigation Methods
	// ------------------------------------------------------------------------

	public static void goTo(String url) {
		getDefaultBrowser().get(url);
	}

	public static boolean isCurrentUrl(String url) {
		return getDefaultBrowser().getCurrentUrl().equals(url);
	}

	// ------------------------------------------------------------------------
	// -------------------------------------------------------- Actions Methods
	// ------------------------------------------------------------------------

	public static void writeById(String id, String text) {
		writeById(id, text, false);
	}

	public static void writeById(String id, String text, boolean verifyValue) {
		WebElement element = getElementBy(By.id(id));
		element.sendKeys(text);

		if (verifyValue && !text.equals(element.getAttribute("value"))) {
			throw new IllegalStateException("Valor não confere");
		}
	}

	public static void writeByName(String name, String text) {
		writeByName(name, text, false);
	}

	public static void writeByName(String name, String text, boolean verifyValue) {
		WebElement element = getElementBy(By.name(name));
		element.sendKeys(text);

		if (verifyValue && !text.equals(element.getAttribute("value"))) {
			throw new IllegalStateException("Valor não confere");
		}
	}

	public static void clickById(String id) {
		getElementBy(By.id(id)).click();
	}

	public static void clickByClass(String className) {
		getElementBy(By.className(className)).click();
	}

	public static void clickRadioByName(String name, int index) {
		List<WebElement> radios = getElementsBy(By.name(name));
		if (index >= 0 && index < radios.size()) {
			radios.get(index).click();
		} else {
			throw new IllegalStateException("radio not found");
		}
	}

	public static void selectComboboxById(String id, String value) {
		selectComboboxById(id, value, false);
	}

	public static void selectComboboxById(String id, String value, boolean verifyValue) {
		WebElement element = getElementBy(By.id(id));
		Select select = new Select(element);
		select.selectByValue(value);

		if (verifyValue && !value.equals(readComboboxById(id))) {
			throw new IllegalStateException("Valor não confere");
		}
	}

	public static void submitById(String id) {
		WebElement element = getElementBy(By.id(id));
		element.submit();
	}

	public static void submitByName(String name) {
		WebElement element = getElementBy(By.name(name));
		element.submit();
	}

	public static void clearInputById(String id) {
		jsExec("arguments[0].value = '';", getElementBy(By.id(id)));
	}

	public static void clearInputByName(String name) {
		jsExec("arguments[0].value = '';", getElementBy(By.name(name)));
	}

	// ------------------------------------------------------------------------
	// ----------------------------------------------------- JavaScript Methods
	// ------------------------------------------------------------------------

	public static String jsReadValueById(String id) {
		Object jsReturn = jsExec("return $('[id=\"" + id + "\"]').val();");
		return jsReturn != null ? jsReturn.toString() : null;
	}

	public static void jsWriteById(String id, String value) {
		jsExec("$('[id=\"" + id + "\"]').val(\"" + value + "\");");
	}

	public static void jsSubmitById(String id) {
		jsExec("$('[id=\"" + id + "\"]').submit();");
	}
	
	public static void jsClickById(String id) {
		jsExec("$('[id=\"" + id + "\"]').click();");
	}

	public static void jsClickByContainsId(String idPart) {
		jsExec("$('[id*=\"" + idPart + "\"]').click();");
	}

	public static void jsSelectComboboxById(String id, String value) {
		jsExec("$('[id=\"" + id + "\"]').val(\"" + value + "\")");
	}

	public static String jsReadComboboxById(String id) {
		Object jsReturn = jsExec("return $('[id=\"" + id + "\"]').val()");
		return jsReturn != null ? jsReturn.toString() : null;
	}
	
	public static String jsReadComboboxByName(String name) {
		Object jsReturn = jsExec("return $('[name=\"" + name + "\"]').val()");
		return jsReturn != null ? jsReturn.toString() : null;
	}	

	public static void jsRadioByNameAndIndexes(String name, int... indexes) {
		jsExec("$('[name=\"" + name + "\"]').removeAttr(\"checked\");");
		for (int i = 0; i < indexes.length; i++) {
			jsExec("$('[name=\"" + name + "\"][value=\"" + indexes[i] + "\"]').click()");
		}
	}

	public static boolean jsExistsById(String id) {
		Object jsReturn = jsExec("return $('[id=\"" + id + "\"]').length != 0");
		return jsReturn instanceof Boolean && (Boolean) jsReturn;
	}

	public static boolean jsExistsByName(String name) {
		Object jsReturn = jsExec("return $('[name=\"" + name + "\"]').length != 0");
		return jsReturn instanceof Boolean && (Boolean) jsReturn;
	}

	public static void jsWaitById(String id) {
		jsWaitById(id, 10);
	}

	public static void jsWaitById(String id, int timeoutInSeconds) {
		int startInSeconds = (int) new Date().getTime() / 1000;

		while ((new Date().getTime() / 1000) - startInSeconds > timeoutInSeconds) {
			Object result = jsExec("return $('[id=\"" + id + "\"]').length != 0");
			if (result instanceof Boolean && (Boolean) result) {
				return;
			}
		}

		throw new IllegalStateException("Componente não foi encontrado dentro do tempo estipulado.");
	}

	public static Object jsExec(String executeString, Object... elements) {
		JavascriptExecutor js = (JavascriptExecutor) getDefaultBrowser();
		return js.executeScript(executeString, elements);
	}

	// ------------------------------------------------------------------------
	// ----------------------------------------------------------- Read Methods
	// ------------------------------------------------------------------------

	public static String readTextById(String id) {
		WebElement element = getElementBy(By.id(id));
		return element.getText();
	}

	public static String readComboboxById(String id) {
		WebElement element = getElementBy(By.id(id));
		Select select = new Select(element);
		return select.getFirstSelectedOption().getAttribute("value");
	}

	public static String readComboboxByName(String name) {
		WebElement element = getElementBy(By.name(name));
		Select select = new Select(element);
		return select.getFirstSelectedOption().getAttribute("value");
	}

	public static boolean existsById(String id) {
		return existsBy(By.id(id));
	}

	public static boolean existsByName(String name) {
		return existsBy(By.name(name));
	}

	public static boolean existsBy(By by) {
		try {
			getDefaultBrowser().findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public static WebElement getElementBy(By by) {
		return getDefaultBrowser().findElement(by);
	}

	public static List<WebElement> getElementsBy(By by) {
		return getDefaultBrowser().findElements(by);
	}

	// ------------------------------------------------------------------------
	// ----------------------------------------------------------- Wait Methods
	// ------------------------------------------------------------------------

	public static void waitById(String id) {
		waitById(id, 10);
	}
	
	public static void waitByName(String name) {
		waitByName(name, 10);
	}
	
	public static void waitById(String id, long timeoutInSeconds) {
		WebDriverWait waiting = new WebDriverWait(getDefaultBrowser(), timeoutInSeconds);
		waiting.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
	}

	public static void waitByName(String name, long timeoutInSeconds) {
		WebDriverWait waiting = new WebDriverWait(getDefaultBrowser(), timeoutInSeconds);
		waiting.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
	}

	public static void waitByClass(String className, long timeout) {
		WebDriverWait waiting = new WebDriverWait(getDefaultBrowser(), timeout);
		waiting.until(ExpectedConditions.presenceOfElementLocated(By.className(className)));
	}
}
