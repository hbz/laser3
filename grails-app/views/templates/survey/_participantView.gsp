<%@ page import="de.laser.storage.RDConstants; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.properties.PropertyDefinition;" %>


<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <g:link class="item ${params.tab == 'overview' ? 'active' : ''}"
                    controller="myInstitution" action="surveyInfos" id="${surveyInfo.id}"
                    params="[surveyConfigID: surveyConfig.id, tab: 'overview']">

                ${message(code: 'default.overview.label')}
            </g:link>

            <g:if test="${surveyConfig.subSurveyUseForTransfer}">

                <g:link class="item ${params.tab == 'invoicingInformation' ? 'active' : ''}"
                        controller="myInstitution" action="surveyInfos" id="${surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: 'invoicingInformation']">
                    ${message(code: 'surveyOrg.invoicingInformation')}
                    <span class="ui floating blue circular label">${SurveyOrg.countByOrgAndSurveyConfigAndPersonIsNotNull(institution, surveyConfig)}/${SurveyOrg.countByOrgAndSurveyConfigAndAddressIsNotNull(institution, surveyConfig)}</span>
                </g:link>

            </g:if>
        </div>


        <div class="ui bottom attached tab segment active">
            <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                <g:if test="${params.tab == 'invoicingInformation'}">

                    <div class="ui top attached stackable tabular la-tab-with-js menu">
                        <a class="active item" data-tab="contacts">
                            ${message(code: 'surveyOrg.person.label')}
                            <span class="ui floating blue circular label">${SurveyOrg.countByOrgAndSurveyConfigAndPersonIsNotNull(institution, surveyConfig)}</span>
                        </a>

                        <a class="item" data-tab="addresses">
                            ${message(code: 'surveyOrg.address.label')}
                            <span class="ui floating blue circular label">${SurveyOrg.countByOrgAndSurveyConfigAndAddressIsNotNull(institution, surveyConfig)}</span>
                        </a>

                        <a class="item" data-tab="xRechnung">
                            ${message(code: 'surveyOrg.eInvoice.label')}
                        </a>
                    </div>


                    <div class="ui bottom attached tab segment active" data-tab="contacts">
                        <laser:render template="/templates/cpa/person_table" model="${[
                                persons       : visiblePersons,
                                showContacts  : true,
                                showAddresses : true,
                                showOptions   : false,
                                tmplConfigShow: ['lineNumber', 'function', 'position', 'name', 'showContacts', 'showAddresses', 'surveyInvoicingInformation']
                        ]}"/>

                    </div>

                    <div class="ui bottom attached tab segment" data-tab="addresses">

                        <laser:render template="/templates/cpa/address_table" model="${[
                                addresses                     : addresses,
                                editable                      : editable,
                                showSurveyInvoicingInformation: true,
                                showOptions                   : false
                        ]}"/>

                    </div>

                    <div class="ui bottom attached tab segment" data-tab="xRechnung">

                        <ui:msg message="surveyOrg.eInvoice.expl" icon="info" noClose="true"/>

                        <div class="la-inline-lists">
                            <div class="ui card">
                                <div class="content">
                                    <dl>
                                        <dt>
                                            <g:message code="surveyOrg.eInvoicePortal.label"/>
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                  data-content="${message(code: 'surveyOrg.eInvoicePortal.expl')}">
                                                <i class="question circle icon"></i>
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
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                  data-content="${message(code: 'surveyOrg.eInvoiceLeitwegId.expl')}">
                                                <i class="question circle icon"></i>
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
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                  data-content="${message(code: 'surveyOrg.eInvoiceLeitkriterium.expl')}">
                                                <i class="question circle icon"></i>
                                            </span>
                                        </dt>
                                        <dd>
                                            <ui:xEditable owner="${surveyOrg}" field="eInvoiceLeitkriterium"/>
                                        </dd>
                                    </dl>
                                </div>
                            </div><!-- .card -->
                        </div>
                    </div>

                </g:if>
            </g:if>

            <g:if test="${params.tab == 'overview'}">

                <div class="la-inline-lists">
                    <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">

                        <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig       : surveyConfig,
                                                                                              costItemSums       : costItemSums,
                                                                                              subscription       : subscription,
                                                                                              visibleOrgRelations: visibleOrgRelations,
                                                                                              surveyResults      : surveyResults]"/>

                    </g:if>

                    <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                        <laser:render template="/templates/survey/generalSurvey" model="[surveyConfig : surveyConfig,
                                                                                         surveyResults: surveyResults]"/>
                    </g:if>

                    <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">

                        <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig       : surveyConfig,
                                                                                              costItemSums       : costItemSums,
                                                                                              subscription       : subscription,
                                                                                              visibleOrgRelations: visibleOrgRelations,
                                                                                              surveyResults      : surveyResults]"/>

                        <laser:render template="/templates/survey/entitlementSurvey"/>
                    </g:if>

                </div>
            </g:if>
        </div>
    </div>
</div>