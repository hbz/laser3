 <%@ page import="de.laser.*" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <g:set var="entityName" value="${message(code: 'default.provider.label')}" />
        <title>${message(code:'laser')} : <g:message code="menu.public.all_providers" /></title>
    </head>
    <body>

    <laser:serviceInjection />

        <semui:breadcrumbs>
            <semui:crumb message="menu.public.all_providers" class="active" />
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
                            data-confirm-term-how="ok" controller="organisations" action="listProvider"
                            params="${params+[exportXLS:true]}">
                        ${message(code:'default.button.exports.xls')}
                    </g:link>
                </semui:exportDropdownItem>
                <semui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="organisations" action="listProvider"
                            params="${params+[format:'csv']}">
                        ${message(code:'default.button.exports.csv')}
                    </g:link>
                </semui:exportDropdownItem>
            </g:if>
            <g:else>
                <semui:exportDropdownItem>
                    <g:link class="item" action="listProvider" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                </semui:exportDropdownItem>
                <semui:exportDropdownItem>
                    <g:link class="item" action="listProvider" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                </semui:exportDropdownItem>
            </g:else>
        </semui:exportDropdown>

            <g:if test="${accessService.checkPermX('ORG_INST,ORG_CONSORTIUM', 'ROLE_ADMIN,ROLE_ORG_EDITOR') || accessService.checkConstraint_ORG_COM_EDITOR()}">
                <laser:render template="actions" />
            </g:if>
        </semui:controlButtons>

        <semui:h1HeaderWithIcon message="menu.public.all_providers" total="${orgListTotal}" floated="true" />

        <semui:messages data="${flash}" />
        <laser:render template="/templates/filter/javascript" />
        <semui:filter showFilterButton="true">
            <g:form action="listProvider" method="get" class="ui form">
                <laser:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow: [['name', 'identifier'], ['platform', '']],
                                  tmplConfigFormFilter: true
                          ]"/>
            </g:form>
        </semui:filter>
        <div class="la-clear-before">
            <g:if test="${orgList}">
                <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList: orgList,
                              tmplShowCheckbox: false,
                              tmplConfigShow: ['lineNumber', 'shortname', 'name', 'isWekbCurated', 'altname', 'platform']
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
        <semui:paginate total="${orgListTotal}" params="${params}" max="${max}" offset="${offset}" />

        <laser:render template="/myInstitution/export/individuallyExportModalOrgs" model="[modalID: 'individuallyExportModal', orgType: 'provider']" />

    </body>
</html>
