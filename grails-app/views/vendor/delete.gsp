<%@ page import="de.laser.ui.Btn" %>
<laser:htmlStart message="vendor.label" />

    <laser:render template="breadcrumb"/>

    <ui:h1HeaderWithIcon text="${vendor.name}"/>

    <g:if test="${delResult.status != deletionService.RESULT_SUCCESS}">
        <laser:render template="nav" />
    </g:if>

    <g:if test="${delResult}">
        <g:if test="${delResult.status == deletionService.RESULT_SUCCESS}">
            <ui:msg class="success" message="deletion.success.msg" />
            <g:link controller="vendor" action="list" class="${Btn.SIMPLE}">${message(code:'menu.public.all_vendors')}</g:link>
        </g:if>
        <g:else>
            <g:if test="${delResult.status == deletionService.RESULT_SUBSTITUTE_NEEDED}">
                <ui:msg class="info" message="vendor.delete.info2" />
            </g:if>

            <g:if test="${delResult.status == deletionService.RESULT_BLOCKED}">
                <ui:msg class="error" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.org" />
            </g:if>
            <g:else>
                <ui:msg class="info" message="vendor.delete.info" />
            </g:else>
            <g:if test="${delResult.status == deletionService.RESULT_ERROR}">
                <ui:msg class="error" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
            </g:if>

            <g:link controller="vendor" action="list" class="${Btn.SIMPLE}">${message(code:'menu.public.all_vendors')}</g:link>
            <g:link controller="vendor" action="show" params="${[id: vendor.id]}" class="${Btn.SIMPLE}"><g:message code="default.button.cancel.label"/></g:link>

            <g:if test="${editable}">
                <g:form controller="vendor" action="delete" params="${[id: vendor.id, process: true]}" style="display:inline-block;vertical-align:top">

                    <g:if test="${delResult.deletable}">
                        <g:if test="${delResult.status != deletionService.RESULT_ERROR}">
                            <input type="submit" class="${Btn.NEGATIVE}" value="${message(code:'deletion.vendor')}" />
                        </g:if>
                    </g:if>
                    <g:else>
                        <input disabled type="submit" class="${Btn.NEGATIVE}" value="${message(code:'deletion.vendor')}" />
                    </g:else>
                </g:form>
            </g:if>

        </g:else>

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <table class="ui celled la-js-responsive-table la-table compact table">
                <thead>
                    <tr>
                        <th>Anhängende, bzw. referenzierte Objekte</th>
                        <th>${message(code:'default.count.label')}</th>
                        <th>Objekt-Ids</th>
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
                                    ${info[1].collect{ item -> item.hasProperty('id') ? item.id : 'x'}.sort().join(', ')}
                                </div>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </sec:ifAnyGranted>
    </g:if>

<laser:htmlEnd />
