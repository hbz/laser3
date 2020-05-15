<g:form class="ui form" id="create_task" url="[controller: 'dev', action: 'index']" method="post">

    <div class="field fieldcontain  required">
        <label for="title">
            <g:message code="task.title.label"/>
        </label>
        <g:textField id="title" name="title" required="required" value=""/>
    </div>

    <div class="field fieldcontain">
        <label for="description">
            <g:message code="task.description.label"/>
        </label>
        <g:textArea name="description" value="" rows="5" cols="40"/>
    </div>


    <div class="field fieldcontain required">
        <fieldset>
            <legend>
                <g:message code="task.typ"/>
            </legend>

            <div class="ui radio checkbox">
                <input id="generalradio" type="radio" value="general" name="linkto" tabindex="0" class="hidden"
                       checked="">
                <label for="generalradio">${message(code: 'task.general')}</label>
            </div>
            &nbsp &nbsp
            <div class="ui radio checkbox">
                <input id="licenseradio" type="radio" value="license" name="linkto" tabindex="0" class="hidden">
                <label for="licenseradio">
                    <g:message code="license.label"/>
                </label>
            </div>
            &nbsp &nbsp
            <div class="ui radio checkbox">
                <input id="pkgradio" type="radio" value="pkg" name="linkto" tabindex="0" class="hidden">
                <label for="pkgradio">
                    <g:message code="package.label"/>
                </label>
            </div>
            &nbsp &nbsp
            <div class="ui radio checkbox">
                <input id="subscriptionradio" type="radio" value="subscription" name="linkto" tabindex="0"
                       class="hidden">
                <label for="subscriptionradio">
                    <g:message code="default.subscription.label"/>
                </label>
            </div>
            &nbsp &nbsp
            <div class="ui radio checkbox">
                <input id="orgradio" type="radio" value="org" name="linkto" tabindex="0" class="hidden">
                <label for="orgradio">
                    <g:message code="task.org.label"/>
                </label>
            </div>
        </fieldset>
    </div>


    <div id="licensediv" class="field fieldcontain  required" >
        <label for="license">
            Art der Lizenz
        </label>
        <select id="license" class="ui dropdown search many-to-one required "  required="required" name="license">
            <option value="">Bitte auswählen</option>


            <option value="1430">
                Allianzlizenz
            </option>

            <option value="1436">
                Deal-Lizenz
            </option>

            <option value="1435">
                FID-Lizenz
            </option>

            <option value="1432">
                Konsortiallizenz
            </option>

            <option value="1452">
                Lokale Lizenz
            </option>

            <option value="1431">
                Nationallizenz
            </option>

        </select>
    </div>




    <a href="#" class="ui button modalCreateTask" onclick="$('#modalCreateTask').modal('hide')">Schließen</a>
    <input type="submit" class="ui button green" name="save" value="Anlegen" onclick="event.preventDefault(); $('#modalCreateTask').find('form').submit()">
</g:form>



<r:script>
       $('#test').dropdown({
            //forceSelection: false
            //clearable: true
              preserveHTML: false
        });

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
                        }
                    });
            })
        }
        chooseRequiredDropdown('status.id');
</r:script>