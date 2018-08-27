
<g:if test="${person}">
        <div class="ui divided middle aligned selection list la-flex-list">

        <div class="ui item person-details">
            <h5 class="ui header">
                <g:link controller="person" action="show" id="${person?.id}">
                    ${person?.title}
                    ${person?.first_name}
                    ${person?.middle_name}
                    ${person?.last_name}
                </g:link>
            </h5>

        </div><!-- .person-details -->

        <g:if test="${person?.contacts}">

            <g:each in="${person?.contacts.sort{it.content}}" var="contact">
                <g:render template="/templates/cpa/contact" model="${[contact: contact, tmplShowDeleteButton: tmplShowDeleteButton]}"></g:render>
            </g:each>

        </g:if>
        <g:if test="${person?.addresses}">

            <g:each in="${person?.addresses.sort{it.type?.getI10n('value')}}" var="address">
                <g:render template="/templates/cpa/address" model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton]}"></g:render>
            </g:each>

        </g:if>

        <g:if test="${! personRole}">

            <g:each in="${person?.roleLinks}" var="role">
                <div class="item">
                    <g:link controller="organisations" action="addressbook" id="${role.org?.id}">${role.org}</g:link>
                </div>
            </g:each>

        </g:if>

        </div><!-- .la-flex-list -->
	</g:if>

	<g:if test="${personRole}">
        <div class="ui divided middle aligned selection list la-flex-list">

        <div class="ui item person-details">
            <g:link controller="person" action="show" id="${personRole?.prs?.id}">
                ${personRole?.prs?.title}
                ${personRole?.prs?.first_name}
                ${personRole?.prs?.middle_name}
                ${personRole?.prs?.last_name}
            </g:link>
            <g:if test="${personRole?.functionType}">
                 &nbsp;
                 (${personRole?.functionType?.getI10n('value')})
            </g:if>
            <g:if test="${personRole?.responsibilityType}">
                &nbsp;
                (${personRole?.responsibilityType?.getI10n('value')})
            </g:if>
            <g:if test="${editable && tmplShowDeleteButton}">
                <g:set var="oid" value="${personRole?.class.name}:${personRole?.id}" />

                <g:link class="ui mini icon negative button deletePersonRoleLink-${personRole?.id}"
                        controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
                    <i class="trash alternate icon"></i>
                </g:link>
                <script>
                    $('.deletePersonRoleLink-${personRole?.id}').on( 'click', function(event) {
                        event.preventDefault()
                        if (confirm('Wollen Sie wirklich die Zuordnung der Person zu dieser Organisation löschen?')) {
                            window.location.href = $(this).attr('href')
                        }
                    })
                </script>
            </g:if>
        </div><!-- .person-details -->

        <g:if test="${personRole?.prs?.contacts}">

            <g:each in="${personRole?.prs?.contacts.sort{it.content}}" var="contact">
                <g:render template="/templates/cpa/contact" model="${[contact: contact, tmplShowDeleteButton: tmplShowDeleteButton]}"></g:render>
            </g:each>

        </g:if>

        <g:if test="${personRole?.prs?.addresses}">

            <g:each in="${personRole?.prs?.addresses.sort{it.type?.getI10n('value')}}" var="address">
                <g:render template="/templates/cpa/address" model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton]}"></g:render>
            </g:each>

        </g:if>

        </div><!-- .la-flex-list -->
    </g:if>

							