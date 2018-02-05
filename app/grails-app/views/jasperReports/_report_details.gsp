<%@ page import="com.k_int.kbplus.JasperReportsController" %>
<div class="well">
    ${message(code:'jasper.reports.desc', default:'Report Description')}: ${reportdesc}

</div>

<g:form controller="jasperReports" action="generateReport">
    <input type="hidden" id="hiddenReportName" name="_file">
    <input type="hidden" id="hiddenReportFormat" name="_format">
    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th class="text-center" colspan="2">
                ${message(code:'jasper.reports.params', default:'Report Parameters')}
            </th>
        </tr>
        <tr>
            <th>${message(code:'default.description.label', default:'Description')}</th>
            <th>${message(code:'default.value.label', default:'Value')}</th>
        </tr>
        </thead>
        <tbody>

        <g:each in="${report_parameters}" var="rparam">
            <tr>
            <td>${rparam.getDescription()}</td>
            <td>
                <g:if test="${rparam.getValueClass().equals(java.sql.Timestamp) || rparam.getValueClass().equals(java.sql.Date) }">
                    <semui:datepicker name="${rparam.getName()}" placeholder ="default.date.label"  >
                    </semui:datepicker>
                </g:if>
                <g:elseif test="${rparam.getName().contains("select")}">
                    <g:select name="${rparam.getName()}" 
                    from="${rparam.getName().substring(rparam.getName().indexOf('&')+1).split('&')}"/>
                </g:elseif>
                <g:else>
                    <g:if test="${rparam.getName().contains("search")}">

                       <input type="hidden" id="${rparam.getName()}" name="${rparam.getName()}"/>
                        <script type="text/javascript">
                            createSelect2Search('#${rparam.getName()}', '${rparam.getValueClass().toString().replace("class ","")}');
                        </script>
                    </g:if>
                    <g:else>
                        <input type="text" name="${rparam.getName()}"/>
                    </g:else>
                </g:else>
            </td>
        </g:each>
        </tr>
        </tbody>
    </table>
    <g:submitButton name="submit" class="ui button" value="${message(code:'jasper.generate.label', default:'Generate Report')}"/>
</g:form>
