
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

            <g:each in="${person?.contacts?.toSorted()}" var="contact">
                <g:render template="/templates/cpa/contact" model="${[contact: contact, tmplShowDeleteButton: tmplShowDeleteButton]}"/>
            </g:each>

        </g:if>
        <g:if test="${person?.addresses}">

            <g:each in="${person?.addresses?.sort{it?.type?.getI10n('value')}}" var="address">
                <g:render template="/templates/cpa/address" model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton]}"/>
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
            <g:link controller="person" action="show" id="${person?.id}">
            %{--<g:link controller="person" action="show" id="${personRole?.prsId}">--}%
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

        </div><!-- .person-details -->
        <g:if test="${personRole?.prs?.contacts}">
            <g:each in="${personRole?.prs?.contacts?.toSorted()}" var="contact">
                <g:if test="${tmplConfigShow.contains(contact?.contentType?.value)}">
                    <g:render template="/templates/cpa/contact" model="${[
                            contact: contact,
                            tmplShowDeleteButton: tmplShowDeleteButton
                    ]}"/>
                </g:if>
                <g:else>
                    %{--TODO: Nach erfolgreichem Test diesen else-Zweig bitte wieder entfernen.--}%
                    TESTAUSGABE: ${contact?.contentType} (Hier könnte der gewünschte Kontakt fehlen!!!)<br>
                </g:else>
            </g:each>

        </g:if>
        <g:if test="${tmplConfigShow?.contains('address') && personRole?.prs?.addresses}">

            <g:each in="${personRole?.prs?.addresses?.sort{it.type?.getI10n('value')}}" var="address">
                <g:render template="/templates/cpa/address" model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton]}"/>
            </g:each>

        </g:if>
        </div><!-- .la-flex-list -->
    </g:if>

							