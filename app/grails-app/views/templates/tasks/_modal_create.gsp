<%@ page import="java.sql.Timestamp; org.springframework.context.i18n.LocaleContextHolder; com.k_int.kbplus.Org; com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.Task; org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil; de.laser.helper.RDStore;de.laser.helper.RDConstants" %>
<laser:serviceInjection />
<% double start = System.currentTimeMillis()
double t1 = 0L
double t2 = 0L
double t3 = 0L
double t4 = 0L
double t5 = 0L
double t6 = 0L
double t7 = 0L
double t8 = 0L
%>
<semui:modal id="modalCreateTask" message="task.create.new">

    <g:form class="ui form" id="create_task" url="[controller: 'task', action: 'create']" method="post">
        <g:if test="${controllerName != 'myInstitution' && controllerName != 'ajax'}">
            <g:hiddenField name="${owntp}" value="${(owntp == 'surveyConfig') ? ownobj?.id : params.id}"/>
            <g:hiddenField name="linkto" value="${owntp}"/>
        </g:if>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
            <label for="title">
                <g:message code="task.title.label" default="Title"/>
            </label>
            <g:textField id="title" name="title" required="" value="${taskInstance?.title}"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
            <label for="description">
                <g:message code="task.description.label" default="Description"/>
            </label>
            <g:textArea name="description" value="${taskInstance?.description}" rows="5" cols="40"/>
        </div>

        <g:if test="${controllerName == 'myInstitution' || controllerName == 'ajax'}">
            <div class="field fieldcontain required">
                <fieldset>
                    <legend>
                        <g:message code="task.typ" default="Task Typ"/>
                    </legend>
                    <div class="ui radio checkbox">
                        <input id="generalradio" type="radio" value="general" name="linkto" tabindex="0" class="hidden" checked="">
                        <label for="generalradio">${message(code: 'task.general')}</label>
                    </div>
                    &nbsp &nbsp
                    <div class="ui radio checkbox">
                        <input id="licenseradio" type="radio" value="license" name="linkto" tabindex="0" class="hidden">
                        <label for="licenseradio">
                            <g:message code="task.license.label" default="License"/>
                        </label>
                    </div>
                    &nbsp &nbsp
                    <div class="ui radio checkbox">
                        <input id="pkgradio" type="radio" value="pkg" name="linkto" tabindex="0" class="hidden">
                        <label for="pkgradio">
                            <g:message code="task.pkg.label" default="Pkg"/>
                        </label>
                    </div>
                    &nbsp &nbsp
                    <div class="ui radio checkbox">
                        <input id="subscriptionradio" type="radio" value="subscription" name="linkto" tabindex="0" class="hidden">
                        <label for="subscriptionradio">
                            <g:message code="default.subscription.label" default="Subscription"/>
                        </label>
                    </div>
                    &nbsp &nbsp
                    <div class="ui radio checkbox">
                        <input id="orgradio" type="radio" value="org" name="linkto" tabindex="0" class="hidden">
                        <label for="orgradio">
                            <g:message code="task.org.label" default="Subscription"/>
                        </label>
                    </div>
                </fieldset>
            </div>
            <% t1 = System.currentTimeMillis() %>

            <div id="licensediv"
                 class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'license', 'error')} required">
                <label for="license">
                    <g:message code="task.linkto" default="Task link to "/><g:message code="task.license.label" default="License"/>
                </label>
                <g:select class="ui dropdown search many-to-one"
                          id="license"
                          name="license"
                          from="${validLicensesDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          noSelection="[null: '']"/>

            </div>
            <% t2 = System.currentTimeMillis() %>

            <div id="orgdiv" class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'org', 'error')} required">
            <label for="org">
                <g:message code="task.linkto" default="Task link to "/><g:message code="task.org.label" default="Org"/>
            </label>
                <g:select id="org"
                          name="org"
                          from="${validOrgsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          class="ui dropdown search many-to-one"
                          noSelection="[null: '']"/>
            </div>
            <% t3 = System.currentTimeMillis()%>

            <div id="pkgdiv" class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'pkg', 'error')} required">
                <label for="pkg">
                    <g:message code="task.linkto" default="Task link to "/><g:message code="task.pkg.label" default="Pkg"/>
                </label>
                <g:select id="pkg" name="pkg" from="${validPackages}" optionKey="id" value="${ownobj?.id}"
                          class="ui dropdown search many-to-one" noSelection="[null: '']"/>
            </div>
            <% t4 = System.currentTimeMillis() %>

            <div id="subscriptiondiv"
                 class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} required">
                <label for="subscription">
                    <g:message code="task.linkto" default="Task link to "/><g:message code="default.subscription.label" default="Subscription"/>
                </label>
                <g:select class="ui dropdown search many-to-one"
                          id="subscription"
                          name="subscription"
                          from="${validSubscriptionsDropdown}"
                          optionKey="${{it.optionKey}}"
                          optionValue="${{it.optionValue}}"
                          value="${ownobj?.id}"
                          noSelection="[null: '']"/>

            </div>
            <% t5 = System.currentTimeMillis() %>

        </g:if>


        <% t6 = System.currentTimeMillis()%>
        <div class="field">
            <div class="two fields">

                <div class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
                    <label for="status">
                        <g:message code="task.status.label" default="Status"/>
                    </label>
                    <laser:select id="status" name="status.id"
                                  from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
                                  optionValue="value" optionKey="id" required=""
                                  value="${taskInstance?.status?.id ?: com.k_int.kbplus.RefdataValue.getByValueAndCategory("Open", RDConstants.TASK_STATUS).id}"
                                  class="ui dropdown search many-to-one"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>

                <semui:datepicker class="wide eight" label="task.endDate.label" id="endDate" name="endDate"
                                  placeholder="default.date.label" value="${taskInstance?.endDate}" required=""
                                  bean="${taskInstance}"/>

            </div>
        </div>
        <% t7 = System.currentTimeMillis() %>

        <div class="field" id="radioGroup">
            <div class="two fields">
                <div class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'responsible', 'error')}">
                    <fieldset>
                        <legend>
                            <g:message code="task.responsible.label" default="Responsible"/>
                        </legend>

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
                    </fieldset>
                </div>

                <div id="responsibleUser"
                     class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')}">
                    <label for="responsibleUserInput">
                        <g:message code="task.responsibleUser.label" default="Responsible User"/>
                    </label>
                    <g:select id="responsibleUserInput"
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
            <% t8 = System.currentTimeMillis()%>
        </div>

    </g:form>
    %{--controllerName ${controllerName}<br>--}%
    %{--validLicensesDropdown ${validLicensesDropdown?.size()}<br>--}%
    %{--validOrgsDropdown ${validOrgsDropdown?.size()}<br>--}%
    %{--validPackages ${validPackages?.size()}<br>--}%
    %{--validSubscriptionsDropdown ${validSubscriptionsDropdown?.size()}<br>--}%
    %{--validResponsibleUsers ${validResponsibleUsers?.size()}<br><br>--}%
    %{--Zeiten:<br/>--}%
    %{--<% java.text.DecimalFormat myFormatter = new java.text.DecimalFormat("###,###"); %>--}%
    %{--t1 ${myFormatter.format(t1-start)}<br>--}%
    %{--t2 ${myFormatter.format(t2-t1)}<br>--}%
    %{--t3 ${myFormatter.format(t3-t2)}<br>--}%
    %{--t4 ${myFormatter.format(t4-t3)}<br>--}%
    %{--t5 ${myFormatter.format(t5-t4)}<br>--}%
    %{--t6 ${myFormatter.format(t6-t5)}<br>--}%
    %{--t7 ${myFormatter.format(t7-t6)}<br>--}%
    %{--t8 ${myFormatter.format(t8-t7)}<br>--}%

    %{--<% def ende = System.currentTimeMillis()--}%
        %{--def dauerBackFrontend = backendStart ? ende-backendStart : 0L--}%
        %{--def dauerFrontend = ende-start--}%
    %{--%>--}%
    %{--****************** Backend + Frontend DAUER: ${backendStart? myFormatter.format(dauerBackFrontend) : 'n/a'} ******************<br>--}%
    %{--****************** Frontend           DAUER: ${myFormatter.format(dauerFrontend)} ******************--}%
    <g:if test="${controllerName == 'myInstitution' || controllerName == 'ajax'}">
        <script>
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


        </script>
    </g:if>
    <script>
            $("#radioresponsibleOrg").change(function () {
                $('#radioGroup').find("#responsibleUser").toggle();

            });
            $("#radioresponsibleUser").change(function () {
                $('#radioGroup').find("#responsibleUser").toggle();
            });
            if ($("#radioresponsibleUser").is(':checked')) {
                $("#responsibleUser").show();
            } else {
                $("#responsibleUser").hide();
            }



        function chooseRequiredDropdown(opt) {
            $(document).ready(function () {
                $('#create_task')
                    .form({

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
                            opt: {
                                identifier: opt,
                                rules: [
                                    {
                                        type: 'empty',
                                        prompt: '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                                    }
                                ]
                            },
                        }
                    });
            })
        }
        chooseRequiredDropdown('status.id');




    </script>
    <script>
        var ajaxPostFunc = function () {

            $("#radioresponsibleOrgEdit").change(function () {
                $("#responsibleUserEdit").hide();
            });

            $("#radioresponsibleUserEdit").change(function () {
                $("#responsibleUserEdit").show();
            });

            if ($("#radioresponsibleUserEdit").is(':checked')) {
                $("#responsibleUserEdit").show();
            } else {
                $("#responsibleUserEdit").hide();
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
                        }
                    }
                });
        }
    </script>

</semui:modal>
