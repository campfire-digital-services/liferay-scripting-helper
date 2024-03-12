package au.com.permeance.utility.scriptinghelper.portlets.actions;

import au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.PortalUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.osgi.service.component.annotations.Component;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ScriptingHelperPortlet.PORTLET_ID,
		"mvc.command.name=/scripting/import"
	},
	service = MVCActionCommand.class
)
public class ImportZipGroovyMVCActionCommand extends BaseMVCActionCommand {

	Log _log = LogFactoryUtil.getLog(ImportZipGroovyMVCActionCommand.class);

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
					actionRequest.setAttribute(
						fi.getFieldName(
						).substring(
							portletId.length()
						),
						fi.getString());
				}
			}
			else {
				fileUploaded = fi;
			}
		}

		if (fileUploaded == null) {
			actionRequest.setAttribute(
				"script_trace", "No file was uploaded for import!");
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

				if ((imscript != null) && (imscript.length() > 0)) {
					_log.info(
						"Importing script \"" + filename + "\" of type " +
							lang);
					output.append(
						"Importing script \"" + filename + "\" of type " +
							lang + "\n");

					PortletPreferences prefs = actionRequest.getPreferences();

					prefs.setValue("savedscript." + filename, imscript);
					prefs.setValue("lang." + filename, lang);
					prefs.store();
				}

				entry = zipstream.getNextEntry();
			}

			SessionMessages.add(actionRequest, "success");
		}
		catch (Exception e) {
			_log.error(e);
			SessionErrors.add(actionRequest, "error");
		}

		actionResponse.setRenderParameter("mvcPath", "/import.jsp");
	}

	private static String getStreamAsString(
			InputStream is, String encoding, boolean closeStream)
		throws IOException {

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
			}

			return new String(baos.toByteArray(), Charset.forName(encoding));
		}
		finally {
			if (closeStream) {
				try {
					if (is != null) {
						is.close();
					}
				}
				catch (Exception e) {
				}
			}
		}
	}

	private static String defaultLanguage = null;

}