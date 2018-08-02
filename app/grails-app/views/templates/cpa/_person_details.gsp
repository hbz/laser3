<div class="ui item person-details">
	<g:if test="${person}">

		<h5 class="ui header">
			<g:link controller="person" action="show" id="${person?.id}">
				${person?.title?.encodeAsHTML()}
				${person?.first_name?.encodeAsHTML()}
				${person?.middle_name?.encodeAsHTML()}
				${person?.last_name?.encodeAsHTML()}
			</g:link>
		</h5>

		<g:if test="${person?.contacts}">
			<div class="ui relaxed divided  list">
				<g:each in="${person?.contacts.sort{it.content}}" var="contact">
					<div class="item">
						<g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
					</div>
				</g:each>
			</div>
		</g:if>
		<g:if test="${person?.contacts}">
			<div class="ui divided middle aligned selection list la-flex-list">
				<g:each in="${person?.addresses.sort{it.type?.getI10n('value')}}" var="address">
					<div class="item">
						<g:render template="/templates/cpa/address" model="${[address: address]}"></g:render>
					</div>
				</g:each>
			</div>
		</g:if>

		<g:if test="${!personRole}">
			<div class="ui divided middle aligned selection list la-flex-list">
				<g:each in="${person?.roleLinks}" var="role">
					<div class="item">
						<g:link controller="organisations" action="addressbook" id="${role.org?.id}">${role.org}</g:link>
					</div>
				</g:each>
			</div>
		</g:if>
	</g:if>
	<g:if test="${personRole}">

		<g:link controller="person" action="show" id="${personRole?.prsId}">
			${personRole?.prs?.title?.encodeAsHTML()}
			${personRole?.prs?.first_name?.encodeAsHTML()}
			${personRole?.prs?.middle_name?.encodeAsHTML()}
			${personRole?.prs?.last_name?.encodeAsHTML()}
		</g:link>
		<g:if test="${personRole?.functionType}">
			, ${personRole?.functionType?.getI10n('value')}
		</g:if>
        <g:if test="${personRole?.responsibilityType}">
            , ${personRole?.responsibilityType?.getI10n('value')}
        </g:if>

		<g:if test="${personRole?.prs?.contacts}">
			<div class="ui list">
				<g:each in="${personRole?.prs?.contacts.sort{it.content}}" var="contact">
					<div class="item">
						<g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
					</div>
				</g:each>
			</div>
		</g:if>
		<g:if test="${personRole?.prs?.addresses}">
			<div class="ui list">
				<g:each in="${personRole?.prs?.addresses.sort{it.type?.getI10n('value')}}" var="address">
					<div class="item">
						<g:render template="/templates/cpa/address" model="${[address: address]}"></g:render>
					</div>
				</g:each>
			</div>
		</g:if>

	</g:if>
</div>
							