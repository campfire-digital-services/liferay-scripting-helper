package au.com.permeance.utility.scriptinghelper.socket;

import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author acbenaissi
 */
@Component(
	immediate = true,
	property = "org.osgi.http.websocket.endpoint.path=/o/websocket/backgroundtask-log",
	service = Endpoint.class
)
public class BackgroundTasksSocket extends Endpoint {

	public static ConcurrentHashMap<Long, StringBuilder> outputMap =
		new ConcurrentHashMap<>();

	public static void addToOutputMap(long taskId, String message) {
		outputMap.computeIfAbsent(
			taskId, k -> new StringBuilder()
		).append(
			message
		);
		broadcastToTaskSessions(taskId, message);
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		sessions.remove(session);
		sessionTaskMap.remove(session);
	}

	@Override
	public void onOpen(final Session session, EndpointConfig endpointConfig) {

		URI requestURI = session.getRequestURI();

		long backgroundTaskId = GetterUtil.getLong(
			HttpComponentsUtil.getParameter(
				requestURI.toString(), "backgroundTaskId"));
		sessionTaskMap.put(session, backgroundTaskId);
		sessions.add(session);
		String existingOutput = outputMap.getOrDefault(
			backgroundTaskId, new StringBuilder()
		).toString();

		try {
			session.getBasicRemote(
			).sendText(
				existingOutput
			);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Activate
	@Modified
	protected void activate() {
		try {
			long companyId = PortalUtil.getDefaultCompanyId();
			long userId = _userLocalService.getGuestUserId(companyId);
			String objectName = "TaskLog";

			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.fetchObjectDefinition(
					companyId, "C_" + objectName);

			if (objectDefinition == null) {
				objectDefinition =
					_objectDefinitionLocalService.addCustomObjectDefinition(
							userId, 0, false, false,
							LocalizedMapUtil.getLocalizedMap(objectName),
							objectName, null, null,
							LocalizedMapUtil.getLocalizedMap(objectName),
							true, ObjectDefinitionConstants.SCOPE_COMPANY,
							ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT,
							Arrays.asList(
									ObjectFieldUtil.createObjectField(
											ObjectFieldConstants.BUSINESS_TYPE_LONG_INTEGER,
											ObjectFieldConstants.DB_TYPE_LONG,
											"backgroundTaskId"),
									ObjectFieldUtil.createObjectField(
											ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT,
											ObjectFieldConstants.DB_TYPE_STRING,
											"message"),
									ObjectFieldUtil.createObjectField(
											ObjectFieldConstants.BUSINESS_TYPE_TEXT,
											ObjectFieldConstants.DB_TYPE_STRING,
											"scriptname")

							)

					);

				_objectDefinitionLocalService.publishCustomObjectDefinition(
					userId, objectDefinition.getObjectDefinitionId());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void broadcastToTaskSessions(long taskId, String message) {
		for (Session session : sessions) {
			Long associatedTaskId = sessionTaskMap.get(session);

			if ((associatedTaskId != null) && (associatedTaskId == taskId)) {
				try {
					session.getBasicRemote(
					).sendText(
						message
					);
				}
				catch (IOException ioe) {

				}
			}
		}
	}

	private void addField(
			ObjectDefinition objectDefinition, String name, String businessType,
			String dbType)
		throws Exception {

		_objectFieldLocalService.addCustomObjectField(
				name,objectDefinition.getUserId(),0,objectDefinition.getObjectDefinitionId(), businessType, dbType,false, false, null,
				LocalizedMapUtil.getLocalizedMap(name), false, name, null, null, false, false, Collections.emptyList());

	}

	private static final CopyOnWriteArrayList<Session> sessions =
		new CopyOnWriteArrayList<>();
	private static ConcurrentHashMap<Session, Long> sessionTaskMap =
		new ConcurrentHashMap<>();

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private UserLocalService _userLocalService;

}