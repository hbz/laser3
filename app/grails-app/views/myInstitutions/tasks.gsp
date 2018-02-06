<%@ page import="com.k_int.kbplus.RefdataCategory"%>

<!doctype html>
<r:require module="annotations" />

<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${institution.name} - ${message(code:'task.plural', default:'Tasks')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.getDesignation()}" />
            <semui:crumb message="menu.institutions.tasks" class="active"/>
        </semui:breadcrumbs>

        <semui:messages data="${flash}" />

        <h1 class="ui header">${institution.name}</h1>

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

        <g:render template="/templates/tasks/table" model="${[ownobj:institution, owntp:'org', taskInstanceList:taskInstanceList]}"/>

        <g:render template="/templates/tasks/table2" model="${[ownobj:institution, owntp:'org', taskInstanceList:myTaskInstanceList]}"/>

  </body>
</html>
