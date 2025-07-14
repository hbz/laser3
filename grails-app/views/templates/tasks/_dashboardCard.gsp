<%@page import="de.laser.utils.SqlDateUtils; de.laser.AccessService; de.laser.storage.RDConstants; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>
<laser:serviceInjection />
<g:set var="overwriteEditable" value="${accessService.hasAccessToTask(tsk, AccessService.WRITE)}" />

<div class="ui card">
    <div class="ui label">
        <div class="right floated author">
            Status: <ui:xEditableRefData config="${RDConstants.TASK_STATUS}" owner="${tsk}" field="status" overwriteEditable="${overwriteEditable}" />
        </div>
    </div>

    <div class="content">
        <div class="meta">
            <div class="">
                Fällig:
                <g:if test="${SqlDateUtils.isBeforeToday(tsk.endDate)}">
                    <strong style="color:#bb1600"><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk.endDate}"/></strong>
                </g:if>
                <g:else>
                    <strong><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk.endDate}"/></strong>
                </g:else>
            </div>
        </div>
        <g:if test="${overwriteEditable}">
            <a class="header" onclick="JSPC.app.dashboard.editTask(${tsk.id});">${tsk.title}</a>
        </g:if>
        <g:else>
            <a class="header" onclick="JSPC.app.dashboard.readTask(${tsk.id});">${tsk.title}</a>
        </g:else>

        <div class="description">
            <g:if test="${tsk.description}">
                <span><em>${tsk.description}</em></span> <br />
            </g:if>
        </div>
    </div>
    <div class="extra content">
        <g:if test="${tsk.getObjectInfo()}">
            <g:set var="tskObj" value="${tsk.getObjectInfo()}" />
            <div class="item">
                <span class="la-popup-tooltip" data-content="${message(code: 'task.' + tskObj.controller)}" data-position="left center" data-variation="tiny">
                    <i class="${tskObj.icon}"></i>
                </span>
                <g:if test="${tskObj.controller.contains('survey')}">
                    <g:link controller="${tskObj.controller}" action="show" params="${[id: tskObj.object.surveyInfo.id, surveyConfigID:tskObj.object.id]}">${tskObj.object.getSurveyName()}</g:link>
                </g:if>
                <g:else>
                    <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object.id]}">${tskObj.object}</g:link>
                </g:else>
            </div>
        </g:if>
        <g:else>
            <i class="${Icon.TASK}"></i> ${message(code: 'task.general')}
        </g:else>
    </div>
</div>

