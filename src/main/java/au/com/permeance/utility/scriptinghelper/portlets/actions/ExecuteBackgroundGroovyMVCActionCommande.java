package au.com.permeance.utility.scriptinghelper.portlets.actions;

import au.com.permeance.utility.scriptinghelper.backgroundtask.GroovyBackgroundTaskExecutor;
import au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ScriptingHelperPortlet.PORTLET_ID,
		"mvc.command.name=/scripting/execute/background"
	},
	service = MVCActionCommand.class
)
public class ExecuteBackgroundGroovyMVCActionCommande
	extends BaseMVCActionCommand {

	Log _log = LogFactoryUtil.getLog(
		ExecuteBackgroundGroovyMVCActionCommande.class);

	public static String getDefaultLanguage() {
		if (defaultLanguage == null) {
			Iterator<String> iter = ScriptingUtil.getSupportedLanguages(
			).iterator();
			defaultLanguage = iter.next();
		}

		return defaultLanguage;
	}

	public static String resolveLanguage(String lang) {
		for (String lang1 : ScriptingUtil.getSupportedLanguages()) {
			if (lang1.equalsIgnoreCase(lang)) {
				return lang1;
			}
		}

		return getDefaultLanguage();
	}

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String language = ParamUtil.getString(actionRequest, "language");
		String script = ParamUtil.getString(actionRequest, "script");
		String themesel = ParamUtil.getString(actionRequest, "themesel");

		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getScopeGroupId();

		String titleTask = ParamUtil.getString(
			actionRequest, "bgTaskName", "bgTask");

		titleTask += "_" + System.currentTimeMillis();

		Map<String, Serializable> taskContextMap = new HashMap<>();
		taskContextMap.put("language", language);
		taskContextMap.put("script", script);
		_backgroundTaskManager.addBackgroundTask(
			userId, groupId, titleTask,
			GroovyBackgroundTaskExecutor.class.getName(), taskContextMap, null);

		actionRequest.setAttribute("script", script);
		actionRequest.setAttribute("themesel", themesel);
		actionRequest.setAttribute(WebKeys.REDIRECT, PortletURLBuilder.create(
				PortalUtil.getControlPanelPortletURL(
						actionRequest, ScriptingHelperPortlet.PORTLET_ID,
						PortletRequest.RENDER_PHASE)
		).setMVCPath(
				"/backgroundtasks.jsp"
		).buildString());
	}

	private static String defaultLanguage = null;

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

}