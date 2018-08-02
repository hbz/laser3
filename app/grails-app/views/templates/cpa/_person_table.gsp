
<table class="ui table la-table">
    <colgroup>
        <col style="width: 170px;">
        <col style="width: 236px;">
        <col style="width: 277px;">
        <col style="width: 332px;">
        <col style="width: 112px;">
    </colgroup>
	<thead>
		<tr>
            <th>
                ${message(code:'person.name.label')}
            </th>
            <g:if test="${controllerName == 'myInstitution'}">
			    <th>${message(code:'person.organisation.label')}
                </th>
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
                    ${person?.first_name?.encodeAsHTML() ? person?.last_name?.encodeAsHTML() + ', ' + person?.first_name?.encodeAsHTML() : person?.last_name?.encodeAsHTML()}
                    ${person?.middle_name?.encodeAsHTML()}
				</td>
                <g:if test="${controllerName == 'myInstitution'}">
				<td>
					<g:each in="${person?.roleLinks.unique{ it.org }}" var="role">
                        <div class="la-flexbox">
                            <i class="icon university la-list-icon"></i>
						    <g:link controller="organisations" action="addressbook" id="${role.org?.id}">${role.org}</g:link>
                        </div>
                        <div>
                        <g:if test="${role.functionType}">
                            (${role.functionType?.getI10n('value')})
                        </g:if>
                        </div>

					</g:each>
				</td>
                </g:if>

                <td>
                    <div class="ui divided middle aligned selection list la-flex-list la-contact-info-list">
                        <g:each in="${person.contacts.sort{it.content}}" var="contact">
                            <g:render template="/templates/cpa/contact" model="${[contact: contact]}"></g:render>
                        </g:each>
                    </div>
                </td>

                <td>
                    <g:each in="${person.addresses.sort{it.type?.getI10n('value')}}" var="address">
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
