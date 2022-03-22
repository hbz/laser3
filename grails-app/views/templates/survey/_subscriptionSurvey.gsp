<%@ page import="de.laser.SurveyConfig; de.laser.DocContext; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.properties.PropertyDefinition; de.laser.SurveyOrg; de.laser.SurveyConfigProperties; de.laser.Subscription; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory" %>
<laser:serviceInjection />
<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

<div class="ui stackable grid">
    <div class="ten wide column">
        <g:if test="${controllerName == 'survey' && actionName == 'show'}">

            <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>

                <g:link class="ui icon button right floated" controller="subscription" action="members" id="${subscription.id}">
                    <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>
                    <div class="ui blue circular label">
                        ${countParticipants.subMembers}
                    </div>
                </g:link>

                <g:link class="ui icon button right floated"  controller="survey" action="surveyParticipants"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id]">
                    <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>
                    <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
                </g:link>

                <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                    ( ${countParticipants.subMembersWithMultiYear}
                    ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
                </g:if>
                <br><br><br>
        </g:if>

        <div class="ui card ">
            <div class="content">

                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM') && surveyOrg}">
                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyOrg.ownerComment.label', args: [institution.sortname])}
                        </dt>
                        <dd><semui:xEditable owner="${surveyOrg}" field="ownerComment" type="textarea"/></dd>

                    </dl>
                </g:if>

                <g:if test="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon la-popup-tooltip la-delay"
                                     data-content="${message(code: "surveyconfig.scheduledStartDate.comment")}">
                                    ${message(code: 'surveyconfig.scheduledStartDate.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="scheduledStartDate" type="date"
                                                 overwriteEditable="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon la-popup-tooltip la-delay"
                                     data-content="${message(code: "surveyconfig.scheduledEndDate.comment")}">
                                    ${message(code: 'surveyconfig.scheduledEndDate.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"
                                                 overwriteEditable="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}"/></dd>

                        </dl>
                    </g:if>
                    <dl>
                        <dt class="control-label">
                            <div class="ui icon la-popup-tooltip la-delay"
                                 data-content="${message(code: "surveyconfig.internalComment.comment")}">
                                ${message(code: 'surveyconfig.internalComment.label')}
                                <i class="question small circular inverted icon"></i>
                            </div>
                        </dt>
                        <dd><semui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

                    </dl>
                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.url.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="url" type="text"/>
                            <g:if test="${surveyConfig.url}">
                                <semui:linkIcon href="${surveyConfig.url}"/>
                            </g:if>

                        </dd>
                        <dt class="control-label">
                            ${message(code: 'surveyInfo.comment.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="urlComment" type="textarea"/>
                        </dd>

                    </dl>

                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.url2.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="url2" type="text"/>
                            <g:if test="${surveyConfig.url2}">
                                <semui:linkIcon href="${surveyConfig.url2}"/>
                            </g:if>
                        </dd>
                        <dt class="control-label">
                            ${message(code: 'surveyInfo.comment.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="urlComment2" type="textarea"/>
                        </dd>

                    </dl>

                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.url3.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="url3" type="text"/>
                            <g:if test="${surveyConfig.url3}">
                                <semui:linkIcon href="${surveyConfig.url3}"/>
                            </g:if>
                        </dd>
                        <dt class="control-label">
                            ${message(code: 'surveyInfo.comment.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="urlComment3" type="textarea"/>
                        </dd>

                    </dl>

                    <br />

                    <div class="ui form">
                        <g:form action="setSurveyConfigComment" controller="survey" method="post"
                                params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]">
                            <div class="field">
                                <label><div class="ui icon la-popup-tooltip la-delay"
                                            data-content="${message(code: "surveyconfig.comment.comment")}">
                                    ${message(code: 'surveyconfig.comment.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div></label>
                                <textarea name="comment" rows="15">${surveyConfig.comment}</textarea>
                            </div>

                            <div class="left aligned">
                                <button type="submit"
                                        class="ui button">${message(code: 'default.button.save_changes')}</button>
                            </div>
                        </g:form>
                    </div>

                </g:if>
                <g:else>
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.scheduledStartDate.label')}
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="scheduledStartDate" type="date"
                                                 overwriteEditable="${false}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.scheduledEndDate.label')}
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"
                                                 overwriteEditable="${false}"/></dd>

                        </dl>
                    </g:if>

                    <g:if test="${surveyConfig.url}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label')}
                            </dt>
                            <dd>
                                <semui:xEditable owner="${surveyConfig}" field="url" type="text"
                                                 overwriteEditable="${false}"/>

                                <g:if test="${surveyConfig.urlComment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyConfig.urlComment}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>
                                    <semui:linkIcon href="${surveyConfig.url}"/>
                            </dd>

                        </dl>
                    </g:if>

                    <g:if test="${surveyConfig.url2}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url2.label')}
                            </dt>
                            <dd>
                                <semui:xEditable owner="${surveyConfig}" field="url2" type="text"
                                                 overwriteEditable="${false}"/>

                                <g:if test="${surveyConfig.urlComment2}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyConfig.urlComment2}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>

                                <semui:linkIcon href="${surveyConfig.url2}"/>
                            </dd>

                        </dl>
                    </g:if>

                    <g:if test="${surveyConfig.url3}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url3.label')}
                            </dt>
                            <dd>
                                <semui:xEditable owner="${surveyConfig}" field="url3" type="text"
                                                 overwriteEditable="${false}"/>

                                <g:if test="${surveyConfig.urlComment3}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyConfig.urlComment3}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>

                                <semui:linkIcon href="${surveyConfig.url3}"/>
                            </dd>

                        </dl>
                    </g:if>

                    <div class="ui form la-padding-left-07em">
                        <div class="field">
                            <label>
                                <g:message code="surveyConfigsInfo.comment"/>
                            </label>
                            <g:if test="${surveyConfig.comment}">
                                <textarea readonly="readonly" rows="1">${surveyConfig.comment}</textarea>
                            </g:if>
                            <g:else>
                                <g:message code="surveyConfigsInfo.comment.noComment"/>
                            </g:else>
                        </div>
                    </div>
                </g:else>

                <br />

                <div class="field" style="text-align: right;">
                    <button id="subscription-info-toggle"
                            class="ui button blue la-modern-button"><g:message code="surveyConfigsInfo.subscriptionInfo.show"/> <i class="ui angle double down icon"></i></button></button>
                    <laser:script file="${this.getGroovyPageFileName()}">
                        $('#subscription-info-toggle').on('click', function () {
                            $("#subscription-info").transition('slide down');
                            if ($("#subscription-info").hasClass('visible')) {
                                $(this).html('<g:message code="surveyConfigsInfo.subscriptionInfo.show"/> <i class="ui angle double down icon"></i>')
                            } else {
                                $(this).html('<g:message code="surveyConfigsInfo.subscriptionInfo.hide"/> <i class="ui angle double up icon"></i>')
                            }
                        })
                    </laser:script>
                </div>

                <g:if test="${subscription && subscription.packages}">

                                <div class="field" style="text-align: right;">
                                    <button id="package-info-toggle"
                                            class="ui button blue la-modern-button"><g:message code="surveyConfigsInfo.packageInfo.show"/> <i class="ui angle double down icon"></i></button></button>
                                    <laser:script file="${this.getGroovyPageFileName()}">
                                        $('#package-info-toggle').on('click', function () {
                                            $("#packages").transition('slide down');
                                            if ($("#packages").hasClass('visible')) {
                                                $(this).html('<g:message code="surveyConfigsInfo.packageInfo.show"/> <i class="ui angle double down icon"></i>')
                                            } else {
                                                $(this).html('<g:message code="surveyConfigsInfo.packageInfo.hide"/> <i class="ui angle double up icon"></i>')
                                            }
                                        })
                                    </laser:script>
                                </div>
                </g:if>

            </div>
        </div>

        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
            <g:render template="/templates/survey/propertiesCompareInfo" model="[customProperties: customProperties]"/>
        </g:if>

        <div id="subscription-info" class="la-inline-lists hidden">

            <div class="ui card">
                <div class="content">
                    <div class="header">
                        <g:if test="${!subscription}">
                            <semui:headerTitleIcon type="Subscription"/>
                            <h2 class="ui icon header">
                                <g:link controller="public" action="gasco"
                                        params="${[q: '"' + surveyConfig.subscription.name + '"']}">
                                    ${surveyConfig.subscription.name}
                                </g:link>
                            </h2>

                            <div class="field" style="text-align: right;">
                                <g:link class="ui button" controller="public" action="gasco"
                                        params="${[q: '"' + surveyConfig.subscription.name + '"']}">
                                    GASCO-Monitor
                                </g:link>
                            </div>
                        </g:if>
                        <g:else>
                            <semui:headerTitleIcon type="Subscription"/>
                            <h2 class="ui icon header">
                            <g:link controller="subscription" action="show" id="${subscription.id}">
                                ${subscription.name}
                            </g:link>
                            </h2>
                            <semui:auditInfo auditable="[subscription, 'name']"/>
                        </g:else>
                    </div>
                </div>
                <g:if test="${subscription}">
                    <div class="content">
                        <div class="ui two column stackable grid container">
                            <div class="column">
                                <dl>
                                    <dt class="control-label">${message(code: 'default.status.label')}</dt>
                                    <dd>${subscription.status.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscription, 'status']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.kind.label')}</dt>
                                    <dd>${subscription.kind?.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscription, 'kind']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                                    <dd>${subscription.form?.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscription, 'form']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                                    <dd>${subscription.resource?.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscription, 'resource']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                                    <dd>${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscription, 'hasPerpetualAccess']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">
                                        <g:message code="default.identifiers.label"/>
                                    </dt>
                                    <dd>
                                        <g:each in="${subscription.ids?.sort { it.ns.ns }}"
                                                var="id">
                                            <span class="ui small basic image label">
                                                ${id.ns.ns}: <div class="detail">${id.value}</div>
                                            </span>
                                        </g:each>
                                    </dd>
                                </dl>
                            </div>


                            <div class="column">
                                %{--<g:if test="${subscription.packages}">
                                    <table class="ui three column la-selectable table">
                                        <g:each in="${subscription.packages.sort { it.pkg.name }}" var="sp">
                                            <tr>
                                                <th scope="row"
                                                    class="control-label la-js-dont-hide-this-card">${message(code: 'subscription.packages.label')}</th>
                                                <td>
                                                    <g:link controller="package" action="show"
                                                            id="${sp.pkg.id}">${sp.pkg.name}</g:link>

                                                    <g:if test="${sp.pkg.contentProvider}">
                                                        (${sp.pkg.contentProvider.name})
                                                    </g:if>
                                                </td>
                                                <td class="right aligned">
                                                </td>

                                            </tr>
                                        </g:each>
                                    </table>
                                </g:if>--}%

                                <g:if test="${visibleOrgRelations}">

                                    <g:render template="/templates/links/orgLinksAsList"
                                              model="${[roleLinks    : visibleOrgRelations,
                                                        roleObject   : subscription,
                                                        roleRespValue: 'Specific subscription editor',
                                                        editmode     : false,
                                                        showPersons  : false
                                              ]}"/>

                                </g:if>
                            </div>
                        </div>


                        <br />

                           <div class="ui form">

                            <g:set var="oldEditable" value="${editable}"/>
                            <div id="subscription-properties" style="margin: 1em 0">
                                <g:set var="editable" value="${false}" scope="request"/>
                                <g:set var="editable" value="${false}" scope="page"/>
                                <g:render template="/subscription/properties" model="${[
                                        subscription: subscription, calledFromSurvey: true
                                ]}"/>

                            </div>

                            <g:set var="editable" value="${oldEditable ?: false}" scope="page"/>
                            <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                        </div>


                        <div class="ui card">
                            <div class="content">
                                <h2 class="ui header">
                                    <g:message code="license.plural"/>
                                </h2>

                                <g:if test="${links && links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}">
                                    <table class="ui fixed table">
                                        <g:each in="${links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}" var="link">
                                            <tr><g:set var="pair" value="${link.getOther(subscription)}"/>
                                                <th scope="row" class="control-label la-js-dont-hide-this-card">${pair.licenseCategory?.getI10n("value")}</th>
                                                <td>
                                                    <g:link controller="license" action="show" id="${pair.id}">
                                                        ${pair.reference} (${pair.status.getI10n("value")})
                                                    </g:link>
                                                    <g:formatDate date="${pair.startDate}" format="${message(code:'default.date.format.notime')}"/>-<g:formatDate date="${pair.endDate}" format="${message(code:'default.date.format.notime')}"/><br />
                                                    <g:set var="comment" value="${DocContext.findByLink(link)}"/>
                                                    <g:if test="${comment}">
                                                        <em>${comment.owner.content}</em>
                                                    </g:if>
                                                </td>
                                                <td class="right aligned">
                                                    <g:if test="${pair.propertySet}">
                                                        <button id="derived-license-properties-toggle${link.id}"
                                                                class="ui icon blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay"
                                                                data-content="${message(code:'subscription.details.viewLicenseProperties')}">
                                                            <i class="ui angle double down icon"></i>
                                                        </button>
                                                        <laser:script file="${this.getGroovyPageFileName()}">
                                                    $("#derived-license-properties-toggle${link.id}").on('click', function() {
                                                        $("#derived-license-properties${link.id}").transition('slide down');
                                                        //$("#derived-license-properties${link.id}").toggleClass('hidden');

                                                        if ($("#derived-license-properties${link.id}").hasClass('visible')) {
                                                            $(this).html('<i class="ui angle double down icon"></i>')
                                                        } else {
                                                            $(this).html('<i class="ui angle double up icon"></i>')
                                                        }
                                                    })
                                                        </laser:script>
                                                    </g:if>
                                                </td>
                                            </tr>
                                            <g:if test="${pair.propertySet}">
                                                <tr>
                                                    <td colspan="3"><div id="${link.id}Properties"></div></td>
                                                </tr>
                                            </g:if>
                                        </g:each>
                                    </table>
                                </g:if>

                            </div><!-- .content -->
                        </div>

                    </div>
                </g:if>
            </div>

        </div>

        <g:if test="${subscription && subscription.packages}">
            <div id="packages" class="la-inline-lists hidden"></div>
        </g:if>


    </div>

    <aside class="six wide column la-sidekick">
        <div class="ui one cards">

            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <div id="container-tasks">
                    <g:render template="/templates/tasks/card"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>

                </div>
                <div id="container-notes">
                    <g:render template="/templates/notes/card"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')]}"/>
                </div>

                <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')}">

                    <g:render template="/templates/tasks/modal_create"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>

                </g:if>
                <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')}">
                    <g:render template="/templates/notes/modal_create"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
                </g:if>
            </g:if>

            <div id="container-documents">
                <g:render template="/survey/cardDocuments"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>

        </div>
    </aside>

</div><!-- .grid -->

<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
    <g:set var="costItemSurvey"
           value="${surveyOrg ? CostItem.findBySurveyOrg(surveyOrg) : null}"/>

    <g:if test="${surveyInfo.owner.id != institution.id && ((costItemSums && costItemSums.subscrCosts) || costItemSurvey)}">
        <g:set var="showCostItemSurvey" value="${true}"/>

        <div class="ui card la-time-card">

            <div class="content">
                <div class="header"><g:message code="surveyConfigsInfo.costItems"/></div>
            </div>

            <div class="content">
                <%
                    def elementSign = 'notSet'
                    String icon = ''
                    String dataTooltip = ""
                %>

                <table class="ui celled compact la-js-responsive-table la-table-inCard table">
                    <thead>
                    <tr>
                        <th colspan="4" class="center aligned">
                            <g:message code="surveyConfigsInfo.oldPrice"/>
                        </th>
                        <th colspan="4" class="center aligned">
                            <g:message code="surveyConfigsInfo.newPrice"/>
                        </th>
                        <th rowspan="2">Diff.</th>
                    </tr>
                    <tr>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.costInBillingCurrency"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.taxRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.costInBillingCurrencyAfterTax"/></th>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.costInBillingCurrency"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.taxRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.costInBillingCurrencyAfterTax"/></th>

                    </tr>
                    </thead>


                    <tbody class="top aligned">
                    <g:if test="${costItemSums && costItemSums.subscrCosts}">
                        <g:each in="${costItemSums.subscrCosts.sort { it.costItemElement }}" var="costItem">
                            <tr>
                                <td>
                                    <%
                                        elementSign = 'notSet'
                                        icon = ''
                                        dataTooltip = ""
                                        if (costItem.costItemElementConfiguration) {
                                            elementSign = costItem.costItemElementConfiguration
                                        }
                                        switch (elementSign) {
                                            case RDStore.CIEC_POSITIVE:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.positive')
                                                icon = '<i class="plus green circle icon"></i>'
                                                break
                                            case RDStore.CIEC_NEGATIVE:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.negative')
                                                icon = '<i class="minus red circle icon"></i>'
                                                break
                                            case RDStore.CIEC_NEUTRAL:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.neutral')
                                                icon = '<i class="circle yellow icon"></i>'
                                                break
                                            default:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.notSet')
                                                icon = '<i class="question circle icon"></i>'
                                                break
                                        }
                                    %>
                                    <span class="la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${dataTooltip}">${raw(icon)}</span>

                                    ${costItem.costItemElement?.getI10n('value')}
                                </td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItem.costInBillingCurrency}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${(costItem.billingCurrency?.getI10n('value').split('-')).first()}
                                </td>
                                <td>${costItem.taxKey ? costItem.taxKey.taxType?.getI10n("value") + " (" + costItem.taxKey.taxRate + "%)" : ''}</td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItem.costInBillingCurrencyAfterTax}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${(costItem.billingCurrency?.getI10n('value').split('-')).first()}

                                    <g:if test="${costItem.startDate || costItem.endDate}">
                                        <br />(${formatDate(date: costItem.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItem.endDate, format: message(code: 'default.date.format.notime'))})
                                    </g:if>
                                </td>

                                <g:if test="${costItemSurvey && costItemSurvey.costItemElement == costItem.costItemElement}">
                                    <g:set var="showCostItemSurvey" value="${false}"/>
                                    <td>
                                        <%
                                            elementSign = 'notSet'
                                            icon = ''
                                            dataTooltip = ""
                                            if (costItemSurvey.costItemElementConfiguration) {
                                                elementSign = costItemSurvey.costItemElementConfiguration
                                            }
                                            switch (elementSign) {
                                                case RDStore.CIEC_POSITIVE:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.positive')
                                                    icon = '<i class="plus green circle icon"></i>'
                                                    break
                                                case RDStore.CIEC_NEGATIVE:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.negative')
                                                    icon = '<i class="minus red circle icon"></i>'
                                                    break
                                                case RDStore.CIEC_NEUTRAL:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.neutral')
                                                    icon = '<i class="circle yellow icon"></i>'
                                                    break
                                                default:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.notSet')
                                                    icon = '<i class="question circle icon"></i>'
                                                    break
                                            }
                                        %>
                                        <span class="la-popup-tooltip la-delay" data-position="right center"
                                              data-content="${dataTooltip}">${raw(icon)}</span>

                                        ${costItemSurvey.costItemElement?.getI10n('value')}
                                    </td>
                                    <td>
                                        <strong>
                                            <g:formatNumber
                                                    number="${costItemSurvey.costInBillingCurrency}"
                                                    minFractionDigits="2" maxFractionDigits="2"
                                                    type="number"/>
                                        </strong>

                                        ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}
                                    </td>
                                    <td>${costItemSurvey.taxKey ? costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)" : ''}</td>
                                    <td>
                                        <strong>
                                            <g:formatNumber
                                                    number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                                    minFractionDigits="2" maxFractionDigits="2"
                                                    type="number"/>
                                        </strong>

                                        ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}


                                        <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                            <br />(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                                        </g:if>

                                        <g:if test="${costItemSurvey.costDescription}">
                                            <br />

                                            <div class="ui icon la-popup-tooltip la-delay"
                                                 data-position="right center"
                                                 data-variation="tiny"
                                                 data-content="${costItemSurvey.costDescription}">
                                                <i class="question small circular inverted icon"></i>
                                            </div>
                                        </g:if>
                                    </td>

                                </g:if>
                                <g:else>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </g:else>

                                <td>
                                    <g:if test="${costItemSurvey && costItemSurvey.costItemElement == costItem.costItemElement}">

                                        <g:set var="oldCostItem"
                                               value="${costItem.costInBillingCurrency ?: 0.0}"/>

                                        <g:set var="newCostItem"
                                               value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                        <strong><g:formatNumber
                                                number="${(newCostItem - oldCostItem)}"
                                                minFractionDigits="2" maxFractionDigits="2" type="number"/>
                                            <br />
                                            (<g:formatNumber
                                                    number="${((newCostItem - oldCostItem) / oldCostItem) * 100}"
                                                    minFractionDigits="2"
                                                    maxFractionDigits="2" type="number"/>%)</strong>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>

                    <g:if test="${showCostItemSurvey && costItemSurvey}">
                        <tr>

                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>

                            <td>
                                <%
                                    elementSign = 'notSet'
                                    icon = ''
                                    dataTooltip = ""
                                    if (costItemSurvey.costItemElementConfiguration) {
                                        elementSign = costItemSurvey.costItemElementConfiguration
                                    }
                                    switch (elementSign) {
                                        case RDStore.CIEC_POSITIVE:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.positive')
                                            icon = '<i class="plus green circle icon"></i>'
                                            break
                                        case RDStore.CIEC_NEGATIVE:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.negative')
                                            icon = '<i class="minus red circle icon"></i>'
                                            break
                                        case RDStore.CIEC_NEUTRAL:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.neutral')
                                            icon = '<i class="circle yellow icon"></i>'
                                            break
                                        default:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.notSet')
                                            icon = '<i class="question circle icon"></i>'
                                            break
                                    }
                                %>

                                <span class="la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${dataTooltip}">${raw(icon)}</span>


                                ${costItemSurvey.costItemElement?.getI10n('value')}

                            </td>
                            <td>
                                <strong>
                                    <g:formatNumber
                                            number="${costItemSurvey.costInBillingCurrency}"
                                            minFractionDigits="2" maxFractionDigits="2"
                                            type="number"/>
                                </strong>

                                ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}
                            </td>
                            <td>${costItemSurvey.taxKey ? costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)" : ''}</td>
                            <td>
                                <strong>
                                    <g:formatNumber
                                            number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                            minFractionDigits="2" maxFractionDigits="2"
                                            type="number"/>
                                </strong>

                                ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}

                                <g:set var="newCostItem"
                                       value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                    <br />(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                                </g:if>

                                <g:if test="${costItemSurvey.costDescription}">
                                    <br />

                                    <div class="ui icon la-popup-tooltip la-delay"
                                         data-position="right center"
                                         data-variation="tiny"
                                         data-content="${costItemSurvey.costDescription}">
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </g:if>
                            </td>

                            <td>
                            </td>
                        </tr>
                    </g:if>

                    </tbody>
                </table>
            </div>
        </div>
    </g:if>

    <g:if test="${surveyInfo.owner.id == institution.id && costItemSums.consCosts}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <g:if test="${costItemSums.ownCosts}">
                    <g:if test="${(contextOrg.id != subscription.getConsortia()?.id && subscription.instanceOf) || !subscription.instanceOf}">
                        <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.ownCosts')}</h2>
                        <g:render template="/subscription/financials" model="[data: costItemSums.ownCosts]"/>
                    </g:if>
                </g:if>
                <g:if test="${costItemSums.consCosts}">
                    <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.consCosts')}</h2>
                    <g:render template="/subscription/financials" model="[data: costItemSums.consCosts]"/>
                </g:if>
            </div>
        </div>
    </g:if>
</g:if>

<g:if test="${controllerName == 'survey' && actionName == 'show'}">
    <g:set var="surveyProperties" value="${surveyConfig.surveyProperties}"/>

    <semui:form>

        <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'surveyProperty.selected.label')} <semui:totalNumber
                total="${surveyProperties.size()}"/></h4>

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.name')}</th>
                <th>${message(code: 'surveyProperty.expl.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyProperty.mandatoryProperty')}</th>
                <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING && surveyProperties}">
                    <th>${message(code:'default.actions.label')}</th>
                </g:if>
            </tr>
            </thead>

            <tbody>
            <g:each in="${surveyProperties.sort { it.surveyProperty.getI10n('name') }}" var="surveyProperty" status="i">
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyProperty.surveyProperty.getI10n('name')}

                        <g:if test="${surveyProperty.surveyProperty.tenant?.id == contextService.getOrg().id}">
                            <i class='shield alternate icon'></i>
                        </g:if>

                        <g:if test="${surveyProperty.surveyProperty.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyProperty.surveyProperty.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                    </td>

                    <td>
                        <g:if test="${surveyProperty.surveyProperty.getI10n('expl')}">
                            ${surveyProperty.surveyProperty.getI10n('expl')}
                        </g:if>
                    </td>
                    <td>
                        ${PropertyDefinition.getLocalizedValue(surveyProperty.surveyProperty.type)}
                        <g:if test="${surveyProperty.surveyProperty.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(surveyProperty.surveyProperty.refdataCategory)}"
                                    var="refdataValue">
                                <g:set var="refdataValues"
                                       value="${refdataValues + refdataValue?.getI10n('value')}"/>
                            </g:each>
                            <br />
                            (${refdataValues.join('/')})
                        </g:if>
                    </td>

                    <td>
                        <g:set var="surveyPropertyMandatoryEditable" value="${(editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                                (surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL || (surveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL && surveyProperty.surveyProperty != RDStore.SURVEY_PROPERTY_PARTICIPATION)))}"/>
                        <g:form action="surveyPropertyMandatory" method="post" class="ui form"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveyConfigProperties: surveyProperty.id]">

                            <div class="ui checkbox">
                                <input type="checkbox"
                                       onchange="${surveyPropertyMandatoryEditable ? 'this.form.submit()' :  ''}" ${!surveyPropertyMandatoryEditable ? 'readonly="readonly" disabled="true"' : ''}
                                       name="mandatoryProperty" ${surveyProperty.mandatoryProperty ? 'checked' : ''}>
                            </div>
                        </g:form>
                    </td>
                    <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                            SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyProperty.surveyProperty)
                            && ((RDStore.SURVEY_PROPERTY_PARTICIPATION.id != surveyProperty.surveyProperty.id) || surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL)}">
                        <td>
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyElements", args: [surveyProperty.surveyProperty.getI10n('name')])}"
                                    data-confirm-term-how="delete"
                                    controller="survey" action="deleteSurveyPropFromConfig"
                                    id="${surveyProperty.id}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </td>
                    </g:if>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <g:if test="${editable && properties && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
                    <td colspan="6">
                        <g:form action="addSurveyPropToConfig" controller="survey" method="post" class="ui form">
                            <g:hiddenField name="id" value="${surveyInfo.id}"/>
                            <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>

                            <div class="field required">
                                <label>${message(code: 'surveyConfigs.property')} <g:message code="messageRequiredField" /></label>
                                <semui:dropdown name="selectedProperty"

                                                class="la-filterPropDef"
                                                from="${properties}"
                                                iconWhich="shield alternate"
                                                optionKey="${{ "${it.id}" }}"
                                                optionValue="${{ it.getI10n('name') }}"
                                                noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                                required=""/>

                            </div>
                            <input type="submit" class="ui button"
                                   value="${message(code: 'surveyConfigsInfo.add.button')}"/>

                        </g:form>
                    </td>
                </g:if>
            </tr>
            </tfoot>

        </table>

    </semui:form>
</g:if>

<g:if test="${surveyResults}">

    <semui:form>
        <h3 class="ui header"><g:message code="surveyConfigsInfo.properties"/>
        <semui:totalNumber
                total="${surveyResults.size()}"/>
        </h3>

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>
                    <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_USER')}">
                        ${message(code: 'surveyResult.participantComment')}
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
                <th>
                    <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_USER')}">
                        ${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentOnlyForParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
            </tr>
            </thead>
            <g:each in="${surveyResults.sort{it.type.getI10n('name')}}" var="surveyResult" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyResult.type.getI10n('name')}

                        <g:if test="${surveyResult.type.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${surveyResult.type.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:set var="surveyConfigProperties"
                               value="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyResult.surveyConfig, surveyResult.type)}"/>
                        <g:if test="${surveyConfigProperties && surveyConfigProperties.mandatoryProperty}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'default.mandatory.tooltip')}">
                                <i class="info circle icon"></i>
                            </span>
                        </g:if>


                    </td>
                    <td>
                        ${PropertyDefinition.getLocalizedValue(surveyResult.type.type)}
                        <g:if test="${surveyResult.type.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(surveyResult.type.refdataCategory)}"
                                    var="refdataValue">
                                <g:if test="${refdataValue.getI10n('value')}">
                                    <g:set var="refdataValues"
                                           value="${refdataValues + refdataValue.getI10n('value')}"/>
                                </g:if>
                            </g:each>
                            <br />
                            (${refdataValues.join('/')})
                        </g:if>
                    </td>
                    <g:set var="surveyOrg"
                           value="${SurveyOrg.findBySurveyConfigAndOrg(surveyResult.surveyConfig, institution)}"/>

                    <g:if test="${surveyResult.surveyConfig.subSurveyUseForTransfer && surveyOrg && surveyOrg.existsMultiYearTerm()}">
                        <td>
                            <g:message code="surveyOrg.perennialTerm.available"/>
                        </td>
                        <td>

                        </td>
                        <td>

                        </td>
                    </g:if>
                    <g:else>
                        <td>
                            <g:if test="${surveyResult.type.isIntegerType()}">
                                <semui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                            </g:if>
                            <g:elseif test="${surveyResult.type.isStringType()}">
                                <semui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isBigDecimalType()}">
                                <semui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isDateType()}">
                                <semui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isURLType()}">
                                <semui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                                 overwriteEditable="${overwriteEditable}"
                                                 class="la-overflow la-ellipsis"/>
                                <g:if test="${surveyResult.urlValue}">
                                    <semui:linkIcon/>
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isRefdataValueType()}">

                                <g:if test="${surveyResult.surveyConfig.subSurveyUseForTransfer && surveyResult.type == RDStore.SURVEY_PROPERTY_PARTICIPATION && surveyResult.owner?.id != contextService.getOrg().id}">
                                    <semui:xEditableRefData  tokenmsg="${message(code: 'survey.participationProperty.confirmation')}"
                                                             how="ok"
                                                             cssClass="js-open-confirm-modal-xeditable"
                                                             confirmationValue="${RefdataValue.class.name}:${RDStore.YN_NO.id}"
                                                             owner="${surveyResult}"
                                                             field="refValue" type="text"
                                                             id="participation"
                                                             config="${surveyResult.type.refdataCategory}"/>
                                </g:if>
                                <g:else>
                                    <semui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                            config="${surveyResult.type.refdataCategory}"/>
                                </g:else>
                            </g:elseif>
                        </td>
                        <td>
                            <semui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                        </td>
                        <td>
                            <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_USER')}">
                                <semui:xEditable owner="${surveyResult}" type="textarea" field="ownerComment"/>
                            </g:if>
                            <g:else>
                                <semui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                            </g:else>
                        </td>
                    </g:else>

                </tr>
            </g:each>
        </table>
    </semui:form>
    <br />
</g:if>



<laser:script file="${this.getGroovyPageFileName()}">

    $('body #participation').editable('destroy').editable({
        tpl: '<select class="ui dropdown"></select>'
        }).on('shown', function() {
            r2d2.initDynamicSemuiStuff('body');
            $(".table").trigger('reflow');
            $('.ui.dropdown').dropdown({ clearable: true });
        }).on('hidden', function() {
            $(".table").trigger('reflow')
    });
    <g:if test="${links}">
       <g:each in="${links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}" var="link">
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="getLicensePropertiesForSubscription" />",
                      data: {
                           loadFor: "${link.sourceLicense.id}",
                           linkId: ${link.id}
                }
            }).done(function(response) {
                $("#${link.id}Properties").html(response);
            }).fail();
       </g:each>
    </g:if>

    <g:if test="${subscription && subscription.packages}">
        JSPC.app.loadPackages = function () {
                  $.ajax({
                      url: "<g:createLink controller="ajaxHtml" action="getGeneralPackageData"/>",
                      data: {
                          subscription: "${subscription.id}"
                      }
                  }).done(function(response){
                      $("#packages").html(response);
                      r2d2.initDynamicSemuiStuff("#packages");
                  })
              }


        JSPC.app.loadPackages();
    </g:if>

</laser:script>
