
package au.com.permeance.utility.scriptinghelper.portlets;

import static au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet.PORTLET_ID;
import static com.liferay.application.list.constants.PanelCategoryKeys.CONTROL_PANEL_CONFIGURATION;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.portal.kernel.model.Portlet;

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
	public String getPortletId() {
		return PORTLET_ID;
	}

	@Override
	@Reference(target = "(javax.portlet.name=" + PORTLET_ID + ")", unbind = "-")
	public void setPortlet(final Portlet portlet) {
		super.setPortlet(portlet);
	}
}
