<g:if test="${contact}">
	<div class="ui item contact-details">
		<semui:contactIcon type="${contact?.contentType.('value')}" />

        <div class="content la-space-right">
            <semui:xEditable owner="${contact}" field="content" />
        </div>

		<g:if test="${editable && tmplShowDeleteButton}">
			<div class="content">
                <g:if test="${['Mail', 'E-Mail'].contains(contact?.contentType?.value)}">
                    <span data-position="top right" data-tooltip="Mail senden an ..">
                        <a href="mailto:${contact?.content}" class="ui mini icon blue button">
                            <i class="share square icon"></i>
                        </a>
                    </span>
                </g:if>
                <g:if test="${contact.contentType?.getI10n('value') == 'Url'}">
                    <span data-position="top right" data-tooltip="Diese URL aufrufen ..">
                        <a href="${contact?.content}" target="_blank" class="ui mini icon blue button">
                            <i class="share square icon"></i>
                        </a>
                    </span>
                </g:if>

                <g:set var="oid" value="${contact.class.name}:${contact.id}" />

				<g:link class="ui mini icon negative button" controller="ajax" action="delete" params="[cmd: 'deleteContact', oid: oid]">
					<i class="trash alternate icon"></i>
				</g:link>
			</div>
		</g:if>
	</div>			
</g:if>
${contactInstance?.contentType}