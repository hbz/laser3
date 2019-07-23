<%@ page import="de.laser.helper.RDStore; de.laser.interfaces.TemplateSupport; com.k_int.kbplus.License; com.k_int.kbplus.Org" %>
<laser:serviceInjection />
<g:set var="org" value="${contextService.org}"/>
<g:set var="user" value="${contextService.user}" />
<semui:actionsDropdown>

    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
    </g:if>
    <g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR')}">
        <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />
    </g:if>
    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <g:if test="${license.getLicensingConsortium()?.id == org.id && ! license.isTemplate()}">
            <g:if test="${!( license.instanceOf && ! license.hasTemplate())}">
                <div class="divider"></div>

                <semui:actionsDropdownItem controller="license" action="addMembers" params="${[id:license?.id]}" message="myinst.emptyLicense.child" />
            </g:if>
        </g:if>

        <div class="divider"></div>
        <%
            License license = License.get(license?.id)
            boolean isCopyLicenseEnabled = license?.orgLinks?.find{it.org.id == org.id && (it.roleType.id == RDStore.OR_LICENSING_CONSORTIUM.id || it.roleType.id == RDStore.OR_LICENSEE.id) }
        %>
        <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_YODA">
            <% isCopyLicenseEnabled = true %>
        </sec:ifAnyGranted>
        <g:if test="${isCopyLicenseEnabled}">
            <semui:actionsDropdownItem controller="license" action="copyLiceInstitutionsServicense" params="${[id:license?.id]}" message="myinst.copyLicense" />
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled controller="license" action="copyLiceInstitutionsServicense" params="${[id:license?.id]}" message="myinst.copyLicense" />
        </g:else>

        <g:if test="${actionName == 'show'}">
            <g:if test="${(license.getLicensingConsortium()?.id == org.id) || (license.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_LOCAL && license.getLicensee()?.id == org.id) && ! license.isTemplate()}">
                <div class="divider"></div>
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalsgruppen konfigurieren" />
            </g:if>

            <g:if test="${editable}">
                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate icon"></i> Vertrag löschen</g:link>
            </g:if>
            <g:else>
                <a class="item disabled" href="#"><i class="trash alternate icon"></i> Vertrag löschen</a>
            </g:else>

        </g:if>

    </g:if>
</semui:actionsDropdown>

<g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
    <g:render template="/templates/tasks/modal_create" model="${[ownobj:license, owntp:'license']}"/>
    <g:render template="/templates/documents/modal" model="${[ownobj:license, owntp:'license']}"/>
</g:if>
<g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR')}">
    <g:render template="/templates/notes/modal_create" model="${[ownobj: license, owntp: 'license']}"/>
</g:if>