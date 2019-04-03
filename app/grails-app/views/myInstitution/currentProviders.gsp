<%@ page import="com.k_int.kbplus.RefdataValue" %>
<!doctype html>

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.my.providers')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.my.providers" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:if test="${filterSet}">
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial', default: 'Achtung!  Dennoch fortfahren?')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentProviders"
                                params="${params+[exportXLS:'yes']}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link class="item" action="currentProviders" params="${params+[exportXLS:'yes']}">${message(code:'default.button.exports.xls', default:'XLS Export')}</g:link>
                    </g:else>
                </semui:exportDropdownItem>
            </semui:exportDropdown>
        </semui:controlButtons>


    <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.my.providers" />
        <semui:totalNumber total="${orgListTotal}"/>
    </h1>

    <semui:messages data="${flash}" />
    <semui:filter>
        <g:form action="currentProviders" method="get" class="ui form">
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
  </body>
</html>
