<%@ page import="java.sql.Timestamp; de.laser.Org; de.laser.License; de.laser.Subscription; de.laser.Task; de.laser.storage.RDStore;de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<laser:serviceInjection />
<ui:modal id="modalCreateTask" message="task.create.new">

    <g:form class="ui form" id="create_task" url="[controller: 'task', action: 'create']" method="post">
        <g:if test="${controllerName != 'myInstitution' && controllerName != 'ajaxHtml'}">
            <g:hiddenField name="${owntp}" value="${(owntp == 'surveyConfig') ? ownobj?.id : params.id}"/>
            <g:hiddenField name="linkto" value="${owntp}"/>
        </g:if>

        <div class="field ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
            <label for="title">
                <g:message code="default.title.label" /> <g:message code="messageRequiredField" />
            </label>
            <g:textField id="title" name="title" required="" value="${taskInstance?.title}"/>
        </div>

        <div class="field ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
            <label for="description">
                <g:message code="default.description.label" />
            </label>
            <g:textArea name="description" value="${taskInstance?.description}" rows="5" cols="40"/>
        </div>

        <g:if test="${controllerName == 'myInstitution' || controllerName == 'ajaxHtml'}">
            <div class="field required">
                <fieldset>
                    <legend>
                        <g:message code="task.typ" /> <g:message code="messageRequiredField" />
                    </legend>
                    <div class="ui radio checkbox">
                        <input id="generalradio" type="radio" value="general" name="linkto" tabindex="0" class="hidden" checked="">
                        <label for="generalradio">${message(code: 'task.general')}</label>
                    </div>
                    &nbsp; &nbsp;
                    <div class="ui radio checkbox">
                        <input id="licenseradio" type="radio" value="license" name="linkto" tabindex="0" class="hidden">
                        <label for="licenseradio">
                            <g:message code="license.label" />
                        </label>
                    </div>
                    &nbsp; &nbsp;
                    %{--<div class="ui radio checkbox">
                        <input id="pkgradio" type="radio" value="pkg" name="linkto" tabindex="0" class="hidden">
                        <label for="pkgradio">
                            <g:message code="package.label" />
                        </label>
                    </div>
                    &nbsp; &nbsp;--}%
                    <div class="ui radio checkbox">
                        <input id="subscriptionradio" type="radio" value="subscription" name="linkto" tabindex="0" class="hidden">
                        <label for="subscriptionradio">
                            <g:message code="default.subscription.label" />
                        </label>
                    </div>
                    &nbsp; &nbsp;
                    <div class="ui radio checkbox">
                        <input id="orgradio" type="radio" value="org" name="linkto" tabindex="0" class="hidden">
                        <label for="orgradio">
                            <g:message code="task.org.label" />
                        </label>
                    </div>
                </fieldset>
            </div>

            <div id="licensediv"
                 class="field ${hasErrors(bean: taskInstance, field: 'license', 'error')} required">
                <label for="license">
                    <g:message code="task.linkto" /><g:message code="license.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select class="ui dropdown search many-to-one"
                          id="license"
                          name="license"
                          from="${validLicensesDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="orgdiv" class="field ${hasErrors(bean: taskInstance, field: 'org', 'error')} required">
            <label for="org">
                <g:message code="task.linkto" /><g:message code="task.org.label" /> <g:message code="messageRequiredField" />
            </label>
                <g:select id="org"
                          name="org"
                          from="${validOrgsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown search many-to-one"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="pkgdiv" class="field ${hasErrors(bean: taskInstance, field: 'pkg', 'error')} required">
                <label for="pkg">
                    <g:message code="task.linkto" /><g:message code="package.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select id="pkg" name="pkg" from="${validPackages}" optionKey="id" value="${ownobj?.id}"
                          class="ui dropdown search many-to-one"
                          required=""
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>

            <div id="subscriptiondiv"
                 class="field ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} required">
                <label for="subscription">
                    <g:message code="task.linkto" /><g:message code="default.subscription.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:select class="ui dropdown search many-to-one"
                          id="subscription"
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
                    <label for="status">
                        <g:message code="task.status.label" /> <g:message code="messageRequiredField" />
                    </label>
                    <ui:select id="status" name="status.id"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  optionValue="value" optionKey="id" required=""
                                  value="${taskInstance?.status?.id ?: RDStore.TASK_STATUS_OPEN.id}"
                                  class="ui dropdown search many-to-one"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>

                <ui:datepicker class="wide eight" label="task.endDate.label" id="endDate" name="endDate"
                                  placeholder="default.date.label" value="${taskInstance?.endDate}" required=""
                                  bean="${taskInstance}"/>
            </div>
        </div>

        <div class="field" id="radioGroup">
            <label for="radioGroup">
                <g:message code="task.responsible.label" />
            </label>
            <div class="two fields">
                <div class="field wide eight ${hasErrors(bean: taskInstance, field: 'responsible', 'error')} required">
                    <fieldset>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="radioresponsibleOrg" type="radio" value="Org" name="responsible" tabindex="0" class="hidden" checked="">
                                <label for="radioresponsibleOrg">${message(code: 'task.responsibleOrg.label')} <strong>${contextOrg.getDesignation()}</strong> </label>
                            </div>
                        </div>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="radioresponsibleUser" type="radio" value="User" name="responsible" tabindex="0" class="hidden">
                                <label for="radioresponsibleUser">${message(code: 'task.responsibleUser.label')}</label>
                            </div>
                        </div>
                    </fieldset>
                </div>

                <div id="responsibleUserWrapper"
                     class="field wide eight ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')} required">
                    <label for="responsibleUserInput">
                        <g:message code="task.responsibleUser.label" />
                    </label>
                    <g:select id="responsibleUserInput"
                              name="responsibleUser.id"
                              from="${taskService.getUserDropdown(contextOrg)}"
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
            // initial side call
            $("#generalradio").prop( "checked", true );
            $("#licensediv, #orgdiv, #pkgdiv, #subscriptiondiv").hide();

            JSPC.app.showHideRequire = function (taskType) {
                var arr = [ 'license', 'org', 'pkg', 'subscription' ];
                $('#'+ taskType +'radio').change(function () {

                    var hideArray = arr.filter(function(val, index, arr) {
                        return val != taskType;
                    });
                    var hide = hideArray.map(function(val, index, arr) {
                        return '#' + val + 'div';
                    }).join(", ");

                    $(hide).hide();
                    $('#' + taskType + 'div').show();
                    JSPC.app.chooseRequiredDropdown(taskType);
                });
            }

            JSPC.app.showHideRequire ( 'general' );
            JSPC.app.showHideRequire ( 'license' );
            JSPC.app.showHideRequire ( 'pkg' );
            JSPC.app.showHideRequire ( 'subscription' );
            JSPC.app.showHideRequire ( 'org' );
        </laser:script>
    </g:if>

    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.callbacks.modal.show.modalCreateTask = function (trigger) {
            r2d2.resetModalForm ('#modalCreateTask');

            $('#modalCreateTask #radioresponsibleOrg').prop ('checked', true);
            JSPC.app.toggleResponsibleUser();

            // myInstitution
            $('#generalradio').prop ('checked', true);
            $("#licensediv, #orgdiv, #pkgdiv, #subscriptiondiv").hide();
        };

        $("#radioresponsibleOrg").change(function () { JSPC.app.toggleResponsibleUser() });
        $("#radioresponsibleUser").change(function () { JSPC.app.toggleResponsibleUser() });

        JSPC.app.toggleResponsibleUser = function () {
            if ($("#radioresponsibleUser").is(':checked')) {
                $("#responsibleUserWrapper").show()
            } else {
                $("#responsibleUserWrapper").hide()
            }
        }

        JSPC.app.toggleResponsibleUser();

        JSPC.app.chooseRequiredDropdown = function (opt) {
            $(document).ready(function () {

                $.fn.form.settings.rules.responsibleUserInput = function() {
                    if($("#radioresponsibleUser").is(":checked")) {
                        return $('#responsibleUserInput').val()
                    }
                    else return true
                }
                $('#create_task').form({
                        inline: true,
                        fields: {
                            title: {
                                identifier: 'title',
                                rules: [
                                    {
                                        type: 'empty',
                                        prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            },
                            endDate: {
                                identifier: 'endDate',
                                rules: [
                                    {
                                        type: 'empty',
                                        prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            },
                            opt: {
                                identifier: opt,
                                rules: [
                                    {
                                        type: 'empty',
                                        prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            },
                            responsible: {
                                rules: [
                                    {
                                        type: 'checked',
                                        prompt: '<g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            },
                            responsibleUserInput: {
                                identifier: 'responsibleUserInput',
                                rules: [
                                    {
                                        type: 'responsibleUserInput',
                                        prompt: '<g:message code="validation.responsibleMustBeChecked" />'
                                    }
                                ]
                            }
                        }
                    });
            })
        }
        JSPC.app.chooseRequiredDropdown('status.id');
    </laser:script>

</ui:modal>
