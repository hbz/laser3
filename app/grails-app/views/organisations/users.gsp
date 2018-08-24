<%@ page import="com.k_int.kbplus.Org" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>

    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

      <h1 class="ui header"><semui:headerIcon />

        ${orgInstance.name}
      </h1>

      <g:render template="nav" contextPath="." />

      <semui:messages data="${flash}" />

      <table  class="ui celled la-table table">
        <thead>
          <tr>
            <th>${message(code:'user.label', default:'User')}</th>
            <th>${message(code:'user.email', default:'Email')}</th>
            <th>${message(code:'user.sys_role', default:'System Role')}</th>
            <th>${message(code:'user.inst_role', default:'Institutional Role')}</th>
            <th>${message(code:'user.status', default:'Status')}</th>
            <th>${message(code:'user.actions', default:'Actions')}</th>
          </tr>
        </thead>

        <g:each in="${users}" var="userOrg">
          <tr>
            <td>
                <g:link controller="userDetails" action="edit" id="${userOrg[0].user.id}">
                    ${userOrg[0].user.displayName}
                  <g:if test="${userOrg[0].user.defaultDash?.name}">
                    <br>
                    ${userOrg[0].user.defaultDash.name}
                  </g:if>
                </g:link>
            </td>
            <td>
                ${userOrg[0].user.email}
            </td>
            <td>
              <g:if test="${userOrg[1]}">
                <ul>
                  <g:each in="${userOrg[1]}" var="admRole">
                    <li>${admRole}</li>
                  </g:each>
                </ul>
                </g:if>
            </td>
            <td><g:message code="cv.roles.${userOrg[0].formalRole?.authority}"/></td>
            <td>
              <g:message code="cv.membership.status.${userOrg[0].status}" />
            </td>
            <td class="x">
              <g:if test="${editable}">
                <g:if test="${((userOrg[0].status==1 ) || (userOrg[0].status==3)) }">
                  <g:link controller="organisations" action="revokeRole" params="${[grant:userOrg[0].id, id:params.id]}" class="ui icon negative button">
                    <i class="times icon"></i>
                  </g:link>
                </g:if>
                <g:else>
                  <g:link controller="organisations" action="enableRole" params="${[grant:userOrg[0].id, id:params.id]}" class="ui icon positive button">
                    <i class="checkmark icon"></i>
                  </g:link>
                </g:else>
                <g:link controller="organisations" action="deleteRole" params="${[grant:userOrg[0].id, id:params.id]}" class="ui icon negative button">
                  <i class="trash alternate icon"></i>
                </g:link>
              </g:if>
            </td>
          </tr>
        </g:each>
      </table>

  </body>
</html>
