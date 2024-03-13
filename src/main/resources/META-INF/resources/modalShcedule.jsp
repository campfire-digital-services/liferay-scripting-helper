<%@ page import="com.liferay.portal.kernel.scheduler.TimeUnit" %>
<%@ page import="com.liferay.portal.kernel.util.CalendarFactoryUtil" %>
<%@ page import="java.util.Calendar" %>
<%@ include file="init.jsp" %>

<liferay-portlet:actionURL copyCurrentRenderParameters="<%= true %>" name="/scripting/schedule" var="scheduleURL" />

<%
Calendar cal = CalendarFactoryUtil.getCalendar(timeZone, locale);
%>
<aui:form action="${scheduleURL}" enctype="multipart/form-data" method="post" name="fms">
    <aui:input name="script" type="hidden" value="" />
    <div class="form-group-autofit">

        <div class="form-group-item">
            <aui:input name="bgTaskName" type="text" value="" required="true" label="enter-new-script-name:" />
        </div>
        <div class="form-group-item">
            <aui:input name="interval" type="number" value="0" required="true" label="interval:" />
        </div>
        <div class="form-group-item">
            <aui:select name="timeUnit" label="Select Time Unit">
                <c:forEach var="timeUnit" items="<%= TimeUnit.values()%>">
                    <aui:option value="${timeUnit.value}">${timeUnit.value}</aui:option>
                </c:forEach>
            </aui:select>
        </div>

    </div>
    <div class="form-group-autofit">

        <div class="form-group-item">
            <label><liferay-ui:message key="Start Date" /></label>

            <liferay-ui:input-date

                dayParam="startDateDay"
                dayValue="<%= cal.get(Calendar.DATE) %>"
                monthParam="startDateMonth"
                monthValue="<%= cal.get(Calendar.MONTH) %>"
                yearParam="startDateYear"
                yearValue="<%= cal.get(Calendar.YEAR) %>"
                name="startDate"
            />
        </div>
        <div class="form-group-item">
            <label><liferay-ui:message key=" Start Time" /></label>
            <liferay-ui:input-time
                    amPmParam="startDateAmPm"
                    amPmValue="<%= cal.get(Calendar.AM_PM) %>"
                    hourParam="startDateHour"
                    hourValue="<%= cal.get(Calendar.HOUR) %>"
                    minuteParam="startDateMinute"
                    minuteValue="<%= cal.get(Calendar.MINUTE) %>"
                    name="startTime"
            />
        </div>

    </div>
    <aui:button onClick="scheduleGroovy()" primary="false" type="button" value="OK"/>

</aui:form>
<aui:script>

    function scheduleGroovy(){
        var form = document.<portlet:namespace />fms;
        Liferay.Util.postForm(form, {
            data: {
                    script: Liferay.Util.getOpener().<portlet:namespace />codearea.value
            }
        });
    }

</aui:script>