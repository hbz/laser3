<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>

<r:require module="annotations"/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyConfigsInfo.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="surveyConfigsInfo.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<br>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<g:render template="nav"/>

<semui:messages data="${flash}"/>

<br>
<g:if test="${navigation}">
    <br>

    <div class="ui center aligned grid">
        <div class='ui big label la-annual-rings'>

            <g:if test="${navigation?.prev}">
                <g:link action="surveyConfigsInfo" id="${surveyInfo?.id}"
                        params="[surveyConfigID: navigation?.prev?.id]" class="item"
                        title="${message(code: 'surveyConfigsInfo.prevSurveyConfig')}">
                    <i class='arrow left icon'></i>
                </g:link>
            </g:if>
            <g:else>
                <i class=' icon'></i>
            </g:else>
            <g:message code="surveyConfigsInfo.totalSurveyConfig"
                       args="[surveyConfig?.configOrder, navigation?.total]"/>
            <g:if test="${navigation?.next}">
                <g:link action="surveyConfigsInfo" id="${surveyInfo?.id}"
                        params="[surveyConfigID: navigation?.next?.id]" class="item"
                        title="${message(code: 'surveyConfigsInfo.nextSurveyConfig')}">
                    <i class='arrow right icon'></i>
                </g:link>
            </g:if>
            <g:else>
                <i class=' icon'></i>
            </g:else>
        </div>
    </div>
</g:if>

<br>

<g:if test="${surveyConfig?.type == 'Subscription'}">
    <h2 class="ui icon header"><semui:headerIcon/>
    <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.id}">
        ${surveyConfig?.subscription?.name}
    </g:link>
    </h2>
    <semui:auditInfo auditable="[surveyConfig?.subscription, 'name']"/>
    <semui:anualRings object="${surveyConfig?.subscription}" controller="subscription" action="show"
                      navNext="${null}" navPrev="${null}"/>
</g:if>
<g:else>
    <h2><g:message code="surveyConfigsInfo.surveyConfig.info" args="[surveyConfig?.getConfigNameShort()]"/></h2>
</g:else>


<g:if test="${surveyConfig}">
    <div class="ui stackable grid">
        <div class="twelve wide column">
            <div class="la-inline-lists">

                <div class="ui card">
                    <div class="content">
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.details.status')}</dt>
                                <dd>${surveyConfig?.subscription?.status?.getI10n('value')}</dd>
                                <dd><semui:auditInfo auditable="[surveyConfig?.subscription, 'status']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.details.type')}</dt>
                                <dd>${surveyConfig?.subscription.type?.getI10n('value')}</dd>
                                <dd><semui:auditInfo auditable="[surveyConfig?.subscription, 'type']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                                <dd>${surveyConfig?.subscription?.form?.getI10n('value')}</dd>
                                <dd><semui:auditInfo auditable="[surveyConfig?.subscription, 'form']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                                <dd>${surveyConfig?.subscription?.resource?.getI10n('value')}</dd>
                                <dd><semui:auditInfo auditable="[surveyConfig?.subscription, 'resource']"/></dd>
                            </dl>
                            <g:if test="${surveyConfig?.subscription.instanceOf && (contextOrg?.id == surveyConfig?.subscription.getConsortia()?.id)}">
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.isInstanceOfSub.label')}</dt>
                                    <dd>
                                        <g:link controller="subscription" action="show"
                                                id="${surveyConfig?.subscription.instanceOf.id}">${surveyConfig?.subscription.instanceOf}</g:link>
                                    </dd>
                                </dl>

                                <dl>
                                    <dt class="control-label">
                                        ${message(code: 'license.details.linktoLicense.pendingChange', default: 'Automatically Accept Changes?')}
                                    </dt>
                                    <dd>
                                        ${surveyConfig?.subscription?.isSlaved?.getI10n('value')}
                                    </dd>
                                </dl>
                            </g:if>
                            <dl>
                                <dt class="control-label">
                                    <g:message code="default.identifiers.label"/>
                                </dt>
                                <dd>
                                    <g:each in="${surveyConfig?.subscription?.ids?.sort { it?.identifier?.ns?.ns }}"
                                            var="id">
                                        <span class="ui small teal image label">
                                            ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                                        </span>
                                    </g:each>
                                </dd>
                            </dl>
                        </g:if>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyConfig.orgs.label')}</dt>
                            <dd>
                                <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                        params="[surveyConfigID: surveyConfig?.id]" class="ui icon"><div
                                        class="ui circular label">${surveyConfig?.orgs?.size() ?: 0}</div></g:link>
                            </dd>
                        </dl>
                    </div>
                </div>
                <g:if test="${surveyConfig?.type == 'Subscription'}">

                    <g:if test="${surveyConfig?.subscription?.packages}">
                        <div class="ui card la-js-hideable">
                            <div class="content">
                                <table class="ui three column la-selectable table">
                                    <g:each in="${surveyConfig?.subscription?.packages.sort { it.pkg.name }}" var="sp">
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
                                                roleObject   : surveyConfig?.subscription,
                                                roleRespValue: 'Specific subscription editor',
                                                editmode     : false
                                      ]}"/>

                        </div>
                    </div>

                    <div class="ui card la-js-hideable">
                        <div class="content">
                            <g:set var="derivedPropDefGroups"
                                   value="${surveyConfig?.subscription?.owner?.getCalculatedPropDefGroups(contextService.getOrg())}"/>

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
                                    license             : surveyConfig?.subscription?.owner,
                                    derivedPropDefGroups: derivedPropDefGroups
                            ]}"/>
                        </div>
                    </g:if>

                    <g:set var="oldEditable" value="${editable}"/>
                    <div id="subscription-properties" class="hidden" style="margin: 1em 0">
                        <g:set var="editable" value="${false}" scope="request"/>
                        <g:set var="editable" value="${false}" scope="page"/>
                        <g:render template="/subscription/properties" model="${[
                                subscriptionInstance: surveyConfig?.subscription,
                                authorizedOrgs      : authorizedOrgs
                        ]}"/>

                        <g:set var="editable" value="${oldEditable ?: false}" scope="page"/>
                        <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>

                    </div>



                <%-- FINANCE, to be reactivated as of ERMS-943 --%>
                <%-- assemble data on server side --%>
                    <g:if test="${costItemSums.ownCosts || costItemSums.consCosts || costItemSums.subscrCosts}">
                        <div class="ui card la-dl-no-table">
                            <div class="content">
                                <g:if test="${costItemSums.ownCosts && contextOrg.id != subscription.getConsortia()?.id}">
                                    <h5 class="ui header">${message(code: 'financials.label', default: 'Financials')} : ${message(code: 'financials.tab.ownCosts')}</h5>
                                    <g:render template="/subscription/financials"
                                              model="[data: costItemSums.ownCosts]"/>
                                </g:if>
                                <g:if test="${costItemSums.consCosts}">
                                    <h5 class="ui header">${message(code: 'financials.label', default: 'Financials')} : ${message(code: 'financials.tab.consCosts')}</h5>
                                    <g:render template="/subscription/financials"
                                              model="[data: costItemSums.consCosts]"/>
                                </g:if>
                                <g:elseif test="${costItemSums.subscrCosts}">
                                    <h5 class="ui header">${message(code: 'financials.label', default: 'Financials')} : ${message(code: 'financials.tab.subscrCosts')}</h5>
                                    <g:render template="/subscription/financials"
                                              model="[data: costItemSums.subscrCosts]"/>
                                </g:elseif>
                            </div>
                        </div>
                    </g:if>

                </g:if>

                <div class="ui card ">
                    <div class="content">
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <dl>
                                <dt class="control-label">
                                    <div class="ui icon"
                                         data-tooltip="${message(code: "surveyConfig.scheduledStartDate.comment")}">
                                        ${message(code: 'surveyConfig.scheduledStartDate.label')}
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </dt>
                                <dd><semui:xEditable owner="${surveyConfig}" field="scheduledStartDate"
                                                     type="date"/></dd>

                            </dl>
                            <dl>
                                <dt class="control-label">
                                    <div class="ui icon"
                                         data-tooltip="${message(code: "surveyConfig.scheduledEndDate.comment")}">
                                        ${message(code: 'surveyConfig.scheduledEndDate.label')}
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </dt>
                                <dd><semui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"/></dd>

                            </dl>
                        </g:if>
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon" data-tooltip="${message(code: "surveyConfig.header.comment")}">
                                    ${message(code: 'surveyConfig.header.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="header"/></dd>

                        </dl>
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon" data-tooltip="${message(code: "surveyConfig.comment.comment")}">
                                    ${message(code: 'surveyConfig.comment.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="comment" type="textarea"/></dd>

                        </dl>
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon"
                                     data-tooltip="${message(code: "surveyConfig.internalComment.comment")}">
                                    ${message(code: 'surveyConfig.internalComment.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

                        </dl>

                    </div>
                </div>

            </div>

        </div>

        <aside class="four wide column la-sidekick">

            <g:render template="/templates/tasks/card"
                      model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>

            <div id="container-documents">
                <g:render template="/survey/cardDocuments"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>

            <div id="container-notes">
                <g:render template="/templates/notes/card"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: accessService.checkPermAffiliation('ORG_CONSORTIUM_SURVEY', 'INST_EDITOR')]}"/>
            </div>

            <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM_SURVEY', 'INST_EDITOR')}">

                <g:render template="/templates/tasks/modal_create"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>

            </g:if>
            <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM_SURVEY', 'INST_EDITOR')}">
                <g:render template="/templates/notes/modal_create"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
            </g:if>

        </aside><!-- .four -->

    </div><!-- .grid -->

</g:if>

<br>
<g:if test="${surveyConfig?.type == 'Subscription'}">
    <div>
        <h4 class="ui left aligned icon header">${message(code: 'surveyProperty.selected.label')} <semui:totalNumber
                total="${surveyProperties.size()}"/></h4>
        <semui:form>
            <table class="ui celled sortable table la-table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'surveyProperty.name')}</th>
                    <th>${message(code: 'surveyProperty.explain.label')}</th>
                    <th>${message(code: 'surveyProperty.comment.label')}</th>
                    <th>${message(code: 'surveyProperty.type.label')}</th>
                    <th></th>
                </tr>
                </thead>

                <tbody>
                <g:each in="${surveyProperties.sort { it.surveyProperty?.name }}" var="surveyProperty" status="i">
                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
                        <td>
                            ${surveyProperty?.surveyProperty?.getI10n('name')}

                            <g:if test="${surveyProperty?.surveyProperty?.owner?.id == institution?.id}">
                                <i class='shield alternate icon'></i>
                            </g:if>

                            <g:if test="${surveyProperty?.surveyProperty?.getI10n('explain')}">
                                <span class="la-long-tooltip" data-position="right center" data-variation="tiny"
                                      data-tooltip="${surveyProperty?.surveyProperty?.getI10n('explain')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>

                        </td>

                        <td>
                            <g:if test="${surveyProperty?.surveyProperty?.getI10n('explain')}">
                                ${surveyProperty?.surveyProperty?.getI10n('explain')}
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${surveyProperty?.surveyProperty?.comment}">
                                ${surveyProperty?.surveyProperty?.comment}
                            </g:if>
                        </td>
                        <td>

                            ${surveyProperty?.surveyProperty?.getLocalizedType()}

                        </td>
                        <td>
                            <g:if test="${editable && com.k_int.kbplus.SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyProperty?.surveyProperty)}">
                                <g:link class="ui icon negative button"
                                        controller="survey" action="deleteSurveyPropfromSub"
                                        id="${surveyProperty?.id}">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
                <tfoot>
                <tr>
                    <g:if test="${editable}">
                        <td colspan="6">
                            <g:form action="addSurveyConfig" controller="survey" method="post" class="ui form">
                                <g:hiddenField name="id" value="${surveyInfo?.id}"/>
                                <g:hiddenField name="surveyConfigID" value="${surveyConfig?.id}"/>

                                <div class="field required">
                                    <label>${message(code: 'surveyConfigs.property')}</label>
                                    <semui:dropdown name="selectedProperty"

                                                    class="la-filterPropDef"
                                                    from="${properties}"
                                                    iconWhich="shield alternate"
                                                    optionKey="${{ "com.k_int.kbplus.SurveyProperty:${it.id}" }}"
                                                    optionValue="${{ it.getI10n('name') }}"
                                                    noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                                    required=""/>

                                </div>
                                <input type="submit" class="ui button"
                                       value="${message(code: 'surveyConfigsInfo.add.button')}"/>

                                <input type="submit" name="addtoallSubs" class="ui button"
                                       value="${message(code: "surveyConfigsInfo.addtoallSubs.button")}"/>
                            </g:form>
                        </td>
                    </g:if>
                </tr>
                </tfoot>

            </table>

        </semui:form>
    </div>

</g:if>
<br>
<g:form action="surveyConfigFinish" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

    <div class="ui right floated compact segment">
        <div class="ui checkbox">
            <input type="checkbox" onchange="this.form.submit()"
                   name="configFinish" ${surveyConfig?.configFinish ? 'checked' : ''}>
            <label><g:message code="surveyConfig.configFinish.label"/></label>
        </div>
    </div>

</g:form>

<g:javascript>
    $(".la-popup").popup({});
</g:javascript>
</body>
</html>


