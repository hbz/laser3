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


<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'currentSurveys.label', default: 'Current Surveys')}
<semui:totalNumber total="${countSurvey}"/>
</h1>

<semui:messages data="${flash}"/>

<semui:filter>
    <g:form action="currentSurveys" controller="survey" method="get" class="form-inline ui small form">
        <div class="three fields">
            <div class="field">
                <label for="name">${message(code: 'surveyInfo.name.label')}
                </label>

                <div class="ui input">
                    <input type="text" id="name" name="name"
                           placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                           value="${params.name}"/>
                </div>
            </div>


            <div class="field fieldcontain">
                <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                                  placeholder="filter.placeholder" value="${params.startDate}"/>
            </div>


            <div class="field fieldcontain">
                <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                                  placeholder="filter.placeholder" value="${params.endDate}"/>
            </div>

        </div>

        <div class="four fields">

            <div class="field fieldcontain">
                <label>${message(code: 'surveyInfo.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${RefdataCategory.getAllRefdataValues('Survey Status')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <label>${message(code: 'surveyInfo.type.label')}</label>
                <laser:select class="ui dropdown" name="type"
                              from="${RefdataCategory.getAllRefdataValues('Survey Type')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.type}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label', default: 'Filter')}">
            </div>

        </div>
    </g:form>
</semui:filter>

<div>
    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th rowspan="2" class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>
            <g:sortableColumn params="${params}" property="surveyConfig.surveyInfo.name" title="${message(code: 'surveyInfo.name.label')}"/>
            <g:sortableColumn params="${params}" property="surveyConfig.surveyInfo.type" title="${message(code: 'surveyInfo.type.label')}"/>
            <g:sortableColumn params="${params}" property="surveyConfig.surveyInfo.startDate"
                              title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
            <g:sortableColumn params="${params}" property="surveyConfig.surveyInfo.endDate"
                              title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
            <g:sortableColumn params="${params}" property="surveyConfig.surveyInfo.status"
                              title="${message(code: 'surveyInfo.status.label')}"/>
            <g:sortableColumn params="${params}" property="surveyConfig.surveyInfo.owner"
                              title="${message(code: 'surveyInfo.owner.label')}"/>
            <th>${message(code:'default.actions')}</th>

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

                <td>
                    ${s.status?.getI10n('value')}
                </td>
                <td>
                    ${s.owner}
                </td>

                <td class="x">

                    <g:if test="${editable}">
                        <g:link controller="myInstitution" action="surveyInfos" id="${s.id}" class="ui icon button"><i
                                class="write icon"></i></g:link>

                    </g:if>
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
