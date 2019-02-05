<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'menu.institutions.myConsortiaLicenses')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="menu.institutions.myConsortiaLicenses" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'menu.institutions.myConsortiaLicenses')}</h1>

<semui:messages data="${flash}"/>

<h2>IN ARBEIT</h2>

<table id="costTable_${i}" data-queryMode="${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>Name / Teilnehmer / Vertrag</th>
            <th>Verknüpfte Pakete</th>
            <th>Anbieter</th>
            <th>Laufzeit von / bis</th>
            <th>getDerivedStartDate</th>
            <th>Endpreis</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${costItems}" var="entry" status="jj">
            <%
                com.k_int.kbplus.CostItem ci = entry[0]
                com.k_int.kbplus.Subscription subCons = entry[1]
                com.k_int.kbplus.Org subscr = entry[2]
            %>
            <tr>
                <td>
                    ${ jj + 1 }
                    <g:if test="${ci.isVisibleForSubscriber}">
                        <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                            <i class="ui icon eye orange"></i>
                        </span>
                    </g:if>
                </td>
                <td>
                    <div class="la-flexbox">
                        <i class="icon balance scale la-list-icon"></i>
                        <g:link controller="subscriptionDetails" action="show" id="${subCons.id}">${subCons}</g:link>
                    </div>
                    <div class="la-flexbox">
                        <i class="icon university la-list-icon"></i>
                        <g:link controller="organisations" action="show" id="${subscr.id}">${subscr}</g:link>
                    </div>
                    <g:if test="${subCons.owner}">
                        <div class="la-flexbox">
                            <i class="icon folder open outline la-list-icon"></i>
                            <g:link controller="licenseDetails" action="show" id="${subCons.owner.id}">${subCons.owner}</g:link>
                        </div>
                    </g:if>
                </td>
                <td>
                    <g:each in="${subCons.packages}" var="subPkg">
                        <div class="la-flexbox">
                            <g:link controller="packageDetails" action="show" id="${subPkg.pkg.id}">${subPkg.pkg.name}</g:link>
                        </div>
                    </g:each>
                </td>
                <td>
                    <g:each in="${subCons.providers}" var="p">
                        <g:link controller="organisations" action="show" id="${p.id}">${p}</g:link> <br/>
                    </g:each>
                </td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </g:each>
    </tbody>
</table>
<%--
    <semui:filter>
        <g:form action="manageConsortia" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name', 'libraryType'], ['federalState', 'libraryNetwork','property']],
                              tmplConfigFormFilter: true,
                              useNewLayouter: true
                      ]"/>
        </g:form>
    </semui:filter>
--%>

<%--
    <g:form action="manageConsortia" controller="myInstitution" method="post" class="ui form">

        <g:render template="/templates/filter/orgFilterTable"
                  model="[orgList: consortiaMembers,
                          tmplShowCheckbox: true,
                          tmplConfigShow: ['sortname', 'name', 'mainContact', 'currentFTEs', 'numberOfSubscriptions', 'libraryType']
                  ]"/>


        <g:if test="${consortiaMembers}">
            <input type="submit" class="ui button" onclick="if(confirm('Wollen Sie die ausgewählten Konsorten wirklich entfernen?')){return true;} return false;"
               value="${message(code: 'default.button.revoke.label', default: 'Revoke')}"/>
        </g:if>
    </g:form>
    <g:render template="../templates/copyEmailaddresses" model="[orgList: consortiaMembers]"/>
    <semui:paginate action="manageConsortia" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${consortiaMembersCount}" />
--%>

</body>
</html>
