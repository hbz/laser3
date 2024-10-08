<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.PersonRole; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.Org; de.laser.Person; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>

<g:set var="entityName" value="${message(code: 'person.label')}"/>
<laser:htmlStart text="${message(code:"default.show.label", args:[entityName])}" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.public.all_orgs" controller="organisation" action="index" />
    <g:message code="default.show.label" args="[entityName]" class="active"/>
</ui:breadcrumbs>

<g:set var="personType" value="${!personInstance.contactType || personInstance.contactType?.id == RDStore.PERSON_CONTACT_TYPE_PERSONAL.id}" />

<ui:h1HeaderWithIcon text="${personInstance}" />

<ui:messages data="${flash}"/>

<div class="ui grid">
    <div class="twelve wide column">

        <div class="la-inline-lists">
            <div class="ui card">
                <div class="content">
                    <dl><dt>${RefdataCategory.getByDesc(RDConstants.PERSON_CONTACT_TYPE).getI10n('desc')}</dt>
                        <dd>
                            <ui:xEditableRefData owner="${personInstance}" field="contactType" config="${RDConstants.PERSON_CONTACT_TYPE}"/>

                            <laser:script file="${this.getGroovyPageFileName()}">
                                $('a[data-name=contactType]').on('save', function(e, params) {
                                    window.location.reload()
                                });
                            </laser:script>
                        </dd>
                    </dl>

                    <g:if test="${personType}">
                        <dl><dt id="person_title"><g:message code="person.title.label" /></dt>
                            <dd><ui:xEditable owner="${personInstance}" field="title"/></dd>
                        </dl>
                    </g:if>

                    <dl>
                        <dt id="person_last_name">
                            <g:if test="${personType}">
                                <g:message code="person.last_name.label" />
                            </g:if>
                            <g:else>
                                <g:message code="person.funktionName.label"/>
                            </g:else>
                        </dt>
                        <dd><ui:xEditable owner="${personInstance}" field="last_name"/></dd>
                    </dl>

                    <g:if test="${personType}">

                        <dl><dt><g:message code="person.first_name.label" /></dt>
                            <dd><ui:xEditable owner="${personInstance}" field="first_name"/></dd></dl>

                        <dl><dt><g:message code="person.middle_name.label" /></dt>
                            <dd><ui:xEditable owner="${personInstance}" field="middle_name"/></dd></dl>

                        <dl><dt><g:message code="person.gender.label" /></dt>
                            <dd><ui:xEditableRefData owner="${personInstance}" field="gender" config="${RDConstants.GENDER}"/></dd>
                        </dl>
                    </g:if>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">

                    <dl><dt><g:message code="person.contacts.label"/></dt>
                        <dd>
                            <div class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${personInstance?.contacts?.toSorted()}" var="c">

                                    <laser:render template="/templates/cpa/contact" model="${[
                                            contact: c,
                                            tmplShowDeleteButton: true,
                                            controller: 'person',
                                            action: 'show',
                                            id: personInstance.id
                                    ]}"/>

                                </g:each>
                            </div>
                            <g:if test="${editable}">
                                <input class="${Btn.SIMPLE}" type="button" data-ui="modal" data-href="#contactFormModal"
                                       value="${message(code: 'default.add.label', args: [message(code: 'person.contacts.label')])}">
                                <laser:render template="/contact/formModal" model="['prsId': personInstance?.id]"/>
                            </g:if>
                        </dd>
                    </dl>

                    <%--<dl>
                        <dt><g:message code="person.addresses.label" /></dt>
                        <dd>
                            <div class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${personInstance.addresses.sort{it.type.each {it?.getI10n('value')}}}" var="a">
                                    <laser:render template="/templates/cpa/address" model="${[
                                            address: a,
                                            editable            : editable,
                                            tmplShowDeleteButton: true,
                                            controller: 'person',
                                            action: 'show',
                                            id: personInstance.id
                                    ]}"/>

                                </g:each>
                            </div>
                            <g:if test="${editable}">
                                <% Map model = [:]
                                model.prsId = personInstance.id
                                model.redirect = '.'
                                model.typeId = RDStore.ADDRESS_TYPE_LIBRARY.id
                                model.hideType = true%>
                                <input class="${Btn.BASIC_ICON}" type="button"
                                       value="${message(code: 'default.add.label', args: [message(code: 'address.label')])}"
                                       onclick="JSPC.app.addresscreate_prs('${model.prsId}', '${model.typeId}', '${model.redirect}', '${model.modalId}', '${model.hideType}');"
                                >
                            </g:if>
                        </dd>
                    </dl>--%>

                </div>
            </div><!-- .card -->

            <g:if test="${!myPublicContact}">
                <div class="ui card">
                    <div class="content">

                        <dl><dt><g:message code="person.functions.label" /></dt>
                            <dd>
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${personInstance.roleLinks}" var="link">
                                        <g:if test="${link.functionType}">
                                            <div class="ui item address-details">
                                                <span class="la-popup-tooltip" data-content="${message(code:'org.label')}" data-position="top right" data-variation="tiny">
                                                    <i class="${Icon.ORG} la-list-icon"></i>
                                                </span>

                                                <div class="content la-space-right">
                                                    <div class="header">
                                                        ${link.functionType?.getI10n('value')}
                                                    </div>
                                                    <g:link controller="organisation" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                                                </div>

                                                <div class="content">
                                                    <g:if test="${editable}">
                                                        <g:set var="oid" value="${link.class.name}:${link.id}" />
                                                        <g:if test="${personInstance.roleLinks?.size() > 1}">
                                                            <div class="ui buttons">
                                                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: [link.functionType?.getI10n('value')])}"
                                                                        data-confirm-term-how="delete"
                                                                        controller="person" action="deletePersonRole" id="${personInstance.id}"  params="[oid: oid]"
                                                                        role="button"
                                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </g:link>
                                                            </div>
                                                        </g:if>
                                                        <g:else>
                                                            <div class="ui buttons">
                                                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                        controller="person"
                                                                        action="delete"
                                                                        id="${personInstance?.id}"
                                                                        params="[previousReferer: request.getHeader('referer')]"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.org.PrsLinksAndContact.function", args:[link?.functionType?.getI10n('value'), personInstance.toString()])}"
                                                                        data-confirm-term-how="delete"
                                                                        role="button"
                                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </g:link>
                                                            </div>
                                                        </g:else>
                                                    </g:if>
                                                </div>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>

                                <g:if test="${editable}">
                                    <a href="#prFunctionModal" data-ui="modal" class="${Btn.SIMPLE}">${message('code':'default.button.add.label')}</a>
                                </g:if>
                            </dd>
                        </dl>

                        <dl><dt><g:message code="person.positions.label" /></dt>
                            <dd>
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${personInstance.roleLinks}" var="link">
                                        <g:if test="${link.positionType}">
                                            <div class="ui item address-details">
                                                <span class="la-popup-tooltip" data-content="${message(code:'org.label')}" data-position="top right" data-variation="tiny">
                                                    <i class="${Icon.ORG} la-list-icon"></i>
                                                </span>

                                                <div class="content la-space-right">
                                                    <div class="header">
                                                        ${link.positionType?.getI10n('value')}
                                                    </div>
                                                    <g:link controller="organisation" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                                                </div>

                                                <div class="content">
                                                    <g:if test="${editable}">
                                                        <g:set var="oid" value="${link.class.name}:${link.id}" />
                                                        <g:if test="${personInstance.roleLinks?.size() > 1}">
                                                            <div class="ui buttons">
                                                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                        data-confirm-tokenMsg="${message(code: 'confirm.dialog.delete.function', args: [link.positionType?.getI10n('value')])}"
                                                                        data-confirm-term-how="unlink"
                                                                        controller="person" action="deletePersonRole" id="${personInstance.id}"  params="[oid: oid]"
                                                                        role="button"
                                                                        aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                                    <i class="${Icon.CMD.UNLINK}"></i>
                                                                </g:link>
                                                            </div>
                                                        </g:if>
                                                        <g:else>
                                                            <div class="ui buttons">
                                                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                        controller="person"
                                                                        action="delete"
                                                                        id="${personInstance?.id}"
                                                                        params="[previousReferer: request.getHeader('referer')]"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.org.PrsLinksAndContact.function", args:[link.positionType?.getI10n('value'), personInstance.toString()])}"
                                                                        data-confirm-term-how="delete"
                                                                        role="button"
                                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </g:link>
                                                            </div>
                                                        </g:else>
                                                    </g:if>
                                                </div>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>

                                <g:if test="${editable}">
                                    <a href="#prPositionModal" data-ui="modal" class="${Btn.SIMPLE}">${message('code':'default.button.add.label')}</a>
                                </g:if>
                            </dd>
                        </dl>

                        <dl><dt><g:message code="person.responsibilites.label" /></dt>
                            <dd>
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${personInstance.roleLinks}" var="link">
                                        <g:if test="${link.responsibilityType}">
                                            <div class="ui item address-details">

                                                <g:if test="${link.pkg}">
                                                    <span class="la-popup-tooltip" data-content="${message(code:'package.label')}" data-position="top right" data-variation="tiny">
                                                        <i class="${Icon.ORG} la-list-icon"></i>
                                                    </span>
                                                </g:if>
                                                <g:if test="${link.sub}">
                                                    <span class="la-popup-tooltip" data-content="${message(code:'default.subscription.label')}" data-position="top right" data-variation="tiny">
                                                        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                                    </span>
                                                </g:if>
                                                <g:if test="${link.lic}">
                                                    <span class="la-popup-tooltip" data-content="${message(code:'license.label')}" data-position="top right" data-variation="tiny">
                                                        <i class="${Icon.LICENSE} la-list-icon"></i>
                                                    </span>
                                                </g:if>
                                                <g:if test="${link.tipp}">
                                                    <span class="la-popup-tooltip" data-content="${message(code:'title.label')}" data-position="top right" data-variation="tiny">
                                                        <i class="${Icon.TIPP} la-list-icon"></i>
                                                    </span>
                                                </g:if>

                                                <div class="content">
                                                    <div class="header">
                                                        ${link.responsibilityType?.getI10n('value')}
                                                    </div>
                                                    <g:link controller="organisation" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                                                    <br />

                                                    <g:if test="${link.pkg}">
                                                        <g:link controller="package" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>
                                                    </g:if>
                                                    <g:if test="${link.sub}">
                                                        <g:link controller="subscription" action="show" id="${link.sub.id}">${link.sub.name}</g:link>
                                                    </g:if>
                                                    <g:if test="${link.lic}">
                                                        <g:link controller="license" action="show" id="${link.lic.id}">${link.lic}</g:link>
                                                    </g:if>
                                                    <g:if test="${link.tipp}">
                                                        <g:link controller="tipp" action="show" id="${link.tipp.id}">${link.tipp.name}</g:link>
                                                    </g:if>
                                                </div>

                                                <div class="content">
                                                    <g:if test="${editable}">
                                                        <div class="ui buttons">
                                                            <g:set var="oid" value="${link.class.name}:${link.id}" />
                                                            <g:link class="${Btn.MODERN.NEGATIVE}" controller="person" action="deletePersonRole" id="${personInstance.id}" params="[oid: oid]"
                                                                    role="button"
                                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                <i class="${Icon.CMD.DELETE}"></i>
                                                            </g:link>
                                                        </div>
                                                    </g:if>

                                                </div>

                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>

                            </dd></dl>

                    </div>
                </div><!-- .card -->
            </g:if>

            <div class="ui grid">
                <div class="sixteen wide column">
                    <div class="la-inline-lists">
                        <div class="ui card">
                            <div class="content">
                                <h2 class="ui header">${message(code:'org.properties.private')} ${institution.name}</h2>
                                <g:set var="propertyWrapper" value="private-property-wrapper-${institution.id}" />
                                <div id="${propertyWrapper}">
                                    <laser:render template="/templates/properties/private" model="${[
                                            prop_desc: PropertyDefinition.PRS_PROP,
                                            ownobj: personInstance,
                                            propertyWrapper: "${propertyWrapper}",
                                            tenant: institution]}"/>
                                    <laser:script file="${this.getGroovyPageFileName()}">
                                        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${institution.id});
                                    </laser:script>
                                </div>
                            </div>
                        </div><!-- .card -->
                    </div>
                </div>
            </div>

            <g:if test="${personInstance?.tenant && !myPublicContact}">
                <div class="ui card">
                    <div class="content">
                        <dl><dt><g:message code="person.tenant.label" /></dt>
                            <dd>

                                <g:if test="${editable /* && personInstance?.tenant?.id == contextService.getOrg().id */ && personInstance?.isPublic}">
                                    <ui:xEditableRefData owner="${personInstance}" field="tenant"
                                                            dataController="person" dataAction="getPossibleTenantsAsJson" />
                                </g:if>
                                <g:else>
                                    <g:link controller="organisation" action="show"
                                            id="${personInstance.tenant?.id}">${personInstance.tenant}</g:link>
                                </g:else>

                                <g:if test="${! personInstance.isPublic}">
                                    <span class="la-popup-tooltip" data-content="${message(code:'address.private')}" data-position="top right">
                                        <i class="${Icon.ACP_PRIVATE}"></i>
                                    </span>
                                    *&nbsp;${message(code: 'default.can.not.be.changed')}
                                </g:if>
                                <g:else>
                                    <span class="la-popup-tooltip" data-content="${message(code:'address.public')}" data-position="top right">
                                        <i class="${Icon.ACP_PUBLIC}"></i>
                                    </span>
                                </g:else>

                            </dd></dl>
                    </div>
                </div><!-- .card -->
            </g:if>
            <g:if test="${editable && personInstance?.tenant?.id == contextService.getOrg().id}">
                <div class="ui card">
                    <div class="content">
                            <g:link controller="person"
                                    action="delete"
                                    id="${personInstance?.id}"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contact", args: [personInstance])}"
                                    data-confirm-term-how="delete"
                                    class="${Btn.NEGATIVE_CONFIRM}"
                                    params="[previousReferer: request.getHeader('referer')]">
                                ${message(code: 'default.delete.label', args: ["${message(code: 'person')}"])}
                            </g:link>
                    </div>
                </div>
            </g:if>

        </div>

    </div><!-- .twelve -->

    <aside class="four wide column">
    </aside><!-- .four -->

</div><!-- .grid -->

<laser:render template="prsRoleModal" model="[
        tmplId: 'prFunctionModal',
        tmplRoleType: 'Funktion',
        roleType: PersonRole.TYPE_FUNCTION,
        roleTypeValues: PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION),
        message:'person.function_new.label',
        presetOrgId: presetOrg?.id]" />

<laser:render template="prsRoleModal" model="[
        tmplId: 'prPositionModal',
        tmplRoleType: 'Position',
        roleType: PersonRole.TYPE_POSITION,
        roleTypeValues: PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION),
        message:'person.position_new.label',
        presetOrgId: presetOrg?.id]" />

<laser:script file="${this.getGroovyPageFileName()}">
        %{--function addresscreate_org(orgId, typeId, redirect, modalId, hideType) {--}%
            %{--var url = '<g:createLink controller="ajaxHtml" action="createAddress"/>'+'?orgId='+orgId+'&typeId='+typeId+'&redirect='+redirect+'&modalId='+modalId+'&hideType='+hideType;--}%
            %{--private_address_modal(url);--}%
        %{--}--}%

    JSPC.app.addresscreate_prs = function (prsId, typeId, redirect, modalId, hideType) {
        var url = '<g:createLink controller="ajaxHtml" action="createAddress"/>'?prsId=' + prsId + '&typeId=' + typeId + '&redirect=' + redirect + '&modalId=' + modalId + '&hideType=' + hideType;
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }
</laser:script>

<laser:htmlEnd />