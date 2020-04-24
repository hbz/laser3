<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${message(code:'license.nav.permissionInfo')}</title>
</head>

<body>

    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    <br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <g:render template="nav" />

      <h2 class="ui header">${message(code:'subscription.details.permissionInfo.orgs_granted')}</h2>
      <table class="ui celled la-table table">
        <thead>
          <tr>
            <th>Organisation</th>
            <th>${message(code:'subscription.details.permissionInfo.roles_and_perm')}</th>
          </tr>
        </thead>
        <g:each in="${license.orgLinks}" var="ol">
          <tr>
            <td>${ol.org.name}</td>
            <td>
              ${message(code:'subscription.license.connection', args:["${ol.roleType?.getI10n('value')}"])}<br/>
              ${message(code:'subscription.details.permissionInfo.role.info')}<br/>
              <ul>
                <g:each in="${ol.roleType?.sharedPermissions}" var="sp">
                  <li>${message(code:"default.perm.${sp.perm.code}", default:"${sp.perm.code}")}
                      <g:if test="${license.checkPermissions(sp.perm.code,user)}">
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

      <h2 class="ui header">${message(code:'subscription.details.user.permissions')}</h2>
      <table class="ui celled la-table table">
        <thead>
          <tr>
          <th>${message(code:'subscription.details.permissionInfo.aff_via')}</th>
            <th>${message(code:'default.permissions.label')}</th>
          </tr>
        </thead>
        <g:each in="${user.affiliations}" var="ol">
          <g:if test="${ol.status==1}">
            <tr>
              <td>${message(code:'subscription.details.permissionInfo.aff_to', args:[ol.org?.name])} <g:message code="cv.roles.${ol.formalRole?.authority}"/></td>
              <td>
                <ul>
                  <g:each in="${ol.formalRole.grantedPermissions}" var="gp">
                    <li>${message(code:"default.perm.${gp.perm.code}", default:"${gp.perm.code}")}</li>
                  </g:each>
                </ul>
              </td>
            </tr>
            <g:each in="${ol.org.outgoingCombos}" var="oc">
              <tr>
                <td> --&gt; ${message(code:'subscription.details.permissionInfo.org_rel', args:[oc.toOrg.name,oc.type.value])}</td>
                <td>
                  <ul>
                    <g:each in="${oc.type.sharedPermissions}" var="gp">
                      <li>${message(code:"default.perm.${gp.perm.code}", default:"${gp.perm.code}")}</li>
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
