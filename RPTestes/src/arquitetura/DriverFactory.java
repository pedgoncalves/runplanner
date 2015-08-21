package arquitetura;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class DriverFactory {
	private static Drivers driverSelecionado = Drivers.CHROME;
	
	private static WebDriver driver;
	
	private DriverFactory(){}
	
	public static WebDriver getDriver(){
		if (driver == null) {
			switch (driverSelecionado) {
				case FIREFOX: driver = getFirefoxDriver(); break;
				case CHROME: driver = getChromeDriver(); break;
			}
		}
		return driver;
	}
	
	private static WebDriver getFirefoxDriver(){
		FirefoxProfile profile = new FirefoxProfile();

		return new FirefoxDriver(profile);
	}
	
	private static WebDriver getChromeDriver(){

		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		
		org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
        proxy.setHttpProxy("localhost:8080");
		capabilities.setCapability(CapabilityType.PROXY, proxy); 
		capabilities.setJavascriptEnabled(true);
		capabilities.setCapability("chrome.binary", "c:/Program Files (x86)/Google/Chrome/Application/chrome.exe");
		System.setProperty("webdriver.chrome.driver", "./Drivers/chromedriver.exe");
		driver = new ChromeDriver(capabilities);
		driver.manage().window().maximize();
		return driver;
	}
	
	public static void fecharBrowser(){
		if(driver != null) {
			driver.quit();
			driver = null;
		}
	}
	
	public static void alterarDriver(Drivers driver) {
		fecharBrowser();
		driverSelecionado = driver;
	}
}
