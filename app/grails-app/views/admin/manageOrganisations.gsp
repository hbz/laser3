<%@ page import="de.laser.api.v0.ApiToolkit; de.laser.api.v0.ApiManager; com.k_int.kbplus.auth.Role; com.k_int.kbplus.OrgSettings; de.laser.helper.RDStore; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact; com.k_int.kbplus.Org; com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection />
<!doctype html>

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.admin.manageOrganisations')}</title>
    </head>
    <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
        <semui:crumb message="menu.admin.manageOrganisations" class="active" />
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.admin.manageOrganisations" />
        <semui:totalNumber total="${orgListTotal}"/>
    </h1>

    <semui:filter>
        <g:form action="manageOrganisations" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name', 'identifier', 'type'], ['federalState', 'libraryNetwork', 'sector', 'libraryType']],
                              tmplConfigFormFilter: true,
                              useNewLayouter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <table class="ui sortable celled la-table table">
        <g:set var="sqlDateToday" value="${new java.sql.Date(System.currentTimeMillis())}"/>

        <thead>
            <tr>
                <th>${message(code:'sidewide.number')}</th>
                <th>${message(code: 'org.sortname.label', default: 'Sortname')}</th>
                <th>${message(code: 'org.fullName.label', default: 'Name')}</th>
                <th>${message(code: 'org.type.label', default: 'Type')}</th>
                <th>${message(code:'org.customerType.label')}</th>
                <th>${message(code:'org.apiLevel.label')}</th>
                <th>${message(code:'org.hasAccessOrg')}</th>
                <th class="la-action-info">${message(code:'default.actions')}</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${orgList}" var="org" status="i">
                <tr>
                    <td class="center aligned">
                        ${ (params.int('offset') ?: 0)  + i + 1 }<br>
                    </td>

                    <td>
                        ${org.sortname}

                        <g:if test="${org.status?.value == 'Deleted'}">
                            <span  class="la-popup-tooltip la-delay" data-content="Diese Organisation wurde as 'gelöscht' markiert." data-position="top left">
                                <i class="icon minus circle red"></i>
                            </span>
                        </g:if>
                    </td>

                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}">
                            ${fieldValue(bean: org, field: "name")} <br>
                            <g:if test="${org.shortname}">
                                (${fieldValue(bean: org, field: "shortname")})
                            </g:if>
                        </g:link>
                    </td>

                    <td>
                        <g:each in="${org.orgType?.sort{it?.getI10n("value")}}" var="type">
                            ${type.getI10n("value")}
                        </g:each>
                    </td>

                    <td>
                        <%
                            def customerType = OrgSettings.get(org, OrgSettings.KEYS.CUSTOMER_TYPE)
                            if (customerType != OrgSettings.SETTING_NOT_FOUND) {
                                println customerType.getValue()?.getI10n('authority')
                            }

                            def gascoEntry = OrgSettings.get(org, OrgSettings.KEYS.GASCO_ENTRY)
                            if (gascoEntry != OrgSettings.SETTING_NOT_FOUND && gascoEntry.getValue()?.value == 'Yes') {
                                println '<i class="icon green globe"></i>'
                            }
                        %>
                    </td>

                    <td>
                        <%
                            def apiLevel = OrgSettings.get(org, OrgSettings.KEYS.API_LEVEL)
                            if (apiLevel != OrgSettings.SETTING_NOT_FOUND) {
                                println '<div>' + apiLevel.getValue() + '</div>'
                            }

                            def accessStatistics = OrgSettings.get(org, OrgSettings.KEYS.NATSTAT_SERVER_ACCESS)
                            if (accessStatistics != OrgSettings.SETTING_NOT_FOUND) {
                                if (accessStatistics.getValue()?.value == 'Yes') {
                                    println '<div><i class="ui icon lock open"></i> Statistikserver</div>'
                                }
                            }

                            def accessOA = OrgSettings.get(org, OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS)
                            if (accessOA!= OrgSettings.SETTING_NOT_FOUND) {
                                if (accessOA.getValue()?.value == 'Yes') {
                                    println '<div><i class="ui icon lock open"></i> OAMontior</div>'
                                }
                            }
                        %>
                    </td>
                    <td>
                        <g:set var="accessUserList" value="${org?.hasAccessOrgListUser()}"/>

                        <g:if test="${accessUserList?.instAdms?.size() > 0}">
                            Inst_Admins: ${accessUserList?.instAdms?.size()}<br>
                        </g:if>

                        <g:if test="${accessUserList?.instEditors?.size() > 0}">
                            Inst_Editors: ${accessUserList?.instEditors?.size()}<br>
                        </g:if>

                        <g:if test="${accessUserList?.instUsers?.size() > 0}">
                            Inst_Users: ${accessUserList?.instUsers?.size()}<br>
                        </g:if>

                    </td>

                    <td class="x">
                        <g:if test="${org.hasPerm('org_consortium')}">
                            <button type="button" class="ui icon button la-popup-tooltip la-delay"
                                    data-gascoTarget="${Org.class.name}:${org.id}" data-orgName="${org.name}"
                                    data-semui="modal" data-href="#gascoEntryModal"
                                    data-content="GASCO-Eintrag ändern" data-position="top left"><i class="globe icon"></i></button>
                        </g:if>

                        <button type="button" class="ui icon button la-popup-tooltip la-delay"
                                data-ctTarget="${Org.class.name}:${org.id}" data-orgName="${org.name}"
                                data-semui="modal" data-href="#customerTypeModal"
                                data-content="Kundentyp ändern" data-position="top left"><i class="user icon"></i></button>

                        <button type="button" class="ui icon button la-popup-tooltip la-delay"
                                data-alTarget="${Org.class.name}:${org.id}" data-orgName="${org.name}"
                                data-semui="modal" data-href="#apiLevelModal"
                                data-content="API-Zugriff ändern" data-position="top left"><i class="key icon"></i></button>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <%-- changing gasco entry --%>

    <semui:modal id="gascoEntryModal" message="org.gascoEntry.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="changeGascoEntry"/>
            <input type="hidden" name="target" value="" />

            <div class="field">
                <label for="orgName_ct">${message(code:'org.label')}</label>
                <input type="text" id="orgName_gasco" name="orgName" value="" readonly />
            </div>

            <div class="field">
                <label for="gascoEntry">${message(code:'org.gascoEntry.label')}</label>
                <laser:select id="gascoEntry" name="gascoEntry"
                              from="${RefdataCategory.getAllRefdataValues('YN')}"
                              optionKey="${{'com.k_int.kbplus.RefdataValue:' + it.id}}"
                              optionValue="value"
                              class="ui dropdown"
                />
            </div>
        </g:form>

        <r:script>
            $('button[data-gascoTarget]').on('click', function(){

                $('#gascoEntryModal #orgName_gasco').attr('value', $(this).attr('data-orgName'))
                $('#gascoEntryModal input[name=target]').attr('value', $(this).attr('data-gascoTarget'))
            })
        </r:script>

    </semui:modal>

    <%-- changing customer type --%>

    <semui:modal id="customerTypeModal" message="org.customerType.label" isEditModal="isEditModal" formID="customerTypeChangeForm"
                 showDeleteButton="showDeleteButton" deleteFormID="customerTypeDeleteForm" msgDelete="Kundentyp löschen">

        <g:form id="customerTypeChangeForm" class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="changeCustomerType"/>
            <input type="hidden" name="target" value="" />

            <div class="field">
                <label for="orgName_ct">${message(code:'org.label')}</label>
                <input type="text" id="orgName_ct" name="orgName" value="" readonly />
            </div>

            <div class="field">
                <label for="customerType">${message(code:'org.customerType.label')}</label>
                <laser:select id="customerType" name="customerType"
                          from="${[Role.findByAuthority('FAKE')] + Role.findAllByRoleType('org')}"
                          optionKey="id"
                          optionValue="authority"
                          class="ui dropdown"
                />
            </div>
        </g:form>

        <g:form id="customerTypeDeleteForm" class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="deleteCustomerType"/>
            <input type="hidden" name="target" value=""/>
        </g:form>

        <r:script>
            $('button[data-ctTarget]').on('click', function(){

                $('#customerTypeModal #orgName_ct').attr('value', $(this).attr('data-orgName'))
                $('#customerTypeModal input[name=target]').attr('value', $(this).attr('data-ctTarget'))
            })
        </r:script>

    </semui:modal>

    <%-- changing api access --%>

    <semui:modal id="apiLevelModal" message="org.apiLevel.label" isEditModal="isEditModal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
            <input type="hidden" name="cmd" value="changeApiLevel"/>
            <input type="hidden" name="target" value=""/>

            <div class="field">
                <label for="orgName_al">${message(code:'org.label')}</label>
                <input type="text" id="orgName_al" name="orgName" value="" readonly />
            </div>

            <div class="field">
                <label for="apiLevel">${message(code:'org.apiLevel.label')}</label>
                <g:select id="apiLevel" name="apiLevel"
                          from="${['Kein Zugriff'] + ApiToolkit.getAllApiLevels()}"
                          class="ui dropdown"
                />
            </div>
        </g:form>

        <r:script>
            $('button[data-alTarget]').on('click', function(){

                $('#apiLevelModal #orgName_al').attr('value', $(this).attr('data-orgName'))
                $('#apiLevelModal input[name=target]').attr('value', $(this).attr('data-alTarget'))
            })
        </r:script>

    </semui:modal>

    </body>
</html>
