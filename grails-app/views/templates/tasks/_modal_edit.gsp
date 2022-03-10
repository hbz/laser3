<%@ page import="de.laser.helper.RDStore; de.laser.Task;de.laser.helper.RDConstants; de.laser.RefdataCategory" %>
<laser:serviceInjection />

<semui:modal id="modalEditTask" message="task.edit" isEditModal="true" >

    <g:form class="ui form" id="edit_task" url="[controller:'task',action:'edit',id:taskInstance?.id]" method="post">
        <g:hiddenField name="version" value="${taskInstance?.version}" />
        <div class="field ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
            <label for="title">
                <g:message code="task.title.label"/>
            </label>
            <g:textField name="title" required="" value="${taskInstance?.title}"/>
        </div>

        <div class="field ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
            <label for="description">
                <g:message code="task.description.label"/>
            </label>
            <g:textArea name="description" value="${taskInstance?.description}" rows="5" cols="40"/>
        </div>

        <div class="field ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
            <strong>Betrifft:</strong>
            <g:if test="${taskInstance.getObjects()}">
                <g:each in="${taskInstance.getObjects()}" var="tskObj">
                    <br />
                    - ${message(code: 'task.' + tskObj.controller)}:
                    <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object?.id]}">${tskObj.object}</g:link>
                </g:each>
            </g:if>
            <g:else>
                <br />
                - ${message(code: 'task.general')}
            </g:else>
        </div>

        <div class="field">
            <div class="two fields">

                <div class="field wide eight ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
                    <label for="status">
                        <g:message code="task.status.label" />
                    </label>
                    <laser:select id="status" name="status.id" from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  optionValue="value" optionKey="id" required=""
                                  value="${taskInstance?.status?.id ?: RDStore.TASK_STATUS_OPEN.id}"
                                  class="ui dropdown search many-to-one"/>
                </div>

                <semui:datepicker class="wide eight" label="task.endDate.label" id="endDate" name="endDate" placeholder="default.date.label" value="${formatDate(format:message(code:'default.date.format.notime'), date:taskInstance?.endDate)}" required="true" bean="${taskInstance}" />

            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field wide eight ${hasErrors(bean: taskInstance, field: 'responsible', 'error')}">
                    <fieldset>
                        <legend>
                            <g:message code="task.responsible.label" />
                        </legend>
                        <g:if test="${taskInstance?.responsibleOrg?.id}"><g:set var="checked" value="checked" /></g:if><g:else> <g:set var="checked" value="" /></g:else>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="radioresponsibleOrgEdit" type="radio" value="Org" name="responsible" tabindex="0" class="hidden" ${checked}>
                                <label for="radioresponsibleOrgEdit">${message(code: 'task.responsibleOrg.label')} <strong>${contextService?.org?.getDesignation()}</strong></label>
                            </div>
                        </div>
                        <g:if test="${taskInstance?.responsibleUser?.id}"><g:set var="checked" value="checked" /></g:if><g:else> <g:set var="checked" value="" /></g:else>
                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="radioresponsibleUserEdit" type="radio" value="User" name="responsible" tabindex="0" class="hidden" ${checked}>
                                <label for="radioresponsibleUserEdit">${message(code: 'task.responsibleUser.label')}</label>
                            </div>
                        </div>
                    </fieldset>
                </div>
                <div id="responsibleUserEdit"
                     class="field wide eight ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')}">
                    <label for="responsibleUser">
                        <g:message code="task.responsibleUser.label" />
                    </label>
                    <g:select id="responsibleUser"
                              name="responsibleUser.id"
                              from="${validResponsibleUsers}"
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


    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.callbacks.dynPostFunc = function () {
            console.log('dynPostFunc @ tasks/_modal_edit.gsp');

        $("#radioresponsibleOrgEdit").change(function () {
            JSPC.app.toggleResponsibleUserEdit();
        });
        $("#radioresponsibleUserEdit").change(function () {
            JSPC.app.toggleResponsibleUserEdit();
        });

        JSPC.app.toggleResponsibleUserEdit = function () {
            if ($("#radioresponsibleUserEdit").is(':checked')) {
                $("#responsibleUserEdit").show();
            } else {
                $("#responsibleUserEdit").hide();
            }
        }

        JSPC.app.toggleResponsibleUserEdit();

            $.fn.form.settings.rules.responsibleUser = function() {
                    if($("#radioresponsibleUser").is(":checked")) {
                        return $('#responsibleUserInput').val();
                    }
                    else return true;
                }

            $('#edit_task')
                .form({
                    on: 'blur',
                    inline: true,
                    fields: {
                        title: {
                            identifier: 'title',
                            rules: [
                                {
                                    type: 'empty',
                                    prompt: '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                                }
                            ]
                        },

                        endDate: {
                            identifier: 'endDate',
                            rules: [
                                {
                                    type: 'empty',
                                    prompt: '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
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
                            responsibleUser: {
                                identifier: 'responsibleUser',
                                rules: [
                                    {
                                        type: 'responsibleUser',
                                        prompt: '<g:message code="validation.responsibleMustBeChecked" />'
                                    }
                                ]
                            }
                    }
                });
        }
    </laser:script>
</semui:modal>

