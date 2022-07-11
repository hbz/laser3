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
        <g:if test="${entitlements}">
            ${message(code: 'myinst.renewalUpload.noupload.note', args: [institution.name])}<br />
            <table class="ui celled la-js-responsive-table la-table table">
                <tbody>
                <input type="hidden" name="subscription.copy_docs" value="${permissionInfo?.sub_id}"/>
                <input type="hidden" name="subscription.name" value="${permissionInfo?.sub_name}"/>

                <tr><th>${message(code: 'default.select.label')}</th><th>${message(code: 'myinst.renewalUpload.props')}</th><th>${message(code: 'default.value.label')}</th>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.copyStart" value="${true}"/></th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <td><ui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo?.sub_startDate}" required="" /></td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.copyEnd" value="${true}"/></th>
                    <th>${message(code: 'default.endDate.label')}</th>
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
                <g:if test="${entitlements}">
                    <button type="submit"
                            class="ui button">${message(code: 'myinst.renewalUpload.accept')}</button>
                </g:if>
            </div>
            <br /><hr />
            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th></th>
                    <th>${message(code: 'title.label')}</th>
                    <th>${message(code: 'subscription.details.from_pkg')}</th>
                    <th>ISSN</th>
                    <th>eISSN</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'tipp.startVolume')}</th>
                    <th>${message(code: 'tipp.endVolume')}</th>
                    <th>${message(code: 'tipp.startIssue')}</th>
                    <th>${message(code: 'tipp.endIssue')}</th>



                    <th>${message(code: 'subscription.details.core_medium')}</th>
                </tr>
                </thead>
                <tbody>

                <g:each in="${entitlements}" var="e">
                    <tr>
                        <td>${++index}</td>
                        <td><input type="hidden" name="entitlements.${++counter}.tipp_id" value="${e.tipp.id}"/>
                            <input type="hidden" name="entitlements.${counter}.core_status" value="${e.coreStatus}"/>
                            <input type="hidden" name="entitlements.${counter}.coverages" value="${e.coverages}"/>
                            <%--<input type="hidden" name="entitlements.${counter}.start_date" value="${e.startDate}"/>
                            <input type="hidden" name="entitlements.${counter}.end_date" value="${e.endDate}"/>
                            <input type="hidden" name="entitlements.${counter}.coverage" value="${e.coverageDepth}"/>
                            <input type="hidden" name="entitlements.${counter}.coverage_note" value="${e.coverageNote}"/>--%>
                            ${e.name}</td>
                        <td><g:link controller="package" action="show"
                                    id="${e.tipp.pkg.id}">${e.tipp.pkg.name}(${e.tipp.pkg.id})</g:link></td>
                        <td>${e.tipp.getIdentifierValue('ISSN')}</td>
                        <td>${e.tipp.getIdentifierValue('eISSN')}</td>
                        <td>
                            <g:each in="${e.coverages}" var="covStmt">
                                <g:formatDate formatName="default.date.format.notime" date="${covStmt.startDate}"/>
                                <g:formatDate formatName="default.date.format.notime" date="${covStmt.endDate}"/>
                                ${covStmt.startVolume}
                                ${covStmt.endVolume}
                                ${covStmt.startIssue}
                                ${covStmt.endIssue}
                            </g:each>
                        </td>
                        <td>${e.coreStatus ?: 'N'}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>

        <div class="la-float-right">
            <g:if test="${entitlements}">
                <button type="submit"
                        class="ui button">${message(code: 'myinst.renewalUpload.accept')}</button>
            </g:if>
        </div>
    </div>
    <input type="hidden" name="ecount" value="${counter}"/>
</g:form>

<laser:htmlEnd />
