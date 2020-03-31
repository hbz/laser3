<%@ page import="com.k_int.kbplus.Subscription" %>
<div class="ui stackable grid">
    <div class="twelve wide column">
        <g:if test="${controllerName == 'survey' && actionName == 'show'}">

            <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>
            <div class="ui horizontal segments">
                <div class="ui segment center aligned">
                    <b>${message(code: 'surveyConfig.subOrgs.label')}:</b>
                    <g:link controller="subscription" action="members" id="${subscriptionInstance.id}">
                        <div class="ui circular label">
                            ${countParticipants.subMembers}
                        </div>
                    </g:link>
                </div>

                <div class="ui segment center aligned">
                    <b>${message(code: 'surveyConfig.orgs.label')}:</b>
                    <g:link controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig?.id]">
                        <div class="ui circular label">${countParticipants.surveyMembers}</div>
                    </g:link>

                    <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                        ( ${countParticipants.subMembersWithMultiYear}
                        ${message(code: 'surveyConfig.subOrgsWithMultiYear.label')} )
                    </g:if>
                </div>
            </div>
        </g:if>

        <div class="ui card ">
            <div class="content">
                <g:if test="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon la-popup-tooltip la-delay"
                                     data-content="${message(code: "surveyConfig.scheduledStartDate.comment")}">
                                    ${message(code: 'surveyConfig.scheduledStartDate.label')}
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
                                     data-content="${message(code: "surveyConfig.scheduledEndDate.comment")}">
                                    ${message(code: 'surveyConfig.scheduledEndDate.label')}
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
                                 data-content="${message(code: "surveyConfig.internalComment.comment")}">
                                ${message(code: 'surveyConfig.internalComment.label')}
                                <i class="question small circular inverted icon"></i>
                            </div>
                        </dt>
                        <dd><semui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

                    </dl>

                    <br>

                    <div class="ui form">
                        <g:form action="setSurveyConfigComment" controller="survey" method="post"
                                params="[surveyConfigID: surveyConfig?.id, id: surveyInfo?.id]">
                            <div class="field">
                                <label><div class="ui icon la-popup-tooltip la-delay"
                                            data-content="${message(code: "surveyConfig.comment.comment")}">
                                    ${message(code: 'surveyConfig.comment.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div></label>
                                <textarea name="comment" rows="15">${surveyConfig?.comment}</textarea>
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
                                ${message(code: 'surveyConfig.scheduledStartDate.label')}
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="scheduledStartDate" type="date"
                                                 overwriteEditable="${false}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyConfig.scheduledEndDate.label')}
                            </dt>
                            <dd><semui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"
                                                 overwriteEditable="${false}"/></dd>

                        </dl>
                    </g:if>

                    <div class="ui form">
                        <div class="field">
                            <label>
                                <g:message code="surveyConfigsInfo.comment"/>
                            </label>
                            <g:if test="${surveyConfig?.comment}">
                                <textarea readonly="readonly" rows="15">${surveyConfig?.comment}</textarea>
                            </g:if>
                            <g:else>
                                <g:message code="surveyConfigsInfo.comment.noComment"/>
                            </g:else>
                        </div>
                    </div>
                </g:else>

                <br>

                <div class="field" style="text-align: right;">
                    <button id="subscription-info-toggle"
                            class="ui button">Lizenzinformationen anzeigen</button>
                    <script>
                        $('#subscription-info-toggle').on('click', function () {
                            $('#subscription-info').toggleClass('hidden')
                            if ($('#subscription-info').hasClass('hidden')) {
                                $(this).text('Lizenzinformationen anzeigen')
                            } else {
                                $(this).text('Lizenzinformationen ausblenden')
                            }
                        })
                    </script>
                </div>

            </div>
        </div>


        <div id="subscription-info" class="la-inline-lists hidden">

            <div class="ui card">
                <div class="content">
                    <div class="header">
                        <g:if test="${!subscriptionInstance}">
                            <h2 class="ui icon header"><semui:headerIcon/>

                                <i class="icon clipboard outline la-list-icon"></i>
                                <g:link controller="public" action="gasco"
                                        params="${[q: '"' + surveyConfig?.subscription?.name + '"']}">
                                    ${surveyConfig?.subscription?.name}
                                </g:link>
                            </h2>

                            <div class="field" style="text-align: right;">
                                <g:link class="ui button" controller="public" action="gasco"
                                        params="${[q: '"' + surveyConfig?.subscription?.name + '"']}">
                                    GASCO-Monitor
                                </g:link>
                            </div>
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
                                    <dt class="control-label">${message(code: 'subscription.type.label')}</dt>
                                    <dd>${subscriptionInstance?.type?.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscriptionInstance, 'type']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.kind.label')}</dt>
                                    <dd>${subscriptionInstance?.kind?.getI10n('value')}</dd>
                                    <dd><semui:auditInfo auditable="[subscriptionInstance, 'kind']"/></dd>
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

                                <div class="eight wide field" style="text-align: right;">
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

                            </div>



                            <g:set var="oldEditable" value="${editable}"/>
                            <div id="subscription-properties" class="hidden" style="margin: 1em 0">
                                <g:set var="editable" value="${false}" scope="request"/>
                                <g:set var="editable" value="${false}" scope="page"/>
                                <g:render template="/subscription/properties" model="${[
                                        subscriptionInstance: subscriptionInstance,
                                        authorizedOrgs      : authorizedOrgs
                                ]}"/>

                            </div>

                            <g:if test="${derivedPropDefGroups?.global || derivedPropDefGroups?.local || derivedPropDefGroups?.member || derivedPropDefGroups?.fallback}">
                                <div id="derived-license-properties" class="hidden" style="margin: 1em 0">

                                    <g:render template="/subscription/licProp" model="${[
                                            license             : subscriptionInstance?.owner,
                                            derivedPropDefGroups: derivedPropDefGroups
                                    ]}"/>
                                </div>
                            </g:if>
                            <g:set var="editable" value="${oldEditable ?: false}" scope="page"/>
                            <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                        </div>

                    </div>
                </g:if>
            </div>

        </div>

        <g:if test="${surveyInfo?.type.id == de.laser.helper.RDStore.SURVEY_TYPE_RENEWAL.id}">
            <div class="ui card la-time-card">

                <div class="content">
                    <div class="header"><g:message code="surveyConfigsInfo.costItems"/></div>
                </div>

                <div class="content">

                    <g:set var="surveyOrg"
                           value="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>
                    <g:set var="costItemSurvey"
                           value="${surveyOrg ? com.k_int.kbplus.CostItem.findBySurveyOrg(surveyOrg) : null}"/>
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
                                        ${costItemSub.costItemElement.getI10n('value')}
                                        <b><g:formatNumber
                                                number="${costItemSub.costInBillingCurrency}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/></b>

                                        ${(costItemSub.billingCurrency.getI10n('value').split('-')).first()}

                                        <g:set var="oldCostItem"
                                               value="${costItemSub.costInBillingCurrency ?: 0.0}"/>

                                        <g:if test="${costItemSub.startDate || costItemSub.endDate}">
                                            <br>(${formatDate(date: costItemSub?.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSub?.endDate, format: message(code: 'default.date.format.notime'))})
                                        </g:if>
                                        <br>

                                    </g:each>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${costItemSurvey}">
                                    ${costItemSurvey.costItemElement.getI10n('value')}
                                    <b><g:formatNumber
                                            number="${costItemSurvey.costInBillingCurrency}"
                                            minFractionDigits="2" maxFractionDigits="2" type="number"/></b>

                                    ${(costItemSurvey.billingCurrency.getI10n('value').split('-')).first()}

                                    <g:set var="newCostItem"
                                           value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                    <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                        <br>(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                                    </g:if>

                                    <g:if test="${costItemSurvey.costDescription}">
                                        <br>

                                        <div class="ui icon la-popup-tooltip la-delay"
                                             data-position="right center"
                                             data-variation="tiny"
                                             data-content="${costItemSurvey.costDescription}">
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
                                        (<g:formatNumber
                                                number="${((newCostItem - oldCostItem) / oldCostItem) * 100}"
                                                minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/>%)</b>
                                </g:if>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </g:if>

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

                        <g:if test="${surveyProperty?.surveyProperty?.tenant?.id == institution?.id}">
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
                        ${com.k_int.properties.PropertyDefinition.getLocalizedValue(surveyProperty?.surveyProperty.type)}
                        <g:if test="${surveyProperty?.surveyProperty.type == 'class com.k_int.kbplus.RefdataValue'}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(surveyProperty?.surveyProperty.refdataCategory)}"
                                    var="refdataValue">
                                <g:set var="refdataValues"
                                       value="${refdataValues + refdataValue?.getI10n('value')}"/>
                            </g:each>
                            <br>
                            (${refdataValues.join('/')})
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${editable && surveyInfo.status == de.laser.helper.RDStore.SURVEY_IN_PROCESSING &&
                                com.k_int.kbplus.SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyProperty?.surveyProperty)
                                && (de.laser.helper.RDStore.SURVEY_PROPERTY_PARTICIPATION?.id != surveyProperty?.surveyProperty?.id)}">
                            <g:link class="ui icon negative button"
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
                    <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM_SURVEY', 'INST_EDITOR')}">
                        ${message(code: 'surveyResult.commentOnlyForOwner')}
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
                        ${com.k_int.properties.PropertyDefinition.getLocalizedValue(surveyResult?.type.type)}
                        <g:if test="${surveyResult?.type.type == 'class com.k_int.kbplus.RefdataValue'}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(surveyResult?.type.refdataCategory)}"
                                    var="refdataValue">
                                <g:set var="refdataValues"
                                       value="${refdataValues + refdataValue?.getI10n('value')}"/>
                            </g:each>
                            <br>
                            (${refdataValues.join('/')})
                        </g:if>
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
                            <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM_SURVEY', 'INST_EDITOR')}">
                                <semui:xEditable owner="${surveyResult}" type="textarea" field="ownerComment"/>
                            </g:if>
                            <g:else>
                                <semui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                            </g:else>
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