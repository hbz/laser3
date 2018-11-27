<g:if test="${address}">
	<div class="ui item address-details">
        <span  data-tooltip="${message(code:'adress.icon.label.adress')}" data-position="top right" data-variation="tiny">
            <i class="ui icon building map marker alternate la-list-icon"></i>
        </span>
        <div class="content la-space-right">
            <g:link controller="address" action="show" id="${address?.id}">
                ${address.type?.getI10n('value')}:

                <g:if test="${address?.name}">
                    <br />
                    ${address?.name}
                </g:if>

                <br />
                ${address?.street_1} ${address?.street_2}

                <br />
                ${address?.zipcode} ${address?.city}

                <g:if test="${address?.state || address?.country}">
                    <br />
                    ${address?.state?.getI10n('value')}
                    <g:if test="${address?.state && address?.country}">, </g:if>
                    ${address?.country?.getI10n('value')}
                </g:if>

                <g:if test="${address?.pob || address?.pobZipcode || address?.pobCity}">
                    <br />
                    <g:message code="address.pob.label" default="Pob" />
                    ${address?.pob}
                    <g:if test="${address?.pobZipcode || address?.pobCity}">, </g:if>
                    ${address?.pobZipcode} ${address?.pobCity}
                </g:if>

                <g:if test="${address?.additionFirst}">
                    <br />
                    ${address?.additionFirst}
                </g:if>
                <g:if test="${address?.additionSecond}">
                    <br />
                    ${address?.additionSecond}
                </g:if>
            </g:link>
        </div>
        <div class="content">
            <g:if test="${editable && tmplShowDeleteButton}">
                <div class="ui mini icon buttons">
                    <g:set var="oid" value="${address.class.name}:${address.id}" />
                    <g:link class="ui negative button js-open-confirm-modal"
                            data-confirm-term-what="address"
                            data-confirm-term-where="addressbook"
                            data-confirm-term-how="delete"
                            controller="ajax" action="delete" params="[cmd: 'deleteAddress', oid: oid]"  >
                        <i class="trash alternate icon"></i>
                    </g:link>
                </div>
            </g:if>
        </div>


	</div>
</g:if>