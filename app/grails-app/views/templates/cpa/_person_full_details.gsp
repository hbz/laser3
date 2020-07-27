<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Person;com.k_int.kbplus.PersonRole;de.laser.helper.RDConstants" %>

<g:if test="${person && personContext}">
    <div class="ui divided middle aligned selection list la-flex-list la-list-border-around">

        <div class="ui item person-details">
            <div class="content la-space-right">
                <h5 class="ui header">
                    <g:link controller="person" action="show" id="${person.id}">
                        ${person.title}
                        ${person.first_name}
                        ${person.middle_name}
                        ${person.last_name}
                    </g:link>
                </h5>
            </div>

            <div class="content">
                <g:if test="${editable}">

                    <g:if test="${tmplShowAddPersonRoles}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#prsRoleFormModal${personRole.id}_F"
                               value="Funktionen">
                        <g:render template="/person/prsRoleModal" model="[personInstance: person,
                                                                          tmplId: 'prsRoleFormModal' + personRole.id + '_F',
                                                                          tmplRoleType: 'Funktion',
                                                                          roleType: PersonRole.TYPE_FUNCTION,
                                                                          roleTypeValues: PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION),
                                                                          message:'person.function_new.label',
                                                                          presetOrgId: personContext.id ]"/>

                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#prsRoleFormModal${personRole.id}_P"
                               value="Positionen">
                        <g:render template="/person/prsRoleModal" model="[personInstance: person,
                                                                          tmplId: 'prsRoleFormModal' + personRole.id + '_P',
                                                                          tmplRoleType: 'Funktion',
                                                                          roleType: PersonRole.TYPE_POSITION,
                                                                          roleTypeValues: PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION),
                                                                          message:'person.position_new.label',
                                                                          presetOrgId: personContext.id ]"/>
                    </g:if>

                    <g:if test="${tmplShowAddContacts}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#contactFormModal${personRole.id}"
                               value="${message(code: 'person.contacts.label')}">
                        <g:render template="/contact/formModal" model="['prsId': person.id, prId: personRole.id, modalId: 'contactFormModal' + personRole.id]"/>
                    </g:if>

                    <g:if test="${tmplShowAddAddresses}">
                        <% Map model = [:]
                        model.prsId = person?.id
                        model.typeId = RDStore.ADRESS_TYPE_LIBRARY
                        model.redirect = '.'
                        model.hideType = true%>
                        <input class="ui mini icon button" type="button"
                               value="${message(code: 'person.addresses.label')}"
                               onclick="addresscreate_prs('${model.prsId}', '${model.typeId}', '${model.redirect}', '${model.modalId}', '${model.hideType}');" >
                    </g:if>

                </g:if>
            </div>
        </div><!-- .person-details -->

        <g:if test="${person.contacts}">
            <g:each in="${person.contacts.toSorted()}" var="contact">
                <g:if test="${tmplConfigShow.contains(contact.contentType.value)}">
                    <g:render template="/templates/cpa/contact" model="${[
                            overwriteEditable   : editable,
                            contact             : contact,
                            tmplShowDeleteButton: tmplShowDeleteButton
                    ]}"/>
                </g:if>
            </g:each>

        </g:if>
        <g:if test="${tmplConfigShow.contains('address') && person.addresses}">

            <g:each in="${person.addresses.sort { it.type.getI10n('value') }}" var="address">
                <g:render template="/templates/cpa/address"
                          model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton]}"/>
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
                                <g:set var="oid" value="${personRoleLink.class.name}:${personRoleLink.id}"/>
                                <g:if test="${person.roleLinks.size() > 1}">
                                    <g:link class="ui mini icon negative button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.function.contact", args: [personRoleLink.functionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="unlink"
                                            controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
                                        <i class="unlink icon"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link class="ui mini icon negative button js-open-confirm-modal"
                                            controller="person"
                                            action="_delete"
                                            id="${person.id}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.org.PrsLinksAndContact.function", args: [personRoleLink.functionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="delete">
                                        <i class="trash alternate icon"></i>
                                    </g:link>
                                    %{--<g:form controller="person" action="_delete" data-confirm-id="${person.id.toString()+ '_form'}">--}%
                                        %{--<g:hiddenField name="id" value="${person.id}" />--}%
                                        %{--<div class="ui mini icon negative button js-open-confirm-modal"--}%
                                             %{--data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contact", args: [person.toString()])}"--}%
                                             %{--data-confirm-term-how="delete"--}%
                                             %{--data-confirm-id="${person.id}" >--}%
                                            %{--<i class="unlink icon"></i>--}%
                                        %{--</div>--}%
                                    %{--</g:form>--}%
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
                                <g:set var="oid" value="${personRole.class.name}:${personRole.id}"/>

                                <g:if test="${person.roleLinks.size() > 1}">
                                    <g:link class="ui mini icon negative button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.position.contact", args: [personRole.positionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="unlink"
                                            controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
                                        <i class="unlink icon"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link class="ui mini icon negative button js-open-confirm-modal"
                                            controller="person"
                                            action="_delete"
                                            id="${person.id}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.org.PrsLinksAndContact.position", args: [personRole.positionType.getI10n('value'), person.toString()])}"
                                            data-confirm-term-how="delete">
                                        <i class="trash alternate icon"></i>
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
                                <g:set var="oid" value="${personRole.class.name}:${personRole.id}"/>
                                <g:link class="ui mini icon negative button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.contact.organisation")}"
                                        data-confirm-term-how="unlink"
                                        controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
                                    <i class="unlink icon"></i>
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
            <g:set var="oid" value="${tmplUnlinkedObj.class.name}:${tmplUnlinkedObj.id}"/>
            <g:link class="ui icon negative button js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.contact")}"
                    data-confirm-term-how="unlink"
                    controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
                <i class="unlink icon"></i>
            </g:link>
        </td>
    </g:if>
</g:if>

							
