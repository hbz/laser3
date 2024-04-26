<%@ page import="de.laser.ExportClickMeService; grails.plugin.springsecurity.SpringSecurityUtils;de.laser.CustomerTypeService; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>

<laser:htmlStart message="myinst.currentSubscriptions.label" serviceInjection="true" />

        <ui:breadcrumbs>
            <ui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </ui:breadcrumbs>

        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.SUBSCRIPTIONS]"/>
                </ui:exportDropdownItem>
                <%--
                <g:if test="${filterSet || defaultSet}">
                    <ui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[exportXLS:true]}">
                            ${message(code:'default.button.exports.xls')}
                        </g:link>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                data-confirm-term-how="ok" controller="myInstitution" action="currentSubscriptions"
                                params="${params+[format:'csv']}">
                            ${message(code:'default.button.exports.csv')}
                        </g:link>
                    </ui:exportDropdownItem>
                </g:if>
                <g:else>
                    <ui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <g:link class="item" controller="myInstitution" action="currentSubscriptions" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                    </ui:exportDropdownItem>
                </g:else>
                --%>
            </ui:exportDropdown>

            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support() || contextService.getOrg().isCustomerType_Inst_Pro()}">
                <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
            </g:if>
        </ui:controlButtons>

    <ui:h1HeaderWithIcon message="myinst.currentSubscriptions.label" total="${num_sub_rows}" floated="true" />

    <ui:messages data="${flash}"/>

    <laser:render template="${customerTypeService.getSubscriptionFilterTemplatePath()}"/>

    <laser:render template="/templates/subscription/subscriptionTable"/>

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
