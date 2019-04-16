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

    <g:if test="${editable}">
        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>
    </g:if>

    <h1 class="ui left aligned icon header">
        <semui:headerIcon />
        ${orgInstance.name}
    </h1>

    <g:render template="nav" />

    <semui:messages data="${flash}" />

    <g:if test="${pendingRequests && editable}">

        <h3 class="ui header">Offene Anfragen</h3>

        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code:'user.username.label')}</th>
                <th>${message(code:'user.displayName.label')}</th>
                <th>${message(code:'user.email')}</th>
                <th>${message(code:'profile.membership.role')}</th>
                <th>${message(code: "profile.membership.date2")}</th>
                <th>${message(code:'user.status')}</th>
                <th></th>
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
                        <g:link controller="organisation" action="processAffiliation"
                                params="${[assoc:uo.id, id:params.id, cmd:'approve']}" class="ui icon positive button"
                                data-tooltip="${message(code:'profile.membership.accept.button')}" data-position="top left" >
                            <i class="checkmark icon"></i>
                        </g:link>

                        <g:link controller="organisation" action="processAffiliation"
                                params="${[assoc:uo.id, id:params.id, cmd:'reject']}" class="ui icon negative button"
                                data-tooltip="${message(code:'profile.membership.cancel.button')}" data-position="top left" >
                            <i class="times icon"></i>
                        </g:link>
                    </td>
                </tr>
            </g:each>
        </table>

        <h3 class="ui header">${message(code: 'profile.membership.existing')}</h3>

    </g:if>

    <sec:ifNotGranted roles="ROLE_ADMIN">
        <div class="ui info message">${message(code:'user.edit.info')}</div>
    </sec:ifNotGranted>

    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th>${message(code:'user.username.label')}</th>
            <th>${message(code:'user.displayName.label')}</th>
            <th>${message(code:'user.email')}</th>
            <th>${message(code:'profile.membership.role')}</th>
            <%--<th>${message(code:'user.sys_role', default:'System Role')}</th>--%>
            <g:if test="${editable}">
                <th></th>
            </g:if>
        </tr>
        </thead>

        <g:each in="${users}" var="uo">
            <tr>
                <td>
                    ${uo.user.username}

                    <g:if test="${! uo.user.enabled}">
                        <span data-position="top left" data-tooltip="${message(code:'user.disabled.text')}">
                            <i class="icon minus circle red"></i>
                        </span>
                    </g:if>
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

                <g:if test="${editable}">
                    <td class="x">
                        <%
                            boolean checkEditable = Org.executeQuery(
                                    'SELECT DISTINCT uo.org from UserOrg uo where uo.user = :user',
                                    [user: uo.user]
                            ).size() == 1
                        %>
                        <g:if test="${checkEditable}">
                            <g:link controller="user" action="edit" id="${uo.user.id}" class="ui icon button">
                                <i class="icon write"></i>
                            </g:link>
                        </g:if>

                        <g:link class="ui icon negative button js-open-confirm-modal"
                                data-confirm-term-what="user"
                                data-confirm-term-what-detail="${uo.user.displayName}"
                                data-confirm-term-where="organisation"
                                data-confirm-term-where-detail="${uo.user.getSettingsValue(UserSettings.KEYS.DASHBOARD)?.name}"
                                data-confirm-term-how="delete"
                                controller="organisation"
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
