
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
        <div class="ui divided middle aligned selection list la-flex-list la-list-border-around">

        <div class="ui item person-details">
            <span></span>
            <div class="content" class="la-space-right">
                <h5 class="ui header">
                <g:link controller="person" action="show" id="${personRole?.prs?.id}">
                    ${personRole?.prs?.title}
                    ${personRole?.prs?.first_name}
                    ${personRole?.prs?.middle_name}
                    ${personRole?.prs?.last_name}
                </g:link>
                </h5>
                <g:if test="${personRole?.functionType}">
                     (${personRole?.functionType?.getI10n('value')})
                </g:if>
                <g:if test="${personRole?.responsibilityType}">
                    (${personRole?.responsibilityType?.getI10n('value')})
                </g:if>
                <script>
                    $('.person-details').mouseenter(function(){
                        $( this).parent().addClass('la-border-selected');
                    })
                    $('.person-details').mouseleave(function(){
                        $( this).parent().removeClass('la-border-selected');
                    })
                </script>
            </div>

            <div class="content">
                <g:if test="${editable && tmplShowDeleteButton}">
                    <g:set var="oid" value="${personRole?.class.name}:${personRole?.id}" />

                    <g:link class="ui mini icon negative button js-open-confirm-modal"
                            data-confirm-term-what="contact"
                            data-confirm-term-where="organisation"
                            data-confirm-term-how="unlink"
                            controller="ajax" action="delete" params="[cmd: 'deletePersonRole', oid: oid]">
                        <i class="unlink icon"></i>
                    </g:link>
                </g:if>
            </div>
        </div><!-- .person-details -->
        <g:if test="${personRole?.prs?.contacts}">
            <g:each in="${personRole?.prs?.contacts?.toSorted()}" var="contact">
                <g:if test="${tmplConfigShow.contains(contact?.contentType?.value)}">
                    <g:render template="/templates/cpa/contact" model="${[
                            contact: contact,
                            tmplShowDeleteButton: true
                    ]}"/>
                </g:if>
            </g:each>

        </g:if>
        <g:if test="${tmplConfigShow?.contains('address') && personRole?.prs?.addresses}">

            <g:each in="${personRole?.prs?.addresses?.sort{it.type?.getI10n('value')}}" var="address">
                <g:render template="/templates/cpa/address" model="${[address: address, tmplShowDeleteButton: tmplShowDeleteButton]}"/>
            </g:each>

        </g:if>
        </div><!-- .la-flex-list -->
    </g:if>

							