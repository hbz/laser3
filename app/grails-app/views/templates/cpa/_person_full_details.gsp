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

                    <g:if test="${tmplShowAddPersonRoles}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#prsRoleFormModal${person.id}_F"
                               value="Funktionen">
                        <g:render template="/person/prsRoleModal" model="[personInstance: person,
                                                                          tmplId: 'prsRoleFormModal' + person.id + '_F',
                                                                          tmplRoleType: 'Funktion',
                                                                          roleType: PersonRole.TYPE_FUNCTION,
                                                                          roleTypeValues: PersonRole.getAllRefdataValues('Person Function'),
                                                                          message:'person.function_new.label',
                                                                          presetOrgId: personContext.id ]"/>

                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#prsRoleFormModal${person.id}_P"
                               value="Positionen">
                        <g:render template="/person/prsRoleModal" model="[personInstance: person,
                                                                          tmplId: 'prsRoleFormModal' + person.id + '_P',
                                                                          tmplRoleType: 'Funktion',
                                                                          roleType: PersonRole.TYPE_POSITION,
                                                                          roleTypeValues: PersonRole.getAllRefdataValues('Person Position'),
                                                                          message:'person.position_new.label',
                                                                          presetOrgId: personContext.id ]"/>
                    </g:if>

                    <g:if test="${tmplShowAddContacts}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#contactFormModal${person.id}"
                               value="${message(code: 'person.contacts.label')}">
                        <g:render template="/contact/formModal" model="['prsId': person.id, modalId: 'contactFormModal' + person.id]"/>
                    </g:if>

                    <g:if test="${tmplShowAddAddresses}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#addressFormModal${person.id}"
                               value="${message(code: 'person.addresses.label')}">
                        <g:render template="/address/formModal" model="['prsId': person.id, modalId: 'addressFormModal' + person.id]"/>
                    </g:if>

                </g:if>
            </div>
        </div><!-- .person-details -->

        <g:if test="${person.contacts}">
            <g:each in="${person.contacts?.toSorted()}" var="contact">
                <g:if test="${tmplConfigShow.contains(contact?.contentType?.value)}">
                    <g:render template="/templates/cpa/contact" model="${[
                            overwriteEditable   : editable,
                            contact             : contact,
                            tmplShowDeleteButton: tmplShowDeleteButton
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

        <g:if test="${tmplShowFunctions}">
            <g:each in="${person.roleLinks.toSorted()}" var="personRole">
                <g:if test="${personRole.org.id == personContext.id && personRole?.functionType}">

                    <div class="ui item person-details">
                        <span></span>
                        <div class="content la-space-right">
                            ${personRole?.functionType?.getI10n('value')}
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
        </g:if>

        <g:if test="${tmplShowPositions}">
            <g:each in="${person.roleLinks.toSorted()}" var="personRole">
                <g:if test="${personRole.org.id == personContext.id && personRole?.positionType}">

                    <div class="ui item person-details">
                        <span></span>
                        <div class="content la-space-right">
                            ${personRole?.positionType?.getI10n('value')} (Position)
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
        </g:if>

        <g:if test="${tmplShowResponsiblities}">
            <g:each in="${person.roleLinks.toSorted()}" var="personRole">
                <g:if test="${personRole.org.id == personContext.id && personRole?.responsibilityType}">

                    <div class="ui item person-details">
                        <span></span>
                        <div class="content la-space-right">
                            ${personRole?.responsibilityType?.getI10n('value')} (Verantwortlichkeit)
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
        </g:if>

    </div><!-- .la-flex-list -->

    <g:if test="${tmplUnlink}"><%-- TODO.: refactoring layout --%>

        <g:set var="oid" value="${tmplUnlink?.class.name}:${tmplUnlink?.id}"/>

        <g:link class="ui mini icon negative button js-open-confirm-modal"
                data-confirm-term-what="contact"
                data-confirm-term-what-detail="${tmplUnlink.prs}"
                data-confirm-term-where="subscription"
                data-confirm-term-how="unlink"
                controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
            <i class="unlink icon"></i>
        </g:link>

    </g:if>

</g:if>

							