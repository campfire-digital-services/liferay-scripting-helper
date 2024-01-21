
package au.com.permeance.utility.scriptinghelper.portlets;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.portal.kernel.model.Portlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet.PORTLET_ID;
import static com.liferay.application.list.constants.PanelCategoryKeys.CONTROL_PANEL_CONFIGURATION;

@Component(
	immediate = true,
	property = {
		"panel.category.key=" + CONTROL_PANEL_CONFIGURATION,
		"service.ranking:Integer=100"
	},
	service = PanelApp.class
)
public class ScriptingHelperPanelApp extends BasePanelApp {

	@Override
	public Portlet getPortlet() {
		return _portlet;
	}
	@Override
	public String getPortletId() {
		return PORTLET_ID;
	}

	@Reference(
			target = "(javax.portlet.name=" + PORTLET_ID + ")"
	)
	private Portlet _portlet;

}