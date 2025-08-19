<%@ page import="de.laser.*" %>
<laser:htmlStart message="menu.public.all_providers" />

        <g:set var="entityName" value="${message(code: 'provider.label')}" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.public.all_providers" class="active" />
        </ui:breadcrumbs>

    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.PROVIDERS]"/>
            </ui:exportDropdownItem>
        </ui:exportDropdown>

            <g:if test="${contextService.is_ORG_COM_EDITOR_or_ROLEADMIN()}">
                <ui:actionsDropdown>
                    <ui:actionsDropdownItem controller="provider" action="findProviderMatches" message="org.create_new_provider.label"/>
                </ui:actionsDropdown>
            </g:if>
    </ui:controlButtons>

        <ui:h1HeaderWithIcon message="menu.public.all_providers" total="${providersTotal}" floated="true" />

        <ui:messages data="${flash}" />

        <ui:filter>
            <g:form action="list" method="get" class="ui form">
                <laser:render template="/templates/filter/providerFilter"
                          model="[
                                  tmplConfigShow: [['name', 'identifier', 'provStatus'], ['electronicBillings', 'invoiceDispatchs', 'invoicingVendors', 'inhouseInvoicing'],  ['property&value', 'isMyX']],
                                  tmplConfigFormFilter: true
                          ]"/>
            </g:form>
        </ui:filter>
        <div class="la-clear-before">
            <g:if test="${providerList}">
                <laser:render template="/templates/filter/providerFilterTable"
                      model="[providerList: providerList,
                              tmplShowCheckbox: false,
                              tmplConfigShow: ['lineNumber', 'abbreviatedName', 'name', 'altname', 'platform', 'isMyX', 'marker', 'isWekbCurated']
                      ]"/>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"provider.label")]}"/></strong>
                </g:if>
                <g:else>
                    <br /><strong><g:message code="result.empty.object" args="${[message(code:"provider.label")]}"/></strong>
                </g:else>
            </g:else>
        </div>
        <ui:paginate total="${providersTotal}" params="${params}" max="${max}" offset="${offset}" />

        <g:render template="/clickMe/export/js"/>
<laser:htmlEnd />
