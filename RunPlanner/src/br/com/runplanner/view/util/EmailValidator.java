package br.com.runplanner.view.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class EmailValidator implements Validator {

	private Pattern pattern;
	private Matcher matcher;

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public EmailValidator(){
		pattern = Pattern.compile(EMAIL_PATTERN);
	}

	public boolean validate(final String hex) {
		matcher = pattern.matcher(hex);
		return matcher.matches();
	}

	public void validate(FacesContext arg0, UIComponent arg1, Object arg2)
			throws ValidatorException {
		
		String emailValue = (String) arg2;
		if ( !validate(emailValue) ) {
			
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
	    			MessagesResources.getStringFromBundle("template.email.invalid",""),"");
			
			throw new ValidatorException(message);
		}
	}
}
