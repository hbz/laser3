<%@ page import="de.laser.Subscription; de.laser.RefdataCategory; de.laser.helper.RDStore;de.laser.helper.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.finance.CostItem" %>
<laser:serviceInjection/>
<!doctype html>



<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.currentSubscriptions.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <semui:crumb message="createIssueEntitlementsSurvey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon
        type="Survey"/>${message(code: 'createIssueEntitlementsSurvey.label')}</h1>


<semui:messages data="${flash}"/>


<div class="ui icon info message">
    <i class="info icon"></i>
    ${message(code: 'allSubscriptions.info2')}
</div>


<h2 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>${message(code: 'myinst.currentSubscriptions.label')}
<semui:totalNumber total="${num_sub_rows}"/>
</h2>

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="createIssueEntitlementsSurvey" controller="survey" method="get" class="ui small form">
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
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn"
                                  placeholder="filter.placeholder" value="${validOn}"/>
            </div>

            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">

            <!-- 2-1 + 2-2 -->
            <g:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>

            <!-- 2-3 -->
            <div class="field">
                <label>${message(code: 'subscription.form.label')}</label>
                <laser:select class="ui dropdown" name="form"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.form}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
            <!-- 2-4 -->
            <div class="field">
                <label>${message(code: 'subscription.resource.label')}</label>
                <laser:select class="ui dropdown" name="resource"
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
                           class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button"
                               value="${message(code: 'default.button.filter.label')}">

            </div>
        </div>
    </g:form>
</semui:filter>

<div class="subscription-results">
    <g:if test="${subscriptions}">
        <table class="ui celled sortable table table-tworow la-js-responsive-table la-table">
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
                                  title="${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}"
                                  rowspan="2"/>

                <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.startDate"
                                  title="${message(code: 'default.startDate.label')}"/>


                <th scope="col" rowspan="2">
                    <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code:'subscription.numberOfLicenses.label')}" data-position="top center">
                        <i class="users large icon"></i>
                    </a>
                </th>
                <th scope="col" rowspan="2">
                    <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.numberOfCostItems.label')}" data-position="top center">
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
                            <g:each in="${allLinkedLicenses.get(s)}" var="license">
                                <div class="la-flexbox">
                                    <i class="icon balance scale la-list-icon"></i>
                                    <g:link controller="license" action="show" id="${license.id}">${license.reference}</g:link><br />
                                </div>
                            </g:each>
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
                                <g:link controller="organisation" action="show" id="${org.id}">${org.name}</g:link><br />
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
                                            controller="survey" action="addSubtoIssueEntitlementsSurvey"
                                            params="[sub: s.id]">
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
                    <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
                </g:if>
                <g:else>
                    <br /><strong><g:message code="result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
                </g:else>
            </g:else>
    </div>



<g:if test="${true}">
    <semui:paginate action="createSubscriptionSurvey" controller="survey" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${num_sub_rows}"/>
</g:if>

</body>
</html>