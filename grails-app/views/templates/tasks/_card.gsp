<%@page import="de.laser.helper.RDStore" %>
<laser:serviceInjection />

<%--OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accessService.checkMinUserOrgRole(user, institution, 'INST_EDITOR')} @ ${institution}--%>
<g:set var="overwriteEditable" value="${editable || accessService.checkMinUserOrgRole(user, institution, 'INST_EDITOR')}" />

<semui:card message="task.plural" class="notes la-js-hideable ${css_class}" href="#modalCreateTask" editable="${overwriteEditable}">
    <g:each in="${tasks}" var="tsk">
        <div class="ui grid la-js-dont-hide-this-card">
            <div class="twelve wide column summary">
                <a onclick="JSPC.app.taskedit(${tsk.id});">${tsk.title}</a>
                <br />
                <div class="content">
                    ${message(code:'task.endDate.label')}
                    <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk.endDate}"/>
                </div>
                <g:if test="${tsk.status == RDStore.TASK_STATUS_DONE}">
                    <i class="ui icon green check"></i>
                </g:if>
                <g:elseif test="${tsk.status == RDStore.TASK_STATUS_OPEN}">
                    <i class="ui icon red times"></i>
                </g:elseif>
                <g:elseif test="${tsk.status == RDStore.TASK_STATUS_DEFERRED}">
                    <i class="ui icon yellow pause"></i>
                </g:elseif>
            </div>
            <div class="right aligned four wide column la-column-left-lessPadding">
                <g:link action="deleteTask" controller="ajax"
                        class="ui icon negative button la-modern-button la-modern-button js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.task")}"
                        data-confirm-term-how="delete"
                        params='[deleteId:tsk.id, id: params.id, returnToShow: controllerName]'
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                    <i class="trash alternate outline icon"></i>
                </g:link>
            </div>
        </div>
    </g:each>
</semui:card>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.taskedit = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
        func();
    }
</laser:script>
