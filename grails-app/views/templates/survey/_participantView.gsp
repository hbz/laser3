<%@ page import="de.laser.survey.SurveyPersonResult; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyPackageResult; de.laser.survey.SurveyVendorResult; de.laser.survey.SurveyConfigVendor; de.laser.survey.SurveyConfigPackage; de.laser.storage.RDConstants; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.properties.PropertyDefinition;" %>


<div class="ui stackable grid">
    <div class="sixteen wide column">

        <g:if test="${controllerName == 'survey'}">
            <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, participant: participant.id]}"/>
            <g:set var="participant" value="${participant}"/>
        </g:if>
        <g:else>
            <g:set var="parame" value="${[surveyConfigID: surveyConfig.id]}"/>
            <g:set var="participant" value="${contextService.getOrg()}"/>
        </g:else>


        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <g:link class="item ${params.viewTab == 'overview' ? 'active' : ''}"
                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                    params="${parame + [viewTab: 'overview']}">

                ${message(code: 'default.overview.label')}
            </g:link>

            <g:link class="item ${params.viewTab == 'surveyContacts' ? 'active' : ''}"
                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                    params="${parame + [viewTab: 'surveyContacts']}">
                ${message(code: 'surveyOrg.surveyContacts')}
                <ui:bubble float="true"
                           count="${SurveyPersonResult.countByParticipantAndSurveyConfigAndPersonIsNotNullAndSurveyPerson(participant, surveyConfig, true)}"/>
            </g:link>

            <g:if test="${surveyConfig.invoicingInformation}">
                <g:link class="item ${params.viewTab == 'invoicingInformation' ? 'active' : ''}"
                        controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                        params="${parame + [viewTab: 'invoicingInformation']}">
                    ${message(code: 'surveyOrg.invoicingInformation.short')}
                    <ui:bubble float="true"
                               count="${SurveyPersonResult.countByParticipantAndSurveyConfigAndPersonIsNotNullAndBillingPerson(participant, surveyConfig, true)}/${SurveyOrg.countByOrgAndSurveyConfigAndAddressIsNotNull(participant, surveyConfig)}"/>
                </g:link>
            </g:if>

            <g:if test="${surveyConfig.packageSurvey}">
                <g:link class="item ${params.viewTab == 'packageSurvey' ? 'active' : ''}"
                        controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                        params="${parame + [viewTab: 'packageSurvey']}">

                    ${message(code: 'surveyconfig.packageSurvey.label')}

                    <ui:bubble float="true"
                               count="${SurveyPackageResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}/${SurveyConfigPackage.countBySurveyConfig(surveyConfig)}"/>
                </g:link>
            </g:if>

            <g:if test="${surveyConfig.vendorSurvey}">
                <g:link class="item ${params.viewTab == 'vendorSurvey' ? 'active' : ''}"
                        controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                        params="${parame + [viewTab: 'vendorSurvey']}">

                    ${message(code: 'surveyconfig.vendorSurvey.label')}

                    <ui:bubble float="true"
                               count="${SurveyVendorResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}/${SurveyConfigVendor.countBySurveyConfig(surveyConfig)}"/>
                </g:link>
            </g:if>

            <g:if test="${surveyConfig.subscription}">
                <g:link class="item ${params.viewTab == 'additionalInformation' ? 'active' : ''}"
                        controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                        params="${parame + [viewTab: 'additionalInformation']}">

                    ${message(code: 'surveyOrg.additionalInformation')}
                </g:link>
            </g:if>

            <g:if test="${surveyConfig.subscription && subscription}">
                <g:if test="${subscriptionService.areStatsAvailable(subscription)}">
                    <laser:javascript src="echarts.js"/>

                    <g:link class="item ${params.viewTab == 'stats' ? 'active' : ''}"
                            controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                            params="${parame + [viewTab: 'stats']}">
                        ${message(code: 'default.stats.label')}
                    </g:link>
                </g:if>
                <g:else>
                    <div class="item disabled"><div class="la-popup-tooltip" data-content="${message(code: 'default.stats.noStatsForSubscription')}"><g:message
                            code="default.stats.label"/></div></div>
                </g:else>
            </g:if>

        </div>

        <div class="ui bottom attached tab segment active">
            <div class="ui stackable grid">
                <g:if test="${params.viewTab == 'surveyContacts'}">
                    <div class="sixteen wide column">
                        <h2 class="ui left floated aligned header">${message(code: 'surveyOrg.surveyContacts')}</h2>
                        <g:link controller="organisation" action="contacts" id="${participant.id}" target="_blank" class="${Btn.SIMPLE} right floated">
                            <g:message code="survey.contacts.surveyContact.add"/>
                        </g:link>
                        <br>
                        <br>

                        <laser:render template="/addressbook/person_table" model="${[
                                persons       : visiblePersons,
                                participant   : participant,
                                showContacts  : true,
                                showOptions   : false,
                                tmplConfigShow: ['lineNumber', 'function', 'position', 'name', 'showContacts', 'surveyInvoicingInformation']
                        ]}"/>
                    </div>
                </g:if>

                <g:if test="${params.viewTab == 'invoicingInformation' && surveyConfig.invoicingInformation}">
                    <div class="two wide column">
                        <div class="ui fluid vertical tabular la-tab-with-js menu">
                            <a class="${params.subTab ? (params.subTab == 'contacts' ? 'active' : '') : 'active'} item" data-tab="contacts">
                                ${message(code: 'surveyOrg.person.label')}
                                <ui:bubble float="true"
                                           count="${SurveyPersonResult.countByParticipantAndSurveyConfigAndPersonIsNotNullAndBillingPerson(participant, surveyConfig, true)}"/>
                            </a>

                            <a class="${params.subTab == 'addresses' ? 'active' : ''} item" data-tab="addresses">
                                ${message(code: 'surveyOrg.address.label')}
                                <ui:bubble float="true" count="${SurveyOrg.countByOrgAndSurveyConfigAndAddressIsNotNull(participant, surveyConfig)}"/>
                            </a>

                            <a class="${params.subTab == 'xRechnung' ? 'active' : ''} item" data-tab="xRechnung">
                                ${message(code: 'surveyOrg.eInvoice.label')}
                            </a>
                        </div>
                    </div>

                    <div class="fourteen wide column">
                        <div class="ui bottom attached tab   ${params.subTab ? (params.subTab == 'contacts' ? 'active' : '') : 'active'}" data-tab="contacts">
                           %{-- <h2 class="ui left floated aligned header">${message(code: 'surveyOrg.person.label.heading')}</h2>--}%
                            <g:link controller="organisation" action="contacts" id="${participant.id}" params="[tab: 'contacts']" target="_blank"
                                    class="${Btn.SIMPLE} right floated">
                                <g:message code="survey.contacts.add"/>
                            </g:link>
                            <br>
                            <br>

                            <laser:render template="/addressbook/person_table" model="${[
                                    persons       : visiblePersons,
                                    participant   : participant,
                                    showContacts  : true,
                                    showOptions   : false,
                                    tmplConfigShow: ['lineNumber', 'function', 'position', 'name', 'showContacts', 'surveyInvoicingInformation']
                            ]}"/>

                        </div>

                        <div class="ui bottom attached tab   ${params.subTab == 'addresses' ? 'active' : ''}" data-tab="addresses">
                            %{--<h2 class="ui left floated aligned header">${message(code: 'surveyOrg.address.label.heading')}</h2>--}%
                            <g:link controller="organisation" action="contacts" id="${participant.id}" params="[tab: 'addresses']" target="_blank"
                                    class="${Btn.SIMPLE} right floated">
                                <g:message code="survey.contacts.add"/>
                            </g:link>
                            <br>
                            <br>


                            <laser:render template="/addressbook/address_table" model="${[
                                    addresses                     : addresses,
                                    editable                      : editable,
                                    showSurveyInvoicingInformation: true,
                                    showOptions                   : false,
                                    participant                   : participant
                            ]}"/>

                        </div>

                        <div class="ui bottom attached tab   ${params.subTab == 'xRechnung' ? 'active' : ''}" data-tab="xRechnung">

                            <ui:msg message="surveyOrg.eInvoice.expl" class="info" showIcon="true" hideClose="true"/>

                            <div class="la-inline-lists">
                                <div class="ui card">
                                    <div class="content">
                                        <dl>
                                            <dt>
                                                <g:message code="surveyOrg.eInvoicePortal.label"/>
                                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                                      data-content="${message(code: 'surveyOrg.eInvoicePortal.expl')}">
                                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                                </span>
                                            </dt>
                                            <dd>
                                                <ui:xEditableRefData owner="${surveyOrg}" field="eInvoicePortal" config="${RDConstants.E_INVOICE_PORTAL}"/>
                                            </dd>
                                        </dl>
                                    </div>

                                    <div class="content">
                                        <dl>
                                            <dt>
                                                <g:message code="surveyOrg.eInvoiceLeitwegId.label"/>
                                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                                      data-content="${message(code: 'surveyOrg.eInvoiceLeitwegId.expl')}">
                                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                                </span>
                                            </dt>
                                            <dd>
                                                <ui:xEditable owner="${surveyOrg}" field="eInvoiceLeitwegId"/>
                                            </dd>
                                        </dl>
                                    </div>

                                    <div class="content">
                                        <dl>
                                            <dt>
                                                <g:message code="surveyOrg.eInvoiceLeitkriterium.label"/>
                                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                                      data-content="${message(code: 'surveyOrg.eInvoiceLeitkriterium.expl')}">
                                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                                </span>
                                            </dt>
                                            <dd>
                                                <ui:xEditable owner="${surveyOrg}" field="eInvoiceLeitkriterium"/>
                                            </dd>
                                        </dl>
                                    </div>
                                </div><!-- .card -->

                                <g:if test="${editable}">
                                    <g:link controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                            params="${parame + [viewTab: 'invoicingInformation', subTab: 'xRechnung', setEInvoiceValuesFromOrg: true,]}"
                                            class="${Btn.SIMPLE} right floated">
                                        <g:message code="surveyOrg.setEInvoiceValuesFromOrg"/>
                                    </g:link>
                                    <br>
                                    <br>
                                </g:if>
                            </div>
                        </div>
                    </div>

                </g:if>


                <g:if test="${params.viewTab == 'additionalInformation' && surveyConfig.subscription}">
                    <div class="sixteen wide column">
                        <div class="la-inline-lists">
                            <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig    : surveyConfig,
                                                                                                  subscription    : subscription,
                                                                                                  visibleProviders: providerRoles,
                                                                                                  surveyResults   : surveyResults]"/>
                        </div>
                    </div>
                </g:if>


                <g:if test="${params.viewTab == 'overview'}">
                    <laser:render template="/templates/survey/generalInfos"/>
                </g:if>
                <g:if test="${params.viewTab == 'stats' && surveyConfig.subscription && subscription}">
                    <g:render template="/templates/stats/stats"/>
                </g:if>

                <g:if test="${params.viewTab == 'packageSurvey' && surveyConfig.packageSurvey}">
                    <div class="sixteen wide column">
                        <div class="la-inline-lists">
                            <g:render template="/templates/survey/costsWithSurveyPackages"/>
                        </div>
                    </div>

                    <div class="two wide column">
                        <div class="ui fluid vertical tabular menu">
                            <g:link class="item ${params.viewTab == 'packageSurvey' && params.subTab == 'allPackages' ? 'active' : ''}"
                                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                    params="${parame + [viewTab: 'packageSurvey', subTab: 'allPackages']}">
                                ${message(code: 'surveyPackages.all')}
                                <ui:bubble float="true" count="${SurveyConfigPackage.countBySurveyConfig(surveyConfig)}"/>
                            </g:link>
                            <g:link class="item ${params.viewTab == 'packageSurvey' && params.subTab == 'selectPackages' ? 'active' : ''}"
                                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                    params="${parame + [viewTab: 'packageSurvey', subTab: 'selectPackages']}">
                                ${message(code: 'surveyPackages.selectedPackages')}
                                <ui:bubble float="true" count="${SurveyPackageResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}"/>
                            </g:link>
                        </div>
                    </div>

                    <div class="fourteen wide column">
                        <div class="ui tab active">
                            <h2 class="ui left floated aligned icon header la-clear-before">${message(code: params.subTab == 'selectPackages' ? 'surveyPackages.selectedPackages' : 'surveyPackages.all')}
                            <ui:totalNumber total="${recordsCount}"/>
                            </h2>

                            <g:if test="${params.subTab == 'selectPackages'}">
                                <g:set var="tmplConfigShowList"
                                       value="${['lineNumber', 'name', 'status', 'titleCount', 'provider', 'automaticUpdates', 'lastUpdatedDisplay', 'surveyCostItemsPackages', 'surveyPackagesComments', 'removeSurveyPackageResult']}"/>
                            </g:if>
                            <g:else>
                                <g:set var="tmplConfigShowList"
                                       value="${['lineNumber', 'name', 'status', 'titleCount', 'provider', 'automaticUpdates', 'lastUpdatedDisplay', 'surveyCostItemsPackages', 'addSurveyPackageResult']}"/>
                            </g:else>

                            <g:render template="/templates/survey/packages" model="[
                                    processController: controllerName,
                                    processAction    : actionName,
                                    tmplShowCheckbox : false,
                                    tmplConfigShow   : tmplConfigShowList]"/>
                        </div>
                    </div>

                </g:if>

                <g:if test="${params.viewTab == 'vendorSurvey' && surveyConfig.vendorSurvey}">

                    <div class="ui top attached stackable tabular menu">
                        <g:link class="item ${params.viewTab == 'vendorSurvey' && params.subTab == 'allVendors' ? 'active' : ''}"
                                controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                params="${parame + [viewTab: 'vendorSurvey', subTab: 'allVendors']}">
                            ${message(code: 'surveyVendors.all')}
                            <ui:bubble float="true" count="${SurveyConfigVendor.countBySurveyConfig(surveyConfig)}"/>
                        </g:link>
                        <g:link class="item ${params.viewTab == 'vendorSurvey' && params.subTab == 'selectVendors' ? 'active' : ''}"
                                controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                params="${parame + [viewTab: 'vendorSurvey', subTab: 'selectVendors']}">
                            ${message(code: 'surveyVendors.selectedVendor')}
                            <ui:bubble float="true" count="${SurveyVendorResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}"/>
                        </g:link>
                    </div>

                    <div class="ui bottom attached tab segment active">

                        <h2 class="ui left floated aligned icon header la-clear-before">${message(code: params.subTab == 'selectVendors' ? 'surveyVendors.selectedVendors' : 'surveyVendors.all')}
                        <ui:totalNumber total="${vendorListTotal}"/>
                        </h2>

                        <g:if test="${params.subTab == 'selectVendors'}">
                            <g:set var="tmplConfigShowList"
                                   value="${['lineNumber', 'sortname', 'name', 'isWekbCurated', 'surveyVendorsComments', 'removeSurveyVendorResult']}"/>
                        </g:if>
                        <g:else>
                            <g:set var="tmplConfigShowList" value="${['lineNumber', 'sortname', 'name', 'isWekbCurated', 'addSurveyVendorResult']}"/>
                        </g:else>

                        <g:render template="/templates/survey/vendors" model="[
                                processController: controllerName,
                                processAction    : actionName,
                                tmplShowCheckbox : false,
                                tmplConfigShow   : tmplConfigShowList]"/>
                    </div>

                </g:if>
            </div>
        </div>
    </div>
</div>