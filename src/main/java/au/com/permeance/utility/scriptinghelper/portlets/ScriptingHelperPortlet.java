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

import static com.liferay.portal.kernel.model.PortletCategoryConstants.NAME_HIDDEN;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.osgi.service.component.annotations.Component;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.io.unsync.UnsyncPrintWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.scripting.ScriptingHelperUtil;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.UnsyncPrintWriterPool;
import com.liferay.portal.kernel.util.WebKeys;

@Component(
		immediate = true,
		property = {
			"com.liferay.portlet.css-class-wrapper=portlet-controlpanel",
			"com.liferay.portlet.display-category=" + NAME_HIDDEN,
			"com.liferay.portlet.icon=/scripting-helper.png",
			"com.liferay.portlet.preferences-company-wide=true",
			"com.liferay.portlet.instanceable=true",
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
			"javax.portlet.security-role-ref=administrator"
		},
		service = Portlet.class
	)
public class ScriptingHelperPortlet extends MVCPortlet {
	
	static final String PORTLET_ID = "au_com_permeance_utility_scriptinghelper_portlets_ScriptingHelperPortlet";

	private static Log _log = LogFactoryUtil.getLog(ScriptingHelperPortlet.class);

	@Override
	public void init() throws PortletException{
		super.init();
		super.copyRequestParameters = false;
	}
	
	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		try {
			sCheckPermissions(renderRequest);

			List<String> savedscripts = new ArrayList<String>();
			PortletPreferences prefs = renderRequest.getPreferences();
			for (String prefName : prefs.getMap().keySet()) {
				if (prefName != null && prefName.startsWith("savedscript.")) {
					savedscripts.add(prefName.substring("savedscript.".length()));
				}
			}
			if (savedscripts.size() > 0) {
				Collections.sort(savedscripts);
			}
			renderRequest.setAttribute("savedscripts", savedscripts);

			include("/view.jsp", renderRequest, renderResponse);
		} catch (Exception e) {
			_log.warn(e);
			include("/error.jsp", renderRequest, renderResponse);
		}
	}

	private static String defaultLanguage = null;

	public static String getDefaultLanguage() {
		if (defaultLanguage == null) {
			Iterator<String> iter = ScriptingUtil.getSupportedLanguages().iterator();
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
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {
		ZipOutputStream zout = null;
		OutputStream out = null;
		try {
			sCheckPermissions(resourceRequest);
			_log.info("Export All As Zip");

			Map<String, String> savedscripts = new TreeMap<String, String>();
			PortletPreferences prefs = resourceRequest.getPreferences();
			for (String prefName : prefs.getMap().keySet()) {
				if (prefName != null && prefName.startsWith("savedscript.")) {
					String scriptName = prefName.substring("savedscript.".length());
					String script = prefs.getValue(prefName, "");
					String lang = prefs.getValue("lang." + scriptName, getDefaultLanguage());
					savedscripts.put(scriptName + "." + lang, script);
				}
			}

			// ContentType must be set before calling getPortletOutputStream()
			//  to override with a new type;
			resourceResponse.setContentType("application/zip");
			resourceResponse.addProperty(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate");

			String filename = "liferay-scripts.zip";
			resourceResponse.addProperty(HttpHeaders.CONTENT_DISPOSITION, "filename=" + filename);

			out = resourceResponse.getPortletOutputStream();
			zout = new ZipOutputStream(out);

			for (String key : savedscripts.keySet()) {
				String value = savedscripts.get(key);
				zout.putNextEntry(new ZipEntry(key));
				zout.write(value.getBytes("utf-8"));
			}

		} catch (Exception e) {
			_log.error(e);
		} finally {
			try {
				if (zout != null) {
					zout.close();
				}
			} catch (Exception e) {
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public void execute(ActionRequest actionRequest, ActionResponse actionResponse) {
		try {
			sCheckPermissions(actionRequest);

			String portletId = "_" + PortalUtil.getPortletId(actionRequest) + "_";

			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(100 * 1024 * 1024);
			PortletFileUpload upload = new PortletFileUpload(factory);

			FileItem fileUploaded = null;
			List<FileItem> items = upload.parseRequest(actionRequest);
			for (FileItem fi : items) {
				if (fi.isFormField()) {
					actionRequest.setAttribute(fi.getFieldName(), fi.getString());
					if (fi.getFieldName().startsWith(portletId)) {
						actionRequest.setAttribute(fi.getFieldName().substring(portletId.length()), fi.getString());
					}
				} else {
					fileUploaded = fi;
				}
			}

			String cmd = (String) actionRequest.getAttribute("cmd");
			String language = (String) actionRequest.getAttribute("language");
			String script = (String) actionRequest.getAttribute("script");
			String editorheight = (String) actionRequest.getAttribute("editorheight");
			String themesel = (String) actionRequest.getAttribute("themesel");
			if (language == null) {
				language = getDefaultLanguage();
			}
			if (script == null) {
				script = StringPool.BLANK;
			}
			actionResponse.setRenderParameter("language", language);
			actionResponse.setRenderParameter("script", script);
			actionResponse.setRenderParameter("editorheight", editorheight);
			actionResponse.setRenderParameter("themesel", themesel);

			if ("execute".equals(cmd)) {

				Map<String, Object> portletObjects = ScriptingHelperUtil.getPortletObjects(getPortletConfig(),
						getPortletContext(), actionRequest, actionResponse);

				UnsyncByteArrayOutputStream unsyncByteArrayOutputStream = new UnsyncByteArrayOutputStream();

				UnsyncPrintWriter unsyncPrintWriter = UnsyncPrintWriterPool.borrow(unsyncByteArrayOutputStream);

				portletObjects.put("out", unsyncPrintWriter);

				_log.info("Executing script");
				ScriptingUtil.exec(null, portletObjects, language, script);
				unsyncPrintWriter.flush();
				actionResponse.setRenderParameter("script_output", unsyncByteArrayOutputStream.toString());
			} else if ("save".equals(cmd)) {
				String newscriptname = (String) actionRequest.getAttribute("newscriptname");
				if (newscriptname == null || newscriptname.trim().length() == 0) {
					actionResponse.setRenderParameter("script_trace", "No script name specified to save into!");
					SessionErrors.add(actionRequest, "error");
					return;
				}

				_log.info("Saving new script: " + newscriptname.trim());
				PortletPreferences prefs = actionRequest.getPreferences();
				prefs.setValue("savedscript." + newscriptname.trim(), script);
				prefs.setValue("lang." + newscriptname.trim(), language);
				prefs.store();
			} else if ("saveinto".equals(cmd)) {
				String scriptname = (String) actionRequest.getAttribute("savedscript");
				if (scriptname == null) {
					actionResponse.setRenderParameter("script_trace", "No script specified to save into!");
					SessionErrors.add(actionRequest, "error");
					return;
				}

				_log.info("Saving saved script: " + scriptname);
				PortletPreferences prefs = actionRequest.getPreferences();
				prefs.setValue("savedscript." + scriptname, script);
				prefs.setValue("lang." + scriptname, language);
				prefs.store();
			} else if ("loadfrom".equals(cmd)) {
				String scriptname = (String) actionRequest.getAttribute("savedscript");
				if (scriptname == null) {
					actionResponse.setRenderParameter("script_trace", "No script specified to load from!");
					SessionErrors.add(actionRequest, "error");
					return;
				}
				_log.info("Loading saved script: " + scriptname);
				PortletPreferences prefs = actionRequest.getPreferences();
				language = prefs.getValue("lang." + scriptname, getDefaultLanguage());
				script = prefs.getValue("savedscript." + scriptname, StringPool.BLANK);
				actionResponse.setRenderParameter("language", language);
				actionResponse.setRenderParameter("script", script);
			} else if ("delete".equals(cmd)) {
				String scriptname = (String) actionRequest.getAttribute("savedscript");
				if (scriptname == null) {
					actionResponse.setRenderParameter("script_trace", "No script specified to delete!");
					SessionErrors.add(actionRequest, "error");
					return;
				}
				_log.info("Deleting saved script: " + scriptname);
				PortletPreferences prefs = actionRequest.getPreferences();
				prefs.reset("savedscript." + scriptname);
				prefs.reset("lang." + scriptname);
				prefs.store();
			} else if ("import".equals(cmd)) {
				if (fileUploaded == null) {
					actionResponse.setRenderParameter("script_trace", "No file was uploaded for import!");
					SessionErrors.add(actionRequest, "error");
					return;
				}

				StringBuilder output = new StringBuilder();

				InputStream instream = fileUploaded.getInputStream();
				ZipInputStream zipstream = null;
				try {
					zipstream = new ZipInputStream(instream);
					ZipEntry entry = zipstream.getNextEntry();
					while (entry != null) {
						String filename = entry.getName();
						if (filename.contains("/")) {
							int qs = filename.lastIndexOf("/");
							if (qs != -1) {
								filename = filename.substring(qs + 1);
							}
						}
						if (filename.contains("\\")) {
							int qs = filename.lastIndexOf("\\");
							if (qs != -1) {
								filename = filename.substring(qs + 1);
							}
						}

						String ext = StringPool.BLANK;
						if (filename.length() > 0) {
							int qs = filename.lastIndexOf(".");
							if (qs > 0) {
								ext = filename.substring(qs + 1);
								filename = filename.substring(0, qs);
							}
						}

						String lang = resolveLanguage(ext);
						String imscript = getStreamAsString(zipstream, "utf-8", false);

						if (imscript != null && imscript.length() > 0) {
							_log.info("Importing script \"" + filename + "\" of type " + lang);
							output.append("Importing script \"" + filename + "\" of type " + lang + "\n");

							PortletPreferences prefs = actionRequest.getPreferences();
							prefs.setValue("savedscript." + filename, imscript);
							prefs.setValue("lang." + filename, lang);
							prefs.store();
						}

						entry = zipstream.getNextEntry();
					}

					actionResponse.setRenderParameter("script_output", output.toString());
				} finally {
					try {
						if (zipstream != null) {
							zipstream.close();
						}
					} catch (Exception e) {
					}
					try {
						if (instream != null) {
							instream.close();
						}
					} catch (Exception e) {
					}
				}

				_log.info(fileUploaded.getName());
			}
			SessionMessages.add(actionRequest, "success");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.close();
			actionResponse.setRenderParameter("script_trace", sw.toString());
			_log.error(e);
			SessionErrors.add(actionRequest, e.toString());
		}
	}

	private static String getStreamAsString(InputStream is, String encoding, boolean closeStream) throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int read = is.read(buf);
			while (read != -1) {
				baos.write(buf, 0, read);
				read = is.read(buf);
			}
			if (encoding == null) {
				return new String(baos.toByteArray());
			} else {
				return new String(baos.toByteArray(), Charset.forName(encoding));
			}
		} finally {
			if (closeStream) {
				try {
					if (is != null) {
						is.close();
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public static void sCheckPermissions(PortletRequest request) throws Exception {
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

		if (!permissionChecker.isOmniadmin() && !permissionChecker.isCompanyAdmin()) {
			throw new Exception("No permissions to execute this request");
		}
	}

}
