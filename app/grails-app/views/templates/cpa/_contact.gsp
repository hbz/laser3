<g:if test="${contact}">
	<div class="ui item contact-details">
        <div class="content">
            <g:link controller="contact" action="show" id="${contact?.id}">
                ${contact.type?.getI10n('value')}:
                (${contact.contentType?.getI10n('value')})
                ${contact?.content?.encodeAsHTML()}
            </g:link>
        </div>

		<g:if test="${editable && tmplShowDeleteButton}">
			<div class="ui mini icon buttons">
				<g:set var="oid" value="${contact.class.name}:${contact.id}" />
				<g:link class="ui negative button" controller="${controller}" action="${action}" id="${id}" params="[cmd: 'deleteContact', oid: oid]">
					<i class="trash alternate icon"></i>
				</g:link>
			</div>
		</g:if>
	</div>			
</g:if>