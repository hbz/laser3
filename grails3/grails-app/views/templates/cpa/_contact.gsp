<%@ page import="de.laser.helper.RDStore" %>
<laser:serviceInjection/>
<g:if test="${contact}">
	<div class="ui js-copyTriggerParent item contact-details" style="display: flex;">
        <div style="display: flex">
            <semui:contactIcon type="${contact.contentType?.value}" />
            <div class="content la-space-right">
                <semui:xEditable class="js-copyTopic" owner="${contact}" field="content" overwriteEditable="${overwriteEditable}" />
                <g:if test="${contact.language}">(${contact.language.getI10n("value")})</g:if>
            </div>
        </div>


        <div class="content">
            <g:if test="${contact.contentType in [RDStore.CCT_EMAIL]}">
                <span class="la-popup-tooltip la-delay" data-position="top right" data-content="Mail senden an ..">
                    <a href="mailto:${contact.content}" class="ui  icon blue button la-modern-button">
                        <i class="share square icon"></i>
                    </a>
                </span>
            </g:if>

            <g:if test="${contact.contentType in [RDStore.CCT_URL]}">
                <span class="la-popup-tooltip la-delay" data-position="top right" data-content="Diese URL aufrufen ..">
                    <a href="${contact.content}" target="_blank" class="ui icon blue button la-modern-button">
                        <i class="share square icon"></i>
                    </a>
                </span>
            </g:if>

            <g:if test="${editable && tmplShowDeleteButton}">
				<g:link class="ui icon negative button js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contactItems.addressbook")}"
                        data-confirm-term-how="delete"
                        controller="ajax" action="delete" params="[cmd: 'deleteContact', oid: genericOIDService.getOID(contact)]"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
					<i class="trash alternate outline icon"></i>
				</g:link>
            </g:if>
        </div><!-- .content -->

	</div>			
</g:if>