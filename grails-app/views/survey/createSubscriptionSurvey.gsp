<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.survey.SurveyConfig; de.laser.Subscription;de.laser.RefdataCategory; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.finance.CostItem" %>
<laser:htmlStart message="myinst.currentSubscriptions.label" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <ui:crumb message="createSubscriptionSurvey.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="createSubscriptionSurvey.label" type="Survey" />

<ui:messages data="${flash}"/>

<ui:msg class="info" showIcon="true" hideClose="true" message="allSubscriptions.info" />

<ui:h1HeaderWithIcon message="myinst.currentSubscriptions.label" total="${num_sub_rows}" floated="true" />

<ui:filter>
    <g:form action="createSubscriptionSurvey" controller="survey" method="get" class="ui small form">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <input type="hidden" name="id" value="${params.id}"/>

        <div class="three fields">
            <!-- 1-1 -->
            <div class="field">
                <label for="q">${message(code: 'default.search.text')}
                    <span data-position="right center" data-variation="tiny"
                          class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.subscription')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
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
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code: 'default.button.filter.label')}">
            </div>
        </div>
    </g:form>
</ui:filter>


<div class="subscription-results">
        <g:if test="${subscriptions}">
        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
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
                                  title="${message(code: 'provider.label')} / ${message(code: 'vendor.label')}"
                                  rowspan="2"/>

                <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.startDate"
                                  title="${message(code: 'default.startDate.label.shy')}"/>


                <th scope="col" rowspan="2">
                    <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code:'subscription.numberOfLicenses.label')}" data-position="top center">
                        <i class="users large icon"></i>
                    </a>
                </th>
                <th scope="col" rowspan="2">
                    <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.numberOfCostItems.label')}" data-position="top center">
                        <i class="${Icon.FNC.COST} large"></i>
                    </a>
                </th>

                <th rowspan="2" class="two wide">${message(code:'default.actions.label')}</th>
            </tr>

            <tr>
                <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.endDate"
                                  title="${message(code: 'default.endDate.label.shy')}"/>
            </tr>
            </thead>
            <g:each in="${subscriptions}" var="s" status="i">
                <g:if test="${!s.instanceOf}">
                    <g:set var="childSubIds" value="${Subscription.executeQuery('select s.id from Subscription s where s.instanceOf = :parent',[parent:s])}"/>
                    <tr>
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
                                        ( ${s.getSubscriberRespConsortia().name} )
                                    </g:if>
                                </g:if>
                            </g:link>
                            <g:each in="${allLinkedLicenses.get(s)}" var="license">
                                <div class="la-flexbox">
                                    <i class="${Icon.LICENSE} la-list-icon"></i>
                                    <g:link controller="license" action="show" id="${license.id}">${license.reference}</g:link><br />
                                </div>
                            </g:each>
                        </td>
                        <td>
                        <!-- packages -->
                            <g:each in="${s.packages.sort { it.pkg.name }}" var="sp" status="ind">
                                <g:if test="${ind < 10}">
                                    <div class="la-flexbox">
                                        <i class="${Icon.PACKAGE} la-list-icon"></i>
                                        <g:link controller="subscription" action="index" id="${s.id}"
                                                params="[pkgfilter: sp.pkg.id]"
                                                title="${sp.pkg.provider?.name}">
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
                                <g:link controller="organisation" action="show" id="${org.id}">${org.name}</g:link><br />
                            </g:each>
                            <g:each in="${s.vendors}" var="vendor">
                                <g:link controller="vendor" action="show"
                                        id="${vendor.id}">${vendor.name} (${message(code: 'vendor.label')})</g:link><br />
                            </g:each>
                        </td>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br />
                            <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label.shy')}:">
                                <g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/>
                            </span>
                        </td>
                            <td>
                                <g:link controller="subscription" action="members" params="${[id: s.id]}">
                                    ${childSubIds.size()}
                                </g:link>
                            </td>
                            <td>
                                <g:link mapping="subfinance" controller="finance" action="index"
                                        params="${[sub: s.id]}">
                                    ${childSubIds.isEmpty() ? 0 : CostItem.executeQuery('select count(*) from CostItem ci where ci.sub.id in (:subs) and ci.owner = :context and ci.costItemStatus != :deleted',[subs:childSubIds, context:institution, deleted:RDStore.COST_ITEM_DELETED])[0]}
                                </g:link>
                            </td>


                        <td class="x">
                            <g:if test="${editable && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )}">
                                    <g:link class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                            data-content="${message(code: 'survey.toggleSurveySub.add.label', args:[SurveyConfig.countBySubscriptionAndSubSurveyUseForTransfer(s, true), SurveyConfig.countBySubscriptionAndSubSurveyUseForTransfer(s, false)])}"
                                            controller="survey" action="addSubtoSubscriptionSurvey"
                                            params="[sub: s.id]">
                                        <i class="${Icon.CMD.EDIT}"></i>
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
                <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
            </g:if>
            <g:else>
                <br /><strong><g:message code="result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
            </g:else>
        </g:else>
    </div>



<g:if test="${true}">
    <ui:paginate action="createSubscriptionSurvey" controller="survey" params="${params}"
                    max="${max}" total="${num_sub_rows}"/>
</g:if>

<laser:htmlEnd />