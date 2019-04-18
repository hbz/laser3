<%@ page import="com.k_int.kbplus.ReaderNumber; de.laser.SubscriptionsQueryService; de.laser.helper.RDStore; com.k_int.kbplus.Subscription; java.text.SimpleDateFormat; com.k_int.kbplus.PersonRole; com.k_int.kbplus.ReaderNumber; com.k_int.kbplus.License; com.k_int.kbplus.Contact; com.k_int.kbplus.Org; com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection />
<table class="ui sortable celled la-table table">
    <g:set var="sqlDateToday" value="${new java.sql.Date(System.currentTimeMillis())}"/>
    <thead>
    <tr>
        <g:if test="${tmplConfigShow?.contains('lineNumber')}">
            <th>${message(code:'sidewide.number')}</th>
        </g:if>
        <g:if test="${tmplShowCheckbox}">
            <th>
                <g:if test="${orgList}">
                    <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                </g:if>
            </th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('sortname')}">
            <g:sortableColumn title="${message(code: 'org.sortname.label', default: 'Sortname')}" property="lower(o.sortname)" params="${request.getParameterMap()}"/>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('shortname')}">
            <g:sortableColumn title="${message(code: 'org.shortname.label', default: 'Shortname')}" property="lower(o.shortname)" params="${request.getParameterMap()}"/>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('name')}">
            <g:sortableColumn title="${message(code: 'org.fullName.label', default: 'Name')}" property="lower(o.name)" params="${request.getParameterMap()}"/>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('mainContact')}">
            <th>${message(code: 'org.mainContact.label', default: 'Main Contact')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('publicContacts')}">
            <th>${message(code: 'org.publicContacts.label', default: 'Public Contacts')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('privateContacts')}">
            <th>${message(code: 'org.privateContacts.label', default: 'Public Contacts')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('currentFTEs')}">
            <th class="la-th-wrap">${message(code: 'org.currentFTEs.label', default: 'Current FTEs')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('numberOfSubscriptions')}">
            <th class="la-th-wrap">${message(code: 'org.subscriptions.label', default: 'Public Contacts')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('identifier')}">
            <th>Identifier</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('wibid')}">
            <th>WIB</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('isil')}">
            <th>ISIL</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('type')}">
            <th>${message(code: 'org.type.label', default: 'Type')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('sector')}">
            <th>${message(code: 'org.sector.label', default: 'Sector')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('federalState')}">
            <th>${message(code: 'org.federalState.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('libraryNetwork')}">
            <th class="la-th-wrap la-hyphenation">${message(code: 'org.libraryNetworkTableHead.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('libraryType')}">
            <th>${message(code: 'org.libraryType.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('country')}">
            <th>${message(code: 'org.country.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('consortiaToggle')}">
            <th class="la-th-wrap la-hyphenation">${message(code: 'org.consortiaToggle.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('addSubMembers')}">
            <th>
                ${message(code: 'subscription.details.addMembers.option.package.label')}
            </th>
            <th>
                ${message(code: 'subscription.details.addMembers.option.issueEntitlement.label')}
            </th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('surveySubInfo')}">
            <th>
                ${message(code: 'subscription')}
            </th>
        </g:if>
    </tr>
    </thead>
    <tbody>
        <g:each in="${orgList}" var="org" status="i">
            <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                <tr class="disabled">
            </g:if>
            <g:else>
                <tr>
            </g:else>
            <g:if test="${tmplConfigShow?.contains('lineNumber')}">
                <td class="center aligned">
                    ${ (params.int('offset') ?: 0)  + i + 1 }<br>
                </td>
            </g:if>
            <g:if test="${tmplShowCheckbox}">
                <td>
                    <g:checkBox name="selectedOrgs" value="${org.id}" checked="false"/>
                </td>
            </g:if>

            <g:if test="${tmplConfigShow?.contains('sortname')}">
                <td>
                    ${org.sortname}
                </td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('shortname')}">
                <td>
                    <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                            <g:if test="${org.shortname}">
                                ${fieldValue(bean: org, field: "shortname")}
                            </g:if>
                    </g:if>
                    <g:else>
                        <g:link controller="organisation" action="show" id="${org.id}">
                            <g:if test="${org.shortname}">
                                ${fieldValue(bean: org, field: "shortname")}
                            </g:if>
                        </g:link>
                    </g:else>
                </td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('name')}">
                <td>
                    <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                            ${fieldValue(bean: org, field: "name")} <br>
                            <g:if test="${org.shortname && !tmplConfigShow?.contains('shortname')}">
                                (${fieldValue(bean: org, field: "shortname")})
                            </g:if>
                    </g:if>
                    <g:else>
                        <g:link controller="organisation" action="show" id="${org.id}">
                            ${fieldValue(bean: org, field: "name")} <br>
                            <g:if test="${org.shortname && !tmplConfigShow?.contains('shortname')}">
                                (${fieldValue(bean: org, field: "shortname")})
                            </g:if>
                        </g:link>
                    </g:else>
                </td>
            </g:if>

            <g:if test="${tmplConfigShow?.contains('mainContact')}">
            <td>
                <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(RefdataValue.getByValueAndCategory('General contact person', 'Person Function'), org)}" var="personRole">
                    <g:if test="${(personRole.prs?.isPublic?.value=='Yes') || (personRole.prs?.isPublic?.value=='No' && personRole?.prs?.tenant?.id == contextService.getOrg()?.id)}">
                        ${personRole?.getPrs()?.getFirst_name()} ${personRole?.getPrs()?.getLast_name()}<br>
                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                personRole.getPrs(),
                                RefdataValue.getByValueAndCategory('E-Mail', 'ContactContentType')
                        )}" var="email">
                            <i class="ui icon envelope outline"></i>
                            <span data-position="right center" data-tooltip="Mail senden an ${personRole?.getPrs()?.getFirst_name()} ${personRole?.getPrs()?.getLast_name()}">
                                <a href="mailto:${email?.content}" >${email?.content}</a>
                            </span><br>
                        </g:each>
                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                personRole.getPrs(),
                                RefdataValue.getByValueAndCategory('Phone', 'ContactContentType')
                        )}" var="telNr">
                            <i class="ui icon phone"></i>
                            <span data-position="right center">
                                ${telNr?.content}
                            </span><br>
                        </g:each>
                    </g:if>
                </g:each>
            </td>
        </g:if>
            <g:if test="${tmplConfigShow?.contains('publicContacts')}">
                <td>
                    <g:each in="${org?.prsLinks?.toSorted()}" var="pl">
                        <g:if test="${pl?.functionType?.value && pl?.prs?.isPublic?.value!='No'}">
                            <g:render template="/templates/cpa/person_details" model="${[
                                    personRole: pl,
                                    tmplShowDeleteButton: false,
                                    tmplConfigShow: ['E-Mail', 'Mail', 'Phone'],
                                    controller: 'organisation',
                                    action: 'show',
                                    id: org.id
                            ]}"/>
                        </g:if>
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('privateContacts')}">
                <td>
                    <g:each in="${org?.prsLinks?.toSorted()}" var="pl">
                        <g:if test="${pl?.functionType?.value && pl?.prs?.isPublic?.value=='No' && pl?.prs?.tenant?.id == contextService.getOrg()?.id}">
                            <g:render template="/templates/cpa/person_details" model="${[
                                    personRole: pl,
                                    tmplShowDeleteButton: false,
                                    tmplConfigShow: ['E-Mail', 'Mail', 'Phone'],
                                    controller: 'organisation',
                                    action: 'show',
                                    id: org.id
                            ]}"/>
                        </g:if>
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('currentFTEs')}">
                <td>
                    <g:each in="${com.k_int.kbplus.ReaderNumber.findAllByOrgAndType(org, RefdataValue.getByValueAndCategory('Students', 'Number Type'))?.sort {it.type?.getI10n("value")}}" var="fte">
                        <g:if test="${fte.startDate <= sqlDateToday && fte.endDate >= sqlDateToday}">
                            ${fte.type?.getI10n("value")} : ${fte.number} <br>
                        </g:if>
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('numberOfSubscriptions')}">
                <td>
                    <div class="la-flexbox">
                        <% (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([org: org, actionName: actionName,status: RDStore.SUBSCRIPTION_CURRENT.id], contextService.org)
                            def numberOfSubscriptions = Subscription.executeQuery("select s.id " + base_qry, qry_params).size()
                        %>
                        <g:if test="${actionName == 'manageConsortia'}">
                            <g:link controller="myInstitution" action="manageConsortiaSubscriptions" params="${[member: org.id, status: RDStore.SUBSCRIPTION_CURRENT.id]}">
                                <div class="ui circular label">
                                    ${numberOfSubscriptions}
                                </div>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="myInstitution" action="currentSubscriptions" params="${[q:org.name]}" title="${message(code: 'org.subscriptions.tooltip', args: [org.name])}">
                                <div class="ui circular label">
                                    ${numberOfSubscriptions}
                                </div>
                            </g:link>
                        </g:else>
                    </div>
                </td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('identifier')}">
                <td><g:if test="${org.ids}">
                    <div class="ui list">
                        <g:each in="${org.ids.sort{it.identifier.ns.ns}}" var="id"><div class="item">${id.identifier.ns.ns}: ${id.identifier.value}</div></g:each>
                    </div>
                </g:if></td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('wibid')}">
                <td>${org.getIdentifiersByType('wibid')?.value?.join(', ')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('isil')}">
                <td>${org.getIdentifiersByType('isil')?.value?.join(', ')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('type')}">
                <td>
                    <g:each in="${org.orgType?.sort{it?.getI10n("value")}}" var="type">
                        ${type.getI10n("value")}
                    </g:each>
                </td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('sector')}">
                <td>${org.sector?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('federalState')}">
                <td>${org.federalState?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('libraryNetwork')}">
                <td>${org.libraryNetwork?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('libraryType')}">
                <td>${org.libraryType?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('country')}">
                <td>${org.country?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('consortiaToggle')}">
                <td>
                    <%-- here: switch if in consortia or not --%>
                    <g:if test="${!consortiaMemberIds.contains(org.id)}">
                        <g:link class="ui icon positive button" data-tooltip="${message(code:'org.consortiaToggle.add.label')}" controller="organisation" action="toggleCombo" params="${params+[direction:'add', fromOrg:org.id]}">
                            <i class="plus icon"></i>
                        </g:link>
                    </g:if>
                    <g:elseif test="${consortiaMemberIds.contains(org.id)}">
                        <g:link class="ui icon negative button" data-tooltip="${message(code:'org.consortiaToggle.remove.label')}" controller="organisation" action="toggleCombo" params="${params+[direction:'remove', fromOrg:org.id]}">
                            <i class="minus icon"></i>
                        </g:link>
                    </g:elseif>
                </td>
            </g:if>

            <g:if test="${tmplConfigShow?.contains('addSubMembers')}">
                <g:if test="${subInstance?.packages}">
                    <td><g:each in="${subInstance?.packages}" >
                        <g:checkBox type="text" id="selectedPackage_${org.id+it.pkg.id}" name="selectedPackage_${org.id+it.pkg.id}" value="1"
                                    checked="false" onclick="checkselectedPackage(${org.id+it.pkg.id});"/> ${it.pkg.name}<br>
                        </g:each>
                    </td>
                    <td><g:each in="${subInstance?.packages}" >
                        <g:checkBox type="text" id="selectedIssueEntitlement_${org.id+it.pkg.id}"
                                    name="selectedIssueEntitlement_${org.id+it.pkg.id}" value="1" checked="false"
                                    onclick="checkselectedIssueEntitlement(${org.id+it.pkg.id});"/> ${it.pkg.name}<br>
                    </g:each>
                    </td>
                </g:if><g:else>
                    <td>${message(code: 'subscription.details.addMembers.option.noPackage.label', args: [subInstance?.name])}</td>
                    <td>${message(code: 'subscription.details.addMembers.option.noPackage.label', args: [subInstance?.name])}</td>
                </g:else>
            </g:if>

            <g:if test="${tmplConfigShow?.contains('surveySubInfo')}">
                <td>
                    <g:if test="${com.k_int.kbplus.Subscription.get(surveyConfig?.subscription?.id)?.getDerivedSubscribers()?.id?.contains(org?.id)}">
                        <g:link controller="subscription" action="show"
                                id="${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(org)?.id}">
                            ${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(org)?.dropdownNamingConvention()}
                        </g:link>

                    </g:if>
                </td>
            </g:if>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${tmplShowCheckbox}">
    <script language="JavaScript">
        $('#orgListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
            }
            else {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
            }
        })
        <g:if test="${tmplConfigShow?.contains('addSubMembers')}">
            function checkselectedIssueEntitlement(selectedid) {
                if ($('#selectedIssueEntitlement_' + selectedid).prop('checked')) {
                    $('#selectedPackage_' + selectedid).prop('checked', false);
                }
            }
            function checkselectedPackage(selectedid) {
                if ($('#selectedPackage_' + selectedid).prop('checked')) {
                    $('#selectedIssueEntitlement_' + selectedid).prop('checked', false);
                }

            }
        </g:if>
    </script>
</g:if>
