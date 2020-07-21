<%@ page import="org.codehaus.groovy.grails.plugins.codecs.HTMLCodec; org.springframework.web.util.HtmlUtils" %>
<g:if test="${address}">
	<div class="ui item address-details">
        <% String url = ''
        if (address.name) url += address.name
        url += (url.length()>0 && address.street_1)? '+' : ''
        if (address.street_1) url += address.street_1
        url += (url.length()>0 && address.street_2)? '+' : ''
        if (address.street_2) url += address.street_2
        url += (url.length()>0 && address.zipcode)? '+' : ''
        if (address.zipcode) url += address.zipcode
        url += (url.length()>0 && address.city)? '+' : ''
        if (address.city) url += address.city
        url += (url.length()>0 && address.country)? '+' : ''
        if (address.country) url += address.country
        url = url.replace(' ', '+')
//        String encodedUrl = URLEncoder.encode(url)
        url = 'https://maps.google.com/?q='+url
//        url = 'https://www.google.com/maps/search/?api:1&query='+encodedUrl
        %>
        <div style="display: flex">
            <a href="${url}" target="_blank">
                <i class="ui js-linkGoogle icon building map marker alternate la-list-icon"></i>
            </a>
            <div class="content la-space-right">
                <strong>${address.type?.getI10n('value')}:</strong>
                <div class="item" onclick="addressedit(${address.id});" >
                    <g:if test="${address.name}">
                        <br />
                        ${address.name}
                    </g:if>
                    <g:if test="${address.additionFirst}">
                        <br />
                        ${address.additionFirst}
                    </g:if>
                    <g:if test="${address.additionSecond}">
                        <br />
                        ${address.additionSecond}
                    </g:if>
                    <g:if test="${address.street_1 || address.street_2}">
                        <br />
                        ${address.street_1} ${address.street_2}
                    </g:if>
                    <g:if test="${address.zipcode || address.city}">
                        <br />
                        ${address.zipcode} ${address.city}
                    </g:if>
                    <g:if test="${address.region || address.country}">
                        <br />
                        ${address.region?.getI10n('value')}
                        <g:if test="${address.region && address.country}">, </g:if>
                        ${address.country?.getI10n('value')}
                    </g:if>
                    <g:if test="${address.pob || address.pobZipcode || address.pobCity}">
                        <br />
                        <g:message code="address.pob.label" />
                        ${address.pob}
                        <g:if test="${address.pobZipcode || address.pobCity}">, </g:if>
                        ${address.pobZipcode} ${address.pobCity}
                    </g:if>
    %{--
                    <g:if test="${editable}">
                        <g:render template="/address/formModal" model="['addressId': address.id, modalId: 'addressFormModal' + address.id]"/>
                    </g:if>
    --}%
                </div>
            </div>
        </div>
        <div class="content">
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
<g:javascript>
    function addressedit(id) {
        var url = '<g:createLink controller="ajax" action="AddressEdit"/>?id='+id;
        private_address_modal(url)
    }
    function private_address_modal(url) {
        $.ajax({
            url: url,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#addressFormModal").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function () {
                        r2d2.initDynamicSemuiStuff('#addressFormModal');
                        r2d2.initDynamicXEditableStuff('#addressFormModal');

                        // ajaxPostFunc()
                    }
                }).modal('show');
            }
        });
    }
</g:javascript>