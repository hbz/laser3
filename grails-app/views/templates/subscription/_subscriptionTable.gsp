<%@ page import="de.laser.survey.SurveyConfig; de.laser.Subscription; de.laser.finance.CostItem; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.License;de.laser.Links" %>
<laser:serviceInjection />

<g:form action="compareSubscriptions" controller="compare" method="post">

<div class="subscription-results subscription-results la-clear-before">
    <g:if test="${subscriptions}">
        <%--<input type="submit"
                   value="${message(code: 'license.linking.submit')}"
                   class="ui primary button"/>--%>
            <table class="ui celled sortable table la-table la-js-responsive-table">
                <thead>
                    <tr>
                        <g:if test="${compare}">
                            <th scope="col" rowspan="2" class="center aligned">
                                <g:message code="default.compare.submit.label"/>
                            </th>
                        </g:if>
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
                        <g:if test="${params.orgRole in ['Subscriber'] && accessService.checkPerm("ORG_MEMBER_BASIC")}">
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
                        <g:if test="${params.orgRole in ['Subscription Consortia']}">
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
                        <g:if test="${!(institution.isCustomerType_Consortium())}">
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
                        <g:if test="${compare}">
                            <td class="center aligned">
                                <g:checkBox id="selectedObjects_${s.id}" name="selectedObjects" value="${s.id}" checked="false"/>
                            </td>
                        </g:if>
                        <td class="center aligned">
                            ${ (params.int('offset') ?: 0)  + i + 1 }
                        </td>
                        <th scope="row" class="la-th-column">
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
                                <%--<div id="${s.id}linkedLicenses">
                                    <laser:script file="${this.getGroovyPageFileName()}">
                                        $.ajax({
                                            url: "<g:createLink controller="ajaxJson" action="getLinkedLicenses" />",
                                            data: {
                                                subscription: "${genericOIDService.getOID(s)}"
                                            }
                                        }).done(function(data) {
                                                let link = "<g:createLink controller="license" action="show"/>";
                                                $.each(data.results,function(k,v) {
                                                    $("#${s.id}linkedLicenses").append('<i class="icon balance scale la-list-icon"></i><a href="'+link+'/'+v.id+'">'+v.name+'</a><br />');
                                                });
                                            });
                                    </laser:script>
                                </div>--%>
                                <g:each in="${allLinkedLicenses}" var="row">
                                    <g:if test="${s == row.destinationSubscription}">
                                        <g:set var="license" value="${row.sourceLicense}"/>
                                        <div class="la-flexbox la-minor-object">
                                            <i class="icon balance scale la-list-icon"></i>
                                            <g:link controller="license" action="show" id="${license.id}">${license.reference}</g:link><br />
                                        </div>
                                    </g:if>
                                </g:each>
                            </g:if>
                        </th>
                        <td>
                        <!-- packages -->
                            <g:each in="${s.packages}" var="sp" status="ind">
                                <g:if test="${ind < 10}">
                                    <div class="la-flexbox">
                                        <i class="icon gift la-list-icon"></i>
                                        <g:link controller="subscription" action="index" id="${s.id}" params="[pkgfilter: sp.pkg.id]"
                                                title="${sp.pkg.contentProvider?.name}">
                                            ${sp.pkg.name}
                                        </g:link>
                                    </div>
                                </g:if>
                            </g:each>
                            <g:if test="${s.packages.size() > 10}">
                                <div>${message(code: 'myinst.currentSubscriptions.etc.label', args: [s.packages.size() - 10])}</div>
                            </g:if>
                            <g:if test="${s.isEditableBy(user) && (s.packages == null || s.packages.size() == 0)}">
                                <i>
                                    <g:if test="${accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM_BASIC","INST_EDITOR","ROLE_ADMIN")}">
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
                        <g:if test="${params.orgRole == 'Subscriber'}">
                            <td>
                                <g:if test="${accessService.checkPerm("ORG_MEMBER_BASIC")}">
                                    ${s.getConsortia()?.name}
                                </g:if>
                            </td>
                        </g:if>
                        <td>
                            <%-- as of ERMS-584, these queries have to be deployed onto server side to make them sortable --%>
                            <g:each in="${s.providers}" var="org">
                                <g:link controller="organisation" action="show" id="${org.id}">${fieldValue(bean: org, field: "name")}
                                    <g:if test="${org.shortname}">
                                        <br />
                                        (${fieldValue(bean: org, field: "shortname")})
                                    </g:if>
                                </g:link><br />
                            </g:each>
                            <g:each in="${s.agencies}" var="org">
                                <g:link controller="organisation" action="show" id="${org.id}">
                                    ${fieldValue(bean: org, field: "name")}
                                    <g:if test="${org.shortname}">
                                        <br />
                                        (${fieldValue(bean: org, field: "shortname")})
                                    </g:if> (${message(code: 'default.agency.label')})
                                </g:link><br />
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
                            <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                            <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label')}:"><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></span>
                        </td>
                        <g:if test="${params.orgRole == 'Subscription Consortia'}">
                            <g:set var="childSubIds" value="${Subscription.executeQuery('select s.id from Subscription s where s.instanceOf = :parent',[parent:s])}"/>
                            <td>
                                <g:if test="${childSubIds.size() > 0}">
                                    <g:link controller="subscription" action="members" params="${[id:s.id]}">
                                        <div class="ui blue circular label">${childSubIds.size()}</div>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="subscription" action="addMembers" params="${[id:s.id]}">
                                        <div class="ui blue circular label">
                                            ${childSubIds.size()}
                                        </div>
                                    </g:link>
                                </g:else>
                            </td>
                            <td>
                                <g:link mapping="subfinance" controller="finance" action="index" params="${[sub:s.id]}">
                                    <g:if test="${institution.isCustomerType_Consortium()}">
                                        <div class="ui blue circular label">
                                            ${childSubIds.isEmpty() ? 0 : CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub.id in (:subs) and ci.owner = :context and ci.costItemStatus != :deleted',[subs:childSubIds, context:institution, deleted:RDStore.COST_ITEM_DELETED])[0]}
                                        </div>
                                    </g:if>
                                </g:link>
                            </td>
                        </g:if>
                        <g:if test="${!(institution.isCustomerType_Consortium())}">
                            <td>
                                <g:if test="${s.isMultiYear}">
                                    <g:if test="${(s.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL &&
                                            s._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)}">
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
                                <g:if test="${institution.isCustomerType_Inst() && s.instanceOf}">
                                    <g:set var="surveysSub" value="${SurveyConfig.executeQuery("select surConfig.id from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and surConfig.surveyInfo.type = :type and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                                            [sub: s.instanceOf, org: institution, invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY], type: [RDStore.SURVEY_TYPE_RENEWAL]])}" />
                                    <g:if test="${surveysSub}">
                                        <g:link controller="subscription" action="surveys" id="${s.id}"
                                                class="ui icon positive button la-modern-button">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: "surveyconfig.subSurveyUseForTransfer.label.info3")}">
                                                <i class="ui icon pie chart"></i>
                                            </span>
                                        </g:link>
                                    </g:if>
                                </g:if>
                                <g:if test="${institution.isCustomerType_Consortium()}">
                                    <g:set var="surveysConsortiaSub" value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s ,true)}" />
                                    <g:if test="${surveysConsortiaSub}">

                                            <g:if test="${surveysConsortiaSub.surveyInfo?.isCompletedforOwner()}">
                                                <g:link controller="subscription" action="surveysConsortia" id="${s.id}"
                                                        class="ui button positive icon la-modern-button">
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                          data-content="${message(code: "surveyconfig.isCompletedforOwner.true")}">
                                                        <i class="ui icon pie chart"></i>
                                                    </span>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="subscription" action="surveysConsortia" id="${s.id}"
                                                        class="ui button blue icon la-modern-button">
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                          data-content="${message(code: "surveyconfig.isCompletedforOwner.false")}">
                                                        <i class="ui icon pie chart"></i>
                                                    </span>
                                                </g:link>
                                            </g:else>

%{--                                        <g:link controller="subscription" action="surveysConsortia" id="${s.id}"
                                                class="ui button blue icon la-modern-button">
                                            <g:if test="${surveysConsortiaSub.surveyInfo?.isCompletedforOwner()}">
                                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                      data-content="${message(code: "surveyconfig.isCompletedforOwner.true")}">
                                                    <i class="ui icon pie chart blue"></i>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                      data-content="${message(code: "surveyconfig.isCompletedforOwner.false")}">
                                                    <i class="ui icon pie chart open"></i>
                                                </span>
                                            </g:else>
                                        </g:link>--}%
                                    </g:if>
                                </g:if>
                                <%--<g:if test="${statsWibid && (s.getCommaSeperatedPackagesIsilList()?.trim()) && s.hasPlatformWithUsageSupplierId()}">
                                    <ui:statsLink class="ui icon button la-modern-button"
                                                     base="${ConfigMapper.getStatsApiUrl()}"
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
                                    </ui:statsLink>
                                </g:if>--%>
                            </g:if>
                            <g:if test="${'showLinking' in tableConfig}">
                            <%--<g:if test="${license in s.licenses}"></g:if>--%>
                                <g:if test="${s in linkedSubscriptions}">
                                    <g:link class="ui icon negative button la-modern-button" action="linkToSubscription" params="${params+[id:license.id,unlink:true,subscription:s.id]}">
                                        <i class="ui minus icon"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link class="ui icon positive button la-modern-button" action="linkToSubscription" params="${params+[id:license.id,subscription:s.id]}">
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
            <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
        </g:if>
        <g:else>
            <br /><strong><g:message code="result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
        </g:else>
    </g:else>

</div>

    <g:if test="${compare}">
        <br />
        <input type="submit" class="ui button" value="${message(code:'menu.my.comp_sub')}" />
    </g:if>

</g:form>

<g:if test="${subscriptions}">
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    max="${max}" total="${num_sub_rows}" />
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
              // initialize the form and fields
              $('.ui.form').form();
            var val = "${params.dateBeforeFilter}";
            if(val == "null"){
                $(".dateBefore").addClass("hidden");
            }else{
                $(".dateBefore").removeClass("hidden");
            }

        $("[name='dateBeforeFilter']").change(function(){
            var val = $(this)['context']['selectedOptions'][0]['label'];

            if(val != "${message(code:'default.filter.date.none')}"){
                $(".dateBefore").removeClass("hidden");
            }else{
                $(".dateBefore").addClass("hidden");
            }
        })
</laser:script>