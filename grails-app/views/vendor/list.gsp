<%@ page import="de.laser.*" %>
<laser:htmlStart message="menu.public.all_vendors" serviceInjection="true" />

        <g:set var="entityName" value="${message(code: 'default.agency.label')}" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.public.all_vendors" class="active" />
        </ui:breadcrumbs>

    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.VENDORS]"/>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    </ui:controlButtons>

        <ui:h1HeaderWithIcon message="menu.public.all_vendors" total="${vendorListTotal}" floated="true" />

        <ui:messages data="${flash}" />

        <ui:filter>
            <g:form action="list" method="get" class="ui form">
                <laser:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow: [['name', 'venStatus'], ['supportedLibrarySystems', 'electronicBillings', 'invoiceDispatchs'], ['curatoryGroup', 'curatoryGroupType']],
                                  tmplConfigFormFilter: true
                          ]"/>
            </g:form>
        </ui:filter>
        <div class="la-clear-before">
            <g:if test="${vendorList}">
                <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList: vendorList,
                              tmplShowCheckbox: false,
                              tmplConfigShow: ['lineNumber', 'sortname', 'name', 'platform', 'marker', 'isWekbCurated']
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
        <ui:paginate total="${vendorListTotal}" params="${params}" max="${max}" offset="${offset}" />

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
