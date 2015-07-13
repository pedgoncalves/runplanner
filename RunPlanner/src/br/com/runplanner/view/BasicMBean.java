package br.com.runplanner.view;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.domain.SystemRoles;
import br.com.runplanner.view.util.Constants;
import br.com.runplanner.view.util.MessagesResources;

public abstract class BasicMBean {


	public abstract String goToList();
	public abstract String goToCreate();
	public abstract String goToModify();
	public abstract String delete();
	public abstract String save();

	protected void addMessage(Severity severity, String sumaryKey) {  

		FacesMessage message = new FacesMessage(severity, 
				MessagesResources.getStringFromBundle(sumaryKey,""),"");  

		FacesContext.getCurrentInstance().addMessage(null, message);  
	}
	
	protected String getRealPath() {
		return FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
	}

	protected String getContextName() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
	}
	
	protected Pessoa getSessionUser() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		return ((Pessoa) session.getAttribute(Constants.USER_SESSION));
	}

	protected void setSelectedMenu(String menu) {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = req.getSession();
		session.setAttribute(Constants.SELECTED_MENU, menu);
	}
	
	protected boolean isUserStudent() {
		Pessoa user = getSessionUser();
		return user.getRole().equals(SystemRoles.USER);
	}

	public static FacesContext getFacesContext(
			HttpServletRequest request, HttpServletResponse response)
	{
		// Get current FacesContext.
		FacesContext facesContext = FacesContext.getCurrentInstance();

		// Check current FacesContext.
		if (facesContext == null) {

			// Create new Lifecycle.
			LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY); 
			Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
 
			// Create new FacesContext.
			FacesContextFactory contextFactory  = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
			facesContext = contextFactory.getFacesContext(request.getSession().getServletContext(), request, response, lifecycle);

			// Create new View.
			UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
			facesContext.setViewRoot(view);                

		}

		return facesContext;
	}
}
