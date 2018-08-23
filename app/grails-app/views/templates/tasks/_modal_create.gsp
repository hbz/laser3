<%@ page import="com.k_int.kbplus.Task" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<semui:modal id="modalCreateTask" message="task.create.new">

    <g:form class="ui form" id="create_task" url="[controller: 'task', action: 'create']" method="post">
        <g:if test="${controllerName != 'myInstitution'}">
            <g:hiddenField name="${owntp}" value="${params.id}"/>
            <g:hiddenField name="linkto" value="${owntp}"/>
        </g:if>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
            <label for="title">
                <g:message code="task.title.label" default="Title"/>
            </label>
            <g:textField name="title" required="" value="${taskInstance?.title}"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
            <label for="description">
                <g:message code="task.description.label" default="Description"/>
            </label>
            <g:textArea name="description" value="${taskInstance?.description}" rows="5" cols="40"/>
        </div>

        <g:if test="${controllerName == 'myInstitution'}">
            <div class="field fieldcontain required">
                <label for="typ">
                    <g:message code="task.typ" default="Task Typ"/>
                </label>
                <div class="ui radio checkbox">
                    <input id="generalradio" type="radio" value="general" name="linkto" tabindex="0" class="hidden" checked="">
                    <label for="general">${message(code: 'task.general')}</label>
                </div>

                <div class="ui radio checkbox">
                    <input id="licenseradio" type="radio" value="license" name="linkto" tabindex="0" class="hidden">
                    <label for="license">
                        <g:message code="task.license.label" default="License"/>
                    </label>
                </div>

                <div class="ui radio checkbox">
                    <input id="pkgradio" type="radio" value="pkg" name="linkto" tabindex="0" class="hidden">
                    <label for="pkg">
                        <g:message code="task.pkg.label" default="Pkg"/>
                    </label>
                </div>

                <div class="ui radio checkbox">
                    <input id="subscriptionradio" type="radio" value="subscription" name="linkto" tabindex="0" class="hidden">
                    <label for="subscription">
                        <g:message code="task.subscription.label" default="Subscription"/>
                    </label>
                </div>
                <div class="ui radio checkbox">
                    <input id="orgradio" type="radio" value="org" name="linkto" tabindex="0" class="hidden">
                    <label for="org">
                        <g:message code="task.org.label" default="Subscription"/>
                    </label>
                </div>
            </div>

            <div id="licensediv"
                 class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'license', 'error')} required">
                <label for="license">
                    <g:message code="task.linkto" default="Task link to "/><g:message code="task.license.label" default="License"/>
                </label>
                <g:select id="license" name="license" from="${validLicenses}" optionKey="id"
                          value="${ownobj?.id}" class="ui dropdown many-to-one" noSelection="['': '']"/>
            </div>

            <div id="orgdiv" class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'org', 'error')} required">
            <label for="org">
                <g:message code="task.linkto" default="Task link to "/><g:message code="task.org.label" default="Org"/>
            </label>
            <g:select id="org" name="org" from="${validOrgs}" optionKey="id" value="${ownobj?.id}"
                      class="ui dropdown many-to-one" noSelection="['': '']"/>
        </div>

            <div id="pkgdiv" class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'pkg', 'error')} required">
                <label for="pkg">
                    <g:message code="task.linkto" default="Task link to "/><g:message code="task.pkg.label" default="Pkg"/>
                </label>
                <g:select id="pkg" name="pkg" from="${validPackages}" optionKey="id" value="${ownobj?.id}"
                          class="ui dropdown many-to-one" noSelection="['': '']"/>
            </div>

            <div id="subscriptiondiv"
                 class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} required">
                <label for="subscription">
                    <g:message code="task.linkto" default="Task link to "/><g:message code="task.subscription.label" default="Subscription"/>
                </label>
                <g:select id="subscription" name="subscription" from="${validSubscriptions}" optionKey="id"
                          value="${ownobj?.id}" class="ui dropdown many-to-one" noSelection="['': '']"/>
            </div>

        </g:if>


        <div class="field">
            <div class="two fields">

                <div class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
                    <label for="status">
                        <g:message code="task.status.label" default="Status"/>
                    </label>
                    <laser:select id="status" name="status.id"
                                  from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Task Status')}"
                                  optionValue="value" optionKey="id" required=""
                                  value="${taskInstance?.status?.id ?: com.k_int.kbplus.RefdataValue.findByValueAndOwner("Open", com.k_int.kbplus.RefdataCategory.findByDesc('Task Status')).id}"
                                  class="ui dropdown many-to-one"/>
                </div>

                <semui:datepicker class="wide eight" label="task.endDate.label" name="endDate"
                                  placeholder="default.date.label" value="${taskInstance?.endDate}" required="true"
                                  bean="${taskInstance}"/>

            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'responsible', 'error')}">
                    <label for="responsible">
                        <g:message code="task.responsible.label" default="Responsible"/>
                    </label>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioresponsibleOrg" type="radio" value="Org" name="responsible" tabindex="0" class="hidden" checked="">
                            <label for="radioresponsibleOrg">${message(code: 'task.responsibleOrg.label')} <strong>${contextService?.org?.getDesignation()}</strong> </label>
                        </div>
                    </div>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioresponsibleUser" type="radio" value="User" name="responsible" tabindex="0" class="hidden">
                            <label for="radioresponsibleUser">${message(code: 'task.responsibleUser.label')}</label>
                        </div>
                    </div>
                </div>

                <div id="responsibleUser"
                     class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')}">
                    <label for="responsibleUser">
                        <g:message code="task.responsibleUser.label" default="Responsible User"/>
                    </label>
                    <g:select id="responsibleUser" name="responsibleUser.id" from="${validResponsibleUsers}"
                              optionKey="id" optionValue="display" value="${taskInstance?.responsibleUser?.id}"
                              class="ui dropdown many-to-one" noSelection="['null': '']"/>
                </div>
            </div>
        </div>

    </g:form>
    <g:if test="${controllerName == 'myInstitution'}">
        <r:script>
            // initial side call
            $("#generalradio").prop( "checked", true );
            $("#licensediv, #orgdiv, #pkgdiv, #subscriptiondiv").hide();

            function showHideRequire (taskType) {
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
                    chooseRequiredDropdown(taskType);
                });
            }
            showHideRequire (
                'general'
            );

            showHideRequire (
                    'license'
            );
            showHideRequire (
                    'pkg'
            );
            showHideRequire (
                    'subscription'
            );
            showHideRequire (
                    'org'
            );


        </r:script>
    </g:if>
    <r:script>
        $("#radioresponsibleOrg").change(function () {
            $("#responsibleUser").hide();
        });
        $("#radioresponsibleUser").change(function () {
            $("#responsibleUser").show();
        });
        $("#radioresponsibleOrg").prop( "checked", true );
        $("#responsibleUser").hide();

        function chooseRequiredDropdown(opt) {
            $('#create_task')
                    .form({

                inline: true,
                fields: {
                    title: {
                        identifier  : 'title',
                        rules: [
                            {
                                type   : 'empty',
                                prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                            }
                        ]
                    },

                    endDate: {
                        identifier  : 'endDate',
                        rules: [
                            {
                                type   : 'empty',
                                prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                            }
                        ]
                    },
                    opt: {
                        identifier  : opt,
                        rules: [
                            {
                                type   : 'empty',
                                prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                            }
                        ]
                    },
                 }
            });
        }
        chooseRequiredDropdown()




    </r:script>
</semui:modal>
