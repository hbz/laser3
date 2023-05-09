<%@ page import="java.time.temporal.ChronoUnit; de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.survey.SurveyResult; de.laser.Subscription; de.laser.PersonRole; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.ReaderNumber; de.laser.Contact; de.laser.auth.User; de.laser.auth.Role; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.SubscriptionsQueryService; de.laser.storage.RDConstants; de.laser.storage.RDStore; java.text.SimpleDateFormat; de.laser.License; de.laser.Org; de.laser.OrgRole; de.laser.OrgSetting; de.laser.remote.ApiSource; de.laser.AlternativeName" %>
<laser:serviceInjection/>
<g:if test="${'surveySubCostItem' in tmplConfigShow}">
    <g:set var="oldCostItem" value="${0.0}"/>
    <g:set var="oldCostItemAfterTax" value="${0.0}"/>
    <g:set var="sumOldCostItemAfterTax" value="${0.0}"/>
    <g:set var="sumOldCostItem" value="${0.0}"/>
</g:if>

<g:if test="${'surveyCostItem' in tmplConfigShow}">
    <g:set var="sumNewCostItem" value="${0.0}"/>
    <g:set var="sumSurveyCostItem" value="${0.0}"/>
    <g:set var="sumNewCostItemAfterTax" value="${0.0}"/>
    <g:set var="sumSurveyCostItemAfterTax" value="${0.0}"/>
</g:if>

<g:if test="${['platform', 'altname'].any { String tmplConfig -> tmplConfig in tmplConfigShow }}">
    <g:set var="apiSource" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>
</g:if>

<table id="${tableID ?: ''}" class="ui sortable celled la-js-responsive-table la-table table">
    <g:set var="sqlDateToday" value="${new java.sql.Date(System.currentTimeMillis())}"/>
    <thead>
    <tr>
        <g:if test="${tmplShowCheckbox}">
            <th>
                <g:if test="${orgList}">
                    <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                </g:if>
            </th>
        </g:if>

        <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">

            <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                <th>${message(code: 'sidewide.number')}</th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                <g:sortableColumn title="${message(code: 'org.sortname.label')}"
                                  property="lower(o.sortname)" params="${request.getParameterMap()}"/>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                <g:sortableColumn title="${message(code: 'org.fullName.label')}" property="lower(o.name)"
                                  params="${request.getParameterMap()}"/>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('altname')}">
                <th>${message(code: 'org.altname.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('mainContact')}">
                <th>${message(code: 'org.mainContact.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('hasInstAdmin')}">
                <th>${message(code: 'org.hasInstAdmin.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                <th>${message(code: 'org.isWekbCurated.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                <th>${message(code: 'default.status.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('legalInformation')}">
                <th class="la-no-uppercase">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code: 'org.legalInformation.tooltip')}">
                        <i class="handshake outline icon"></i>
                    </span>
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('publicContacts')}">
                <th>${message(code: 'org.publicContacts.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('privateContacts')}">
                <th>${message(code: 'org.privateContacts.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('numberOfSubscriptions')}">
                <th class="la-th-wrap">${message(code: 'org.subscriptions.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('numberOfSurveys')}">
                <th class="la-th-wrap">${message(code: 'survey.active')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('identifier')}">
                <th>${message(code:'default.identifier.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('wibid')}">
                <th>WIB</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('isil')}">
                <th>ISIL</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                <th>${message(code: 'platform')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('type')}">
                <th>${message(code: 'default.type.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('sector')}">
                <th>${message(code: 'org.sector.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('region')}">
                <th>${message(code: 'org.region.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('libraryNetwork')}">
                <th class="la-th-wrap la-hyphenation">${message(code: 'org.libraryNetworkTableHead.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('consortia')}">
                <th class="la-th-wrap la-hyphenation">${message(code: 'consortium.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('libraryType')}">
                <th>${message(code: 'org.libraryType.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('country')}">
                <th>${message(code: 'org.country.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('consortiaToggle')}">
                <th class="la-th-wrap la-hyphenation">${message(code: 'org.consortiaToggle.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('addSubMembers')}">
                <th>
                    ${message(code: 'subscription.details.addMembers.option.package.label')}
                </th>
                <th>
                    ${message(code: 'subscription.details.addMembers.option.issueEntitlement.label')}
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfo')}">
                <th>
                    ${message(code: 'subscription')}
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfoStartEndDate')}">
                <th>
                    ${message(code: 'surveyProperty.subDate')}
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfoStatus')}">
                <th>
                    ${message(code: 'subscription.status.label')}
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubCostItem')}">
                <th>

                    <g:if test="${actionName == 'surveyCostItems'}">
                        <%
                            def tmpParams = params.clone()
                            tmpParams.remove("sort")
                        %>
                        <g:if test="${sortOnCostItemsUp}">
                            <g:link action="surveyCostItems" class="ui icon"
                                    params="${tmpParams + [sortOnCostItemsDown: true]}"><span
                                    class="la-popup-tooltip la-delay"
                                    data-position="top right"
                                    data-content="${message(code: 'surveyCostItems.sortOnPrice')}">
                                <i class="arrow down circle icon blue"></i>
                            </span></g:link>
                        </g:if>
                        <g:else>
                            <g:link action="surveyCostItems" class="ui icon"
                                    params="${tmpParams + [sortOnCostItemsUp: true]}"><span
                                    class="la-popup-tooltip la-delay"
                                    data-position="top right"
                                    data-content="${message(code: 'surveyCostItems.sortOnPrice')}">
                                <i class="arrow up circle icon blue"></i>
                            </span></g:link>
                        </g:else>
                    </g:if>

                    <g:set var="costItemElements"
                           value="${RefdataValue.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :org', [org: institution])}"/>

                        <ui:select name="selectedCostItemElement"
                                      from="${costItemElements}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${selectedCostItemElement}"
                                      class="ui dropdown"
                                      id="selectedCostItemElement"/>
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItem') && surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
                <th>
                    ${message(code: 'surveyCostItems.label')}
                </th>
                <th></th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                <th class="center aligned">
                    <g:if test="${actionName == 'listProvider'}">
                        <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.providers')}"><i class="icon star"></i></span>
                    </g:if>
                    <g:if test="${actionName == 'listInstitution'}">
                        <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.insts')}"><i class="icon star"></i></span>
                    </g:if>
                    <g:if test="${actionName == 'listConsortia'}">
                        <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.consortia')}"><i class="icon star"></i></span>
                    </g:if>
                </th>
            </g:if>

        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:each in="${orgList}" var="org" status="i">

        <g:if test="${controllerName in ["survey"]}">
            <g:set var="surveyOrg" value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, org)}"/>

            <g:set var="existSubforOrg"
                   value="${Subscription.get(surveyConfig.subscription?.id)?.getDerivedSubscribers()?.id?.contains(org?.id)}"/>

            <g:set var="orgSub" value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(org)}"/>
        </g:if>

        <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
            <tr class="disabled">
        </g:if>
        <g:else>
            <tr>
        </g:else>

        <g:if test="${tmplShowCheckbox}">
            <td>
                <g:if test="${controllerName in ["survey"] && actionName == "surveyCostItems"}">
                    <g:if test="${CostItem.findBySurveyOrgAndCostItemStatusNotEqual(surveyOrg, RDStore.COST_ITEM_DELETED)}">
                        <g:checkBox id="selectedOrgs_${org.id}" name="selectedOrgs" value="${org.id}" checked="false"/>
                    </g:if>
                </g:if>
                <g:else>
                    <g:checkBox id="selectedOrgs_${org.id}" name="selectedOrgs" value="${org.id}" checked="false"/>
                </g:else>
            </td>
        </g:if>

        <g:each in="${tmplConfigShow}" var="tmplConfigItem">

            <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}<br />
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                <td>
                    ${org.sortname}
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                <th scope="row" class="la-th-column la-main-object">
                    <div class="la-flexbox">
                        <g:if test="${org.isCustomerType_Inst_Pro()}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${org.getCustomerTypeI10n()}">
                                <i class="chess rook grey la-list-icon icon"></i>
                            </span>
                        </g:if>
                        <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                            ${fieldValue(bean: org, field: "name")}
                        </g:if>
                        <g:else>
                            <g:link controller="organisation" action="show" id="${org.id}">
                                ${fieldValue(bean: org, field: "name")}
                            </g:link>
                        </g:else>

                        <g:if test="${surveyOrg && surveyOrg.orgInsertedItself}">
                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'surveyLinks.newParticipate')}">
                                <i class="paper plane outline large icon"></i>
                            </span>
                        </g:if>
                    </div>
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('altname')}">
                <%
                    SortedSet<String> altnames = new TreeSet<String>()
                    if(params.orgNameContains) {
                        altnames.addAll(org.altnames.findAll { AlternativeName altname -> altname.name.toLowerCase().contains(params.orgNameContains.toLowerCase()) }.name)
                    }
                    else altnames.addAll(org.altnames.name)
                %>
                <td>
                    <ul>
                        <g:each in="${altnames}" var="altname" status="a">
                            <g:if test="${a < 10}">
                                <li>
                                    <g:if test="${org.gokbId}">
                                        <g:link url="${apiSource.baseUrl}/public/orgContent/${org.gokbId}#altnames">${altname}</g:link>
                                    </g:if>
                                    <g:else>
                                        ${altname}
                                    </g:else>
                                </li>
                            </g:if>
                        </g:each>
                    </ul>
                    <g:if test="${altnames.size() > 10}">
                        <div class="ui accordion">
                            <%-- TODO translation string if this solution is going to be accepted --%>
                            <div class="title">Weitere ...<i class="ui dropdown icon"></i></div>
                            <div class="content">
                                <ul>
                                    <g:each in="${altnames.drop(10)}" var="altname">
                                        <li>
                                            <g:if test="${org.gokbId}">
                                                <g:link url="${apiSource.baseUrl}/public/orgContent/${org.gokbId}#altnames">${altname}</g:link>
                                            </g:if>
                                            <g:else>
                                                ${altname}
                                            </g:else>
                                        </li>
                                    </g:each>
                                </ul>
                            </div>
                        </div>
                    </g:if>
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('mainContact')}">
                <td>
                    <g:each in="${PersonRole.findAllByFunctionTypeAndOrg(RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, org)}"
                            var="personRole">
                        <g:if test="${personRole.prs.isPublic || (!personRole.prs.isPublic && personRole.prs.tenant?.id == contextService.getOrg().id)}">
                                <%--
                                <g:if test="${! personRole.prs.isPublic}">
                                    <span class="la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" data-position="top right">
                                        <i class="address card outline icon"></i>
                                    </span>
                                </g:if>
                                <g:else>
                                    <i class="address card icon"></i>
                                </g:else>
                                --%>
                                ${personRole.getPrs()?.getFirst_name()} ${personRole.getPrs()?.getLast_name()} <br />

                                <g:each in="${Contact.findAllByPrsAndContentType(
                                        personRole.getPrs(),
                                        RDStore.CCT_EMAIL
                                )}" var="email">
                                    <div class="item js-copyTriggerParent">
                                            <span data-position="right center"
                                                  class="la-popup-tooltip la-delay js-copyTrigger"
                                                  data-content="Mail senden an ${personRole.getPrs()?.getFirst_name()} ${personRole.getPrs()?.getLast_name()}">
                                        <ui:contactIcon type="${email.contentType.value}"/>
                                        <a class="js-copyTopic" href="mailto:${email.content}">${email.content}</a>
                                        </span><br />
                                    </div>
                                </g:each>
                                <g:each in="${Contact.findAllByPrsAndContentType(
                                        personRole.getPrs(),
                                        RDStore.CCT_PHONE
                                )}" var="telNr">
                                    <div class="item">
                                        <span data-position="right center">
                                            <i class="ui icon phone"></i>
                                            ${telNr.content}
                                        </span><br />
                                    </div>

                                </g:each>

                            </div>
                        </g:if>
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('hasInstAdmin')}">
                <td class="center aligned">
                    <%
                        String instAdminIcon = '<i class="large red times icon"></i>'
                        List<User> users = User.executeQuery('select uo.user from UserOrgRole uo where uo.org = :org and uo.formalRole = :instAdmin', [org: org, instAdmin: Role.findByAuthority('INST_ADM')])
                        if (users)
                            instAdminIcon = '<i class="large green check icon"></i>'
                    %>
                    <g:if test="${contextService.getUser().hasCtxAffiliation_or_ROLEADMIN('INST_ADM')}">
                        <br /><g:link controller="organisation" action="users"
                                    params="${[id: org.id]}">${raw(instAdminIcon)}</g:link>
                    </g:if>
                    <g:else>
                        ${raw(instAdminIcon)}
                    </g:else>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                <td class="center aligned">
                    <g:if test="${org.gokbId != null && RDStore.OT_PROVIDER.id in org.getAllOrgTypeIds()}">
                        <g:link url="${apiSource.baseUrl}/public/orgContent/${org.gokbId}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code:'org.isWekbCurated.header.label')}">
                                <i class="la-gokb la-list-icon icon"></i>
                            </span>
                        </g:link>
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                <td class="center aligned">
                    <g:if test="${org.status == RDStore.ORG_STATUS_CURRENT}">
                        <g:set var="precedents" value="${Org.executeQuery('select c.toOrg from Combo c where c.fromOrg = :org and c.type = :follows',[org: org, follows: RDStore.COMBO_TYPE_FOLLOWS])}"/>
                        <g:each in="${precedents}" var="precedent">
                            <span class="la-popup-tooltip" data-position="top right" data-content="<g:message code="org.succeedsTo.label" args="${[precedent.sortname ?: precedent.name]}"/>">
                                <g:link controller="org" action="show" id="${precedent.id}"><i class="ui icon left arrow"></i></g:link>
                            </span>
                        </g:each>
                        <span class="la-popup-tooltip la-delay" data-position="top right">
                            <i class="ui icon green circle"></i>
                        </span>
                    </g:if>
                    <g:if test="${org.status == RDStore.ORG_STATUS_RETIRED}">
                        <span class="la-popup-tooltip la-delay" data-position="top right" <g:if test="${org.retirementDate}">data-content="<g:message code="org.retirementDate.label"/>: <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${org.retirementDate}"/>"</g:if>>
                            <i class="ui icon yellow circle"></i>
                        </span>
                        <g:set var="successors" value="${Org.executeQuery('select c.fromOrg from Combo c where c.toOrg = :org and c.type = :follows',[org: org, follows: RDStore.COMBO_TYPE_FOLLOWS])}"/>
                        <g:each in="${successors}" var="successor">
                            <span class="la-popup-tooltip" data-position="top right" data-content="<g:message code="org.succeededBy.label" args="${[successor.sortname ?: successor.name]}"/>">
                                <g:link controller="org" action="show" id="${successor.id}"><i class="ui icon right arrow"></i></g:link>
                            </span>
                        </g:each>
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('legalInformation')}">
                <td>
                    <g:if test="${org.createdBy && org.legallyObligedBy}">
                        <span class="la-popup-tooltip la-delay" data-position="top right"
                              data-content="${message(code: 'org.legalInformation.1.tooltip', args: [org.createdBy, org.legallyObligedBy])}">
                            <i class="ui icon green check circle"></i>
                        </span>
                    </g:if>
                    <g:elseif test="${org.createdBy}">
                        <span class="la-popup-tooltip la-delay" data-position="top right"
                              data-content="${message(code: 'org.legalInformation.2.tooltip', args: [org.createdBy])}">
                            <i class="ui icon grey outline circle"></i>
                        </span>
                    </g:elseif>
                    <g:elseif test="${org.legallyObligedBy}">
                        <span class="la-popup-tooltip la-delay" data-position="top right"
                              data-content="${message(code: 'org.legalInformation.3.tooltip', args: [org.legallyObligedBy])}">
                            <i class="ui icon red question mark"></i>
                        </span>
                    </g:elseif>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('publicContacts')}">
                <td>
                    <g:set var="plctr" value="${0}"/>
                    <g:set var="pubLinksSorted" value="${org?.prsLinks?.toSorted()}"/>
                    <g:each in="${org?.prsLinks?.toSorted()}" var="pl">
                        <g:if test="${pl.functionType?.value && pl.prs.isPublic}">
                            <g:if test="${plctr == 0}">
                                <laser:render template="/templates/cpa/person_details" model="${[
                                        personRole          : pl,
                                        tmplShowDeleteButton: false,
                                        tmplConfigShow      : ['E-Mail', 'Mail', 'Phone'],
                                        controller          : 'organisation',
                                        action              : 'show',
                                        id                  : org.id
                                ]}"/>
                            </g:if>
                            <g:else>
                                <g:if test="${plctr == 1}">
                                    <div class="ui accordion styled info">
                                    <div class="title">
                                        <strong><i class="dropdown icon"></i><g:message code="org.privateContacts.showMore"/></strong>
                                        </div>
                                        <div class="content">
                                </g:if>
                                <laser:render template="/templates/cpa/person_details" model="${[
                                        personRole          : pl,
                                        tmplShowDeleteButton: false,
                                        tmplConfigShow      : ['E-Mail', 'Mail', 'Phone'],
                                        controller          : 'organisation',
                                        action              : 'show',
                                        id                  : org.id
                                ]}"/>
                                <g:if test="${plctr == pubLinksSorted.size()-1}"></div></div></g:if>
                            </g:else>
                            <g:set var="plctr" value="${plctr+1}"/>
                        </g:if>
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('privateContacts')}">
                <td>
                    <g:set var="visiblePrivateContacts" value="[]"/>
                    <g:set var="orgLinksSorted" value="${org?.prsLinks?.toSorted()}"/>
                    <g:set var="ol" value="${0}"/>
                    <g:each in="${orgLinksSorted}" var="pl">
                        <g:if test="${pl?.functionType?.value && (!pl.prs.isPublic) && pl?.prs?.tenant?.id == contextService.getOrg().id}">
                            <g:if test="${!visiblePrivateContacts.contains(pl.prs.id)}">
                                <g:set var="visiblePrivateContacts" value="${visiblePrivateContacts + pl.prs.id}"/>
                                <g:if test="${ol == 0}">
                                    <laser:render template="/templates/cpa/person_full_details" model="${[
                                            person                 : pl.prs,
                                            personContext          : org,
                                            tmplShowDeleteButton   : true,
                                            tmplShowAddPersonRoles : false,
                                            tmplShowAddContacts    : false,
                                            //tmplShowAddAddresses   : false,
                                            tmplShowFunctions      : true,
                                            tmplShowPositions      : true,
                                            tmplShowResponsiblities: false,
                                            tmplConfigShow         : ['E-Mail', 'Mail', 'Phone'],
                                            controller             : 'organisation',
                                            action                 : 'show',
                                            id                     : org.id,
                                            editable               : true
                                    ]}"/>
                                </g:if>
                                <g:else>
                                    <g:if test="${ol == 1}">
                                        <div class="ui accordion styled info">
                                        <div class="title">
                                            <strong><i class="dropdown icon"></i><g:message code="org.privateContacts.showMore"/></strong>
                                        </div>
                                        <div class="content">
                                    </g:if>
                                    <laser:render template="/templates/cpa/person_full_details" model="${[
                                            person                 : pl.prs,
                                            personContext          : org,
                                            tmplShowDeleteButton   : true,
                                            tmplShowAddPersonRoles : false,
                                            tmplShowAddContacts    : false,
                                            //tmplShowAddAddresses   : false,
                                            tmplShowFunctions      : true,
                                            tmplShowPositions      : true,
                                            tmplShowResponsiblities: false,
                                            tmplConfigShow         : ['E-Mail', 'Mail', 'Phone'],
                                            controller             : 'organisation',
                                            action                 : 'show',
                                            id                     : org.id,
                                            editable               : true
                                    ]}"/>
                                    <g:if test="${ol == orgLinksSorted.size()-1}"></div></div></g:if>
                                </g:else>
                                <g:set var="ol" value="${ol+1}"/>
                            </g:if>
                        </g:if>
                    <%--
                    <g:if test="${pl?.functionType?.value && (! pl.prs.isPublic) && pl?.prs?.tenant?.id == contextService.getOrg().id}">
                        <laser:render template="/templates/cpa/person_details" model="${[
                                personRole          : pl,
                                tmplShowDeleteButton: false,
                                tmplConfigShow      : ['E-Mail', 'Mail', 'Phone'],
                                controller          : 'organisation',
                                action              : 'show',
                                id                  : org.id
                        ]}"/>
                    </g:if>
                    --%>
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('numberOfSubscriptions')}">
                <td class="center aligned">
                    <div class="la-flexbox">
                        <%
                        def subStatus
                        if(actionName == 'currentProviders') {
                            subStatus = RDStore.SUBSCRIPTION_CURRENT.id.toString()
                        }
                        else subStatus = params.subStatus

                        if(params.filterPvd && params.filterPvd != "" && params.list('filterPvd')){
                            (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([org: org, actionName: actionName, status: subStatus ?: null, date_restr: params.subValidOn ? DateUtils.parseDateGeneric(params.subValidOn) : null, providers: params.list('filterPvd')], contextService.getOrg())
                        }else {
                            (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([org: org, actionName: actionName, status: subStatus ?: null, date_restr: params.subValidOn ? DateUtils.parseDateGeneric(params.subValidOn) : null], contextService.getOrg())
                        }
                        def numberOfSubscriptions = Subscription.executeQuery("select s.id " + base_qry, qry_params).size()
                        /*if(params.subPerpetual == "on") {
                            (base_qry2, qry_params2) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([org: org, actionName: actionName, status: subStatus == RDStore.SUBSCRIPTION_CURRENT.id.toString() ? RDStore.SUBSCRIPTION_EXPIRED.id.toString() : null, hasPerpetualAccess: RDStore.YN_YES.id.toString()], contextService.getOrg())
                            numberOfSubscriptions+=Subscription.executeQuery("select s.id " + base_qry2, qry_params2).size()
                        }*/
                        %>
                        <g:if test="${actionName == 'manageMembers'}">
                            <g:link controller="myInstitution" action="manageConsortiaSubscriptions"
                                    params="${[member: org.id, status: params.subStatus ?: null, validOn: params.subValidOn, filterSet: true, filterPvd: params.list('filterPvd')]}">
                                <div class="ui blue circular label">
                                    ${numberOfSubscriptions}
                                </div>
                            </g:link>
                        </g:if>
                        <g:elseif test="${actionName == 'currentConsortia'}">
                            <g:link controller="myInstitution" action="currentSubscriptions"
                                    params="${[consortia: genericOIDService.getOID(org), status: subStatus ?: null, validOn: params.subValidOn, filterSet: true]}"
                                    title="${message(code: 'org.subscriptions.tooltip', args: [org.name])}">
                                <div class="ui blue circular label">
                                    ${numberOfSubscriptions}
                                </div>
                            </g:link>
                        </g:elseif>
                        <g:elseif test="${actionName == 'currentProviders'}">
                            <g:link controller="myInstitution" action="currentSubscriptions"
                                    params="${[identifier: org.globalUID, status: [RDStore.SUBSCRIPTION_CURRENT.id.toString()], isSiteReloaded: 'yes']}"
                                    title="${message(code: 'org.subscriptions.tooltip', args: [org.name])}">
                                <div class="ui blue circular label">
                                    ${numberOfSubscriptions}
                                </div>
                            </g:link>
                        </g:elseif>
                        <g:else>
                            <g:link controller="myInstitution" action="currentSubscriptions"
                                    params="${[identifier: org.globalUID]}"
                                    title="${message(code: 'org.subscriptions.tooltip', args: [org.name])}">
                                <div class="ui blue circular label">
                                    ${numberOfSubscriptions}
                                </div>
                            </g:link>
                        </g:else>
                    </div>
                </td>
            </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('numberOfSurveys')}">
                    <td class="center aligned">
                        <div class="la-flexbox">
                            <g:if test="${invertDirection}">
                                <g:set var="participantSurveys"
                                       value="${SurveyResult.findAllByParticipantAndOwnerAndEndDateGreaterThanEquals(contextService.getOrg(), org, new Date())}"/>
                            </g:if>
                            <g:else>
                                <g:set var="participantSurveys"
                                       value="${SurveyResult.findAllByOwnerAndParticipantAndEndDateGreaterThanEquals(contextService.getOrg(), org, new Date())}"/>
                            </g:else>
                            <g:set var="numberOfSurveys"
                                   value="${participantSurveys.groupBy { it.surveyConfig.id }.size()}"/>
                            <%
                                def finishColor = ""
                                def countFinish = 0
                                def countNotFinish = 0

                                participantSurveys.each {
                                    if (it.isResultProcessed()) {
                                        countFinish++
                                    } else {
                                        countNotFinish++
                                    }
                                }
                                if (countFinish > 0 && countNotFinish == 0) {
                                    finishColor = "green"
                                } else if (countFinish > 0 && countNotFinish > 0) {
                                    finishColor = "yellow"
                                } else {
                                    finishColor = "red"
                                }
                            %>

                            <g:if test="${invertDirection}">
                                <g:link controller="myInstitution" action="currentSurveys"
                                        params="[owner: org.id]">
                                    <div class="ui circular ${finishColor} label">
                                        ${numberOfSurveys}
                                    </div>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="myInstitution" action="manageParticipantSurveys"
                                        id="${org.id}">
                                    <div class="ui circular ${finishColor} label">
                                        ${numberOfSurveys}
                                    </div>
                                </g:link>
                            </g:else>

                        </div>
                    </td>
                </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('identifier')}">
                <td><g:if test="${org.ids}">
                    <div class="ui list">
                        <g:each in="${org.ids?.sort { it?.ns?.ns }}" var="id"><div
                                class="item">${id.ns.ns}: ${id.value}</div></g:each>
                    </div>
                </g:if></td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('wibid')}">
                <td>${org.getIdentifiersByType('wibid') && !org.getIdentifiersByType('wibid').value.contains('Unknown') ? org.getIdentifiersByType('wibid').value.join(', ') : ''}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('isil')}">
                <td>${org.getIdentifiersByType('isil') && !org.getIdentifiersByType('isil').value.contains('Unknown') ? org.getIdentifiersByType('isil').value.join(', ') : ''}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                <td>
                    <ul>
                        <g:each in="${org.platforms}" var="platform">
                            <li><g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link></li>
                        </g:each>
                    </ul>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('type')}">
                <td>
                    <g:each in="${org.orgType?.sort { it?.getI10n("value") }}" var="type">
                        ${type.getI10n("value")}
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('sector')}">
                <td>${org.sector?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('region')}">
                <td>${org.region?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('libraryNetwork')}">
                <td>${org.libraryNetwork?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('consortia')}">
                <td>
                    <g:each in="${org.outgoingCombos}" var="combo">
                        ${combo.toOrg.name}<br /><br />
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('libraryType')}">
                <td>${org.libraryType?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('country')}">
                <td>${org.country?.getI10n('value')}</td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('addSubMembers')}">
                <g:if test="${subInstance?.packages}">
                    <td><g:each in="${subInstance?.packages}">
                        <g:checkBox type="text" id="selectedPackage_${org.id + it.pkg.id}"
                                    name="selectedPackage_${org.id + it.pkg.id}" value="1"
                                    checked="false"
                                    onclick="JSPC.app.checkselectedPackage(${org.id + it.pkg.id});"/> ${it.pkg.name}<br />
                    </g:each>
                    </td>
                    <td><g:each in="${subInstance?.packages}">
                        <g:checkBox type="text" id="selectedIssueEntitlement_${org.id + it.pkg.id}"
                                    name="selectedIssueEntitlement_${org.id + it.pkg.id}" value="1" checked="false"
                                    onclick="JSPC.app.checkselectedIssueEntitlement(${org.id + it.pkg.id});"/> ${it.pkg.name}<br />
                    </g:each>
                    </td>
                </g:if><g:else>
                <td>${message(code: 'subscription.details.addMembers.option.noPackage.label', args: [subInstance?.name])}</td>
                <td>${message(code: 'subscription.details.addMembers.option.noPackage.label', args: [subInstance?.name])}</td>
            </g:else>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfo')}">
                <td>
                    <g:if test="${existSubforOrg}">

                        <g:if test="${orgSub.isCurrentMultiYearSubscriptionNew()}">
                            <g:message code="surveyOrg.perennialTerm.available"/>
                            <br />
                            <g:link controller="subscription" action="show"
                                    id="${orgSub.id}">
                                ${orgSub.name}
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="subscription" action="show"
                                    id="${orgSub.id}">
                                ${orgSub.name}
                            </g:link>
                        </g:else>

                        <ui:xEditableAsIcon owner="${orgSub}" class="ui icon center aligned" iconClass="info circular inverted" field="comment" type="textarea" overwriteEditable="${false}"/>

                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfoStartEndDate')}">
                <td>
                    <g:if test="${existSubforOrg}">
                        <g:if test="${orgSub.isCurrentMultiYearSubscriptionNew()}">
                            <g:message code="surveyOrg.perennialTerm.available"/>
                            <br />
                            <g:link controller="subscription" action="show"
                                    id="${orgSub.id}">
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${orgSub.startDate}"/><br />
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${orgSub.endDate}"/>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="subscription" action="show"
                                    id="${orgSub.id}">
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${orgSub.startDate}"/><br />
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${orgSub.endDate}"/>
                            </g:link>
                        </g:else>

                        <ui:xEditableAsIcon owner="${orgSub}" class="ui icon center aligned" iconClass="info circular inverted" field="comment" type="textarea" overwriteEditable="${false}"/>

                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfoStatus')}">
                <td>
                    <g:if test="${existSubforOrg}">
                        <g:if test="${orgSub.isCurrentMultiYearSubscriptionNew()}">
                            <g:message code="surveyOrg.perennialTerm.available"/>
                            <br />
                            <g:link controller="subscription" action="show"
                                    id="${orgSub.id}">
                                ${orgSub.status.getI10n('value')}
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="subscription" action="show"
                                    id="${orgSub.id}">
                                ${orgSub.status.getI10n('value')}
                            </g:link>
                        </g:else>

                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubCostItem')}">
                <td class="x">

                    <g:set var="oldCostItem" value="${0.0}"/>
                    <g:set var="oldCostItemAfterTax" value="${0.0}"/>
                <g:if test="${existSubforOrg}">
                    <g:if test="${surveyConfig.subSurveyUseForTransfer && orgSub.isCurrentMultiYearSubscriptionNew()}">
                        <g:message code="surveyOrg.perennialTerm.available"/>
                    </g:if>
                    <g:else>
                        <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemStatusNotEqual(orgSub, institution, RDStore.COST_ITEM_DELETED)}"
                                var="costItem">

                            <g:if test="${costItem.costItemElement?.id?.toString() == selectedCostItemElement}">

                                <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}"
                                                   minFractionDigits="2"
                                                   maxFractionDigits="2" type="number"/></strong>

                                (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                                 maxFractionDigits="2" type="number"/>)

                                ${(costItem.billingCurrency?.getI10n('value')?.split('-')).first()}

                                <g:set var="sumOldCostItem"
                                       value="${sumOldCostItem + costItem.costInBillingCurrency?:0}"/>
                                <g:set var="sumOldCostItemAfterTax"
                                       value="${sumOldCostItemAfterTax + costItem.costInBillingCurrencyAfterTax?:0}"/>

                                <g:set var="oldCostItem" value="${costItem.costInBillingCurrency?:null}"/>
                                <g:set var="oldCostItemAfterTax" value="${costItem.costInBillingCurrencyAfterTax?:null}"/>

                            </g:if>
                        </g:each>
                    </g:else>
                </g:if>

                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItem') && surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
                %{-- // TODO Moe - date.minusDays() --}%
                <td class="center aligned" style="${(existSubforOrg && orgSub && orgSub.endDate && ChronoUnit.DAYS.between(DateUtils.dateToLocalDate(orgSub.startDate), DateUtils.dateToLocalDate(orgSub.endDate)) < 364) ? 'background: #FFBF00 !important;' : ''}">

                    <g:if test="${surveyConfig.subSurveyUseForTransfer && orgSub && orgSub.isCurrentMultiYearSubscriptionNew()}">
                        <g:message code="surveyOrg.perennialTerm.available"/>
                    </g:if>
                    <g:else>

                        <g:set var="costItem" scope="request"
                               value="${CostItem.findBySurveyOrgAndCostItemStatusNotEqual(surveyOrg, RDStore.COST_ITEM_DELETED)}"/>

                        <g:if test="${costItem}">

                            <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                               maxFractionDigits="2" type="number"/></strong>

                            (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                             maxFractionDigits="2" type="number"/>)

                            ${(costItem.billingCurrency?.getI10n('value')?.split('-')).first()}

                            <g:set var="sumSurveyCostItem"
                                   value="${sumSurveyCostItem + costItem.costInBillingCurrency?:0}"/>
                            <g:set var="sumSurveyCostItemAfterTax"
                                   value="${sumSurveyCostItemAfterTax + costItem.costInBillingCurrencyAfterTax?:0}"/>

                            <g:if test="${oldCostItem || oldCostItemAfterTax}">
                                <br /><strong><g:formatNumber number="${((costItem.costInBillingCurrencyAfterTax-oldCostItemAfterTax)/oldCostItemAfterTax)*100}"
                                                       minFractionDigits="2"
                                                       maxFractionDigits="2" type="number"/>%</strong>

                                (<g:formatNumber number="${((costItem.costInBillingCurrency-oldCostItem)/oldCostItem)*100}" minFractionDigits="2"
                                                 maxFractionDigits="2" type="number"/>%)
                            </g:if>

                            <br />
                            <g:if test="${costItem.startDate || costItem.endDate}">
                                (${costItem.startDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.startDate) : ''} - ${costItem.endDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.endDate) : ''})
                            </g:if>

                            <button onclick="JSPC.app.addEditSurveyCostItem(${params.id}, ${surveyConfig.id}, ${org.id}, ${costItem.id})"
                                    class="ui icon circular button right floated trigger-modal"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </button>
                        </g:if>
                        <g:else>
                            <button onclick="JSPC.app.addEditSurveyCostItem(${params.id}, ${surveyConfig.id}, ${org.id}, ${'null'})"
                                    class="ui icon circular button right floated trigger-modal"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </button>
                        </g:else>

                    </g:else>
                </td>

                <td class="center aligned">
                    <g:set var="costItem" scope="request"
                           value="${CostItem.findBySurveyOrgAndCostItemStatusNotEqual(surveyOrg, RDStore.COST_ITEM_DELETED)}"/>
                    <g:if test="${costItem && costItem.costDescription}">

                        <div class="ui icon la-popup-tooltip la-delay" data-content="${costItem.costDescription}">
                            <i class="info circular inverted icon"></i>
                        </div>
                    </g:if>

                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                <td class="center aligned">
                    <g:if test="${actionName == 'listProvider'}">
                        <g:if test="${currentProviderIdList && (org.id in currentProviderIdList)}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.providers')}"><i class="icon yellow star"></i></span>
                        </g:if>
                    </g:if>
                    <g:if test="${actionName == 'listInstitution'}">
                        <g:if test="${currentConsortiaMemberIdList && (org.id in currentConsortiaMemberIdList)}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.insts')}"><i class="icon yellow star"></i></span>
                        </g:if>
                    </g:if>
                    <g:if test="${actionName == 'listConsortia'}">
                        <g:if test="${currentConsortiaIdList && (org.id in currentConsortiaIdList)}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.consortia')}"><i class="icon yellow star"></i></span>
                        </g:if>
                    </g:if>
                </td>
            </g:if>

        </g:each><!-- tmplConfigShow -->
        </tr>
    </g:each><!-- orgList -->
    </tbody>
    <g:if test="${orgList && ('surveySubCostItem' in tmplConfigShow || 'surveyCostItem' in tmplConfigShow)}">
        <tfoot>
        <tr>
            <g:if test="${tmplShowCheckbox}">
                <td></td>
            </g:if>
            <g:each in="${1..(tmplConfigShow?.size()- ('surveySubCostItem' in tmplConfigShow ? 2 : 1))}" var="tmplConfigItem">
                    <td></td>
            </g:each>
            <g:if test="${'surveySubCostItem' in tmplConfigShow}">
                <td>
                    <strong><g:formatNumber number="${sumOldCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></strong>
                    (<g:formatNumber number="${sumOldCostItem}" minFractionDigits="2"
                                     maxFractionDigits="2" type="number"/>)
                </td>
            </g:if>
            <g:if test="${'surveyCostItem' in tmplConfigShow}">
                <td>
                    <strong><g:formatNumber number="${sumSurveyCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></strong>
                    (<g:formatNumber number="${sumSurveyCostItem}" minFractionDigits="2"
                                     maxFractionDigits="2" type="number"/>)

                    <g:if test="${sumOldCostItemAfterTax || sumOldCostItem}">
                        <br /><strong><g:formatNumber number="${((sumSurveyCostItemAfterTax-sumOldCostItemAfterTax)/sumOldCostItemAfterTax)*100}"
                                               minFractionDigits="2"
                                               maxFractionDigits="2" type="number"/>%</strong>

                        (<g:formatNumber number="${((sumSurveyCostItem-sumOldCostItem)/sumOldCostItem)*100}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>%)
                    </g:if>
                </td>
                <td></td>
            </g:if>
        </tr>
        </tfoot>
    </g:if>
</table>

<g:if test="${tmplShowCheckbox}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#orgListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
            }
        })
        <g:if test="${tmplConfigShow?.contains('addSubMembers')}">

        JSPC.app.checkselectedIssueEntitlement = function (selectedid) {
            if ($('#selectedIssueEntitlement_' + selectedid).prop('checked')) {
                $('#selectedPackage_' + selectedid).prop('checked', false);
            }
        }
        JSPC.app.checkselectedPackage = function (selectedid) {
            if ($('#selectedPackage_' + selectedid).prop('checked')) {
                $('#selectedIssueEntitlement_' + selectedid).prop('checked', false);
            }

        }

        </g:if>
    </laser:script>

</g:if>
<g:if test="${tmplConfigShow?.contains('surveyCostItem') && surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
    <laser:script file="${this.getGroovyPageFileName()}">
   $('table[id^=costTable] .x .trigger-modal').on('click', function(e) {
                    e.preventDefault();

                    $.ajax({
                        url: $(this).attr('href')
                    }).done( function(data) {
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty().html(data);

                        $('#dynamicModalContainer .ui.modal').modal({
                            onVisible: function () {
                                r2d2.initDynamicUiStuff('#costItem_ajaxModal');
                                r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');
                            },
                            detachable: true,
                            closable: false,
                            transition: 'scale',
                            onApprove : function() {
                                $(this).find('.ui.form').submit();
                                return false;
                            }
                        }).modal('show');
                    })
                });

        JSPC.app.addEditSurveyCostItem = function (id, surveyConfigID, participant, costItem) {
            event.preventDefault();
            $.ajax({
                url: "<g:createLink controller='survey' action='editSurveyCostItem'/>",
                                data: {
                                    id: id,
                                    surveyConfigID: surveyConfigID,
                                    participant: participant,
                                    costItem: costItem
                                }
            }).done( function(data) {
                $('.ui.dimmer.modals > #modalSurveyCostItem').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                    onVisible: function () {
                        r2d2.initDynamicUiStuff('#modalSurveyCostItem');
                        r2d2.initDynamicXEditableStuff('#modalSurveyCostItem');
                    },
                    detachable: true,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        };

    </laser:script>
</g:if>
<g:if test="${tmplConfigShow?.contains('surveySubCostItem') && surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#selectedCostItemElement').on('change', function() {
            var selectedCostItemElement = $("#selectedCostItemElement").val()
            var url = "<g:createLink controller="survey" action="surveyCostItems" params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]}"/>&selectedCostItemElement="+selectedCostItemElement;
            location.href = url;
         });
    </laser:script>
</g:if>