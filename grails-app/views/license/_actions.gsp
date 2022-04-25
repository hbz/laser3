<%@ page import="de.laser.License; de.laser.interfaces.CalculatedType; de.laser.storage.RDStore; de.laser.Org" %>
<laser:serviceInjection />

<g:if test="${actionName == 'show'}">
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="show" params="[id: license.id, export: 'pdf']">Export PDF</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</g:if>

<g:if test="${accessService.checkMinUserOrgRole(user,institution,'INST_EDITOR')}">
    <semui:actionsDropdown>

        <g:if test="${contextCustomerType in ["ORG_INST","ORG_CONSORTIUM"]}">
            <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
            <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
        </g:if>
        <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />
        <g:if test="${editable}">
            <g:if test="${license.getLicensingConsortium()?.id == institution.id}">
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

            <g:if test="${(contextCustomerType == "ORG_INST" && license._getCalculatedType() == License.TYPE_LOCAL) || (contextCustomerType == "ORG_CONSORTIUM" && license._getCalculatedType() == License.TYPE_CONSORTIAL)}">
                <semui:actionsDropdownItem controller="license" action="copyLicense" params="${[sourceObjectId: genericOIDService.getOID(license), copyObject: true]}" message="myinst.copyLicense" />
            </g:if>

            <g:if test="${(contextCustomerType == "ORG_INST" && !license.instanceOf) || contextCustomerType == "ORG_CONSORTIUM"}">
                <semui:actionsDropdownItem controller="license" action="copyElementsIntoLicense" params="${[sourceObjectId: genericOIDService.getOID(license)]}" message="myinst.copyElementsIntoLicense" />
            </g:if>

        </g:if>
        <g:if test="${actionName == 'show'}">
            <%-- the second clause is to prevent the menu display for consortia at member subscriptions --%>
            <g:if test="${accessService.checkPermAffiliation('ORG_INST, ORG_CONSORTIUM','INST_EDITOR') && !(institution.id == license.getLicensingConsortium()?.id && license.instanceOf)}">
                <div class="divider"></div>
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />
            </g:if>

            <g:if test="${editable}">
                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.license')}</g:link>
            </g:if>
            <g:else>
                <a class="item disabled" href="#"><i class="trash alternate outline icon"></i> ${message(code:'deletion.license')}</a>
            </g:else>
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
    </semui:actionsDropdown>
</g:if>

<g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
    <laser:render template="/templates/tasks/modal_create" model="${[ownobj:license, owntp:'license', validResponsibleUsers: taskService.getUserDropdown(institution)]}"/>
    <laser:render template="/templates/documents/modal" model="${[ownobj:license, owntp:'license']}"/>
</g:if>
<g:if test="${accessService.checkMinUserOrgRole(user,institution,'INST_EDITOR')}">
    <laser:render template="/templates/notes/modal_create" model="${[ownobj: license, owntp: 'license']}"/>
</g:if>