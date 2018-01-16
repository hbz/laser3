<g:if test="${contact}">
	<div class="ui item contact-details">
		<g:link controller="contact" action="show" id="${contact?.id}">
			${contact.type?.getI10n('value')}:
            (${contact.contentType?.getI10n('value')})
			${contact?.content?.encodeAsHTML()}
		</g:link>
	</div>			
</g:if>