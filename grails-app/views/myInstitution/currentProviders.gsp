<%@ page import="de.laser.RefdataValue" %>

<laser:htmlStart message="menu.my.providers" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.my.providers" class="active" />
        </ui:breadcrumbs>

        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <a class="item" data-ui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
                </ui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <ui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentProviders"
                                params="${params+[exportXLS:true]}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentProviders"
                                params="${params+[format:'csv']}">
                            ${message(code:'default.button.exports.csv')}
                        </g:link>
                    </ui:exportDropdownItem>
                </g:if>
                <g:else>
                    <ui:exportDropdownItem>
                        <g:link class="item" action="currentProviders" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <g:link class="item" action="currentProviders" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                    </ui:exportDropdownItem>
                </g:else>
            </ui:exportDropdown>
            <ui:actionsDropdown>

                <ui:actionsDropdownItem data-ui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>

            </ui:actionsDropdown>

        </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.my.providers" total="${orgListTotal}" floated="true" />

    <ui:messages data="${flash}" />

    <ui:filter>
        <g:form action="currentProviders" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              propList: propList,
                              orgRoles: orgRoles,
                              tmplConfigShow: [['name', 'role', 'property&value'], ['privateContacts', '', '', '']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <div class="la-clear-before">
        <g:if test="${orgList}">
        <laser:render template="/templates/filter/orgFilterTable"
                  model="[orgList: orgList,
                          tmplShowCheckbox: false,
                          tmplConfigShow: ['lineNumber', 'shortname', 'name', 'isWekbCurated', 'numberOfSubscriptions']
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

    <laser:render template="/templates/copyEmailaddresses" model="[orgList: orgList]"/>

    <ui:paginate total="${orgListTotal}" params="${params}" max="${max}" offset="${offset}" />

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

    <laser:render template="export/individuallyExportModalOrgs" model="[modalID: 'individuallyExportModal', orgType: 'provider']" />

<laser:htmlEnd />
