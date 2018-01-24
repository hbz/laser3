<div class="ui item person-details">
	<g:if test="${person}">

		<h5 class="ui header">
			<g:link controller="person" action="show" id="${person?.id}">
				${person?.first_name?.encodeAsHTML()}
				${person?.middle_name?.encodeAsHTML()}
				${person?.last_name?.encodeAsHTML()}
			</g:link>
		</h5>

		<div class="ui list">
			<g:each in="${person?.contacts}" var="contact">
				<div class="item">
					<g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
				</div>
			</g:each>
		</div>
		<div class="ui list">
			<g:each in="${person?.addresses}" var="address">
				<div class="item">
					<g:render template="/templates/cpa/address" model="${[address: address]}"></g:render>
				</div>
			</g:each>
		</div>

		<g:if test="${!personRole}">
			<div class="ui list">
				<g:each in="${person?.roleLinks}" var="role">
					<div class="item">
						<g:link controller="organisations" action="addressbook" id="${role.org?.id}">${role.org}</g:link>
					</div>
				</g:each>
			</div>
		</g:if>
	</g:if>
	<g:if test="${personRole}">

        <h5 class="ui header">
            <g:link controller="person" action="show" id="${personRole?.prsId}">
                ${personRole?.prs?.first_name?.encodeAsHTML()}
                ${personRole?.prs?.middle_name?.encodeAsHTML()}
                ${personRole?.prs?.last_name?.encodeAsHTML()}
            </g:link>
            <g:if test="${personRole?.functionType}">
                <small> - ${personRole?.functionType?.getI10n('value')}</small>
            </g:if>
        </h5>

        <div class="ui list">
            <g:each in="${personRole?.prs?.contacts}" var="contact">
                <div class="item">
                    <g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
                </div>
            </g:each>
        </div>
        <div class="ui list">
            <g:each in="${personRole?.prs?.addresses}" var="address">
                <div class="item">
                    <g:render template="/templates/cpa/address" model="${[address: address]}"></g:render>
                </div>
            </g:each>
			</div>

	</g:if>
</div>
							