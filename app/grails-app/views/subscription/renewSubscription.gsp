<%@ page import="de.laser.AuditConfig;de.laser.helper.RDConstants" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} ${message(code: 'myinst.renewals')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <semui:crumb message="myinst.renewals" class="active"/>
</semui:breadcrumbs>

<semui:messages data="${flash}"/>

<g:set var="counter" value="${-1}"/>
<g:set var="index" value="${0}"/>

<g:form action="processSimpleRenewal_Consortia" method="post" params="${params}">

    <div>
        <hr/>
        <table class="ui celled la-table table">
            <tbody>
            <input type="hidden" name="subscription.old_subid" value="${permissionInfo?.sub_id}"/>

            <tr>
                <th>${message(code: 'myinst.renewalUpload.props')}</th>
                <th>${message(code: 'default.value.label')}</th>
                <th>${message(code: 'copyElementsIntoObject.audit')}</th>
            </tr>
            <tr>
                <th>${message(code: 'myinst.emptySubscription.name')}</th>
                <td>
                    <div class="ui form field">
                        <input type="text" name="subscription.name" value="${permissionInfo?.sub_name}">
                    </div>
                </td>
                <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                    <td class="center aligned">
                        <div class="ui checkbox">
                            <input type="checkbox" name="auditList" value="name" ${AuditConfig.getConfig(subscription, 'name') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>

            </tr>
            <tr>
                <th>${message(code: 'default.startDate.label')}</th>
                <td><semui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo?.sub_startDate}" required="" /></td>
                <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                    <td class="center aligned">
                        <div class="ui checkbox">
                            <input type="checkbox" name="auditList" value="startDate" ${AuditConfig.getConfig(subscription, 'startDate') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
            </tr>
            <tr>
                <th>${message(code: 'default.endDate.label')}</th>
                <td><semui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date" placeholder="default.date.label" value="${permissionInfo?.sub_endDate}" /></td>
                <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                    <td class="center aligned">
                        <div class="ui checkbox">
                            <input type="checkbox" name="auditList" value="endDate" ${AuditConfig.getConfig(subscription, 'endDate') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
            </tr>
            <tr>
                <th>${message(code: 'default.status.label')}</th>
                <td>
                    <g:set var="rdcSubStatus" value="${com.k_int.kbplus.RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_STATUS)}"/>
                    <g:select from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subStatus"
                              value="${permissionInfo?.sub_status}"/>
                </td>
                <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                    <td class="center aligned">
                        <div class="ui checkbox">
                            <input type="checkbox" name="auditList" value="status" ${AuditConfig.getConfig(subscription, 'status') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
            </tr>
            </tbody>
        </table>

        <div class="la-float-right">
            <button type="submit"
                    class="ui button">${message(code: 'myinst.renewalUpload.renew')}</button>
        </div>

    </div>
</g:form>

</body>
</html>
