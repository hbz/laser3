<%@ page import="de.laser.helper.RDStore" %>
<laser:serviceInjection />

<%
    String title
    if(params.comboType == 'Consortium') {
        title = message(code: 'menu.institutions.manage_consortia')
    }
    else if(params.comboType == 'Department') {
        title = message(code: 'menu.institutions.manage_departments')
    }
%>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${title}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="${title}" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <g:if test="${filterSet}">
            <semui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal"
                        data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageMembers"
                        params="${params+[exportXLS:true]}">
                    ${message(code:'default.button.exports.xls')}
                </g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal"
                        data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageMembers"
                        params="${params+[format:'csv']}">
                    ${message(code:'default.button.exports.csv')}
                </g:link>
            </semui:exportDropdownItem>
        </g:if>
        <g:else>
            <semui:exportDropdownItem>
                <g:link class="item" action="manageMembers" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="manageMembers" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
            </semui:exportDropdownItem>
        </g:else>
    </semui:exportDropdown>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui left aligned icon header"><semui:headerIcon />${title}
<semui:totalNumber total="${membersCount}"/>
</h1>

<semui:messages data="${flash}"/>
    <%
        List configShow = []
        if(params.comboType == 'Consortium') {
            configShow = [['name', 'libraryType'], ['federalState', 'libraryNetwork','property']]
        }
        else if(params.comboType == 'Department') {
            configShow = [['name'], ['property']]
        }
    %>
    <semui:filter>
        <g:form action="'manageMembers'" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: configShow,
                              tmplConfigFormFilter: true,
                              useNewLayouter: true
                      ]"/>
        </g:form>
    </semui:filter>


    <g:form action="'manageMembers'" controller="myInstitution" method="post" class="ui form">

        <g:render template="/templates/filter/orgFilterTable"
                  model="[orgList: members,
                          tmplShowCheckbox: true,
                          comboType: params.comboType,
                          tmplConfigShow: ['name', 'mainContact', 'numberOfSubscriptions']
                  ]"/>


        <g:if test="${members && params.comboType == 'Consortium'}">
            <input type="submit" class="ui button" data-confirm-term-what="function" data-confirm-term-what-detail="${message(code:'members.confirmDelete')}"
                   data-confirm-term-how="delete" value="${message(code: 'default.button.revoke.label')}"/>
        </g:if>
    </g:form>
    <g:render template="../templates/copyEmailaddresses" model="[orgList: members]"/>
    <semui:paginate action="'manageMembers'" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${membersCount}" />

</body>
</html>
