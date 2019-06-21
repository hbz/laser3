<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb controller="myInstitution" action="surveyInfos" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    <semui:crumb message="survey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>
${message(code: 'survey.label')} - ${surveyInfo.name}

<g:if test="${surveyInfo.startDate || surveyInfo.endDate}"></g:if>
(<g:formatDate formatName="default.date.format.notime"
               date="${surveyInfo.startDate}"/>
-
<g:formatDate formatName="default.date.format.notime"
              date="${surveyInfo.endDate}"/>)
</h1>


<g:if test="${navigation}">
    <div class="ui center aligned grid">
        <div class='ui big label la-annual-rings'>

            <g:if test="${navigation?.prev}">
                <g:link controller="myInstitution" action="surveyConfigsInfo" id="${surveyInfo?.id}"
                        params="[surveyConfigID: navigation?.prev?.id]" class="item" title="${message(code: 'surveyConfigsInfo.prevSurveyConfig')}">
                    <i class='arrow left icon'></i>
                </g:link>
            </g:if>
            <g:message code="surveyConfigsInfo.totalSurveyConfig" args="[surveyConfig?.configOrder, navigation?.total]"/>
            <g:if test="${navigation?.next}">
                <g:link controller="myInstitution" action="surveyConfigsInfo" id="${surveyInfo?.id}"
                        params="[surveyConfigID: navigation?.next?.id]" class="item" title="${message(code: 'surveyConfigsInfo.nextSurveyConfig')}">
                    <i class='arrow right icon'></i>
                </g:link>
            </g:if>
        </div>
    </div>
</g:if>

<br>

<semui:messages data="${flash}"/>

<br>

<g:if test="${ownerId}">
    <g:set var="choosenOrg" value="${com.k_int.kbplus.Org.findById(ownerId)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <semui:form>
        <h3><g:message code="surveyInfo.owner.label"/>:</h3>

        <table class="ui table la-table la-table-small">
            <tbody>
            <tr>
                <td>
                    <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                    ${choosenOrg?.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${choosenOrgCPAs}">
                        <g:set var="oldEditable" value="${editable}" />
                        <g:set var="editable" value="${false}" scope="request"/>
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <g:render template="/templates/cpa/person_details" model="${[person: gcp, tmplHideLinkToAddressbook: true]}" />
                        </g:each>
                        <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>

                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
    </semui:form>
</g:if>

<br>

<g:if test="${surveyConfig?.type == 'Subscription'}">
    <h2 class="ui icon header"><semui:headerIcon/>
    <g:link controller="subscription" action="show" id="${subscriptionInstance?.id}">
        ${subscriptionInstance?.name}
    </g:link>
    </h2>
    <semui:auditButton auditable="[subscriptionInstance, 'name']"/>

</g:if>
<g:else>
    <h2><g:message code="surveyConfigsInfo.surveyConfig.info" args="[surveyConfig?.getConfigNameShort()]"/></h2>
</g:else>

<div class="ui stackable grid">
    <div class="twelve wide column">
        <div class="la-inline-lists">

            <div class="ui card">
                <div class="content">
                    <g:if test="${surveyConfig?.type == 'Subscription'}">
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.details.status')}</dt>
                            <dd>${subscriptionInstance?.status?.getI10n('value')}</dd>
                            <dd><semui:auditButton auditable="[subscriptionInstance, 'status']"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.details.type')}</dt>
                            <dd>${subscriptionInstance?.type?.getI10n('value')}</dd>
                            <dd><semui:auditButton auditable="[subscriptionInstance, 'type']"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                            <dd>${subscriptionInstance?.form?.getI10n('value')}</dd>
                            <dd><semui:auditButton auditable="[subscriptionInstance, 'form']"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                            <dd>${subscriptionInstance?.resource?.getI10n('value')}</dd>
                            <dd><semui:auditButton auditable="[subscriptionInstance, 'resource']"/></dd>
                        </dl>
                        <g:if test="${subscriptionInstance?.instanceOf && (contextOrg?.id == subscriptionInstance?.getConsortia()?.id)}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isInstanceOfSub.label')}</dt>
                                <dd>
                                    <g:link controller="subscription" action="show"
                                            id="${subscriptionInstance?.instanceOf.id}">${subscriptionInstance?.instanceOf}</g:link>
                                </dd>
                            </dl>

                            <dl>
                                <dt class="control-label">
                                    ${message(code: 'license.details.linktoLicense.pendingChange', default: 'Automatically Accept Changes?')}
                                </dt>
                                <dd>
                                    ${subscriptionInstance?.isSlaved?.getI10n('value')}
                                </dd>
                            </dl>
                        </g:if>
                        <dl>
                            <dt class="control-label">
                                <g:message code="default.identifiers.label"/>
                            </dt>
                            <dd>
                                <g:each in="${subscriptionInstance?.ids?.sort { it?.identifier?.ns?.ns }}"
                                        var="id">
                                    <span class="ui small teal image label">
                                        ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                                    </span>
                                </g:each>
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div>
            <g:if test="${surveyConfig?.type == 'Subscription'}">

                <g:if test="${subscriptionInstance?.packages}">
                    <div class="ui card la-js-hideable">
                        <div class="content">
                            <table class="ui three column la-selectable table">
                                <g:each in="${subscriptionInstance?.packages.sort { it.pkg.name }}" var="sp">
                                    <tr>
                                        <th scope="row"
                                            class="control-label la-js-dont-hide-this-card">${message(code: 'subscription.packages.label')}</th>
                                        <td>
                                            <g:link controller="package" action="show"
                                                    id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>

                                            <g:if test="${sp.pkg?.contentProvider}">
                                                (${sp.pkg?.contentProvider?.name})
                                            </g:if>
                                        </td>
                                        <td class="right aligned">
                                        </td>

                                    </tr>
                                </g:each>
                            </table>

                        </div><!-- .content -->
                    </div>
                </g:if>

                <div class="ui card la-js-hideable">
                    <div class="content">

                        <g:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks    : visibleOrgRelations,
                                            roleObject   : subscriptionInstance,
                                            roleRespValue: 'Specific subscription editor',
                                            editmode     : false
                                  ]}"/>

                    </div>
                </div>

                <div class="ui card la-js-hideable">
                    <div class="content">
                        <g:set var="derivedPropDefGroups"
                               value="${subscriptionInstance?.owner?.getCalculatedPropDefGroups(contextService.getOrg())}"/>

                        <div class="ui la-vertical buttons">
                            <g:if test="${derivedPropDefGroups?.global || derivedPropDefGroups?.local || derivedPropDefGroups?.member || derivedPropDefGroups?.fallback}">

                                <button id="derived-license-properties-toggle"
                                        class="ui button la-js-dont-hide-button">Vertragsmerkmale anzeigen</button>
                                <script>
                                    $('#derived-license-properties-toggle').on('click', function () {
                                        $('#derived-license-properties').toggleClass('hidden')
                                        if ($('#derived-license-properties').hasClass('hidden')) {
                                            $(this).text('Vertragsmerkmale anzeigen')
                                        } else {
                                            $(this).text('Vertragsmerkmale ausblenden')
                                        }
                                    })
                                </script>

                            </g:if>

                            <button id="subscription-properties-toggle"
                                    class="ui button la-js-dont-hide-button">Lizenzsmerkmale anzeigen</button>
                            <script>
                                $('#subscription-properties-toggle').on('click', function () {
                                    $('#subscription-properties').toggleClass('hidden')
                                    if ($('#subscription-properties').hasClass('hidden')) {
                                        $(this).text('Lizenzsmerkmale anzeigen')
                                    } else {
                                        $(this).text('Lizenzsmerkmale ausblenden')
                                    }
                                })
                            </script>
                        </div>

                    </div><!-- .content -->
                </div>

                <g:if test="${derivedPropDefGroups?.global || derivedPropDefGroups?.local || derivedPropDefGroups?.member || derivedPropDefGroups?.fallback}">
                    <div id="derived-license-properties" class="hidden" style="margin: 1em 0">

                        <g:render template="/subscription/licProp" model="${[
                                license             : subscriptionInstance?.owner,
                                derivedPropDefGroups: derivedPropDefGroups
                        ]}"/>
                    </div>
                </g:if>


                <div id="subscription-properties" class="hidden" style="margin: 1em 0">
                    <g:set var="editable" value="${false}" scope="page"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:render template="/subscription/properties" model="${[
                            subscriptionInstance: subscriptionInstance,
                            authorizedOrgs      : authorizedOrgs
                    ]}"/>


                    <g:set var="editable" value="${true}" scope="page"/>

                </div>

            </g:if>


            <div class="la-inline-lists">
                <div class="ui stackable cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <div class="header"><g:message code="surveyConfigsInfo.comment"/></div>
                        </div>

                        <div class="content">
                            <g:if test="${surveyConfig?.comment}">
                                ${surveyConfig?.comment}
                            </g:if><g:else>
                                <g:message code="surveyConfigsInfo.comment.noComment"/>
                            </g:else>
                        </div>
                    </div>
                </div>
            </div>

            <div class="la-inline-lists">
                <div class="ui stackable cards">
                    <div class="ui card la-time-card">

                        <div class="content">
                            <div class="header"><g:message code="surveyConfigsInfo.costItems"/></div>
                        </div>

                        <div class="content">

                            <g:set var="surveyOrg"
                                   value="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>
                            <g:set var="costItem"
                                   value="${com.k_int.kbplus.CostItem.findBySurveyOrg(com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution))}"/>
                            <g:set var="costItemsSub"
                                   value="${subscriptionInstance?.costItems.findAll {
                                       it?.costItemElement?.id == costItem?.costItemElement?.id
                                   }}"/>


                            <table class="ui celled la-table-small la-table-inCard table">
                                <thead>
                                <tr>
                                    <th>
                                        <g:message code="surveyConfigsInfo.oldPrice"/>
                                    </th>
                                    <th>
                                        <g:message code="surveyConfigsInfo.newPrice"/>
                                    </th>
                                </tr>
                                </thead>
                                <tbody class="top aligned">
                                <tr>
                                    <td>
                                        <g:each in="${costItemsSub}" var="costItemSub">
                                            ${costItemSub?.costItemElement?.getI10n('value')}
                                            <b><g:formatNumber
                                                    number="${consCostTransfer ? costItemSub?.costInBillingCurrencyAfterTax : costItemSub?.costInBillingCurrency}"
                                                    minFractionDigits="2" maxFractionDigits="2" type="number"/></b>

                                            ${(costItemSub?.billingCurrency?.getI10n('value').split('-')).first()}

                                            <g:if test="${costItemSub?.startDate || costItemSub?.endDate}">
                                                (${formatDate(date: costItemSub?.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSub?.endDate, format: message(code: 'default.date.format.notime'))})
                                            </g:if>

                                        </g:each>
                                    </td>
                                    <td>
                                        <g:if test="${costItem}">
                                            ${costItem?.costItemElement?.getI10n('value')}
                                            <b><g:formatNumber
                                                    number="${consCostTransfer ? costItem?.costInBillingCurrencyAfterTax : costItem?.costInBillingCurrency}"
                                                    minFractionDigits="2" maxFractionDigits="2" type="number"/></b>

                                            <br>${(costItem?.billingCurrency?.getI10n('value').split('-')).first()}

                                            <g:if test="${costItem?.costDescription}">
                                                <br>

                                                <div class="ui icon" data-position="right center" data-variation="tiny"
                                                     data-tooltip="${costItem?.costDescription}">
                                                    <i class="question small circular inverted icon"></i>
                                                </div>
                                            </g:if>

                                        </g:if>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <aside class="four wide column la-sidekick">
        <div id="container-documents">
            <g:render template="/survey/cardDocuments"
                      model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
        </div>
    </aside><!-- .four -->

</div><!-- .grid -->




<semui:form>
    <h3><g:message code="surveyConfigsInfo.properties"/>
    <semui:totalNumber
            total="${surveyResults?.size()}"/>
    </h3>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.label')}</th>
            <th>${message(code: 'surveyProperty.type.label')}</th>
            <th>${message(code: 'surveyResult.result')}</th>
            <th>${message(code: 'surveyResult.commentParticipant')}</th>
        </tr>
        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    ${surveyResult?.type?.getI10n('name')}

                    <g:if test="${surveyResult?.type?.getI10n('explain')}">
                        <span class="la-long-tooltip" data-position="right center" data-variation="tiny"
                              data-tooltip="${surveyResult?.type?.getI10n('explain')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                </td>
                <td>
                    ${surveyResult?.type?.getLocalizedType()}

                </td>
                <g:set var="surveyOrg"
                       value="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyResult?.surveyConfig, institution)}"/>

                <g:if test="${!surveyOrg?.checkPerennialTerm()}">

                    <td>
                        <g:if test="${surveyResult?.type?.type == Integer.toString()}">
                            <semui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${surveyResult?.type?.type == String.toString()}">
                            <semui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult?.type?.type == BigDecimal.toString()}">
                            <semui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult?.type?.type == Date.toString()}">
                            <semui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult?.type?.type == URL.toString()}">
                            <semui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                             overwriteEditable="${overwriteEditable}"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${surveyResult.value}">
                                <semui:linkIcon/>
                            </g:if>
                        </g:elseif>
                        <g:elseif test="${surveyResult?.type?.type == RefdataValue.toString()}">
                            <semui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                    config="${surveyResult.type?.refdataCategory}"/>
                        </g:elseif>
                    </td>
                    <td>
                        <semui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                    </td>
                </g:if>
                <g:else>
                    <td>
                        <g:message code="surveyOrg.perennialTerm.available"/>
                    </td>
                    <td>

                    </td>
                </g:else>

            </tr>
        </g:each>
    </table>

</semui:form>

</div>

</body>
</html>
