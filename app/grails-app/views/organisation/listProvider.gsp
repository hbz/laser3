 <%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'default.provider.label', default: 'Provider')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="menu.public.all_provider" /></title>
    </head>
    <body>

    <laser:serviceInjection />

        <semui:breadcrumbs>
            <semui:crumb message="menu.public.all_provider" class="active" />
        </semui:breadcrumbs>

 <semui:controlButtons>
        <semui:exportDropdown>
            <g:if test="${filterSet}">
                <semui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="organisations" action="listProvider"
                            params="${params+[exportXLS:true]}">
                        ${message(code:'default.button.exports.xls')}
                    </g:link>
                </semui:exportDropdownItem>
                <semui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
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
                <g:render template="actions" />
            </g:if>
        </semui:controlButtons>

        <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.public.all_provider" />
            <semui:totalNumber total="${orgListTotal}"/>
        </h1>

        <semui:messages data="${flash}" />
        <semui:filter>
            <g:form action="listProvider" method="get" class="ui form">
                <g:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow: [['name'], ['country', 'property']],
                                  tmplConfigFormFilter: true,
                                  useNewLayouter: true
                          ]"/>
            </g:form>
        </semui:filter>
        <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'shortname', 'name', 'country']
              ]"/>
        <semui:paginate total="${orgListTotal}" params="${params}" />

    </body>
</html>
