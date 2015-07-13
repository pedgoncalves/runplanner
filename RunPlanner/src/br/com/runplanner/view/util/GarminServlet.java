package br.com.runplanner.view.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

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

import sun.misc.BASE64Decoder;
import br.com.runplanner.view.ActivityMBean;

public class GarminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public GarminServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings("deprecation")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//System.out.println("Entrando no Garmin Servlet...");

		String dados = request.getParameter("dados");
		String tipo = request.getParameter("tipo");


		String fileName = "activity_garmin_"+new Date().getTime();
		File f = null;
		
		if ( tipo.equals("tcx" ) ) {
			fileName += ".xml";
			dados = URLDecoder.decode(dados, "utf-8");

			f = new File(fileName);
			FileWriter writer = new FileWriter(f);
			writer.append(dados);
			writer.flush();
			writer.close();
		}
		else if ( tipo.equals("fit" ) ) {
			fileName += ".fit";
			dados = dados.substring( dados.indexOf(".fit")+5 );
			
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] decoded = decoder.decodeBuffer(dados);
			
			f = new File(fileName);
			FileOutputStream writer = new FileOutputStream(f);
			writer.write(decoded);
			writer.flush();
			writer.close();
		}
		
		

		//System.out.println("Arquivo criado: " + f.getAbsolutePath());
		
		FacesContext facesContext = getFacesContext(request, response);
		Application app = facesContext.getApplication();
		ActivityMBean activityMBean = (ActivityMBean)app.getVariableResolver().resolveVariable(facesContext, "activityMBean");
		
		activityMBean.setActivityFile(f);
		
		//System.out.println("Saindo do Garmin Servlet...");
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
