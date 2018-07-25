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

        <h1 class="ui header"><semui:headerIcon />${institution.name}</h1>

        <semui:filter>
            <form class="ui form">
                <div class="fields">
                    <div class="field">
                        <label>${message(code: 'task.title.label', default: 'Title')}</label>
                        <div class="ui input">
                            <input type="text" name="taskName"
                                   placeholder="${message(code: 'default.search.ph', default: 'enter search term ..')}"
                                   value="${params.taskName?.encodeAsHTML()}"/>
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
                    <div class="fields">
                        <div class="field">
                            <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.filterreset.label')}</a>
                        </div>
                        <div class="field">
                            <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}"/>
                        </div>
                    </div>
            </form>
        </semui:filter>

        <semui:messages data="${flash}" />

        <h2 class="ui header">Mir zugewiesene Aufgaben</h2>

        <g:if test="${editable}">
            <input type="submit" class="ui button" value="${message(code:'task.create.new')}" data-semui="modal" href="#modalCreateTask" />
        </g:if>

        <g:render template="/templates/tasks/modal_create" />

        <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList]}"/>

        <g:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList]}"/>



  </body>
</html>
