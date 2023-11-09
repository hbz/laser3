<laser:htmlStart message="myinst.renewals" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <ui:crumb message="myinst.renewals" class="active"/>
</ui:breadcrumbs>

<g:if test="${(errors && (errors.size() > 0))}">
    <div>
        <ul>
            <g:each in="${errors}" var="e">
                <li>${e}</li>
            </g:each>
        </ul>
    </div>
</g:if>

<ui:messages data="${flash}"/>

<g:set var="counter" value="${-1}"/>
<g:set var="index" value="${0}"/>
<g:form action="processRenewal" method="post" enctype="multipart/form-data" params="${params}">

    <div>
        <hr />

            ${message(code: 'myinst.renewalUpload.noupload.note', args: [institution.name])}<br />
            <table class="ui celled la-js-responsive-table la-table table">
                <tbody>
                <input type="hidden" name="subscription.copy_docs" value="${permissionInfo?.sub_id}"/>
                <input type="hidden" name="subscription.name" value="${permissionInfo?.sub_name}"/>

                <tr><th>${message(code: 'default.select.label')}</th><th>${message(code: 'myinst.renewalUpload.props')}</th><th>${message(code: 'default.value.label')}</th>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.copyStart" value="${true}"/></th>
                    <th>${message(code: 'default.startDate.label.shy')}</th>
                    <td><ui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo?.sub_startDate}" required="" /></td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.copyEnd" value="${true}"/></th>
                    <th>${message(code: 'default.endDate.label.shy')}</th>
                    <td><ui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date" placeholder="default.date.label" value="${permissionInfo?.sub_endDate}" /></td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.copyDocs" value="${true}"/></th>
                    <th>${message(code: 'myinst.renewalUpload.copy')}</th>
                    <td>${message(code: 'subscription')}: ${permissionInfo?.sub_name}</td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.copyLicense" value="${permissionInfo?.sub_license ? true : false}"/></th>
                    <th>${message(code: 'myinst.renewalUpload.copyLiense')}</th>
                    <td>${message(code: 'license')}: ${permissionInfo?.sub_license?:message(code: 'myinst.renewalUpload.noLicensetoSub')}</td>
                </tr>
                </tbody>
            </table>

            <div class="la-float-right">

                    <button type="submit"
                            class="ui button">${message(code: 'myinst.renewalUpload.accept')}</button>

            </div>

    </div>
    <input type="hidden" name="ecount" value="${counter}"/>
</g:form>

<laser:htmlEnd />
