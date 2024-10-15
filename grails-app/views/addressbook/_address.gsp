<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${address}">
	<div class="ui item address-details">
        <div style="display: flex" class="js-copyTriggerParent">
            <a href="${address.generateGoogleMapURL()}" target="_blank" class="la-popup-tooltip" data-position="top right" data-content="${message(code: 'address.googleMaps.link')}">
                <i class="${Icon.LNK.GOOGLE_MAPS} js-linkGoogle la-list-icon la-js-copyTriggerIcon"></i>
            </a>
            <div class="content la-space-right js-copyTrigger la-popup-tooltip" data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
                <g:if test="${ !hideAddressType}">
                    <strong>
                        <g:each in="${address.type.sort{it?.getI10n('value')}}" var="type">
                            <p class="js-copyTopic">${type.getI10n('value')}</p>
                        </g:each>
                     </strong>
                </g:if>
                <%-- in one line because otherwise, the copy function copies many unneeded whitespaces --%>
                <div class="item js-copyTopic" ><g:if test="${address.name}">${address.name}<br /></g:if> <g:if test="${address.additionFirst}">${address.additionFirst}<br /></g:if> <g:if test="${address.additionSecond}">${address.additionSecond}<br /></g:if> <g:if test="${address.street_1 || address.street_2}">${address.street_1} ${address.street_2}<br /></g:if> <g:if test="${address.zipcode || address.city}">${address.zipcode} ${address.city}<br /></g:if> <g:if test="${address.region || address.country}">${address.region?.getI10n('value')}<g:if test="${address.region && address.country}">,</g:if> ${address.country?.getI10n('value')}</g:if><g:if test="${address.pob || address.pobZipcode || address.pobCity}"><br /><g:message code="address.pob.label" />${address.pob} <g:if test="${address.pobZipcode || address.pobCity}">,</g:if> ${address.pobZipcode} ${address.pobCity}</g:if></div>
            </div>
        </div>
        <div class="content">
            <g:if test="${editable && tmplShowDeleteButton}">
                <div class="ui icon buttons">
                    <a class="${Btn.MODERN.SIMPLE}"
                       onclick="JSPC.app.editAddress(${address.id});"
                       role="button"
                       aria-label="${message(code: 'ariaLabel.change.universal')}">
                        <i class="${Icon.CMD.EDIT}"></i>
                    </a>

                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.address.addressbook")}"
                            data-confirm-term-how="delete"
                            controller="addressbook" action="deleteAddress" params="[id: address.id]"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                        <i class="${Icon.CMD.DELETE}"></i>
                    </g:link>
                </div>
            </g:if>
        </div>
	</div>
</g:if>
<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editAddress = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", "<g:createLink controller="ajaxHtml" action="editAddress"/>?id=" + id);
        func();
    }
</laser:script>