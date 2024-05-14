<%@ page import="de.laser.survey.SurveyOrg;" %>
<laser:serviceInjection/>
<table class="ui table la-js-responsive-table la-table">
    <colgroup>
        <col style="width:  15px;">
        <col style="width: 236px;">
        <col style="width: 118px;">
        <col style="width:  82px;">
        <col style="width:  82px;">
    </colgroup>
    <thead>
    <tr>
        <th>${message(code: 'sidewide.number')}</th>
        <g:if test="${tmplShowOrgName}">
            <g:sortableColumn params="${params}" property="pr.org.sortname" title="${message(code: 'person.organisation.label')}"/>
        </g:if>
        <th>
            ${message(code: 'default.type.label')}
        </th>
        <th>
            ${message(code: 'address.label')}
        </th>
        <g:if test="${showSurveyInvoicingInformation}">
            <th>${message(code: 'surveyOrg.address.selected')}</th>
        </g:if>
        <g:if test="${showOptions}">
            <th class="la-action-info">${message(code: 'default.actions.label')}</th>
        </g:if>
    </tr>
    </thead>
    <tbody>
    <g:each in="${addresses}" var="address" status="c">
        <tr>
            <td>
                ${c + 1 + (offset ?: 0)}
            </td>
            <g:if test="${tmplShowOrgName}">
                <td>
                    <i class="icon university la-list-icon"></i>
                    <g:link controller="organisation" action="addressbook"
                            id="${address.org.id}">${address.org.name} (${address.org.sortname})</g:link>
                </td>
            </g:if>
            <td>
                <div class="ui divided middle aligned list la-flex-list ">
                <g:each in="${address.type.sort{it?.getI10n('value')}}" var="type">
                    <div class="ui item ">
                        ${type.getI10n('value')}
                    </div>
                </g:each>
                </div>
            </td>
            <td>
                <div class="ui item address-details">
                    <div style="display: flex">
                        <a href="${address.generateGoogleMapURL()}" target="_blank" class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code: 'address.googleMaps.link')}">
                            <i class="ui js-linkGoogle blue icon building map marker alternate la-list-icon"></i>
                        </a>

                        <g:if test="${address.name}">
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
                            <g:if test="${address.region && address.country}">,</g:if>
                            ${address.country?.getI10n('value')}
                        </g:if>
                        <g:if test="${address.pob || address.pobZipcode || address.pobCity}">
                            <br />
                            <g:message code="address.pob.label"/>
                            ${address.pob}
                            <g:if test="${address.pobZipcode || address.pobCity}">,</g:if>
                            ${address.pobZipcode} ${address.pobCity}
                        </g:if>
                    </div>
                </div>
            </td>
        <g:if test="${showSurveyInvoicingInformation}">
            <td>
                <g:if test="${editable}">
                    <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndAddress(institution, surveyConfig, address)}">
                        <g:link controller="myInstitution" action="setSurveyInvoicingInformation"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setAddress: false, addressId: address.id]">
                            <i class="check bordered large green icon"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="myInstitution" action="setSurveyInvoicingInformation"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setAddress: true, addressId: address.id]">
                            <i class="close bordered large red icon"></i>
                        </g:link>
                    </g:else>
                </g:if>
                <g:else>
                    <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndAddress(institution, surveyConfig, address)}">
                        <i class="check bordered large green icon"></i>
                    </g:if>
                </g:else>
            </td>
        </g:if>
            <g:if test="${showOptions}">
                <td class="x">
                    <g:if test="${editable && tmplShowDeleteButton}">

                        <button type="button" onclick="JSPC.app.editAddress(${address.id})" class="ui icon button blue la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="write icon"></i>
                        </button>
                        <g:link class="ui negative button icon js-open-confirm-modal la-modern-button"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.address.addressbook")}"
                                data-confirm-term-how="delete"
                                controller="ajax" action="delete" params="[cmd: 'deleteAddress', oid: genericOIDService.getOID(address)]"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="trash alternate outline icon"></i>
                        </g:link>
                    </g:if>
                </td>
            </g:if>
        </tr>
    </g:each>
    </tbody>
</table>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editAddress = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", "<g:createLink controller="ajaxHtml" action="editAddress"/>?id=" + id);
        func();
    }
</laser:script>