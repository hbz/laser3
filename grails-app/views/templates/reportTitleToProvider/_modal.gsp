<%@ page import="de.laser.utils.RandomUtils; de.laser.ui.Icon; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.storage.BeanStore; de.laser.Task; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

    <ui:modal id="modalCreateRttpTask" message="task.create.reportTitleToProvider">

        <g:set var="preID" value="${RandomUtils.getHtmlID()}" />

        <g:form id="${preID}_form" class="ui form" url="[controller: 'task', action: 'createTask']" method="post">
            <g:hiddenField id="${preID}_preID" name="preID" value="${preID}" />
            <g:hiddenField id="${preID}_tipp" name="tipp" value="${tipp?.id}"/>%{-- only _flyoutAndTippTask.gsp --}%
            <g:hiddenField id="${preID}_linkto" name="linkto" value="tipp"/>

            <div class="field required">
                <label for="${preID}_title">
                    <g:message code="default.title.label" /> <g:message code="messageRequiredField" />
                </label>
                <g:textField id="${preID}_title" name="title" value="${message(code: 'task.create.reportTitleToProvider.title')}" required=""/>
            </div>

            <div class="field">
                <label for="${preID}_description">
                    <g:message code="default.description.label" />
                </label>
                <g:textArea id="${preID}_description" name="description" value="${message(code: 'task.create.reportTitleToProvider.desc')}" rows="5" cols="40" />
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
        JSPC.callbacks.modal.onShow.modalCreateRttpTask = function (trigger) {
            let preID = '#' + $('#modalCreateRttpTask form input[name=preID]').val()
%{--            console.log ( 'modalCreateRttpTask / preID: ' + preID )--}%
%{--            console.log ( trigger )--}%

        <g:if test="${tipp}">%{-- only _flyoutAndTippTask.gsp --}%
        </g:if>
        <g:else>
            $(preID + '_tipp').val($(trigger).attr('data-tipp'));
%{--            $(preID + '_title').val("${message(code: 'task.create.reportTitleToProvider.title')}");--}%
%{--            $(preID + '_description').val("${message(code: 'task.create.reportTitleToProvider.desc')}");--}%
        </g:else>

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

            $radRespOrg.prop ('checked', true);
            func_toggleResponsibleUser();
        };
    </laser:script>

