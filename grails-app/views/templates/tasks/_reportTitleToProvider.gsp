<%@ page import="de.laser.ui.Icon; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.storage.BeanStore; de.laser.Task; de.laser.remote.ApiSource; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor()}">

    <g:if test="${tipp.platform?.provider}">
        <g:set var="currentTasks" value="${taskService.getTasks(BeanStore.getContextService().getUser(), tipp)}" />

        <g:if test="${contextService.getOrg().isCustomerType_Basic()}">

            <ui:msg class="info" showIcon="true">
                ${message(code:'tipp.reportTitleToProvider.info1')}
                <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
                <br />
                ${message(code:'tipp.reportTitleToProvider.proHint')}
            </ui:msg>

        </g:if>%{-- BASIC --}%
        <g:elseif test="${contextService.getOrg().isCustomerType_Pro()}">

            <ui:msg class="info" showIcon="true">
                ${message(code:'tipp.reportTitleToProvider.info1')}
                <br />
                <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
                und <a href="#modalCreateTask" data-ui="modal">erstellen Sie sich ggf. eine Aufgabe</a> zur Erinnerung.
                <br />

                <g:if test="${currentTasks.cmbTaskInstanceList}">
                        <br />
                        ${message(code:'tipp.reportTitleToProvider.info2')} <br />

                        <g:each in="${currentTasks.myTaskInstanceList.sort{ it.dateCreated }.reverse()}" var="tt">
                            <i class="${Icon.TASK}"></i>
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tt.dateCreated}"/> -
                            <a href="#" onclick="JSPC.app.editTask(${tt.id});">${tt.title}</a> <br />
                        </g:each>
                </g:if>
            </ui:msg>

        </g:elseif>%{-- PRO --}%

    </g:if>

    <g:if test="${contextService.getOrg().isCustomerType_Pro()}">

    <ui:modal id="modalCreateTask" message="task.create.reportTitleToProvider">

        <g:form class="ui form" id="create_task" url="[controller: 'task', action: 'createTask']" method="post">
            <g:hiddenField name="tipp" value="${tipp.id}"/>
            <g:hiddenField name="linkto" value="tipp"/>

            <div class="field required">
                <label for="title">
                    <g:message code="default.title.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:textField id="title" name="title" required="" value="${message(code: 'task.create.reportTitleToProvider.title')}"/>
            </div>

            <div class="field">
                <label for="description">
                    <g:message code="default.description.label" />
                </label>
                <g:textArea name="description" value="${message(code: 'task.create.reportTitleToProvider.desc')}" rows="5" cols="40"/>
            </div>

            <div class="field">
                <div class="two fields">

                    <div class="field wide eight required">
                        <label for="status">
                            <g:message code="task.status.label" /> <g:message code="messageRequiredField" />
                        </label>
                        <ui:select id="status" name="status.id"
                                   from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                   optionValue="value" optionKey="id" required=""
                                   value="${RDStore.TASK_STATUS_OPEN.id}"
                                   class="ui dropdown search many-to-one"
                                   noSelection="${['' : message(code:'default.select.choose.label')]}"
                        />
                    </div>

                    <ui:datepicker class="wide eight" label="task.endDate.label" id="endDate" name="endDate"
                                   placeholder="default.date.label" value="" required="" />
                </div>
            </div>

            <div class="field" id="radioGroup">
                <span style="margin-bottom: 4px">
                    <strong><g:message code="task.responsible.label" /></strong>
                </span>
                <div class="two fields">
                    <div class="field wide eight required">
                        <fieldset>
                            <div class="field">
                                <div class="ui radio checkbox">
                                    <input id="radioresponsibleOrg" type="radio" value="Org" name="responsible" tabindex="0" class="hidden">
                                    <label for="radioresponsibleOrg">${message(code: 'task.responsibleOrg.label')} <strong>${contextOrg.getDesignation()}</strong></label>
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

                    <div id="responsibleUserWrapper" class="field wide eight required">
                        <label for="responsibleUserInput">
                            <g:message code="task.responsibleUser.label" />
                        </label>
                        <g:select id="responsibleUserInput"
                                  name="responsibleUser.id"
                                  from="${taskService.getUserDropdown(contextOrg)}"
                                  optionKey="id"
                                  optionValue="display"
                                  value="${contextService.getUser().id}"
                                  class="ui dropdown search many-to-one"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                        />
                    </div>
                </div>
            </div>

        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">

            JSPC.app.editTask = function (id) {
                var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id);
                func();
            }

            JSPC.callbacks.modal.onShow.modalCreateTask = function (trigger) {
                /* r2d2.helper.resetModalForm ('#modalCreateTask'); */

                $('#modalCreateTask #radioresponsibleUser').prop ('checked', true);
                JSPC.app.toggleResponsibleUser();
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

    </g:if>%{-- PRO --}%

    <laser:render template="/info/flyoutWrapper"/>
</g:if>