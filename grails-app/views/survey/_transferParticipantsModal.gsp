<%@ page import="de.laser.RefdataValue; de.laser.License" %>
<g:set var="auditConfigProvidersAgencies" value="${parentSuccessorSubscription.orgRelations?.findAll {it.isShared}}" />

<ui:modal id="transferParticipantsModal" message="surveyInfo.transferParticipants"
             msgSave="${message(code: 'surveyInfo.transferParticipants.button')}">

    <h3 class="ui header"><g:message code="surveyInfo.transferParticipants.option"/>:</h3>

    <g:form class="ui form"
            url="[controller: 'survey', action: 'processTransferParticipantsByRenewal', params: [id: params.id, surveyConfigID: surveyConfig.id]]">
        <div class="field">
            <g:set var="properties" value="${de.laser.AuditConfig.getConfigs(parentSuccessorSubscription)}"></g:set>
            <g:if test="${properties}">

                <label><g:message code="copyElementsIntoObject.auditConfig" />:</label>
                <div class="ui bulleted list">
                    <g:each in="${properties}" var="prop" >
                        <div class="item">
                            <strong><g:message code="subscription.${prop.referenceField}.label" /></strong>:
                        <g:if test="${parentSuccessorSubscription.getProperty(prop.referenceField) instanceof RefdataValue}">
                            ${parentSuccessorSubscription.getProperty(prop.referenceField).getI10n('value')}
                        </g:if>
                        <g:elseif test="${parentSuccessorSubscription.getProperty(prop.referenceField) instanceof java.lang.Boolean}">
                            ${parentSuccessorSubscription.getProperty(prop.referenceField) ? de.laser.storage.RDStore.YN_YES.getI10n('value') : de.laser.storage.RDStore.YN_NO.getI10n('value')}
                        </g:elseif>
                        <g:else>
                            ${parentSuccessorSubscription.getProperty(prop.referenceField)}
                        </g:else>
                        </div>
                    </g:each>
                </div>
            </g:if>
            <g:else>
                <g:message code="copyElementsIntoObject.noAuditConfig"/>
            </g:else>

            <g:if test="${auditConfigProvidersAgencies}">
                <label><g:message code="property.share.tooltip.on" />:</label>
                <div class="ui bulleted list">
                    <g:each in="${auditConfigProvidersAgencies}" var="role" >
                        <div class="item">
                            <strong> ${role.roleType.getI10n("value")}</strong>:
                        ${role.org.name}
                        </div>
                    </g:each>
                </div>

            </g:if>

        </div>
        <div class="two fields">
            <g:set var="validPackages" value="${parentSuccessorSubscription.packages?.sort { it.pkg.name }}"/>
            <div class="field">

                <label><g:message code="myinst.addMembers.linkPackages"/></label>
                <g:if test="${validPackages}">
                    <div class="ui checkbox">
                        <input type="checkbox" id="linkAllPackages" name="linkAllPackages">
                        <label for="linkAllPackages"><g:message code="myinst.addMembers.linkAllPackages"
                                                                args="${superOrgType}"/>
                        (<g:each in="${validPackages}" var="pkg">
                            ${pkg.getPackageName()},
                        </g:each>)
                        </label>
                    </div>

                    <div class="ui checkbox">
                        <input type="checkbox" id="linkWithEntitlements" name="linkWithEntitlements">
                        <label for="linkWithEntitlements"><g:message
                                code="myinst.addMembers.withEntitlements"/></label>
                    </div>

                    <div class="field">
                        <g:select class="ui search multiple dropdown"
                                  optionKey="id" optionValue="${{ it.getPackageName() }}"
                                  from="${validPackages}" name="packageSelection" value=""
                                  noSelection='["": "${message(code: 'subscriptionsManagement.noSelection.package')}"]'/>
                    </div>
                </g:if>
                <g:else>
                    <g:message code="subscriptionsManagement.noValidPackages" args="${superOrgType}"/>
                </g:else>
            </div>

            <g:if test="${!auditConfigProvidersAgencies}">
                <div class="field">
                    <g:set var="providers" value="${parentSuccessorSubscription.getProviders()?.sort { it.name }}"/>
                    <g:set var="agencies" value="${parentSuccessorSubscription.getAgencies()?.sort { it.name }}"/>

                    <g:if test="${(providers || agencies)}">
                        <label><g:message code="surveyInfo.transferParticipants.moreOption"/></label>

                        <div class="ui checkbox">
                            <input type="checkbox" id="transferProviderAgency" name="transferProviderAgency" checked>
                            <label for="transferProviderAgency"><g:message
                                    code="surveyInfo.transferParticipants.transferProviderAgency"
                                    args="${superOrgType}"/>
                            <g:set var="providerAgency" value="${providers + agencies}"/>
                            (${providerAgency ? providerAgency.name.join(', ') : ''})
                            </label>
                        </div>

                        <div class="field">

                            <g:if test="${providers}">
                                <label><g:message code="surveyInfo.transferParticipants.transferProvider"
                                                  args="${superOrgType}"/>:</label>
                                <g:select class="ui search multiple dropdown"
                                          optionKey="id" optionValue="name"
                                          from="${providers}" name="providersSelection" value=""
                                          noSelection='["": "${message(code: 'surveyInfo.transferParticipants.noSelectionTransferProvider')}"]'/>
                            </g:if>
                        </div>

                        <div class="field">
                            <g:set var="agencies"
                                   value="${parentSuccessorSubscription.getAgencies()?.sort { it.name }}"/>
                            <g:if test="${agencies}">
                                <label><g:message code="surveyInfo.transferParticipants.transferAgency"
                                                  args="${superOrgType}"/>:</label>
                                <g:select class="ui search multiple dropdown"
                                          optionKey="id" optionValue="name"
                                          from="${agencies}" name="agenciesSelection" value=""
                                          noSelection='["": "${message(code: 'surveyInfo.transferParticipants.noSelectionTransferAgency')}"]'/>
                            </g:if>
                        </div>
                    </g:if>
                    <g:else>
                        <g:message code="surveyInfo.transferParticipants.noTransferProviderAgency"
                                   args="${superOrgType}"/>
                    </g:else>
                </div>
            </g:if>
        </div>

        <div class="ui two fields">
            <div class="field">
                <label><g:message code="surveyInfo.transferParticipants.copyMemberLicenses"/></label>
                <g:if test="${memberLicenses}">
                    <div class="ui radio checkbox">
                        <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics" value="no">
                        <label for="generateSlavedLics">${message(code: 'myinst.separate_lics_no')}</label>
                    </div>
                    <br />
                    <div class="ui radio checkbox">
                        <input class="hidden" type="radio" id="generateSlavedLics1" name="generateSlavedLics" value="all" checked="checked">
                        <label for="generateSlavedLics1">${message(code: 'myinst.separate_lics_all')}</label>
                    </div>
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code:'myinst.separate_lics_all.expl')}">
                        <i class="question circle icon la-popup"></i>
                    </span>
                    <br />
                    <div class="ui radio checkbox">
                        <input class="hidden" type="radio" id="generateSlavedLics2" name="generateSlavedLics" value="partial">
                        <label for="generateSlavedLics2">${message(code: 'myinst.separate_lics_partial')}</label>
                    </div>
                    <div class="generateSlavedLicsReference-wrapper hidden">
                        <br />
                        <g:select from="${memberLicenses}"
                                  class="ui fluid search multiple dropdown hide"
                                  optionKey="${{ License.class.name + ':' + it.id }}"
                                  optionValue="${{ it.reference }}"
                                  noSelection="${['' : message(code:'default.select.all.label')]}"
                                  name="generateSlavedLicsReference"/>
                    </div>
                    <laser:script file="${this.getGroovyPageFileName()}">
                        $('*[name=generateSlavedLics]').change(function () {
                            $('*[name=generateSlavedLics][value=partial]').prop('checked') ? $('.generateSlavedLicsReference-wrapper').removeClass('hidden') : $('.generateSlavedLicsReference-wrapper').addClass('hidden');
                        })
                        $('*[name=generateSlavedLics]').trigger('change')
                    </laser:script>
                </g:if>
                <g:else>
                    <ui:msg class="info" message="surveyInfo.transferParticipants.noMemberLicenses" />
                </g:else>
            </div>
        </div>

    </g:form>

</ui:modal>