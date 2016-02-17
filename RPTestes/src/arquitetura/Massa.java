package arquitetura;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Massa {
	
	private Properties prop;

	public Massa(String path) {
	
		try {
			InputStream in = Massa.class.getClassLoader().getResourceAsStream(path);
			prop = new Properties();
			prop.load(in);
		} catch (IOException e) {
			throw new IllegalStateException(e); 
		}
	}
	
	public String getString(String chave){
		
		return prop.getProperty(chave);
	}
	
	public Boolean getBoolean(String chave){
		
		try {
			return Boolean.parseBoolean(prop.getProperty(chave));
		} catch (Exception e) {
			return false;
		}
	}
	
	public String[] getLista(String chave) {
		
		return prop.getProperty(chave).split(";");
		
	}

}
