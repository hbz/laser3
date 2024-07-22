<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<g:if test="${contact}">
	<div class="ui js-copyTriggerParent item contact-details" style="display: flex; flex-flow: row wrap;">
        <div  style="display: flex; flex-flow: row wrap;" class="js-copyTrigger la-popup-tooltip la-delay"
    data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
            <ui:contactIcon type="${contact.contentType?.value}" />
            <div class="content la-space-right">
                <span class="js-copyTopic"><ui:xEditable owner="${contact}" field="content" overwriteEditable="${overwriteEditable}" /></span>
                <g:if test="${contact.language}">(${contact.language.getI10n("value")})</g:if>
            </div>
        </div>


        <div class="content">
            <g:if test="${contact.contentType == RDStore.CCT_EMAIL}">
                <span class="la-popup-tooltip" data-position="top right" data-content="${message(code: 'tooltip.sendMail')}">
                    <a href="mailto:${contact.content}" class="${Btn.MODERN.SIMPLE}">
                        <i class="${Icon.LNK.EXTERNAL}"></i>
                    </a>
                </span>
            </g:if>

            <g:if test="${contact.contentType == RDStore.CCT_URL}">
                <span class="la-popup-tooltip" data-position="top right"  data-content="${message(code: 'tooltip.callUrl')}">
                    <a href="${contact.content}" target="_blank" class="${Btn.MODERN.SIMPLE}">
                        <i class="${Icon.LNK.EXTERNAL}"></i>
                    </a>
                </span>
            </g:if>

            <g:if test="${editable && tmplShowDeleteButton}">
				<g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contactItems.addressbook")}"
                        data-confirm-term-how="delete"
                        controller="ajax" action="delete" params="[cmd: 'deleteContact', oid: genericOIDService.getOID(contact)]"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
					<i class="${Icon.CMD.DELETE}"></i>
				</g:link>
            </g:if>
        </div><!-- .content -->
	</div>			
</g:if>