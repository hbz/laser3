<%@ page import="de.laser.ui.Btn; de.laser.CustomerTypeService; de.laser.RefdataCategory;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.Combo;de.laser.RefdataValue;de.laser.Org" %>

<laser:htmlStart message="myinst.emptySubscription.label" />

        <ui:breadcrumbs>
            <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
            <ui:crumb message="myinst.emptySubscription.label" class="active" />
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon message="myinst.emptySubscription.label" />

        <ui:messages data="${flash}"/>

        <ui:form controller="subscription" action="processEmptySubscription" class="newSubscription">
                <div class="field required">
                    <label>${message(code:'myinst.emptySubscription.name')} <g:message code="messageRequiredField" /></label>
                    <input type="text" name="newEmptySubName" placeholder=""/>
                 </div>

                <div class="two fields">
                    <ui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from" value="${defaultStartYear}" />

                    <ui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to" value="${defaultEndYear}" />
                </div>

                <div class="field required">
                    <label>${message(code:'default.status.label')} <g:message code="messageRequiredField" /></label>
                    <ui:select name="status" from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" optionKey="id" optionValue="value"
                                  noSelection="${['' : '']}"
                                  value="${['':'']}"
                                  class="ui select dropdown"/>
                </div>

                <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                    <input type="hidden" name="type" value="${RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE.id}" />
                </g:if>
                <g:elseif test="${contextService.getOrg().isCustomerType_Consortium()}">
                    <input type="hidden" name="type" value="${RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id}" />
                </g:elseif>
                <g:elseif test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                    <input type="hidden" name="type" value="${RDStore.SUBSCRIPTION_TYPE_LOCAL.id}" />
                </g:elseif>
            <div class="field">
                <br />
                <input type="submit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${message(code:'default.button.create.label')}" />
                <input type="button" class="${Btn.SIMPLE_CLICKCONTROL}" onclick="JSPC.helper.goBack();" value="${message(code:'default.button.cancel.label')}" />
            </div>
        </ui:form>

    <hr />
        <laser:script file="${this.getGroovyPageFileName()}">
             $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
                if($("#valid_from").val() !== '' && $("#valid_to").val() !== '') {
                    var startDate = Date.parse(JSPC.helper.formatDate($("#valid_from").val()));
                    var endDate = Date.parse(JSPC.helper.formatDate($("#valid_to").val()));
                    return (startDate < endDate);
                }
                else return true;
             };
                    $('.newSubscription')
                            .form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            newEmptySubName: {
                                identifier  : 'newEmptySubName',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            },
                            valid_from: {
                                identifier: 'valid_from',
                                rules: [
                                    {
                                        type: 'endDateNotBeforeStartDate',
                                        prompt: '<g:message code="validation.startDateAfterEndDate"/>'
                                    }
                                ]
                            },
                            valid_to: {
                                identifier: 'valid_to',
                                rules: [
                                    {
                                        type: 'endDateNotBeforeStartDate',
                                        prompt: '<g:message code="validation.endDateBeforeStartDate"/>'
                                    }
                                ]
                            },
                            status: {
                                identifier  : 'status',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            }
                         }
                    });
        </laser:script>
<laser:htmlEnd />
