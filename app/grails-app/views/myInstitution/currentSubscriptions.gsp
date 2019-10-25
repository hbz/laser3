<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection />
<!doctype html>

<%-- r:require module="annotations" / --%>

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <g:if test="${filterSet || defaultSet}">
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[exportXLS:true]}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
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

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}
            <semui:totalNumber total="${num_sub_rows}"/>
        </h1>

<semui:filter>
    <g:form action="currentSubscriptions" controller="myInstitution" method="get" class="ui small form">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <div class="three fields">
        %{--<div class="four fields">--}%
            <!-- 1-1 -->
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text', default: 'Search text')}
                    <span data-position="right center" data-variation="tiny"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.search.tooltip.subscription')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                           value="${params.q}"/>
                </div>
            </div>
            <!-- 1-2 -->
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" data-variation="tiny"  class="la-popup-tooltip la-delay" data-content="Lizenz, Vertrag, Paket, Titel">
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
                fakeList.addAll(RefdataCategory.getAllRefdataValues('Subscription Status'))
                //fakeList.add(RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null', 'filter.fake.values'))
                fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status'))
            %>

            <div class="field fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.status.label')}</label>
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
                              from="${RefdataCategory.getAllRefdataValues('Subscription Status')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.consortium}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>

            </div>
            <!-- 2-2 -->
            <div class="field disabled fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.status.label')}</label>
                <laser:select name="status" class="ui dropdown"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Status')}"
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
                              from="${RefdataCategory.getAllRefdataValues('Subscription Form')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.form}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <!-- 2-4 -->
            <div class="field">
                <label>${message(code:'subscription.resource.label')}</label>
                <laser:select class="ui fluid dropdown" name="resource"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Resource')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.resource}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="field">
                <label for="subscritionType">${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>

                <fieldset id="subscritionType">
                    <div class="inline fields la-filter-inline">
                        <%
                            List subTypes = RefdataCategory.getAllRefdataValues('Subscription Type')
                            boolean orgHasAdministrativeSubscriptions = OrgRole.executeQuery('select oo.sub from OrgRole oo where oo.org = :context and oo.roleType = :consortium and oo.sub.administrative = true',[context:institution,consortium:RDStore.OR_SUBSCRIPTION_CONSORTIA])
                            if(accessService.checkPermAffiliation("ORG_CONSORTIUM","INST_USER")) {
                                subTypes -= RDStore.SUBSCRIPTION_TYPE_LOCAL
                            }
                            if(!orgHasAdministrativeSubscriptions) {
                                subTypes -= RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE
                            }
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

        <div class="two fields">
            <div class="field">
                <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                <%--
                <g:if test="${params.orgRole == 'Subscriber'}">
                    <input id="radioSubscriber" type="hidden" value="Subscriber" name="orgRole" tabindex="0" class="hidden">
                </g:if>
                <g:if test="${params.orgRole == 'Subscription Consortia'}">
                    <input id="radioKonsortium" type="hidden" value="Subscription Consortia" name="orgRole" tabindex="0" class="hidden">
                </g:if>
                --%>

                <label>${message(code: 'myinst.currentSubscriptions.filter.filterForRole.label')}</label>

                <div class="inline fields la-filter-inline">
                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioSubscriber" type="radio" value="Subscriber" name="orgRole" tabindex="0" class="hidden"
                                   <g:if test="${params.orgRole == 'Subscriber'}">checked=""</g:if>
                                >
                            <label for="radioSubscriber">
                                <g:message code="subscription.details.consortiaMembers.label"/>
                            </label>
                        </div>
                    </div>
                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioKonsortium" type="radio" value="Subscription Consortia" name="orgRole" tabindex="0" class="hidden"
                                   <g:if test="${params.orgRole == 'Subscription Consortia'}">checked=""</g:if>
                            >
                            <label for="radioKonsortium">${message(code: 'myinst.currentSubscriptions.filter.consortium.label')}</label>
                        </div>
                    </div>
                </div>
                </g:if>
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
                        %{--<div class="ui checkbox">
                            <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                            <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox" value="${params.subRunTime}"
                                   tabindex="0">
                        </div>--}%
                    </div>
                </div>
            </div>

            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
            </div>
        </div>

    </g:form>
</semui:filter>

<div class="subscription-results">
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
                ${message(code: 'myinst.currentSubscriptions.subscription_type', default: 'Subscription Type')}
            </th>
            */ %>

            <g:if test="${params.orgRole in ['Subscriber', 'Subscription Collective'] && accessService.checkPerm("ORG_BASIC_MEMBER")}">
                <th scope="col" rowspan="2" >${message(code: 'consortium')}</th>
            </g:if>
            <g:elseif test="${params.orgRole == 'Subscriber'}">
                <th rowspan="2">${message(code:'org.institution.label')}</th>
            </g:elseif>

            <g:sortableColumn scope="col" params="${params}" property="orgRole§provider" title="${message(code: 'default.provider.label', default: 'Provider')} / ${message(code: 'default.agency.label', default: 'Agency')}" rowspan="2" />
            <%--<th rowspan="2" >${message(code: 'default.provider.label', default: 'Provider')} / ${message(code: 'default.agency.label', default: 'Agency')}</th>--%>

            <%--
            <g:if test="${params.orgRole == 'Subscription Consortia'}">
                <th>${message(code: 'consortium.subscriber', default: 'Subscriber')}</th>
            </g:if>
            --%>
            <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate" title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>


            <g:if test="${params.orgRole in ['Subscription Consortia','Subscription Collective']}">
                <th scope="col" rowspan="2" >${message(code: 'subscription.numberOfLicenses.label')}</th>
                <th scope="col" rowspan="2" >${message(code: 'subscription.numberOfCostItems.label')}</th>
            </g:if>
            <% /* <g:sortableColumn params="${params}" property="s.manualCancellationDate"
                              title="${message(code: 'default.cancellationDate.label')}"/> */ %>
            <th scope="col" rowspan="2" class="two">${message(code:'default.actions')}</th>
        </tr>

        <tr>
            <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate" title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
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
                        <g:link controller="organisation" action="show" id="${org.id}">${org.name} (${message(code: 'default.agency.label', default: 'Agency')})</g:link><br />
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
                            ${CostItem.findAllBySubInListAndOwner(Subscription.findAllByInstanceOf(s), institution)?.size()}
                        </g:link>
                    </td>
                </g:if>
                <td class="x">

                    <g:set var="surveysConsortiaSub" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndIsSubscriptionSurveyFix(s ,true)}" />
                    <g:set var="surveysSub" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndIsSubscriptionSurveyFix(s.instanceOf ,true)}" />

                    <g:if test="${contextService.org?.getCustomerType() in ['ORG_INST', 'ORG_BASIC_MEMBER'] && surveysSub && (surveysSub?.surveyInfo?.startDate <= new Date(System.currentTimeMillis())) }">

                        <g:link controller="subscription" action="surveys" id="${s?.id}"
                                class="ui icon button">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: "surveyConfig.isSubscriptionSurveyFix.label.info3")}">
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

                            <g:if test="${CostItem.findBySub(s) || CostItem.findAllBySubInListAndOwner(Subscription.findAllByInstanceOfAndStatusNotEqual(s, RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')), institution)}">
                                <span data-position="top right" data-content="${message(code:'subscription.delete.existingCostItems')}">
                                    <button class="ui icon button negative" disabled="disabled">
                                        <i class="trash alternate icon"></i>
                                    </button>
                                </span>
                            </g:if>
                            <g:else>
                                <g:link class="ui icon negative button js-open-confirm-modal"
                                        data-confirm-term-what="subscription"
                                        data-confirm-term-what-detail="${s.name}"
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
            <br><strong><g:message code="result.empty.object" args="${message(code:"subscription.plural")}"/></strong>
        </g:else>
    </g:else>

</div>

    <g:if test="${subscriptions}">
        <semui:paginate action="currentSubscriptions" controller="myInstitution" params="${params}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
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


  </body>
</html>
