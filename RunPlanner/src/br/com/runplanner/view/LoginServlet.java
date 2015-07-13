package br.com.runplanner.view;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.jsf.FacesContextUtils;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = -3678376765500956672L;

	public LoginServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		FacesContext context = getFacesContext(request, response);
		LoginMBean bean = (LoginMBean) FacesContextUtils.getWebApplicationContext(context).getBean("loginBean"); 
		bean.doLogin();
		
	}
	
	public static FacesContext getFacesContext(
	        HttpServletRequest request, HttpServletResponse response)
	    {
	        // Get current FacesContext.
	        FacesContext facesContext = FacesContext.getCurrentInstance();

	        // Check current FacesContext.
	        if (facesContext == null) {

	            // Create new Lifecycle.
	            LifecycleFactory lifecycleFactory = (LifecycleFactory)
	                FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY); 
	            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

	            // Create new FacesContext.
	            FacesContextFactory contextFactory  = (FacesContextFactory)
	                FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
	            facesContext = contextFactory.getFacesContext(
	                request.getSession().getServletContext(), request, response, lifecycle);

	            // Create new View.
	            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(
	                facesContext, "");
	            facesContext.setViewRoot(view);                

	            // Set current FacesContext.
	            FacesContextWrapper.setCurrentInstance(facesContext);
	        }

	        return facesContext;
	    }

	    // Helpers -----------------------------------------------------------------------------------

	    // Wrap the protected FacesContext.setCurrentInstance() in a inner class.
	    private static abstract class FacesContextWrapper extends FacesContext {
	        protected static void setCurrentInstance(FacesContext facesContext) {
	            FacesContext.setCurrentInstance(facesContext);
	        }
	    }
	
}
