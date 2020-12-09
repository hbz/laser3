<g:if test="${person}">
    <div class="ui divided middle aligned selection list la-flex-list">

        <div class="ui item person-details">
            <h5 class="ui header">

                    ${person.title}
                    ${person.first_name}
                    ${person.middle_name}
                    ${person.last_name}

            </h5>

            <g:if test="${overwriteEditable}">
                <div class="content la-space-right">
                    <button class="ui mini icon button" type="button" onclick="JSPC.app.editPerson(${person.id})" >
                        <i class="write icon"></i>
                    </button>
                </div>
            </g:if>

        </div><!-- .person-details -->

        <g:if test="${person.contacts}">

            <g:each in="${person.contacts.toSorted()}" var="contact">
                <g:render template="/templates/cpa/contact"
                          model="${[contact: contact, tmplShowDeleteButton: tmplShowDeleteButton, overwriteEditable: overwriteEditable]}"/>
            </g:each>

        </g:if>
        <g:if test="${person.addresses}">

            <g:each in="${person.addresses.sort { it?.type.each {it?.getI10n('value')} }}" var="address">
                <g:render template="/templates/cpa/address"
                          model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton, editable: editable]}"/>
            </g:each>

        </g:if>

        <g:if test="${!personRole && !tmplHideLinkToAddressbook}">

            <g:each in="${person.roleLinks}" var="role">
                <div class="item">
                    <g:link controller="organisation" action="addressbook" id="${role.org.id}">${role.org}</g:link>
                </div>
            </g:each>

        </g:if>

    </div><!-- .la-flex-list -->
</g:if>

<g:if test="${personRole}">
    <div class="ui divided middle aligned selection list la-flex-list la-list-border-around">

        <div class="ui item person-details">
            <div class="content la-space-right">
                <h5 class="ui header">

                        ${personRole.prs.title}
                        ${personRole.prs.first_name}
                        ${personRole.prs.middle_name}
                        ${personRole.prs.last_name}

                </h5>
                <g:if test="${personRole.functionType}">
                    (${personRole.functionType.getI10n('value')})
                </g:if>
                <g:if test="${personRole.positionType}">
                    (${personRole.positionType.getI10n('value')})
                </g:if>
                <g:if test="${personRole.responsibilityType}">
                    (${personRole.responsibilityType.getI10n('value')})
                </g:if>
            </div>

            <div class="content">
                <g:if test="${editable && tmplShowDeleteButton}">

                    <g:if test="${showAddContacts}">
                        <input class="ui mini icon button" type="button" data-semui="modal"
                               data-href="#contactFormModal${personRole.prs.id}"
                               value="${message(code: 'default.add.label', args: [message(code: 'person.contacts.label')])}">
                        <g:render template="/contact/formModal" model="['prsId': personRole.prs.id, modalId: 'contactFormModal'+personRole.prs.id]"/>
                    </g:if>


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
    <g:if test="${personRole.prs.contacts}">
        <g:each in="${personRole.prs.contacts.toSorted()}" var="contact">
            <g:if test="${tmplConfigShow.contains(contact.contentType?.value)}">
                <g:render template="/templates/cpa/contact" model="${[
                        contact             : contact,
                        tmplShowDeleteButton: true
                ]}"/>
            </g:if>
        </g:each>

    </g:if>
    <g:if test="${tmplConfigShow?.contains('address') && personRole.prs.addresses}">

        <g:each in="${personRole.prs.addresses.sort { it.type.each {it?.getI10n('value')} }}" var="address">
            <g:render template="/templates/cpa/address"
                      model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton, editable: editable]}"/>
        </g:each>

    </g:if>
    </div><!-- .la-flex-list -->
</g:if>
<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editPerson = function (id) {
            var url = '<g:createLink controller="ajaxHtml" action="editPerson" params="[showAddresses: showAddresses?:false, showContacts: showContacts?:false, org: (restrictToOrg ? restrictToOrg?.id : '')]"/>&id='+id;
            JSPC.app.person_editModal(url)
        }
    JSPC.app.person_editModal = function (url) {
            $.ajax({
                url: url,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#personModal").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal({
                        onVisible: function () {
                            r2d2.initDynamicSemuiStuff('#personModal');
                            r2d2.initDynamicXEditableStuff('#personModal');
                        }
                    }).modal('show');
                }
            });
        }
</laser:script>
							
