<%-- 
/**
 * Copyright (C) 2013-2021 Campfire Digital Services
 * (formerly Permeance Technologies)
 * 
 * This file is part of the Liferay Scripting Helper.
 * 
 * Scripting Helper is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Scripting Helper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Scripting Helper. If
 * not, see <http://www.gnu.org/licenses/>.
 */
--%>

<%@ include file="init.jsp"%>
<%@ page import="com.liferay.portal.kernel.scripting.ScriptingUtil" %>
<%@ page import="com.liferay.portal.kernel.util.TextFormatter" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>

<div class="container-fluid-1280">

	<portlet:actionURL name="execute" var="actionUrl" />
	<portlet:resourceURL var="resourceUrl" />
	
	<%
	    String language = ParamUtil.getString(renderRequest, "language", "groovy");
	    String script = ParamUtil.getString(renderRequest, "script", "");
	    if(script.length() == 0) script = "// ### Groovy Sample ###\n\nnumber = com.liferay.portal.kernel.service.UserLocalServiceUtil.getUsersCount();\n\nout.println(number);";
		String scriptOutput = ParamUtil.getString(renderRequest, "script_output", "");
		String scriptError = ParamUtil.getString(renderRequest, "script_trace", "");
		
		java.util.List<String> savedscripts = (java.util.List<String>)renderRequest.getAttribute("savedscripts");
	
		String requestSuccess = ParamUtil.getString(renderRequest, "requestsuccess", "none");
	
	    String themesel = ParamUtil.getString(renderRequest, "themesel", "vibrant-ink");
	    String editorheight = ParamUtil.getString(renderRequest, "editorheight", "400");
			
		java.util.List<String> cmThemes = new java.util.ArrayList<String>();
		cmThemes.add("default");
		cmThemes.add("ambiance");
		cmThemes.add("blackboard");
		cmThemes.add("cobalt");
		cmThemes.add("eclipse");
		cmThemes.add("elegant");
		cmThemes.add("erlang-dark");
		cmThemes.add("lesser-dark");
		cmThemes.add("monokai");
		cmThemes.add("neat");
		cmThemes.add("night");
		cmThemes.add("rubyblue");
		cmThemes.add("vibrant-ink");
		cmThemes.add("xq-dark");
		
	%>
	<liferay-ui:success key="success"
		message="ui-request-processed-successfully" />
	<liferay-ui:error key="error"
		message="ui-request-processed-error" />
	
	<aui:form action="${actionUrl}" enctype="multipart/form-data" method="post" name="fm">
	<div class="card-horizontal main-content-card">
		<div class="panel-group">
			<div class="panel-body">
				<table border="0">
				<tr><td align="left" valign="top" style="width:*">
					<aui:input type="hidden" name="cmd" value="noop" />
					<aui:input type="hidden" name="newscriptname" value="" />
					<aui:input type="hidden" name="editorheight" value="<%=editorheight %>" />
					<aui:fieldset>
						<aui:select name="language" onChange='pickCodeMirrorMode();'>
				
							<%
							    for (String supportedLanguage : ScriptingUtil.getSupportedLanguages()) {
							%>
				
							<aui:option
								label="<%= TextFormatter.format(supportedLanguage, TextFormatter.J) %>"
								selected="<%= supportedLanguage.equals(language) %>"
								value="<%= supportedLanguage %>" />
				
							<%
							    }
							%>
						</aui:select>
				
				    <style type="text/css">
				      .CodeMirror {border: 1px solid black; font-size:13px; width: 100%}
				    </style>
				
						<aui:input id="codearea" cssClass="lfr-textarea-container" name="script"
							type="textarea" value="<%= script %>" />
							
							<script type="text/javascript">
							var editor = CodeMirror.fromTextArea(document.getElementById('<%=renderResponse.getNamespace() %>codearea'), {
							    tabSize: 3,
							    lineWrapping: true,
							    lineNumbers: true
							});
							
							function pickCodeMirrorMode() {
							    var lang = document.<portlet:namespace />fm.<portlet:namespace />language.value;
							    var sellang = 'clike';
							    if(lang == 'javascript') sellang = 'javascript';
							    if(lang == 'beanshell') sellang = 'clike';
							    if(lang == 'groovy') sellang = 'groovy';
							    if(lang == 'python') sellang = 'python';
							    if(lang == 'ruby') sellang = 'ruby';
								editor.setOption('mode', sellang);
							}
				
							function pickCodeMirrorTheme() {
							    var theme = document.<portlet:namespace />fm.<portlet:namespace />themesel.value;
								editor.setOption('theme', theme);
							}
							
							
							var editorheight = <%=editorheight %>;
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
								if(editorheight < 200) editorheight = 200;
								editor.getScrollerElement().style.height=editorheight + 'px';
								document.<portlet:namespace />fm.<portlet:namespace />editorheight.value = editorheight;
							}
							</script>
							
							<a href="#" onclick="editorAutoformat(); return false;"><liferay-ui:message key="autoformat"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
							<a href="#" onclick="editorExtendDown(); return false;"><liferay-ui:message key="extend-down"/></a>&nbsp;&nbsp;
							<a href="#" onclick="editorExtendUp(); return false;"><liferay-ui:message key="extend-up"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
							
							<liferay-ui:message key="editor-theme:"/> <select name="<portlet:namespace />themesel" onChange='pickCodeMirrorTheme();'>
							<%
							    for (String cmTheme : cmThemes) {
							%>			
							<option <% if(themesel.equals(cmTheme)) { %>selected<% } %>
								value="<%=cmTheme %>" ><%=cmTheme %></option>
							<%
							    }
							%>				
							
							</select>
							<script type="text/javascript">
							pickCodeMirrorTheme();
							</script>
							
					</aui:fieldset>
				
					<aui:button-row>
				<%
				String executeButtonScript = "return " + renderResponse.getNamespace() + "execute();";
				String saveButtonScript = "return " + renderResponse.getNamespace() + "save();";
				%>	
					
						<aui:button onClick="<%= executeButtonScript %>" type="submit" value="execute"/>
						<aui:button onClick="<%= saveButtonScript %>" type="submit" value="save" primary="false"/>
					</aui:button-row>
				</td>
				<td align="left" valign="top" width="35px">
				&nbsp;&nbsp;&nbsp;
				</td>	
				<td align="left" valign="top">
				
				<aui:fieldset>
						<aui:select name="savedscript" size="10" label="saved-scripts">
				
							<%
								if(savedscripts != null) {
							    for (String savedscript : savedscripts) {
							%>
				
							<aui:option
								selected="false"
								value="<%= savedscript %>" ><%= savedscript %></aui:option>
				
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
				String importButtonScript = "return " + renderResponse.getNamespace() + "import();";
				%>	
				
					<aui:button-row>
						<aui:button onClick="<%= loadfromButtonScript %>" type="submit" value="load-from" primary="false"/>
						<aui:button onClick="<%= saveintoButtonScript %>" type="submit" value="save-into" primary="false"/>
						<aui:button onClick="<%= deleteButtonScript %>" type="submit" value="delete" primary="false"/>
					</aui:button-row>
					<BR/>
					<b><liferay-ui:message key="export/import"/></b><BR/>
					<aui:button-row>
						<aui:button onClick="self.location = '${resourceUrl}';" value="export-all-as-zip"  />
					</aui:button-row>
					<aui:button-row>
						<aui:input name="importfile" type="file" style="width: auto;" label="import-zip-file">
						</aui:input>		
						<aui:button onClick="<%= importButtonScript %>" type="submit" value="import-zip" primary="false" />
					</aui:button-row>
				</td>	
				</tr>
				</table>
			</div>
		</div>
	</div>
			<b><liferay-ui:message key="output:"/></b>
	
			<pre><c:out value="<%=scriptOutput%>" /></pre>
			<br />
			<br />
			
	<%
		if(scriptError.length() > 0) {
	%>		
			<b><liferay-ui:message key="error:"/></b>
	
			<pre><c:out value="<%=scriptError%>" /></pre>
			<br />
			<br />
	<%
		}
	%>		
	
	</aui:form>

		<aui:script>
			function <portlet:namespace />saveinto() {
				var scriptname = document.<portlet:namespace />fm.<portlet:namespace />savedscript.value;
				if(scriptname == null || scriptname == '') {
					scriptname = prompt('<liferay-ui:message key="enter-new-script-name:"/>', '');
					if(scriptname != null) {
						document.<portlet:namespace />fm.<portlet:namespace />newscriptname.value = scriptname;
						document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'save';
						return true;
					} else {
						return false;
					}		
				} 
				if(confirm('<liferay-ui:message key="overwrite-existing-saved-script"/> \'' + scriptname + '\'?')) { 
					document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'saveinto'; 
					return true; 
				} else { 
					return false;
				}
			}

			function <portlet:namespace />loadfrom() {
				var scriptname = document.<portlet:namespace />fm.<portlet:namespace />savedscript.value;
				if(scriptname == null || scriptname == '') {
					alert('<liferay-ui:message key="please-select-saved-script-to-load-from"/>');
					return false;
				} 
				if(confirm('<liferay-ui:message key="discard-any-unsaved-changes-and-load-script?"/>')) { 
					document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'loadfrom'; 
					return true; 
				} else { 
					return false;
				}
			}

			function <portlet:namespace />delete() {
				var scriptname = document.<portlet:namespace />fm.<portlet:namespace />savedscript.value;
				if(scriptname == null || scriptname == '') {
					alert('<liferay-ui:message key="please-select-saved-script-to-delete"/>');
					return false;
				} 
				if(confirm('<liferay-ui:message key="delete-saved-script"/> \'' + scriptname + '\'?')) { 
					document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'delete'; 
					return true; 
				} else { 
					return false;
				}
			}

			function <portlet:namespace />import() {
				var filename = document.<portlet:namespace />fm.<portlet:namespace />importfile.value;
				if(filename == null || filename == '') {
					alert("<liferay-ui:message key="please-select-a-zip-file-to-upload"/>");
					return false;
				}			
				if(confirm('<liferay-ui:message key="import-scripts-from-file?-this-will-overwrite-any-existing-scripts-with-the-same-names"/>')) { 
					document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'import'; 
					return true; 
				} else { 
					return false;
				}
			}


			function <portlet:namespace />execute() {
				document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'execute';
				return true;
			}
		
		
			function <portlet:namespace />save() {
				var scriptname = prompt('<liferay-ui:message key="enter-new-script-name:"/>', '');
				if(scriptname != null) {
					document.<portlet:namespace />fm.<portlet:namespace />newscriptname.value = scriptname;
					document.<portlet:namespace />fm.<portlet:namespace />cmd.value = 'save';
					return true;
				} else {
					return false;
				}		
			}
		</aui:script>

</div>
