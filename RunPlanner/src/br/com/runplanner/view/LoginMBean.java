package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.SystemRoles;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.util.mail.EmailThreadProductor;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MD5Util;
import br.com.runplanner.view.util.MessagesResources;
import br.com.runplanner.view.util.PasswordUtil;

@Component("loginBean")
@Scope("session")
public class LoginMBean implements Serializable {


	private static final long serialVersionUID = -7773821493415197564L;

	@Autowired
	private PessoaService pessoaService;
	
	@Autowired
	private EmailThreadProductor emailThreadProductor;
	
	//private String templateVersion = "/pages/template/templateV3Inner.xhtml";
	private String templateVersion = "/pages/template/templateV2Inner.xhtml";
	private String templateBlank = "/pages/template/templateV2Blank.xhtml";
	//private String templateVersion = "/pages/template/template.xhtml";
	
	private Long userId;
	private String name;
	private String email;
	private String adviceName;
	private String adviceBanner;
	
	private byte[] photo;
	private byte[] adviceLogo;
	
	private String recoverMail;
	
	private List<String> images;  
	
	public void recover() {
		Pessoa user = pessoaService.loadByEmailActive(recoverMail,true);
		
		if ( user==null ) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					MessagesResources.getStringFromBundle("template.msg.recovery.notfound",""),"");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
	        
	        return;
		}
		
		String generatedPass = PasswordUtil.gerarSenha();
		user.setPassword( MD5Util.crypt(generatedPass) );
		
		try {
			pessoaService.update(user);
			
			String messageBody = Constants.EMAIL_RESTART_PASS_BODY;
			messageBody = messageBody.replace("$1", user.getEmail());
			messageBody = messageBody.replace("$2", generatedPass);
			
			emailThreadProductor.enviarMensagem(user.getEmail(), Constants.EMAIL_RESTART_PASS_TITLE, messageBody);
			
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					MessagesResources.getStringFromBundle("template.msg.changepass.sucess",""),"");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
		} catch (EntityNotFoundException e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					MessagesResources.getStringFromBundle("template.msg.entitynotfound.edit",""),"");  
	        FacesContext.getCurrentInstance().addMessage(null, message); 
			e.printStackTrace();
		}		
		recoverMail="";
	}
	
	public void doLogin() throws IOException, ServletException {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		
		boolean isBlank = ((HttpServletRequest) context.getRequest()).getParameter("isBlank")!=null;
		((HttpServletRequest) context.getRequest()).getSession().setAttribute("isBlank",isBlank);
		if (isBlank) {
			templateVersion = templateBlank;
		}
		
		RequestDispatcher dispatcher = ((ServletRequest) context.getRequest()).getRequestDispatcher("/j_spring_security_check");
		dispatcher.forward((ServletRequest) context.getRequest(),(ServletResponse) context.getResponse());

		FacesContext.getCurrentInstance().responseComplete();
		 
		//Buscar informacoes do usuario
		if ( SecurityContextHolder.getContext().getAuthentication()!=null
				&& SecurityContextHolder.getContext().getAuthentication().isAuthenticated() ) {
			
			email = SecurityContextHolder.getContext().getAuthentication().getName();
			
			Pessoa user = pessoaService.loadByEmailActive(email,true);

			if ( user.getAdvice() == null && !user.getRole().equals(SystemRoles.ADMIN) ) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, 
		    			MessagesResources.getStringFromBundle("template.login.bad.credentials",""), "");  
		    	
		        FacesContext.getCurrentInstance().addMessage(null, message);
		        
		        SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
			}
			else {
				
				HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				HttpSession session = req.getSession();
				session.setAttribute(Constants.USER_SESSION,user);
				session.setAttribute(Constants.SELECTED_MENU,Constants.MENU_NONE);
				
				this.name = user.getNome();
				this.userId = user.getId();
				
				Advice advice = user.getAdvice();

				if ( advice!=null ) {
					this.adviceName = advice.getName();
					this.adviceBanner = advice.getAdviceBanner();
					
					if ( advice.getLogo()!=null ) {
						this.adviceLogo = advice.getLogo();
					}
					else {					
						String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_PHOTO;
						FileInputStream is = new FileInputStream(new File(photoPath));
						this.adviceLogo = new byte[is.available()];
						is.read(this.adviceLogo);
					}
				}
				
				
				
				if ( user.getPhoto()!=null ) {
					this.photo = user.getThumb();
				}
				else {					
					String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_PHOTO;
					FileInputStream is = new FileInputStream(new File(photoPath));
					this.photo = new byte[is.available()];
					is.read(this.photo);
				}
				
				//Atualizar data de login
				pessoaService.updateLoginTime(user.getId(), new Date());
			}
			
		}
	}

	public String getUserName() {
		return name;
	}
	
	public String getUserEmail() {
		return email;
	}
	
	public StreamedContent getUserPhoto() {
		if ( photo!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(photo);
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}
	
	public StreamedContent getAdviceLogo() {
		if ( adviceLogo!=null ) {
			ByteArrayInputStream stream = new ByteArrayInputStream(adviceLogo);
			return new DefaultStreamedContent(stream, "image/jpeg");
		}
		return null;
	}

	public String getAdviceName() {
		return adviceName;
	}

	public String getRecoverMail() {
		return recoverMail;
	}

	public void setRecoverMail(String recoverMail) {
		this.recoverMail = recoverMail;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	

	public Long getUserId() {
		return userId;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public void setAdviceName(String adviceName) {
		this.adviceName = adviceName;
	}

	public void setAdviceLogo(byte[] adviceLogo) {
		this.adviceLogo = adviceLogo;
	}

	public List<String> getImages() {  
		images = new ArrayList<String>();  
		  
        for(int i=1;i<=4;i++) {  
            images.add("banner" + i + ".png");  
        }  
		
        return images;  
    }

	public String getTemplateVersion() {
		return templateVersion;
	}

	public String getAdviceBanner() {
		return adviceBanner;
	}

	public void setAdviceBanner(String adviceBanner) {
		this.adviceBanner = adviceBanner;
	} 
	
	 
}