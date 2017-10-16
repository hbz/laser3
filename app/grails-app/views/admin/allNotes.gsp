<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} Data import explorer</title>
  </head>

  <body>

    <laser:breadcrumbs>
        <laser:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <laser:crumb text="All Notes" class="active"/>
    </laser:breadcrumbs>
  
    <div class="container">
      <table class="table table-bordered">
        <tr>
          <th colspan="4">Note attached to</th>
        </tr>
        <tr>
          <th>Date</th>
          <th>Sharing</th>
          <th>Note</th>
          <th>By</th>
        </tr>
        <g:each in="${alerts}" var="ua">
          <tr>
            <td colspan="4">
              <g:if test="${ua.license}">
                <span class="label label-info">License</span>
                <em><g:link action="index"
                        controller="licenseDetails" 
                        id="${ua.license.id}">${ua.license.reference}</g:link></em>
           
              </g:if>
            </td>
          </tr>
          <tr>
            <td><g:formatDate format="dd MMMM yyyy" date="${ua.alert.createTime}" /></td>
            <td>
              <g:if test="${ua.alert.sharingLevel==2}">- Shared with KB+ Community -</g:if>
              <g:elseif test="${ua.alert.sharingLevel==1}">- JC Only -</g:elseif>
              <g:else>- Private -</g:else>
            </td>
            <td>
              ${ua.owner.content}
            </td>
            <td>
              ${ua.alert?.createdBy?.displayName}
            </td>
          </tr>
        </g:each>
      </table>
    </div>
  </body>
</html>
