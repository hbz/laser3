<%@ page import="com.k_int.kbplus.Org;com.k_int.kbplus.UserSettings;com.k_int.kbplus.auth.UserOrg" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui left aligned icon header">
        <semui:headerIcon />
        ${orgInstance.name}
    </h1>

    <g:render template="nav" />

    <semui:messages data="${flash}" />

    <g:if test="${pendingRequests && editable}">

        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>Account</th>
                <th>${message(code:'user.label')}</th>
                <th>${message(code:'user.email')}</th>
                <th>${message(code:'profile.membership.role')}</th>
                <th>${message(code: "profile.membership.date2")}</th>
                <th>${message(code:'user.status')}</th>
                <th>${message(code:'user.actions')}</th>
            </tr>
            </thead>

            <g:each in="${pendingRequests}" var="uo">
                <tr>
                    <td>
                        ${uo.user.username}
                    </td>
                    <td>
                        ${uo.user.displayName}
                    </td>
                    <td>
                        ${uo.user.email}
                    </td>
                    <td>
                        <g:message code="cv.roles.${uo.formalRole?.authority}"/>
                    </td>
                    <td>
                        <g:formatDate format="dd. MMMM yyyy" date="${uo.dateRequested}"/>
                    </td>
                    <td>
                        <g:message code="cv.membership.status.${uo.status}" />
                    </td>
                    <td class="x">
                        <g:link controller="organisations" action="processAffiliation"
                                params="${[assoc:uo.id, id:params.id, cmd:'approve']}" class="ui icon positive button"
                                data-tooltip="${message(code:'profile.membership.accept.button')}" data-position="top left" >
                            <i class="checkmark icon"></i>
                        </g:link>

                        <g:link controller="organisations" action="processAffiliation"
                                params="${[assoc:uo.id, id:params.id, cmd:'reject']}" class="ui icon negative button"
                                data-tooltip="${message(code:'profile.membership.cancel.button')}" data-position="top left" >
                            <i class="times icon"></i>
                        </g:link>
                    </td>
                </tr>
            </g:each>
        </table>
    </g:if>

    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th>Account</th>
            <th>${message(code:'user.label')}</th>
            <th>${message(code:'user.email')}</th>
            <th>${message(code:'profile.membership.role')}</th>
            <%--<th>${message(code:'user.sys_role', default:'System Role')}</th>--%>
            <g:if test="${editable}">
                <th>${message(code:'user.actions')}</th>
            </g:if>
        </tr>
        </thead>

        <g:each in="${users}" var="uo">
            <tr>
                <td>
                    ${uo.user.username}
                </td>
                <td>
                    ${uo.user.displayName}
                </td>
                <td>
                    ${uo.user.email}
                </td>
                <td>
                    <g:message code="cv.roles.${uo.formalRole?.authority}"/>
                </td>
                <%--<td>
                    <g:if test="${userOrg[1]}">
                        <g:each in="${userOrg[1]}" var="admRole">
                            ${admRole} <br />
                        </g:each>
                    </g:if>
                </td>--%>

                <g:if test="${editable}">
                    <td class="x">
                        <g:link controller="user" action="edit" id="${uo.user.id}" class="ui icon button">
                            <i class="icon write"></i>
                        </g:link>

                        <g:link class="ui icon negative button js-open-confirm-modal"
                                data-confirm-term-what="user"
                                data-confirm-term-what-detail="${uo.user.displayName}"
                                data-confirm-term-where="organisation"
                                data-confirm-term-where-detail="${uo.user.getSettingsValue(UserSettings.KEYS.DASHBOARD)?.name}"
                                data-confirm-term-how="delete"
                                controller="organisations"
                                action="processAffiliation"
                                params="${[assoc:uo.id, id:params.id, cmd:'delete']}"
                                data-tooltip="${message(code:'profile.membership.delete.button')}" data-position="top left" >
                            <i class="trash alternate icon"></i>
                        </g:link>
                    </td>
                </g:if>
            </tr>
        </g:each>
    </table>

</body>
</html>
