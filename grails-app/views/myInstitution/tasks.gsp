<%@ page import="de.laser.RefdataCategory;de.laser.storage.RDConstants"%>
<laser:htmlStart message="task.plural" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.institutions.tasks" class="active"/>
        </ui:breadcrumbs>

        <ui:controlButtons>
            <laser:render template="actions"/>
        </ui:controlButtons>

        <ui:h1HeaderWithIcon message="task.plural" floated="true" />

        <%--<g:if test="${editable}">
            <input type="submit" class="ui left floated  button la-clear-before" value="${message(code:'task.create.new')}" data-semui="modal" data-href="#modalCreateTask" />
        </g:if>--%>
        <laser:render template="/templates/filter/javascript" />

        <ui:filter showFilterButton="true">
            <form class="ui form">
                <div class="four fields">
                    <div class="field">
                        <label>${message(code: 'default.title.label')}</label>
                        <div class="ui input">
                            <input type="text" name="taskName"
                                   placeholder="${message(code: 'default.search.ph')}"
                                   value="${params.taskName}"/>
                        </div>
                    </div>

                    <ui:datepicker label="task.endDate.from.label" id="endDateFrom" name="endDateFrom" placeholder="default.date.label" value="${params.endDateFrom}" />

                    <ui:datepicker label="task.endDate.to.label" id="endDateTo" name="endDateTo" placeholder="default.date.label" value="${params.endDateTo}" />

                    <div class="field">
                        <label>${message(code: 'default.status.label')}</label>
                        <laser:select class="ui fluid dropdown"
                                  name="taskStatus"
                                  value="${params.taskStatus}"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  noSelection="${['' : message(code:'default.all')]}"
                                  optionKey="id"
                                  optionValue="value" />
                    </div>
                </div>
                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                    <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}"/>
                </div>
            </form>
        </ui:filter>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/tasks/modal_create" />

        <laser:render template="/templates/tasks/tables" model="${[
                taskInstanceList: taskInstanceList,     taskInstanceCount: taskInstanceCount,
                myTaskInstanceList: myTaskInstanceList, myTaskInstanceCount: myTaskInstanceCount
        ]}"/>

%{--        <laser:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList,taskInstanceCount:taskInstanceCount]}"/>--}%
%{--        <laser:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList,taskInstanceCount:myTaskInstanceCount]}"/>--}%

<laser:htmlEnd />
