package br.com.runplanner.view;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Pessoa;

@Component("themeSwitcherBean")
@Scope("session")
public class ThemeSwitcherBean extends BasicMBean{
	private static final String DEFAULT_THEME = "runplanner";

	private Map<String, String> themes;  
      
    private Pessoa logged;  
      
    public Map<String, String> getThemes() {  
        return themes;  
    }  
  
    public String getTheme() {
    	logged = getSessionUser();
    	if(logged != null && logged.getAdvice() != null
    			&& logged.getAdvice().getTheme() != null 
    			&& !logged.getAdvice().getTheme().equals("")) {
    		return logged.getAdvice().getTheme();
    	}
    	return DEFAULT_THEME;
    }  
  
    public void setTheme(String theme) {  
    	logged.getAdvice().setTheme(theme);  
    }  
  
    @PostConstruct  
    public void init() {
        themes = new TreeMap<String, String>();  
        themes.put("Tema Azul", "runplanner");  
        themes.put("Tema Amarelo", "runplanner-amarelo");  
        themes.put("Tema Vermelho", "runplanner-vermelho");
    }

	@Override
	public String goToList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String goToCreate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String goToModify() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String delete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String save() {
		// TODO Auto-generated method stub
		return null;
	}  
      
    /*public void saveTheme() {  
        gp.setTheme(theme);  
    }  */
  
}

/*class GuestPreferences implements Serializable {

    private static final long serialVersionUID = 1L;
	private String theme = "runplanner"; //default

    public String getTheme() {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if(params.containsKey("theme")) {
            	theme = params.get("theme");
            }
            
            return theme;
    }

    public void setTheme(String theme) {
            this.theme = theme;
    }
}
*/