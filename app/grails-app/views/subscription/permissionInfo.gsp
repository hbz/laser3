<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code:'default.permissionInfo.label')}</title>
  </head>

  <body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
    <h1 class="ui icon header la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${subscriptionInstance}" field="name" />
    </h1>
    <semui:anualRings object="${subscriptionInstance}" controller="subscription" action="permissionInfo" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <g:render template="nav" contextPath="." />

      <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id in [subscriptionInstance.getConsortia()?.id,subscriptionInstance.getCollective()?.id])}">
          <g:render template="message" />
      </g:if>

    <h3 class="ui header">${message(code:'subscription.details.permissionInfo.orgs_granted')}</h3>

        <table  class="ui celled la-table table">
            <thead>
                <tr>
                    <th>${message(code:'org.label')}</th>
                    <th>${message(code:'subscription.details.permissionInfo.roles_and_perm')}</th>
                </tr>
            </thead>
            <g:each in="${subscriptionInstance.orgRelations}" var="ol">
                <tr>
                    <td>${ol.org.name}</td>
                    <td>
                        <g:message code="subscription.license.connection" args="${[ol.roleType?.value?:'']}"/>
                        <br/>
                        ${message(code:'subscription.details.permissionInfo.role.info')}
                        <br/>
                      <ul>
                        <g:each in="${ol.roleType?.sharedPermissions}" var="sp">
                          <li><g:message code="default.perm.${sp.perm.code}" />
                              <g:if test="${subscriptionInstance.checkPermissions(sp.perm.code,user)}">
                                [${message(code:'default.perm.granted')}]
                              </g:if>
                              <g:else>
                                [${message(code:'default.perm.not_granted')}]
                              </g:else>

                          </li>
                        </g:each>
                      </ul>
                    </td>
                </tr>
            </g:each>
        </table>

      <h3 class="ui header">${message(code:'subscription.details.permissionInfo.user_perms')}</h3>

      <table  class="ui celled la-table table">
        <thead>
          <tr>
            <th>${message(code:'subscription.details.permissionInfo.aff_via')}</th><th>${message(code:'default.permissions.label')}</th>
          </tr>
        </thead>
        <g:each in="${user.affiliations}" var="ol">
          <g:if test="${ol.status==1}">
            <tr>
              <td>${message(code:'subscription.details.permissionInfo.aff_to', args:[ol.org?.name])} <strong><g:message code="cv.roles.${ol.formalRole?.authority}"/></strong> (${message(code:"cv.membership.status.${ol.status}")})</td>
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

    
  </body>
</html>
