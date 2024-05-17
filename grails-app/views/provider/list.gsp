<%@ page import="de.laser.*" %>
<laser:htmlStart message="menu.public.all_providers" serviceInjection="true" />

        <g:set var="entityName" value="${message(code: 'default.provider.label')}" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.public.all_providers" class="active" />
        </ui:breadcrumbs>

    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.PROVIDERS]"/>
            </ui:exportDropdownItem>
            <%--
            <g:if test="${filterSet}">
                <ui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="organisations" action="listProvider"
                            params="${params+[exportXLS:true]}">
                        ${message(code:'default.button.exports.xls')}
                    </g:link>
                </ui:exportDropdownItem>
                <ui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="organisations" action="listProvider"
                            params="${params+[format:'csv']}">
                        ${message(code:'default.button.exports.csv')}
                    </g:link>
                </ui:exportDropdownItem>
            </g:if>
            <g:else>
                <ui:exportDropdownItem>
                    <g:link class="item" action="listProvider" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                </ui:exportDropdownItem>
                <ui:exportDropdownItem>
                    <g:link class="item" action="listProvider" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                </ui:exportDropdownItem>
            </g:else>
            --%>
        </ui:exportDropdown>

            <g:if test="${contextService.is_ORG_COM_EDITOR_or_ROLEADMIN()}">
                <ui:actionsDropdown>
                    <ui:actionsDropdownItem controller="organisation" action="findProviderMatches" message="org.create_new_provider.label"/>
                </ui:actionsDropdown>
            </g:if>
    </ui:controlButtons>

        <ui:h1HeaderWithIcon message="menu.public.all_providers" total="${orgListTotal}" floated="true" />

        <ui:messages data="${flash}" />

        <ui:filter>
            <g:form action="list" method="get" class="ui form">
                <laser:render template="/templates/filter/providerFilter"
                          model="[
                                  tmplConfigShow: [['name', 'identifier', 'provStatus'], ['inhouseInvoicing', 'electronicBillings', 'invoiceDispatchs', 'invoicingVendors'], ['curatoryGroup', 'curatoryGroupType'], ['property&value', 'isMyX']],
                                  tmplConfigFormFilter: true
                          ]"/>
            </g:form>
        </ui:filter>
        <div class="la-clear-before">
            <g:if test="${providerList}">
                <laser:render template="/templates/filter/providerFilterTable"
                      model="[providerList: providerList,
                              apiSource: wekbApi,
                              tmplShowCheckbox: false,
                              tmplConfigShow: ['lineNumber', 'sortname', 'name', 'altname', 'platform', 'isMyX', 'marker', 'isWekbCurated']
                      ]"/>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"default.provider.label")]}"/></strong>
                </g:if>
                <g:else>
                    <br /><strong><g:message code="result.empty.object" args="${[message(code:"default.provider.label")]}"/></strong>
                </g:else>
            </g:else>
        </div>
        <ui:paginate total="${providersTotal}" params="${params}" max="${max}" offset="${offset}" />

        <g:render template="/clickMe/export/js"/>
<laser:htmlEnd />
