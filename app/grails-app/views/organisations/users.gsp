<%@ page import="com.k_int.kbplus.Org" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>

    <div>
      <h1>${orgInstance.name}</h1>
      <g:render template="nav" contextPath="." />
    </div>

    <div>
      

      <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </g:if>


      <table  class="ui celled striped table">
        <tr>
          <th>${message(code:'user.label', default:'User')}</th>
          <th>${message(code:'user.email', default:'Email')}</th>
          <th>${message(code:'user.sys_role', default:'System Role')}</th>
          <th>${message(code:'user.inst_role', default:'Institutional Role')}</th>
          <th>${message(code:'user.status', default:'Status')}</th>
          <th>${message(code:'user.actions', default:'Actions')}</th>
        </tr>

        <g:each in="${users}" var="userOrg">
          <tr>
            <td><g:link controller="userDetails" action="edit" id="${userOrg[0].user.id}">${userOrg[0].user.displayName} ${userOrg[0].user.defaultDash?.name?"(${userOrg[0].user.defaultDash.name})":""}</g:link></td>
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
            <td>
              <g:if test="${editable}">
              <g:if test="${((userOrg[0].status==1 ) || (userOrg[0].status==3)) }">
                <g:link controller="organisations" action="revokeRole" params="${[grant:userOrg[0].id, id:params.id]}" class="ui button">${message(code:'default.button.revoke.label', default:'Revoke')}</g:link>
              </g:if>
              <g:else>
                <g:link controller="organisations" action="enableRole" params="${[grant:userOrg[0].id, id:params.id]}" class="ui button">${message(code:'default.button.allow.label', default:'Allow')}</g:link>
              </g:else>
              <g:link controller="organisations" action="deleteRole" params="${[grant:userOrg[0].id, id:params.id]}" class="ui button">${message(code:'default.button.delete_link.label', default:'Delete Link')}</g:link>
            </g:if>
            </td>
          </tr>
        </g:each>
      </table>
    </div>
  </body>
</html>
