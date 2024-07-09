<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.survey.SurveyConfig; de.laser.Subscription; de.laser.finance.CostItem; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.License;de.laser.Links" %>
<laser:serviceInjection />

<g:form action="compareSubscriptions" controller="compare" method="post">

<div class="subscription-results subscription-results la-clear-before">
    <g:if test="${subscriptions}">
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
                        <th class="center aligned"  rowspan="2" scope="col">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'default.previous.label')}">
                                <i class="${Icons.LNK.PREV}"></i>
                            </span>
                        </th>
                        <g:sortableColumn params="${params}" property="s.name" title="${subscriptionHeader}" rowspan="2" scope="col" />
                        <th class="center aligned" rowspan="2" scope="col">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'default.next.label')}">
                                <i class="${Icons.LNK.NEXT}"></i>
                            </span>
                        </th>
                        <g:if test="${'showPackages' in tableConfig}">
                            <th rowspan="2" scope="col">
                                ${message(code: 'license.details.linked_pkg')}
                            </th>
                        </g:if>
                        <g:if test="${params.orgRole in ['Subscriber'] && contextService.getOrg().isCustomerType_Inst()}">
                            <th scope="col" rowspan="2" >${message(code: 'consortium')}</th>
                        </g:if>
                        <g:elseif test="${params.orgRole == 'Subscriber'}">
                            <th rowspan="2">${message(code:'org.institution.label')}</th>
                        </g:elseif>
                        <g:if test="${'showProviders' in tableConfig}">
                            <g:sortableColumn scope="col" params="${params}" property="provider" title="${message(code: 'provider.label')}" rowspan="2" />
                        </g:if>
                        <g:if test="${'showVendors' in tableConfig}">
                            <g:sortableColumn scope="col" params="${params}" property="vendor" title="${message(code: 'vendor.label')}" rowspan="2" />
                        </g:if>
                        <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate" title="${message(code: 'default.startDate.label.shy')}"/>
                        <g:if test="${params.orgRole in ['Subscription Consortia']}">
                            <th scope="col" rowspan="2" class="center aligned">
                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'subscription.numberOfLicenses.label')}" data-position="top center">
                                    <i class="${Icons.ORG} large"></i>
                                </span>
                            </th>
                            <th scope="col" rowspan="2" class="center aligned">
                                <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.numberOfCostItems.label')}" data-position="top center">
                                    <i class="${Icons.COSTS} large"></i>
                                </span>
                            </th>
                        </g:if>
                        <g:if test="${!(institution.isCustomerType_Consortium())}">
                            <th scope="col" rowspan="2" class="la-no-uppercase center aligned">
                                <ui:multiYearIcon />
                            </th>
                        </g:if>
                        <th scope="col" rowspan="2" class="two">${message(code:'default.actions.label')}</th>
                    </tr>
                    <tr>
                        <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate" title="${message(code: 'default.endDate.label.shy')}"/>
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
                        <%
                            LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(s,false)
                            Long navPrevSub = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                            Long navNextSub = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
                        %>
                        <td class="center aligned">
                            <g:if test="${navPrevSub}">
                                <g:link controller="subscription" action="show" id="${navPrevSub}"><i class="${Icons.LNK.PREV}"></i></g:link>
                            </g:if>
                        </td>
                        <th scope="row" class="la-th-column">
                            <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}">
                                <g:if test="${s.name}">
                                    ${s.name}
                                    <g:if test="${s?.referenceYear}">
                                        ( ${s.referenceYear} )
                                    </g:if>
                                </g:if>
                                <g:else>
                                    -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                                </g:else>
                                <g:if test="${s.instanceOf}">
                                    <g:if test="${s.consortia && s.consortia == institution}">
                                        ( ${s.getSubscriberRespConsortia()?.name} )
                                    </g:if>
                                </g:if>
                            </g:link>
                            <g:if test="${'showLicense' in tableConfig}">
                                <g:each in="${allLinkedLicenses}" var="row">
                                    <g:if test="${s == row.destinationSubscription}">
                                        <g:set var="license" value="${row.sourceLicense}"/>
                                        <div class="la-flexbox la-minor-object">
                                            <i class="${Icons.LICENSE} la-list-icon"></i>
                                            <g:link controller="license" action="show" id="${license.id}">
                                                ${license.reference}
                                            </g:link><br />
                                        </div>
                                    </g:if>
                                </g:each>
                            </g:if>
                        </th>
                        <td class="center aligned">
                            <g:if test="${navNextSub}">
                                <g:link controller="subscription" action="show" id="${navNextSub}"><i class="${Icons.LNK.NEXT}"></i></g:link>
                            </g:if>
                        </td>
                        <g:if test="${'showPackages' in tableConfig}">
                        <td>
                        <!-- packages -->
                            <g:each in="${s.packages}" var="sp" status="ind">
                                <g:if test="${ind < 10}">
                                    <div class="la-flexbox">
                                        <i class="${Icons.PACKAGE} la-list-icon"></i>
                                        <g:link controller="subscription" action="index" id="${s.id}" params="[pkgfilter: sp.pkg.id]"
                                                title="${sp.pkg.provider?.name}">
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
                                    <g:if test="${contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )}">
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
                        </g:if>
                        <g:if test="${params.orgRole == 'Subscriber'}">
                            <td>
                                <g:if test="${contextService.getOrg().isCustomerType_Inst()}">
                                    ${s.getConsortia()?.name}
                                </g:if>
                            </td>
                        </g:if>
                        <g:if test="${'showProviders' in tableConfig}">
                            <td>
                                <g:each in="${s.providers}" var="provider">
                                    <g:link controller="provider" action="show" id="${provider.id}">${fieldValue(bean: provider, field: "name")}
                                        <g:if test="${provider.sortname}">
                                            <br /> (${fieldValue(bean: provider, field: "sortname")})
                                        </g:if>
                                    </g:link><br />
                                </g:each>
                            </td>
                        </g:if>
                        <g:if test="${'showVendors' in tableConfig}">
                            <td>
                                <g:each in="${s.vendors}" var="vendor">
                                    <g:link controller="vendor" action="show" id="${vendor.id}">
                                        ${fieldValue(bean: vendor, field: "name")}
                                        <g:if test="${vendor.sortname}">
                                            <br /> (${fieldValue(bean: vendor, field: "sortname")})
                                        </g:if>
                                    </g:link><br />
                                </g:each>
                            </td>
                        </g:if>
                        <%--
                            <td>
                                <g:if test="${params.orgRole == 'Subscription Consortia'}">
                                   <g:each in="${s.getDerivedNonHiddenSubscribers()}" var="subscriber">
                                        <g:link controller="organisation" action="show" id="${subscriber.id}">${subscriber.name}</g:link> <br />
                                   </g:each>
                                </g:if>
                            </td>
                        --%>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                            <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label.shy')}:"><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></span>
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
                                            ${childSubIds.isEmpty() ? 0 : CostItem.executeQuery('select count(*) from CostItem ci where ci.sub.id in (:subs) and ci.owner = :context and ci.costItemStatus != :deleted',[subs:childSubIds, context:institution, deleted:RDStore.COST_ITEM_DELETED])[0]}
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
                                        <ui:multiYearIcon isConsortial="true" color="orange" />
                                    </g:if>
                                    <g:else>
                                        <ui:multiYearIcon color="orange" />
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
                                                <i class="${Icons.SURVEY}"></i>
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
                                                        <i class="${Icons.SURVEY}"></i>
                                                    </span>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="subscription" action="surveysConsortia" id="${s.id}"
                                                        class="ui button blue icon la-modern-button">
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                          data-content="${message(code: "surveyconfig.isCompletedforOwner.false")}">
                                                        <i class="${Icons.SURVEY}"></i>
                                                    </span>
                                                </g:link>
                                            </g:else>

%{--                                        <g:link controller="subscription" action="surveysConsortia" id="${s.id}"
                                                class="ui button blue icon la-modern-button">
                                            <g:if test="${surveysConsortiaSub.surveyInfo?.isCompletedforOwner()}">
                                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                      data-content="${message(code: "surveyconfig.isCompletedforOwner.true")}">
                                                    <i class="${Icons.SURVEY} icon blue"></i>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                                      data-content="${message(code: "surveyconfig.isCompletedforOwner.false")}">
                                                    <i class="${Icons.SURVEY} icon open"></i>
                                                </span>
                                            </g:else>
                                        </g:link>--}%
                                    </g:if>
                                </g:if>

                            </g:if>
                            <g:if test="${'showLinking' in tableConfig}">
                                <%
                                    boolean linkPossible
                                    if(institution.isCustomerType_Inst()) {
                                        linkPossible = s._getCalculatedType() == CalculatedType.TYPE_LOCAL
                                    }
                                    else {
                                        linkPossible = institution.isCustomerType_Consortium()
                                    }
                                %>
                                <g:if test="${linkPossible}">
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