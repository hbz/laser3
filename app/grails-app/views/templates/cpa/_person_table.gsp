
<table class="ui stripped table">
	<thead>
		<tr>
            <th>
                ${message(code:'person.last_name.label')},
                ${message(code:'person.first_name.label')}
            </th>
			<th></th>
			<th>${message(code:'person.contacts.label')}</th>
			<th>${message(code:'person.addresses.label')}</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${persons}" var="person">
			<tr>
				<td>
                    <g:link controller="person" action="show" id="${person.id}">
                        ${person?.last_name?.encodeAsHTML()}
                        ,
                        ${person?.first_name?.encodeAsHTML()}
                        ${person?.middle_name?.encodeAsHTML()}
                    </g:link>
				</td>

				<td>
					<g:each in="${person?.roleLinks}" var="role">
						<g:link controller="organisations" action="addressbook" id="${role.org?.id}">${role.org}</g:link>
					</g:each>
				</td>

                <td>
                    <g:each in="${person.contacts}" var="contact">
                        <g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
                    </g:each>
                </td>

                <td>
                    <g:each in="${person.addresses}" var="address">
                        <g:render template="/templates/cpa/address" model="${[address: address]}"></g:render>
                    </g:each>
                </td>
			</tr>
		</g:each>
	</tbody>
</table>
