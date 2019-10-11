<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'issueEntitlementsSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="issueEntitlementsSurvey.label" class="active"/>
</semui:breadcrumbs>


%{--<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="surveyInfos"
                    params="${params + [exportXLS: true]}">${message(code: 'survey.exportSurvey')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</semui:controlButtons>--}%



<h1 class="ui left aligned icon header"><semui:headerIcon/>
${message(code: 'issueEntitlementsSurvey.label')} - ${surveyInfo.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<br>

<semui:messages data="${flash}"/>

<br>

<g:if test="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)?.finishDate != null}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message code="renewEntitlementsWithSurvey.finish.info"/>.
            </p>
        </div>
    </div>
</g:if>

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
                        <g:set var="oldEditable" value="${editable}"/>
                        <g:set var="editable" value="${false}" scope="request"/>
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <g:render template="/templates/cpa/person_details"
                                      model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
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

<g:if test="${subscriptionInstance}">
    <h2 class="ui icon header"><semui:headerIcon/>
    <g:link controller="subscription" action="show" id="${subscriptionInstance?.id}">
        ${subscriptionInstance?.name}
    </g:link>
    </h2>
    <semui:auditInfo auditable="[subscriptionInstance, 'name']"/>
</g:if>

<div class="ui stackable grid">
    <div class="twelve wide column">
        <div class="la-inline-lists">

            <g:if test="${subscriptionInstance}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.details.status')}</dt>
                            <dd>${subscriptionInstance?.status?.getI10n('value')}</dd>
                            <dd><semui:auditInfo auditable="[subscriptionInstance, 'status']"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.details.type')}</dt>
                            <dd>${subscriptionInstance?.type?.getI10n('value')}</dd>
                            <dd><semui:auditInfo auditable="[subscriptionInstance, 'type']"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                            <dd>${subscriptionInstance?.form?.getI10n('value')}</dd>
                            <dd><semui:auditInfo auditable="[subscriptionInstance, 'form']"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                            <dd>${subscriptionInstance?.resource?.getI10n('value')}</dd>
                            <dd><semui:auditInfo auditable="[subscriptionInstance, 'resource']"/></dd>
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
                                    ${subscriptionInstance?.isSlaved ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
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

                    </div>
                </div>

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
                                    class="ui button la-js-dont-hide-button">Lizenzmerkmale anzeigen</button>
                            <script>
                                $('#subscription-properties-toggle').on('click', function () {
                                    $('#subscription-properties').toggleClass('hidden')
                                    if ($('#subscription-properties').hasClass('hidden')) {
                                        $(this).text('Lizenzmerkmale anzeigen')
                                    } else {
                                        $(this).text('Lizenzmerkmale ausblenden')
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

                <g:set var="oldEditable" value="${editable}"/>
                <div id="subscription-properties" class="hidden" style="margin: 1em 0">
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:set var="editable" value="${false}" scope="page"/>
                    <g:render template="/subscription/properties" model="${[
                            subscriptionInstance: subscriptionInstance,
                            authorizedOrgs      : authorizedOrgs
                    ]}"/>

                    <g:set var="editable" value="${oldEditable ?: false}" scope="page"/>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>

                </div>

            </g:if>


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

            <div class="ui card la-time-card">
                <div class="content">
                    <div class="header"><g:message code="renewEntitlementsWithSurvey.currentEntitlements"/></div>
                </div>

                <div class="content">
                    <dl>
                        <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                        <dd>${ies?.size() ?: 0}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'tipp.listPrice')}</dt>
                        <dd><g:formatNumber number="${iesListPriceSum}" type="currency"/></dd>
                    </dl>
                </div>

                <div class="content">
                    <div class="ui la-vertical buttons">
                        <g:link controller="subscription" action="showEntitlementsRenewWithSurvey"
                                id="${surveyConfig?.id}"
                                class="ui button">
                            <g:message code="renewEntitlementsWithSurvey.toCurrentEntitlements"/>
                        </g:link>
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

    <div class="ui form twelve wide column">
        <div class="two fields">

            <div class="eight wide field" style="text-align: left;">
                <g:if test="${subscriptionInstance && editable}">
                    <g:link class="ui button green js-open-confirm-modal"
                            data-confirm-term-what="renewalEntitlements"
                            data-confirm-term-how="concludeBinding"
                            controller="myInstitution" action="surveyInfoFinish"
                            id="${surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig?.id, issueEntitlementsSurvey: true]">
                        <g:message code="renewEntitlementsWithSurvey.submit"/>
                    </g:link>
                </g:if>
            </div>


            <div class="eight wide field" style="text-align: right;">
                <g:if test="${subscriptionInstance}">
                    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                            id="${subscriptionInstance?.id}"
                            params="${[targetSubscriptionId: subscriptionInstance?.id,
                                       surveyConfigID      : surveyConfig?.id]}"
                            class="ui button">
                        <g:message code="surveyInfo.toIssueEntitlementsSurvey"/>
                    </g:link>
                </g:if>
            </div>
        </div>
    </div>

</div><!-- .grid -->

<br>
<br>



<br>
<br>

</body>
</html>
