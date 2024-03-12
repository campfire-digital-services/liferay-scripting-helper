package au.com.permeance.utility.scriptinghelper.backgroundtask;

import au.com.permeance.utility.scriptinghelper.socket.BackgroundTasksSocket;
import au.com.permeance.utility.scriptinghelper.socket.WebSocketOutputStream;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.PortalUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author acbenaissi
 */
@Component(
	immediate = true,
	property = "background.task.executor.class.name=au.com.permeance.utility.scriptinghelper.backgroundtask.GroovyBackgroundTaskExecutor",
	service = BackgroundTaskExecutor.class
)
public class GroovyBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		String groovyScript = backgroundTask.getTaskContextMap(
		).get(
			"script"
		).toString();
		Map<String, Object> portletObjects = new HashMap<>();
		String language = backgroundTask.getTaskContextMap(
		).get(
			"language"
		).toString();

		long backgroundTaskId = backgroundTask.getBackgroundTaskId();
		WebSocketOutputStream webSocketOutputStream = new WebSocketOutputStream(
			backgroundTaskId);

		PrintStream groovyOut = new PrintStream(
			webSocketOutputStream, true, "UTF-8");

		portletObjects.put("out", groovyOut);

		ScriptingUtil.eval(null, portletObjects, null, language, groovyScript);

		String existingOutput = BackgroundTasksSocket.outputMap.getOrDefault(
			backgroundTaskId, new StringBuilder()
		).toString();

		saveTaskLog(
			backgroundTask.getUserId(), backgroundTaskId, existingOutput, backgroundTask.getName());

		return BackgroundTaskResult.SUCCESS;
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return null;
	}

	private void saveTaskLog(long userId, long backgroundTaskId, String logs, String scriptName) {
		try {
			long companyId = PortalUtil.getDefaultCompanyId();

			String objectName = "TaskLog";

			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.fetchObjectDefinition(
					companyId, "C_" + objectName);

			if (objectDefinition != null) {
				Map<String, Serializable> properties = new HashMap<>();
				properties.put("backgroundTaskId", backgroundTaskId);
				properties.put("scriptname", scriptName);
				properties.put("message", logs);

				ServiceContext serviceContext = new ServiceContext();

				serviceContext.setCompanyId(companyId);
				serviceContext.setScopeGroupId(0);
				_objectEntryLocalService.addObjectEntry(
					userId, 0, objectDefinition.getObjectDefinitionId(),
					properties, serviceContext);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}