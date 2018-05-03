
<table class="ui stripped table">
	<thead>
		<tr>
            <th>
                ${message(code:'person.last_name.label')},
                ${message(code:'person.first_name.label')}
            </th>
            <g:if test="${controllerName == 'myInstitution'}">
			<th>Kontext</th>
            </g:if>
			<th>${message(code:'person.contacts.label')}</th>
			<th>${message(code:'person.addresses.label')}</th>
            <th></th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${persons}" var="person">
			<tr>
				<td>

                        ${person?.last_name?.encodeAsHTML()}
                        ,
                        ${person?.first_name?.encodeAsHTML()}
                        ${person?.middle_name?.encodeAsHTML()}

				</td>
                <g:if test="${controllerName == 'myInstitution'}">
				<td>
					<g:each in="${person?.roleLinks.unique{ it.org }}" var="role">
						<g:link controller="organisations" action="addressbook" id="${role.org?.id}">${role.org}</g:link>
                        <br />
					</g:each>
				</td>
                </g:if>

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
                <td class="x">
                    <g:if test="${editable}">
                        <g:form controller="person" action="delete">
                            <g:hiddenField name="id" value="${person?.id}" />
                                <g:link class="ui icon button" controller="person" action="show" id="${person?.id}">
                                    <i class="write icon"></i>
                                </g:link>
                                <button class="ui icon negative button" type="submit" name="_action_delete">
                                    <i class="trash alternate icon"></i>
                                </button>
                        </g:form>
                    </g:if>
                </td>
			</tr>
		</g:each>
	</tbody>
</table>
