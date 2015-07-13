package br.com.runplanner.view.util;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import br.com.runplanner.domain.Activity;

public class UrlUtil {

	public static String getShortenUrl(Activity activity) {

		if (activity.getShortenUrl()!=null) {
			return activity.getShortenUrl();
		}

		StringBuffer urlString = new StringBuffer();

		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		urlString.append( context.getRequestScheme() );
		urlString.append( "://" );
		urlString.append( context.getRequestServerName() );
		urlString.append( context.getRequestContextPath() );
		urlString.append( "/public/" );
		urlString.append( activity.getCodedId() );

		//TODO Shortner de URL
		/*@SuppressWarnings("deprecation")
		String urlEncoded = URLEncoder.encode(urlString.toString());
		

		try {
			URL url = new URL("http://migre.me/api.txt?url="+urlEncoded);

			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			BufferedReader rd = new BufferedReader (new InputStreamReader (is));  

			String content = rd.readLine();

			if (activity.getShortenUrl()==null) {
				activity.setShortenUrl(content);
			}

			return content;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  */

		return urlString.toString();
	}

}
