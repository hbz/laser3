<%@ page import="com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>

<g:if test="${person && personContext}">
    <div class="ui divided middle aligned selection list la-flex-list la-list-border-around">

        <div class="ui item person-details">
            <span></span>

            <div class="content la-space-right">
                <h5 class="ui header">
                    <g:link controller="person" action="show" id="${person.id}">
                        ${person.title}
                        ${person.first_name}
                        ${person.middle_name}
                        ${person.last_name}
                    </g:link>
                </h5>

                <script>
                    $('.person-details').mouseenter(function () {
                        $(this).parent().addClass('la-border-selected');
                    })
                    $('.person-details').mouseleave(function () {
                        $(this).parent().removeClass('la-border-selected');
                    })
                </script>
            </div>

            <div class="content">
                <g:if test="${editable}">

                    <g:if test="${true || showAddPersonRole}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#prsRoleFormModal${person.id}_F"
                               value="${message(code: 'default.add.label', args: ['Funktion'])}">
                        <g:render template="/person/prsRoleModal" model="[personInstance: person,
                                                                          tmplId: 'prsRoleFormModal' + person.id + '_F',
                                                                          tmplRoleType: 'Funktion',
                                                                          roleType: PersonRole.TYPE_FUNCTION,
                                                                          roleTypeValues: PersonRole.getAllRefdataValues('Person Function'),
                                                                          message:'person.function_new.label',
                                                                          presetOrgId: personContext.id ]"/>

                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#prsRoleFormModal${person.id}_P"
                               value="${message(code: 'default.add.label', args: ['Position'])}">
                        <g:render template="/person/prsRoleModal" model="[personInstance: person,
                                                                          tmplId: 'prsRoleFormModal' + person.id + '_P',
                                                                          tmplRoleType: 'Funktion',
                                                                          roleType: PersonRole.TYPE_POSITION,
                                                                          roleTypeValues: PersonRole.getAllRefdataValues('Person Position'),
                                                                          message:'person.position_new.label',
                                                                          presetOrgId: personContext.id ]"/>
                    </g:if>

                    <g:if test="${showAddContacts}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#contactFormModal${person.id}"
                               value="${message(code: 'default.add.label', args: [message(code: 'person.contacts.label', default: 'Contacts')])}">
                        <g:render template="/contact/formModal" model="['prsId': person.id, modalId: 'contactFormModal' + person.id]"/>
                    </g:if>

                </g:if>
            </div>
        </div><!-- .person-details -->

        <g:if test="${person.contacts}">
            <g:each in="${person.contacts?.toSorted()}" var="contact">
                <g:if test="${tmplConfigShow.contains(contact?.contentType?.value)}">
                    <g:render template="/templates/cpa/contact" model="${[
                            contact             : contact,
                            tmplShowDeleteButton: true
                    ]}"/>
                </g:if>
            </g:each>

        </g:if>
        <g:if test="${tmplConfigShow?.contains('address') && person.addresses}">

            <g:each in="${person.addresses?.sort { it.type?.getI10n('value') }}" var="address">
                <g:render template="/templates/cpa/address"
                          model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton]}"/>
            </g:each>

        </g:if>

        <g:each in="${person.roleLinks.toSorted()}" var="personRole">
            <g:if test="${personRole.org.id == personContext.id}">

                <div class="ui item person-details">
                    <span></span>

                    <div class="content la-space-right">
                        <g:if test="${personRole?.functionType}">
                            ${personRole?.functionType?.getI10n('value')}
                        </g:if>
                        <g:if test="${personRole?.positionType}">
                            ${personRole?.positionType?.getI10n('value')}
                        </g:if>
                        <g:if test="${personRole?.responsibilityType}">
                            ${personRole?.responsibilityType?.getI10n('value')}
                        </g:if>
                    </div>

                    <div class="content">
                        <g:if test="${editable && tmplShowDeleteButton}">

                            <g:set var="oid" value="${personRole?.class.name}:${personRole?.id}"/>

                            <g:link class="ui mini icon negative button js-open-confirm-modal"
                                    data-confirm-term-what="contact"
                                    data-confirm-term-where="organisation"
                                    data-confirm-term-how="unlink"
                                    controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
                                <i class="unlink icon"></i>
                            </g:link>
                        </g:if>
                    </div>

                </div><!-- .person-details -->

            </g:if>
        </g:each>

    </div><!-- .la-flex-list -->
</g:if>

							