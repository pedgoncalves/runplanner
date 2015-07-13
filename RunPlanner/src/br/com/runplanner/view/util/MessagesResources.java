package br.com.runplanner.view.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class MessagesResources {
	private static final String DEFAULT_BUNDLE = "messages";
	
	private static ClassLoader getCurrentClassLoader(Object fallbackClass) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = fallbackClass.getClass().getClassLoader();
		}
		return loader; 
	}
	
	/**
	 * Obtem uma string a partir de um resource bundle, correspondente a chave informada.
	 * As strings podem utilizar tokens no formato {i}, onde 'i' corresponde ao indice
	 * no array de params.
	 */
	public static String getStringFromBundle(ClassLoader loader, String bundleName, Locale locale, 
			String key, String defaultValue, Object... params) {
		String text = null;		
				
		if (bundleName != null) {
			text = loadStringFromBundle(loader, bundleName, locale, key, null);
		}
		
		if (text == null) {			
			text = loadStringFromBundle(loader, DEFAULT_BUNDLE, locale, key, defaultValue);						
		}
		
		if (text == null) {			
			return key;
		}
		
		//adicionar parametros a mensagem
		if (params != null && params.length > 0) {
			MessageFormat mf = new MessageFormat(text, locale);
			text = mf.format(params);
		}		
		
		return text;
	}
	
	/**
	 * Obtem uma string a partir do message bundle da aplicacao.
	 */
	public static String getStringFromBundle(String key, String defaultValue, Object... params) {
		FacesContext context = FacesContext.getCurrentInstance();
		return getStringFromBundle(getCurrentClassLoader(context), context.getApplication().getMessageBundle(), 
				context.getViewRoot().getLocale(), key, defaultValue, params);
	}
	
	/**
	 * Obtem o bundle da aplicacao.
	 */
	public static ResourceBundle getBundleApplication() {
		FacesContext context = FacesContext.getCurrentInstance();
		String bundleName = context.getApplication().getMessageBundle();
		
		return ResourceBundle.getBundle(bundleName == null?DEFAULT_BUNDLE:bundleName, 
				context.getViewRoot().getLocale(), getCurrentClassLoader(context));
	}
	
	/**
	 * Obtem o bundle da aplicacao.
	 */
	private static ResourceBundle getBundle(String bundleName, Locale locale, ClassLoader loader) {
		return ResourceBundle.getBundle(bundleName, locale, loader);
	}
	
	private static String loadStringFromBundle(ClassLoader loader, String bundleName, 
			Locale locale, String key, String defaultValue) {		
		String result = null;
		ResourceBundle bundle = getBundle(bundleName, locale, loader);
		if (bundle != null) {
			try {
				result = bundle.getString(key);
				
			} catch (MissingResourceException e) {
				result = defaultValue;
			}
		}
		return result;
	}
	
	/**
	 * Obtem uma mensagem a partir de um resource bundle, correspondente a chave informada.
	 * As mensagens podem utilizar tokens no formato {i}, onde 'i' corresponde ao indice
	 * no array de params.
	 */
	public static FacesMessage getMessageFromBundle(FacesMessage.Severity severity, String key, Object... params) {
		String summary = getStringFromBundle(key, null, params);
		String detail = getStringFromBundle(key + "_detail", summary, params);
		FacesMessage message = new FacesMessage(severity, summary, detail);
		
		return message;
	}	
	
	/**
	 * Obtem o label do componente. Se nao existir, retorna o ID.
	 */
	public static String getLabel(FacesContext facesContext, UIComponent component) {
        Object label = component.getAttributes().get("label");
        if(label != null) {
            return label.toString();
        }
        
        ValueExpression expression = component.getValueExpression("label");
        if(expression != null) {
            return expression.getExpressionString();
        }
        
        return component.getClientId(facesContext);
    }
}
