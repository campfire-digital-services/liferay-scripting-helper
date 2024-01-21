<%@ include file="init.jsp" %>
<%@ page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.JSPNavigationItemList" %><%@
page import="com.liferay.portal.kernel.language.LanguageUtil" %><%@
page import="com.liferay.portal.kernel.scripting.ScriptingUtil" %><%@
page import="com.liferay.portal.kernel.util.GetterUtil" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %><%@
page import="com.liferay.portal.kernel.util.TextFormatter" %>

<portlet:actionURL name="execute" var="actionUrl" />
<portlet:resourceURL var="resourceUrl" />

<portlet:renderURL var="redirectURL" />

<liferay-portlet:actionURL copyCurrentRenderParameters="<%= true %>" name="/scripting/execute" var="executeURL" />
<liferay-portlet:actionURL copyCurrentRenderParameters="<%= true %>" name="/scripting/execute/background" var="executeBGURL" />

<%
String language = ParamUtil.getString(renderRequest, "language", "groovy");
String script = GetterUtil.getString(renderRequest.getAttribute("script"), "");

if (script.length() == 0) script = "// ### Groovy Sample ###\n\nnumber = com.liferay.portal.kernel.service.UserLocalServiceUtil.getUsersCount();\n\nout.println(number);";
String scriptOutput = GetterUtil.getString(renderRequest.getAttribute("script_output"), "");
String scriptError = GetterUtil.getString(renderRequest.getAttribute("script_trace"), "");

java.util.List<String> savedscripts = (java.util.List<String>)renderRequest.getAttribute("savedscripts");

String requestSuccess = ParamUtil.getString(renderRequest, "requestsuccess", "none");

String themesel = GetterUtil.getString(renderRequest.getAttribute("themesel"), "vibrant-ink");
String editorheight = GetterUtil.getString(renderRequest.getAttribute("editorheight"), "400");
%>

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
						navigationItem.setActive(true);
						navigationItem.setHref(renderResponse.createRenderURL());
						navigationItem.setLabel(LanguageUtil.get(httpServletRequest, "scripts"));
					});
				add(
					navigationItem -> {
						navigationItem.setActive(false);
						navigationItem.setHref(renderResponse.createRenderURL(), "mvcPath", "/backgroundtasks.jsp");
						navigationItem.setLabel(LanguageUtil.get(httpServletRequest, "backgroundtasks"));
					});
			}
		}
	%>'
/>

<aui:form action="#" enctype="multipart/form-data" method="post" name="fm">
	<aui:input name="redirect" type="hidden" value="<%= redirectURL %>" />
	<aui:input name="cmd" type="hidden" value="noop" />
	<aui:input name="newscriptname" type="hidden" value="" />
	<aui:input name="bgTaskName" type="hidden" value="" />
	<aui:input name="editorheight" type="hidden" value="<%= editorheight %>" />

	<clay:container-fluid
		cssClass="container-fluid-max-xl container-form-lg search-admin-index-actions-container"
	>
		<clay:row>
			<clay:col
				size="4"
			>
				<clay:sheet>
					<aui:fieldset>
						<aui:select label="saved-scripts" name="savedscript" size="10">

							<%
							if (savedscripts != null) {
								for (String savedscript : savedscripts) {
							%>

									<aui:option
										selected="false"
									value="<%= savedscript %>" ><%= savedscript %></aui:option
								>

							<%
								}
							}
							%>

						</aui:select>
					</aui:fieldset>

					<%
					String saveintoButtonScript = "return " + renderResponse.getNamespace() + "saveinto();";
					String loadfromButtonScript = "return " + renderResponse.getNamespace() + "loadfrom();";
					String deleteButtonScript = "return " + renderResponse.getNamespace() + "delete();";
					%>

					<aui:button-row>
						<aui:button onClick="<%= loadfromButtonScript %>" primary="false" type="submit" value="load-from" />
						<aui:button onClick="<%= saveintoButtonScript %>" primary="false" type="submit" value="save-into" />
						<aui:button onClick="<%= deleteButtonScript %>" primary="false" type="submit" value="delete" />
					</aui:button-row>
				</clay:sheet>
			</clay:col>

			<clay:col
				size="8"
			>
				<clay:sheet>
					<aui:fieldset>
						<aui:select name="language" onChange="pickCodeMirrorMode();">

							<%
							for (String supportedLanguage : ScriptingUtil.getSupportedLanguages()) {
							%>

								<aui:option
									label="<%= TextFormatter.format(supportedLanguage, TextFormatter.J) %>"
									selected="<%= supportedLanguage.equals(language) %>"
									value="<%= supportedLanguage %>"
								/>

							<%
							}
							%>

						</aui:select>

						<style type="text/css">
							.CodeMirror {border: 1px solid black; font-size:13px; width: 100%}
						</style>

						<aui:input
							cssClass="lfr-textarea-container"
							id="codearea"
							name="script"
							type="textarea"
							value="<%= script %>"
						/>

						<a href="#" onclick="editorAutoformat(); return false;"><liferay-ui:message key="autoformat" /></a>&nbsp;&nbsp;&nbsp;&nbsp;
						<a href="#" onclick="editorExtendDown(); return false;"><liferay-ui:message key="extend-down" /></a>&nbsp;&nbsp;
						<a href="#" onclick="editorExtendUp(); return false;"><liferay-ui:message key="extend-up" /></a>&nbsp;&nbsp;&nbsp;&nbsp;

						<liferay-ui:message key="editor-theme:" />

						<select id="themesel" name="<portlet:namespace />themesel" onChange="pickCodeMirrorTheme();">
							<option value="default" <%= "default".equals(themesel) ? "selected" : "" %>>default</option>
							<option value="ambiance" <%= "ambiance".equals(themesel) ? "selected" : "" %>>ambiance</option>
							<option value="blackboard" <%= "blackboard".equals(themesel) ? "selected" : "" %>>blackboard</option>
							<option value="cobalt" <%= "cobalt".equals(themesel) ? "selected" : "" %>>cobalt</option>
							<option value="eclipse" <%= "eclipse".equals(themesel) ? "selected" : "" %>>eclipse</option>
							<option value="elegant" <%= "elegant".equals(themesel) ? "selected" : "" %>>elegant</option>
							<option value="erlang-dark" <%= "erlang-dark".equals(themesel) ? "selected" : "" %>>erlang-dark</option>
							<option value="lesser-dark" <%= "lesser-dark".equals(themesel) ? "selected" : "" %>>lesser-dark</option>
							<option value="monokai" <%= "monokai".equals(themesel) ? "selected" : "" %>>monokai</option>
							<option value="neat" <%= "neat".equals(themesel) ? "selected" : "" %>>neat</option>
							<option value="night" <%= "night".equals(themesel) ? "selected" : "" %>>night</option>
							<option value="rubyblue" <%= "rubyblue".equals(themesel) ? "selected" : "" %>>rubyblue</option>
							<option value="vibrant-ink" <%= "vibrant-ink".equals(themesel) ? "selected" : "" %>>vibrant-ink</option>
							<option value="xq-dark" <%= "xq-dark".equals(themesel) ? "selected" : "" %>>xq-dark</option>
						</select>
					</aui:fieldset>

					<aui:button-row>

						<%
						String executeButtonScript = "return " + renderResponse.getNamespace() + "execute();";
						String executeBGButtonScript = "return " + renderResponse.getNamespace() + "executeBG();";
						String saveButtonScript = "return " + renderResponse.getNamespace() + "save();";
						%>

						<aui:button onClick="<%= executeButtonScript %>" type="submit" value="execute" />
						<aui:button onClick="<%= executeBGButtonScript %>" primary="false" type="submit" value="background" />
						<aui:button onClick="<%= saveButtonScript %>" primary="false" type="submit" value="save" />
					</aui:button-row>

					<b><liferay-ui:message key="output:" /></b>

					<pre><c:out value="<%= scriptOutput %>" /></pre>

					<br />
					<br />

					<%
					if (scriptError.length() > 0) {
					%>

						<b><liferay-ui:message key="error:" /></b>

						<pre><c:out value="<%= scriptError %>" /></pre>

						<br />
						<br />

					<%
					}
					%>

				</clay:sheet>
			</clay:col>
		</clay:row>
	</clay:container-fluid>
</aui:form>

<aui:script>
	function <portlet:namespace />saveinto() {
		var scriptname = document.<portlet:namespace />fm.<portlet:namespace />savedscript.value;
		if (scriptname == null || scriptname == '') {
			scriptname = prompt('<liferay-ui:message key="enter-new-script-name:" />', '');
			if (scriptname != null) {
				document.<portlet:namespace />fm.action = "${actionUrl}";
				document.<portlet:namespace />fm.<portlet:namespace />newscriptname.value = scriptname;
				document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'save';
				return true;
			}
			else {
				return false;
			}
		}
		if (confirm('<liferay-ui:message key="overwrite-existing-saved-script" /> \'' + scriptname + '\'?')) {
			document.<portlet:namespace />fm.action = "${actionUrl}";
			document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'saveinto';
			return true;
		}
		else {
			return false;
		}
	}

	function <portlet:namespace />loadfrom() {
		var scriptname = document.<portlet:namespace />fm.<portlet:namespace />savedscript.value;
		if (scriptname == null || scriptname == '') {
			alert('<liferay-ui:message key="please-select-saved-script-to-load-from" />');
			return false;
		}
		document.<portlet:namespace />fm.action = "${actionUrl}";
		document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'loadfrom';
		return true;

	}

	function <portlet:namespace />delete() {
		var scriptname = document.<portlet:namespace />fm.<portlet:namespace />savedscript.value;
		if (scriptname == null || scriptname == '') {
			alert('<liferay-ui:message key="please-select-saved-script-to-delete" />');
			return false;
		}
		if (confirm('<liferay-ui:message key="delete-saved-script" /> \'' + scriptname + '\'?')) {
			document.<portlet:namespace />fm.action = "${actionUrl}";
			document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'delete';
			return true;
		} else {
			return false;
		}
	}

	function <portlet:namespace />execute() {
		document.<portlet:namespace />fm.action = "${executeURL}";
	}

	function <portlet:namespace />executeBG() {
		bgTaskName = prompt('<liferay-ui:message key="enter-new-script-name:" />', '');
		if (bgTaskName != null) {
			document.<portlet:namespace />fm.<portlet:namespace />bgTaskName.value = bgTaskName;
			document.<portlet:namespace />fm.action = "${executeBGURL}";
			return true;
		}else {
			return false;
		}
	}

	function <portlet:namespace />save() {
		var scriptname = prompt('<liferay-ui:message key="enter-new-script-name:" />', '');
		if (scriptname != null) {
			document.<portlet:namespace />fm.action = "${actionUrl}";
			document.<portlet:namespace />fm.<portlet:namespace />newscriptname.value = scriptname;
			document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'save';
			return true;
		}
		else {
			return false;
		}
	}
</aui:script>

<script type="text/javascript">

	var editor = CodeMirror.fromTextArea(document.getElementById('<%= renderResponse.getNamespace() %>codearea'), {
		tabSize: 3,
		lineWrapping: true,
		lineNumbers: true
	});

	function pickCodeMirrorMode() {
		var lang = document.<portlet:namespace />fm.<portlet:namespace />language.value;
		var sellang = 'clike';
		if (lang == 'javascript') sellang = 'javascript';
		if (lang == 'beanshell') sellang = 'clike';
		if (lang == 'groovy') sellang = 'groovy';
		if (lang == 'python') sellang = 'python';
		if (lang == 'ruby') sellang = 'ruby';
		editor.setOption('mode', sellang);
	}

	function pickCodeMirrorTheme() {
		var theme = document.getElementById('themesel').value;
		editor.setOption('theme', theme);
	}

	var editorheight = <%= editorheight %>;
	editor.getScrollerElement().style.height=editorheight + 'px';
	//editor.getScrollerElement().style.width='500px';
	pickCodeMirrorMode();

	function editorAutoformat() {
		CodeMirror.commands["selectAll"](editor);
		editor.autoFormatRange(editor.getCursor(true), editor.getCursor(false));
	}

	function editorExtendDown() {
		editorheight = editorheight + 200;
		editor.getScrollerElement().style.height=editorheight + 'px';
		document.<portlet:namespace />fm.<portlet:namespace />editorheight.value = editorheight;
	}
	function editorExtendUp() {
		editorheight = editorheight - 200;
		if (editorheight < 200) editorheight = 200;
		editor.getScrollerElement().style.height=editorheight + 'px';
		document.<portlet:namespace />fm.<portlet:namespace />editorheight.value = editorheight;
	}

	pickCodeMirrorTheme();

</script>