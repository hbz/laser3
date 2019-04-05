<%@ page import="com.k_int.kbplus.RefdataCategory"%>

<!doctype html>
<r:require module="annotations" />

<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'task.plural', default:'Tasks')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.institutions.tasks" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${institution.name}</h1>

        <semui:filter>
            <form class="ui form">
                <div class="four fields">
                    <div class="field">
                        <label>${message(code: 'task.title.label', default: 'Title')}</label>
                        <div class="ui input">
                            <input type="text" name="taskName"
                                   placeholder="${message(code: 'default.search.ph', default: 'enter search term ..')}"
                                   value="${params.taskName}"/>
                        </div>
                    </div>

                    <semui:datepicker label="task.endDate.from.label" name="endDateFrom" placeholder="default.date.label" value="${params.endDateFrom}" />

                    <semui:datepicker label="task.endDate.to.label" name="endDateTo" placeholder="default.date.label" value="${params.endDateTo}" />

                    <div class="field">
                        <label>${message(code: 'task.status.label', default: 'Status')}</label>
                        <laser:select class="ui dropdown"
                                  name="taskStatus"
                                  value="${params.taskStatus}"
                                  from="${RefdataCategory.getAllRefdataValues('Task Status')}"
                                  noSelection="${['' : 'Alle']}"
                                  optionKey="id"
                                  optionValue="value" />
                    </div>
                </div>
                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                    <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}"/>
                </div>
            </form>
        </semui:filter>

        <semui:messages data="${flash}" />

        <h2 class="ui header">${message(code: 'task.myTasks.header', default:'Tasks assigned to me')} <semui:totalNumber total="${taskInstanceCount}"/></h2>

        <g:if test="${editable}">
            <input type="submit" class="ui button" value="${message(code:'task.create.new')}" data-semui="modal" data-href="#modalCreateTask" />
        </g:if>

        <g:render template="/templates/tasks/modal_create" />

        <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList,taskInstanceCount:taskInstanceCount]}"/>

        <g:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList,taskInstanceCount:myTaskInstanceCount]}"/>

        <g:render template="/templates/tasks/js_taskedit"/>



  </body>
</html>
