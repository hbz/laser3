<%@ page import="de.laser.RefdataValue" %>
<!doctype html>

<html>
    <head>
        <meta name="layout" content="laser" />
        <title>${message(code:'laser')} : ${message(code:'menu.my.providers')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.my.providers" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <a class="item" data-semui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
                </semui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentProviders"
                                params="${params+[exportXLS:true]}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentProviders"
                                params="${params+[format:'csv']}">
                            ${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:if>
                <g:else>
                    <semui:exportDropdownItem>
                        <g:link class="item" action="currentProviders" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item" action="currentProviders" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                    </semui:exportDropdownItem>
                </g:else>
            </semui:exportDropdown>
            <semui:actionsDropdown>

                <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>

            </semui:actionsDropdown>

        </semui:controlButtons>

    <semui:headerWithIcon message="menu.my.providers" floated="true">
        <semui:totalNumber total="${orgListTotal}"/>
    </semui:headerWithIcon>

    <semui:messages data="${flash}" />

    <laser:render template="/templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <g:form action="currentProviders" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              propList: propList,
                              orgRoles: orgRoles,
                              tmplConfigShow: [['name', 'role', 'property&value'], ['privateContacts', '', '', '']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <div class="la-clear-before">
        <g:if test="${orgList}">
        <laser:render template="/templates/filter/orgFilterTable"
                  model="[orgList: orgList,
                          tmplShowCheckbox: false,
                          tmplConfigShow: ['lineNumber', 'shortname', 'name', 'isWekbCurated', 'privateContacts', 'numberOfSubscriptions']
                  ]"/>
        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"default.ProviderAgency.label")]}"/></strong>
            </g:if>
            <g:else>
                <br /><strong><g:message code="result.empty.object" args="${[message(code:"default.ProviderAgency.label")]}"/></strong>
            </g:else>
        </g:else>
    </div>

    <laser:render template="/templates/copyEmailaddresses" model="[orgList: orgList]"/>

    <semui:paginate total="${orgListTotal}" params="${params}" max="${max}" offset="${offset}" />

    <semui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>

    <laser:render template="export/individuallyExportModalOrgs" model="[modalID: 'individuallyExportModal', orgType: 'provider']" />

  </body>
</html>
