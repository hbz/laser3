<%@ page import="de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection />
<!doctype html>

<%-- r:require module="annotations" / --%>

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser')} : ${message(code:'myinst.currentSubscriptions.label')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <g:if test="${filterSet || defaultSet}">
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[exportXLS:true]}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[format:'csv']}">
                            ${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:if>
                <g:else>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                    </semui:exportDropdownItem>
                </g:else>
            </semui:exportDropdown>

            <g:if test="${accessService.checkPermX('ORG_INST,ORG_CONSORTIUM', 'ROLE_ADMIN')}">
                <g:render template="actions" />
            </g:if>

        </semui:controlButtons>

        <semui:messages data="${flash}"/>

        <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'myinst.currentSubscriptions.label')}
            <semui:totalNumber total="${num_sub_rows}"/>
        </h1>

    <g:render template="../templates/filter/javascript" />
    <semui:filter showFilterButton="true">
    <g:form action="currentSubscriptions" controller="myInstitution" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <div class="three fields">
        %{--<div class="four fields">--}%
            <!-- 1-1 -->
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay" data-content="${message(code:'default.search.tooltip.subscription')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.q}"/>
                </div>
            </div>
            <!-- 1-2 -->
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay" data-content="${message(code:'default.search.tooltip.subscription.identifier')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="identifier" name="identifier"
                           placeholder="${message(code: 'default.search.identifier.ph')}"
                           value="${params.identifier}"/>
                </div>
            </div>
            <!-- 1-3 -->
            <div class="field fieldcontain">
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>
            <% /*
            <!-- 1-4 -->
            <div class="field disabled fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.renewalDate.label"  id="renewalDate" name="renewalDate"
                                  placeholder="filter.placeholder" value="${params.renewalDate}"/>
            </div>
            <!-- 1-5 -->
            <div class="field disabled fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.durationDateEnd.label"
                                  id="durationDate" name="durationDate" placeholder="filter.placeholder" value="${params.durationDate}"/>
            </div>
            */ %>

            <!-- TMP -->
            <%
                def fakeList = []
                fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                //fakeList.add(RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null', 'filter.fake.values'))
                fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS))
            %>

            <div class="field fieldcontain">
                <label>${message(code: 'default.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${ fakeList }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">

            <!-- 2-1 + 2-2 -->
            <g:render template="../templates/properties/genericFilter" model="[propList: propList]"/>
<%--
            <!-- 2-1 -->
            <div class="field disabled fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.consortium.label')}</label>
                <laser:select name="status" class="ui dropdown"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.consortium}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>

            </div>
            <!-- 2-2 -->
            <div class="field disabled fieldcontain">
                <label>${message(code: 'default.status.label')}</label>
                <laser:select name="status" class="ui dropdown"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

           --%>
            <!-- 2-3 -->
            <div class="field">
                <label>${message(code:'subscription.form.label')}</label>
                <laser:select class="ui fluid dropdown" name="form"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.form}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <!-- 2-4 -->
            <div class="field">
                <label>${message(code:'subscription.resource.label')}</label>
                <laser:select class="ui fluid dropdown" name="resource"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.resource}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="three fields">

            <div class="field">
                <fieldset id="subscritionType">
                        <legend >${message(code: 'myinst.currentSubscriptions.subscription_type')}</legend>
                        <div class="inline fields la-filter-inline">
                            <%
                                List subTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE)

                                if(accessService.checkPermAffiliation("ORG_CONSORTIUM","INST_USER")) {
                                    subTypes -= RDStore.SUBSCRIPTION_TYPE_LOCAL
                                    //TODO [ticket=2276] provisoric, name check is in order to prevent id mismatch
                                }

                                if(institution.globalUID != com.k_int.kbplus.Org.findByName('LAS:eR Backoffice').globalUID)
                                    subTypes -= RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE
                            %>
                            <g:each in="${subTypes}" var="subType">
                                <div class="inline field">
                                    <div class="ui checkbox">
                                        <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
                                        <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox" value="${subType.id}"
                                            <g:if test="${params.list('subTypes').contains(subType.id.toString())}"> checked="" </g:if>
                                               tabindex="0">
                                    </div>
                                </div>
                            </g:each>
                        </div>
                    </fieldset>
            </div>
            <div class="field">
                <legend >${message(code: 'myinst.currentSubscriptions.subscription_kind')}</legend>
                <select id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND).sort{it.getI10n('value')}}" var="subKind">
                        <option <%=(params.list('subKinds').contains(subKind.id.toString())) ? 'selected="selected"' : ''%>
                        value="${subKind.id}" ">
                        ${subKind.getI10n('value')}
                        </option>
                    </g:each>
                </select>

            </div>

            <div class="field">
                <label>${message(code: 'myinst.currentSubscriptions.subscription.runTime')}</label>
                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.multiYear')}</label>
                            <input id="checkSubRunTimeMultiYear" name="subRunTimeMultiYear" type="checkbox" <g:if test="${params.subRunTimeMultiYear}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                            <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox" <g:if test="${params.subRunTime}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <g:if test="${accessService.checkPerm("ORG_INST")}">
            <div class="four fields">
        </g:if>
        <g:else>
            <div class="three fields">
        </g:else>
            <div class="field">
                <label>${message(code:'subscription.isPublicForApi.label')}</label>
                <laser:select class="ui fluid dropdown" name="isPublicForApi"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.isPublicForApi}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code:'subscription.hasPerpetualAccess.label')}</label>
                <laser:select class="ui fluid dropdown" name="hasPerpetualAccess"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPerpetualAccess}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

            <g:if test="${accessService.checkPerm("ORG_INST")}">
            <div class="field">
                <fieldset>
                    <legend id="la-legend-searchDropdown">${message(code: 'gasco.filter.consortialAuthority')}</legend>

                    <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown"
                              optionKey="${{ "com.k_int.kbplus.Org:" + it.id }}"
                              optionValue="${{ it.getName() }}"
                              name="consortia"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              value="${params.consortia}"/>
                </fieldset>
            </div>
            </g:if>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}">
            </div>

        </div>

    </g:form>
</semui:filter>

<div class="subscription-results subscription-results la-clear-before">
<g:if test="${subscriptions}">
    <table class="ui celled sortable table table-tworow la-table">
        <thead>
        <tr>
            <th scope="col" rowspan="2" class="center aligned">
                ${message(code:'sidewide.number')}
            </th>
            <g:sortableColumn params="${params}" property="s.name" title="${message(code: 'subscription.slash.name')}" rowspan="2" scope="col" />
            <th rowspan="2" scope="col">
                ${message(code: 'license.details.linked_pkg')}
            </th>
            <% /*
            <th>
                ${message(code: 'myinst.currentSubscriptions.subscription_type', default: RDConstants.SUBSCRIPTION_TYPE)}
            </th>
            */ %>

            <g:if test="${params.orgRole in ['Subscriber', 'Subscription Collective'] && accessService.checkPerm("ORG_BASIC_MEMBER")}">
                <th scope="col" rowspan="2" >${message(code: 'consortium')}</th>
            </g:if>
            <g:elseif test="${params.orgRole == 'Subscriber'}">
                <th rowspan="2">${message(code:'org.institution.label')}</th>
            </g:elseif>

            <g:sortableColumn scope="col" params="${params}" property="orgRole§provider" title="${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}" rowspan="2" />
            <%--<th rowspan="2" >${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}</th>--%>

            <%--
            <g:if test="${params.orgRole == 'Subscription Consortia'}">
                <th>${message(code: 'consortium.subscriber')}</th>
            </g:if>
            --%>
            <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate" title="${message(code: 'default.startDate.label')}"/>


            <g:if test="${params.orgRole in ['Subscription Consortia','Subscription Collective']}">
                <th scope="col" rowspan="2" >${message(code: 'subscription.numberOfLicenses.label')}</th>
                <th scope="col" rowspan="2" >${message(code: 'subscription.numberOfCostItems.label')}</th>
            </g:if>

            <g:if test="${!(contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY'])}">
            <th class="la-no-uppercase" scope="col" rowspan="2" >
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${message(code: 'subscription.isMultiYear.label')}">
                 <i class="map orange icon"></i>
                </span>
            </th>

            </g:if>

            <% /* <g:sortableColumn params="${params}" property="s.manualCancellationDate"
                              title="${message(code: 'default.cancellationDate.label')}"/> */ %>
            <th scope="col" rowspan="2" class="two">${message(code:'default.actions.label')}</th>
        </tr>

        <tr>
            <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate" title="${message(code: 'default.endDate.label')}"/>
        </tr>
        </thead>
        <g:each in="${subscriptions}" var="s" status="i">
            <tr>
                <td class="center aligned">
                    ${ (params.int('offset') ?: 0)  + i + 1 }
                </td>
                <td>
                    <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}">
                        <g:if test="${s.name}">
                            ${s.name}
                        </g:if>
                        <g:else>
                            -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
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
                            <g:link controller="license" action="show" id="${s.owner.id}">${s.owner?.reference?:message(code:'missingLicenseReference', default:'** No License Reference Set **')}</g:link>
                        </div>
                    </g:if>
                </td>
                <td>
                <!-- packages -->
                    <g:each in="${s.packages.sort{it?.pkg?.name}}" var="sp" status="ind">
                        <g:if test="${ind < 10}">
                            <div class="la-flexbox">
                                <i class="icon gift la-list-icon"></i>
                                <g:link controller="subscription" action="index" id="${s.id}" params="[pkgfilter: sp.pkg?.id]"
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
                            <g:if test="${accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM","INST_EDITOR","ROLE_ADMIN")}">
                                <g:message code="myinst.currentSubscriptions.no_links" />
                                <g:link controller="subscription" action="linkPackage"
                                        id="${s.id}">${message(code: 'subscription.details.linkPackage.label')}</g:link>
                            </g:if>
                            <g:else>
                                <g:message code="myinst.currentSubscriptions.no_links_basic" />
                            </g:else>
                        </i>
                    </g:if>
                <!-- packages -->
                </td>
                <%--<td>
                    ${s.type?.getI10n('value')}
                </td>--%>
                <g:if test="${params.orgRole in ['Subscriber', 'Subscription Collective']}">
                    <td>
                        <g:if test="${accessService.checkPerm("ORG_BASIC_MEMBER")}">
                            ${s.getConsortia()?.name}
                        </g:if>
                        <g:else>
                            ${s.getCollective()?.name}
                        </g:else>
                    </td>
                </g:if>
                <td>
                <%-- as of ERMS-584, these queries have to be deployed onto server side to make them sortable --%>
                    <g:each in="${s.providers}" var="org">
                        <g:link controller="organisation" action="show" id="${org.id}">${org.name}</g:link><br />
                    </g:each>
                    <g:each in="${s.agencies}" var="org">
                        <g:link controller="organisation" action="show" id="${org.id}">${org.name} (${message(code: 'default.agency.label')})</g:link><br />
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
                <g:if test="${params.orgRole in ['Subscription Consortia','Subscription Collective']}">
                    <td>
                        <g:link controller="subscription" action="members" params="${[id:s.id]}">${Subscription.findAllByInstanceOf(s)?.size()}</g:link>
                    </td>
                    <td>
                        <g:link mapping="subfinance" controller="finance" action="index" params="${[sub:s.id]}">
                            <g:if test="${contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']}">
                                ${CostItem.findAllBySubInListAndOwnerAndCostItemStatusNotEqual(Subscription.findAllByInstanceOf(s), institution, RDStore.COST_ITEM_DELETED)?.size()}
                            </g:if>
                            <g:elseif test="${contextService.getOrg().getCustomerType() == 'ORG_INST_COLLECTIVE'}">
                                ${CostItem.findAllBySubInListAndOwnerAndCostItemStatusNotEqualAndIsVisibleForSubscriber(Subscription.findAllByInstanceOf(s), institution, RDStore.COST_ITEM_DELETED,true)?.size()}
                            </g:elseif>
                        </g:link>
                    </td>
                </g:if>
                <g:if test="${!(contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY'])}">
                    <td>
                        <g:if test="${s.isMultiYear}">
                            <g:if test="${(s.type == de.laser.helper.RDStore.SUBSCRIPTION_TYPE_CONSORTIAL &&
                                    s.getCalculatedType() == de.laser.interfaces.TemplateSupport.CALCULATED_TYPE_PARTICIPATION)}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </g:else>
                        </g:if>
                    </td>
                </g:if>
                <td class="x">

                    <g:set var="surveysConsortiaSub" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s ,true)}" />
                    <g:set var="surveysSub" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s.instanceOf ,true)}" />

                    <g:if test="${contextService.org?.getCustomerType() in ['ORG_INST', 'ORG_BASIC_MEMBER'] && surveysSub && (surveysSub?.surveyInfo?.startDate <= new Date(System.currentTimeMillis())) }">

                        <g:link controller="subscription" action="surveys" id="${s?.id}"
                                class="ui icon button">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: "surveyConfig.subSurveyUseForTransfer.label.info3")}">
                            <i class="ui icon envelope open"></i>
                        </span>
                        </g:link>
                    </g:if>

                    <g:if test="${contextService.org?.getCustomerType() in ['ORG_CONSORTIUM_SURVEY', 'ORG_CONSORTIUM'] && surveysConsortiaSub }">
                        <g:link controller="subscription" action="surveysConsortia" id="${s?.id}"
                                class="ui icon button">
                            <g:if test="${surveysConsortiaSub?.surveyInfo?.isCompletedforOwner()}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyConfig.isCompletedforOwner.true")}">
                                    <i class="ui icon envelope green"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: "surveyConfig.isCompletedforOwner.false")}">
                                    <i class="ui icon envelope open"></i>
                                </span>
                            </g:else>
                        </g:link>
                    </g:if>
                    <g:if test="${statsWibid && (s.getCommaSeperatedPackagesIsilList()?.trim()) && s.hasPlatformWithUsageSupplierId()}">
                        <laser:statsLink class="ui icon button"
                                         base="${grailsApplication.config.statsApiUrl}"
                                         module="statistics"
                                         controller="default"
                                         action="select"
                                         target="_blank"
                                         params="[mode:usageMode,
                                                  packages:s.getCommaSeperatedPackagesIsilList(),
                                                  institutions:statsWibid
                                         ]"
                                         title="Springe zu Statistik im Nationalen Statistikserver"> <!-- TODO message -->
                            <i class="chart bar outline icon"></i>
                        </laser:statsLink>
                    </g:if>
                    <%--<g:if test="${ contextService.getUser().isAdmin() || contextService.getUser().isYoda() ||
                        (editable && (OrgRole.findAllByOrgAndSubAndRoleType(institution, s, RDStore.OR_SUBSCRIBER) || s.consortia?.id == institution?.id))
                        }">
                        <g:if test="${editable && ((institution?.id in s.allSubscribers.collect{ it.id }) || s.consortia?.id == institution?.id)}">--%>

                        <%-- ERMS-1348 removing delete buttons
                        <g:if test="${editable && accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM","INST_EDITOR","ROLE_ADMIN")}">

                            <g:if test="${CostItem.findBySub(s) || CostItem.findAllBySubInListAndOwner(Subscription.findAllByInstanceOfAndStatusNotEqual(s, RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS)), institution)}">
                                <span data-position="top right" data-content="${message(code:'subscription.delete.existingCostItems')}">
                                    <button class="ui icon button negative" disabled="disabled">
                                        <i class="trash alternate icon"></i>
                                    </button>
                                </span>
                            </g:if>
                            <g:else>
                                <g:link class="ui icon negative button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.subscription", args: [s.name])}"
                                        data-confirm-term-how="delete"
                                        controller="myInstitution" action="actionCurrentSubscriptions"
                                        params="${[curInst: institution.id, basesubscription: s.id]}">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:else>
                        </g:if>
                     --%>
                </td>
            </tr>
        </g:each>
    </table>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br><strong><g:message code="filter.result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br><strong><g:message code="result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
    </g:else>
</g:else>

</div>

    <g:if test="${subscriptions}">
        <semui:paginate action="currentSubscriptions" controller="myInstitution" params="${params}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}" max="${max}"
                        total="${num_sub_rows}"/>
    </g:if>

    <r:script>
        $(document).ready(function(){
              // initialize the form and fields
              $('.ui.form')
              .form();
            var val = "${params.dateBeforeFilter}";
            if(val == "null"){
                $(".dateBefore").addClass("hidden");
            }else{
                $(".dateBefore").removeClass("hidden");
            }
        });

        $("[name='dateBeforeFilter']").change(function(){
            var val = $(this)['context']['selectedOptions'][0]['label'];

            if(val != "${message(code:'default.filter.date.none', default:'-None-')}"){
                $(".dateBefore").removeClass("hidden");
            }else{
                $(".dateBefore").addClass("hidden");
            }
        })
    </r:script>

    <%--
    <r:script>

        function availableTypesSelectUpdated(optionSelected) {

            var selectedOption = $( "#availablePropertyTypes option:selected" )
            var selectedValue = selectedOption.val()

            if (selectedValue) {
                //Set the value of the hidden input, to be passed on controller
                $('#propertyFilterType').val(selectedOption.text())

                updateInputType(selectedValue)
            }
        }

        function updateInputType(selectedValue) {
            //If we are working with RefdataValue, grab the values and create select box
            if(selectedValue.indexOf("RefdataValue") != -1) {
                var refdataType = selectedValue.split("&&")[1]
                $.ajax({
                    url:'<g:createLink controller="ajax" action="sel2RefdataSearch"/>'+'/'+refdataType+'?format=json',
                    success: function(data) {
                        var select = ' <select id="propertyFilter" name="propertyFilter" > '
                        //we need empty when we dont want to search by property
                        select += ' <option></option> '
                        for (var index=0; index < data.length; index++ ) {
                            var option = data[index]
                            select += ' <option value="'+option.text+'">'+option.text+'</option> '
                        }
                        select += '</select>'
                        $('#propertyFilter').replaceWith(select)
                    },async:false
                });
            }else{
                //If we dont have RefdataValues,create a simple text input
                $('#propertyFilter').replaceWith('<input id="propertyFilter" type="text" name="propertyFilter" placeholder="${message(code:'license.search.property.ph', default:'property value')}" />')
            }
        }

        function setTypeAndSearch(){
            var selectedType = $("#propertyFilterType").val()
            //Iterate the options, find the one with the text we want and select it
            var selectedOption = $("#availablePropertyTypes option").filter(function() {
                return $(this).text() == selectedType ;
            }).prop('selected', true); //This will trigger a change event as well.


            //Generate the correct select box
            availableTypesSelectUpdated(selectedOption)

            //Set selected value for the actual search
            var paramPropertyFilter = "${params.propertyFilter}";
            var propertyFilterElement = $("#propertyFilter");
            if(propertyFilterElement.is("input")){
                propertyFilterElement.val(paramPropertyFilter);
            }
            else {
                $("#propertyFilter option").filter(function() {
                    return $(this).text() == paramPropertyFilter ;
                }).prop('selected', true);
            }
        }

        $('#availablePropertyTypes').change(function(e) {
            var optionSelected = $("option:selected", this);
            availableTypesSelectUpdated(optionSelected);
        });

        window.onload = setTypeAndSearch()
    </r:script>
    --%>

    <semui:debugInfo>
        <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>

  </body>
</html>
