<%@ page import="com.k_int.kbplus.Subscription" %>

<g:if test="${controllerName == 'survey' && actionName == 'show'}">
    <div class="ui card ">
        <div class="content">
            <dl>
                <dt class="control-label">
                    <div class="ui icon la-popup-tooltip la-delay"
                         data-content="${message(code: "surveyConfig.scheduledStartDate.comment")}">
                        ${message(code: 'surveyConfig.scheduledStartDate.label')}
                        <i class="question small circular inverted icon"></i>
                    </div>
                </dt>
                <dd><semui:xEditable owner="${surveyConfig}" field="scheduledStartDate"
                                     type="date"/></dd>

            </dl>
            <dl>
                <dt class="control-label">
                    <div class="ui icon la-popup-tooltip la-delay"
                         data-content="${message(code: "surveyConfig.scheduledEndDate.comment")}">
                        ${message(code: 'surveyConfig.scheduledEndDate.label')}
                        <i class="question small circular inverted icon"></i>
                    </div>
                </dt>
                <dd><semui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"/></dd>

            </dl>
            <dl>
                <dt class="control-label">
                    <div class="ui icon la-popup-tooltip la-delay"
                         data-content="${message(code: "surveyConfig.header.comment")}">
                        ${message(code: 'surveyConfig.header.label')}
                        <i class="question small circular inverted icon"></i>
                    </div>
                </dt>
                <dd><semui:xEditable owner="${surveyConfig}" field="header"/></dd>

            </dl>
            <dl>
                <dt class="control-label">
                    <div class="ui icon la-popup-tooltip la-delay"
                         data-content="${message(code: "surveyConfig.comment.comment")}">
                        ${message(code: 'surveyConfig.comment.label')}
                        <i class="question small circular inverted icon"></i>
                    </div>
                </dt>
                <dd><semui:xEditable owner="${surveyConfig}" field="comment" type="textarea"/></dd>

            </dl>
            <dl>
                <dt class="control-label">
                    <div class="ui icon la-popup-tooltip la-delay"
                         data-content="${message(code: "surveyConfig.internalComment.comment")}">
                        ${message(code: 'surveyConfig.internalComment.label')}
                        <i class="question small circular inverted icon"></i>
                    </div>
                </dt>
                <dd><semui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

            </dl>

        </div>
    </div>
</g:if>


<div class="ui stackable grid">
    <div class="twelve wide column">
        <div class="la-inline-lists">

            <div class="ui card">
                <div class="content">
                    <div class="header">
                        <g:if test="${!subscriptionInstance}">
                            <h2 class="ui icon header"><semui:headerIcon/>

                                <i class="icon clipboard outline la-list-icon"></i>
                                <g:link controller="public" action="gasco"
                                        params="[q: surveyConfig?.subscription?.name]">
                                    ${surveyConfig?.subscription?.name}
                                </g:link>
                            </h2>
                        </g:if>
                        <g:else>

                            <h2 class="ui icon header"><semui:headerIcon/>
                            <g:link controller="subscription" action="show" id="${subscriptionInstance?.id}">
                                ${subscriptionInstance?.name}
                            </g:link>
                            </h2>
                            <semui:auditInfo auditable="[subscriptionInstance, 'name']"/>
                        </g:else>
                    </div>
                </div>
                <g:if test="${subscriptionInstance}">
                    <div class="content">
                        <div class="ui two column stackable grid container">
                            <div class="column">
                                <dl>
                                    <dt class="control-label">${message(code: 'default.status.label')}</dt>
                                    <dd>${subscriptionInstance?.status?.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscriptionInstance, 'status']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'default.type.label')}</dt>
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
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                                    <dd>${subscriptionInstance?.hasPerpetualAccess ? de.laser.helper.RDStore.YN_YES.getI10n('value') : de.laser.helper.RDStore.YN_NO.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscriptionInstance, 'hasPerpetualAccess']"/></dd>
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
                                            ${message(code: 'license.details.linktoLicense.pendingChange')}
                                        </dt>
                                        <dd>
                                            ${subscriptionInstance?.isSlaved ? de.laser.helper.RDStore.YN_YES.getI10n('value') : de.laser.helper.RDStore.YN_NO.getI10n('value')}
                                        </dd>
                                    </dl>
                                </g:if>
                                <dl>
                                    <dt class="control-label">
                                        <g:message code="default.identifiers.label"/>
                                    </dt>
                                    <dd>
                                        <g:each in="${subscriptionInstance?.ids?.sort { it?.ns?.ns }}"
                                                var="id">
                                            <span class="ui small teal image label">
                                                ${id.ns.ns}: <div class="detail">${id.value}</div>
                                            </span>
                                        </g:each>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">
                                        <div class="ui icon la-popup-tooltip la-delay"
                                             data-content="${message(code: "surveyConfig.scheduledStartDate.comment")}">
                                            ${message(code: 'surveyConfig.scheduledStartDate.label')}
                                        </div>
                                    </dt>
                                    <dd><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${surveyConfig?.scheduledStartDate}"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">
                                        <div class="ui icon la-popup-tooltip la-delay"
                                             data-content="${message(code: "surveyConfig.scheduledEndDate.comment")}">
                                            ${message(code: 'surveyConfig.scheduledEndDate.label')}
                                        </div>
                                    </dt>
                                    <dd><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${surveyConfig?.scheduledEndDate}"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">
                                        <g:message code="surveyConfigsInfo.comment"/>
                                    </dt>
                                    <dd>
                                        <g:if test="${surveyConfig?.comment}">
                                            ${surveyConfig?.comment}
                                        </g:if><g:else>
                                            <g:message code="surveyConfigsInfo.comment.noComment"/>
                                        </g:else>
                                    </dd>
                                </dl>
                            </div>


                            <div class="column">
                                <g:if test="${subscriptionInstance?.packages}">
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
                                </g:if>

                                <g:if test="${visibleOrgRelations}">

                                    <g:render template="/templates/links/orgLinksAsList"
                                              model="${[roleLinks    : visibleOrgRelations,
                                                        roleObject   : subscriptionInstance,
                                                        roleRespValue: 'Specific subscription editor',
                                                        editmode     : false,
                                                        showPersons  : false
                                              ]}"/>

                                </g:if>
                            </div>
                        </div>

                        <br>
                        <g:set var="derivedPropDefGroups"
                               value="${subscriptionInstance?.owner?.getCalculatedPropDefGroups(contextService.getOrg())}"/>

                        <div class="ui form">
                            <div class="two fields">
                                <div class="eight wide field" style="text-align: left;">
                                    <g:if test="${derivedPropDefGroups?.global || derivedPropDefGroups?.local || derivedPropDefGroups?.member || derivedPropDefGroups?.fallback}">

                                        <button id="derived-license-properties-toggle"
                                                class="ui button">Vertragsmerkmale anzeigen</button>
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
                                </div>

                                <div class="eight wide field" style="text-align: right;">
                                    <button id="subscription-properties-toggle"
                                            class="ui button">Lizenzmerkmale anzeigen</button>
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
                            </div>
                        </div>

                    </div>
                </g:if>
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

            <g:if test="${controllerName == 'survey' && actionName == 'show'}">

                <div class="ui card">
                    <div class="content">

                        <dl>
                            <dt class="control-label">${message(code: 'surveyConfig.orgs.label')}</dt>
                            <dd>
                                <g:link controller="survey" action="surveyParticipants"
                                        id="${surveyConfig.surveyInfo.id}"
                                        params="[surveyConfigID: surveyConfig?.id]" class="ui icon"><div
                                        class="ui circular label">${surveyConfig?.orgs?.size()}</div></g:link>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyConfig.subOrgs.label')}</dt>
                            <dd>
                                <div class="ui circular label">
                                    ${com.k_int.kbplus.Subscription.findAllByInstanceOf(subscriptionInstance)?.size()}
                                </div>
                            </dd>
                        </dl>
                    </div>
                </div>
            </g:if>

            <div class="ui card la-time-card">

                <div class="content">
                    <div class="header"><g:message code="surveyConfigsInfo.costItems"/></div>
                </div>

                <div class="content">

                    <g:set var="costItemSurvey"
                           value="${com.k_int.kbplus.CostItem.findBySurveyOrg(com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution))}"/>
                    <g:set var="costItemsSub"
                           value="${subscriptionInstance?.costItems?.findAll {
                               it?.costItemElement?.id == costItemSurvey?.costItemElement?.id
                           }}"/>

                    <%
                        // ERMS-1521 HOTFIX
                        if (!costItemsSub) {
                            costItemsSub = subscriptionInstance?.costItems.findAll {
                                it.costItemElement?.id == com.k_int.kbplus.RefdataValue.getByValueAndCategory('price: consortial price', de.laser.helper.RDConstants.COST_ITEM_ELEMENT)?.id
                            }
                        }
                    %>

                    <table class="ui celled la-table-small la-table-inCard table">
                        <thead>
                        <tr>
                            <th>
                                <g:message code="surveyConfigsInfo.oldPrice"/>
                            </th>
                            <th>
                                <g:message code="surveyConfigsInfo.newPrice"/>
                            </th>
                            <th>Diff.</th>
                        </tr>
                        </thead>
                        <tbody class="top aligned">
                        <tr>
                            <td>
                                <g:if test="${costItemsSub}">
                                    <g:each in="${costItemsSub}" var="costItemSub">
                                        ${costItemSub?.costItemElement?.getI10n('value')}
                                        <b><g:formatNumber
                                                number="${costItemSub?.costInBillingCurrency}"
                                                minFractionDigits="2" maxFractionDigits="2" type="number"/></b>

                                        ${(costItemSub?.billingCurrency?.getI10n('value').split('-')).first()}

                                        <g:set var="oldCostItem" value="${costItemSub.costInBillingCurrency ?: 0.0}"/>

                                        <g:if test="${costItemSub?.startDate || costItemSub?.endDate}">
                                            <br>(${formatDate(date: costItemSub?.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSub?.endDate, format: message(code: 'default.date.format.notime'))})
                                        </g:if>
                                        <br>

                                    </g:each>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${costItemSurvey}">
                                    ${costItemSurvey?.costItemElement?.getI10n('value')}
                                    <b><g:formatNumber
                                            number="${costItemSurvey?.costInBillingCurrency}"
                                            minFractionDigits="2" maxFractionDigits="2" type="number"/></b>

                                    ${(costItemSurvey?.billingCurrency?.getI10n('value').split('-')).first()}

                                    <g:set var="newCostItem" value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                    <g:if test="${costItemSurvey?.startDate || costItemSurvey?.endDate}">
                                        <br>(${formatDate(date: costItemSurvey?.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey?.endDate, format: message(code: 'default.date.format.notime'))})
                                    </g:if>

                                    <g:if test="${costItemSurvey?.costDescription}">
                                        <br>

                                        <div class="ui icon la-popup-tooltip la-delay" data-position="right center"
                                             data-variation="tiny"
                                             data-content="${costItemSurvey?.costDescription}">
                                            <i class="question small circular inverted icon"></i>
                                        </div>
                                    </g:if>

                                </g:if>
                            </td>
                            <td>
                                <g:if test="${oldCostItem && newCostItem}">
                                    <b><g:formatNumber
                                            number="${(newCostItem - oldCostItem)}"
                                            minFractionDigits="2" maxFractionDigits="2" type="number"/>
                                        <br>
                                        (<g:formatNumber number="${((newCostItem - oldCostItem) / oldCostItem) * 100}"
                                                         minFractionDigits="2"
                                                         maxFractionDigits="2" type="number"/>%)</b>
                                </g:if>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>

    <aside class="four wide column la-sidekick">
        <g:if test="${controllerName == 'survey' && actionName == 'show'}">

            <g:render template="/templates/tasks/card"
                      model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>


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
        </g:if>

        <div id="container-documents">
            <g:render template="/survey/cardDocuments"
                      model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
        </div>
    </aside><!-- .four -->

</div><!-- .grid -->

<g:if test="${controllerName == 'survey' && !surveyConfig.pickAndChoose && actionName == 'show'}">
    <g:set var="surveyProperties" value="${surveyConfig.surveyProperties}"/>

    <semui:form>

        <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'surveyProperty.selected.label')} <semui:totalNumber
                total="${surveyProperties.size()}"/></h4>

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.name')}</th>
                <th>${message(code: 'surveyProperty.expl.label')}</th>
                <th>${message(code: 'surveyProperty.comment.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
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

                        <g:if test="${surveyProperty?.surveyProperty?.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyProperty?.surveyProperty?.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                    </td>

                    <td>
                        <g:if test="${surveyProperty?.surveyProperty?.getI10n('expl')}">
                            ${surveyProperty?.surveyProperty?.getI10n('expl')}
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
                        <g:if test="${editable && surveyInfo.status == de.laser.helper.RDStore.SURVEY_IN_PROCESSING &&
                                com.k_int.kbplus.SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyProperty?.surveyProperty)
                                && (com.k_int.kbplus.SurveyProperty.findByName('Participation')?.id != surveyProperty?.surveyProperty?.id)}">
                            <g:link role="button" class="ui icon negative button"
                                    controller="survey" action="deleteSurveyPropFromConfig"
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
                <g:if test="${editable && properties && surveyInfo.status == de.laser.helper.RDStore.SURVEY_IN_PROCESSING}">
                    <td colspan="6">
                        <g:form action="addSurveyPropToConfig" controller="survey" method="post" class="ui form">
                            <g:hiddenField name="id" value="${surveyInfo?.id}"/>
                            <g:hiddenField name="surveyConfigID" value="${surveyConfig?.id}"/>

                            <div class="field required">
                                <label>${message(code: 'surveyConfigs.property')}</label>
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

<g:if test="${surveyResults && !surveyConfig.pickAndChoose}">

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
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>${message(code: 'surveyResult.commentParticipant')}</th>
                <th>
                    ${message(code: 'surveyResult.commentOnlyForParticipant')}
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                        <i class="question circle icon"></i>
                    </span>
                </th>
            </tr>
            </thead>
            <g:each in="${surveyResults}" var="surveyResult" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyResult?.type?.getI10n('name')}

                        <g:if test="${surveyResult?.type?.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${surveyResult?.type?.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                    </td>
                    <td>
                        ${surveyResult?.type?.getLocalizedType()}

                    </td>
                    <g:set var="surveyOrg"
                           value="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyResult?.surveyConfig, institution)}"/>

                    <g:if test="${!surveyOrg?.existsMultiYearTerm()}">

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
                                <g:if test="${surveyResult?.urlValue}">
                                    <semui:linkIcon/>
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${surveyResult?.type?.type == com.k_int.kbplus.RefdataValue.toString()}">

                                <g:if test="${surveyResult?.type?.name in ["Participation"] && surveyResult?.owner?.id != institution?.id}">
                                    <semui:xEditableRefData owner="${surveyResult}" field="refValue" type="text"
                                                            id="participation"
                                                            config="${surveyResult.type?.refdataCategory}"/>
                                </g:if>
                                <g:else>
                                    <semui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                            config="${surveyResult.type?.refdataCategory}"/>
                                </g:else>
                            </g:elseif>
                        </td>
                        <td>
                            <semui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                        </td>
                        <td>
                            <semui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                        </td>
                    </g:if>
                    <g:else>
                        <td>
                            <g:message code="surveyOrg.perennialTerm.available"/>
                        </td>
                        <td>

                        </td>
                        <td>

                        </td>
                    </g:else>

                </tr>
            </g:each>
        </table>
    </semui:form>
</g:if>

<r:script>
                                    $('body #participation').editable({
                                        validate: function (value) {
                                            if (value == "com.k_int.kbplus.RefdataValue:${de.laser.helper.RDStore.YN_NO.id}") {
                                                var r = confirm("Wollen Sie wirklich im nächstem Jahr nicht mehr bei dieser Lizenz teilnehmen?  " );
                                                if (r == false) {
                                                   return "Sie haben die Nicht-Teilnahme an der Lizenz für das nächste Jahr nicht zugestimmt!"
                                                }
                                            }
                                        },
                                        tpl: '<select class="ui dropdown"></select>'
                                    }).on('shown', function() {
                                        $(".table").trigger('reflow');
                                        $('.ui.dropdown')
                                                .dropdown({
                                            clearable: true
                                        })
                                        ;
                                    }).on('hidden', function() {
                                        $(".table").trigger('reflow')
                                    });
</r:script>