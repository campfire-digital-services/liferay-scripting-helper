<%@ include file="init.jsp" %>
<%@ page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.JSPNavigationItemList" %>

<%@ page import="com.liferay.object.model.ObjectDefinition" %><%@
page import="com.liferay.object.model.ObjectEntry" %><%@
page import="com.liferay.object.service.ObjectDefinitionLocalServiceUtil" %><%@
page import="com.liferay.object.service.ObjectEntryLocalServiceUtil" %><%@
page import="com.liferay.portal.kernel.language.LanguageUtil" %><%@
page import="com.liferay.portal.kernel.util.GetterUtil" %><%@
page import="java.util.List" %>

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
						navigationItem.setActive(false);
						navigationItem.setHref(renderResponse.createRenderURL(), "mvcPath", "/backgroundtasks.jsp");
						navigationItem.setLabel(LanguageUtil.get(httpServletRequest, "in-progress"));
					});
				add(
					navigationItem -> {
						navigationItem.setActive(true);
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
					ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(themeDisplay.getCompanyId(), "C_TaskLog");

					if (objectDefinition != null) {
						List<ObjectEntry> objectEntries = ObjectEntryLocalServiceUtil.getObjectEntries(0,objectDefinition.getObjectDefinitionId(),
						 -1, -1);
						for (ObjectEntry objectEntry : objectEntries) {


							String logs = GetterUtil.getString(objectEntry.getValues().get("message"));
							String scriptname = GetterUtil.getString(objectEntry.getValues().get("scriptname"));
				%>

				<liferay-frontend:fieldset
					collapsed="<%= true %>"
					collapsible="<%= true %>"
					label="<%=scriptname%>"
				>
					<pre class="alert-secondary" style="max-height: 500px;">
						<%= logs %>
					</pre>
				</liferay-frontend:fieldset>

				<%

						}
					}
				%>

			</div>
			</clay:col>
		</clay:row>
	</clay:container-fluid>
</aui:form>