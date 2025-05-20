<%@ page import="de.laser.remote.Wekb; de.laser.utils.DateUtils; de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.properties.PropertyDefinition; de.laser.Subscription; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.SubscriptionPackage;" %>
<g:set bean="genericOIDService" var="genericOIDService"/>
<g:set bean="gokbService" var="gokbService"/>
<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant)}"/>

<g:if test="${ownerView && surveyOrg}">
    <dl>
        <dt>
            ${message(code: 'surveyOrg.ownerComment.label', args: [participant.sortname])}
        </dt>
        <dd>
            ${surveyOrg.ownerComment}
        </dd>
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
                        ${message(code: 'provider.label')}
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
                        ${message(code: 'vendor.label')}
                    </h2>
                    <g:link absolute="true" controller="vendor" action="show"
                            id="${role.vendor.id}">${role.vendor.name}</g:link>
            </g:each>

            <br/>
            <br/>
        </g:if>

        %{-- EXPORT PROBLEM @ laser:render in call stack - ERMS-5437 --}%
        <g:render template="/subscription/export/propertiesPDF" model="${[
                subscription: subscription, calledFromSurvey: true
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
            subscription.packages.each { SubscriptionPackage subscriptionPackage ->
                Map packageInfos = [:]

                packageInfos.packageInstance = subscriptionPackage.pkg

                Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: subscriptionPackage.pkg.gokbId])
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
                                                        url="${baseUrl}/resource/show/${curatoryGroup.curatoryGroup}">${curatoryGroup.name} ${curatoryGroup.type ? "(${curatoryGroup.type})" : ""}</g:link>
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

<laser:render template="/templates/survey/costsWithSub"/>

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
                            <g:if test="${refdataValue.getI10n('value')}">
                                <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                            </g:if>
                        </g:each>
                        <br/>
                        (${refdataValues.join('/')})
                    </g:if>
                </td>
                <td>
                    <g:if test="${surveyResult.type.isLongType()}">
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                      field="longValue"/>
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
            </tr>
        </g:each>
    </table>
    <br/>
</g:if>


