<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection/>
<!doctype html>

<r:require module="annotations"/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'currentSurveys.label', default: 'Current Surveys')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="currentSurveys.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<semui:messages data="${flash}"/>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'currentSurveys.label', default: 'Current Surveys')}
<semui:totalNumber total="${countSurvey}"/>
</h1>

<semui:filter>

</semui:filter>

<div>
    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th rowspan="2" class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>
            <g:sortableColumn params="${params}" property="si.name" title="${message(code: 'surveyInfo.name.label')}"/>
            <g:sortableColumn params="${params}" property="si.type" title="${message(code: 'surveyInfo.type.label')}"/>
            <g:sortableColumn params="${params}" property="si.startDate"
                              title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
            <g:sortableColumn params="${params}" property="si.endDate"
                              title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
            <th rowspan="2" class="two wide"></th>

        </tr>

        </thead>
        <g:each in="${surveys}" var="s" status="i">
            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>
                <td>
                    ${s.name}
                </td>
                <td>
                    ${s.type?.getI10n('value')}
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/>

                </td>
                <td>

                    <g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/>
                </td>

                <td class="x">

                </td>
            </tr>

        </g:each>
    </table>
</div>

<g:if test="${surveys}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${countSurvey}"/>
</g:if>

</body>
</html>
