<%@ page import="de.laser.api.v0.ApiManager; com.k_int.kbplus.OrgSettings; de.laser.helper.RDStore; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact; com.k_int.kbplus.Org; com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
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

                <th></th>
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
                                println customerType.getValue()?.getI10n('value')
                            }
                        %>
                    </td>

                    <td>
                        <%
                            def apiLevel = OrgSettings.get(org, OrgSettings.KEYS.API_LEVEL)
                            if (apiLevel != OrgSettings.SETTING_NOT_FOUND) {
                                println apiLevel.getValue()
                            }
                        %>
                    </td>

                    <td class="x">
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
                          from="${[RefdataValue.findByValue('generic.null.value')] + RefdataCategory.getAllRefdataValues('system.customer.type')}"
                          optionKey="id"
                          optionValue="value"
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
                          from="${['Kein Zugriff'] + ApiManager.getAllApiLevels()}"
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
