<g:if test="${address}">
	<div class="ui item address-details">
        <div class="content">
            <g:link controller="address" action="show" id="${address?.id}">
                ${address.type?.getI10n('value')}:

                <g:if test="${address?.name}">
                    <br />
                    ${address?.name.encodeAsHTML()}
                </g:if>

                <br />
                ${address?.street_1.encodeAsHTML()} ${address?.street_2.encodeAsHTML()}

                <br />
                ${address?.zipcode.encodeAsHTML()} ${address?.city.encodeAsHTML()}

                <g:if test="${address?.state || address?.country}">
                    <br />
                    ${address?.state?.getI10n('value')}
                    <g:if test="${address?.state && address?.country}">, </g:if>
                    ${address?.country.getI10n('value')}
                </g:if>

                <g:if test="${address?.pob || address?.pobZipcode || address?.pobCity}">
                    <br />
                    <g:message code="address.pob.label" default="Pob" />
                    ${address?.pob.encodeAsHTML()}
                    <g:if test="${address?.pobZipcode || address?.pobCity}">, </g:if>
                    ${address?.pobZipcode?.encodeAsHTML()} ${address?.pobCity?.encodeAsHTML()}
                </g:if>

                <g:if test="${address?.additionFirst}">
                    <br />
                    ${address?.additionFirst.encodeAsHTML()}
                </g:if>
                <g:if test="${address?.additionSecond}">
                    <br />
                    ${address?.additionSecond.encodeAsHTML()}
                </g:if>
            </g:link>
        </div>

        <g:if test="${editable && tmplShowDeleteButton}">
            <div class="ui mini icon buttons">
                <g:set var="oid" value="${address.class.name}:${address.id}" />
                <g:link class="ui negative button" controller="${controller}" action="${action}" id="${id}" params="[cmd: 'deleteAddress', oid: oid]">
                    <i class="trash alternate icon"></i>
                </g:link>
            </div>
        </g:if>
	</div>
</g:if>