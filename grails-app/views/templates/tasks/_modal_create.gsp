<%@ page import="de.laser.utils.SwissKnife; java.sql.Timestamp; de.laser.Org; de.laser.License; de.laser.Subscription; de.laser.Task; de.laser.storage.RDStore;de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<laser:serviceInjection />

<ui:modal id="modalCreateTask" message="task.create.new">

    <g:set var="preID" value="${SwissKnife.getRandomID()}" />

    <g:form id="${preID}_form" class="ui form" url="[controller: 'task', action: 'createTask']" method="post">
        <g:hiddenField id="${preID}_preID" name="preID" value="${preID}" />

        <g:if test="${controllerName != 'myInstitution' && controllerName != 'ajaxHtml'}">
            <g:hiddenField id="${preID}_${owntp}" name="${owntp}" value="${(owntp in ['surveyConfig']) ? ownobj?.id : params.id}"/>
            <g:hiddenField id="${preID}_linkto" name="linkto" value="${owntp}"/>
        </g:if>

        <div class="field ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
            <label for="${preID}_title">
                <g:message code="default.title.label" /> <g:message code="messageRequiredField" />
            </label>
            <g:textField id="${preID}_title" name="title" required="" value="${taskInstance?.title}"/>
        </div>

        <div class="field ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
            <label for="${preID}_description">
                <g:message code="default.description.label" />
            </label>
            <g:textArea id="${preID}_description" name="description" value="${taskInstance?.description}" rows="5" cols="40"/>
        </div>

        <g:if test="${controllerName == 'myInstitution' || controllerName == 'ajaxHtml'}">
            <div class="field required">
                <fieldset>
                    <legend>
                        <g:message code="task.typ" /> <g:message code="messageRequiredField" />
                    </legend>
                    <div class="ui radio checkbox">
                        <input id="${preID}_generalradio" type="radio" value="general" name="linkto" tabindex="0" class="hidden" checked="">
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

            <div id="${preID}_licensediv" class="field ${hasErrors(bean: taskInstance, field: 'license', 'error')} required">
                <label for="${preID}_license">
                    <g:message code="task.linkto" /> <g:message code="license.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select class="ui dropdown search many-to-one"
                          id="${preID}_license"
                          name="license"
                          from="${validLicensesDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_orgdiv" class="field ${hasErrors(bean: taskInstance, field: 'org', 'error')} required">
            <label for="${preID}_org">
                <g:message code="task.linkto" /> <g:message code="task.org.label" /> <g:message code="messageRequiredField" />
            </label>
                <g:select id="${preID}_org"
                          name="org"
                          from="${validOrgsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown search many-to-one"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_providerdiv" class="field ${hasErrors(bean: taskInstance, field: 'provider', 'error')} required">
            <label for="${preID}_provider">
                <g:message code="task.linkto" /> <g:message code="task.provider.label" /> <g:message code="messageRequiredField" />
            </label>
                <g:select id="${preID}_provider"
                          name="provider"
                          from="${validProvidersDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown search many-to-one"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_vendordiv" class="field ${hasErrors(bean: taskInstance, field: 'vendor', 'error')} required">
            <label for="${preID}_vendor">
                <g:message code="task.linkto" /> <g:message code="task.vendor.label" /> <g:message code="messageRequiredField" />
            </label>
                <g:select id="${preID}_vendor"
                          name="vendor"
                          from="${validVendorsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown search many-to-one"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="${preID}_subscriptiondiv" class="field ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} required">
                <label for="${preID}_subscription">
                    <g:message code="task.linkto" /> <g:message code="default.subscription.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select class="ui dropdown search many-to-one"
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
                <div class="field wide eight ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
                    <label for="${preID}_status">
                        <g:message code="task.status.label" /> <g:message code="messageRequiredField" />
                    </label>
                    <ui:select id="${preID}_status" name="status.id"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  optionValue="value" optionKey="id" required=""
                                  value="${taskInstance?.status?.id ?: RDStore.TASK_STATUS_OPEN.id}"
                                  class="ui dropdown many-to-one la-not-clearable"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>

                <ui:datepicker class="wide eight" label="task.endDate.label" id="${preID}_endDate" name="endDate"
                                  placeholder="default.date.label" value="${taskInstance?.endDate}" required=""
                                  bean="${taskInstance}"/>
            </div>
        </div>

        <div class="field" id="radioGroup">
            <span style="margin-bottom: 4px"><strong>
                <g:message code="task.responsible.label" />
            </strong></span>
            <div class="two fields">
                <div class="field wide eight ${hasErrors(bean: taskInstance, field: 'responsible', 'error')} required">
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

                <div id="${preID}_responsibleUserWrapper"
                     class="field wide eight ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')} required">
                    <label for="${preID}_responsibleUserInput">
                        <g:message code="task.responsibleUser.label" />
                    </label>
                    <g:select id="${preID}_responsibleUserInput"
                              name="responsibleUser.id"
                              from="${taskService.getUserDropdown()}"
                              optionKey="id"
                              optionValue="display"
                              value="${taskInstance?.responsibleUser?.id}"
                              class="ui dropdown search many-to-one"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>
            </div>
        </div>

    </g:form>
    <g:if test="${controllerName == 'myInstitution' || controllerName == 'ajaxHtml'}">
        <laser:script file="${this.getGroovyPageFileName()}">
            $('#${preID}_generalradio').prop( 'checked', true );
            $('#${preID}_licensediv, #${preID}_orgdiv, #${preID}_subscriptiondiv').hide();

            JSPC.app.showHideRequire = function (taskType) {
                var arr = [ 'license', 'org', 'provider', 'vendor', 'subscription' ];
                $('#${preID}_' + taskType + 'radio').change(function () {

                    var hideArray = arr.filter(function(val, index, arr) {
                        return val != taskType;
                    });
                    var hide = hideArray.map(function(val, index, arr) {
                        return '#${preID}_' + val + 'div';
                    }).join(', ');

                    $(hide).hide();
                    $('#${preID}_' + taskType + 'div').show();
                    JSPC.app.chooseRequiredDropdown(taskType);
                });
            }

            JSPC.app.showHideRequire ( 'general' );
            JSPC.app.showHideRequire ( 'license' );
            JSPC.app.showHideRequire ( 'subscription' );
            JSPC.app.showHideRequire ( 'org' );
            JSPC.app.showHideRequire ( 'provider' );
            JSPC.app.showHideRequire ( 'vendor' );
        </laser:script>
    </g:if>

    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.callbacks.modal.onShow.modalCreateTask = function (trigger) {
            let preID = '#' + $('#modalCreateTask form input[name=preID]').val()
            console.log ( 'modalCreateTask / preID: ' + preID )

            r2d2.helper.resetModalForm ('#modalCreateTask');

            $('#${preID}_radioresponsibleUser').prop ('checked', true);
            JSPC.app.toggleResponsibleUser();

            // myInstitution
            $('#${preID}_generalradio').prop ('checked', true);
            $('#${preID}_licensediv, #${preID}_orgdiv, #${preID}_subscriptiondiv, #${preID}_providerdiv, #${preID}_vendordiv').hide();
        };

        $('#${preID}_radioresponsibleOrg').change(function () { JSPC.app.toggleResponsibleUser() });
        $('#${preID}_radioresponsibleUser').change(function () { JSPC.app.toggleResponsibleUser() });

        JSPC.app.toggleResponsibleUser = function () {
            if ($('#${preID}_radioresponsibleUser').is(':checked')) {
                $('#${preID}_responsibleUserWrapper').show()
            } else {
                $('#${preID}_responsibleUserWrapper').hide()
            }
        }

        JSPC.app.toggleResponsibleUser();

        JSPC.app.chooseRequiredDropdown = function (opt) {
            $(document).ready(function () {

                $.fn.form.settings.rules.responsibleUserInput = function() {
                    if($('#${preID}_radioresponsibleUser').is(':checked')) {
                        return $('#${preID}_responsibleUserInput').val()
                    }
                    else return true
                }
                $('#${preID}_form').form({
                        inline: true,
                        fields: {
                            title: {
                                identifier: '${preID}_title',
                                rules: [{
                                        type: 'empty',
                                        prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                                }]
                            },
                            endDate: {
                                identifier: '${preID}_endDate',
                                rules: [{
                                        type: 'empty',
                                        prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                                }]
                            },
                            opt: {
                                identifier: '${preID}_' + opt,
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
                            },
                            responsibleUserInput: {
                                identifier: '${preID}_responsibleUserInput',
                                rules: [{
                                        type: 'responsibleUserInput',
                                        prompt: '<g:message code="validation.responsibleMustBeChecked" />'
                                }]
                            }
                        }
                    });
            })
        }

        JSPC.app.chooseRequiredDropdown('status'); // todo remove, but init
    </laser:script>

</ui:modal>
