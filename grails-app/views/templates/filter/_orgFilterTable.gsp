<%@ page import="de.laser.remote.Wekb; de.laser.addressbook.PersonRole; de.laser.addressbook.Contact; de.laser.wekb.Package; de.laser.wekb.Vendor; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyInfo; de.laser.utils.AppUtils; de.laser.convenience.Marker; java.time.temporal.ChronoUnit; de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.survey.SurveyResult; de.laser.Subscription; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.ReaderNumber; de.laser.auth.User; de.laser.auth.Role; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.SubscriptionsQueryService; de.laser.storage.RDConstants; de.laser.storage.RDStore; java.text.SimpleDateFormat; de.laser.License; de.laser.Org; de.laser.OrgRole; de.laser.OrgSetting; de.laser.AlternativeName; de.laser.RefdataCategory;" %>
<laser:serviceInjection/>
<g:if test="${'surveySubCostItem' in tmplConfigShow}">
    <g:set var="oldCostItem" value="${0.0}"/>
    <g:set var="oldCostItemAfterTax" value="${0.0}"/>
    <g:set var="sumOldCostItemAfterTax" value="${0.0}"/>
    <g:set var="sumOldCostItem" value="${0.0}"/>
</g:if>

<g:if test="${'surveyCostItem' in tmplConfigShow || 'surveyCostItemPackage' in tmplConfigShow}">
    <g:set var="sumNewCostItem" value="${0.0}"/>
    <g:set var="sumSurveyCostItem" value="${0.0}"/>
    <g:set var="sumNewCostItemAfterTax" value="${0.0}"/>
    <g:set var="sumSurveyCostItemAfterTax" value="${0.0}"/>
</g:if>

<table id="${tableID ?: ''}" class="ui sortable celled la-js-responsive-table la-table table ${fixedHeader ?: ''}">
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
                <g:sortableColumn title="${message(code: 'org.sortname.label')}" property="lower(o.sortname)" params="${request.getParameterMap()}"/>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                <g:sortableColumn title="${message(code: 'org.fullName.label')}" property="lower(o.name)" params="${request.getParameterMap()}"/>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('altname')}">
                <th>${message(code: 'altname.plural')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('mainContact')}">
                <th>${message(code: 'org.mainContact.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('hasInstAdmin')}">
                <th>${message(code: 'org.hasInstAdmin.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('legalInformation')}">
                <th class="center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code: 'org.legalInformation.tooltip')}">
                        <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION}"></i>
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
                <th class="center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code:'org.subscriptions.label')}">
                        <i class="${Icon.SUBSCRIPTION} large"></i>
                    </span>
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('currentSubscriptions')}">
                <th class="la-th-wrap">${message(code: 'org.subscriptions.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('numberOfSurveys')}">
                <th class="center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code:'survey.active')}">
                        <i class="${Icon.SURVEY} large"></i>
                    </span>
                </th>
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
            <g:if test="${tmplConfigItem.equalsIgnoreCase('customerType')}">
                <th>${message(code: 'org.customerType.label')}</th>
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
                    ${message(code: 'exportClickMe.subscription.costItems')}: ${selectedCostItemElementID ? RefdataValue.get(selectedCostItemElementID).getI10n('value') : ''}
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItem')}">
                <th>
                    ${message(code: 'surveyCostItems.label')}:

                    <g:set var="costItemElements"
                           value="${costItemsByCostItemElement ? costItemsByCostItemElement.collect { RefdataValue.findByValueAndOwner(it.key, RefdataCategory.findByDesc(RDConstants.COST_ITEM_ELEMENT))} : [RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE]}"/>

                    <g:if test="${costItemElements}">
                        <g:if test="${actionName == 'surveyCostItems'}">
                            <%
                                def tmpParams = params.clone()
                                tmpParams.remove("sort")
                            %>
                            <g:if test="${sortOnCostItemsUp}">
                                <g:link action="surveyCostItems"
                                        params="${tmpParams + [sortOnCostItemsDown: true]}"><span
                                        class="la-popup-tooltip"
                                        data-position="top right"
                                        data-content="${message(code: 'surveyCostItems.sortOnPrice')}">
                                    <i class="arrow down circle icon blue"></i>
                                </span></g:link>
                            </g:if>
                            <g:else>
                                <g:link action="surveyCostItems"
                                        params="${tmpParams + [sortOnCostItemsUp: true]}"><span
                                        class="la-popup-tooltip"
                                        data-position="top right"
                                        data-content="${message(code: 'surveyCostItems.sortOnPrice')}">
                                    <i class="arrow up circle icon blue"></i>
                                </span></g:link>
                            </g:else>
                        </g:if>


                        %{--<ui:select name="selectedCostItemElementID"
                                   from="${costItemElements}"
                                   optionKey="id"
                                   optionValue="value"
                                   value="${selectedCostItemElementID}"
                                   class="ui dropdown clearable"
                                   id="selectedCostItemElementID"
                                   noSelection="${['': message(code: 'default.select.choose.label')]}"/>--}%
                    </g:if>
                    ${selectedCostItemElementID ? RefdataValue.get(selectedCostItemElementID).getI10n('value') : ''}
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemPackage')}">
                <th>
                    ${message(code: 'surveyCostItems.label')}:
                        <g:if test="${actionName == 'surveyCostItemsPackages'}">
                            <%
                                def tmpParams2 = params.clone()
                                tmpParams2.remove("sort")
                            %>
                            <g:if test="${sortOnCostItemsUp}">
                                <g:link action="surveyCostItemsPackages"
                                        params="${tmpParams2 + [sortOnCostItemsDown: true]}"><span
                                        class="la-popup-tooltip"
                                        data-position="top right"
                                        data-content="${message(code: 'surveyCostItems.sortOnPrice')}">
                                    <i class="arrow down circle icon blue"></i>
                                </span></g:link>
                            </g:if>
                            <g:else>
                                <g:link action="surveyCostItemsPackages"
                                        params="${tmpParams2 + [sortOnCostItemsUp: true]}"><span
                                        class="la-popup-tooltip"
                                        data-position="top right"
                                        data-content="${message(code: 'surveyCostItems.sortOnPrice')}">
                                    <i class="arrow up circle icon blue"></i>
                                </span></g:link>
                            </g:else>
                        </g:if>
                    ${selectedCostItemElementID ? RefdataValue.get(selectedCostItemElementID).getI10n('value') : ''}
                </th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isBetaTester')}">
                <th class="center aligned">
                    todo
                </th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                <th class="center aligned">
                    <ui:markerIcon type="WEKB_CHANGES" />
                </th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                <th class="center aligned">
                    <g:if test="${actionName == 'listProvider'}">
                        <ui:myXIcon tooltip="${message(code: 'menu.my.providers')}" />
                    </g:if>
                    <g:if test="${actionName == 'listInstitution'}">
                        <ui:myXIcon tooltip="${message(code: 'menu.my.insts')}" />
                    </g:if>
                    <g:if test="${actionName == 'listConsortia'}">
                        <ui:myXIcon tooltip="${message(code: 'menu.my.consortia')}" />
                    </g:if>
                </th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('mailInfos')}">
                <th class="center aligned">
                </th>
            </g:if>

        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:each in="${orgList}" var="org" status="i">

        <g:if test="${controllerName in ["survey"]}">
            <g:set var="surveyOrg" value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, org)}"/>

            <g:set var="orgSub" value="${(surveyConfig.subscription || params.sub instanceof Subscription) ?  OrgRole.executeQuery('select oo.sub from OrgRole oo where oo.org = :org and oo.roleType in (:subscrRoles) and oo.sub.instanceOf = :sub',
                    [org: org, subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], sub: (params.sub instanceof Subscription ? params.sub : surveyConfig.subscription)])[0] : null}"/>

        </g:if>

        <tr class="${org.isArchived() ? 'warning' : ''} ${tmplDisableOrgIds && (org.id in tmplDisableOrgIds) ? 'disabled' : ''}">

        <g:if test="${tmplShowCheckbox}">
            <td>
                <g:if test="${controllerName in ["survey"] && actionName == "surveyCostItems"}">
                    <g:if test="${CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED)}">
                        <g:checkBox id="selectedOrgs_${org.id}" name="selectedOrgs" value="${org.id}" checked="false"/>
                    </g:if>
                </g:if>
                <g:elseif test="${controllerName in ["survey"] && actionName == "surveyCostItemsPackages"}">
                    <g:if test="${selectedPackageID && CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkg(surveyOrg, RDStore.COST_ITEM_DELETED, Package.get(Long.valueOf(selectedPackageID)))}">
                        <g:checkBox id="selectedOrgs_${org.id}" name="selectedOrgs" value="${org.id}" checked="false"/>
                    </g:if>
                </g:elseif>
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
                    <ui:archiveIcon org="${org}" /> ${org.sortname}
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                <th scope="row" class="la-th-column la-main-object">
                    <div class="la-flexbox">
                        <g:if test="${org instanceof Org}">
                            <ui:customerTypeOnlyProIcon org="${org}" cssClass="la-list-icon" />

                            <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                                ${fieldValue(bean: org, field: "name")}
                            </g:if>
                            <g:else>
                                <g:link controller="organisation" action="show" id="${org.id}">
                                    ${fieldValue(bean: org, field: "name")}
                                </g:link>
                            </g:else>

                            <g:if test="${surveyOrg && surveyOrg.orgInsertedItself}">
                                <span data-position="top right" class="la-popup-tooltip"
                                      data-content="${message(code: 'surveyLinks.newParticipate')}">
                                    <i class="paper plane outline large icon"></i>
                                </span>
                            </g:if>
                        </g:if>
                        <g:elseif test="${org instanceof Vendor}">
                            <g:link controller="vendor" action="show" id="${org.id}">
                                ${fieldValue(bean: org, field: "name")}
                            </g:link>
                        </g:elseif>
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
                    <ul class="la-simpleList">
                        <g:each in="${altnames}" var="altname" status="a">
                            <g:if test="${a < 10}">
                                <li>${altname}</li>
                            </g:if>
                        </g:each>
                    </ul>
                    <g:if test="${altnames.size() > 10}">
                        <div class="ui accordion">
                            <%-- TODO translation string if this solution is going to be accepted --%>
                            <div class="title">Weitere ...<i class="dropdown icon"></i></div>
                            <div class="content">
                                <ul class="la-simpleList">
                                    <g:each in="${altnames.drop(10)}" var="altname">
                                        <li>${altname}</li>
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
                                ${personRole.getPrs()?.getFirst_name()} ${personRole.getPrs()?.getLast_name()} <br />

                                <g:each in="${Contact.findAllByPrsAndContentType(
                                        personRole.getPrs(), RDStore.CCT_EMAIL
                                )}" var="email">
                                    <div class="item js-copyTriggerParent">
                                            <span class="js-copyTrigger">
                                                    <ui:contactIcon type="${email.contentType.value}"/>
                                                    <a class="js-copyTopic">${email.content}</a>
                                                    <span class="la-popup-tooltip" data-position="top right"  data-content="${message(code: 'tooltip.sendMailTo')} ${personRole.getPrs()?.getFirst_name()} ${personRole.getPrs()?.getLast_name()}">
                                                        <a href="mailto:${email.content}"  class="${Btn.MODERN.SIMPLE} tiny">
                                                            <i class="${Icon.LNK.MAIL_TO}"></i>
                                                        </a>
                                                    </span>
                                            </span>
                                        <br />
                                    </div>
                                </g:each>
                                <g:each in="${Contact.findAllByPrsAndContentType(
                                        personRole.getPrs(), RDStore.CCT_PHONE
                                )}" var="telNr">
                                    <div class="item">
                                        <span data-position="right center">
                                            <i class="${Icon.SYM.PHONE}"></i>
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
                    <g:if test="${contextService.isInstAdm()}">
                        <br /><g:link controller="organisation" action="users"
                                    params="${[id: org.id]}"><i class="${Icon.SYM.NO} large red"></i></g:link>
                    </g:if>
                    <g:else>
                        <i class="${Icon.SYM.YES} large green"></i>
                    </g:else>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('legalInformation')}">
                <td>
                    <g:if test="${org.createdBy && org.legallyObligedBy}">
                        <span class="la-popup-tooltip" data-position="top right"
                              data-content="${message(code: 'org.legalInformation.11.tooltip', args: [org.createdBy, org.legallyObligedBy])}">
                            <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION_11}"></i>
                        </span>
                    </g:if>
                    <g:elseif test="${org.createdBy}">
                        <span class="la-popup-tooltip" data-position="top right"
                              data-content="${message(code: 'org.legalInformation.10.tooltip', args: [org.createdBy])}">
                            <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION_10}"></i>
                        </span>
                    </g:elseif>
                    <g:elseif test="${org.legallyObligedBy}">
                        <span class="la-popup-tooltip" data-position="top right"
                              data-content="${message(code: 'org.legalInformation.01.tooltip', args: [org.legallyObligedBy])}">
                            <i class="${Icon.ATTR.ORG_LEGAL_INFORMATION_01}"></i>
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
                                <laser:render template="/addressbook/person_details" model="${[
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
                                <laser:render template="/addressbook/person_details" model="${[
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
                                    <laser:render template="/addressbook/person_full_details" model="${[
                                            person                 : pl.prs,
                                            personContext          : org,
                                            tmplShowDeleteButton   : true,
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
                                    <laser:render template="/addressbook/person_full_details" model="${[
                                            person                 : pl.prs,
                                            personContext          : org,
                                            tmplShowDeleteButton   : true,
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
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('numberOfSubscriptions')}">
                <td class="center aligned">
                    <div class="la-flexbox">
                        <%

                        if(params.filterPvd && params.filterPvd != "" && params.list('filterPvd')){
                            (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(
                                    [org: org, actionName: actionName, status: RDStore.SUBSCRIPTION_CURRENT.id, date_restr: params.subValidOn ? DateUtils.parseDateGeneric(params.subValidOn) : null, count: true, providers: params.list('filterPvd')]
                            )
                        }else {
                            (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(
                                    [org: org, actionName: actionName, status: RDStore.SUBSCRIPTION_CURRENT.id, date_restr: params.subValidOn ? DateUtils.parseDateGeneric(params.subValidOn) : null, count: true]
                            )
                        }
                        int numberOfSubscriptions = Subscription.executeQuery("select count(*) " + base_qry, qry_params)[0]
                        /*if(params.subPerpetual == "on") {
                            (base_qry2, qry_params2) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([org: org, actionName: actionName, status: subStatus == RDStore.SUBSCRIPTION_CURRENT.id ? RDStore.SUBSCRIPTION_EXPIRED.id : null, hasPerpetualAccess: RDStore.YN_YES.id])
                            numberOfSubscriptions+=Subscription.executeQuery("select s.id " + base_qry2, qry_params2).size()
                        }*/
                        %>
                        <g:if test="${actionName == 'manageMembers'}">
                            <g:link controller="myInstitution" action="manageConsortiaSubscriptions"
                                    params="${[member: org.id, status: RDStore.SUBSCRIPTION_CURRENT.id, validOn: params.subValidOn, filterSet: true, filterPvd: params.list('filterPvd')]}">
                                <ui:bubble count="${numberOfSubscriptions}" />
                            </g:link>
                        </g:if>
                        <g:elseif test="${actionName == 'currentConsortia'}">
                            <g:link controller="myInstitution" action="currentSubscriptions"
                                    params="${[consortia: genericOIDService.getOID(org), status: RDStore.SUBSCRIPTION_CURRENT.id, validOn: params.subValidOn, filterSet: true]}"
                                    title="${message(code: 'org.subscriptions.tooltip', args: [org.name])}">
                                <ui:bubble count="${numberOfSubscriptions}" />
                            </g:link>
                        </g:elseif>
                        <g:elseif test="${actionName == 'currentProviders'}">
                            <g:link controller="myInstitution" action="currentSubscriptions"
                                    params="${[identifier: org.globalUID, status: RDStore.SUBSCRIPTION_CURRENT.id, isSiteReloaded: 'yes']}"
                                    title="${message(code: 'org.subscriptions.tooltip', args: [org.name])}">
                                <ui:bubble count="${numberOfSubscriptions}" />
                            </g:link>
                        </g:elseif>
                        <g:else>
                            <g:link controller="myInstitution" action="currentSubscriptions"
                                    params="${[identifier: org.globalUID]}"
                                    title="${message(code: 'org.subscriptions.tooltip', args: [org.name])}">
                                <ui:bubble count="${numberOfSubscriptions}" />
                            </g:link>
                        </g:else>
                    </div>
                </td>
            </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('numberOfSurveys')}">
                    <td class="center aligned">
                        <div class="la-flexbox">
                            <g:if test="${invertDirection}">
                                <g:set var="countNotFinish"
                                       value="${SurveyInfo.executeQuery("select count(*) from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg where surOrg.org = :org and surOrg.finishDate is null and surInfo.status = :status and surInfo.owner = :owner", [org: contextService.getOrg(), owner: org, status: RDStore.SURVEY_SURVEY_STARTED])[0]}"/>
                            </g:if>
                            <g:else>
                                <g:set var="countNotFinish"
                                       value="${SurveyInfo.executeQuery("select count(*) from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg where surOrg.org = :org and surOrg.finishDate is null and surInfo.status = :status and surInfo.owner = :owner", [org: org, owner: contextService.getOrg(), status: RDStore.SURVEY_SURVEY_STARTED])[0]}"/>
                            </g:else>
                            <%
                                def finishColor = ""
                                if (countNotFinish == 0) {
                                    finishColor = "green"
                                } else if (countNotFinish > 0) {
                                    finishColor = "yellow"
                                }
                            %>

                            <g:if test="${invertDirection}">
                                <g:link controller="myInstitution" action="currentSurveys"
                                        params="[owner: org.id]">
                                    <div class="ui circular ${finishColor} label">
                                        ${countNotFinish}
                                    </div>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="myInstitution" action="manageParticipantSurveys"
                                        id="${org.id}">
                                    <div class="ui circular ${finishColor} label">
                                        ${countNotFinish}
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
                <td>${org.getNonEmptyIdentifiersByType('wibid') ? org.getNonEmptyIdentifiersByType('wibid').value.join(', ') : ''}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('isil')}">
                <td>${org.getNonEmptyIdentifiersByType('isil') ? org.getNonEmptyIdentifiersByType('isil').value.join(', ') : ''}</td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                <td>
                    <g:each in="${org.platforms}" var="platform">
                        <g:if test="${platform.gokbId != null}">
                            <ui:wekbIconLink type="platform" gokbId="${platform.gokbId}" />
                        </g:if>
                        <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                        <br />
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('customerType')}">
                <td>
                    ${org.getCustomerTypeI10n()}
                </td>
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
                    <g:if test="${orgSub}">

                        <g:if test="${orgSub.isCurrentMultiYearSubscriptionToParentSub()}">
                            <g:message code="surveyOrg.perennialTerm.current"/>
                            <br />
                        </g:if>
                        <g:elseif test="${orgSub.isMultiYearSubscription()}">
                            <g:message code="surveyOrg.perennialTerm.available"/>
                            <br />
                        </g:elseif>

                        <g:link controller="subscription" action="show" id="${orgSub.id}">${orgSub.getLabel()}</g:link>

                        <ui:xEditableAsIcon owner="${orgSub}" class="ui icon center aligned" iconClass="info circular inverted" field="comment" type="textarea" overwriteEditable="${false}"/>
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfoStartEndDate')}">
                <td class="center aligned" style="${(orgSub && orgSub.endDate && ChronoUnit.DAYS.between(DateUtils.dateToLocalDate(orgSub.startDate), DateUtils.dateToLocalDate(orgSub.endDate)) < 364) ? 'background: #FFBF00 !important;' : ''}">

                    <g:if test="${orgSub}">
                        <g:if test="${orgSub.isCurrentMultiYearSubscriptionToParentSub()}">
                            <g:message code="surveyOrg.perennialTerm.current"/>
                            <br />
                        </g:if>
                        <g:elseif test="${orgSub.isMultiYearSubscription()}">
                            <g:message code="surveyOrg.perennialTerm.available"/>
                            <br />
                        </g:elseif>

                        <g:link controller="subscription" action="show" id="${orgSub.id}">
                            <g:formatDate formatName="default.date.format.notime" date="${orgSub.startDate}"/><br/>
                            <g:formatDate formatName="default.date.format.notime" date="${orgSub.endDate}"/>
                        </g:link>

                        <ui:xEditableAsIcon owner="${orgSub}" class="ui icon center aligned" iconClass="info circular inverted" field="comment" type="textarea"
                                            overwriteEditable="${false}"/>
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubInfoStatus')}">
                <td>
                    <g:if test="${orgSub}">
                        <g:if test="${orgSub.isCurrentMultiYearSubscriptionToParentSub()}">
                            <g:message code="surveyOrg.perennialTerm.current"/>
                            <br />
                        </g:if>
                        <g:elseif test="${orgSub.isMultiYearSubscription()}">
                            <g:message code="surveyOrg.perennialTerm.available"/>
                            <br />
                        </g:elseif>

                        <g:link controller="subscription" action="show" id="${orgSub.id}">${orgSub.status.getI10n('value')}</g:link>
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubCostItem')}">
                <td class="x">

                    <g:set var="oldCostItem" value="${0.0}"/>
                    <g:set var="oldCostItemAfterTax" value="${0.0}"/>
                <g:if test="${orgSub}">
                        <g:if test="${orgSub.isCurrentMultiYearSubscriptionToParentSub()}">
                            <g:message code="surveyOrg.perennialTerm.current"/>
                            <br />
                        </g:if>
                        <g:elseif test="${orgSub.isMultiYearSubscription()}">
                            <g:message code="surveyOrg.perennialTerm.available"/>
                            <br />
                        </g:elseif>
                        <table class="ui very basic compact table">
                            <tbody>
                            <g:if test="${selectedCostItemElementID}">
                                <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemStatusNotEqualAndCostItemElement(orgSub, institution, RDStore.COST_ITEM_DELETED, RefdataValue.get(Long.valueOf(selectedCostItemElementID)))}"
                                        var="costItem">
                                    <g:set var="sumOldCostItem"
                                           value="${sumOldCostItem + (costItem.costInBillingCurrency ?: 0)}"/>
                                    <g:set var="sumOldCostItemAfterTax"
                                           value="${sumOldCostItemAfterTax + (costItem.costInBillingCurrencyAfterTax ?: 0)}"/>

                                    <g:set var="oldCostItem" value="${costItem.costInBillingCurrency ?: 0.0}"/>
                                    <g:set var="oldCostItemAfterTax" value="${costItem.costInBillingCurrencyAfterTax ?: 0.0}"/>

                                    <tr>
                                        <td>
                                            <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}"
                                                                    minFractionDigits="2"
                                                                    maxFractionDigits="2" type="number"/></strong>

                                            (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                                             maxFractionDigits="2" type="number"/>)
                                        </td>
                                        <td>
                                            ${costItem.billingCurrency?.getI10n('value')}
                                        </td>
                                        <td style="${(costItem.startDate && costItem.endDate && ChronoUnit.DAYS.between(DateUtils.dateToLocalDate(costItem.startDate), DateUtils.dateToLocalDate(costItem.endDate)) < 364) ? 'background: #FFBF00 !important;' : ''}">
                                            <g:if test="${costItem.startDate || costItem.endDate}">
                                                ${costItem.startDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.startDate) : ''} - ${costItem.endDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.endDate) : ''}
                                            </g:if>
                                            <g:link class="ui blue right right floated mini button" controller="finance" action="showCostItem" id="${costItem.id}" params="[sub: costItem.sub?.id]" target="_blank"><g:message code="default.show.label" args="[g.message(code: 'costItem.label')]"/></g:link>
                                        </td>
                                    </tr>
                                </g:each>
                            </g:if>
                            </tbody>
                        </table>
                </g:if>

                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItem')}">
                <td class="center aligned">

                    <g:if test="${orgSub.isCurrentMultiYearSubscriptionToParentSub()}">
                        <g:message code="surveyOrg.perennialTerm.current"/>
                        <br />
                    </g:if>
                    <g:elseif test="${orgSub.isMultiYearSubscription()}">
                        <g:message code="surveyOrg.perennialTerm.available"/>
                        <br />
                    </g:elseif>

                        <g:set var="costItems" scope="request"
                               value="${selectedCostItemElementID ? CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED, RefdataValue.get(Long.valueOf(selectedCostItemElementID))) : null}"/>

                        <g:if test="${costItems}">
                            <table class="ui very basic compact table">
                                <tbody>
                                <g:each in="${costItems}"
                                        var="costItem">
                                    <g:set var="sumSurveyCostItem"
                                           value="${sumSurveyCostItem + (costItem.costInBillingCurrency ?: 0)}"/>
                                    <g:set var="sumSurveyCostItemAfterTax"
                                           value="${sumSurveyCostItemAfterTax + (costItem.costInBillingCurrencyAfterTax ?: 0)}"/>

                                    <tr>
                                        <td>
                                            <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                                                    maxFractionDigits="2" type="number"/></strong>

                                            (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                                             maxFractionDigits="2" type="number"/>)

                                        </td>
                                        <td>
                                            ${costItem.billingCurrency?.getI10n('value')}
                                        </td>

                                        <td>
                                            <g:if test="${oldCostItem || oldCostItemAfterTax}">

                                                <strong><g:formatNumber
                                                        number="${(((costItem.costInBillingCurrencyAfterTax ?: 0) - oldCostItemAfterTax) / oldCostItemAfterTax) * 100}"
                                                        minFractionDigits="2"
                                                        maxFractionDigits="2" type="number"/>%</strong>

                                                (<g:formatNumber number="${(((costItem.costInBillingCurrency ?: 0) - oldCostItem) / oldCostItem) * 100}"
                                                                 minFractionDigits="2"
                                                                 maxFractionDigits="2" type="number"/>%)

                                            </g:if>
                                        </td>
                                        <td style="${(costItem.startDate && costItem.endDate && ChronoUnit.DAYS.between(DateUtils.dateToLocalDate(costItem.startDate), DateUtils.dateToLocalDate(costItem.endDate)) < 364) ? 'background: #FFBF00 !important;' : ''}">
                                            <g:if test="${costItem.startDate || costItem.endDate}">
                                                ${costItem.startDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.startDate) : ''} - ${costItem.endDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.endDate) : ''}
                                            </g:if>
                                        </td>

                                        <td>
                                            <g:if test="${editable}">
                                                <button class="${Btn.ICON.SIMPLE} circular right floated triggerSurveyCostItemModal"
                                                        data-href="${g.createLink(action: 'editSurveyCostItem', params: [id                       : params.id,
                                                                                                                         surveyConfigID           : surveyConfig.id,
                                                                                                                         participant              : org.id,
                                                                                                                         costItem                 : costItem.id,
                                                                                                                         selectedCostItemElementID: selectedCostItemElementID])}"
                                                        role="button"
                                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                                                </button>
                                            </g:if>
                                        </td>
                                        <td>
                                            <g:if test="${costItem && costItem.costDescription}">

                                                <div class="ui icon la-popup-tooltip" data-content="${costItem.costDescription}">
                                                    <i class="info circular inverted icon"></i>
                                                </div>
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>

                        </g:if>
                        <g:else>
                            <g:if test="${editable}">
                                <button class="${Btn.ICON.SIMPLE} circular right floated triggerSurveyCostItemModal"
                                        data-href="${g.createLink(action: 'editSurveyCostItem', params: [id                       : params.id,
                                                                                                         surveyConfigID           : surveyConfig.id,
                                                                                                         participant              : org.id,
                                                                                                         selectedCostItemElementID: selectedCostItemElementID])}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                                </button>
                            </g:if>
                        </g:else>
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemPackage')}">
            %{-- // TODO Moe - date.minusDays() --}%
                <td class="center aligned">

                    <g:if test="${selectedPackageID && selectedCostItemElementID}">
                        <g:set var="costItems" scope="request"
                               value="${CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkg(surveyOrg, RDStore.COST_ITEM_DELETED, RefdataValue.get(Long.valueOf(selectedCostItemElementID)), Package.get(Long.valueOf(selectedPackageID)))}"/>

                        <g:if test="${costItems}">
                            <table class="ui very basic compact table">
                                <tbody>
                                <g:each in="${costItems}"
                                        var="costItem">
                                    <g:set var="sumSurveyCostItem"
                                           value="${sumSurveyCostItem + (costItem.costInBillingCurrency ?: 0)}"/>
                                    <g:set var="sumSurveyCostItemAfterTax"
                                           value="${sumSurveyCostItemAfterTax + (costItem.costInBillingCurrencyAfterTax ?: 0)}"/>

                                    <tr>
                                        <td>
                                            <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                                                    maxFractionDigits="2" type="number"/></strong>

                                            (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                                             maxFractionDigits="2" type="number"/>)

                                        </td>
                                        <td>
                                            ${costItem.billingCurrency?.getI10n('value')}
                                        </td>

                                        <td>
                                            <g:if test="${oldCostItem || oldCostItemAfterTax}">

                                                <strong><g:formatNumber
                                                        number="${(((costItem.costInBillingCurrencyAfterTax ?: 0) - oldCostItemAfterTax) / oldCostItemAfterTax) * 100}"
                                                        minFractionDigits="2"
                                                        maxFractionDigits="2" type="number"/>%</strong>

                                                (<g:formatNumber number="${(((costItem.costInBillingCurrency ?: 0) - oldCostItem) / oldCostItem) * 100}"
                                                                 minFractionDigits="2"
                                                                 maxFractionDigits="2" type="number"/>%)

                                            </g:if>
                                        </td>
                                        <td style="${(costItem.startDate && costItem.endDate && ChronoUnit.DAYS.between(DateUtils.dateToLocalDate(costItem.startDate), DateUtils.dateToLocalDate(costItem.endDate)) < 364) ? 'background: #FFBF00 !important;' : ''}">
                                            <g:if test="${costItem.startDate || costItem.endDate}">
                                                ${costItem.startDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.startDate) : ''} - ${costItem.endDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.endDate) : ''}
                                            </g:if>
                                        </td>

                                        <td>
                                            <g:if test="${editable}">
                                                <button class="${Btn.ICON.SIMPLE} circular right floated triggerSurveyCostItemModal"
                                                        data-href="${g.createLink(action: 'editSurveyCostItem', params: [id                       : params.id,
                                                                                                                         surveyConfigID           : surveyConfig.id,
                                                                                                                         participant              : org.id,
                                                                                                                         costItem                 : costItem.id,
                                                                                                                         selectedCostItemElementID: selectedCostItemElementID,
                                                                                                                         selectedPackageID        : selectedPackageID,
                                                                                                                         selectPkg              : "true"])}"
                                                        role="button"
                                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                                                </button>
                                            </g:if>
                                        </td>
                                        <td>
                                            <g:if test="${costItem && costItem.costDescription}">

                                                <div class="ui icon la-popup-tooltip" data-content="${costItem.costDescription}">
                                                    <i class="info circular inverted icon"></i>
                                                </div>
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>

                        </g:if>
                        <g:else>
                            <g:if test="${editable}">
                                <button class="${Btn.ICON.SIMPLE} circular right floated triggerSurveyCostItemModal"
                                        data-href="${g.createLink(action: 'editSurveyCostItem', params: [id                       : params.id,
                                                                                                         surveyConfigID           : surveyConfig.id,
                                                                                                         participant              : org.id,
                                                                                                         selectedCostItemElementID: selectedCostItemElementID,
                                                                                                         selectedPackageID        : selectedPackageID,
                                                                                                         selectPkg              : "true"])}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                                </button>
                            </g:if>
                        </g:else>
                    </g:if>
                </td>
            </g:if>


            <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                <td class="center aligned">
                    <g:if test="${org.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
                        <g:if test="${org instanceof Org}">
                            <ui:cbItemMarkerAction org="${org}" type="${Marker.TYPE.WEKB_CHANGES}" simple="true"/>
                        </g:if>
                        <g:elseif test="${org instanceof Vendor}">
                            <ui:cbItemMarkerAction vendor="${org}" type="${Marker.TYPE.WEKB_CHANGES}" simple="true"/>
                        </g:elseif>
                    </g:if>
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                <td class="center aligned">
                    <g:if test="${actionName == 'listProvider'}">
                        <g:if test="${currentProviderIdList && (org.id in currentProviderIdList)}">
                            <span class="la-popup-tooltip" data-content="${message(code: 'menu.my.providers')}"><i class="${Icon.SIG.MY_OBJECT} yellow"></i></span>
                        </g:if>
                    </g:if>
                    <g:if test="${actionName == 'listInstitution'}">
                        <g:if test="${currentConsortiaMemberIdList && (org.id in currentConsortiaMemberIdList)}">
                            <span class="la-popup-tooltip" data-content="${message(code: 'menu.my.insts')}"><i class="${Icon.SIG.MY_OBJECT} yellow"></i></span>
                        </g:if>
                    </g:if>
                    <g:if test="${actionName == 'listConsortia'}">
                        <g:if test="${currentConsortiaIdList && (org.id in currentConsortiaIdList)}">
                            <span class="la-popup-tooltip" data-content="${message(code: 'menu.my.consortia')}"><i class="${Icon.SIG.MY_OBJECT} yellow"></i></span>
                        </g:if>
                    </g:if>
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('mailInfos')}">
                <td class="center aligned">
                    <a href="#" class="ui button icon la-modern-button infoFlyout-trigger" data-template="org" data-org="${org.id}">
                        <i class="ui info icon"></i>
                    </a>
                </td>
            </g:if>

        </g:each><!-- tmplConfigShow -->
        </tr>
    </g:each><!-- orgList -->
    </tbody>
    <g:if test="${orgList && ('surveySubCostItem' in tmplConfigShow || 'surveyCostItem' in tmplConfigShow || 'surveyCostItemPackage' in tmplConfigShow)}">
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
            <g:if test="${'surveyCostItem' in tmplConfigShow || 'surveyCostItemPackage' in tmplConfigShow}">
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
<g:if test="${(tmplConfigShow?.contains('surveyCostItem') || tmplConfigShow?.contains('surveyCostItemPackage')) && editable}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('.triggerSurveyCostItemModal').on('click', function(e) {
            e.preventDefault();

            $.ajax({
                url: $(this).attr('data-href')
            }).done( function (data) {
                $('.ui.dimmer.modals > #surveyCostItemModal').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                   onShow: function () {
                        r2d2.initDynamicUiStuff('#surveyCostItemModal');
                        r2d2.initDynamicXEditableStuff('#surveyCostItemModal');
                        $("html").css("cursor", "auto");
                    },
                    detachable: true,
                    autofocus: false,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('#surveyCostItemModal .ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        });
    </laser:script>

</g:if>
<g:if test="${tmplConfigShow?.contains('surveySubCostItem')}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#selectedCostItemElementID').on('change', function() {
            var selectedCostItemElementID = $(this).val()
            var url = "<g:createLink controller="survey" action="surveyCostItems" params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]}"/>&selectedCostItemElementID="+selectedCostItemElementID;
            location.href = url;
         });
    </laser:script>
</g:if>

<g:if test="${tmplConfigShow?.contains('mailInfos')}">
    <laser:render template="/info/flyoutWrapper"/>
</g:if>