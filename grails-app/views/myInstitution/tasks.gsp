<%@ page import="de.laser.RefdataCategory;de.laser.storage.RDConstants"%>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'task.plural')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.institutions.tasks" class="active"/>
        </semui:breadcrumbs>

        <semui:controlButtons>
            <g:render template="actions"/>
        </semui:controlButtons>

        <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'task.plural')}</h1>

        <%--<g:if test="${editable}">
            <input type="submit" class="ui left floated  button la-clear-before" value="${message(code:'task.create.new')}" data-semui="modal" data-href="#modalCreateTask" />
        </g:if>--%>
        <g:render template="/templates/filter/javascript" />

        <semui:filter showFilterButton="true">
            <form class="ui form">
                <div class="four fields">
                    <div class="field">
                        <label>${message(code: 'task.title.label')}</label>
                        <div class="ui input">
                            <input type="text" name="taskName"
                                   placeholder="${message(code: 'default.search.ph')}"
                                   value="${params.taskName}"/>
                        </div>
                    </div>

                    <semui:datepicker label="task.endDate.from.label" id="endDateFrom" name="endDateFrom" placeholder="default.date.label" value="${params.endDateFrom}" />

                    <semui:datepicker label="task.endDate.to.label" id="endDateTo" name="endDateTo" placeholder="default.date.label" value="${params.endDateTo}" />

                    <div class="field">
                        <label>${message(code: 'task.status.label')}</label>
                        <laser:select class="ui fluid dropdown"
                                  name="taskStatus"
                                  value="${params.taskStatus}"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  noSelection="${['' : message(code:'accessMethod.all')]}"
                                  optionKey="id"
                                  optionValue="value" />
                    </div>
                </div>
                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                    <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}"/>
                </div>
            </form>
        </semui:filter>

        <semui:messages data="${flash}" />

        <g:render template="/templates/tasks/modal_create" />

        <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList,taskInstanceCount:taskInstanceCount]}"/>
        <g:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList,taskInstanceCount:myTaskInstanceCount]}"/>

        <g:render template="/templates/tasks/js_taskedit"/>

  </body>
</html>
