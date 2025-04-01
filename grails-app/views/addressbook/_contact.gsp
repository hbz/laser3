<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<g:if test="${contact}">
    <div class="ui item contact-details js-copyTriggerParent" style="display: flex; flex-flow: row wrap;">
        <div  style="display: flex; flex-flow: row wrap;">

            <div class="content la-space-right" >
                <ui:contactIcon type="${contact.contentType?.value}" />
                <span class="js-copyTrigger js-copyTopic la-popup-tooltip" data-content="${message(code: 'tooltip.clickToCopySimple')}">
%{--                    <ui:xEditable owner="${contact}" field="content" overwriteEditable="${overwriteEditable}" />--}%
                    ${contact.content}
                </span>
                <g:if test="${contact.language}">
                    <g:if test="${contact.language.value == 'ger'}">
                        <i class="flag de"></i>
                    </g:if>
                    <g:elseif test="${contact.language.value == 'eng'}">
                        <i class="flag gb"></i>
                    </g:elseif>
                    <g:else>
                        (${contact.language.getI10n("value")})
                    </g:else>
                </g:if>

                <g:if test="${contact.contentType == RDStore.CCT_EMAIL}">
                    <span class="la-popup-tooltip" data-position="top right" data-content="${message(code: 'tooltip.sendMail')}">
                        <a href="mailto:${contact.content}" class="${Btn.MODERN.SIMPLE} tiny">
                            <i class="${Icon.LNK.MAIL_TO}"></i>
                        </a>
                    </span>
                </g:if>

                <g:if test="${contact.contentType == RDStore.CCT_URL}">
                    <span class="la-popup-tooltip" data-position="top right"  data-content="${message(code: 'tooltip.callUrl')}">
                        <a href="${contact.content}" target="_blank" class="${Btn.MODERN.SIMPLE} tiny">
                            <i class="${Icon.LNK.EXTERNAL}"></i>
                        </a>
                    </span>
                </g:if>

                <g:if test="${editable && tmplShowDeleteButton}">
                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} tiny"
                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contactItems.addressbook")}"
                            data-confirm-term-how="delete"
                            controller="addressbook" action="deleteContact" params="[id: contact.id]"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                        <i class="${Icon.CMD.DELETE}"></i>
                    </g:link>
                </g:if>
            </div>
        </div>
    </div>
</g:if>
