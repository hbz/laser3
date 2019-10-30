<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyInfo.renewal')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="surveyInfo.renewal" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
</semui:controlButtons>

<g:if test="${(errors && (errors.size() > 0))}">
    <div>
        <ul>
            <g:each in="${errors}" var="e">
                <li>${e}</li>
            </g:each>
        </ul>
    </div>
</g:if>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:messages data="${flash}"/>

<g:set var="counter" value="${-1}"/>
<g:set var="index" value="${0}"/>

<g:form action="processRenewalwithSurvey" method="post" enctype="multipart/form-data" params="${params}">

    <div>
        <hr/>

        ${message(code: 'myinst.renewalUpload.noupload.note', args: [institution?.name])}<br/>
        <table class="ui celled la-table table">
            <tbody>
            <input type="hidden" name="subscription.old_subid" value="${permissionInfo?.sub_id}"/>

            <tr>
                <th>${message(code: 'myinst.renewalUpload.props', default: 'Subscription Properties')}</th>
                <th>${message(code: 'default.value.label', default: 'Value')}</th>
            </tr>
            <tr>
                <th>${message(code: 'subscription.details.copyElementsIntoSubscription.audit')}</th>
                <td>
                    <div class="ui checkbox">
                        <input type="checkbox" id="subscription.isCopyAuditOn" name="subscription.isCopyAuditOn" checked />
                        <label for="subscription.isCopyAuditOn">${message(code:'subscription.details.copyElementsIntoSubscription.copyAudit')}</label>
                    </div>
                </td>
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
                <th>${message(code: 'default.startDate.label', default: 'Start Date')}</th>
                <td><semui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo?.sub_startDate}" required="" /></td>
            </tr>
            <tr>
                <th>${message(code: 'default.endDate.label', default: 'End Date')}</th>
                <td><semui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date" placeholder="default.date.label" value="${permissionInfo?.sub_endDate}" /></td>
            </tr>
            <tr>
                <th>${message(code: 'default.status.label')}</th>
                <td>
                <g:set var="rdcSubStatus" value="${com.k_int.kbplus.RefdataCategory.findByDesc('Subscription Status')}"/>
                <g:select from="${com.k_int.kbplus.RefdataValue.findAllByOwner(rdcSubStatus)}" class="ui dropdown"
                          optionKey="id"
                          optionValue="${{ it.getI10n('value') }}"
                          name="subStatus"
                          value="${de.laser.helper.RDStore.SUBSCRIPTION_INTENDED.id.toString()}"
                          disabled="${true}"/>
                </td>
            </tr>
            </tbody>
        </table>

        <div class="la-float-right">
            <button type="submit"
                    class="ui button">${message(code: 'myinst.renewalUpload.renew')}</button>
        </div>

    </div>
    <input type="hidden" name="ecount" value="${counter}"/>
</g:form>

</body>
</html>
<r:script>
    formularFieldsDisableIfAuditOn($('input[name="subscription.isCopyAuditOn"]'));

    $('input[name="subscription.isCopyAuditOn"]').change( function() {
        formularFieldsDisableIfAuditOn($(this));
    });

    function formularFieldsDisableIfAuditOn(that) {
        var isCopyAuditOn = $(that).is(":checked");
        $('input[name="subscription.name"]').prop( "disabled", isCopyAuditOn);
        $('input[name="subscription.start_date"]').prop( "disabled", isCopyAuditOn);
        $('input[name="subscription.end_date"]').prop( "disabled", isCopyAuditOn);
        if (isCopyAuditOn) {
            $('.ui.dropdown > select[name=subStatus]').parent('.dropdown').addClass('disabled');
        } else {
            $('.ui.dropdown > select[name=subStatus]').parent('.dropdown').removeClass('disabled');
        }
        // $('.ui.dropdown > select[name=subStatus]').parent('.dropdown').toggleClass('disabled');
        $('.ui.dropdown > select[name=subStatus]').prop('disabled', isCopyAuditOn);
    }
</r:script>