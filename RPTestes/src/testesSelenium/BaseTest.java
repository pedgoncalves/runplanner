package testesSelenium;

import arquitetura.ItensMenu;
import arquitetura.SeleniumBaseTest;

abstract class BaseTest {

	protected static ItensMenu item; 

	static void before(){
		SeleniumBaseTest.waitPorId(item.getURLItem());
	} 
	 
}
