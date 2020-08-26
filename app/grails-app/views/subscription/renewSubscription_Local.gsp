<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} ${message(code: 'myinst.renewals', default: 'Renewal')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <semui:crumb message="myinst.renewals" class="active"/>
</semui:breadcrumbs>

<semui:messages data="${flash}"/>

<g:form action="processSimpleRenewal_Local" method="post" enctype="multipart/form-data" params="${params}">

    <div>
        <hr/>
        <table class="ui celled la-table table">
            <tbody>
                <input type="hidden" name="subscription.old_subid" value="${permissionInfo?.sub_id}"/>

                <tr>
                    <th>${message(code: 'myinst.renewalUpload.props')}</th>
                    <th>${message(code: 'default.value.label')}</th>
                </tr>
                <tr>
                    <th>${message(code: 'myinst.emptySubscription.name')}</th>
                    <td>
                        <div class="ui input">
                            <input type="text" name="subscription.name" value="${permissionInfo?.sub_name}">
                        </div>
                    </td>

                </tr>
                <tr>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <td><semui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo?.sub_startDate}" required="" /></td>
                </tr>
                <tr>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <td><semui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date" placeholder="default.date.label" value="${permissionInfo?.sub_endDate}" /></td>
                </tr>
                <tr>
                    <th>${message(code: 'default.status.label')}</th>
                    <td>
                    <g:set value="${com.k_int.kbplus.RefdataCategory.getByDesc(de.laser.helper.RDConstants.SUBSCRIPTION_STATUS)}"
                           var="rdcSubStatus"/>
                    <g:select from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.SUBSCRIPTION_STATUS)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subStatus"
                              value="${de.laser.helper.RDStore.SUBSCRIPTION_INTENDED.id.toString()}"/>
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
