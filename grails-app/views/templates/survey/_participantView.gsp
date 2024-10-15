<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyPackageResult; de.laser.survey.SurveyVendorResult; de.laser.survey.SurveyConfigVendor; de.laser.survey.SurveyConfigPackage; de.laser.storage.RDConstants; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.properties.PropertyDefinition;" %>


<div class="ui stackable grid">
    <div class="sixteen wide column">

        <g:if test="${controllerName == 'survey'}">
            <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, participant: participant.id]}"/>
            <g:set var="participant" value="${participant}"/>
        </g:if>
        <g:else>
            <g:set var="parame" value="${[surveyConfigID: surveyConfig.id]}"/>
            <g:set var="participant" value="${institution}"/>
        </g:else>


        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <g:link class="item ${params.viewTab == 'overview' ? 'active' : ''}"
                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                    params="${parame+[viewTab: 'overview']}">

                ${message(code: 'default.overview.label')}
            </g:link>

            <g:if test="${surveyConfig.subscription}">
                        <g:link class="item ${params.viewTab == 'additionalInformation' ? 'active' : ''}"
                                controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                params="${parame+[viewTab: 'additionalInformation']}">

                            ${message(code: 'surveyOrg.additionalInformation')}
                        </g:link>
            </g:if>

            <g:if test="${surveyConfig.invoicingInformation}">
                <g:link class="item ${params.viewTab == 'invoicingInformation' ? 'active' : ''}"
                        controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                        params="${parame+[viewTab: 'invoicingInformation']}">
                    ${message(code: 'surveyOrg.invoicingInformation.short')}
                    <ui:bubble float="true" count="${SurveyOrg.countByOrgAndSurveyConfigAndPersonIsNotNull(participant, surveyConfig)}/${SurveyOrg.countByOrgAndSurveyConfigAndAddressIsNotNull(participant, surveyConfig)}"/>
                </g:link>
            </g:if>

            <g:if test="${surveyConfig.subscription && subscription}">
                <g:if test="${subscriptionService.areStatsAvailable(subscription)}">
                    <laser:javascript src="echarts.js"/>

                    <g:link class="item ${params.viewTab == 'stats' ? 'active' : ''}"
                            controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                            params="${parame+[viewTab: 'stats']}">
                        ${message(code: 'default.stats.label')}
                    </g:link>
                </g:if>
                <g:else>
                    <div class="item disabled"><div class="la-popup-tooltip" data-content="${message(code: 'default.stats.noStatsForSubscription')}"><g:message code="default.stats.label"/></div></div>
                </g:else>
            </g:if>

            <g:if test="${surveyConfig.packageSurvey}">
                    <g:link class="item ${params.viewTab == 'packageSurvey' ? 'active' : ''}"
                            controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                            params="${parame+[viewTab: 'packageSurvey']}">

                        ${message(code: 'surveyconfig.packageSurvey.label')}

                        <ui:bubble float="true" count="${SurveyPackageResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}/${SurveyConfigPackage.countBySurveyConfig(surveyConfig)}"/>
                    </g:link>
            </g:if>

        <g:if test="${surveyConfig.vendorSurvey}">
            <g:link class="item ${params.viewTab == 'vendorSurvey' ? 'active' : ''}"
                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                    params="${parame+[viewTab: 'vendorSurvey']}">

                ${message(code: 'surveyconfig.vendorSurvey.label')}

                <ui:bubble float="true" count="${SurveyVendorResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}/${SurveyConfigVendor.countBySurveyConfig(surveyConfig)}"/>
            </g:link>
        </g:if>

        </div>


        <div class="ui bottom attached tab segment active">

            <g:if test="${params.viewTab == 'invoicingInformation' && surveyConfig.invoicingInformation}">

                <h3>
                    ${message(code: 'surveyOrg.invoicingInformation')}
                </h3>

                <div class="ui top attached stackable tabular la-tab-with-js menu">
                    <a class="active item" data-tab="contacts">
                        ${message(code: 'surveyOrg.person.label')}
                        <ui:bubble float="true" count="${SurveyOrg.countByOrgAndSurveyConfigAndPersonIsNotNull(participant, surveyConfig)}"/>
                    </a>

                    <a class="item" data-tab="addresses">
                        ${message(code: 'surveyOrg.address.label')}
                        <ui:bubble float="true" count="${SurveyOrg.countByOrgAndSurveyConfigAndAddressIsNotNull(participant, surveyConfig)}"/>
                    </a>

                    <a class="item" data-tab="xRechnung">
                        ${message(code: 'surveyOrg.eInvoice.label')}
                    </a>
                </div>


                <div class="ui bottom attached tab segment active" data-tab="contacts">

                    <g:link controller="organisation" action="contacts" id="${participant.id}" class="${Btn.SIMPLE} right floated">
                        <g:message code="default.show.label" args="[message(code: 'org.publicContacts.label')]"/>
                    </g:link>
                    <br>
                    <br>

                    <laser:render template="/addressbook/person_table" model="${[
                            persons       : visiblePersons,
                            participant   : participant,
                            showContacts  : true,
                            showAddresses : true,
                            showOptions   : false,
                            tmplConfigShow: ['lineNumber', 'function', 'position', 'name', 'showContacts', 'showAddresses', 'surveyInvoicingInformation']
                    ]}"/>

                </div>

                <div class="ui bottom attached tab segment" data-tab="addresses">

                    <g:link controller="organisation" action="contacts" id="${participant.id}" class="${Btn.SIMPLE} right floated">
                        <g:message code="default.show.label" args="[message(code: 'org.publicContacts.label')]"/>
                    </g:link>
                    <br>
                    <br>


                    <laser:render template="/addressbook/address_table" model="${[
                            addresses                     : addresses,
                            editable                      : editable,
                            showSurveyInvoicingInformation: true,
                            showOptions                   : false,
                            participant   : participant
                    ]}"/>

                </div>

                <div class="ui bottom attached tab segment" data-tab="xRechnung">

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
                                    params="${parame + [viewTab: 'invoicingInformation', setEInvoiceValuesFromOrg: true]}" class="ui right floated blue button">
                                <g:message code="surveyOrg.setEInvoiceValuesFromOrg"/>
                            </g:link>
                            <br>
                            <br>
                        </g:if>
                    </div>
                </div>

            </g:if>


            <g:if test="${params.viewTab == 'additionalInformation' && surveyConfig.subscription}">
                <div class="la-inline-lists">
                    <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig    : surveyConfig,
                                                                                          subscription    : subscription,
                                                                                          visibleProviders: providerRoles,
                                                                                          surveyResults   : surveyResults]"/>
                </div>
            </g:if>


            <g:if test="${params.viewTab == 'overview'}">
                    <laser:render template="/templates/survey/generalInfos"/>
            </g:if>
            <g:if test="${params.viewTab == 'stats' && surveyConfig.subscription && subscription}">
                <g:render template="/templates/stats/stats"/>
            </g:if>

            <g:if test="${params.viewTab == 'packageSurvey' && surveyConfig.packageSurvey}">

                <div class="la-inline-lists">
                <g:render template="/templates/survey/costsWithSurveyPackages"/>
                </div>


                <div class="ui top attached stackable tabular menu">
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

                <div class="ui bottom attached tab segment active">

                    <h2 class="ui left floated aligned icon header la-clear-before">${message(code: params.subTab == 'selectPackages' ? 'surveyPackages.selectedPackages' : 'surveyPackages.all')}
                    <ui:totalNumber total="${recordsCount}"/>
                    </h2>

                    <g:if test="${params.subTab == 'selectPackages'}">
                        <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'status', 'titleCount', 'provider', 'automaticUpdates', 'lastUpdatedDisplay', 'surveyCostItemsPackages', 'surveyPackagesComments', 'removeSurveyPackageResult']}"/>
                    </g:if>
                    <g:else>
                        <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'status', 'titleCount', 'provider', 'automaticUpdates', 'lastUpdatedDisplay', 'surveyCostItemsPackages', 'addSurveyPackageResult']}"/>
                    </g:else>

                    <g:render template="/templates/survey/packages" model="[
                            processController: controllerName,
                            processAction    : actionName,
                            tmplShowCheckbox : false,
                            tmplConfigShow   : tmplConfigShowList]"/>
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
                        ${message(code: 'surveyVendors.selectedVendors')}
                        <ui:bubble float="true" count="${SurveyVendorResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}"/>
                    </g:link>
                </div>

                <div class="ui bottom attached tab segment active">

                    <h2 class="ui left floated aligned icon header la-clear-before">${message(code: params.subTab == 'selectVendors' ? 'surveyVendors.selectedVendors' : 'surveyVendors.all')}
                    <ui:totalNumber total="${vendorListTotal}"/>
                    </h2>

                    <g:if test="${params.subTab == 'selectVendors'}">
                        <g:set var="tmplConfigShowList" value="${['lineNumber', 'sortname', 'name', 'isWekbCurated', 'surveyVendorsComments', 'removeSurveyVendorResult']}"/>
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