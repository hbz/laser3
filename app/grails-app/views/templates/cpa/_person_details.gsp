<div class="person-details">
	<g:if test="${person}">
		<div>
			<strong>
				<g:link controller="person" action="show" id="${person?.id}">
					${person?.first_name?.encodeAsHTML()}
					${person?.middle_name?.encodeAsHTML()}
					${person?.last_name?.encodeAsHTML()}
				</g:link>
			</strong>
			
			<ul>
				<g:each in="${person?.contacts}" var="contact">
					<li>
						<g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
					</li>
				</g:each>
			</ul>
			<ul>
				<g:each in="${person?.addresses}" var="address">
					<li>
						<g:render template="/templates/cpa/address" model="${[address: address]}"></g:render>
					</li>
				</g:each>
			</ul>
		</div>
	</g:if>
	<g:if test="${personRole}">
		<div>
			<strong>
				<g:link controller="person" action="show" id="${personRole?.prsId}">
					${personRole?.prs?.first_name?.encodeAsHTML()}
					${personRole?.prs?.middle_name?.encodeAsHTML()}
					${personRole?.prs?.last_name?.encodeAsHTML()}
				</g:link>
			</strong>
			<br />
			${personRole?.functionType}
			
			<ul>
				<g:each in="${personRole?.prs?.contacts}" var="contact">
					<li>
						<g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
					</li>
				</g:each>
			</ul>
			<ul>
				<g:each in="${personRole?.prs?.addresses}" var="address">
					<li>
						<g:render template="/templates/cpa/address" model="${[address: address]}"></g:render>
					</li>
				</g:each>
			</ul>
		</div>
	</g:if>
</div>
							