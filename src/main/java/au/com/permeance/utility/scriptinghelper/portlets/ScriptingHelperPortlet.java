/**
 * Copyright (C) 2017 Permeance Technologies
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package au.com.permeance.utility.scriptinghelper.portlets;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Component;

import javax.portlet.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.liferay.portal.kernel.model.PortletCategoryConstants.NAME_HIDDEN;

@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.css-class-wrapper=portlet-controlpanel",
		"com.liferay.portlet.display-category=" + NAME_HIDDEN,
		"com.liferay.portlet.icon=/scripting-helper.png",
		"com.liferay.portlet.preferences-company-wide=true",
		"com.liferay.portlet.instanceable=false",
		"com.liferay.portlet.render-weight=50",
		"com.liferay.portlet.header-portlet-css=/codemirror/lib/codemirror.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/ambiance.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/blackboard.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/cobalt.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/eclipse.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/elegant.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/erlang-dark.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/lesser-dark.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/neat.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/night.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/rubyblue.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/vibrant-ink.css",
		"com.liferay.portlet.header-portlet-css=/codemirror/theme/xq-dark.css",
		"com.liferay.portlet.header-portlet-javascript=/codemirror/codemirror-2.3.5-compressed.js",
		"javax.portlet.name=" + ScriptingHelperPortlet.PORTLET_ID,
		"javax.portlet.display-name=Scripting Helper",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.portlet-mode=text/html",
		"javax.portlet.security-role-ref=administrator",
		"javax.portlet.version=3.0"
	},
	service = Portlet.class
)
public class ScriptingHelperPortlet extends MVCPortlet {

	public static final String PORTLET_ID =
		"au_com_permeance_utility_scriptinghelper_portlets_ScriptingHelperPortlet";

	public static String getDefaultLanguage() {
		if (defaultLanguage == null) {
			Iterator<String> iter = ScriptingUtil.getSupportedLanguages(
			).iterator();
			defaultLanguage = iter.next();
		}

		return defaultLanguage;
	}

	public static void sCheckPermissions(PortletRequest request)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (!permissionChecker.isOmniadmin() &&
			!permissionChecker.isCompanyAdmin()) {

			throw new Exception("No permissions to execute this request");
		}
	}

	@Override
	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		try {
			sCheckPermissions(renderRequest);

			List<String> savedscripts = new ArrayList<>();
			PortletPreferences prefs = renderRequest.getPreferences();

			for (String prefName : prefs.getMap().keySet()) {
				if ((prefName != null) && prefName.startsWith("savedscript.")) {
					savedscripts.add(
						prefName.substring("savedscript.".length()));
				}
			}

			if (savedscripts.size() > 0) {
				Collections.sort(savedscripts);
			}

			renderRequest.setAttribute("savedscripts", savedscripts);

			include("/view.jsp", renderRequest, renderResponse);
		}
		catch (Exception e) {
			_log.warn(e);
			include("/error.jsp", renderRequest, renderResponse);
		}
	}

	public void execute(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		try {
			sCheckPermissions(actionRequest);

			String cmd = ParamUtil.getString(actionRequest, "cmd");
			String language = ParamUtil.getString(actionRequest, "language");
			String script = ParamUtil.getString(actionRequest, "script");
			String editorheight = ParamUtil.getString(
				actionRequest, "editorheight", "");
			String themesel = ParamUtil.getString(
				actionRequest, "themesel", "");

			if (language == null) {
				language = getDefaultLanguage();
			}

			if (script == null) {
				script = StringPool.BLANK;
			}

			actionRequest.setAttribute("editorheight", editorheight);
			actionRequest.setAttribute("language", language);
			actionRequest.setAttribute("script", script);
			actionRequest.setAttribute("themesel", themesel);

			if ("save".equals(cmd)) {
				String newscriptname = ParamUtil.getString(
					actionRequest, "newscriptname");

				if ((newscriptname == null) ||
					(newscriptname.trim().length() == 0)) {

					actionRequest.setAttribute(
						"script_trace",
						"No script name specified to save into!");
					SessionErrors.add(actionRequest, "error");

					return;
				}

				_log.info("Saving new script: " + newscriptname.trim());
				PortletPreferences prefs = actionRequest.getPreferences();

				prefs.setValue("savedscript." + newscriptname.trim(), script);
				prefs.setValue("lang." + newscriptname.trim(), language);
				prefs.store();
			}
			else if ("saveinto".equals(cmd)) {
				String scriptname = ParamUtil.getString(
					actionRequest, "savedscript");

				if (scriptname == null) {
					actionRequest.setAttribute(
						"script_trace", "No script specified to save into!");
					SessionErrors.add(actionRequest, "error");

					return;
				}

				_log.info("Saving saved script: " + scriptname);
				PortletPreferences prefs = actionRequest.getPreferences();

				prefs.setValue("savedscript." + scriptname, script);
				prefs.setValue("lang." + scriptname, language);
				prefs.store();
			}
			else if ("loadfrom".equals(cmd)) {
				String scriptname = ParamUtil.getString(
					actionRequest, "savedscript");

				if (scriptname == null) {
					actionRequest.setAttribute(
						"script_trace", "No script specified to load from!");
					SessionErrors.add(actionRequest, "error");

					return;
				}

				_log.info("Loading saved script: " + scriptname);
				PortletPreferences prefs = actionRequest.getPreferences();

				language = prefs.getValue(
					"lang." + scriptname, getDefaultLanguage());
				script = prefs.getValue(
					"savedscript." + scriptname, StringPool.BLANK);
				actionRequest.setAttribute("language", language);
				actionRequest.setAttribute("script", script);
			}
			else if ("delete".equals(cmd)) {
				String scriptname = ParamUtil.getString(
					actionRequest, "savedscript");

				if (scriptname == null) {
					actionRequest.setAttribute(
						"script_trace", "No script specified to delete!");
					SessionErrors.add(actionRequest, "error");

					return;
				}

				_log.info("Deleting saved script: " + scriptname);
				PortletPreferences prefs = actionRequest.getPreferences();

				prefs.reset("savedscript." + scriptname);
				prefs.reset("lang." + scriptname);
				prefs.store();
			}

			SessionMessages.add(actionRequest, "success");
		}
		catch (Exception e) {
			StringWriter sw = new StringWriter();

			PrintWriter pw = new PrintWriter(sw);

			e.printStackTrace(pw);
			pw.close();
			actionRequest.setAttribute("script_trace", sw.toString());
			_log.error(e);
			SessionErrors.add(actionRequest, e.toString());
		}
	}

	@Override
	public void init() throws PortletException {
		super.init();

		super.copyRequestParameters = false;
	}

	@Override
	public void serveResource(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse) {

		ZipOutputStream zout = null;
		OutputStream out = null;

		try {
			sCheckPermissions(resourceRequest);
			_log.info("Export All As Zip");

			Map<String, String> savedscripts = new TreeMap<>();
			PortletPreferences prefs = resourceRequest.getPreferences();

			for (String prefName : prefs.getMap().keySet()) {
				if ((prefName != null) && prefName.startsWith("savedscript.")) {
					String scriptName = prefName.substring(
						"savedscript.".length());
					String script = prefs.getValue(prefName, "");
					String lang = prefs.getValue(
						"lang." + scriptName, getDefaultLanguage());

					savedscripts.put(scriptName + "." + lang, script);
				}
			}

			// ContentType must be set before calling getPortletOutputStream()
			//  to override with a new type;

			resourceResponse.setContentType("application/zip");
			resourceResponse.addProperty(
				HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate");

			String filename = "liferay-scripts.zip";
			resourceResponse.addProperty(
				HttpHeaders.CONTENT_DISPOSITION, "filename=" + filename);

			out = resourceResponse.getPortletOutputStream();
			zout = new ZipOutputStream(out);

			for (String key : savedscripts.keySet()) {
				String value = savedscripts.get(key);
				zout.putNextEntry(new ZipEntry(key));
				zout.write(value.getBytes("utf-8"));
			}
		}
		catch (Exception e) {
			_log.error(e);
		}
		finally {
			try {
				if (zout != null) {
					zout.close();
				}
			}
			catch (Exception e) {
			}

			try {
				if (out != null) {
					out.close();
				}
			}
			catch (Exception e) {
			}
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		ScriptingHelperPortlet.class);

	private static String defaultLanguage = null;

}