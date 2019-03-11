package spring.mine.common.controller;

import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import spring.mine.common.form.BaseForm;
import spring.mine.internationalization.MessageUtil;
import us.mn.state.health.lims.common.action.IActionConstants;
import us.mn.state.health.lims.common.log.LogEvent;
import us.mn.state.health.lims.common.util.StringUtil;
import us.mn.state.health.lims.common.util.SystemConfiguration;
import us.mn.state.health.lims.login.dao.UserModuleDAO;
import us.mn.state.health.lims.login.daoimpl.UserModuleDAOImpl;
import us.mn.state.health.lims.login.valueholder.UserSessionData;

@Component
public abstract class BaseController implements IActionConstants {

	@Autowired
	MessageUtil messageUtil;
	// Request being autowired appears to be threadsafe because of how Spring
	// handles autowiring, despite all controllers being singletons
	// However this is still not best practice and it would be better to rely on
	// Spring's dependency injection for accessing the request
	@Autowired
	protected HttpServletRequest request;

	protected abstract String findLocalForward(String forward);

	/**
	 * Must be implemented by subclasses to set the title for the requested page.
	 * The value returned should be a key String from the
	 * ApplicationResources.properties file.
	 *
	 * @return the title key for this page.
	 */
	protected abstract String getPageTitleKey();

	/**
	 * Must be implemented by subclasses to set the subtitle for the requested page.
	 * The value returned should be a key String from the
	 * ApplicationResources.properties file.
	 *
	 * @return the subtitle key this page.
	 */
	protected abstract String getPageSubtitleKey();

	/**
	 * This getPageTitleKey method accepts a request and form parameter so that a
	 * subclass can override the method and conditionally return different titles.
	 *
	 * @param request the request
	 * @param form    the form associated with this request.
	 * @return the title key for this page.
	 */
	protected String getPageTitleKey(HttpServletRequest request, BaseForm form) {
		return getPageTitleKey();
	}

	protected String getPageTitleKeyParameter(HttpServletRequest request, BaseForm form) {
		return null;
	}

	/**
	 * This getSubtitleKey method accepts a request and form parameter so that a
	 * subclass can override the method and conditionally return different
	 * subtitles.
	 *
	 * @param request the request
	 * @param form    the form associated with this request.
	 * @return the subtitle key this page.
	 */
	protected String getPageSubtitleKey(HttpServletRequest request, BaseForm form) {
		return getPageSubtitleKey();
	}

	protected String getPageSubtitleKeyParameter(HttpServletRequest request, BaseForm form) {
		return null;
	}

	/**
	 * Template method to allow subclasses to handle special cases. The default is
	 * to return the message
	 *
	 * @param message The message
	 * @return The message
	 */
	protected String getActualMessage(String message) {
		return message;
	}

	/**
	 * Utility method to simplify the lookup of MessageResource Strings in the
	 * ApplicationResources.properties file for this application.
	 *
	 * @param messageKey the message key to look up
	 */
	protected String getMessageForKey(String messageKey) throws Exception {
		String message = StringUtil.getContextualMessageForKey(messageKey);
		return message == null ? getActualMessage(messageKey) : message;
	}

	/**
	 * Utility method to simplify the lookup of MessageResource Strings in the
	 * ApplicationResources.properties file for this application.
	 *
	 * @param request    the HttpServletRequest
	 * @param messageKey the message key to look up
	 */
	protected String getMessageForKey(HttpServletRequest request, String messageKey) throws Exception {
		if (null == messageKey) {
			return null;
		}
		java.util.Locale locale = (java.util.Locale) request.getSession()
				.getAttribute("org.apache.struts.action.LOCALE");
		// Return the message for the user's locale.
		return MessageUtil.getMessage(messageKey);
		// return ResourceLocator.getInstance().getMessageResources().getMessage(locale,
		// messageKey);
	}

	protected String getMessageForKey(HttpServletRequest request, String messageKey, String arg0) throws Exception {
		if (null == messageKey) {
			return null;
		}
		java.util.Locale locale = (java.util.Locale) request.getSession()
				.getAttribute("org.apache.struts.action.LOCALE");
		// Return the message for the user's locale.
		return MessageUtil.getMessage(messageKey);
		// return ResourceLocator.getInstance().getMessageResources().getMessage(locale,
		// messageKey, arg0);
	}

	protected void setPageTitles(HttpServletRequest request, BaseForm form) {

		String pageSubtitle = null;
		String pageTitle = null;
		String pageTitleKey = getPageTitleKey(request, form);
		String pageSubtitleKey = getPageSubtitleKey(request, form);

		String pageTitleKeyParameter = getPageTitleKeyParameter(request, form);
		String pageSubtitleKeyParameter = getPageSubtitleKeyParameter(request, form);

		request.getSession().setAttribute(Globals.LOCALE_KEY, SystemConfiguration.getInstance().getDefaultLocale());

		try {
			if (StringUtil.isNullorNill(pageTitleKeyParameter)) {
				pageTitle = getMessageForKey(request, pageTitleKey);
			} else {
				pageTitle = getMessageForKey(request, pageTitleKey, pageTitleKeyParameter);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogEvent.logError("BaseController", "setPageTitles", "could not get message for key: " + pageTitleKey);
		}

		try {
			if (StringUtil.isNullorNill(pageSubtitleKeyParameter)) {
				pageSubtitle = getMessageForKey(request, pageSubtitleKey);
			} else {
				pageSubtitle = getMessageForKey(request, pageSubtitleKey, pageSubtitleKeyParameter);
			}

		} catch (Exception e) {
			e.printStackTrace();
			LogEvent.logError("BaseController", "setPageTitles", "could not get message for key: " + pageSubtitleKey);
		}

		if (null != pageTitle) {
			request.setAttribute(PAGE_TITLE_KEY, pageTitle);
		}
		if (null != pageSubtitle) {
			request.setAttribute(PAGE_SUBTITLE_KEY, pageSubtitle);
		}

	}

	protected String getSysUserId(HttpServletRequest request) {
		UserSessionData usd = (UserSessionData) request.getSession().getAttribute(USER_SESSION_DATA);
		if (usd == null) {
			return null;
		}
		return String.valueOf(usd.getSystemUserId());
	}

	protected void setSuccessFlag(HttpServletRequest request, String forwardFlag) {
		request.setAttribute(FWD_SUCCESS, FWD_SUCCESS.equals(forwardFlag));
	}

	protected void setSuccessFlag(HttpServletRequest request) {
		request.setAttribute(FWD_SUCCESS, Boolean.TRUE);
	}

	protected boolean userHasPermissionForModule(HttpServletRequest request, String module) {
		UserModuleDAO userModuleDAO = new UserModuleDAOImpl();
		if (!userModuleDAO.isUserAdmin(request)
				&& SystemConfiguration.getInstance().getPermissionAgent().equals("ROLE")) {
			@SuppressWarnings("rawtypes")
			HashSet accessMap = (HashSet) request.getSession().getAttribute(IActionConstants.PERMITTED_ACTIONS_MAP);
			return accessMap.contains(module);
		}

		return true;
	}

	protected String findForward(String forward) {
		if (LOGIN_PAGE.equals(forward)) {
			return "redirect:LoginPage.do";
		}
		if (HOME_PAGE.equals(forward)) {
			return "redirect:Home.do";
		}
		return findLocalForward(forward);
	}

	protected ModelAndView findForward(String forward, BaseForm form) {
		// TO DO move the set page titles into an interceptor if possible
		setPageTitles(request, form);

		// insert global forwards here
		return new ModelAndView(findForward(forward), "form", form);
	}

	protected ModelAndView findForward(String forward, Map<String, Object> requestObjects, BaseForm form) {
		ModelAndView mv = findForward(forward, form);
		mv.addAllObjects(requestObjects);
		return mv;
	}

	protected ModelAndView getForward(ModelAndView mv, String id, String startingRecNo, String direction) {
		LogEvent.logInfo("BaseAction", "getForward()", "This is forward " + mv.getViewName());

		if (id != null) {
			mv.addObject(ID, id);
		}
		if (startingRecNo != null) {
			mv.addObject("startingRecNo", startingRecNo);
		}
		if (direction != null) {
			mv.addObject("direction", direction);
		}

		return mv;
	}

	protected ModelAndView getForwardWithParameters(ModelAndView mv, Map<String, String> params) {
		mv.addAllObjects(params);
		return mv;
	}

	protected void saveErrors(Errors errors) {
		if (request.getAttribute(REQUEST_ERRORS) == null) {
			request.setAttribute(REQUEST_ERRORS, errors);
		} else {
			Errors previousErrors = (Errors) request.getAttribute(REQUEST_ERRORS);
			if (previousErrors.hasErrors() && previousErrors.getObjectName().equals(errors.getObjectName())) {
				previousErrors.addAllErrors(errors);
			} else {
				request.setAttribute(REQUEST_ERRORS, errors);
			}
		}
	}

	protected Errors getErrors() {
		return (Errors) request.getAttribute(REQUEST_ERRORS);
	}

}
