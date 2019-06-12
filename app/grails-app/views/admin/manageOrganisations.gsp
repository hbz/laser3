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

    <table class="ui sortable celled la-table table">
        <g:set var="sqlDateToday" value="${new java.sql.Date(System.currentTimeMillis())}"/>

        <thead>
            <tr>

                <th>${message(code:'sidewide.number')}</th>

                <th>${message(code: 'org.sortname.label', default: 'Sortname')}</th>

                <th>${message(code: 'org.fullName.label', default: 'Name')}</th>

                <%-- <th>${message(code: 'org.mainContact.label', default: 'Main Contact')}</th> --%>

                <%-- <th>Identifier</th> --%>

                <th>${message(code: 'org.type.label', default: 'Type')}</th>

                <%-- <th>${message(code: 'org.sector.label', default: 'Sector')}</th> --%>

                <%-- <th>${message(code: 'org.libraryNetworkTableHead.label')}</th> --%>

                <%-- <th>${message(code: 'org.libraryType.label')}</th> --%>

                <th>${message(code:'org.customerType.label')}</th>

                <th>${message(code:'org.apiLevel.label')}</th>

                <th>${message(code:'org.hasAccessOrg')}</th>

                <th>${message(code:'default.actions')}</th>
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
                            <span data-tooltip="Diese Organisation wurde as 'gelöscht' markiert." data-position="top left">
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

                    <%--
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
                    --%>

                    <%--
                    <td>
                        <g:if test="${org.ids}">
                            <div class="ui list">
                                <g:each in="${org.ids.sort{it.identifier.ns.ns}}" var="id"><div class="item">${id.identifier.ns.ns}: ${id.identifier.value}</div></g:each>
                            </div>
                        </g:if>
                    </td>
                    --%>

                    <td>
                        <g:each in="${org.orgType?.sort{it?.getI10n("value")}}" var="type">
                            ${type.getI10n("value")}
                        </g:each>
                    </td>

                    <%--
                    <td>${org.sector?.getI10n('value')}</td>
                    --%>

                    <%--
                    <td>${org.libraryNetwork?.getI10n('value')}</td>
                    --%>

                    <%--
                    <td>${org.libraryType?.getI10n('value')}</td>
                    --%>

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

                            def accessStatistics = OrgSettings.get(org, OrgSettings.KEYS.STATISTICS_SERVER_ACCESS)
                            if (accessStatistics != OrgSettings.SETTING_NOT_FOUND) {
                                if (accessStatistics.getValue()?.value == 'Yes') {
                                    println '<div><i class="ui icon lock open"></i> Statistikserver</div>'
                                }
                            }

                            def accessOA2020 = OrgSettings.get(org, OrgSettings.KEYS.OA2020_SERVER_ACCESS)
                            if (accessOA2020 != OrgSettings.SETTING_NOT_FOUND) {
                                if (accessOA2020.getValue()?.value == 'Yes') {
                                    println '<div><i class="ui icon lock open"></i> OA2020</div>'
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
                            <button type="button" class="ui icon button"
                                    data-gascoTarget="${Org.class.name}:${org.id}" data-orgName="${org.name}"
                                    data-semui="modal" data-href="#gascoEntryModal"
                                    data-tooltip="GASCO-Eintrag ändern" data-position="top left"><i class="globe icon"></i></button>
                        </g:if>

                        <button type="button" class="ui icon button"
                                data-ctTarget="${Org.class.name}:${org.id}" data-orgName="${org.name}"
                                data-semui="modal" data-href="#customerTypeModal"
                                data-tooltip="Kundentyp ändern" data-position="top left"><i class="user icon"></i></button>

                        <button type="button" class="ui icon button"
                                data-alTarget="${Org.class.name}:${org.id}" data-orgName="${org.name}"
                                data-semui="modal" data-href="#apiLevelModal"
                                data-tooltip="API-Zugriff ändern" data-position="top left"><i class="key icon"></i></button>
                    </td>
                </tr>

            </g:each>

        </tbody>
    </table>

    <%-- changing gasco entry --%>

    <semui:modal id="gascoEntryModal" message="org.gascoEntry.label" editmodal="editmodal">

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

    <semui:modal id="customerTypeModal" message="org.customerType.label" editmodal="editmodal">

        <g:form class="ui form" url="[controller: 'admin', action: 'manageOrganisations']">
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

        <r:script>
            $('button[data-ctTarget]').on('click', function(){

                $('#customerTypeModal #orgName_ct').attr('value', $(this).attr('data-orgName'))
                $('#customerTypeModal input[name=target]').attr('value', $(this).attr('data-ctTarget'))
            })
        </r:script>

    </semui:modal>

    <%-- changing api access --%>

    <semui:modal id="apiLevelModal" message="org.apiLevel.label" editmodal="editmodal">

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
