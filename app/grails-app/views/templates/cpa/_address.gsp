<g:if test="${address}">
	<div class="ui item address-details">
		<g:link controller="address" action="show" id="${address?.id}">
            ${address.type?.getI10n('value')}:

			<br />
			${address?.street_1.encodeAsHTML()}
			${address?.street_2.encodeAsHTML()}

			<g:if test="${address?.pob}">
                <br />
                ${address?.pob.encodeAsHTML()}
            </g:if>

            <br />
            ${address?.zipcode.encodeAsHTML()}
            ${address?.city.encodeAsHTML()}

			<g:if test="${address?.state || address?.country}">
                <br />
                ${address?.state?.getI10n('value')}
                <g:if test="${address?.state && address?.country}">, </g:if>
                ${address?.country.getI10n('value')}
            </g:if>
		</g:link>
	</div>
</g:if>