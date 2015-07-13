package br.com.runplanner.view.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.springframework.web.jsf.FacesContextUtils;

import br.com.runplanner.service.PessoaService;

public class CustomerConverter implements Converter {
	
	private PessoaService pessoaService;
	
	public Object getAsObject(FacesContext context, UIComponent arg1, String submittedValue) {
		if (submittedValue.trim().equals("")) {  
            return null;  
        } else {  
            try {  
                Long number = Long.parseLong(submittedValue);  
  
                return getPessoaService(context).loadById(number);  
            } catch(NumberFormatException exception) {  
                exception.printStackTrace();  
            }  
        }  
  
        return null;  
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		if (value == null || value.equals("")) {  
            return "";  
        } else {  
            return String.valueOf(value);  
        }  
	}

	public PessoaService getPessoaService(FacesContext context) {
		if ( pessoaService==null ) {
			pessoaService = (PessoaService) FacesContextUtils.getWebApplicationContext(context).getBean("pessoaService"); 
		}
		return pessoaService;
	}


}
