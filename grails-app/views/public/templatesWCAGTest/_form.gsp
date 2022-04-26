<g:form class="ui form" id="create_task" url="[controller: 'dev', action: 'index']" method="post">
    <div class="field required">
        <label for="title">
            <g:message code="task.title.label"/> <g:message code="messageRequiredField" />
        </label>
        <g:textField id="title" name="title" required="required" value=""/>
    </div>

    <div class="field">
        <label for="description">
            <g:message code="default.description.label"/>
        </label>
        <g:textArea name="description" value="" rows="5" cols="40"/>
    </div>


    <div class="field required">
        <fieldset>
            <legend>
                <g:message code="task.typ"/> <g:message code="messageRequiredField" />
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


    <div id="licensediv" class="field required" >
        <label for="license">
            Art der Lizenz <g:message code="messageRequiredField" />
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
    <label for="license">
        Das ist das Label
    </label>
    <select  id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">>
        <option value="1116">Allianzlizenz</option>
        <option value="1214">Deal-Lizenz</option>
        <option value="1213">FID-Lizenz</option>
        <option value="1118">Konsortiallizenz</option>
        <option value="1215">Lokale Lizenz</option>
        <option value="1117">Nationallizenz</option>
    </select>
</div>




    <a href="#" class="ui button modalCreateTask" onclick="$('#modalCreateTask').modal('hide')">Schließen</a>
    <input type="submit" class="ui button green" name="save" value="Anlegen" onclick="event.preventDefault(); $('#modalCreateTask').find('form').submit()">
</g:form>



<laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.chooseRequiredDropdown = function (opt) {
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
        JSPC.app.chooseRequiredDropdown('status.id');
</laser:script>