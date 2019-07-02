<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection/>
<!doctype html>

<r:require module="annotations"/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
        <semui:crumb controller="survey" action="surveyConfigs" id="${surveyInfo.id}" message="surveyConfigs.label"/>
    </g:if>
    <semui:crumb message="myinst.currentSubscriptions.label" class="active"/>
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


<div class="ui icon info message">
    <i class="info icon"></i>

    ${message(code: 'allSubscriptions.info')}
</div>


<h1 class="ui left aligned icon header"><semui:headerIcon/>${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}
<semui:totalNumber total="${num_sub_rows}"/>
</h1>

<semui:filter>
    <g:form action="allSubscriptions" controller="survey" method="get" class="form-inline ui small form">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <input type="hidden" name="id" value="${params.id}"/>

        <div class="three fields">
            <!-- 1-1 -->
            <div class="field">
                <label for="q">${message(code: 'default.search.text', default: 'Search text')}
                    <span data-position="right center" data-variation="tiny"
                          data-tooltip="${message(code: 'default.search.tooltip.subscription')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="q" name="q"
                           placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                           value="${params.q}"/>
                </div>
            </div>
            <!-- 1-2 -->
            <div class="field fieldcontain">
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn"
                                  placeholder="filter.placeholder" value="${validOn}"/>
            </div>


            <!-- TMP -->
            <%
                def fakeList = []
                fakeList.addAll(RefdataCategory.getAllRefdataValues('Subscription Status'))
                fakeList.add(RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null', 'filter.fake.values'))
                fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status'))
            %>

            <div class="field fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${fakeList}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">

            <!-- 2-1 + 2-2 -->
            <g:render template="../templates/properties/genericFilter" model="[propList: propList]"/>

            <!-- 2-3 -->
            <div class="field">
                <label>${message(code: 'subscription.form.label')}</label>
                <laser:select class="ui dropdown" name="form"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Form')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.form}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
            <!-- 2-4 -->
            <div class="field">
                <label>${message(code: 'subscription.resource.label')}</label>
                <laser:select class="ui dropdown" name="resource"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Resource')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.resource}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="two fields">
            <div class="field">
                <label>${message(code: 'menu.my.providers')}</label>
                <g:select class="ui dropdown" name="provider"
                          from="${providers}"
                          optionKey="id"
                          optionValue="name"
                          value="${params.provider}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <div class="two fields">

                    <div class="field la-field-right-aligned">
                        <a href="${request.forwardURI}"
                           class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button"
                               value="${message(code: 'default.button.filter.label', default: 'Filter')}">
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:filter>
<semui:form>
    <div class="subscription-results">
        <table class="ui celled sortable table table-tworow la-table">
            <thead>
            <tr>
                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <g:sortableColumn params="${params}" property="s.name" title="${message(code: 'license.slash.name')}"
                                  rowspan="2"/>
                <th rowspan="2">
                    ${message(code: 'license.details.linked_pkg', default: 'Linked Packages')}
                </th>

                <g:if test="${params.orgRole == 'Subscriber'}">
                    <th rowspan="2">${message(code: 'consortium', default: 'Consortia')}</th>
                </g:if>

                <g:sortableColumn params="${params}" property="orgRole§provider"
                                  title="${message(code: 'default.provider.label', default: 'Provider')} / ${message(code: 'default.agency.label', default: 'Agency')}"
                                  rowspan="2"/>

                <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.startDate"
                                  title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>


                <g:if test="${params.orgRole == 'Subscription Consortia'}">
                    <th rowspan="2">${message(code: 'subscription.numberOfLicenses.label', default: 'Number of ChildLicenses')}</th>
                    <th rowspan="2">${message(code: 'subscription.numberOfCostItems.label', default: 'Cost Items')}</th>
                </g:if>

                <th rowspan="2" class="two wide"></th>

            </tr>

            <tr>
                <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
            </tr>
            </thead>
            <g:each in="${subscriptions}" var="s" status="i">
                <g:if test="${true || !s.instanceOf}">
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
                                    -- ${message(code: 'myinst.currentSubscriptions.name_not_set', default: 'Name Not Set')}  --
                                </g:else>
                                <g:if test="${s.instanceOf}">
                                    <g:if test="${s.consortia && s.consortia == institution}">
                                        ( ${s.subscriber?.name} )
                                    </g:if>
                                </g:if>
                            </g:link>
                            <g:if test="${s.owner}">
                                <div class="la-flexbox">
                                    <i class="icon balance scale la-list-icon"></i>
                                    <g:link controller="license" action="show"
                                            id="${s.owner.id}">${s.owner?.reference ?: message(code: 'missingLicenseReference', default: '** No License Reference Set **')}</g:link>
                                </div>
                            </g:if>
                        </td>
                        <td>
                        <!-- packages -->
                            <g:each in="${s.packages.sort { it?.pkg?.name }}" var="sp" status="ind">
                                <g:if test="${ind < 10}">
                                    <div class="la-flexbox">
                                        <i class="icon gift la-list-icon"></i>
                                        <g:link controller="subscription" action="index" id="${s.id}"
                                                params="[pkgfilter: sp.pkg?.id]"
                                                title="${sp.pkg?.contentProvider?.name}">
                                            ${sp.pkg.name}
                                        </g:link>
                                    </div>
                                </g:if>
                            </g:each>
                            <g:if test="${s.packages.size() > 10}">
                                <div>${message(code: 'myinst.currentSubscriptions.etc.label', args: [s.packages.size() - 10])}</div>
                            </g:if>
                            <g:if test="${editable && (s.packages == null || s.packages.size() == 0)}">
                                <i>
                                    ${message(code: 'myinst.currentSubscriptions.no_links', default: 'None currently, Add packages via')}
                                    <g:link controller="subscription" action="linkPackage"
                                            id="${s.id}">${message(code: 'subscription.details.linkPackage.label', default: 'Link Package')}</g:link>
                                </i>
                            </g:if>
                        <!-- packages -->
                        </td>
                    <%--
                    <td>
                        ${s.type?.getI10n('value')}
                    </td>
                    --%>

                        <g:if test="${params.orgRole == 'Subscriber'}">
                            <td>
                                ${s.getConsortia()?.name}
                            </td>
                        </g:if>
                        <td>
                        <%-- as of ERMS-584, these queries have to be deployed onto server side to make them sortable --%>
                            <g:each in="${s.providers}" var="org">
                                <g:link controller="organisation" action="show" id="${org.id}">${org.name}</g:link><br/>
                            </g:each>
                            <g:each in="${s.agencies}" var="org">
                                <g:link controller="organisation" action="show"
                                        id="${org.id}">${org.name} (${message(code: 'default.agency.label', default: 'Agency')})</g:link><br/>
                            </g:each>
                        </td>
                        <%--
                        <td>
                            <g:if test="${params.orgRole == 'Subscription Consortia'}">
                                <g:each in="${s.getDerivedSubscribers()}" var="subscriber">
                                    <g:link controller="organisation" action="show" id="${subscriber.id}">${subscriber.name}</g:link> <br />
                                </g:each>
                            </g:if>
                        </td>
                        --%>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br>
                            <g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/>
                        </td>
                        <g:if test="${params.orgRole == 'Subscription Consortia'}">
                            <td>
                                <g:link controller="subscription" action="members" params="${[id: s.id]}">
                                    ${Subscription.findAllByInstanceOf(s)?.size()}
                                </g:link>
                            </td>
                            <td>
                                <g:link mapping="subfinance" controller="finance" action="index"
                                        params="${[sub: s.id]}">
                                    ${CostItem.findAllBySubInListAndOwner(Subscription.findAllByInstanceOf(s), institution)?.size()}
                                </g:link>
                            </td>
                        </g:if>


                        <td class="x">
                            <g:if test="${editable && accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")}">
                                <g:if test="${!surveyInfo?.surveyConfigs?.subscription?.id.contains(s.id)}">
                                    <g:link class="ui icon positive button"
                                            data-tooltip="${message(code: 'survey.toggleSurveySub.add.label')}"
                                            controller="survey" action="toggleSurveySub"
                                            params="${params + [direction: 'add', sub: s.id]}">
                                        <i class="plus icon"></i>
                                    </g:link>
                                </g:if>
                                <g:elseif test="${surveyInfo?.surveyConfigs?.subscription?.id.contains(s.id)}">
                                    <g:link class="ui icon negative button"
                                            data-tooltip="${message(code: 'survey.toggleSurveySub.remove.label')}"
                                            controller="survey" action="toggleSurveySub"
                                            params="${params + [direction: 'remove', sub: s.id]}">
                                        <i class="minus icon"></i>
                                    </g:link>
                                </g:elseif>
                            </g:if>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </table>
    </div>

</semui:form>

<g:if test="${true}">
    <semui:paginate action="allSubscriptions" controller="survey" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${num_sub_rows}"/>
</g:if>

</body>
</html>