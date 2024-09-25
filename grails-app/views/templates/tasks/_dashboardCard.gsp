<%@page import="de.laser.storage.RDConstants; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>

<div class="ui card">
    <div class="ui label">
        <div class="right floated author">
            Status: <ui:xEditableRefData config="${RDConstants.TASK_STATUS}" owner="${tsk}" field="status" />
        </div>
    </div>

    <div class="content">
        <div class="meta">
            <div class="">Fällig: <strong><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk?.endDate}"/></strong></div>
        </div>
        <a class="header" onclick="JSPC.app.dashboard.editTask(${tsk?.id});">${tsk?.title}</a>

        <div class="description">
            <g:if test="${tsk.description}">
                <span><em>${tsk.description}</em></span> <br />
            </g:if>
        </div>
    </div>
    <div class="extra content">
        <g:if test="${tsk.getObjects()}">
            <g:each in="${tsk.getObjects()}" var="tskObj">
                <div class="item">
                    <span class="la-popup-tooltip" data-content="${message(code: 'task.' + tskObj.controller)}" data-position="left center" data-variation="tiny">
                        <g:if test="${tskObj.controller == 'organisation'}">
                            <i class="${Icon.ORG}"></i>
                        </g:if>
                        <g:if test="${tskObj.controller == 'provider'}">
                            <i class="${Icon.PROVIDER}"></i>
                        </g:if>
                        <g:if test="${tskObj.controller == 'vendor'}">
                            <i class="${Icon.VENDOR}"></i>
                        </g:if>
                        <g:if test="${tskObj.controller.contains('subscription')}">
                            <i class="${Icon.SUBSCRIPTION}"></i>
                        </g:if>
                        <g:if test="${tskObj.controller.contains('license')}">
                            <i class="${Icon.LICENSE}"></i>
                        </g:if>
                        <g:if test="${tskObj.controller.contains('survey')}">
                            <i class="${Icon.SURVEY}"></i>
                        </g:if>
                        <g:if test="${tskObj.controller.contains('tipp')}">
                            <i class="${Icon.TIPP}"></i>
                        </g:if>
                    </span>
                    <g:if test="${tskObj.controller.contains('survey')}">
                        <g:link controller="${tskObj.controller}" action="show" params="${[id: tskObj.object?.surveyInfo.id, surveyConfigID:tskObj.object?.id]}">${tskObj.object.getSurveyName()}</g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object?.id]}">${tskObj.object}</g:link>
                    </g:else>
                </div>
            </g:each>
        </g:if>
        <g:else>
            <i class="${Icon.TASK}"></i> ${message(code: 'task.general')}
        </g:else>
    </div>
</div>

