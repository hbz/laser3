<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.License; de.laser.interfaces.CalculatedType; de.laser.storage.RDStore; de.laser.Org" %>
<laser:serviceInjection />

<g:if test="${actionName == 'show'}">
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" action="show" target="_blank" params="[id: license.id, export: 'pdf']">Export PDF</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</g:if>

<g:if test="${contextService.isInstEditor(CustomerTypeService.ORG_SUPPORT)}">
    <ui:actionsDropdown>
        <laser:render template="/templates/sidebar/actions" />

        <g:if test="${editable}">
            <g:if test="${license.getLicensingConsortium()?.id == contextService.getOrg().id}">
                <g:if test="${!( license.instanceOf )}">
                    <div class="divider"></div>
                <%-- TODO integrate confirmation in actionsDropdownItem --%>
                    <g:link controller="license"
                            action="processAddMembers"
                            params="${[id:license.id, cmd:'generate']}"
                            class="item js-no-wait-wheel js-open-confirm-modal"
                            data-confirm-term-how="ok" data-confirm-tokenMsg="${message(code:'license.addMembers.confirm')}">
                        ${message(code:'myinst.emptyLicense.child')}
                    </g:link>
                </g:if>
            </g:if>

            <div class="divider"></div>

%{--            <g:if test="${license._getCalculatedType() == License.TYPE_CONSORTIAL}">--}%
            <g:if test="${license._getCalculatedType() in [License.TYPE_CONSORTIAL, License.TYPE_ADMINISTRATIVE]}">
                <ui:actionsDropdownItem controller="license" action="copyLicense" params="${[sourceObjectId: genericOIDService.getOID(license), copyObject: true]}" message="myinst.copyLicense" />
            </g:if>

            <ui:actionsDropdownItem controller="license" action="copyElementsIntoLicense" params="${[sourceObjectId: genericOIDService.getOID(license)]}" message="myinst.copyElementsIntoLicense" />

        </g:if>
        <g:if test="${actionName == 'show'}">
            <%-- the second clause is to prevent the menu display for consortia at member subscriptions --%>
            <%--<g:if test="${!(contextService.getOrg().id == license.getLicensingConsortium()?.id && license.instanceOf)}">
                <div class="divider"></div>
                <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />
            </g:if>--%>

            <g:if test="${editable}">
                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="${Icon.CMD.DELETE}"></i> ${message(code:'deletion.license')}</g:link>
            </g:if>
        </g:if>

        <g:if test="${editable && actionName == 'linkedSubs'}">
            <div class="divider"></div>
            <g:if test="${license.instanceOf}">
                <g:link class="item" action="linkMemberLicensesToSubs" id="${params.id}"><g:message code="license.linktoMemberSubscription"/></g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="linkLicenseToSubs" id="${params.id}"><g:message code="license.linktoSubscription"/></g:link>
            </g:else>
        </g:if>
    </ui:actionsDropdown>
</g:if>

<g:if test="${contextService.isInstEditor(CustomerTypeService.ORG_SUPPORT)}">
    <laser:render template="/templates/sidebar/modals" model="${[tmplConfig: [ownobj: license, owntp: 'license']]}" />
</g:if>
