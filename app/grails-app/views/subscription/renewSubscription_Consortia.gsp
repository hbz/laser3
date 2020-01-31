<%@ page import="de.laser.AuditConfig;de.laser.helper.RDConstants" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} ${message(code: 'myinst.renewals', default: 'Renewal')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <semui:crumb message="myinst.renewals" class="active"/>
</semui:breadcrumbs>

<g:if test="${(errors && (errors.size() > 0))}">
    <div>
        <ul>
            <g:each in="${errors}" var="e">
                <li>${e}</li>
            </g:each>
        </ul>
    </div>
</g:if>

<semui:messages data="${flash}"/>

<g:set var="counter" value="${-1}"/>
<g:set var="index" value="${0}"/>

<g:form action="processSimpleRenewal_Consortia" method="post" enctype="multipart/form-data" params="${params}">

    <div>
        <hr/>
        ${message(code: 'myinst.renewalUpload.noupload.note', args: [institution?.name])}<br/>
        <table class="ui celled la-table table">
            <tbody>
            <input type="hidden" name="subscription.old_subid" value="${permissionInfo?.sub_id}"/>

            <tr>
                <th>${message(code: 'myinst.renewalUpload.props', default: 'Subscription Properties')}</th>
                <th>${message(code: 'default.value.label', default: 'Value')}</th>
                <th>${message(code: 'subscription.details.copyElementsIntoSubscription.audit')}</th>
            </tr>
            <tr>
                <th>${message(code: 'myinst.emptySubscription.name')}</th>
                <td>
                    <div class="ui form field">
                        <input type="text" name="subscription.name" value="${permissionInfo?.sub_name}">
                    </div>
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="name" ${AuditConfig.getConfig(subscription, 'name') ? 'checked': ''} />
                    </div>
                </td>

            </tr>
            <tr>
                <th>${message(code: 'default.startDate.label', default: 'Start Date')}</th>
                <td><semui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo?.sub_startDate}" required="" /></td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="startDate" ${AuditConfig.getConfig(subscription, 'startDate') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'default.endDate.label', default: 'End Date')}</th>
                <td><semui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date" placeholder="default.date.label" value="${permissionInfo?.sub_endDate}" /></td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="endDate" ${AuditConfig.getConfig(subscription, 'endDate') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'default.status.label')}</th>
                <td>
                    <g:set var="rdcSubStatus" value="${com.k_int.kbplus.RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_STATUS)}"/>
                    <g:select from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subStatus"
                              value="${permissionInfo?.sub_status}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="status" ${AuditConfig.getConfig(subscription, 'status') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.details.type')}</th>
                <td>
                    <g:set var="rdcSubType" value="${com.k_int.kbplus.RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_TYPE)}"/>
                    <g:select from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subType"
                              value="${permissionInfo?.sub_type}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="type" ${AuditConfig.getConfig(subscription, 'type') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.form.label')}</th>
                <td>
                    <g:set var="rdcSubForm" value="${com.k_int.kbplus.RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_FORM)}"/>
                    <g:select from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subForm"
                              value="${permissionInfo?.sub_form}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="form" ${AuditConfig.getConfig(subscription, 'form') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.resource.label')}</th>
                <td>
                    <g:set var="rdcSubResource" value="${com.k_int.kbplus.RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_RESOURCE)}"/>
                    <g:select from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subResource"
                              value="${permissionInfo?.sub_resource}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="resource" ${AuditConfig.getConfig(subscription, 'resource') ? 'checked': ''} />
                    </div>
                </td>
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
