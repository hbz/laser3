<%@ page import="de.laser.RefdataCategory;de.laser.storage.RDConstants"%>
<laser:htmlStart message="menu.my.tasks" serviceInjection="true"/>

    <ui:breadcrumbs>
        <ui:crumb message="menu.my.tasks" class="active"/>
    </ui:breadcrumbs>

        <ui:controlButtons>
            <laser:render template="${customerTypeService.getActionsTemplatePath()}"/>
        </ui:controlButtons>

        <ui:h1HeaderWithIcon message="menu.my.tasks" type="task" floated="true" total="${taskCount}/${myTaskCount}" />

        <%--<g:if test="${editable}">
            <input type="submit" class="ui left floated  button la-clear-before" value="${message(code:'task.create.new')}" data-ui="modal" data-href="#modalCreateTask" />
        </g:if>--%>

        <ui:filter>
            <form class="ui form">
                <div class="four fields">
                    <div class="field">
                        <label for="taskName">${message(code: 'default.title.label')}</label>
                        <div class="ui input">
                            <input type="text" id="taskName" name="taskName" placeholder="${message(code: 'default.search.ph')}" value="${params.taskName}"/>
                        </div>
                    </div>

                    <ui:datepicker label="task.endDate.from.label" id="endDateFrom" name="endDateFrom" placeholder="default.date.label" value="${params.endDateFrom}" />

                    <ui:datepicker label="task.endDate.to.label" id="endDateTo" name="endDateTo" placeholder="default.date.label" value="${params.endDateTo}" />

                    <div class="field">
                        <label>${message(code: 'default.status.label')}</label>
                        <ui:select class="ui fluid dropdown"
                                  name="taskStatus"
                                  value="${params.taskStatus}"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  noSelection="${['' : message(code:'default.all')]}"
                                  optionKey="id"
                                  optionValue="value" />
                    </div>
                </div>
                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.filterreset.label')}</a>
                    <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}"/>
                </div>
                <input type="hidden" name="ctrlFilterSend" value="true" />
            </form>
        </ui:filter>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/tasks/modal_create" />

        <laser:render template="/templates/tasks/tables" model="${[cmbTaskInstanceList: cmbTaskInstanceList]}"/>

<laser:htmlEnd />
