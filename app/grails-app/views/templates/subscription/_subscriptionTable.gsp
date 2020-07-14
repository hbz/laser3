<%@ page import="de.laser.helper.ConfigUtils; com.k_int.kbplus.GenericOIDService; de.laser.interfaces.CalculatedType;de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem;com.k_int.kbplus.License;com.k_int.kbplus.Links" %>
<laser:serviceInjection />

<div class="subscription-results subscription-results la-clear-before">
    <g:if test="${subscriptions}">
        <%--<input type="submit"
                   value="${message(code: 'license.linking.submit')}"
                   class="ui primary button"/>--%>
            <table class="ui celled sortable table table-tworow la-table">
                <thead>
                    <tr>
                        <th scope="col" rowspan="2" class="center aligned">
                            ${message(code:'sidewide.number')}
                        </th>
                        <g:if test="${'showLicense' in tableConfig}">
                            <g:set var="subscriptionHeader" value="${message(code: 'subscription.slash.name')}"/>
                        </g:if>
                        <g:else>
                            <g:set var="subscriptionHeader" value="${message(code: 'subscription')}"/>
                        </g:else>
                        <g:sortableColumn params="${params}" property="s.name" title="${subscriptionHeader}" rowspan="2" scope="col" />
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
                        <g:sortableColumn scope="col" params="${params}" property="providerAgency" title="${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}" rowspan="2" />
                        <%--<th rowspan="2" >${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}</th>--%>
                        <%--
                        <g:if test="${params.orgRole == 'Subscription Consortia'}">
                            <th>${message(code: 'consortium.subscriber')}</th>
                        </g:if>
                        --%>
                        <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate" title="${message(code: 'default.startDate.label')}"/>
                        <g:if test="${params.orgRole in ['Subscription Consortia','Subscription Collective']}">
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
                        </g:if>
                        <g:if test="${!(contextService.getOrg().getCustomerType()  == 'ORG_CONSORTIUM')}">
                            <th class="la-no-uppercase" scope="col" rowspan="2" >
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </th>
                        </g:if>
                        <th scope="col" rowspan="2" class="two">${message(code:'default.actions.label')}</th>
                    </tr>
                    <tr>
                        <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate" title="${message(code: 'default.endDate.label')}"/>
                    </tr>
                </thead>
                <tbody>
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
                            <g:if test="${'showLicense' in tableConfig}">
                                <g:each in="${allLinkedLicenses.findAll { Links li -> li.destination == GenericOIDService.getOID(s)}}" var="row">
                                    <g:set var="license" value="${genericOIDService.resolveOID(row.source)}"/>
                                    <div class="la-flexbox">
                                        <i class="icon balance scale la-list-icon"></i>
                                        <g:link controller="license" action="show" id="${license.id}">${license.reference}</g:link><br>
                                    </div>
                                </g:each>
                            </g:if>
                        </td>
                        <td>
                        <!-- packages -->
                            <g:each in="${s.packages}" var="sp" status="ind">
                                <g:if test="${ind < 10}">
                                    <div class="la-flexbox">
                                        <i class="icon gift la-list-icon"></i>
                                        <g:link controller="subscription" action="index" id="${s.id}" params="[pkgfilter: sp.pkg.id]"
                                                title="${sp.pkg.contentProvider.name}">
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
                        <g:if test="${params.orgRole == 'Subscription Consortia'}">
                            <g:set var="childSubs" value="${Subscription.findAllByInstanceOf(s)}"/>
                            <td>
                                <g:if test="${childSubs.size() > 0}">
                                    <g:link controller="subscription" action="members" params="${[id:s.id]}">${childSubs.size()}</g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="subscription" action="addMembers" params="${[id:s.id]}">${childSubs.size()}</g:link>
                                </g:else>
                            </td>
                            <td>
                                <g:link mapping="subfinance" controller="finance" action="index" params="${[sub:s.id]}">
                                    <g:if test="${contextService.getOrg().getCustomerType()  == 'ORG_CONSORTIUM'}">
                                        ${CostItem.findAllBySubInListAndOwnerAndCostItemStatusNotEqual(childSubs, institution, RDStore.COST_ITEM_DELETED)?.size()}
                                    </g:if>
                                    <g:elseif test="${contextService.getOrg().getCustomerType() == 'ORG_INST_COLLECTIVE'}">
                                        ${CostItem.findAllBySubInListAndOwnerAndCostItemStatusNotEqualAndIsVisibleForSubscriber(childSubs, institution, RDStore.COST_ITEM_DELETED,true)?.size()}
                                    </g:elseif>
                                </g:link>
                            </td>
                        </g:if>
                        <g:if test="${!(contextService.getOrg().getCustomerType()  == 'ORG_CONSORTIUM')}">
                            <td>
                                <g:if test="${s.isMultiYear}">
                                    <g:if test="${(s.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL &&
                                            s.getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)}">
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
                            <g:if test="${'showActions' in tableConfig}">
                                <g:set var="surveysConsortiaSub" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s ,true)}" />
                                <g:set var="surveysSub" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s.instanceOf ,true)}" />
                                <g:if test="${contextService.org?.getCustomerType() in ['ORG_INST', 'ORG_BASIC_MEMBER'] && surveysSub && (surveysSub?.surveyInfo?.startDate <= new Date(System.currentTimeMillis())) }">
                                    <g:link controller="subscription" action="surveys" id="${s.id}"
                                            class="ui icon button">
                                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"                                           data-content="${message(code: "surveyConfig.subSurveyUseForTransfer.label.info3")}">
                                            <i class="ui icon envelope open"></i>
                                        </span>
                                    </g:link>
                                </g:if>
                                <g:if test="${contextService.org?.getCustomerType()  == 'ORG_CONSORTIUM' && surveysConsortiaSub }">
                                    <g:link controller="subscription" action="surveysConsortia" id="${s.id}"
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
                                                     base="${ConfigUtils.getStatsApiUrl()}"
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
                            </g:if>
                            <g:if test="${'showLinking' in tableConfig}">
                            <%--<g:if test="${license in s.licenses}"></g:if>--%>
                                <g:if test="${s in linkedSubscriptions}">
                                    <g:link class="ui icon negative button" action="linkToSubscription" params="${params+[id:license.id,unlink:true,subscription:s.id]}">
                                        <i class="ui minus icon"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link class="ui icon positive button" action="linkToSubscription" params="${params+[id:license.id,subscription:s.id]}">
                                        <i class="ui plus icon"></i>
                                    </g:link>
                                </g:else>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
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
    <semui:paginate action="${actionName}" controller="${controllerNamr}" params="${params}"
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