package org.repocrud.config;

import com.vaadin.flow.server.ServletHelper.RequestType;
import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * SecurityUtils takes care of all such static operations that have to do with
 * security and querying rights from different beans of the UI.
 *
 */
public final class SecurityUtils {

	private SecurityUtils() {
		// Util methods only
	}

	/**
	 * Gets the user name of the currently signed in user.
	 *
	 * @return the user name of the current user or <code>null</code> if the user
	 *         has not signed in
	 */
	public static String getUsername() {
		UserDetails userDetails = getUserDetails();
		return userDetails.getUsername();
	}

	public static UserDetails getUserDetails() {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null) return null;
		Authentication authentication = context.getAuthentication();
		if (authentication == null) return null;
		Object principal = authentication.getPrincipal();
		return  (principal instanceof  UserDetails) ? (UserDetails) principal : null;
	}

	/**
	 * Checks if access is granted for the current user for the given secured view,
	 * defined by the view class.
	 *
	 * @param securedClass
	 * @return true if access is granted, false otherwise.
	 */
	public static boolean isAccessGranted(Class<?> securedClass) {
		Secured secured = AnnotationUtils.findAnnotation(securedClass, Secured.class);
		if (secured == null) {
			return true;
		}

		Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();
		if (userAuthentication == null) {
			return false;
		}
		List<String> allowedRoles = Arrays.asList(secured.value());
		return userAuthentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.anyMatch(allowedRoles::contains);
	}

	/**
	 * Checks if the user is logged in.
	 *
	 * @return true if the user is logged in. False otherwise.
	 */
	public static boolean isUserLoggedIn() {
		SecurityContext context = SecurityContextHolder.getContext();
		return context.getAuthentication() != null
				&& !(context.getAuthentication() instanceof AnonymousAuthenticationToken);
	}

	/**
	 * Tests if the request is an internal framework request. The test consists of
	 * checking if the request parameter is present and if its value is consistent
	 * with any of the request types know.
	 *
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return true if is an internal framework request. False otherwise.
	 */
	static boolean isFrameworkInternalRequest(HttpServletRequest request) {
		final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
		return parameterValue != null
				&& Stream.of(RequestType.values()).anyMatch(r -> r.getIdentifier().equals(parameterValue));
	}

}
