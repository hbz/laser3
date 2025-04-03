<%@ page import="de.laser.utils.RandomUtils; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.Task; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<laser:serviceInjection />

<ui:modal id="modalEditTask" message="task.edit" isEditModal="true" >

    <g:set var="preID" value="${RandomUtils.getHtmlID()}" />

    <g:form id="${preID}_form" class="ui form" url="[controller:'task', action:'editTask', id:taskInstance.id]" method="post">
        <g:hiddenField id="${preID}_preID" name="preID" value="${preID}" />
        <g:hiddenField id="${preID}_version" name="version" value="${taskInstance.version}" />

        <div class="field required">
            <label for="${preID}_title">
                <g:message code="default.title.label"/> <g:message code="messageRequiredField" />
            </label>
            <g:textField id="${preID}_title" name="title" required="" value="${taskInstance.title}"/>
        </div>

        <div class="field">
            <label for="${preID}_description">
                <g:message code="default.description.label"/>
            </label>
            <g:textArea id="${preID}_description" name="description" value="${taskInstance.description}" rows="5" cols="40"/>
        </div>

        <div class="field">
            <div class="two fields">

                <div class="field wide eight">
                    <label for="${preID}_status">
                        <g:message code="task.status.label" />
                    </label>
                    <ui:select id="${preID}_status" name="status.id"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  optionValue="value" optionKey="id" required=""
                                  value="${taskInstance.status?.id ?: RDStore.TASK_STATUS_OPEN.id}"
                                  class="ui dropdown la-not-clearable"/>
                </div>

                <ui:datepicker class="wide eight" label="task.endDate.label" id="${preID}_endDate" name="endDate"
                               placeholder="default.date.label" value="${formatDate(format:message(code:'default.date.format.notime'), date:taskInstance.endDate)}" required="true"
                               bean="${taskInstance}" />

            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field wide eight">
                    <fieldset>
                        <legend>
                            <g:message code="task.responsible.label" />
                        </legend>
                        <g:if test="${taskInstance.responsibleOrg?.id}"><g:set var="checked" value="checked" /></g:if><g:else> <g:set var="checked" value="" /></g:else>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="${preID}_radioresponsibleOrg" type="radio" value="Org" name="responsible" tabindex="0" class="hidden" ${checked}>
                                <label for="${preID}_radioresponsibleOrg">${message(code: 'task.responsibleOrg.label')} <strong>${contextService.getOrg().getDesignation()}</strong></label>
                            </div>
                        </div>
                        <g:if test="${taskInstance.responsibleUser?.id}"><g:set var="checked" value="checked" /></g:if><g:else> <g:set var="checked" value="" /></g:else>
                        <div class="field">
                            <div class="ui radio checkbox">
                                <input id="${preID}_radioresponsibleUser" type="radio" value="User" name="responsible" tabindex="0" class="hidden" ${checked}>
                                <label for="${preID}_radioresponsibleUser">${message(code: 'task.responsibleUser.label')}</label>
                            </div>
                        </div>
                    </fieldset>
                </div>
                <div id="${preID}_responsibleUserWrapper"
                     class="field wide eight required">
                    <label for="${preID}_responsibleUserInput">
                        <g:message code="task.responsibleUser.label" /> <g:message code="messageRequiredField" />
                    </label>
                    <g:select id="${preID}_responsibleUserInput"
                              name="responsibleUser.id"
                              from="${taskService.getUserDropdown()}"
                              optionKey="id"
                              optionValue="display"
                              value="${taskInstance.responsibleUser?.id}"
                              class="ui dropdown search la-not-clearable"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field">
                    <label>${message(code: 'task.object.label')}</label>
                    <div style="padding:0.5em 0.75em; border:1px dashed lightgrey; border-radius:0.3rem">
                        <g:if test="${taskInstance.getObjectInfo()}">
                            <g:set var="tskObj" value="${taskInstance.getObjectInfo()}" />
                            <div class="la-flexbox">
                                <i class="${tskObj.icon} la-list-icon"></i>
                                <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object.id]}">${tskObj.object}</g:link>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="la-flexbox">${message(code: 'task.general')}</div>
                        </g:else>
                    </div>
                </div>

                <div class="field">
                    <label>
                        <g:message code="task.creator.label"/>
                    </label>
                    <div style="padding:0.5em 0.75em; border:1px dashed lightgrey; border-radius:0.3rem">
                        <div class="la-flexbox">
                            <g:if test="${taskInstance.creator.id == contextService.getUser().id}">
                                <i class="${Icon.SIG.MY_OBJECT} la-list-icon"></i>
                            </g:if>
                            <g:else>
                                <i class="${Icon.ATTR.TASK_CREATOR} la-list-icon"></i>
                            </g:else>
                            ${taskInstance.creator.getDisplayName()}
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </g:form>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">

        JSPC.callbacks.modal.onVisible.modalEditTask = function (trigger) {
            let preID = '#' + $('#modalEditTask form input[name=preID]').val()
%{--            console.log ( 'modalEditTask / preID: ' + preID )--}%
%{--            console.log ( trigger )--}%

            let $radRespOrg         = $(preID + '_radioresponsibleOrg')
            let $radRespUser        = $(preID + '_radioresponsibleUser')
            let $respUserInput      = $(preID + '_responsibleUserInput')
            let $respUserWrapper    = $(preID + '_responsibleUserWrapper')

            let func_toggleResponsibleUser = function () {
                if ($radRespUser.is(':checked')) {
                    $respUserWrapper.show()
                } else {
                    $respUserWrapper.hide()
                }
            }

            $.fn.form.settings.rules.task_responsibleUserInput = function() {
                if ($radRespUser.is(':checked')) {
                    return $respUserInput.val()
                }
                else return true
            }

            $radRespOrg.change(function ()  { func_toggleResponsibleUser() });
            $radRespUser.change(function () { func_toggleResponsibleUser() });

            func_toggleResponsibleUser();

            $('#${preID}_form').form({
                    on: 'blur',
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
                        responsible: {
                            rules: [{
                                    type: 'checked',
                                    prompt: '<g:message code="validation.needsToBeFilledOut" />'
                            }]
                        },
                        responsibleUserInput: {
                            identifier: '${preID}_responsibleUserInput',
                            rules: [{
                                    type: 'task_responsibleUserInput',
                                    prompt: '<g:message code="validation.responsibleMustBeChecked" />'
                            }]
                        }
                    }
            });
        }
</laser:script>

