<%@ page import="de.laser.utils.RandomUtils; de.laser.Org; de.laser.License; de.laser.Subscription; de.laser.Task; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<laser:serviceInjection />

<ui:modal id="modalCreateTask" message="task.create.new">

    <g:set var="preID" value="${RandomUtils.getHtmlID()}" />

    <g:form id="${preID}_form" class="ui form" url="[controller: 'task', action: 'createTask']" method="post">
        <g:hiddenField id="${preID}_preID" name="preID" value="${preID}" />

        <g:if test="${controllerName != 'myInstitution' && controllerName != 'ajaxHtml'}">
            <g:hiddenField id="${preID}_${owntp}" name="${owntp}" value="${(owntp in ['surveyConfig']) ? ownobj?.id : params.id}"/>
            <g:hiddenField id="${preID}_linkto" name="linkto" value="${owntp}"/>
        </g:if>

        <div class="field required">
            <label for="${preID}_title">
                <g:message code="default.title.label" /> <g:message code="messageRequiredField" />
            </label>
            <g:textField id="${preID}_title" name="title" value="" required=""/>
        </div>

        <div class="field">
            <label for="${preID}_description">
                <g:message code="default.description.label" />
            </label>
            <g:textArea id="${preID}_description" name="description" rows="5" cols="40" value="" />
        </div>

        <g:if test="${controllerName == 'myInstitution' || controllerName == 'ajaxHtml'}">
            <div class="field required">
                <fieldset>
                    <legend>
                        <g:message code="task.typ" />
                    </legend>
                    <div class="ui radio checkbox">
                        <input id="${preID}_generalradio" type="radio" value="general" name="linkto" tabindex="0" class="hidden" checked="checked">
                        <label for="${preID}_generalradio">${message(code: 'task.general')}</label>
                    </div>
                    &nbsp; &nbsp;
                    <div class="ui radio checkbox">
                        <input id="${preID}_licenseradio" type="radio" value="license" name="linkto" tabindex="0" class="hidden">
                        <label for="${preID}_licenseradio">
                            <g:message code="license.label" />
                        </label>
                    </div>
                    &nbsp; &nbsp;
                    <div class="ui radio checkbox">
                        <input id="${preID}_subscriptionradio" type="radio" value="subscription" name="linkto" tabindex="0" class="hidden">
                        <label for="${preID}_subscriptionradio">
                            <g:message code="default.subscription.label" />
                        </label>
                    </div>
                    &nbsp; &nbsp;
                    <div class="ui radio checkbox">
                        <input id="${preID}_orgradio" type="radio" value="org" name="linkto" tabindex="0" class="hidden">
                        <label for="${preID}_orgradio">
                            <g:message code="task.org.label" />
                        </label>
                    </div>
                    &nbsp; &nbsp;
                    <div class="ui radio checkbox">
                        <input id="${preID}_providerradio" type="radio" value="provider" name="linkto" tabindex="0" class="hidden">
                        <label for="${preID}_providerradio">
                            <g:message code="task.provider.label" />
                        </label>
                    </div>
                    &nbsp; &nbsp;
                    <div class="ui radio checkbox">
                        <input id="${preID}_vendorradio" type="radio" value="vendor" name="linkto" tabindex="0" class="hidden">
                        <label for="${preID}_vendorradio">
                            <g:message code="task.vendor.label" />
                        </label>
                    </div>
                </fieldset>
            </div>

            <div id="${preID}_licensediv" class="field required" style="display: none">
                <label for="${preID}_license">
                    <g:message code="task.linkto" /> <g:message code="license.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select class="ui dropdown clearable search"
                          id="${preID}_license"
                          name="license"
                          from="${validLicensesDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_orgdiv" class="field required" style="display: none">
            <label for="${preID}_org">
                <g:message code="task.linkto" /> <g:message code="task.org.label" /> <g:message code="messageRequiredField" />
            </label>
                <g:select id="${preID}_org"
                          name="org"
                          from="${validOrgsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown clearable search"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_providerdiv" class="field required" style="display: none">
                <label for="${preID}_provider">
                    <g:message code="task.linkto" /> <g:message code="task.provider.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select id="${preID}_provider"
                          name="provider"
                          from="${validProvidersDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown clearable search"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_vendordiv" class="field required" style="display: none">
                <label for="${preID}_vendor">
                    <g:message code="task.linkto" /> <g:message code="task.vendor.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select id="${preID}_vendor"
                          name="vendor"
                          from="${validVendorsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown clearable search"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_subscriptiondiv" class="field required" style="display: none">
                <label for="${preID}_subscription">
                    <g:message code="task.linkto" /> <g:message code="default.subscription.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select class="ui dropdown clearable search"
                          id="${preID}_subscription"
                          name="subscription"
                          from="${validSubscriptionsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>
        </g:if>

        <div class="field">
            <div class="two fields">
                <div class="field wide eight">
                    <label for="${preID}_status">
                        <g:message code="task.status.label" />
                    </label>
                    <ui:select id="${preID}_status" name="status.id"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  optionValue="value" optionKey="id" required=""
                                  value="${RDStore.TASK_STATUS_OPEN.id}"
                                  class="ui dropdown la-not-clearable"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>

                <ui:datepicker class="wide eight" label="task.endDate.label" id="${preID}_endDate" name="endDate"
                                  placeholder="default.date.label" value="" required="" />
            </div>
        </div>

        <div class="field" id="${preID}_radioGroup">
            <span style="margin-bottom: 4px">
                <strong><g:message code="task.responsible.label" /></strong>
            </span>
            <div class="two fields">
                <div class="field wide eight required">
                    <fieldset>
                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="${preID}_radioresponsibleOrg" type="radio" value="Org" name="responsible" tabindex="0" class="hidden">
                                <label for="${preID}_radioresponsibleOrg">${message(code: 'task.responsibleOrg.label')} <strong>${contextService.getOrg().getDesignation()}</strong> </label>
                            </div>
                        </div>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="${preID}_radioresponsibleUser" type="radio" value="User" name="responsible" tabindex="0" class="hidden">
                                <label for="${preID}_radioresponsibleUser">${message(code: 'task.responsibleUser.label')}</label>
                            </div>
                        </div>
                    </fieldset>
                </div>

                <div id="${preID}_responsibleUserWrapper" class="field wide eight required">
                    <label for="${preID}_responsibleUserInput">
                        <g:message code="task.responsibleUser.label" /> <g:message code="messageRequiredField" />
                    </label>
                    <g:select id="${preID}_responsibleUserInput"
                              name="responsibleUser.id"
                              from="${taskService.getUserDropdown()}"
                              optionKey="id"
                              optionValue="display"
                              value="${contextService.getUser().id}"
                              class="ui dropdown search la-not-clearable"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>
            </div>
        </div>

    </g:form>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">

        JSPC.callbacks.modal.onShow.modalCreateTask = function (trigger) {
            let preID = '#' + $('#modalCreateTask form input[name=preID]').val()
%{--            console.log ( 'modalCreateTask / preID: ' + preID )--}%
%{--            console.log ( trigger )--}%

            r2d2.helper.resetModalForm ('#modalCreateTask');

            let $radRespOrg         = $(preID + '_radioresponsibleOrg')
            let $radRespUser        = $(preID + '_radioresponsibleUser')
            let $respUserWrapper    = $(preID + '_responsibleUserWrapper')

            let func_toggleResponsibleUser = function () {
                if ($radRespUser.is(':checked')) {
                    $respUserWrapper.show()
                } else {
                    $respUserWrapper.hide()
                }
            }

            $radRespOrg.change(function ()  { func_toggleResponsibleUser() });
            $radRespUser.change(function () { func_toggleResponsibleUser() });

            $(preID + '_form').form({
                inline: true,
                fields: {
                    title: {
                        identifier: preID.replace('#', '') + '_title',
                        rules: [{
                                type: 'empty',
                                prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                        }]
                    },
                    endDate: {
                        identifier: preID.replace('#', '') + '_endDate',
                        rules: [{
                                type: 'empty',
                                prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                        }]
                    },
                    responsible: {
                        rules: [{
                                type: 'checked',
                                prompt: '<g:message code="validation.needsToBeFilledOut" />'
                        }]
                    }
                }
            });

            $radRespUser.prop ('checked', true);
            func_toggleResponsibleUser();

            <g:if test="${controllerName == 'myInstitution' || controllerName == 'ajaxHtml'}">

                JSPC.app.taskTypes = ['general', 'license', 'org', 'provider', 'subscription', 'vendor']

                $.each(JSPC.app.taskTypes, function(i, taskType) {

                    $(preID + '_' + taskType + 'radio').change(function () {
                        let toHide = JSPC.app.taskTypes.map( function(tt) { return preID + '_' + tt + 'div'; } ).join(', ');
                        $(toHide).hide();

                        $(preID + '_' + taskType + 'div').show();

                        if (taskType != 'general') {
                            $(preID + '_form').form({
                                on: 'blur',
                                inline: true,
                                fields: {
                                    taskType : {
                                        identifier: preID.replace('#', '') + '_' + taskType,
                                        rules: [{
                                                type: 'empty',
                                                prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                                        }]
                                    }
                                }
                            });
                        }
                    });
                });
            </g:if>
        };
</laser:script>


