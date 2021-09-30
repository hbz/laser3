 <%@ page import="de.laser.*" %>
 <laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <g:set var="entityName" value="${message(code: 'org.label')}" />
        <title>${message(code:'laser')} : <g:message code="menu.public.all_orgs" /></title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.public.all_orgs" class="active" />
        </semui:breadcrumbs>

            <semui:controlButtons>
                <semui:exportDropdown>
                    <g:if test="${filterSet}">
                        <semui:exportDropdownItem>
                            <g:link class="item js-open-confirm-modal"
                                    data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                    data-confirm-term-how="ok" controller="organisations" action="list"
                                    params="${params+[exportXLS:true]}">
                                ${message(code:'default.button.exports.xls')}
                            </g:link>
                        </semui:exportDropdownItem>
                        <semui:exportDropdownItem>
                            <g:link class="item js-open-confirm-modal"
                                    data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                    data-confirm-term-how="ok" controller="organisations" action="list"
                                    params="${params+[format:'csv']}">
                                ${message(code:'default.button.exports.csv')}
                            </g:link>
                        </semui:exportDropdownItem>
                    </g:if>
                    <g:else>
                        <semui:exportDropdownItem>
                            <g:link class="item" action="list" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                        </semui:exportDropdownItem>
                        <semui:exportDropdownItem>
                            <g:link class="item" action="list" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                        </semui:exportDropdownItem>
                    </g:else>
                </semui:exportDropdown>

                <g:if test="${accessService.checkPermX('ORG_INST,ORG_CONSORTIUM', 'ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                    <g:render template="actions" />
                </g:if>

            </semui:controlButtons>

        <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon /><g:message code="menu.public.all_orgs" />
            <semui:totalNumber total="${orgListTotal}"/>
        </h1>

        <semui:messages data="${flash}" />

        <g:render template="/templates/filter/javascript" />
        <semui:filter showFilterButton="true">
            <g:form action="list" method="get" class="ui form">
                <g:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow: [['name', 'identifier', 'type'],
                                                   ['country&region', 'libraryNetwork'],
                                                   ['sector', 'libraryType', 'subjectGroup']],
                                  tmplConfigFormFilter: true
                          ]"/>
            </g:form>
        </semui:filter>

        <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'sortname', 'name', 'status', 'wibid', 'isil', 'type', 'sector', 'region',
                                       'libraryNetwork', 'libraryType']
              ]"/>

        <semui:paginate total="${orgListTotal}" params="${params}" max="${max}" offset="${offset}" />

    </body>
</html>
