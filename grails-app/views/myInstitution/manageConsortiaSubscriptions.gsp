<%@ page import="de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>

<laser:htmlStart message="menu.my.consortiaSubscriptions" serviceInjection="true"/>

    <g:set var="entityName" value="${message(code: 'org.label')}"/>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</ui:debugInfo>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.consortiaSubscriptions" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: 'consortiaParticipations']"/>
        </ui:exportDropdownItem>
        <%--
        <ui:exportDropdownItem>
            <g:if test="${filterSet || defaultSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageConsortiaSubscriptions"
                        params="${params+[exportXLS:true]}">
                    ${message(code:'default.button.exports.xls')}
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" controller="myInstitution" action="manageConsortiaSubscriptions" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
            </g:else>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:if test="${filterSet || defaultSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageConsortiaSubscriptions"
                        params="${params+[format:'csv']}">
                    ${message(code:'default.button.exports.csv')}
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" controller="myInstitution" action="manageConsortiaSubscriptions" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
            </g:else>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:if test="${filterSet || defaultSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageConsortiaSubscriptions"
                        params="${params+[exportPDF:true]}">
                    ${message(code:'default.button.exports.pdf')}
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" controller="myInstitution" action="manageConsortiaSubscriptions" params="${params+[exportPDF:true]}">${message(code:'default.button.exports.pdf')}</g:link>
            </g:else>
        </ui:exportDropdownItem>
        --%>
    </ui:exportDropdown>
    <ui:actionsDropdown>
        <ui:actionsDropdownItem data-ui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
    </ui:actionsDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="menu.my.consortiaSubscriptions" total="${totalSubsCount}" floated="true" />

<ui:messages data="${flash}"/>

<laser:render template="${customerTypeService.getConsortiaSubscriptionFilterTemplatePath()}"/>

<laser:render template="/templates/subscription/consortiaSubscriptionTable"/>

<laser:render template="/templates/copyEmailaddresses" model="[orgList: totalMembers]"/>

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
