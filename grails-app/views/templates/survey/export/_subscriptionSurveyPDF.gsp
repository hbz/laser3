<%@ page import="de.laser.utils.DateUtils; de.laser.remote.ApiSource; de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.properties.PropertyDefinition; de.laser.Subscription; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.SubscriptionPackage;" %>
<g:set bean="genericOIDService" var="genericOIDService"/>
<g:set bean="gokbService" var="gokbService"/>
<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

<g:if test="${ownerView && surveyOrg}">
    <dl>
        <dt>
            ${message(code: 'surveyOrg.ownerComment.label', args: [institution.sortname])}
        </dt>
        <dd>
            ${surveyOrg.ownerComment}
        </dd>
    </dl>
</g:if>


<g:if test="${surveyConfig.subSurveyUseForTransfer}">
    <dl>
        <dt>
            ${message(code: 'surveyconfig.scheduledStartDate.label')}
        </dt>
        <dd><ui:xEditable owner="${surveyConfig}" field="scheduledStartDate" type="date"
                             overwriteEditable="${false}"/>
        </dd>
    </dl>
    <dl>
        <dt>
            ${message(code: 'surveyconfig.scheduledEndDate.label')}
        </dt>
        <dd><ui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"
                             overwriteEditable="${false}"/></dd>
    </dl>
</g:if>

<g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
    <dl>
        <dt>
            ${message(code: 'surveyconfig.url.label', args: [i+1])}
        </dt>
        <dd>
            <g:link uri="${surveyUrl.url}">
                ${surveyUrl.url}
            </g:link>
            <g:if test="${surveyUrl.urlComment}">
                <br> ${message(code: 'surveyconfig.urlComment.label', args: [i+1])}: ${surveyUrl.urlComment}">
            </g:if>
        </dd>
    </dl>
</g:each>

<dl>
    <dt>
        <g:message code="surveyConfigsInfo.comment"/>
    </dt>
    <dd>
        <g:if test="${subscription}">
            <g:if test="${surveyConfig.comment}">
                ${surveyConfig.comment}
            </g:if>
            <g:else>
                <g:message code="surveyConfigsInfo.comment.noComment"/>
            </g:else>
        </g:if>
        <g:else>
            <g:if test="${surveyConfig.commentForNewParticipants}">
                ${surveyConfig.commentForNewParticipants}
            </g:if>
            <g:else>
                <g:message code="surveyConfigsInfo.comment.noComment"/>
            </g:else>
        </g:else>
    </dd>
</dl>

<g:if test="${surveyConfig.subSurveyUseForTransfer}">

    <g:render template="/templates/survey/propertiesCompareInfo" model="[customProperties: customProperties]"/>

    <br>
    <br>
</g:if>

<h2><g:message code="subscription.label"/></h2>
<g:if test="${!subscription}">
    <div class="withBorder" style="border: 1px solid black;">
        <h4>
            <g:link absolute="true" controller="public" action="gasco"
                    params="${[q: surveyConfig.subscription.name, consortia: "${surveyInfo.owner.class.name}:${surveyInfo.owner.id}"]}">
                ${surveyConfig.subscription.name} (GASCO-Monitor)
            </g:link>
        </h4>
    </div>
</g:if>
<g:else>
    <div class="withBorder" style="border: 1px solid black;">
        <h4>
            <g:link absolute="true" controller="subscription" action="show" id="${subscription.id}">
                ${subscription.name}
            </g:link>
        </h4>
        <dl>
            <dt>${message(code: 'default.status.label')}</dt>
            <dd>${subscription.status.getI10n('value')}</dd>
        </dl>
        <dl>
            <dt>${message(code: 'subscription.kind.label')}</dt>
            <dd>${subscription.kind?.getI10n('value')}</dd>
        </dl>
        <dl>
            <dt>${message(code: 'subscription.form.label')}</dt>
            <dd>${subscription.form?.getI10n('value')}</dd>
        </dl>
        <dl>
            <dt>${message(code: 'subscription.resource.label')}</dt>
            <dd>${subscription.resource?.getI10n('value')}</dd>
        </dl>
        <dl>
            <dt>${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
            <dd>${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</dd>
        </dl>
        <dl>
            <dt>
                <g:message code="default.identifiers.label"/>
            </dt>
            <dd>
                <ul>
                    <g:each in="${subscription.ids?.sort { it.ns.ns }}"
                            var="id">
                        <li>${id.ns.ns}: ${id.value}</li>
                    </g:each>
                </ul>
            </dd>
        </dl>

        <g:if test="${visibleProviders}">

            <br/>
            <br/>
            <g:each in="${visibleProviders}" var="role">
                    <h2>
                        ${message(code: 'default.provider.label')}
                    </h2>
                    <g:link absolute="true" controller="provider" action="show"
                            id="${role.provider.id}">${role.provider.name}</g:link>
            </g:each>

            <br/>
            <br/>
        </g:if>
        <g:if test="${visibleVendors}">

            <br/>
            <br/>
            <g:each in="${visibleVendors}" var="role">
                    <h2>
                        ${message(code: 'default.agency.label')}
                    </h2>
                    <g:link absolute="true" controller="vendor" action="show"
                            id="${role.vendor.id}">${role.vendor.name}</g:link>
            </g:each>

            <br/>
            <br/>
        </g:if>

        %{-- EXPORT PROBLEM @ laser:render in call stack - ERMS-5437 --}%
        <g:render template="/subscription/export/propertiesPDF" model="${[
                subscription: subscription, calledFromSurvey: true, contextOrg: contextOrg
        ]}"/>

    </div>

    <br>
    <br>

    <h2>
        <g:message code="license.plural"/>
    </h2>

        <g:if test="${links && links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}">
            <div class="withBorder" style="border: 1px solid black;">
            <table>
                <g:each in="${links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}"
                        var="link">
                    <tr><g:set var="pair" value="${link.getOther(subscription)}"/>
                        <th>${pair.licenseCategory?.getI10n("value")}</th>
                        <td>
                            <g:link absolute="true" controller="license" action="show" id="${pair.id}">
                                ${pair.reference} (${pair.status.getI10n("value")})
                            </g:link>
                            <g:formatDate date="${pair.startDate}"
                                          format="${message(code: 'default.date.format.notime')}"/>-<g:formatDate
                                date="${pair.endDate}"
                                format="${message(code: 'default.date.format.notime')}"/><br/>
                            <g:set var="comment"
                                   value="${DocContext.findByLink(link)}"/>
                            <g:if test="${comment}">
                                <em>${comment.owner.content}</em>
                            </g:if>
                        </td>
                    </tr>
                %{--<g:if test="${pair.propertySet}">
                    <tr>
                        <td colspan="3"><div id="${link.id}Properties"></div></td>
                    </tr>
                </g:if>--}%
                </g:each>
            </table>
            </div>
        </g:if>

    <br>
    <br>
</g:else>


<g:if test="${subscription && subscription.packages}">
    <h2>
        <g:message code="package.plural"/>
    </h2>
        <%
            List packages = []
            ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
            subscription.packages.each { SubscriptionPackage subscriptionPackage ->
                Map packageInfos = [:]

                packageInfos.packageInstance = subscriptionPackage.pkg

                Map queryResult = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: subscriptionPackage.pkg.gokbId])
                if (queryResult) {
                    List records = queryResult.result
                    packageInfos.packageInstanceRecord = records ? records[0] : [:]
                }
                packages << packageInfos
            }
        %>
        <g:if test="${packages.size() > 0}">
            <div class="withBorder" style="border: 1px solid black;">
            <g:each in="${packages}" var="pkgInfo">
                <h4>
                    <g:link absolute="true" controller="package" action="show"
                            id="${pkgInfo.packageInstance.id}">${pkgInfo.packageInstance.name}</g:link>
                </h4>
                <table>
                    <tbody>
                    <tr>
                        <td>
                            <dl>
                                <dt>${message(code: 'default.status.label')}</dt>
                                <dd>${pkgInfo.packageInstance.packageStatus?.getI10n('value')}</dd>
                            </dl>
                            <g:if test="${pkgInfo.packageInstanceRecord}">
                                <dl>
                                    <dt>${message(code: 'package.show.altname')}</dt>
                                    <dd>
                                        <ul>
                                            <g:each in="${pkgInfo.packageInstanceRecord.altname}"
                                                    var="altname">
                                                <li>${altname}</li>
                                            </g:each>
                                        </ul>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.curatoryGroup.label')}</dt>
                                    <dd>
                                        <div class="ui bulleted list">
                                            <g:each in="${pkgInfo.packageInstanceRecord.curatoryGroups}"
                                                    var="curatoryGroup">
                                                <g:link
                                                        url="${editUrl}resource/show/${curatoryGroup.curatoryGroup}">${curatoryGroup.name} ${curatoryGroup.type ? "(${curatoryGroup.type})" : ""}</g:link>
                                            </g:each>
                                        </div>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.lastUpdated.label')}</dt>
                                    <dd>
                                        <g:if test="${pkgInfo.packageInstanceRecord.lastUpdatedDisplay}">
                                            <g:formatDate formatName="default.date.format.notime"
                                                          date="${DateUtils.parseDateGeneric(pkgInfo.packageInstanceRecord.lastUpdatedDisplay)}"/>
                                        </g:if>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.source.automaticUpdates')}</dt>
                                    <dd>
                                        <g:if test="${pkgInfo.packageInstanceRecord.source?.automaticUpdates}">
                                            <g:message code="package.index.result.automaticUpdates"/>
                                            (${pkgInfo.packageInstanceRecord.source.frequency})
                                        </g:if>
                                        <g:else>
                                            <g:message code="package.index.result.noAutomaticUpdates"/>
                                        </g:else>
                                    </dd>
                                </dl>
                            </g:if>
                            <dl>
                                <dt>${message(code: 'package.file')}</dt>
                                <dd>${pkgInfo.packageInstance.file?.getI10n("value")}</dd>
                            </dl>
                        </td>
                        <td>
                            <dl>
                                <dt>${message(code: 'package.contentType.label')}</dt>
                                <dd>${pkgInfo.packageInstance.contentType?.getI10n("value")}</dd>
                            </dl>
                            <g:if test="${pkgInfo.packageInstanceRecord}">
                                <dl>
                                    <dt>${message(code: 'package.breakable')}</dt>
                                    <dd>${pkgInfo.packageInstanceRecord.breakable ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.breakable, RDConstants.PACKAGE_BREAKABLE).getI10n("value") : message(code: 'default.not.available')}</dd>
                                </dl>
                            <%--<dl>
                                <dt>${message(code: 'package.consistent')}</dt>
                                <dd>${pkgInfo.packageInstanceRecord.consistent ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.consistent, RDConstants.PACKAGE_CONSISTENT).getI10n("value") : message(code: 'default.not.available')}</dd>
                            </dl>--%>
                                <dl>
                                    <dt>${message(code: 'package.scope.label')}</dt>
                                    <dd>
                                        ${pkgInfo.packageInstanceRecord.scope ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.scope, RDConstants.PACKAGE_SCOPE).getI10n("value") : message(code: 'default.not.available')}
                                        <g:if test="${pkgInfo.packageInstanceRecord.scope == RDStore.PACKAGE_SCOPE_NATIONAL.value}">
                                            <dl>
                                                <dt>${message(code: 'package.nationalRange.label')}</dt>
                                                <g:if test="${pkgInfo.packageInstanceRecord.nationalRanges}">
                                                    <dd>
                                                        <ul>
                                                            <g:each in="${pkgInfo.packageInstanceRecord.nationalRanges}"
                                                                    var="nr">
                                                                <li>${RefdataValue.getByValueAndCategory(nr.value, RDConstants.COUNTRY) ? RefdataValue.getByValueAndCategory(nr.value, RDConstants.COUNTRY).getI10n('value') : nr}</li>
                                                            </g:each>
                                                        </ul>
                                                    </dd>
                                                </g:if>
                                            </dl>
                                            <dl>
                                                <dt>${message(code: 'package.regionalRange.label')}</dt>
                                                <g:if test="${pkgInfo.packageInstanceRecord.regionalRanges}">
                                                    <dd>
                                                        <ul>
                                                            <g:each in="${pkgInfo.packageInstanceRecord.regionalRanges}"
                                                                    var="rr">
                                                                <li>${RefdataValue.getByValueAndCategory(rr.value, RDConstants.REGIONS_DE) ? RefdataValue.getByValueAndCategory(rr.value, RDConstants.REGIONS_DE).getI10n('value') : rr}</li>
                                                            </g:each>
                                                        </ul>
                                                    </dd>
                                                </g:if>
                                            </dl>
                                        </g:if>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.paymentType.label')}</dt>
                                    <dd>${RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.paymentType, RDConstants.PAYMENT_TYPE) ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.paymentType, RDConstants.PAYMENT_TYPE).getI10n("value") : pkgInfo.packageInstanceRecord.paymentType}</dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.openAccess.label')}</dt>
                                    <dd>${pkgInfo.packageInstanceRecord.openAccess ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.openAccess, RDConstants.LICENSE_OA_TYPE)?.getI10n("value") : RDStore.LICENSE_OA_TYPE_EMPTY.getI10n("value")}</dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.ddc.label')}</dt>
                                    <dd>
                                        <ul>
                                            <g:each in="${pkgInfo.packageInstanceRecord.ddcs}" var="ddc">
                                                <li>${RefdataValue.getByValueAndCategory(ddc.value, RDConstants.DDC) ? RefdataValue.getByValueAndCategory(ddc.value, RDConstants.DDC).getI10n('value') : message(code: 'package.ddc.invalid')}</li>
                                            </g:each>
                                        </ul>
                                    </dd>
                                </dl>
                            </g:if>
                        </td>
                    </tr>
                    </tbody>
                </table>

                <dl>
                    <dt>${message(code: 'platform.label')}</dt>
                    <dd>
                        <g:if test="${pkgInfo.packageInstance.nominalPlatform}">
                            <g:link absolute="true" controller="platform" action="show"
                                    id="${pkgInfo.packageInstance.nominalPlatform.id}">${pkgInfo.packageInstance.nominalPlatform.name}</g:link>

                            <g:if test="${pkgInfo.packageInstance.nominalPlatform.primaryUrl}">
                                (<g:link
                                    uri="${pkgInfo.packageInstance.nominalPlatform.primaryUrl?.startsWith('http') ? pkgInfo.packageInstance.nominalPlatform.primaryUrl : 'http://' + pkgInfo.packageInstance.nominalPlatform.primaryUrl}">${pkgInfo.packageInstance.nominalPlatform.primaryUrl}</g:link>)
                            </g:if>
                        </g:if>
                    </dd>
                </dl>
            </g:each>
        </div>
        </g:if>
</g:if>

<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_TITLE_SELECTION.id]}">
    <g:set var="costItemSurveys"
           value="${surveyOrg ? CostItem.findAllBySurveyOrg(surveyOrg) : null}"/>

    <% Set<RefdataValue> costItemElementsNotInSurveyCostItems = [] %>

    <g:if test="${surveyInfo.owner.id != institution.id && ((costItemSums && costItemSums.subscrCosts) || costItemSurveys)}">
        <g:set var="showCostItemSurvey" value="${true}"/>

        <h2>
            <g:message code="surveyConfigsInfo.costItems"/>
        </h2>

        <div class="ui card la-time-card">
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
                        <th class="la-smaller-table-head"><g:message code="financials.invoice_total"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.newCosts.taxTypeAndRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.newCosts.totalAmount"/></th>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.invoice_total"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.newCosts.taxTypeAndRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.newCosts.totalAmount"/></th>

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

                                    ${costItem.billingCurrency?.getI10n('value')}
                                </td>
                                <td>
                                    <g:if test="${costItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                        ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                    </g:if>
                                    <g:elseif test="${costItem.taxKey}">
                                        ${costItem.taxKey.taxType?.getI10n("value") + " (" + costItem.taxKey.taxRate + "%)"}
                                    </g:elseif>
                                </td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItem.costInBillingCurrencyAfterTax}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${costItem.billingCurrency?.getI10n('value')}

                                    <g:if test="${costItem.startDate || costItem.endDate}">
                                        <br/>(${formatDate(date: costItem.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItem.endDate, format: message(code: 'default.date.format.notime'))})
                                    </g:if>
                                </td>

                                <g:set var="surveyCostItems" scope="request"
                                       value="${CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndCostItemElement(surveyOrg, RDStore.COST_ITEM_DELETED, costItem.costItemElement)}"/>

                                <% costItemElementsNotInSurveyCostItems <<  costItem.costItemElement %>
                                <g:if test="${surveyCostItems}">
                                    <g:each in="${surveyCostItems}"
                                            var="costItemSurvey">
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

                                            ${costItemSurvey.billingCurrency?.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:if test="${costItemSurvey.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                                ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                            </g:if>
                                            <g:elseif test="${costItemSurvey.taxKey}">
                                                ${costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)"}
                                            </g:elseif>
                                        </td>
                                        <td>
                                            <strong>
                                                <g:formatNumber
                                                        number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                                        minFractionDigits="2" maxFractionDigits="2"
                                                        type="number"/>
                                            </strong>

                                            ${costItemSurvey.billingCurrency?.getI10n('value')}


                                            <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                                <br/>(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                                            </g:if>

                                            <g:if test="${costItemSurvey.costDescription}">
                                                <br/>

                                                <div class="ui icon la-popup-tooltip la-delay"
                                                     data-position="right center"
                                                     data-variation="tiny"
                                                     data-content="${costItemSurvey.costDescription}">
                                                    <i class="question small circular inverted icon"></i>
                                                </div>
                                            </g:if>
                                        </td>
                                        <td>
                                            <g:set var="oldCostItem"
                                                   value="${costItem.costInBillingCurrency ?: 0.0}"/>

                                            <g:set var="newCostItem"
                                                   value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                            <strong><g:formatNumber
                                                    number="${(newCostItem - oldCostItem)}"
                                                    minFractionDigits="2" maxFractionDigits="2" type="number"/>
                                                <br/>
                                                (<g:formatNumber
                                                        number="${((newCostItem - oldCostItem) / oldCostItem) * 100}"
                                                        minFractionDigits="2"
                                                        maxFractionDigits="2" type="number"/>%)</strong>
                                        </td>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </g:else>
                            </tr>
                        </g:each>
                    </g:if>

                    <g:set var="costItemsWithoutSubCostItems"
                           value="${surveyOrg && costItemElementsNotInSurveyCostItems ? CostItem.findAllBySurveyOrgAndCostItemElementNotInList(surveyOrg, costItemElementsNotInSurveyCostItems) : []}"/>
                    <g:if test="${costItemsWithoutSubCostItems}">
                        <g:each in="${costItemsWithoutSubCostItems}" var="costItemSurvey">
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

                                    ${costItemSurvey.billingCurrency?.getI10n('value')}
                                </td>
                                <td>
                                    <g:if test="${costItemSurvey.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                        ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                    </g:if>
                                    <g:elseif test="${costItemSurvey.taxKey}">
                                        ${costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)"}
                                    </g:elseif>
                                </td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${costItemSurvey.billingCurrency?.getI10n('value')}

                                    <g:set var="newCostItem"
                                           value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                    <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                        <br/>(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                                    </g:if>

                                    <g:if test="${costItemSurvey.costDescription}">
                                        <br/>

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
                        </g:each>
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
                        <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.ownCosts')} </h2>
                        <laser:render template="/subscription/financials" model="[data: costItemSums.ownCosts]"/>
                    </g:if>
                </g:if>
                <g:if test="${costItemSums.consCosts}">
                    <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.consCosts')} ${message(code: 'surveyCostItem.info')}</h2>
                    <laser:render template="/subscription/financials" model="[data: costItemSums.consCosts]"/>
                </g:if>
            </div>
        </div>
    </g:if>
</g:if>

<g:if test="${surveyResults}">

    <h2><g:message code="surveyConfigsInfo.properties"/>
    (${surveyResults.size()})
    </h2>

    <table>
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'surveyResult.result')}</th>
            <th>
                <g:if test="${ownerView}">
                    ${message(code: 'surveyResult.participantComment')}
                </g:if>
                <g:else>
                    ${message(code: 'surveyResult.commentParticipant')}
                </g:else>
            </th>
            <th>
                <g:if test="${ownerView}">
                    ${message(code: 'surveyResult.commentOnlyForOwner')}
                </g:if>
                <g:else>
                    ${message(code: 'surveyResult.commentOnlyForParticipant')}
                </g:else>
            </th>
        </tr>
        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">
            <tr>
                <td>
                    ${i + 1}
                </td>
                <td>
                    ${surveyResult.type.getI10n('name')}

                    <g:set var="surveyConfigProperties"
                           value="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyResult.surveyConfig, surveyResult.type)}"/>
                    <g:if test="${surveyConfigProperties && surveyConfigProperties.mandatoryProperty}">
                        *
                    </g:if>
                </td>
                <td>
                    ${PropertyDefinition.getLocalizedValue(surveyResult.type.type)}
                    <g:if test="${surveyResult.type.isRefdataValueType()}">
                        <g:set var="refdataValues" value="${[]}"/>
                        <g:each in="${RefdataCategory.getAllRefdataValues(surveyResult.type.refdataCategory)}"
                                var="refdataValue">
                            <g:set var="refdataValues"
                                   value="${refdataValues + refdataValue.getI10n('value')}"/>
                        </g:each>
                        <br/>
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
                            <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                             field="intValue"/>
                        </g:if>
                        <g:elseif test="${surveyResult.type.isStringType()}">
                            <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                             field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isBigDecimalType()}">
                            <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                             field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isDateType()}">
                            <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="date"
                                             field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isURLType()}">
                            <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="url"
                                             field="urlValue"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${surveyResult.urlValue}">
                                <ui:linkWithIcon href="${surveyResult.urlValue}"/>
                            </g:if>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isRefdataValueType()}">
                            <ui:xEditableRefData overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                                    field="refValue"
                                                    config="${surveyResult.type.refdataCategory}"/>
                        </g:elseif>
                    </td>
                    <td>
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="textarea"
                                         field="comment"/>
                    </td>
                    <td>
                        <g:if test="${ownerView}">
                            <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="textarea"
                                             field="ownerComment"/>
                        </g:if>
                        <g:else>
                            <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="textarea"
                                             field="participantComment"/>
                        </g:else>
                    </td>
                </g:else>

            </tr>
        </g:each>
    </table>
    <br/>
</g:if>


