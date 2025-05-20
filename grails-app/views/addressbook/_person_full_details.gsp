<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.addressbook.PersonRole; de.laser.storage.RDStore; de.laser.addressbook.Person; de.laser.storage.RDConstants" %>

<g:if test="${person && personContext}">
    <div class="ui divided middle aligned ${noSelection ? '' : 'selection'} list la-flex-list la-list-border-around">

        <div class="ui item person-details">
            <g:if test="${personRole?.functionType in [RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_METADATA] && !(person.last_name in
                    [RDStore.PRS_FUNC_SERVICE_SUPPORT.value, RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n("value"), RDStore.PRS_FUNC_TECHNICAL_SUPPORT.value, RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n("value"), RDStore.PRS_FUNC_METADATA.value, RDStore.PRS_FUNC_METADATA.getI10n("value")])}">
                <div class="content la-space-right">
                    <div class="header">
                        ${person.title}
                        ${person.first_name}
                        ${person.middle_name}
                        ${person.last_name}
                    </div>
                </div>
            </g:if>
            <g:elseif test="${!(personRole?.functionType in [RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_METADATA])}">
                <div class="content la-space-right">
                    <div class="header">
                        ${person.title}
                        ${person.first_name}
                        ${person.middle_name}
                        ${person.last_name}
                    </div>
                </div>
            </g:elseif>

            <g:if test="${overwriteEditable}">
                        <div class="content la-space-right">
                        <button class="${Btn.MODERN.SIMPLE}" type="button" onclick="JSPC.app.editPerson(${person.id})"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                        </button>
                        </div>
            </g:if>

            <div class="content">
            </div>
        </div><!-- .person-details -->

        <g:if test="${person.contacts}">
            <g:each in="${person.contacts.toSorted()}" var="contact">
                <%-- contentType should be made not nullable ... subject of kanban! --%>
                <g:if test="${contact.contentType && tmplConfigShow.contains(contact.contentType.value)}">
                    <laser:render template="/addressbook/contact" model="${[
                            overwriteEditable   : editable,
                            contact             : contact,
                            tmplShowDeleteButton: tmplShowDeleteButton
                    ]}"/>
                </g:if>
            </g:each>

        </g:if>

        <g:if test="${tmplShowFunctions}">
            <g:each in="${person.roleLinks.toSorted()}" var="personRoleLink">
                <g:if test="${personRoleLink.org.id == personContext.id && personRoleLink.functionType}">

                    <div class="ui item person-details">
                        <div class="content la-space-right">
                            ${personRoleLink.functionType.getI10n('value')}
                        </div>

                        <div class="content">

                            <g:if test="${editable && tmplShowDeleteButton}">
                                <g:if test="${person.roleLinks.size() > 1}">
                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.function.contact", args: [personRoleLink.functionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="unlink"
                                            controller="addressbook" action="deletePersonRole" params="[id: personRoleLink.id]"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                        <i class="${Icon.CMD.UNLINK}"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            controller="addressbook"
                                            action="deletePerson"
                                            id="${person.id}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.org.PrsLinksAndContact.function", args: [personRoleLink.functionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="delete"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </g:link>
                                </g:else>
                            </g:if>
                        </div>
                    </div><!-- .person-details -->

                </g:if>
            </g:each>
        </g:if>
        <g:if test="${tmplShowPositions}">
            <g:each in="${person.roleLinks.toSorted()}" var="personRole">
                <g:if test="${personRole.org.id == personContext.id && personRole.positionType}">

                    <div class="ui item person-details">
                        <div class="content la-space-right">
                            ${personRole.positionType.getI10n('value')} (Position)
                        </div>

                        <div class="content">
                            <g:if test="${editable && tmplShowDeleteButton}">
                                <g:if test="${person.roleLinks.size() > 1}">
                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.position.contact", args: [personRole.positionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="unlink"
                                            controller="addressbook" action="deletePersonRole" params="[id: personRole.id]"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                        <i class="${Icon.CMD.UNLINK}"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            controller="addressbook"
                                            action="deletePerson"
                                            id="${person.id}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.org.PrsLinksAndContact.position", args: [personRole.positionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="delete"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </g:link>
                                </g:else>
                            </g:if>
                        </div>
                    </div><!-- .person-details -->

                </g:if>
            </g:each>
        </g:if>

        <g:if test="${tmplShowResponsiblities}">
            <g:each in="${person.roleLinks.toSorted()}" var="personRole">
                <g:if test="${personRole.org.id == personContext.id && personRole.responsibilityType}">

                    <div class="ui item person-details">
                        <div class="content la-space-right">
                            ${personRole.responsibilityType.getI10n('value')} (Verantwortlichkeit)
                        </div>

                        <div class="content">
                            <g:if test="${editable && tmplShowDeleteButton}">
                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.contact.organisation")}"
                                        data-confirm-term-how="unlink"
                                        controller="addressbook" action="deletePersonRole" params="[id: personRole.id]"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                    <i class="${Icon.CMD.UNLINK}"></i>
                                </g:link>
                            </g:if>
                        </div>
                    </div><!-- .person-details -->

                </g:if>
            </g:each>
        </g:if>

    </div><!-- .la-flex-list -->
    <g:if test="${editable && tmplUnlinkedObj}">
        <td class="right aligned">
            <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.contact")}"
                    data-confirm-term-how="unlink"
                    controller="addressbook" action="deletePersonRole" params="[id: tmplUnlinkedObj.id]"
                    role="button"
                    aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                <i class="${Icon.CMD.UNLINK}"></i>
            </g:link>
        </td>
    </g:if>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.editPerson = function (id) {
            var url = '<g:createLink controller="ajaxHtml" action="editPerson" params="[showContacts: showContacts ?: false, org: (restrictToOrg ? restrictToOrg?.id : '')]"/>&id=' + id;
            var func = bb8.ajax4SimpleModalFunction("#personModal", url);
            func();
        }
</laser:script>

							
