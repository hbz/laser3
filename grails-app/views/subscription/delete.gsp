<g:set var="deletionService" bean="deletionService" />
<g:set var="accessService" bean="accessService" />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'default.subscription.label')}</title>
</head>

<body>
    <laser:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <semui:headerWithIcon text="${subscription.name}" />

    <g:if test="${delResult.status != deletionService.RESULT_SUCCESS}">
        <laser:render template="nav" />
    </g:if>

    <g:if test="${delResult}">
        <g:if test="${delResult.status == deletionService.RESULT_SUCCESS}">
            <semui:msg class="positive" header="" message="deletion.success.msg" />
            <g:link controller="myInstitution" action="currentSubscriptions" class="ui button">${message(code:'menu.my.subscriptions')}</g:link>
            <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                <g:link controller="subscription" action="members" id="${parentId}" class="ui button">${message(code:"subscription.details.consortiaMembers.label")}</g:link>
            </g:if>
        </g:if>
        <g:else>
            <semui:msg class="info" header="" message="subscription.delete.info" />

            <g:if test="${delResult.status == deletionService.RESULT_BLOCKED}">
                <semui:msg class="negative" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.subscription" />
            </g:if>
            <g:if test="${delResult.status == deletionService.RESULT_ERROR}">
                <semui:msg class="negative" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
            </g:if>

            <g:link controller="myInstitution" action="currentSubscriptions" class="ui button">${message(code:'menu.my.subscriptions')}</g:link>
            <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                <g:link controller="subscription" action="members" id="${parentId}" class="ui button">${message(code:"subscription.details.consortiaMembers.label")}</g:link>
            </g:if>
            <g:link controller="subscription" action="show" params="${[id: subscription.id]}" class="ui button"><g:message code="default.button.cancel.label"/></g:link>

            <g:if test="${editable}">
                <g:if test="${delResult.deletable}">
                    <g:link controller="subscription" action="delete" params="${[id: subscription.id, process: true]}" class="ui button red">${message(code:'deletion.subscription')}</g:link>
                </g:if>
                <g:else>
                    <input disabled type="submit" class="ui button red" value="${message(code:'deletion.subscription')}" />
                </g:else>
            </g:if>
        </g:else>

        <%-- --%>

        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
            <tr>
                <th><g:message code="subscription.delete.header.referencingObject"/></th>
                <th><g:message code="default.count.label"/></th>
                <th><g:message code="subscription.delete.header.objectIDs"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${delResult.info.sort{ a,b -> a[0] <=> b[0] }}" var="info">
                <tr>
                    <td>
                        ${info[0]}
                    </td>
                    <td style="text-align:center">
                        <g:if test="${info.size() > 2 && info[1].size() > 0}">
                            <span class="ui circular label la-popup-tooltip la-delay ${info[2]}"
                                <g:if test="${info[2] == 'red'}">
                                    data-content="${message(code:'subscription.delete.blocker')}"
                                </g:if>
                                <g:if test="${info[2] == 'yellow'}">
                                    data-content="${message(code:'subscription.existingCostItems.warning')}"
                                </g:if>
                            >${info[1].size()}</span>
                        </g:if>
                        <g:else>
                            ${info[1].size()}
                        </g:else>
                    </td>
                    <td>
                        <div style="overflow-y:scroll;scrollbar-color:grey white;max-height:14.25em">
                            ${info[1].collect{ item -> item.hasProperty('id') ? item.id : 'x'}.sort().join(', ')}
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>

</body>
</html>
