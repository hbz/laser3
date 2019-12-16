<%@ page import="com.k_int.kbplus.*; com.k_int.kbplus.auth.*" %>
<laser:serviceInjection />
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.users')}</title>
  </head>
    <body>

        <g:render template="${breadcrumb}" model="${[inContextOrg: inContextOrg, orgInstance: orgInstance, departmentalView: departmentalView, institutionalView: institutionalView, params:params ]}"/>

        <semui:controlButtons>
            <g:render template="/user/actions" />
        </semui:controlButtons>

        <h1 class="ui icon header la-clear-before la-noMargin-top">
            <semui:headerIcon />
            ${titleMessage}
            <semui:totalNumber total="${total}"/>
        </h1>

        <g:if test="${navPath && navConfiguration}">
            <g:render template="${navPath}" model="${navConfiguration}"/>
        </g:if>

        <g:render template="/templates/user/userFilterFragment" model="${filterConfig}"/>

        <g:if test="${pendingRequests && editable}">

            <h3 class="ui header"><g:message code="PendingAffiliationRequest"/></h3>

            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th>${message(code:'user.username.label')}</th>
                    <th>${message(code:'user.displayName.label')}</th>
                    <th>${message(code:'user.email')}</th>
                    <th>${message(code:'profile.membership.role')}</th>
                    <th>${message(code: "profile.membership.date2")}</th>
                    <th>${message(code:'user.status')}</th>
                    <th class="la-action-info">${message(code:'default.actions')}</th>
                </tr>
                </thead>

                <g:each in="${pendingRequests}" var="uo">
                    <tr>
                        <td class="la-main-object">
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

        <g:render template="/templates/user/userListFragment" model="${tableConfig}"/>

          <%-- <semui:paginate total="${total}" params="${params}" /> --%>

    </body>
</html>
