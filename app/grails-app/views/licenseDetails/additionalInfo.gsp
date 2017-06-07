<!doctype html>
<html>
    <head>
        <meta name="layout" content="mmbootstrap"/>
        <title>${message(code:'laser', default:'LAS:eR')} Licence</title>
</head>

<body>

    <div class="container">
      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>

    <div class="container">
        <h1>${license.licensee?.name} ${license.type?.value} Licence : ${license.reference}</h1>

        <g:render template="nav" />
    </div>



    <div class="container">
      <h2>${message(code:'license.additionalInfo.permissions', default:'Permissions for user')}</h2>
      <table  class="table table-striped table-bordered">
      </table>

      <h2>${message(code:'license.additionalInfo.orgsGrant')}</h2>
      <table  class="table table-striped table-bordered">
        <tr>
          <th>Organisation</th><th>${message(code:'license.additionalInfo.table.rolesandPermissions')}</th>
        </tr>
        <g:each in="${license.orgLinks}" var="ol">
          <tr>
            <td>${ol.org.name}</td>
            <td>
                ${message(code:'license.additionalInfo.table.link')} ${ol.id} ${message(code:'license.additionalInfo.table.linkRole')}${ol.roleType?.value}.<br/>
                ${message(code:'license.additionalInfo.table.roleGrant')}<br/>
              <ul>
                <g:each in="${ol.roleType?.sharedPermissions}" var="sp">
                  <li>${sp.perm.code} 
                      <g:if test="${license.checkPermissions(sp.perm.code,user)}">
                        [Granted]
                      </g:if>
                      <g:else>
                        [Not granted]
                      </g:else>
 
                  </li>
                </g:each>
              </ul>
            </td>
          </tr>
        </g:each>
      </table>

      <h2>${message(code:'license.additionalInfo.loggedInPerms')}</h2>
      <table  class="table table-striped table-bordered">
        <tr>
          <th>${message(code:'license.additionalInfo.affiliatedRole')}</th><th>${message(code:'license.additionalInfo.permissions')}</th>
        </tr>
        <g:each in="${user.affiliations}" var="ol">
          <g:if test="${((ol.status==1) || (ol.status==3))}">
            <tr>
              <td>${message(code:'license.additionalInfo.affiliatedTo')} ${ol.org?.name} ${message(code:'license.additionalInfo.withRole')} <g:message code="cv.roles.${ol.formalRole?.authority}"/></td>
              <td>
                <ul>
                  <g:each in="${ol.formalRole.grantedPermissions}" var="gp">
                    <li>${gp.perm.code}</li>
                  </g:each>
                </ul>
              </td>
            </tr>
            <g:each in="${ol.org.outgoingCombos}" var="oc">
              <tr>
                <td> --&gt; This org is related to ${oc.toOrg.name} ( ${oc.type.value} )</td>
                <td>
                  <ul>
                    <g:each in="${oc.type.sharedPermissions}" var="gp">
                      <li>${gp.perm.code}</li>
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
