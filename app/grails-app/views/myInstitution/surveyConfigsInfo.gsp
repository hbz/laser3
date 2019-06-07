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
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <g:render template="/templates/cpa/person_details"
                                      model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                        </g:each>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
    </semui:form>
</g:if>

<br>


<div class="la-inline-lists">
    <div class="ui two stackable cards">
        <div class="ui card la-time-card">
            <div class="content">
                <dl>
                    <dt class="control-label">${message(code: 'subscription.label')}</dt>
                    <dd>
                        <g:link controller="subscription" action="show" id="${subscriptionInstance?.id}">
                            ${subscriptionInstance?.name}
                        </g:link>
                    </dd>

                </dl>
                <dl>
                    <dt class="control-label">${message(code: 'subscription.startDate.label')}</dt>
                    <dd><g:formatDate formatName="default.date.format.notime"
                                      date="${subscriptionInstance?.startDate}"/></dd>

                </dl>
                <dl>
                    <dt class="control-label">${message(code: 'subscription.endDate.label')}</dt>
                    <dd><g:formatDate formatName="default.date.format.notime"
                                      date="${subscriptionInstance?.endDate}"/></dd>
                </dl>
            </div>
        </div>

        <div class="ui card">
            <div class="content">
                <dl>
                    <dt class="control-label">${message(code: 'subscription.details.status')}</dt>
                    <dd>${subscriptionInstance?.status?.getI10n('value')}</dd>
                    <dd><semui:auditButton auditable="[subscriptionInstance, 'status']"/></dd>
                </dl>
                <dl>
                    <dt class="control-label">${message(code: 'subscription.details.type')}</dt>
                    <dd>
                        ${subscriptionInstance?.type?.getI10n('value')}
                    </dd>
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
            </div>
        </div>
    </div>
</div>

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
                       value="${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(institution).costItems.findAll {
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
                                <g:formatNumber
                                        number="${consCostTransfer ? costItemSub?.costInBillingCurrencyAfterTax : costItemSub?.costInBillingCurrency}"
                                        minFractionDigits="2" maxFractionDigits="2" type="number"/>

                                ${(costItemSub?.billingCurrency?.getI10n('value').split('-')).first()}

                            </g:each>
                        </td>
                        <td>
                            <g:if test="${costItem}">
                                ${costItem?.costItemElement?.getI10n('value')}
                                <g:formatNumber
                                        number="${consCostTransfer ? costItem?.costInBillingCurrencyAfterTax : costItem?.costInBillingCurrency}"
                                        minFractionDigits="2" maxFractionDigits="2" type="number"/>

                                ${(costItem?.billingCurrency?.getI10n('value').split('-')).first()}

                                <g:if test="${costItem?.costDescription}">
                                    <br>

                                    <div class="ui icon" data-tooltip="${costItem?.costDescription}">
                                        <i class="info small circular inverted icon"></i>
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

<div class="la-inline-lists">
    <div class="ui stackable cards">
        <div class="ui card la-time-card">
            <div class="content">
                <div class="header"><g:message code="surveyConfigsInfo.docs"/></div>
            </div>

            <div class="content">
                <table class="ui celled la-table table license-documents">
                    <thead>
                    <tr>
                        <th></th>
                        <th>${message(code: 'surveyConfigDocs.docs.table.title', default: 'Title')}</th>
                        <th>${message(code: 'surveyConfigDocs.docs.table.fileName', default: 'File Name')}</th>
                        <th>${message(code: 'surveyConfigDocs.docs.table.type', default: 'Type')}</th>
                        <th>${message(code: 'default.actions', default: 'Actions')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${surveyConfig?.getCurrentDocs()}" var="docctx" status="i">
                        <tr>
                            <td>${i + 1}</td>
                            <td>
                                ${docctx.owner.title}
                            </td>
                            <td>
                                ${docctx.owner.filename}
                            </td>
                            <td>
                                ${docctx.owner?.type?.getI10n('value')}
                            </td>

                            <td class="x">
                                <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">

                                    <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon button"><i
                                            class="download icon"></i></g:link>
                                </g:if>
                            </td>
                        </tr>

                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>


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
                        <span class="la-long-tooltip" data-position="right center" data-variation="tiny" data-tooltip="${surveyResult?.type?.getI10n('explain')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                </td>
                <td>
                    ${surveyResult?.type?.getLocalizedType()}

                </td>
                    <g:set var="surveyOrg" value="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyResult?.surveyConfig, institution)}"/>

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
