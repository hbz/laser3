<%@ page import="de.laser.auth.User; de.laser.DeletionService" %>
<g:set var="deletionService" bean="deletionService" />
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'user.delete.label')}</title>
</head>
<body>
    <laser:render template="/user/global/breadcrumb" model="${[ params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="/user/global/actions" />
    </semui:controlButtons>

    <semui:h1HeaderWithIcon message="user.delete.label" />
    <h2 class="ui header la-noMargin-top">${user.username} - ${user.displayName ?: 'Nutzer unbekannt'}</h2>

    <g:if test="${delResult}">
        <g:if test="${delResult.status == DeletionService.RESULT_SUCCESS}">
            <semui:msg class="positive" header="" message="deletion.success.msg" />

            <g:if test="${controllerName == 'myInstitution'}">
                <g:link action="users" class="ui button"><g:message code="org.nav.users"/></g:link>
            </g:if>
            <g:if test="${controllerName == 'organisation'}">
                <g:link action="users" params="${[id: orgInstance.id]}" class="ui button"><g:message code="org.nav.users"/></g:link>
            </g:if>
            <g:if test="${controllerName == 'user'}">
                <g:link action="list" class="ui button"><g:message code="org.nav.users"/></g:link>
            </g:if>

        </g:if>
        <g:else>
            <g:if test="${delResult.status == DeletionService.RESULT_SUBSTITUTE_NEEDED}">
                <semui:msg noClose="true" class="warning" header="" message="user.delete.info2" />
            </g:if>
            <g:else>
                <semui:msg noClose="true" class="warning" header="" message="user.delete.info" />
            </g:else>

            <g:if test="${delResult.status == DeletionService.RESULT_CUSTOM}">
                <semui:msg class="negative" header="${message(code: 'deletion.blocked.header')}" message="deletion.custom.msg.user" />
            </g:if>
            <g:if test="${delResult.status == DeletionService.RESULT_BLOCKED}">
                <semui:msg class="negative" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.user" />
            </g:if>
            <g:if test="${delResult.status == DeletionService.RESULT_ERROR}">
                <semui:msg class="negative" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
            </g:if>

            <%
                String formAction = (controllerName == 'user' ? 'delete' : 'deleteUser');
                Map<String, Object> formParams = (controllerName == 'organisation') ? [process: true, id: orgInstance.id] : [process: true]
            %>

            <g:form action="${formAction}" data-confirm-id="deleteUserForm_form" params="${formParams}">

                <g:if test="${controllerName == 'myInstitution'}">
                    <g:link action="users" class="ui button"><g:message code="default.button.cancel.label"/></g:link>
                    <input type="hidden" name="uoid" value="${genericOIDService.getOID(user)}" />
                </g:if>
                <g:if test="${controllerName == 'organisation'}">
                    <g:link action="users" params="${[id: orgInstance.id]}" class="ui button"><g:message code="default.button.cancel.label"/></g:link>
                    <input type="hidden" name="uoid" value="${genericOIDService.getOID(user)}" />
                </g:if>
                <g:if test="${controllerName == 'user'}">
                    <g:link action="list" class="ui button"><g:message code="default.button.cancel.label"/></g:link>
                    <input type="hidden" name="id" value="${user.id}" />
                </g:if>

                <g:if test="${editable}">

                    <g:if test="${delResult.deletable}">
                        <g:if test="${delResult.status == DeletionService.RESULT_SUBSTITUTE_NEEDED}">
                            <g:if test="${substituteList}">
                                <div class="ui negative button js-open-confirm-modal" data-confirm-id="deleteUserForm"
                                     data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.user", args: [user.displayName ])}"
                                     data-confirm-term-how="delete">
                                        ${message(code:'deletion.user')}
                                </div>

                                <div class="ui segment">
                                    ${message(code:'user.delete.moveToNewUser')}
                                    &nbsp;
                                    <g:select id="userReplacement" name="userReplacement" class="ui dropdown selection la-not-clearable"
                                              from="${substituteList}"
                                              optionKey="${{ User.class.name + ':' + it.id }}"
                                              optionValue="${{'(' + it.username + ') ' + it.displayName}}" />
                                </div>
                            </g:if>
                            <g:else>
                                <input disabled type="submit" class="ui button red" value="${message(code:'deletion.user')}" />
                                <semui:msg class="negative" header="${message(code: 'deletion.blocked.header')}" message="user.delete.substitute.missing" />
                            </g:else>
                        </g:if>
                        <g:elseif test="${delResult.status != DeletionService.RESULT_ERROR}">
                            <div class="ui negative button js-open-confirm-modal" data-confirm-id="deleteUserForm"
                                 data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.user", args: [user.displayName ])}"
                                 data-confirm-term-how="delete">
                                    ${message(code:'deletion.user')}
                            </div>
                        </g:elseif>
                    </g:if>
                    <g:else>
                        <input disabled type="submit" class="ui button negative" value="${message(code:'deletion.user')}" />
                    </g:else>

                </g:if>
            </g:form>

        </g:else>

        <%-- --%>

        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
            <tr>
                <th>Objekte, Referenzen</th>
                <th>Anzahl</th>
                <th>Objekt-Ids</th>
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
                                <g:if test="${info[2] == DeletionService.FLAG_WARNING}">
                                    data-content="${message(code:'user.delete.warning')}"
                                </g:if>
                                <g:if test="${info[2] == DeletionService.FLAG_SUBSTITUTE}">
                                    data-content="${message(code:'user.delete.substitute')}"
                                </g:if>
                                <g:if test="${info[2] == DeletionService.FLAG_BLOCKER}">
                                    data-content="${message(code:'user.delete.blocker')}"
                                </g:if>
                            >${info[1].size()}</span>
                        </g:if>
                        <g:else>
                            ${info[1].size()}
                        </g:else>
                    </td>
                    <td>
                        <div style="overflow-y:scroll;scrollbar-color:grey white;max-height:14.25em">
                            ${info[1].collect{ item -> (item.hasProperty('id') && item.id) ? item.id : item}.sort().join(', ')}
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <%-- --%>

        <br />

        <div class="ui list">
            <div class="item">
                <span class="ui circular label yellow">1</span>
                <span class="content">
                    ${message(code:'user.delete.warning')}
                </span>
            </div>
            <div class="item">
                <span class="ui circular label teal">2</span>
                <span class="content">
                    ${message(code:'user.delete.substitute')}
                </span>
            </div>
            <div class="item">
                <span class="ui circular label red">3</span>
                <span class="content">
                    ${message(code:'user.delete.blocker')}
                </span>
            </div>
        </div>

    </g:if>

</body>
</html>
