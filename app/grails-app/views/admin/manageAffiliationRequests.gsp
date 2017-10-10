<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Manage Affiliation Requests</title>
  </head>

  <body>

      <laser:breadcrumbs>
          <laser:crumb message="menu.admin.dash" controller="admin" action="index"/>
          <laser:crumb text="Manage Affiliation Requests" class="active"/>
      </laser:breadcrumbs>

      <laser:flash data="${flash}" />

    <div class="container">

        <h2>Manage Pending Membership Requests</h2>

        <table class="table table-striped table-bordered table-condensed">
            <thead>
                <tr>
                    <th>User</th>
                    <th>Display Name</th>
                    <th>Email</th>
                    <th>Organisation</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Date Requested</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${pendingRequests}" var="req">
                <tr>
                    <td>${req.user.username}</td>
                    <td>${req.user.displayName}</td>
                    <td>${req.user.email}</td>
                    <td>${req.org.name}</td>
                    <td><g:message code="cv.roles.${req.formalRole?.authority}"/></td>
                    <td><g:message code="cv.membership.status.${req.status}"/></td>
                    <td><g:formatDate format="dd MMMM yyyy" date="${req.dateRequested}"/></td>
                    <td>
                        <g:link controller="admin" action="actionAffiliationRequest" params="${[req:req.id, act:'approve']}" class="btn">Approve</g:link>
                        <g:link controller="admin" action="actionAffiliationRequest" params="${[req:req.id, act:'deny']}" class="btn">Deny</g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>




  </body>
</html>
