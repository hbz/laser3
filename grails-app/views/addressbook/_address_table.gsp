<%@ page import="de.laser.storage.RDStore; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyOrg;" %>
<laser:serviceInjection/>
<table class="ui table la-js-responsive-table la-table">
    <colgroup>
        <col style="width:  15px;">
        <g:if test="${tmplShowOrgName}">
                <col style="width: 236px;">
        </g:if>
        <col style="width: 118px;">
        <col style="width:  82px;">
        <g:if test="${showSurveyInvoicingInformation}">
                <col style="width:  82px;">
        </g:if>
        <g:if test="${showPreferredForSurvey}">
            <col style="width:  82px;">
        </g:if>
        <g:if test="${showOptions}">
            <col style="width:  82px;">
        </g:if>
    </colgroup>
    <thead>
    <tr>
        <th>${message(code: 'sidewide.number')}</th>
        <g:if test="${tmplShowOrgName}">
            <g:sortableColumn params="${params}" property="sortname" title="${message(code: 'person.organisation.label')}"/>
        </g:if>
        <th>
            ${message(code: 'default.type.label')}
        </th>
        <th>
            ${message(code: 'address.label')}
        </th>
        <g:if test="${showSurveyInvoicingInformation}">
            <th class="center aligned">${message(code: 'surveyOrg.address.selected')}</th>
        </g:if>
        <g:if test="${showPreferredForSurvey}">
            <th class="center aligned">${message(code: 'address.preferredForSurvey')}</th>
        </g:if>
        <g:if test="${showOptions}">
            <th class="center aligned">
                <ui:optionsIcon />
            </th>
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
                    <g:if test="${address.org}">
                        <i class="${Icon.ORG} la-list-icon"></i>
                        <g:link controller="organisation" action="addressbook"
                                id="${address.org.id}">${address.org.name} (${address.org.sortname})</g:link>
                    </g:if>
                    <g:if test="${address.provider}">
                        <i class="${Icon.PROVIDER} la-list-icon"></i>
                        <g:link controller="provider" action="addressbook"
                                id="${address.provider.id}">${address.provider.name} (${address.provider.sortname})</g:link>
                    </g:if>
                    <g:if test="${address.vendor}">
                        <i class="${Icon.VENDOR} la-list-icon"></i>
                        <g:link controller="vendor" action="addressbook"
                                id="${address.vendor.id}">${address.vendor.name} (${address.vendor.sortname})</g:link>
                    </g:if>
                </td>
            </g:if>
            <td>
                <div class="ui divided middle aligned list la-flex-list">
                <g:each in="${address.type.sort{it?.getI10n('value')}}" var="type">
                    <div class="ui item">
                        ${type.getI10n('value')}
                    </div>
                </g:each>
                </div>
            </td>
            <td>
                <div class="ui item address-details">
                    <div style="display: flex">
                        <a href="${address.generateGoogleMapURL()}" target="_blank" class="la-popup-tooltip" data-position="top right" data-content="${message(code: 'address.googleMaps.link')}">
                            <i class="${Icon.LNK.GOOGLE_MAPS} js-linkGoogle la-list-icon"></i>
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
            <td class="center aligned">
                <g:if test="${editable && controllerName == 'myInstitution'}">
                    <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndAddress(participant, surveyConfig, address)}">
                        <g:link controller="myInstitution" action="surveyInfos"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setAddress: false, addressId: address.id, viewTab: 'invoicingInformation', subTab: 'addresses']">
                            <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="myInstitution" action="surveyInfos"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setAddress: true, addressId: address.id, viewTab: 'invoicingInformation', subTab: 'addresses']">
                            <i class="${Icon.SYM.CHECKBOX} large"></i>
                        </g:link>
                    </g:else>
                </g:if>
                <g:elseif test="${editable && controllerName == 'survey'}">
                    <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndAddress(participant, surveyConfig, address)}">
                        <g:link controller="survey" action="evaluationParticipant"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setAddress: false, addressId: address.id, viewTab: 'invoicingInformation', subTab: 'addresses', participant: participant.id]">
                            <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="survey" action="evaluationParticipant"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setAddress: true, addressId: address.id, viewTab: 'invoicingInformation', subTab: 'addresses', participant: participant.id]">
                            <i class="${Icon.SYM.CHECKBOX} large"></i>
                        </g:link>
                    </g:else>
                </g:elseif>
                <g:else>
                    <g:if test="${SurveyOrg.findByOrgAndSurveyConfigAndAddress(participant, surveyConfig, address)}">
                        <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                    </g:if>
                </g:else>
            </td>
        </g:if>
            <g:if test="${showPreferredForSurvey}">
                <td class="center aligned">
                <g:if test="${address.type && RDStore.ADDRESS_TYPE_BILLING.id in address.type.id}">
                    <g:if test="${contextService.getOrg().isCustomerType_Inst() && editable}">
                        <g:if test="${address.preferredForSurvey}">
                            <g:link controller="ajax" action="editPreferredConcatsForSurvey"
                                    params="[id: contextService.getOrg().id, setPreferredAddress: false, addressId: address.id]">
                                <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="ajax" action="editPreferredConcatsForSurvey"
                                    params="[id: contextService.getOrg().id, setPreferredAddress: true, addressId: address.id]">
                                <i class="${Icon.SYM.CHECKBOX} large"></i>
                            </g:link>
                        </g:else>
                    </g:if>
                    <g:else>
                        <g:if test="${address.preferredForSurvey}">
                            <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                        </g:if>
                    </g:else>
                </g:if>
                </td>
            </g:if>
            <g:if test="${showOptions}">
                <td class="x">
                    <g:if test="${editable && tmplShowDeleteButton}">

                        <button type="button" onclick="JSPC.app.editAddress(${address.id})" class="${Btn.MODERN.SIMPLE}"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                        </button>
                        <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.address.addressbook")}"
                                data-confirm-term-how="delete"
                                controller="addressbook" action="deleteAddress" params="[id: address.id]"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="${Icon.CMD.DELETE}"></i>
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