<%@ page import="de.laser.wekb.ProviderRole; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.License; de.laser.wekb.VendorRole" %>
<g:set var="auditConfigProviders" value="${ProviderRole.findAllBySubscriptionAndIsShared(parentSuccessorSubscription, true)}" />
<g:set var="auditConfigVendors" value="${VendorRole.findAllBySubscriptionAndIsShared(parentSuccessorSubscription, true)}" />

<ui:modal id="transferParticipantsModal" message="surveyInfo.transferParticipants"
             msgSave="${message(code: 'surveyInfo.transferParticipants.button')}">

    <h1 class="ui header">
        ${parentSuccessorSubscription.dropdownNamingConvention()}
    </h1>

    <h3 class="ui header"><g:message code="surveyInfo.transferParticipants.option"/>:</h3>

    <g:form class="ui form"
            url="[controller: 'survey', action: 'processTransferParticipantsByRenewal', params: [id: params.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id]]">
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
                        <g:elseif test="${parentSuccessorSubscription.getProperty(prop.referenceField) instanceof java.util.Date}">
                            ${parentSuccessorSubscription.getProperty(prop.referenceField) ? g.formatDate(date: parentSuccessorSubscription.getProperty(prop.referenceField),format: message(code: 'default.date.format.notime')) : ''}
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

            <g:if test="${auditConfigProviders}">
                <label><g:message code="property.share.tooltip.on" />:</label>
                <div class="ui bulleted list">
                    <g:each in="${auditConfigProviders}" var="role" >
                        <div class="item">
                            <strong> <g:message code="provider.label"/></strong>:
                        ${role.provider.name}
                        </div>
                    </g:each>
                </div>
            </g:if>
            <g:if test="${auditConfigVendors}">
                <label><g:message code="property.share.tooltip.on" />:</label>
                <div class="ui bulleted list">
                    <g:each in="${auditConfigVendors}" var="role" >
                        <div class="item">
                            <strong> <g:message code="vendor.label"/></strong>:
                        ${role.vendor.name}
                        </div>
                    </g:each>
                </div>
            </g:if>

        </div>
        <div class="two fields">
            %{--<g:set var="validPackages" value="${parentSuccessorSubscription.packages?.sort { it.pkg.name }}"/>
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
            </div>--}%

            <g:if test="${!auditConfigProviders}">
                <div class="field">
                    <g:set var="providers" value="${parentSuccessorSubscription.getProviders()?.sort { it.name }}"/>

                    <g:if test="${providers}">
                        <label><g:message code="surveyInfo.transferParticipants.moreOption"/></label>

                        <div class="ui checkbox">
                            <input type="checkbox" id="transferProvider" name="transferProvider" checked>
                            <label for="transferProvider"><g:message
                                    code="surveyInfo.transferParticipants.transferAllProviders"
                                    args="${superOrgType}"/>
                            <g:set var="provider" value="${providers}"/>
                            (${provider ? provider.name.join(', ') : ''})
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

                    </g:if>
                    <g:else>
                        <label><g:message code="provider.plural"/>:</label>
                        <g:message code="surveyInfo.transferParticipants.noTransferProvider" args="${superOrgType}"/>
                    </g:else>
                </div>
            </g:if>

            <g:if test="${!auditConfigVendors}">
                <div class="field">
                    <g:set var="vendors" value="${parentSuccessorSubscription.getVendors()?.sort { it.name }}"/>

                    <g:if test="${vendors}">
                        <label><g:message code="surveyInfo.transferParticipants.moreOption"/></label>

                        <div class="ui checkbox">
                            <input type="checkbox" id="transferVendor" name="transferVendor" checked>
                            <label for="transferVendor"><g:message
                                    code="surveyInfo.transferParticipants.transferAllVendors"
                                    args="${superOrgType}"/>
                            <g:set var="vendor" value="${vendors}"/>
                            (${vendor ? vendor.name.join(', ') : ''})
                            </label>
                        </div>

                        <div class="field">
                            <g:set var="vendors"
                                   value="${parentSuccessorSubscription.getVendors()?.sort { it.name }}"/>
                            <g:if test="${vendors}">
                                <label><g:message code="surveyInfo.transferParticipants.transferVendor"
                                                  args="${superOrgType}"/>:</label>
                                <g:select class="ui search multiple dropdown"
                                          optionKey="id" optionValue="name"
                                          from="${vendors}" name="vendorsSelection" value=""
                                          noSelection='["": "${message(code: 'surveyInfo.transferParticipants.noSelectionTransferVendor')}"]'/>
                            </g:if>
                        </div>
                    </g:if>
                    <g:else>
                        <label><g:message code="vendor.plural"/>:</label>
                        <g:message code="surveyInfo.transferParticipants.noTransferVendor" args="${superOrgType}"/>
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
                    <span class="la-long-tooltip la-popup-tooltip" data-content="${message(code:'myinst.separate_lics_all.expl')}">
                        <i class="${Icon.TOOLTIP.HELP} la-popup"></i>
                    </span>
                    <br />
                    <div class="ui radio checkbox">
                        <input class="hidden" type="radio" id="generateSlavedLics2" name="generateSlavedLics" value="partial">
                        <label for="generateSlavedLics2">${message(code: 'myinst.separate_lics_partial')}</label>
                    </div>
                    <div class="generateSlavedLicsReference-wrapper hidden">
                        <br />
                        <g:select from="${memberLicenses}"
                                  class="ui fluid search multiple dropdown clearable hide"
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