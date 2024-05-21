<%@ page import="de.laser.Provider; de.laser.ExportClickMeService; de.laser.RefdataValue" %>

<laser:htmlStart message="menu.my.providers" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.my.providers" class="active" />
        </ui:breadcrumbs>

        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.PROVIDERS]"/>
                </ui:exportDropdownItem>
            </ui:exportDropdown>
            <ui:actionsDropdown>
                <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </ui:actionsDropdown>

        </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.my.providers" total="${providersTotal}" floated="true" />

    <ui:messages data="${flash}" />

    <ui:filter>
        <g:form action="currentProviders" method="get" class="ui form">
            <laser:render template="/templates/filter/providerFilter"
                      model="[
                              propList: propList,
                              tmplConfigShow: [['name', 'identifier', 'provStatus'], ['inhouseInvoicing', 'electronicBillings', 'invoiceDispatchs', 'invoicingVendors'], ['subStatus', 'subPerpetualAccess'], ['property&value', 'isMyX']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <div class="la-clear-before">
        <g:if test="${providerList}">
            <laser:render template="/templates/filter/providerFilterTable"
                      model="[provList: providerList,
                              tmplShowCheckbox: false,
                              tmplConfigShow: ['lineNumber', 'sortname', 'name', 'altname', 'currentSubscriptions', 'marker', 'isWekbCurated']
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

    <laser:render template="/templates/copyEmailaddresses" model="[orgList: providerList, instanceType: Provider.class.name]"/>

    <ui:paginate total="${providersTotal}" params="${params}" max="${max}" offset="${offset}" />

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>


<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
