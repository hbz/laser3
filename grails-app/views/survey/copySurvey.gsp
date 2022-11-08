<%@ page import="de.laser.Subscription; de.laser.RefdataCategory; de.laser.Doc; de.laser.finance.CostItem; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.survey.SurveyConfig" %>
<laser:htmlStart message="copySurvey.label" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                     text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>
    <ui:crumb message="copySurvey.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="${surveyConfig.getConfigNameShort()}" type="Survey" />

<h2 class="ui header">
    ${message(code: 'copySurvey.label')}:
    <g:link controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]">
        ${surveyConfig.getConfigNameShort()}
    </g:link>
</h2>

<ui:messages data="${flash}"/>

<g:if test="${workFlow == '2'}">
    <g:form action="processCopySurvey" controller="survey" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]">
        <ui:greySegment>
            <g:if test="${surveyInfo.type.id == RDStore.SURVEY_TYPE_INTEREST.id}">
                <div class="field required">
                    <label>${message(code: 'surveyInfo.name.label')} <g:message code="messageRequiredField" /></label>
                    <input type="text" name="name" placeholder="" value="${surveyInfo.name}"
                           required/>
                </div>
            </g:if>

            <g:if test="${targetSubs}">
                <h4 class="ui header">${message(code: 'copySurvey.subscription.info2')}</h4>

                <div class="ui ordered list">
                    <g:each in="${targetSubs}" var="sub" status="i">
                        <input type="hidden" name="targetSubs" value="${sub.id}"/>
                        <g:link controller="subscription" class="item" action="surveysConsortia" id="${sub.id}">
                            ${sub.dropdownNamingConvention()}
                        </g:link>
                    </g:each>
                </div>
            </g:if>

            <br />

            <hr />
            <table class="ui celled table">
                <tbody>

                <tr><td>${message(code: 'default.select.label')}</td><td>${message(code: 'copySurvey.property')}</td><td>${message(code: 'default.value.label')}</td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copyDates" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copyDates')}</td>
                    <td>
                        ${message(code: 'copySurvey.copyDates.startDate')}:&nbsp<g:if
                                test="${!surveyInfo.startDate}">-</g:if><g:formatDate date="${surveyInfo.startDate}"
                                                                                      format="${message(code: 'default.date.format.notime')}"/> &nbsp
                        ${message(code: 'copySurvey.copyDates.endDate')}:&nbsp<g:if
                                test="${!surveyInfo.endDate}">-</g:if><g:formatDate date="${surveyInfo.endDate}"
                                                                                    format="${message(code: 'default.date.format.notime')}"/>
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copyMandatory" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copyMandatory')}</td>
                    <td>
                        ${surveyInfo.isMandatory ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copyComment" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copyComment')}</td>
                    <td>
                        ${surveyInfo.comment}
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copySurveyConfigUrl" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copySurveyConfigUrl')}</td>
                    <td>
                        ${surveyConfig.url}
                        <g:if test="${surveyConfig.url}">
                            <ui:linkWithIcon href="${surveyConfig.url}"/>

                        </g:if>
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copySurveyConfigUrl2" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copySurveyConfigUrl2')}</td>
                    <td>
                        ${surveyConfig.url2}
                        <g:if test="${surveyConfig.url2}">
                            <ui:linkWithIcon href="${surveyConfig.url2}"/>

                        </g:if>
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copySurveyConfigUrl3" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copySurveyConfigUrl3')}</td>
                    <td>
                        ${surveyConfig.url3}
                        <g:if test="${surveyConfig.url3}">
                            <ui:linkWithIcon href="${surveyConfig.url3}"/>

                        </g:if>
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copySurveyConfigComment" value="${true}"/></td>
                    <td><g:message code="copySurvey.copySurveyConfigComment"/></td>
                    <td>
                        <g:if test="${surveyConfig.comment}">
                            <textarea class="la-textarea-resize-vertical" readonly="readonly" rows="15">${surveyConfig.comment}</textarea>
                        </g:if>
                    </td>
                </tr>

                <tr>
                    <td><g:checkBox name="copySurvey.copySurveyProperties" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copySurveyProperties')}</td>
                    <td>
                        <table class="ui celled sortable table la-js-responsive-table la-table">
                            <thead>
                            <tr>
                                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                                <th>${message(code: 'surveyProperty.name')}</th>
                                <th>${message(code: 'surveyProperty.expl.label')}</th>
                                <th>${message(code: 'default.type.label')}</th>
                            </tr>
                            </thead>

                            <tbody>
                            <g:each in="${surveyConfig.surveyProperties.sort { it.surveyProperty.getI10n('name') }}"
                                    var="surveyProperty"
                                    status="i">
                                <tr>
                                    <td class="center aligned">
                                        ${i + 1}
                                    </td>
                                    <td>
                                        ${surveyProperty.surveyProperty.getI10n('name')}

                                        <g:if test="${surveyProperty.surveyProperty.tenant?.id == institution.id}">
                                            <i class='shield alternate icon'></i>
                                        </g:if>

                                        <g:if test="${surveyProperty.surveyProperty.getI10n('expl')}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="right center"
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
                                                <g:if test="${refdataValue.getI10n('value')}">
                                                    <g:set var="refdataValues"
                                                           value="${refdataValues + refdataValue.getI10n('value')}"/>
                                                </g:if>
                                            </g:each>
                                            <br />
                                            (${refdataValues.join('/')})
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </td>
                </tr>

                <tr>
                    <td><g:checkBox name="copySurvey.copyDocs" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copyDocs')}</td>
                    <td>
                        <g:each in="${surveyConfig.documents.sort { it.owner.title }}" var="docctx">
                            <g:if test="${(((docctx.owner.contentType == 1) || (docctx.owner.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                                <g:link controller="docstore" id="${docctx.owner.uuid}" target="_blank">
                                    <g:if test="${docctx.owner.title}">
                                        ${docctx.owner.title}
                                    </g:if>
                                    <g:else>
                                        <g:if test="${docctx.owner.filename}">
                                            ${docctx.owner.filename}
                                        </g:if>
                                        <g:else>
                                            ${message(code: 'template.documents.missing')}
                                        </g:else>
                                    </g:else>

                                </g:link>(${docctx.owner.type.getI10n("value")}) <br />
                            </g:if>
                        </g:each>
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copyAnnouncements" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copyAnnouncements')}</td>
                    <td>
                        <g:each in="${surveyConfig.documents.sort { it.owner.title }}" var="docctx">
                            <g:if test="${((docctx.owner.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                                <g:if test="${docctx.owner.title}">
                                    <strong>${docctx.owner.title}</strong>
                                </g:if>
                                <g:else>
                                    <strong>Ohne Titel</strong>
                                </g:else>

                                (${message(code: 'template.notes.created')}
                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${docctx.owner.dateCreated}"/>)

                                <br />
                            </g:if>
                        </g:each>
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copyTasks" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copyTasks')}</td>
                    <td>
                        <g:each in="${tasks}" var="tsk">
                            <div id="summary" class="summary">
                            <strong>${tsk.title}</strong> (${message(code: 'task.endDate.label')}
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${tsk.endDate}"/>)
                            <br />
                        </g:each>
                    </td>
                </tr>
                <tr>
                    <td><g:checkBox name="copySurvey.copyParticipants" value="${true}"/></td>
                    <td>${message(code: 'copySurvey.copyParticipants')}</td>
                    <td>
                        <table class="ui sortable celled la-js-responsive-table la-table table">

                            <thead>
                            <tr>

                                <th>${message(code: 'sidewide.number')}</th>

                                <th>${message(code: 'org.sortname.label')}</th>
                                <th>${message(code: 'org.fullName.label')}</th>
                                <th>${message(code: 'org.libraryType.label')}</th>
                            </tr></thead><tbody>
                        <g:each in="${surveyConfig.orgs.sort { it.org.sortname }}" var="surveyOrg" status="i">
                            <tr>
                                <td class="center aligned">
                                    ${i + 1}
                                </td>
                                <td>${surveyOrg.org.sortname}</td>
                                <td>${surveyOrg.org.name}</td>
                                <td>${surveyOrg.org.libraryType?.getI10n('value')}</td>
                            </tr>
                        </g:each>
                        </tbody>
                        </table>
                    </td>
                </tr>

                </tbody>
            </table>
            <input type="submit" class="ui button js-click-control"
                   value="${message(code: 'default.button.copy.label')}"/>

        </ui:greySegment>
    </g:form>
</g:if>

<g:if test="${workFlow == "1"}">
    <div class="ui icon info message">
        <i class="info icon"></i>
        ${message(code: 'copySurvey.subscription.info')}
    </div>

    <ui:h1HeaderWithIcon message="myinst.currentSubscriptions.label" total="${num_sub_rows}" floated="true" />

    <ui:filter>
        <g:form action="copySurvey" controller="survey" method="get" class="ui small form">
            <input type="hidden" name="isSiteReloaded" value="yes"/>
            <input type="hidden" name="id" value="${params.id}"/>

            <div class="three fields">
                <!-- 1-1 -->
                <div class="field">
                    <label for="q">${message(code: 'default.search.text')}
                        <span data-position="right center" data-variation="tiny"
                              class="la-popup-tooltip la-delay"
                              data-content="${message(code: 'default.search.tooltip.subscription')}">
                            <i class="question circle icon"></i>
                        </span>
                    </label>

                    <div class="ui input">
                        <input type="text" id="q" name="q"
                               placeholder="${message(code: 'default.search.ph')}"
                               value="${params.q}"/>
                    </div>
                </div>
                <!-- 1-2 -->
                <div class="field">
                    <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn"
                                      placeholder="filter.placeholder" value="${validOn}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'default.status.label')}</label>
                    <ui:select class="ui dropdown" name="status"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.status}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
            </div>

            <div class="four fields">

                <!-- 2-1 + 2-2 -->
                <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>

                <!-- 2-3 -->
                <div class="field">
                    <label>${message(code: 'subscription.form.label')}</label>
                    <ui:select class="ui dropdown" name="form"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.form}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
                <!-- 2-4 -->
                <div class="field">
                    <label>${message(code: 'subscription.resource.label')}</label>
                    <ui:select class="ui dropdown" name="resource"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.resource}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>

            </div>

            <div class="two fields">
                <div class="field">
                    <label>${message(code: 'menu.my.providers')}</label>
                    <g:select class="ui dropdown search" name="provider"
                              from="${providers}"
                              optionKey="id"
                              optionValue="name"
                              value="${params.provider}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>

                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}"
                       class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
                    <input type="submit" class="ui primary button"
                           value="${message(code: 'default.button.filter.label')}">
                </div>
            </div>
        </g:form>
    </ui:filter>

    <g:form action="copySurvey" controller="survey" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, workFlow: '2']">
        <div class="subscription-results">
            <g:if test="${subscriptions}">
                <table class="ui celled sortable table la-js-responsive-table la-table">
                    <thead>
                    <tr>
                        <th rowspan="2" class="center aligned"></th>
                        <th rowspan="2" class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <g:sortableColumn params="${params}" property="s.name"
                                          title="${message(code: 'subscription.slash.name')}"
                                          rowspan="2"/>
                        <th rowspan="2">
                            ${message(code: 'license.details.linked_pkg')}
                        </th>

                        <g:sortableColumn params="${params}" property="orgRole§provider"
                                          title="${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}"
                                          rowspan="2"/>

                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.startDate"
                                          title="${message(code: 'default.startDate.label')}"/>


                        <th scope="col" rowspan="2">
                            <a href="#" class="la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.numberOfLicenses.label')}"
                               data-position="top center">
                                <i class="users large icon"></i>
                            </a>
                        </th>
                        <th scope="col" rowspan="2">
                            <a href="#" class="la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.numberOfCostItems.label')}"
                               data-position="top center">
                                <i class="money bill large icon"></i>
                            </a>
                        </th>

                        <th rowspan="2" class="two wide"></th>

                    </tr>

                    <tr>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.endDate"
                                          title="${message(code: 'default.endDate.label')}"/>
                    </tr>
                    </thead>
                    <g:each in="${subscriptions}" var="s" status="i">
                        <g:if test="${!s.instanceOf}">
                            <g:set var="childSubIds" value="${Subscription.executeQuery('select s.id from Subscription s where s.instanceOf = :parent',[parent:s])}"/>
                            <tr>
                                <td>
                                    <g:checkBox name="targetSubs" value="${s.id}" checked="false"/>
                                </td>
                                <td class="center aligned">
                                    ${(params.int('offset') ?: 0) + i + 1}
                                </td>
                                <td>
                                    <g:link controller="subscription" action="show" id="${s.id}">
                                        <g:if test="${s.name}">
                                            ${s.name}
                                        </g:if>
                                        <g:else>
                                            -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                                        </g:else>
                                        <g:if test="${s.instanceOf}">
                                            <g:if test="${s.consortia && s.consortia == institution}">
                                                ( ${s.subscriber.name} )
                                            </g:if>
                                        </g:if>
                                    </g:link>
                                    <g:if test="${sourceLicenses}">
                                        <g:each in="${sourceLicenses}" var="license">
                                            <g:link controller="license" action="show" target="_blank"
                                                    id="${license.id}">
                                                <div data-oid="${genericOIDService.getOID(license)}"
                                                     class="la-multi-sources">
                                                    <strong><i class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:
                                                    </strong>
                                                    ${license.reference}
                                                    <br />
                                                </div>
                                            </g:link>
                                        </g:each>
                                    </g:if>
                                </td>
                                <td>
                                <!-- packages -->
                                    <g:each in="${s.packages.sort { it.pkg.name }}" var="sp" status="ind">
                                        <g:if test="${ind < 10}">
                                            <div class="la-flexbox">
                                                <i class="icon gift la-list-icon"></i>
                                                <g:link controller="subscription" action="index" id="${s.id}"
                                                        params="[pkgfilter: sp.pkg.id]"
                                                        title="${sp.pkg.contentProvider?.name}">
                                                    ${sp.pkg.name}
                                                </g:link>
                                            </div>
                                        </g:if>
                                    </g:each>
                                    <g:if test="${s.packages.size() > 10}">
                                        <div>${message(code: 'myinst.currentSubscriptions.etc.label', args: [s.packages.size() - 10])}</div>
                                    </g:if>
                                <!-- packages -->
                                </td>
                                <%--
                                <td>
                                    ${s.type?.getI10n('value')}
                                </td>
                                --%>

                                <td>
                                <%-- as of ERMS-584, these queries have to be deployed onto server side to make them sortable --%>
                                    <g:each in="${s.providers}" var="org">
                                        <g:link controller="organisation" action="show"
                                                id="${org.id}">${org.name}</g:link><br />
                                    </g:each>
                                    <g:each in="${s.agencies}" var="org">
                                        <g:link controller="organisation" action="show"
                                                id="${org.id}">${org.name} (${message(code: 'default.agency.label')})</g:link><br />
                                    </g:each>
                                </td>
                                <td>
                                    <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br />
                                    <g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/>
                                </td>

                                <td>
                                    <g:link controller="subscription" action="members" params="${[id: s.id]}">
                                        ${childSubIds.size()}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link mapping="subfinance" controller="finance" action="index"
                                            params="${[sub: s.id]}">
                                        ${childSubIds.isEmpty() ? 0 : CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub.id in (:subs) and ci.owner = :context and ci.costItemStatus != :deleted',[subs:childSubIds, context:institution, deleted:RDStore.COST_ITEM_DELETED])[0]}
                                    </g:link>
                                </td>

                                <td class="x">
                                    <g:if test="${editable && accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")}">
                                        <g:link class="ui icon positive button la-popup-tooltip la-delay"
                                                data-content="${message(code: 'survey.toggleSurveySub.add.label', args: [SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransferIsNotNull(s).size(), SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransferIsNull(s).size()])}"
                                                controller="survey" action="copySurvey"
                                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubs: [s.id], workFlow: '2']">
                                            <g:message code="createSubscriptionSurvey.selectButton"/>
                                        </g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                </table>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br /><strong><g:message code="filter.result.empty.object"
                                           args="${[message(code: "subscription.plural")]}"/></strong>
                </g:if>
                <g:else>
                    <br /><strong><g:message code="result.empty.object"
                                           args="${[message(code: "subscription.plural")]}"/></strong>
                </g:else>
            </g:else>
        </div>

        <br />

        <div class="paginateButtons" style="text-align:center">
            <input type="submit"
                   value="${message(code: 'copySurvey.copyInSelectedSubs')}"
                   class="ui button"/>
        </div>

        <g:if test="${num_sub_rows}">
            <ui:paginate action="copySurvey" controller="survey" params="${params}"
                            max="${max}" total="${num_sub_rows}"/>
        </g:if>

    </g:form>
</g:if>

<laser:htmlEnd />