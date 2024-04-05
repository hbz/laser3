<%@ page import="de.laser.RefdataValue" %>

<laser:htmlStart message="menu.my.vendors" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.my.vendors" class="active" />
        </ui:breadcrumbs>
        <%--
        to be implemented
        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
                </ui:exportDropdownItem>
            </ui:exportDropdown>
            <ui:actionsDropdown>
                <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </ui:actionsDropdown>

        </ui:controlButtons>
        --%>

    <ui:h1HeaderWithIcon message="menu.my.providers" total="${vendorListTotal}" floated="true" />

    <ui:messages data="${flash}" />

    <ui:filter>
        <g:form action="currentVendors" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name', 'venStatus', 'subStatus', 'subPerpetualAccess'], ['supportedLibrarySystems', 'electronicBillings', 'invoiceDispatchs'], ['curatoryGroup', 'curatoryGroupType', 'privateContacts']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <div class="la-clear-before">
        <g:if test="${vendorList}">
        <laser:render template="/templates/filter/orgFilterTable"
                  model="[orgList: vendorList,
                          tmplShowCheckbox: false,
                          tmplConfigShow: ['lineNumber', 'sortname', 'name', 'platform', 'currentSubscriptions', 'marker', 'isWekbCurated']
                  ]"/>
        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"default.agency.plural.label")]}"/></strong>
            </g:if>
            <g:else>
                <br /><strong><g:message code="result.empty.object" args="${[message(code:"default.agency.plural.label")]}"/></strong>
            </g:else>
        </g:else>
    </div>

    <%--
    to be implemented
    <laser:render template="/templates/copyEmailaddresses" model="[vendorList: vendorList]"/>
    --%>

    <ui:paginate total="${vendorListTotal}" params="${params}" max="${max}" offset="${offset}" />

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

    <%--
    to be implemented
    <laser:render template="export/individuallyExportModalVendors" model="[modalID: 'individuallyExportModal', contactSwitch: true]" />
    --%>

<laser:htmlEnd />
