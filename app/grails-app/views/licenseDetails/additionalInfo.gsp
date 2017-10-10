<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'license.label', default:'License')}</title>
</head>

<body>

    <div class="container">
      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>

    <div class="container">
        <h1>${license.licensee?.name} ${license.type?.getI10n("value")} ${message(code:'license.label', default:'License')} : ${license.reference}</h1>

        <g:render template="nav" />
    </div>



    <div class="container">
      <h2>${message(code:'license.additionalInfo.perms', default:'Permissions for user')}</h2>
      <table  class="table table-striped table-bordered">
      </table>

      <h2>${message(code:'subscription.details.additionalInfo.orgs_granted', default:'The following organisations are granted the listed permissions from this license')}</h2>
      <table  class="table table-striped table-bordered">
        <tr>
          <th>Organisation</th><th>${message(code:'subscription.details.additionalInfo.roles_and_perm', default:'Roles and Permissions')}</th>
        </tr>
        <g:each in="${license.orgLinks}" var="ol">
          <tr>
            <td>${ol.org.name}</td>
            <td>
              ${message(code:'subscription.license.connection', args:["${ol.roleType?.getI10n('value')}"])}<br/>
              ${message(code:'subscription.details.additionalInfo.role.info', default:'This role grants the following permissions to members of that org whose membership role also includes the permission')}<br/>
              <ul>
                <g:each in="${ol.roleType?.sharedPermissions}" var="sp">
                  <li>${message(code:"default.perm.${sp.perm.code}", default:"${sp.perm.code}")}
                      <g:if test="${license.checkPermissions(sp.perm.code,user)}">
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

      <h2>${message(code:'subscription.details.user.permissions', default:'Logged in user permissions')}</h2>
      <table  class="table table-striped table-bordered">
        <tr>
          <th>${message(code:'subscription.details.additionalInfo.aff_via', default:'Affiliated via Role')}</th><th>${message(code:'default.permissions.label', default:'Permissions')}</th>
        </tr>
        <g:each in="${user.affiliations}" var="ol">
          <g:if test="${((ol.status==1) || (ol.status==3))}">
            <tr>
              <td>${message(code:'subscription.details.additionalInfo.aff_to', args:[ol.org?.name])} <g:message code="cv.roles.${ol.formalRole?.authority}"/></td>
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
                <td> --&gt; ${message(code:'subscription.details.additionalInfo.org_rel', args:[oc.toOrg.name,oc.type.value])}</td>
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
   
    </div>


</body>
</html>
