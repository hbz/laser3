<g:if test="${address}">
	<div class="ui item address-details">
        <span class="la-popup-tooltip la-delay"  data-content="${message(code:'adress.icon.label.adress')}" data-position="top right" data-variation="tiny">
            <i class="ui icon building map marker alternate la-list-icon"></i>
        </span>
        <div class="content la-space-right">

            %{--<a href="#addressFormModal">--}%
            %{--<a onclick="addressEdit(${address?.id});">--}%
            <div class="item" data-href="#addressFormModal${address.id}" data-semui="modal" >
                <strong>${address.type?.getI10n('value')}:</strong>
                <g:if test="${address?.name}">
                    <br />
                    ${address?.name}
                </g:if>
                <g:if test="${address?.additionFirst}">s
                    <br />
                    ${address?.additionFirst}
                </g:if>
                <g:if test="${address?.additionSecond}">
                    <br />
                    ${address?.additionSecond}
                </g:if>
                <g:if test="${address?.street_1 || address?.street_2}">
                    <br />
                    ${address?.street_1} ${address?.street_2}
                </g:if>
                <g:if test="${address?.zipcode || address?.city}">
                    <br />
                    ${address?.zipcode} ${address?.city}
                </g:if>
                <g:if test="${address?.region || address?.country}">
                    <br />
                    ${address?.region?.getI10n('value')}
                    <g:if test="${address?.region && address?.country}">, </g:if>
                    ${address?.country?.getI10n('value')}
                </g:if>
                <g:if test="${address?.pob || address?.pobZipcode || address?.pobCity}">
                    <br />
                    <g:message code="address.pob.label" />
                    ${address?.pob}
                    <g:if test="${address?.pobZipcode || address?.pobCity}">, </g:if>
                    ${address?.pobZipcode} ${address?.pobCity}
                </g:if>
            </div>
            %{--</a>--}%
            %{--</g:link>--}%
        </div>
        <div class="content">
            <g:if test="${editable}">
                <div class="item" data-href="#addressFormModal${address.id}" data-semui="modal" >
                    <i class="write icon"></i>
                </div>
                <br />
                <input class="ui mini icon button" type="button" data-semui="modal"
                   data-href="#addressFormModal${address.id}"
                   value="${message(code: 'person.addresses.label')}">
                <g:render template="/address/formModal" model="['addressId': address.id, modalId: 'addressFormModal' + address.id]"/>
            </g:if>
            <g:if test="${editable && tmplShowDeleteButton}">
                <div class="ui mini icon buttons">
                    <g:set var="oid" value="${address.class.name}:${address.id}" />
                    <g:link class="ui negative button js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.address.addressbook")}"
                            data-confirm-term-how="delete"
                            controller="ajax" action="delete" params="[cmd: 'deleteAddress', oid: oid]"  >
                        <i class="trash alternate icon"></i>
                    </g:link>
                </div>
            </g:if>
        </div>
	</div>
</g:if>
%{--$("[id^='addressFormModal']").remove();--}%
%{--<g:javascript>--}%
    %{--function addressEdit(id) {--}%
        %{--$.ajax({--}%
            %{--url: '<g:createLink controller="address" action="show"/>?id='+id,--}%
            %{--success: function(result){--}%
                %{--$("#dynamicModalContainer").empty();--}%
                %{--$("#addressFormModal").remove();--}%
                %{--$("#dynamicModalContainer").html(result);--}%
                %{--$("#dynamicModalContainer .ui.modal").modal({--}%
                    %{--onVisible: function () {--}%
                        %{--r2d2.initDynamicSemuiStuff('#addressFormModal');--}%
                        %{--r2d2.initDynamicXEditableStuff('#addressFormModal');--}%

                        %{--// ajaxPostFunc()--}%
                    %{--}--}%
                %{--}).modal('show');--}%
            %{--}--}%
        %{--});--}%
    %{--}--}%
%{--</g:javascript>--}%
