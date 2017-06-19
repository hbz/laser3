<g:if test="${contact}">
	<div class="contact-details">
		<g:link controller="contact" action="show" id="${contact?.id}">
			${contact?.type?.encodeAsHTML()}
			:
			${contact?.contentType?.encodeAsHTML()}
			- 
			${contact?.content?.encodeAsHTML()}
		</g:link>
	</div>			
</g:if>