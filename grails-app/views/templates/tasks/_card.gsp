<%@page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<%--OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${userService.hasFormalAffiliation(user, institution, 'INST_EDITOR')} @ ${institution}--%>
<g:set var="overwriteEditable" value="${editable || userService.hasFormalAffiliation(user, institution, 'INST_EDITOR')}" />

<ui:card message="task.plural" class="tasks ${css_class}" href="#modalCreateTask" editable="${overwriteEditable}">
    <div class="ui relaxed divided list">
    <g:each in="${tasks}" var="tsk">
        <g:if test="${tsk.status == RDStore.TASK_STATUS_DONE}">
            <g:set var="icon" value="check circle outline"/>
            <g:set var="tooltip" value="${message(code: 'tooltip.closedTask')}"/>
        </g:if>
        <g:elseif test="${tsk.status == RDStore.TASK_STATUS_OPEN}">
            <g:set var="icon" value="la-open"/>
            <g:set var="tooltip" value="${message(code: 'tooltip.openTask')}"/>
        </g:elseif>
        <g:elseif test="${tsk.status == RDStore.TASK_STATUS_DEFERRED}">
            <g:set var="icon" value="pause circle outline"/>
            <g:set var="tooltip" value="${message(code: 'tooltip.pausedTask')}"/>
        </g:elseif>

            <div class="item">
                <g:if test="${overwriteEditable}">
                    <div class="right floated content">
                        <g:link action="deleteTask" controller="ajax"
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
                <i class="large la-list-icon la-popup-tooltip icon ${icon}" data-content="${tooltip}"></i>
                <div class="content">
                    <a class="header" onclick="JSPC.app.editTask(${tsk.id});">${tsk.title}</a>
                    <div class="description">${message(code:'task.endDate.label')}
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk.endDate}"/>
                    </div>
                </div>
            </div>


        %{--<div class="ui grid">
            <div class="twelve wide column summary">

                    <g:if test="${tsk.status == RDStore.TASK_STATUS_DONE}">
                        <i class="large la-list-icon la-popup-tooltip icon check circle outline"></i>
                    </g:if>
                    <g:elseif test="${tsk.status == RDStore.TASK_STATUS_OPEN}">
                        <i class="large la-list-icon la-popup-tooltip icon la-open"></i>
                    </g:elseif>
                    <g:elseif test="${tsk.status == RDStore.TASK_STATUS_DEFERRED}">
                        <i class="large la-list-icon la-popup-tooltip icon pause circle outline"></i>
                    </g:elseif>
                    <a onclick="JSPC.app.editTask(${tsk.id});">${tsk.title}</a>

                <div class="content">
                    ${message(code:'task.endDate.label')}
                    <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk.endDate}"/>
                </div>
            </div>
            <div class="right aligned four wide column la-column-left-lessPadding">
                <g:link action="deleteTask" controller="ajax"
                        class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.task")}"
                        data-confirm-term-how="delete"
                        params='[deleteId:tsk.id, id: params.id, returnToShow: controllerName]'
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                    <i class="${Icon.CMD.DELETE}"></i>
                </g:link>
            </div>
        </div>--}%
    </g:each>
    </div>
</ui:card>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editTask = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
        func();
    }
    JSPC.app.readTask = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalReadTask", "<g:createLink controller="ajaxHtml" action="readTask"/>?id=" + id);
        func();
    }
</laser:script>
