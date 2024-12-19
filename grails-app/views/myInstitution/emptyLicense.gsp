<%@page import="de.laser.ui.Btn; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDConstants" %>

<laser:htmlStart message="license.new" />

    <ui:breadcrumbs>
        <ui:crumb controller="myInstitution" action="currentLicenses" message="license.current"/>
        <ui:crumb message="license.new" class="active"/>
    </ui:breadcrumbs>

    <ui:controlButtons>
        <laser:render template="actions"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="license.new" />

    <ui:messages data="${flash}"/>

    <ui:form controller="myInstitution" action="processEmptyLicense" class="newLicence">
            <g:if test="${subInstance}">
                <g:hiddenField id="sub_id_${subInstance.id}" name="sub" value="${subInstance.id}"/>
                <ui:msg class="info" hideClose="true" message="myinst.licensewithSub.notice" args="[subInstance.name]" />
            </g:if>

            <g:if test="${params.baselicense}">
                <g:hiddenField name="baselicense" value="${params.baselicense}"/>
                <ui:msg class="info" hideClose="true" message="myinst.copyLicense.notice" args="[params.licenseName]" />
            </g:if>

            <div class="field required">
                <label>${message(code: 'myinst.emptyLicense.name')} <g:message code="messageRequiredField" /></label>
                <input required type="text" name="licenseName" value="${params.licenseName}" placeholder=""/>
            </div>

            <div class="two fields">
                <ui:datepicker label="license.startDate.label" id="licenseStartDate" name="licenseStartDate" value="${params.licenseStartDate?:defaultStartYear}" />

                <ui:datepicker label="license.endDate.label" id="licenseEndDate" name="licenseEndDate" value="${params.licenseEndDate?:defaultEndYear}"/>
            </div>

            <div class="field required">
                <label>${message(code:'default.status.label')} <g:message code="messageRequiredField" /></label>
                <%
                    def fakeList = []
                    fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_STATUS))
                    fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', RDConstants.LICENSE_STATUS))
                %>
                <ui:select name="status" from="${fakeList}" optionKey="id" optionValue="value"
                                noSelection="${['' : '']}"
                                value="${['':'']}"
                                class="ui select dropdown"/>
            </div>

        <div class="field">
            <br />
            <input type="submit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${message(code: 'default.button.create.label')}"/>
            <input type="button" class="${Btn.SIMPLE_CLICKCONTROL}" onclick="JSPC.helper.goBack();" value="${message(code:'default.button.cancel.label')}" />
        </div>
    </ui:form>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.license-results input[type="radio"]').click(function () {
        $('.license-options').slideDown('fast');
    });

    $('.license-options .delete-license').click(function () {
        $('.license-results input:checked').each(function () {
            $(this).parent().parent().fadeOut('slow');
            $('.license-options').slideUp('fast');
        })
    });

    $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
                if($("#licenseStartDate").val() !== '' && $("#licenseEndDate").val() !== '') {
                    var startDate = Date.parse(JSPC.helper.formatDate($("#licenseStartDate").val()));
                    var endDate = Date.parse(JSPC.helper.formatDate($("#licenseEndDate").val()));
                    return (startDate < endDate);
                }
                else return true;
             };
                    $('.newLicence').form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            licenseName: {
                                identifier  : 'licenseName',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            },
                            licenseStartDate: {
                                identifier: 'licenseStartDate',
                                rules: [
                                    {
                                        type: 'endDateNotBeforeStartDate',
                                        prompt: '<g:message code="validation.startDateAfterEndDate"/>'
                                    }
                                ]
                            },
                            licenseEndDate: {
                                identifier: 'licenseEndDate',
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
