
<table class="ui table la-table">
    <colgroup>
        <col style="width:  30px;">
        <col style="width: 170px;">
        <col style="width: 236px;">
        <col style="width: 277px;">
        <col style="width: 332px;">
        <col style="width:  82px;">
    </colgroup>
	<thead>
		<tr>
            <th></th>
            <th>
                ${message(code:'person.name.label')}
            </th>
            <th>
                <g:if test="${controllerName == 'myInstitution'}">
                    ${message(code:'person.organisation.label')}
                </g:if>
                <g:else>
                    Funktion
                </g:else>
            </th>
			<th>${message(code:'person.contacts.label')}</th>
			<th>${message(code:'person.addresses.label')}</th>
            <th></th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${persons}" var="person" status="c">
			<tr>
                <td>
                    ${c + 1 + (offset?:0)}
                </td>
				<td>
                    ${person?.first_name? person?.last_name + ', ' + person?.first_name : person?.last_name}
                    ${person?.middle_name}
				</td>

				<td>
					<g:each in="${person?.roleLinks.unique{ it.org }}" var="role">
                        <g:if test="${controllerName == 'myInstitution'}">
                            <div class="la-flexbox">
                                <i class="icon university la-list-icon"></i>
                                <g:link controller="organisations" action="addressbook" id="${role.org?.id}">${role.org}</g:link>
                            </div>
                            <div>
                                <g:if test="${role.functionType}">
                                    (${role.functionType?.getI10n('value')})
                                </g:if>
                            </div>
                        </g:if>
                        <g:else>
                            <div>
                                <g:if test="${role.functionType}">
                                    ${role.functionType?.getI10n('value')}
                                </g:if>
                            </div>
                        </g:else>
					</g:each>
                </td>
                <td>
                    <div class="ui divided middle aligned selection list la-flex-list ">
                        <g:each in="${person.contacts.sort{it.content}}" var="contact">
                            <g:render template="/templates/cpa/contact" model="${[
                                    contact: contact,
                                    tmplShowDeleteButton: true
                            ]}">

                            </g:render>
                        </g:each>
                    </div>
                </td>

                <td>
                    <div class="ui divided middle aligned selection list la-flex-list ">
                        <g:each in="${person.addresses.sort{it.type?.getI10n('value')}}" var="address">
                            <g:render template="/templates/cpa/address" model="${[
                                    address: address,
                                    tmplShowDeleteButton: true
                            ]}"></g:render>
                        </g:each>
                    </div>
                </td>
                <td class="x">
                    <g:if test="${editable}">
                            <g:form controller="person" action="delete" data-confirm-id="${person?.id.toString()+ '_form'}">
                                <g:hiddenField name="id" value="${person?.id}" />
                                    <g:link class="ui icon button" controller="person" action="show" id="${person?.id}">
                                        <i class="write icon"></i>
                                    </g:link>
                                    <div class="ui icon negative button js-open-confirm-modal" data-confirm-id="${person?.id}" >
                                        <i class="trash alternate icon"></i>
                                    </div>
                            </g:form>
                    </g:if>
                </td>
			</tr>
		</g:each>
	</tbody>
</table>


<semui:confirmationModal text="Wollen Sie diese Person wirklich aus dem System löschen?" deletemodal="true" />

<r:script>
    $(function(){
        $(".js-open-confirm-modal").click(function(){
            var tmp = this.getAttribute("data-confirm-id")+'_form';
            $('.mini.modal')
                    .modal({
                closable  : false,
                onApprove : function() {
                    $('[data-confirm-id='+tmp+']').submit();
                }
            })
                    .modal('show')
            ;
        });
    });
</r:script>

