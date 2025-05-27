<%@ page import="de.laser.survey.SurveyConfigSubscription; de.laser.survey.SurveySubscriptionResult; de.laser.survey.SurveyPersonResult; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyPackageResult; de.laser.survey.SurveyVendorResult; de.laser.survey.SurveyConfigVendor; de.laser.survey.SurveyConfigPackage; de.laser.storage.RDConstants; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.properties.PropertyDefinition;" %>

<ui:greySegment>
    <div class="ui form la-padding-left-07em">
        <div class="field">
            <label>
                <g:message code="surveyInfo.comment.label"/>
            </label>
            <g:if test="${surveyInfo.comment}">
                <textarea class="la-textarea-resize-vertical" readonly="readonly" rows="3">${surveyInfo.comment}</textarea>
            </g:if>
            <g:else>
                <g:message code="surveyConfigsInfo.comment.noComment"/>
            </g:else>
        </div>
    </div>
</ui:greySegment>

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
                               count="${SurveyVendorResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}"/>
                </g:link>
            </g:if>

            <g:if test="${surveyConfig.subscriptionSurvey}">
                <g:link class="item ${params.viewTab == 'subscriptionSurvey' ? 'active' : ''}"
                        controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                        params="${parame + [viewTab: 'subscriptionSurvey']}">

                    ${message(code: 'surveyconfig.subscriptionSurvey.label')}

                    <ui:bubble float="true"
                               count="${SurveySubscriptionResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}"/>
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
                                tmplConfigShow: ['lineNumber', 'function', 'position', 'name', 'showContacts', 'setPreferredSurveyPerson']
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
                                <ui:bubble float="true" count="${surveyOrg.eInvoicePortal ? '1' : '0'}/${surveyOrg.eInvoiceLeitwegId ? '1' : '0'}/${surveyOrg.eInvoiceLeitkriterium ? '1' : '0'}"/>
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
                                    tmplConfigShow: ['lineNumber', 'function', 'position', 'name', 'showContacts', 'setPreferredBillingPerson']
                            ]}"/>

                        </div>

                        <div class="ui bottom attached tab   ${params.subTab == 'addresses' ? 'active' : ''}" data-tab="addresses">
                            %{--<h2 class="ui left floated aligned header">${message(code: 'surveyOrg.address.label.heading')}</h2>--}%
                            <g:link controller="organisation" action="contacts" id="${participant.id}" params="[tab: 'addresses']" target="_blank"
                                    class="${Btn.SIMPLE} right floated">
                                <g:message code="survey.address.add"/>
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

                            <g:if test="${editable}">
                                <g:link controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                        params="${parame + [viewTab: 'invoicingInformation', subTab: 'xRechnung', setEInvoiceValuesFromOrg: true,]}"
                                        class="${Btn.SIMPLE} right floated">
                                    <g:message code="surveyOrg.setEInvoiceValuesFromOrg"/>
                                </g:link>
                                <br>
                                <br>
                            </g:if>

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
                                                <ui:xEditable owner="${surveyOrg}" field="eInvoiceLeitwegId" validation="leitwegID"/>
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

                                        <table class="ui table la-js-responsive-table la-table">
                                            <thead>
                                            <tr>
                                                <th>${message(code: 'sidewide.number')}</th>
                                                <th>${message(code: 'identifier')}</th>
                                                <th>${message(code: 'default.notes.label')}</th>
                                                <th></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <g:each in="${surveyOrg.org.getLeitkriteriums()}" var="leitkriterium" status="i">
                                                <tr>
                                                    <td>${i+1}</td>
                                                    <td>${leitkriterium.value}</td>
                                                    <td>${leitkriterium.note}</td>
                                                    <td>
                                                        <g:if test="${editable}">
                                                            <g:link controller="${controllerName}" action="${actionName}"
                                                                    id="${surveyInfo.id}"
                                                                    params="${parame + [viewTab: 'invoicingInformation', subTab: 'xRechnung', setEInvoiceLeitkriteriumFromOrg: leitkriterium.value]}"
                                                                    class="${Btn.SIMPLE} right floated">
                                                                <g:message code="surveyOrg.setEInvoiceLeitkriteriumFromOrg"/>
                                                            </g:link>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                            </g:each>
                                            </tbody>
                                        </table>
                                    </div>
                                </div><!-- .card -->
                            </div>
                        </div>
                    </div>

                </g:if>


                <g:if test="${params.viewTab == 'additionalInformation' && surveyConfig.subscription}">
                    <div class="sixteen wide column">
                        <div class="la-inline-lists">
                            <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig    : surveyConfig,
                                                                                                  subscription    : subscription,
                                                                                                  visibleProviders: providerRoles]"/>
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

                    <div class="sixteen wide column">

                        <h2 class="ui left floated aligned icon header la-clear-before">${message(code:'surveyVendors.all')}
                        <ui:totalNumber total="${vendorListTotal}"/>
                        </h2>

                        <g:set var="tmplConfigShowList" value="${['lineNumber', 'sortname', 'name', 'isWekbCurated', 'selectSurveyVendorResult']}"/>

                        <g:render template="/templates/survey/vendors" model="[
                                processController: controllerName,
                                processAction    : actionName,
                                tmplShowCheckbox : false,
                                tmplConfigShow   : tmplConfigShowList,
                                tmplConfigShowFilter: [['name']]]"/>
                    </div>

                </g:if>

                <g:if test="${params.viewTab == 'subscriptionSurvey' && surveyConfig.subscriptionSurvey}">

                    <div class="sixteen wide column">
                        <div class="la-inline-lists">
                            <g:render template="/templates/survey/costsWithSurveySubscriptions"/>
                        </div>
                    </div>

                    <div class="two wide column">
                        <div class="ui fluid vertical tabular menu">
                            <g:link class="item ${params.viewTab == 'subscriptionSurvey' && params.subTab == 'allSubscriptions' ? 'active' : ''}"
                                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                    params="${parame + [viewTab: 'subscriptionSurvey', subTab: 'allSubscriptions']}">
                                ${message(code: 'surveySubscriptions.all')}
                                <ui:bubble float="true" count="${SurveyConfigSubscription.countBySurveyConfig(surveyConfig)}"/>
                            </g:link>
                            <g:link class="item ${params.viewTab == 'subscriptionSurvey' && params.subTab == 'selectSubscriptions' ? 'active' : ''}"
                                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                    params="${parame + [viewTab: 'subscriptionSurvey', subTab: 'selectSubscriptions']}">
                                ${message(code: 'surveySubscriptions.selectedSubscriptions')}
                                <ui:bubble float="true" count="${SurveySubscriptionResult.countBySurveyConfigAndParticipant(surveyConfig, participant)}"/>
                            </g:link>
                        </div>
                    </div>

                    <div class="fourteen wide column">
                        <div class="ui tab active">
                            <h2 class="ui left floated aligned icon header la-clear-before">${message(code: params.subTab == 'selectSubscriptions' ? 'surveySubscriptions.selectedSubscriptions' : 'surveySubscriptions.all')}
                            <ui:totalNumber total="${num_sub_rows}"/>
                            </h2>

                            <g:render template="/templates/subscription/subscriptionFilter"/>

                            <g:form controller="$controllerName" action="$actionName" id="${surveyInfo.id}" params="${params}" method="post" class="ui form">

                                <g:render template="/survey/subscriptionTableForParticipant" model="[tmplShowCheckbox: editable]"/>


                                <g:if test="${editable && params.subTab == 'selectSubscriptions'}">
                                    <br>

                                    <div class="field">
                                        <button name="processOption" value="unlinkSubscriptions" type="submit"
                                                class="${Btn.SIMPLE_CLICKCONTROL}">${message(code: 'surveySubscriptions.unlinkSubscription.plural')}</button>
                                    </div>
                                </g:if>
                                <g:if test="${editable && params.subTab == 'allSubscriptions'}">
                                    <br>

                                    <div class="field">
                                        <button name="processOption" value="linkSubscriptions" type="submit"
                                                class="${Btn.SIMPLE_CLICKCONTROL}">${message(code: 'surveySubscriptions.linkSubscription.plural')}</button>
                                    </div>
                                </g:if>

                            </g:form>

                            <ui:paginate action="$actionName" controller="$controllerName" params="${params}"
                                         max="${max}" total="${num_sub_rows}"/>
                        </div>
                    </div>

                </g:if>
            </div>
        </div>
    </div>
</div>