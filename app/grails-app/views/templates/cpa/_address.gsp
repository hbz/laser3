<g:if test="${address}">
	<div class="address-details">
		<g:link controller="address" action="show" id="${address?.id}">
			${address?.type?.encodeAsHTML()} :
			:
			${address?.street_1?.encodeAsHTML()}
			${address?.street_2?.encodeAsHTML()}
			,
			${address?.pob?.encodeAsHTML()}
			,
			${address?.zipcode?.encodeAsHTML()}
			${address?.city?.encodeAsHTML()} 
			,
			${address?.state?.encodeAsHTML()}
			,
			${address?.country?.encodeAsHTML()}
		</g:link>
	</div>
</g:if>