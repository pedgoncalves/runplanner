package br.com.runplanner.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Pessoa;
import br.com.runplanner.service.PessoaService;
import br.com.runplanner.view.util.Constants;

@Component("photoMBean")
@Scope("session")
public class PhotoMBean {


	@Autowired
	private PessoaService pessoaService;	

	public StreamedContent getStreamedUserPhoto() {

		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("client_id");

		if( id==null || id.equals("") ) {
			return getDefaultPhoto();
		}		

		Long clientId = Long.parseLong(id);
		Pessoa pessoa = pessoaService.loadById(clientId);
		if ( pessoa.getPhoto()!= null ) {
			ByteArrayInputStream photoStream = new ByteArrayInputStream(pessoa.getPhoto());
			return new DefaultStreamedContent(photoStream, "image/jpeg");
		}
		else {
			return getDefaultPhoto();
		}
	}
	
	public StreamedContent getStreamedPhotoByPath() {

		if ( FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE ) {
			return new DefaultStreamedContent();
		}
		
		String path = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("photo_path");

		if (path == null || path.length() == 0) {
			return getDefaultPhoto();
		}
		
		byte[] photo = null;
		if (path!=null) {
			File f = new File(path);
			photo = new byte[(int)f.length()];
			
			FileInputStream in;
			try {
				in = new FileInputStream(f);
				in.read(photo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(photo);
		return new DefaultStreamedContent(stream, "image/jpeg");
	}
	
	public StreamedContent getStreamedAdviceBannerByPath() {

		if ( FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE ) {
			return new DefaultStreamedContent();
		}
		
		String path = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("photo_path");

		if (path == null || path.length() == 0) {
			return getDefaultBannerAdvice();
		}
		
		byte[] photo = null;
		if (path!=null) {
			File f = new File(path);
			photo = new byte[(int)f.length()];
			
			FileInputStream in;
			try {
				in = new FileInputStream(f);
				in.read(photo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(photo);
		return new DefaultStreamedContent(stream, "image/jpeg");
	}
	
	public StreamedContent getStreamedBanner() {
     
		String banner = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("banner_path");

		if (banner == null || banner.length() == 0) {
			return getDefaultPhoto();
		}
		
		byte[] photo = null;
		if (banner!=null) {
			File f = new File(banner);
			photo = new byte[(int)f.length()];
			
			FileInputStream in;
			try {
				in = new FileInputStream(f);
				in.read(photo);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(photo);
		return new DefaultStreamedContent(stream, "image/jpeg");
	}
	
	public StreamedContent getStreamedUserThumb() {

		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("client_id");

		if( id==null || id.equals("") ) {
			return getDefaultPhoto();
		}		

		Long clientId = Long.parseLong(id);
		Pessoa pessoa = pessoaService.loadById(clientId);
		if ( pessoa.getPhoto()!= null ) {
			ByteArrayInputStream photoStream = new ByteArrayInputStream(pessoa.getThumb());
			return new DefaultStreamedContent(photoStream, "image/jpeg");
		}
		else {
			return getDefaultPhoto();
		}
	}

	private DefaultStreamedContent getDefaultPhoto() {
		String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_PHOTO;

		try {
			FileInputStream is = new FileInputStream(new File(photoPath));
			byte[] photo = new byte[is.available()];
			is.read(photo);

			ByteArrayInputStream stream = new ByteArrayInputStream(photo);

			return new DefaultStreamedContent(stream, "image/jpeg");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private DefaultStreamedContent getDefaultBannerAdvice() {
		String photoPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+Constants.DEFAULT_BANNER_ADVICE;

		try {
			FileInputStream is = new FileInputStream(new File(photoPath));
			byte[] photo = new byte[is.available()];
			is.read(photo);

			ByteArrayInputStream stream = new ByteArrayInputStream(photo);

			return new DefaultStreamedContent(stream, "image/jpeg");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
		


}
