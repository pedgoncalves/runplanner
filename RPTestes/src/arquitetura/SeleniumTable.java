package arquitetura;


import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SeleniumTable {

	private WebElement table;
	
	public SeleniumTable(String idTabela) {
		table = SeleniumBaseTest.getDriver().findElement(By.id(idTabela));
	}
	
	public WebElement getCelula(int numLinha, int numColuna){
		return table
				.findElement(By.xpath("//tr[" + numLinha + "]"))
					.findElement(By.xpath("//td[" + numColuna + "]"));
	}
	
	public WebElement getCelulaCabecalho(int coluna) {
		return table
				.findElement(By.xpath("//th[" + coluna + "]"));
	}
}
