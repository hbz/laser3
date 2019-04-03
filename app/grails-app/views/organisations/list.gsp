 <%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="menu.public.all_orgs" /></title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.public.all_orgs" class="active" />
        </semui:breadcrumbs>


            <semui:controlButtons>
                <semui:exportDropdown>
                    <semui:exportDropdownItem>
                        <g:if test="${filterSet}">
                            <g:link class="item js-open-confirm-modal"
                                    data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                                    data-confirm-term-how="ok" controller="organisations" action="list"
                                    params="${params+[exportXLS:'yes']}">
                                ${message(code:'default.button.exports.xls')}
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link class="item" action="list" params="${params+[exportXLS:'yes']}">${message(code:'default.button.exports.xls', default:'XLS Export')}</g:link>
                        </g:else>
                    </semui:exportDropdownItem>
                </semui:exportDropdown>
                    <g:render template="actions" />
            </semui:controlButtons>


        <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.public.all_orgs" />
            <semui:totalNumber total="${orgListTotal}"/>
        </h1>

        <semui:messages data="${flash}" />

        <semui:filter>
            <g:form action="list" method="get" class="ui form">
                <g:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow: [['name', 'type'], ['sector', 'federalState', 'libraryNetwork', 'libraryType']],
                                  tmplConfigFormFilter: true,
                                  useNewLayouter: true
                          ]"/>
            </g:form>
        </semui:filter>

        <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'sortname', 'name', 'wibid', 'isil', 'type', 'sector', 'federalState', 'libraryNetwork', 'libraryType']
              ]"/>

        <semui:paginate total="${orgListTotal}" params="${params}" />

    </body>
</html>
