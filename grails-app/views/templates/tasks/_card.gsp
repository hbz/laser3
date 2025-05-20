<%@page import="de.laser.AccessService; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>
<laser:serviceInjection />

%{--# editable: ${editable}--}%
%{--# taskService.hasWRITE(): ${taskService.hasWRITE()}--}%

<ui:card message="task.plural" class="tasks ${css_class}" href="#modalCreateTask" editable="${taskService.hasWRITE()}">
    <div class="ui relaxed divided list">
    <g:each in="${tasks}" var="tsk">
        <g:if test="${tsk.status == RDStore.TASK_STATUS_DONE}">
            <g:set var="icon" value="${Icon.ATTR.TASK_STATUS_DONE}"/>
            <g:set var="tooltip" value="${message(code: 'tooltip.closedTask')}"/>
        </g:if>
        <g:elseif test="${tsk.status == RDStore.TASK_STATUS_OPEN}">
            <g:set var="icon" value="${Icon.ATTR.TASK_STATUS_OPEN}"/>
            <g:set var="tooltip" value="${message(code: 'tooltip.openTask')}"/>
        </g:elseif>
        <g:elseif test="${tsk.status == RDStore.TASK_STATUS_DEFERRED}">
            <g:set var="icon" value="${Icon.ATTR.TASK_STATUS_DEFERRED}"/>
            <g:set var="tooltip" value="${message(code: 'tooltip.pausedTask')}"/>
        </g:elseif>

        <g:set var="overwriteEditable" value="${accessService.hasAccessToTask(tsk, AccessService.WRITE)}" />

            <div class="item">
                <g:if test="${overwriteEditable}"> %{-- TODO: responsibleOrg == contextService.getOrg() + INST_ADM ? --}%
                    <div class="right floated content">
                        <g:link action="deleteTask" controller="task"
                                class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.task")}"
                                data-confirm-term-how="delete"
                                params='[deleteId:tsk.id, id: params.id, returnToShow: controllerName]'
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="${Icon.CMD.DELETE}"></i>
                        </g:link>
                    </div>
                </g:if>
                <i class="large ${icon} la-list-icon la-popup-tooltip" data-content="${tooltip}"></i>
                <div class="content">
                    <g:if test="${overwriteEditable}">
                        <a class="header" onclick="JSPC.app.editTask(${tsk.id});">${tsk.title}</a>
                    </g:if>
                    <g:else>
                        <a class="header" onclick="JSPC.app.readTask(${tsk.id});">${tsk.title}</a>
                    </g:else>
                    <div class="description">${message(code:'task.endDate.label')}
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk.endDate}"/>
                    </div>
                </div>

            </div>
    </g:each>
    </div>
</ui:card>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editTask = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id);
        func();
    }
    JSPC.app.readTask = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalReadTask", "<g:createLink controller="ajaxHtml" action="readTask"/>?id=" + id);
        func();
    }
</laser:script>
