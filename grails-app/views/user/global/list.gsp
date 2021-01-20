<%@ page import="com.k_int.kbplus.*; de.laser.*; de.laser.auth.*" %>
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.institutions.users')}</title>
</head>
<body>
        <g:if test="${controllerName == 'myInstitution'}">
        %{-- myInstitution has no breadcrumb yet --}%
            <g:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <g:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>
        <g:if test="${controllerName == 'user'}">
            <g:render template="/user/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            ${titleMessage} <semui:totalNumber total="${total}"/>
        </h1>

        <semui:controlButtons>
            <g:render template="/user/global/actions" />
        </semui:controlButtons>

        <g:if test="${controllerName == 'myInstitution'}">
            <g:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <g:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>

        <g:render template="/templates/user/filter" model="${filterConfig}"/>

        <g:if test="${pendingRequests && editable}">

            <h3 class="ui header"><g:message code="PendingAffiliationRequest"/></h3>

            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th>${message(code:'user.username.label')}</th>
                    <th>${message(code:'user.displayName.label')}</th>
                    <th>${message(code:'user.email')}</th>
                    <th>${message(code:'profile.membership.role')}</th>
                    <th class="la-action-info">${message(code:'default.actions.label')}</th>
                </tr>
                </thead>

                <g:each in="${pendingRequests}" var="uo">
                    <tr>
                        <th scope="row" class="la-th-column la-main-object">
                            ${uo.user.username}
                        </th>
                        <td>
                            ${uo.user.displayName}
                        </td>
                        <td>
                            ${uo.user.email}
                        </td>
                        <td>
                            <g:message code="cv.roles.${uo.formalRole?.authority}"/>
                        </td>
                        <td class="x">
                            <g:link controller="organisation" action="processAffiliation"
                                    params="${[assoc:uo.id, id:params.id ?: orgInstance?.id, cmd:'approve']}" class="ui icon positive button la-popup-tooltip la-delay"
                                    data-content="${message(code:'profile.membership.accept.button')}" data-position="top left" >
                                <i class="checkmark icon"></i>
                            </g:link>

                            <g:link controller="organisation" action="processAffiliation"
                                    params="${[assoc:uo.id, id:params.id ?: orgInstance?.id, cmd:'reject']}" class="ui icon negative button la-popup-tooltip la-delay"
                                    data-content="${message(code:'profile.membership.cancel.button')}" data-position="top left" >
                                <i class="times icon"></i>
                            </g:link>
                        </td>
                    </tr>
                </g:each>
            </table>

            <h3 class="ui header">${message(code: 'profile.membership.existing')}</h3>

        </g:if>

        <g:if test="${multipleAffiliationsWarning}">
            <div class="ui info message la-clear-before">${message(code:'user.edit.info')}</div>
        </g:if>

        <semui:messages data="${flash}" />

        <g:render template="/templates/user/list" model="${tmplConfig}"/>

          <%-- <semui:paginate total="${total}" params="${params}" /> --%>

</body>
</html>
