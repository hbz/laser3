<%@ page import="de.laser.ui.Icon;" %>
<laser:serviceInjection />

<ui:modal id="modalReadTask" message="task.label" hideSubmitButton="true">

    <div class="ui form">
        <div class="field">
            <label for="title">
                <g:message code="default.title.label"/>
            </label>
            <g:textField name="title" readonly="readonly" value="${taskInstance.title}"/>
        </div>

        <div class="field">
            <label for="description">
                <g:message code="default.description.label"/>
            </label>
            <g:textArea name="description" readonly="readonly" value="${taskInstance.description}" rows="5" cols="40"/>
        </div>

        <div class="field">
            <label>${message(code: 'task.object.label')}: </label>
            <g:if test="${taskInstance.getObjectInfo()}">
                <g:set var="tskObj" value="${taskInstance.getObjectInfo()}" />
                <div class="la-flexbox">
                    <i class="${tskObj.icon} la-list-icon"></i>
                    <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object.id]}">${tskObj.object}</g:link>
                </div>
            </g:if>
            <g:else>
                <div class="la-flexbox">${message(code: 'task.general')}</div>
            </g:else>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field wide eight">
                    <label for="status">
                        <g:message code="task.status.label" />
                    </label>
                    <g:textField name="status" readonly="readonly" value="${taskInstance.status?.getI10n('value')}"/>
                </div>
                <div class="field wide eight">
                    <label for="endDate">
                        <g:message code="task.endDate.label" />
                    </label>
                    <g:textField name="endDate" readonly="readonly" value="${formatDate(format:message(code:'default.date.format.notime'), date:taskInstance.endDate)}"/>
                </div>
            </div>
        </div>

        <div class="field">
            <label for="responsible">
                <g:message code="task.responsible.label"/>
            </label>
            <g:if test="${taskInstance.responsibleOrg?.id}">
                <g:textField name="responsible" readonly="readonly" value="${message(code: 'task.responsibleOrg.label')} ${taskInstance.responsibleOrg.getDesignation()}"/>
            </g:if>
            <g:if test="${taskInstance.responsibleUser?.id}">
                <g:textField name="responsible" readonly="readonly" value="${taskInstance.responsibleUser.getDisplayName()}"/>
            </g:if>
        </div>

    </div>

</ui:modal>

