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

        <semui:messages data="${flash}" />

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

                    <div class="field">
                        <label>&nbsp;</label>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label', default:'Search')}" />
                    </div>

                </div>
            </form>
        </semui:filter>

        <h2 class="ui header">Mir zugewiesene Aufgaben</h2>
        <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList]}"/>

        <g:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList]}"/>

  </body>
</html>
