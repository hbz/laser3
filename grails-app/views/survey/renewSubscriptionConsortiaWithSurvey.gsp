<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataCategory; de.laser.AuditConfig;de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyInfo.renewal" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyInfo.renewal" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
</ui:controlButtons>

<g:if test="${(errors && (errors.size() > 0))}">
    <div>
        <ul>
            <g:each in="${errors}" var="e">
                <li>${e}</li>
            </g:each>
        </ul>
    </div>
</g:if>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:status object="${surveyInfo}"/>

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<ui:messages data="${flash}"/>

<g:set var="counter" value="${-1}"/>
<g:set var="index" value="${0}"/>


<g:form action="processRenewalWithSurvey" method="post" enctype="multipart/form-data" params="${params}">
    <input type="hidden" name="sourceSubId" value="${sourceSubscription.id}"/>

        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
            <tr>
                <th>${message(code: 'myinst.renewalUpload.props')}</th>
                <th>${message(code: 'default.value.label')}</th>
                <th>${message(code: 'copyElementsIntoObject.audit')}</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <th>${message(code: 'myinst.emptySubscription.name')}</th>
                <td>
                    <div class="ui form field">
                        <input type="text" name="subscription.name" value="${permissionInfo.sub_name}">
                    </div>
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                    <input type="checkbox" name="auditList" value="name" ${AuditConfig.getConfig(sourceSubscription, 'name') ? 'checked': ''} />
                    </div>
                </td>

            </tr>
            <tr>
                <th>${message(code: 'default.startDate.label.shy')}</th>
                <td><ui:datepicker class="wide eight" id="subscription.start_date" name="subscription.start_date" placeholder="default.date.label" value="${permissionInfo.sub_startDate}" required="" /></td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="startDate" ${AuditConfig.getConfig(sourceSubscription, 'startDate') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'default.endDate.label.shy')}</th>
                <td><ui:datepicker class="wide eight" id="subscription.end_date" name="subscription.end_date" placeholder="default.date.label" value="${permissionInfo.sub_endDate}" /></td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="endDate" ${AuditConfig.getConfig(sourceSubscription, 'endDate') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.referenceYear.label.shy')}</th>
                <td><ui:datepicker class="wide eight" id="subscription.reference_year" name="subscription.reference_year" placeholder="default.date.format.yyyy" value="${permissionInfo.sub_referenceYear}" type="year"/></td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="referenceYear" ${AuditConfig.getConfig(sourceSubscription, 'referenceYear') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'default.status.label')}</th>
                <td>
                <g:set var="rdcSubStatus" value="${RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_STATUS)}"/>
                <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" class="ui dropdown clearable"
                          optionKey="id"
                          optionValue="${{ it.getI10n('value') }}"
                          name="subStatus"
                          value="${permissionInfo.sub_status}"
                          />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="status" ${AuditConfig.getConfig(sourceSubscription, 'status') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.kind.label')}</th>
                <td>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}" class="ui dropdown clearable"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subKind"
                              value="${permissionInfo.sub_kind}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="kind" ${AuditConfig.getConfig(sourceSubscription, 'kind') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.form.label.shy')}</th>
                <td>
                    <g:set var="rdcSubForm" value="${RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_FORM)}"/>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}" class="ui dropdown clearable"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subForm"
                              value="${permissionInfo.sub_form}"
                              />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="form" ${AuditConfig.getConfig(sourceSubscription, 'form') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.resource.label')}</th>
                <td>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}" class="ui dropdown clearable"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subResource"
                              value="${permissionInfo.sub_resource}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="resource" ${AuditConfig.getConfig(sourceSubscription, 'resource') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                <td>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}" class="ui dropdown clearable"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subIsPublicForApi"
                              value="${permissionInfo.sub_isPublicForApi}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="isPublicForApi" ${AuditConfig.getConfig(sourceSubscription, 'isPublicForApi') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                <td>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}" class="ui dropdown clearable"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subHasPerpetualAccess"
                              value="${permissionInfo.sub_hasPerpetualAccess}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="hasPerpetualAccess" ${AuditConfig.getConfig(sourceSubscription, 'hasPerpetualAccess') ? 'checked': ''} />
                    </div>
                </td>
            </tr>
            <tr>
                <th>${message(code: 'subscription.hasPublishComponent.label.shy')}</th>
                <td>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}" class="ui dropdown clearable"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subHasPublishComponent"
                              value="${permissionInfo.sub_hasPublishComponent}"
                    />
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList" value="hasPublishComponent" ${AuditConfig.getConfig(sourceSubscription, 'hasPublishComponent') ? 'checked': ''} />
                    </div>
                </td>
            </tr>

            <tr>
                <th>${message(code: 'subscription.holdingSelection.label.shy')}</th>
                <td>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}" class="ui dropdown clearable"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subHoldingSelection"
                              value="${permissionInfo.sub_holdingSelection}"/>
                </td>
                <td class="center aligned">
                    <div class="ui checkbox">
                        <input type="checkbox" name="auditList"
                               value="holdingSelection" ${AuditConfig.getConfig(sourceSubscription, 'holdingSelection') ? 'checked' : ''}/>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>

        <div class="la-float-right">
            <button type="submit" class="${Btn.SIMPLE}">${message(code: 'myinst.renewalUpload.renew')}</button>
        </div>

</g:form>

<laser:htmlEnd />
