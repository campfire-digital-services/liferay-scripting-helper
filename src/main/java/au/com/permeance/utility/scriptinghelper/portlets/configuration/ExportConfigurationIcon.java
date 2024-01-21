package au.com.permeance.utility.scriptinghelper.portlets.configuration;

import au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.configuration.icon.BasePortletConfigurationIcon;
import com.liferay.portal.kernel.portlet.configuration.icon.PortletConfigurationIcon;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.ResourceBundle;

@Component(
	immediate = true,
	property = "javax.portlet.name=" + ScriptingHelperPortlet.PORTLET_ID,
	service = PortletConfigurationIcon.class
)
public class ExportConfigurationIcon extends BasePortletConfigurationIcon {

	@Override
	public String getMessage(PortletRequest portletRequest) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", getLocale(portletRequest), getClass());

		return _language.get(resourceBundle, "export-all-as-zip");
	}

	@Override
	public String getURL(
		PortletRequest portletRequest, PortletResponse portletResponse) {

		return "/group/control_panel/manage?p_p_id=au_com_permeance_utility_scriptinghelper_portlets_ScriptingHelperPortlet&p_p_lifecycle=2&p_p_state=maximized&p_p_mode=view&p_p_cacheability=cacheLevelPage";
	}

	@Override
	public double getWeight() {
		return 101;
	}

	@Override
	public boolean isShow(PortletRequest portletRequest) {
		return true;
	}

	@Reference
	private Language _language;

}