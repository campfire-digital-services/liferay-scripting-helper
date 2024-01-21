package au.com.permeance.utility.scriptinghelper.portlets.configuration;

import au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.configuration.icon.BasePortletConfigurationIcon;
import com.liferay.portal.kernel.portlet.configuration.icon.PortletConfigurationIcon;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
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
public class ImportConfigurationIcon extends BasePortletConfigurationIcon {

	@Override
	public String getMessage(PortletRequest portletRequest) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", getLocale(portletRequest), getClass());

		return _language.get(resourceBundle, "import-zip-file");
	}

	@Override
	public String getURL(
		PortletRequest portletRequest, PortletResponse portletResponse) {

		return "javascript:"+StringBundler.concat(
				"Liferay.Util.openModal({onClose: function(event){",
				"window.location.reload();}, title: '", getMessage(portletRequest),
				"', url: '",
				PortletURLBuilder.create(
						PortalUtil.getControlPanelPortletURL(
								portletRequest, ScriptingHelperPortlet.PORTLET_ID,
								PortletRequest.RENDER_PHASE)
				).setMVCPath(
						"/import.jsp"
				).setWindowState(
						LiferayWindowState.POP_UP
				).buildString(),
				"'});");
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