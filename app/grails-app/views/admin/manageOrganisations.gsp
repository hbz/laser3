<%@ page import="de.laser.SubscriptionsQueryService; de.laser.helper.RDStore; com.k_int.kbplus.Subscription; java.text.SimpleDateFormat; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Numbers; com.k_int.kbplus.License; com.k_int.kbplus.Contact; com.k_int.kbplus.Org; com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection />
<!doctype html>

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : Organisationen</title>
    </head>
    <body>

    <h2> TODO </h2>
    <%--
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.my.providers" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.my.providers" />
            <semui:totalNumber total="${orgListTotal}"/>
        </h1>
--%>
    <%--
    <semui:messages data="${flash}" />
    <semui:filter>
        <g:form action="manageOrganisations" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              propList: propList,
                              orgRoles: orgRoles,
                              tmplConfigShow: [['name', 'role'], ['country', 'property']],
                              tmplConfigFormFilter: true,
                              useNewLayouter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'shortname', 'name', 'privateContacts', 'numberOfSubscriptions']
              ]"/>
    <semui:paginate total="${orgListTotal}" params="${params}" />
    --%>



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

                <th>${message(code: 'org.sector.label', default: 'Sector')}</th>

                <th>${message(code: 'org.libraryNetworkTableHead.label')}</th>

                <th>${message(code: 'org.libraryType.label')}</th>

            </tr>
        </thead>
        <tbody>
            <g:each in="${orgList}" var="org" status="i">

                <tr>

                    <td class="center aligned">
                        ${ (params.int('offset') ?: 0)  + i + 1 }<br>
                    </td>

                    <td>${org.sortname}</td>

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

                    <td>${org.sector?.getI10n('value')}</td>

                    <td>${org.libraryNetwork?.getI10n('value')}</td>

                    <td>${org.libraryType?.getI10n('value')}</td>

                </tr>

            </g:each>

        </tbody>
    </table>

    </body>
</html>
