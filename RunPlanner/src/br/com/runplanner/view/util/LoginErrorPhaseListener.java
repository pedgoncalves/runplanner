package br.com.runplanner.view.util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

public class LoginErrorPhaseListener implements PhaseListener
{
    private static final long serialVersionUID = -1216620620302322995L;
 
    @SuppressWarnings("deprecation")
	public void beforePhase(final PhaseEvent arg0) {
        Exception e = (Exception) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(
                AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(
                AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY, null);
        FacesMessage message = null;
        if (e instanceof BadCredentialsException ) {
			message = new FacesMessage(FacesMessage.SEVERITY_FATAL, 
	    			MessagesResources.getStringFromBundle("template.login.bad.credentials",""),"");  
        } else if ((e != null) && (e.getCause() instanceof LockedException )) {
        	message = new FacesMessage(FacesMessage.SEVERITY_FATAL,
        			MessagesResources.getStringFromBundle("template.login.locked",""),"");  
        } else if(e instanceof AuthenticationServiceException) {
        	message = new FacesMessage(FacesMessage.SEVERITY_FATAL,
        			MessagesResources.getStringFromBundle("template.login.multiple.records",""),"");
        }
        
        if(message != null) {
	        FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
 
    public void afterPhase(final PhaseEvent arg0){
    	
    }
 
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
 
}
