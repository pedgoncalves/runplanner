package br.com.runplanner.view.util;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.runplanner.view.PublicActivityMBean;

public class ActivityServlet extends HttpServlet {

	private static final long serialVersionUID = 1718123107826512478L;

	public ActivityServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings("deprecation")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String activityIdParam = request.getPathInfo();
		if ( activityIdParam==null || activityIdParam.trim().equals("") ) {
			throw new ServletException("Url invalida ou nao disponivel");
		}
		activityIdParam = activityIdParam.substring(1);
		
		activityIdParam = new String( Base64.decode(activityIdParam) );
		Long activityId = Long.parseLong(activityIdParam);
		
		FacesContext facesContext = getFacesContext(request, response);
		Application app = facesContext.getApplication();
		PublicActivityMBean activityMBean = (PublicActivityMBean)app.getVariableResolver().resolveVariable(facesContext, "publicMBean");
		activityMBean.setActivityId(activityId);
		try {
			activityMBean.prepareActivity();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
		
		
		request.getRequestDispatcher("/viewActivity.jsf").forward(request, response);
	}

	
	private FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		
		if (facesContext == null) {
			
			LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
			Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

			// Create new FacesContext.
			FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
			facesContext = contextFactory.getFacesContext(request.getSession().getServletContext(), request, response, lifecycle);

			// Create new View.
			UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
			facesContext.setViewRoot(view);

		}

		return facesContext;
	}
}
