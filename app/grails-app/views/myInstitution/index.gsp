<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinstadmin.title', default:'Institutional Admin Dash')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
</semui:breadcrumbs>

<div>
    <hr>

    <g:if test="${pendingRequestsOrg.size() > 0}">

        <h3 class="ui header">${message(code: "PendingAffiliationRequest")}</h3>

        <table class="ui celled la-table table">

            <thead>
            <tr>
                <th>${message(code: "profile.user")}</th>
                <th>${message(code: "profile.display")}</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>${message(code: "profile.membership.date2")}</th>
                <th>${message(code: "profile.membership.actions")}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${pendingRequestsOrg}" var="req">
                <tr>
                    <td>${req.user.username}</td>
                    <td>${req.user.displayName}</td>
                    <td>${req.user.email}</td>
                    <td><g:message code="cv.roles.${req.formalRole?.authority}"/></td>
                    <td><g:message code="cv.membership.status.${req.status}"/></td>
                    <td><g:formatDate format="dd MMMM yyyy" date="${req.dateRequested}"/></td>
                    <td class="x">
                        <g:link action="actionAffiliationRequestOrg"
                                params="${[req: req.id, act: 'approve']}" class="ui icon positive button">
                            <i class="checkmark icon"></i>
                        </g:link>
                        <g:link action="actionAffiliationRequestOrg"
                                params="${[req: req.id, act: 'deny']}" class="ui icon negative button">
                            <i class="times icon"></i>
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

    </g:if><g:else>
    <h3 class="ui header">${message(code: "NoPendingAffiliationRequest")}</h3>
    </g:else>
    <hr>


    <table class="ui celled la-table table">
        <tr>
            <th colspan="3">Note attached to</th>
        </tr>
        <tr>
            <th>Note</th>
        </tr>
        <g:each in="${userAlerts}" var="ua">
            <tr>
                <td colspan="3">
                    <g:if test="${ua.rootObj.class.name == 'com.k_int.kbplus.License'}">
                        <span class="label label-info">${message(code: 'license')}</span>
                        <em><g:link action="show"
                                    controller="licenseDetails"
                                    id="${ua.rootObj.id}">${ua.rootObj.reference}</g:link></em>
                    </g:if>
                    <g:elseif test="${ua.rootObj.class.name == 'com.k_int.kbplus.Subscription'}">
                        <span class="label label-info">${message(code: 'subscription')}</span>
                        <em><g:link action="index"
                                    controller="subscriptionDetails"
                                    id="${ua.rootObj.id}">${ua.rootObj.name}</g:link></em>
                    </g:elseif>
                    <g:elseif test="${ua.rootObj.class.name == 'com.k_int.kbplus.Package'}">
                        <span class="label label-info">${message(code: 'package')}</span>
                        <em><g:link action="show"
                                    controller="packageDetails"
                                    id="${ua.rootObj.id}">${ua.rootObj.name}</g:link></em>
                    </g:elseif>
                    <g:else>
                        Unhandled object type attached to alert: ${ua.rootObj.class.name}:${ua.rootObj.id}
                    </g:else>
                </td>
            </tr>
            <g:each in="${ua.notes}" var="n">
                <tr>
                    <td>
                        ${n.owner.content}<br/>

                        <div class="pull-right"><i>${n.owner.type?.value} (
                        <g:if test="${n.alert.sharingLevel == 2}">Shared with KB+ Community</g:if>
                        <g:elseif test="${n.alert.sharingLevel == 1}">JC Only</g:elseif>
                        <g:else>Private</g:else>
                        ) By ${n.owner.user?.displayName} on <g:formatDate formatName="default.date.format.notime"
                                                                           date="${n.alert.createTime}"/></i></div>
                    </td>
                </tr>
            </g:each>
        </g:each>
    </table>
</div>
</body>
</html>
