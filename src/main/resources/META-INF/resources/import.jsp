<%@ include file="init.jsp" %>

<portlet:resourceURL var="resourceUrl" />

<portlet:renderURL var="redirectURL">
	<portlet:param name="mvcPath" value="/import.jsp" />
</portlet:renderURL>

<liferay-portlet:actionURL name="/scripting/import" var="importGroovyURL">
	<portlet:param name="mvcPath" value="/import.jsp" />
</liferay-portlet:actionURL>

<liferay-ui:success
	key="success"
	message="ui-request-processed-successfully"
/>

<liferay-ui:error
	key="error"
	message="ui-request-processed-error"
/>

<aui:form action="${importGroovyURL}" enctype="multipart/form-data" method="post" name="fm">
	<aui:input name="redirect" type="hidden" value="<%= redirectURL %>" />

	<aui:input label="import-zip-file" name="importfile" style="width: auto;" type="file">
	</aui:input>

	<aui:button onClick='<%= "return " + renderResponse.getNamespace() + "import();" %>' primary="false" type="submit" value="import-zip" />
</aui:form>

<aui:script>
function <portlet:namespace />import() {
var filename = document.<portlet:namespace />fm.<portlet:namespace />importfile.value;
if (filename == null || filename == '') {
	alert("<liferay-ui:message key="please-select-a-zip-file-to-upload" />");
		return false;
	}
	if (confirm('<liferay-ui:message key="import-scripts-from-file?-this-will-overwrite-any-existing-scripts-with-the-same-names" />')) {
	document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'import';
	return true;
	}
else {
		return false;
	}
}

</aui:script>