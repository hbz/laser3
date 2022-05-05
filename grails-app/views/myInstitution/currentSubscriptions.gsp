<%@ page import="de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:serviceInjection />
<!doctype html>

<html>
    <head>
        <meta name="layout" content="laser" />
        <title>${message(code:'laser')} : ${message(code:'myinst.currentSubscriptions.label')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <a class="item" data-semui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
                </semui:exportDropdownItem>
                <g:if test="${filterSet || defaultSet}">
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[exportXLS:true]}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[format:'csv']}">
                            ${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:if>
                <g:else>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                    </semui:exportDropdownItem>
                </g:else>
            </semui:exportDropdown>

            <g:if test="${accessService.checkPermX('ORG_INST,ORG_CONSORTIUM', 'ROLE_ADMIN')}">
                <laser:render template="actions" />
            </g:if>
        </semui:controlButtons>

        <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'myinst.currentSubscriptions.label')}
            <semui:totalNumber total="${num_sub_rows}"/>
        </h1>

    <semui:messages data="${flash}"/>

    <laser:render template="/templates/subscription/subscriptionFilter"/>

    <laser:render template="/templates/subscription/subscriptionTable"/>

    <semui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>

    <laser:render template="export/individuallyExportModalSubs" model="[modalID: 'individuallyExportModal']" />

  </body>
</html>
