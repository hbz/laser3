<%@ page import="de.laser.ui.Btn; de.laser.CustomerTypeService" %>
<laser:htmlStart message="default.subscription.label" />

    <laser:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <ui:h1HeaderWithIcon text="${subscription.name}" />

    <g:if test="${delResult.status != deletionService.RESULT_SUCCESS}">
        <laser:render template="nav" />
    </g:if>

    <g:if test="${delResult}">
        <g:if test="${delResult.status == deletionService.RESULT_SUCCESS}">
            <ui:msg class="success" message="deletion.success.msg" />
            <g:link controller="myInstitution" action="currentSubscriptions" class="${Btn.SIMPLE}">${message(code:'menu.my.subscriptions')}</g:link>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                <g:link controller="subscription" action="members" id="${parentId}" class="${Btn.SIMPLE}">${message(code:"subscription.details.consortiaMembers.label")}</g:link>
            </g:if>
        </g:if>
        <g:else>
            <%--<ui:msg class="info" message="subscription.delete.info" /> deleted as of ERMS-4710 and December 16th, '22--%>

            <g:if test="${delResult.status == deletionService.RESULT_BLOCKED}">
                <ui:msg class="error" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.subscription" />
            </g:if>
            <g:if test="${delResult.status == deletionService.RESULT_ERROR}">
                <ui:msg class="error" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
            </g:if>

            <g:link controller="myInstitution" action="currentSubscriptions" class="${Btn.SIMPLE}">${message(code:'menu.my.subscriptions')}</g:link>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                <g:link controller="subscription" action="members" id="${parentId}" class="${Btn.SIMPLE}">${message(code:"subscription.details.consortiaMembers.label")}</g:link>
            </g:if>
            <g:link controller="subscription" action="show" params="${[id: subscription.id]}" class="${Btn.SIMPLE}"><g:message code="default.button.cancel.label"/></g:link>

            <g:if test="${editable}">
                <g:set var="delLabel" value="${parentId ? message(code:'deletion.subscription.member') : message(code:'deletion.subscription')}"/>
                <g:if test="${delResult.deletable}">
                    <g:link controller="subscription" action="delete" params="${[id: subscription.id, process: true]}" class="${Btn.NEGATIVE}">${delLabel}</g:link>
                </g:if>
                <g:else>
                    <input disabled type="submit" class="${Btn.NEGATIVE}" value="${delLabel}" />
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
                    <td class="center aligned">
                        <g:if test="${info.size() > 2 && info[1].size() > 0}">
                            <span class="ui circular label la-popup-tooltip ${info[2]}"
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
                            ${info[1].collect{ item -> item instanceof Long ? item : item.hasProperty('id') ? item.id : 'x'}.sort().join(', ')}
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>

<laser:htmlEnd />
