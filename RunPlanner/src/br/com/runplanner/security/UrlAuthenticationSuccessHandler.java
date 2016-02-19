package br.com.runplanner.security;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class UrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	protected Log logger = LogFactory.getLog(this.getClass());

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		handle(request, response, authentication);
		clearAuthenticationAttributes(request);
	}

	protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		String targetUrl = determineTargetUrl(request, authentication);

		if (response.isCommitted()) {
			logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
			return;
		}

		redirectStrategy.sendRedirect(request, response, targetUrl);
	}

	protected String determineTargetUrl(HttpServletRequest request, Authentication authentication) {
		boolean isUser = false;
		boolean isTeacher = false;
		boolean isAdmin = false;
		
		Boolean isBlackPage = (Boolean)request.getSession().getAttribute("isBlank");

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority grantedAuthority : authorities) {
			if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
				isUser = true;
				break;
			} else if (grantedAuthority.getAuthority().equals("ROLE_ADM")) {
				isAdmin = true;
				break;
			} else if (grantedAuthority.getAuthority().equals("ROLE_ASSESSOR")) {
				isTeacher = true;
				break;
			} else if (grantedAuthority.getAuthority().equals("ROLE_TEACHER")) {
				isTeacher = true;
				break;
			}
		}

		if (!isBlackPage) {
			if (isUser) {
				return "/pages/indexV3.jsf";
			} else if (isAdmin) {
				return "/pages/advice/index.jsf";
			} else if (isTeacher) {
				return "/pages/teacher/indexV2.jsf";
			} else {
				throw new IllegalStateException();
			}
		}
		else {
			if (isUser) {
				return "/pages/indexV3.jsf";
			} else if (isTeacher) {
				return "/pages/teacher/indexV2Blank.jsf";
			} else {
				throw new IllegalStateException();
			}
		}
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}
		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}
	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

}
