package au.com.permeance.utility.scriptinghelper.portlets.actions;

import au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.io.unsync.UnsyncPrintWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.UnsyncPrintWriterPool;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Component;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import java.util.HashMap;
import java.util.Map;

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ScriptingHelperPortlet.PORTLET_ID,
		"mvc.command.name=/scripting/execute"
	},
	service = MVCActionCommand.class
)
public class ExecuteGroovyMVCActionCommande extends BaseMVCActionCommand {

	Log _log = LogFactoryUtil.getLog(ExecuteGroovyMVCActionCommande.class);

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String language = ParamUtil.getString(actionRequest, "language");
		String script = ParamUtil.getString(actionRequest, "script");
		String themesel = ParamUtil.getString(actionRequest, "themesel");

		Map<String, Object> portletObjects = new HashMap<>();
		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		UnsyncPrintWriter unsyncPrintWriter = UnsyncPrintWriterPool.borrow(
			unsyncByteArrayOutputStream);

		portletObjects.put("out", unsyncPrintWriter);
		_log.info("Executing script");
		ScriptingUtil.eval(null, portletObjects, null, language, script);
		unsyncPrintWriter.flush();
		String redirect = ParamUtil.getString(actionRequest, "redirect");

		actionRequest.setAttribute("script", script);
		actionRequest.setAttribute(
			"script_output", unsyncByteArrayOutputStream.toString());
		actionRequest.setAttribute("themesel", themesel);
		actionRequest.setAttribute(WebKeys.REDIRECT, redirect);
		actionResponse.setRenderParameter(
			"script_output", unsyncByteArrayOutputStream.toString());
	}

}