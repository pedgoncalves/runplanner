package br.com.runplanner.view;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.MessagesResources;

@Component("userMBean")
@Scope("session")
public class UserMBean {

	private static final String USER_FORM_PAGE = "/pages/changepass";	

	@Autowired
	private PessoaService pessoaService;
		
	private String oldPass;
	private String newPass;
	private String retypePass;
	

	public String goToChange() {
		oldPass = new String();
		newPass = new String();
		retypePass = new String();
		
		
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		session.setAttribute(Constants.SELECTED_MENU, Constants.MENU_PROFILE);
		
		return USER_FORM_PAGE;
	}

	public void changePassword() {
		
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		Pessoa loggedIn =((Pessoa) session.getAttribute(Constants.USER_SESSION));		
		
		if ( !MD5Util.crypt(oldPass).equals(loggedIn.getPassword()) ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"user.pass.old.wrong","");
		}
		else if ( !newPass.equals(retypePass) ) {
			addMessage(FacesMessage.SEVERITY_ERROR,"user.pass.retype.wrong","");
		}
		else {
			loggedIn.setPassword(MD5Util.crypt(newPass));
			try {
				pessoaService.update(loggedIn);
			} catch (EntityNotFoundException e) {
				//TODO
			}			
			addMessage(FacesMessage.SEVERITY_INFO,"user.pass.change.sucess","");
		}
		
		oldPass = new String();
		newPass = new String();
		retypePass = new String();
		
	}

	protected void addMessage(Severity severity, String sumaryKey, String detailKey) {  

		FacesMessage message = new FacesMessage(severity, 
				MessagesResources.getStringFromBundle(sumaryKey,""), 
				MessagesResources.getStringFromBundle(detailKey,""));  

		FacesContext.getCurrentInstance().addMessage(null, message);  
	}

	//Gets and Sets
	public String getOldPass() {
		return oldPass;
	}

	public void setOldPass(String oldPass) {
		this.oldPass = oldPass;
	}

	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}

	public String getRetypePass() {
		return retypePass;
	}

	public void setRetypePass(String retypePass) {
		this.retypePass = retypePass;
	}

	public void setPessoaService(PessoaService pessoaService) {
		this.pessoaService = pessoaService;
	}

}
