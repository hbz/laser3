<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>


    <semui:breadcrumbs>
      <g:if test="${params.shortcode}">
        <semui:crumb controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" text="${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}" />
      </g:if>
      <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"  text="${subscriptionInstance.name}" />
      <semui:crumb class="active" text="${message(code:'default.permissions.label', default:'Permissions')}" />
      <g:if test="${editable}">
        <li class="pull-right"><span class="badge badge-warning">${message(code:'default.editable', default:'Editable')}</span>&nbsp;</li>
      </g:if>
    </semui:breadcrumbs>

    <div>
      <h1 class="ui header">${subscriptionInstance?.name} - ${message(code:'subscription.details.user.permissions', default:'Permissions against Current User')}</h1>
      <g:render template="nav" contextPath="." />
    </div>


   <div>

      <h2>${message(code:'subscription.details.permissionInfo.orgs_granted', default:'The following organisations are granted the listed permissions from this license')}</h2>
      <table  class="ui celled striped table">
        <tr>
          <th>${message(code:'org.label', default:'Organisation')}</th><th>${message(code:'subscription.details.permissionInfo.roles_and_perm', default:'Roles and Permissions')}</th>
        </tr>
        <g:each in="${subscriptionInstance.orgRelations}" var="ol">
          <tr>
            <td>${ol.org.name}</td>
            <td>

              <g:message code="subscription.license.connection" args="${[ol.roleType?.value?:'']}"/><br/>
              ${message(code:'subscription.details.permissionInfo.role.info', default:'This role grants the following permissions to members of that org whose membership role also includes the permission')}<br/>
              <ul>
                <g:each in="${ol.roleType?.sharedPermissions}" var="sp">
                  <li><g:message code="default.perm.${sp.perm.code}" />
                      <g:if test="${subscriptionInstance.checkPermissions(sp.perm.code,user)}">
                        [${message(code:'default.perm.granted', default:'Granted')}]
                      </g:if>
                      <g:else>
                        [${message(code:'default.perm.not_granted', default:'Not granted')}]
                      </g:else>

                  </li>
                </g:each>
              </ul>
            </td>
          </tr>
        </g:each>
      </table>

      <h2>${message(code:'subscription.details.permissionInfo.user_perms', default:'Logged in user permissions')}</h2>
      <table  class="ui celled striped table">
        <tr>
          <th>${message(code:'subscription.details.permissionInfo.aff_via', default:'Affiliated via Role')}</th><th>${message(code:'default.permissions.label', default:'Permissions')}</th>
        </tr>
        <g:each in="${user.affiliations}" var="ol">
          <g:if test="${((ol.status==1)||(ol.status==3))}">
            <tr>
              <td>${message(code:'subscription.details.permissionInfo.aff_to', args:[ol.org?.name])} <b><g:message code="cv.roles.${ol.formalRole?.authority}"/></b> (${message(code:"cv.membership.status.${ol.status}")})</td>
              <td>
                <ul>
                  <g:each in="${ol.formalRole?.grantedPermissions}" var="gp">
                    <li><g:message code="default.perm.${gp.perm.code}" /></li>
                  </g:each>
                </ul>
              </td>
            </tr>
            <g:each in="${ol.org.outgoingCombos}" var="oc">
              <tr>
                <td> --&gt; ${message(code:'subscription.details.permissionInfo.org_rel', args:[oc.toOrg.name, oc.type.value])}</td>
                <td>
                  <ul>
                    <g:each in="${oc.type.sharedPermissions}" var="gp">
                      <li><g:message code="default.perm.${gp.perm.code}" /></li>
                    </g:each>
                  </ul>
                </td>
              </tr>
            </g:each>
          </g:if>
        </g:each>
      </table>



    </div>
    
  </body>
</html>
