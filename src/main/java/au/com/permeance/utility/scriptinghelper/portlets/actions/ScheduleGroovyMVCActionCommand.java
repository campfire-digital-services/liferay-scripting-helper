package au.com.permeance.utility.scriptinghelper.portlets.actions;

import au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet;
import com.liferay.calendar.util.JCalendarUtil;
import com.liferay.dispatch.constants.DispatchConstants;
import com.liferay.dispatch.executor.DispatchTaskClusterMode;
import com.liferay.dispatch.executor.DispatchTaskExecutorRegistry;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.dispatch.service.DispatchTriggerLocalService;
import com.liferay.dispatch.service.DispatchTriggerService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelper;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.Trigger;
import com.liferay.portal.kernel.scheduler.TriggerFactory;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ScriptingHelperPortlet.PORTLET_ID,
		"mvc.command.name=/scripting/schedule"
	},
	service = MVCActionCommand.class
)
public class ScheduleGroovyMVCActionCommand
	extends BaseMVCActionCommand {

	Log _log = LogFactoryUtil.getLog(
		ScheduleGroovyMVCActionCommand.class);

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

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String language = ParamUtil.getString(actionRequest, "language", ScriptingHelperPortlet.getDefaultLanguage());
		String script = ParamUtil.getString(actionRequest, "script");
		String themesel = ParamUtil.getString(actionRequest, "themesel");
		String timeUnit = ParamUtil.getString(actionRequest, "timeUnit");
		java.util.Calendar startTimeJCalendar = _getJCalendar(
				actionRequest, "startDate");
		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getScopeGroupId();

		String titleTask = ParamUtil.getString(
			actionRequest, "bgTaskName", "bgTask");

		titleTask += "_" + System.currentTimeMillis();

		UnicodeProperties dispatchTaskSettingsUnicodeProperties =
				UnicodePropertiesBuilder.create(
						true
				).put("titleTask", titleTask).put("script", script).build();


		DispatchTrigger dispatchTrigger = null;


		String dispatchTaskExecutorType = "groovy.scheduled.job";

		dispatchTrigger = _dispatchTriggerService.addDispatchTrigger(
				null, _portal.getUserId(actionRequest),
				dispatchTaskExecutorType, dispatchTaskSettingsUnicodeProperties,
				titleTask);

		_scheduleDispatchTrigger(actionRequest, dispatchTrigger.getDispatchTriggerId());
	}
	private void _scheduleDispatchTrigger(ActionRequest actionRequest, long dispatchTriggerId)
			throws PortalException {

		boolean active = false;

		DispatchTaskClusterMode dispatchTaskClusterMode =
				_getDispatchTaskClusterMode(
						dispatchTriggerId,
						DispatchTaskClusterMode.valueOf(
								0));
		int endDateMonth = ParamUtil.getInteger(actionRequest, "endDateMonth");
		int endDateDay = ParamUtil.getInteger(actionRequest, "endDateDay");
		int endDateYear = ParamUtil.getInteger(actionRequest, "endDateYear");
		int endDateHour = ParamUtil.getInteger(actionRequest, "endDateHour");
		int endDateMinute = ParamUtil.getInteger(
				actionRequest, "endDateMinute");

		int endDateAmPm = ParamUtil.getInteger(actionRequest, "endDateAmPm");

		if (endDateAmPm == Calendar.PM) {
			endDateHour += 12;
		}

		boolean neverEnd = true;
		boolean overlapAllowed = false;
		int interval = ParamUtil.getInteger(
				actionRequest, "interval");

		int startDateMonth = ParamUtil.getInteger(
				actionRequest, "startDateMonth");
		int startDateDay = ParamUtil.getInteger(actionRequest, "startDateDay");
		int startDateYear = ParamUtil.getInteger(
				actionRequest, "startDateYear");
		int startDateHour = ParamUtil.getInteger(
				actionRequest, "startDateHour");
		int startDateMinute = ParamUtil.getInteger(
				actionRequest, "startDateMinute");

		int startDateAmPm = ParamUtil.getInteger(
				actionRequest, "startDateAmPm");

		if (startDateAmPm == Calendar.PM) {
			startDateHour += 12;
		}

		String timeZoneId = ParamUtil.getString(actionRequest, "timeZoneId");
		java.util.Calendar startTimeJCalendar = _getJCalendar(
				actionRequest, "startDate");
		DispatchTrigger dispatchTrigger = _dispatchTriggerService.updateDispatchTrigger(
				dispatchTriggerId, active, "", dispatchTaskClusterMode,
				endDateMonth, endDateDay, endDateYear, endDateHour, endDateMinute,
				neverEnd, overlapAllowed, startDateMonth, startDateDay,
				startDateYear, startDateHour, startDateMinute, timeZoneId);

		String timeUnit = ParamUtil.getString(actionRequest, "timeUnit");

		Trigger trigger = _triggerFactory.createTrigger(
				_getJobName(dispatchTrigger.getDispatchTriggerId()),
				_getGroupName(dispatchTrigger.getDispatchTriggerId()), startTimeJCalendar.getTime(),
				null, interval, TimeUnit.valueOf(timeUnit.toUpperCase()));

		_schedulerEngineHelper.schedule(
				trigger, dispatchTaskClusterMode.getStorageType(), null,
				DispatchConstants.EXECUTOR_DESTINATION_NAME,
				_getPayload(dispatchTriggerId));


	}
	private DispatchTaskClusterMode _getDispatchTaskClusterMode(
			long dispatchTaskId,
			DispatchTaskClusterMode dispatchTaskClusterMode)
			throws PortalException {

		DispatchTrigger dispatchTrigger =
				_dispatchTriggerLocalService.getDispatchTrigger(dispatchTaskId);

		if (_dispatchTaskExecutorRegistry.isClusterModeSingle(
				dispatchTrigger.getDispatchTaskExecutorType())) {

			return DispatchTaskClusterMode.SINGLE_NODE_PERSISTED;
		}

		return dispatchTaskClusterMode;
	}
	private java.util.Calendar _getJCalendar(
			PortletRequest portletRequest, String name) {

		int hour = ParamUtil.getInteger(portletRequest, name + "Hour");

		if (ParamUtil.getInteger(portletRequest, name + "AmPm") ==
				java.util.Calendar.PM) {

			hour += 12;
		}

		TimeZone timeZone = ParamUtil.getBoolean(portletRequest, "allDay") ?
				TimeZoneUtil.getTimeZone(StringPool.UTC) :
				_getTimeZone(portletRequest);

		return JCalendarUtil.getJCalendar(
				ParamUtil.getInteger(portletRequest, name + "Year"),
				ParamUtil.getInteger(portletRequest, name + "Month"),
				ParamUtil.getInteger(portletRequest, name + "Day"), hour,
				ParamUtil.getInteger(portletRequest, name + "Minute"), 0, 0,
				timeZone);
	}
	private TimeZone _getTimeZone(PortletRequest portletRequest) {
		PortletPreferences preferences = portletRequest.getPreferences();

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		User user = themeDisplay.getUser();

		String timeZoneId = preferences.getValue(
				"timeZoneId", user.getTimeZoneId());

		if (Validator.isNull(timeZoneId)) {
			timeZoneId = user.getTimeZoneId();
		}

		return TimeZone.getTimeZone(timeZoneId);
	}
	private String _getGroupName(long dispatchTriggerId) {
		return String.format("DISPATCH_GROUP_%07d", dispatchTriggerId);
	}

	private String _getJobName(long dispatchTriggerId) {
		return String.format("DISPATCH_JOB_%07d", dispatchTriggerId);
	}

	private String _getPayload(long dispatchTriggerId) {
		return String.format("{\"dispatchTriggerId\": %d}", dispatchTriggerId);
	}
	@Reference
	private SchedulerEngineHelper _schedulerEngineHelper;

	@Reference
	private TriggerFactory _triggerFactory;

	private static String defaultLanguage = null;

	@Reference
	private DispatchTaskExecutorRegistry _dispatchTaskExecutorRegistry;

	@Reference
	private DispatchTriggerLocalService _dispatchTriggerLocalService;

	@Reference
	private DispatchTriggerService _dispatchTriggerService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;
}