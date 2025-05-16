<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${person}">
    <div class="ui divided middle aligned selection list la-flex-list">

        <div class="ui item person-details">
            <div class="header">
                    ${person.title}
                    ${person.first_name}
                    ${person.middle_name}
                    ${person.last_name}
            </div>
        <g:if test="${showFunction && person.roleLinks}">
            <div class="content la-space-right">
                (${person.roleLinks.findAll {it.functionType != null}.collect {it.functionType?.getI10n('value')}.join(', ')})
            </div>
        </g:if>

            <g:if test="${overwriteEditable}">
                <div class="content la-space-right">
                    <button class="${Btn.MODERN.SIMPLE}" type="button" onclick="JSPC.app.editPerson(${person.id})"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                        <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                    </button>
                </div>
            </g:if>
        </div><!-- .person-details -->

        <g:if test="${person.contacts}">
            <g:each in="${person.contacts.toSorted()}" var="contact">
                <laser:render template="/addressbook/contact"
                          model="${[contact: contact, tmplShowDeleteButton: tmplShowDeleteButton, overwriteEditable: overwriteEditable]}"/>
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
                <div class="header">
                        ${personRole.prs.title}
                        ${personRole.prs.first_name}
                        ${personRole.prs.middle_name}
                        ${personRole.prs.last_name}
                </div>
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
    <g:if test="${personRole.prs.contacts}">
        <g:each in="${personRole.prs.contacts.toSorted()}" var="contact">
            <g:if test="${tmplConfigShow.contains(contact.contentType?.value)}">
                <laser:render template="/addressbook/contact" model="${[
                        contact             : contact,
                        tmplShowDeleteButton: true
                ]}"/>
            </g:if>
        </g:each>

    </g:if>
    <g:if test="${tmplConfigShow?.contains('address') && personRole.prs.addresses}">

        <g:each in="${personRole.prs.addresses.sort { it.type.each {it?.getI10n('value')} }}" var="address">
            <laser:render template="/addressbook/address"
                      model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton, editable: editable]}"/>
        </g:each>

    </g:if>
    </div><!-- .la-flex-list -->
</g:if>
<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editPerson = function (id) {
            var url = '<g:createLink controller="ajaxHtml" action="editPerson" params="[showContacts: showContacts?:false, org: (restrictToOrg ? restrictToOrg?.id : '')]"/>&id='+id;

            $.ajax({
                url: url,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#personModal").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal({

                        onVisible: function () {
                            r2d2.initDynamicUiStuff('#personModal');
                            r2d2.initDynamicXEditableStuff('#personModal');
                        }
                    }).modal('show');
                }
            });
        }
</laser:script>
							
