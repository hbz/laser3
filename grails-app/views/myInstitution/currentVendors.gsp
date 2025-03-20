<%@ page import="de.laser.ExportClickMeService; de.laser.RefdataValue; de.laser.wekb.Vendor" %>

<laser:htmlStart message="menu.my.vendors" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.my.vendors" class="active" />
        </ui:breadcrumbs>
        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.VENDORS]"/>
                </ui:exportDropdownItem>
            </ui:exportDropdown>
            <ui:actionsDropdown>
                <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </ui:actionsDropdown>

        </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.my.vendors" total="${vendorListTotal}" floated="true" />

    <ui:messages data="${flash}" />

    <ui:filter>
        <g:form action="currentVendors" method="get" class="ui form">
            <laser:render template="/templates/filter/vendorFilter"
                      model="[
                              tmplConfigShow: [['name', 'venStatus'], ['supportedLibrarySystems', 'electronicBillings', 'invoiceDispatchs'], ['property&value', 'isMyX'], ['subStatus', 'subPerpetualAccess', '']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <div class="la-clear-before">
        <g:if test="${vendorList}">
        <laser:render template="/templates/filter/vendorFilterTable"
                  model="[orgList: vendorList,
                          tmplShowCheckbox: false,
                          tmplConfigShow: ['lineNumber', 'sortname', 'name', 'currentSubscriptions', 'marker', 'isWekbCurated']
                  ]"/>
        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"vendor.plural")]}"/></strong>
            </g:if>
            <g:else>
                <br /><strong><g:message code="result.empty.object" args="${[message(code:"vendor.plural")]}"/></strong>
            </g:else>
        </g:else>
    </div>

    <laser:render template="/templates/copyEmailaddresses" model="[orgList: allVendors, instanceType: Vendor.class.name]"/>

    <ui:paginate total="${vendorListTotal}" params="${params}" max="${max}" offset="${offset}" />

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

    <g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
