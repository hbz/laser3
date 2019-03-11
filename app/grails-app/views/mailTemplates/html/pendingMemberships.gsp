<!doctype html>
<html>

<body>

<div>

    <g:if test="${pendingRequests}">

    <h2 class="ui header">Manage Pending Membership Requests</h2>

    <table>
        <thead>
        <tr>
            <th>User</th>
            <th>Display Name</th>
            <th>Email</th>
            <th>Organisation</th>
            <th>Role</th>
            <th>Status</th>
            <th>Date Requested</th>
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
            </tr>
        </g:each>
        </tbody>
    </table>
    </g:if>
</div>

</body>
</html>