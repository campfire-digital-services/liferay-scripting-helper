<%@ include file="init.jsp" %>
<%@ page import="au.com.permeance.utility.scriptinghelper.backgroundtask.GroovyBackgroundTaskExecutor" %>

<%@ page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.JSPNavigationItemList" %><%@
page import="com.liferay.portal.kernel.backgroundtask.BackgroundTask" %><%@
page import="com.liferay.portal.kernel.backgroundtask.BackgroundTaskManagerUtil" %><%@
page import="com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants" %><%@
page import="com.liferay.portal.kernel.language.LanguageUtil" %>

<%@ page import="java.util.List" %>

<portlet:defineObjects />

<portlet:actionURL name="execute" var="actionUrl" />
<portlet:resourceURL var="resourceUrl" />

<portlet:renderURL var="redirectURL">
</portlet:renderURL>

<liferay-ui:success
	key="success"
	message="ui-request-processed-successfully"
/>

<liferay-ui:error
	key="error"
	message="ui-request-processed-error"
/>

<clay:navigation-bar
		navigationItems='<%=
		new JSPNavigationItemList(pageContext) {
			{
				add(
					navigationItem -> {
						navigationItem.setActive(false);
						navigationItem.setHref(renderResponse.createRenderURL());
						navigationItem.setLabel(LanguageUtil.get(httpServletRequest, "scripts"));
					});
				add(
					navigationItem -> {
						navigationItem.setActive(true);
						navigationItem.setHref(renderResponse.createRenderURL(), "navigation", "backgroundtasks");
						navigationItem.setLabel(LanguageUtil.get(httpServletRequest, "backgroundtasks"));
					});
			}
		}
	%>'
/>

<clay:navigation-bar
		navigationItems='<%=
		new JSPNavigationItemList(pageContext) {
			{
				add(
					navigationItem -> {
						navigationItem.setActive(true);
						navigationItem.setHref(renderResponse.createRenderURL(), "mvcPath", "/backgroundtasks.jsp");
						navigationItem.setLabel(LanguageUtil.get(httpServletRequest, "in-progress"));
					});
				add(
					navigationItem -> {
						navigationItem.setActive(false);
						navigationItem.setHref(renderResponse.createRenderURL(), "mvcPath", "/backgroundtasks-finish.jsp");
						navigationItem.setLabel(LanguageUtil.get(httpServletRequest, "finished"));
					});
			}
		}
	%>'
/>

<aui:form action="${actionUrl}" enctype="multipart/form-data" method="post" name="fm">
	<aui:input name="redirect" type="hidden" value="<%= redirectURL %>" />

	<clay:container-fluid
		cssClass="container-fluid-max-xl container-form-lg search-admin-index-actions-container"
	>
		<clay:row>
			<clay:col
				size="12"
			>
			<div class="panel">

				<%
					List<BackgroundTask> backgroundTasks = BackgroundTaskManagerUtil.getBackgroundTasks(GroovyBackgroundTaskExecutor.class.getName(), BackgroundTaskConstants.STATUS_IN_PROGRESS);

					for (BackgroundTask backgroundTask : backgroundTasks) {
				%>

				<liferay-frontend:fieldset
					collapsed="<%= true %>"
					collapsible="<%= true %>"
					label="<%= backgroundTask.getName() %>"
				>
					<pre class="alert-secondary log-socket" data-bg-task-id="<%= backgroundTask.getBackgroundTaskId() %>" id="log-<%= backgroundTask.getBackgroundTaskId() %>" style="max-height: 500px;">

					</pre>

				</liferay-frontend:fieldset>

				<%
					}
				%>

			</div>
			</clay:col>
		</clay:row>
	</clay:container-fluid>
</aui:form>

<script type="text/javascript">
	var logSocket = document.getElementsByClassName('log-socket');

	if (logSocket.length > 0) {
		Array.from(logSocket).forEach(getBackgroundTaskLog)
	}

	function getBackgroundTaskLog(item, index, arr) {
		var backgroundTaskId = item.getAttribute("data-bg-task-id");
		var wsUrl = "ws://"+location.host+"/o/websocket/backgroundtask-log?backgroundTaskId=" + backgroundTaskId;
		var webSocket = new WebSocket(wsUrl);
		var log = item;

		webSocket.onopen = function() {
			log.innerHTML += "Connected ...\n";
		};

		webSocket.onmessage = function(event) {
			log.innerHTML += event.data + "\n";
			log.scrollTop = log.scrollHeight;
		};

		webSocket.onclose = function() {
			log.innerHTML += "Disconnected ...\n";
		};

		webSocket.onerror = function(error) {
			log.innerHTML += "Error ...\n";
		};
	}
</script>