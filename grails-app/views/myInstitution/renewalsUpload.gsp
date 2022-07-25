<laser:htmlStart message="myinst.renewalUpload.label" />

  <ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
    <ui:crumb message="menu.institutions.imp_renew" class="active" />
  </ui:breadcrumbs>

    <ui:form>
      <g:form class="ui form" action="renewalsUpload" method="post" enctype="multipart/form-data" params="${params}">
          <input type="file" id="renewalsWorksheet" name="renewalsWorksheet"/><br /><br />
        <button type="submit" class="ui button">${message(code:'myinst.renewalUpload.upload')}</button>
      </g:form>
    </ui:form>

    <g:if test="${(errors && (errors.size() > 0))}">
      <div>
        <ul>
          <g:each in="${errors}" var="e">
            <li>${e}</li>
          </g:each>
        </ul>
      </div>
    </g:if>

   <ui:messages data="${flash}" />
   <g:set var="counter" value="${-1}" />
   <g:set var="index" value="${0}"/>
      <g:form action="processRenewal" method="post" enctype="multipart/form-data" params="${params}">


        <div>
        <hr />
            <g:if test="${entitlements}">
                ${message(code:'myinst.renewalUpload.upload.note', args:[institution.name])}<br />
                <table class="ui celled la-js-responsive-table la-table table">
                    <tbody>
                    <input type="hidden" name="subscription.copy_docs" value="${permissionInfo?.sub_id}"/>
                    <input type="hidden" name="subscription.name" value="${permissionInfo?.sub_name}"/>

                    <tr><th>${message(code:'default.select.label')}</th><th >${message(code:'myinst.renewalUpload.props')}</th><th>${message(code:'default.value.label')}</th></tr>
                    <tr>
                        <th><g:checkBox name="subscription.copyStart" value="${true}" /></th>
                        <th>${message(code:'default.startDate.label')}</th>
                        <td><ui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo?.sub_startDate}" required="" /></td>
                    </tr>
                    <tr>
                        <th><g:checkBox name="subscription.copyEnd" value="${true}" /></th>
                        <th>${message(code:'default.endDate.label')}</th>
                        <td><ui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date" placeholder="default.date.label" value="${permissionInfo?.sub_endDate}" /></td>
                    </tr>
                    <tr>
                        <th><g:checkBox name="subscription.copyDocs" value="${true}" /></th>
                        <th>${message(code:'myinst.renewalUpload.copy')}</th>
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
                        <button type="submit" class="ui button">${message(code:'myinst.renewalUpload.accept')}</button>
                    </g:if>
                </div>
                <br /><hr />
                <table class="ui celled la-js-responsive-table la-table table">
                    <thead>
                    <tr>
                        <th></th>
                        <th>${message(code:'title.label')}</th>
                        <th>${message(code:'subscription.details.from_pkg')}</th>
                        <th>ISSN</th>
                        <th>eISSN</th>
                        <th>${message(code:'default.startDate.label')}</th>
                        <th>${message(code:'default.endDate.label')}</th>
                        <th>${message(code: 'tipp.startVolume')}</th>
                        <th>${message(code:'tipp.endVolume')}</th>
                        <th>${message(code: 'tipp.startIssue')}</th>
                        <th>${message(code:'tipp.endIssue')}</th>



                        <th>${message(code:'subscription.details.core_medium')}</th>
                    </tr>
                    </thead>
                    <tbody>

                    <g:each in="${entitlements}" var="e">
                        <tr>
                            <td>${++index}</td>
                            <td><input type="hidden" name="entitlements.${++counter}.tipp_id" value="${e.base_entitlement.id}"/>
                                <input type="hidden" name="entitlements.${counter}.core_status" value="${e.core_status}"/>
                                <input type="hidden" name="entitlements.${counter}.start_date" value="${e.start_date}"/>
                                <input type="hidden" name="entitlements.${counter}.end_date" value="${e.end_date}"/>
                                <input type="hidden" name="entitlements.${counter}.coverage" value="${e.coverage}"/>
                                <input type="hidden" name="entitlements.${counter}.coverage_note" value="${e.coverage_note}"/>
                                ${e.base_entitlement.title.title}</td>
                            <td><g:link controller="package" action="show" id="${e.base_entitlement.pkg.id}">${e.base_entitlement.pkg.name}(${e.base_entitlement.pkg.id})</g:link></td>
                            <td>${e.base_entitlement.title.getIdentifierValue('ISSN')}</td>
                            <td>${e.base_entitlement.title.getIdentifierValue('eISSN')}</td>
                            <td>${e.start_date} (Default:<g:formatDate formatName="default.date.format.notime" date="${e.base_entitlement.startDate}"/>)</td>
                            <td>${e.end_date} (Default:<g:formatDate formatName="default.date.format.notime" date="${e.base_entitlement.endDate}"/>)</td>
                            <td>${e.base_entitlement.startVolume}</td>
                            <td>${e.base_entitlement.endVolume}</td>
                            <td>${e.base_entitlement.startIssue}</td>
                            <td>${e.base_entitlement.endIssue}</td>
                            <td>${e.core_status?:'N'}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </g:if>

          <div class="la-float-right">
              <g:if test="${entitlements}">
                  <button type="submit" class="ui button">${message(code:'myinst.renewalUpload.accept')}</button>
              </g:if>
          </div>
        </div>
        <input type="hidden" name="ecount" value="${counter}"/>
      </g:form>

<laser:htmlEnd />
