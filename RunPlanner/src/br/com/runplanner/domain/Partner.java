package br.com.runplanner.domain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@Entity
@NamedQueries({	
	@NamedQuery(name = "Partner.findByAdvice", query = "select o from Partner o "
			+ "where (o.advice.id = :adviceId) order by o.name")
})
public class Partner implements BasicEntity  {

	private static final long serialVersionUID = 6781565028878129976L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Basic
	private String name;
	
	@Basic
	private String url;
	
	@Basic
	private String banner;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Advice advice;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		if (url ==null ) url = "";
		String HTTP_PREFIX = "http://";
		String HTTPS_PREFIX = "https://";
		
		if ( !url.startsWith(HTTP_PREFIX) && !url.startsWith(HTTPS_PREFIX) ) {
			url = HTTP_PREFIX+url;
		}
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}
	
	public byte[] getBannerImage() {
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
		return photo;
		
	}

	public StreamedContent getBannerStream() {
		ByteArrayInputStream stream = new ByteArrayInputStream(getBannerImage());
		return new DefaultStreamedContent(stream, "image/jpeg");
	}
}
