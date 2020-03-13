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
            Aufgabe verknüpfen mit Vertrag
        </label>

        <select id="license" class="ui dropdown search many-to-one required"  required="required" name="license">
            <option value="">Bitte auswählen</option>
            <option value="57">ACS Lizenzvertrag Aktiv (01.01.08-)</option>
            <option value="101">Beck Lizenzvertrag Aktiv (01.01.19-)</option>
            <option value="51">Berlin Phil Media Lizenzvertrag Aktiv (01.04.17-)</option>
            <option value="53">Beuth Rahmenvertrag Aktiv (15.01.01-)</option>
            <option value="52">Brepols Rahmenvertrag Aktiv (01.01.12-)</option>
            <option value="50">CAS Rahmenvertrag Aktiv (01.01.15-)</option>
            <option value="49">Clarivate Rahmenvertrag Aktiv (01.01.17-)</option>
            <option value="30">de Gruyter Rahmenvertrag Aktiv (09.07.12-)</option>
            <option value="48">DIZ Lizenzvertrag Aktiv (01.07.16-)</option>
            <option value="8">DUZ Rahmenvertrag Aktiv (01.01.18-)</option>
            <option value="26">EBSCO Rahmenvertrag Aktiv (01.01.15-)</option>
            <option value="1237">Elsevier Rahmenvertrag Aktiv (01.01.10-)</option>
            <option value="16">Emerald Rahmenvertrag Aktiv (01.01.17-)</option>
            <option value="55">ern+ heinzl Lizenzvertrag Aktiv (01.01.12-)</option>
            <option value="56">Gale Cengage Rahmenvertrag Aktiv (15.12.07-)</option>
            <option value="31">GBI-Genios Rahmenvertrag Videos Aktiv (01.02.17-)</option>
            <option value="32">GBI-Genios Rahmenvertrag wiso-net Aktiv (01.01.06-)</option>
            <option value="54">Herdt Lizenzvertrag Aktiv (01.01.18-)</option>
            <option value="47">K.lab Lizenzvertrag Aktiv (01.01.16-)</option>
            <option value="15">LexisNexis Rahmenvertrag Aktiv (01.01.18-)</option>
            <option value="100">LISK Lizenzvertrag Aktiv (01.10.19-)</option>
            <option value="46">Munzinger Archive Rahmenvertrag Aktiv (01.01.13-)</option>
            <option value="97">Munzinger Duden Lizenzvertrag Aktiv (01.01.13-)</option>
            <option value="98">Munzinger edition text+kritik Rahmenvertrag Aktiv (15.06.08-)</option>
            <option value="45">Naxos Rahmenvertrag Aktiv (01.01.12-)</option>
            <option value="27">NE Rahmenvertrag Aktiv (01.01.16-)</option>
            <option value="1016">New York Times Rahmenvertrag Aktiv (01.01.19-)</option>
            <option value="99">OECD AGB Aktiv (15.06.09-)</option>
            <option value="43">OUP Rahmenvertrag Aktiv (01.01.10-)</option>
            <option value="44">Ovid Rahmenvertrag Aktiv (01.01.12-)</option>
            <option value="35">Prometheus Lizenzvertrag Aktiv (16.05.18-)</option>
            <option value="41">ProQuest Rahmenvertrag Aktiv (01.01.11-)</option>
            <option value="42">RILM Lizenzvertrag Aktiv (01.02.17-)</option>
            <option value="40">Rosetta Stone Rahmenvertrag Aktiv (01.01.18-)</option>
            <option value="28">Statista Rahmenvertrag Aktiv (01.01.14-)</option>
            <option value="39">Thieme Rahmenvertrag Aktiv (19.02.10-)</option>
            <option value="38">VDE Lizenzvertrag Aktiv (15.11.04-)</option>
            <option value="29">Verlag Europa-Lehrmittel Rahmenvertrag Aktiv (01.01.17-31.12.19)</option>
            <option value="34">Wolters Kluwer Rahmenvertrag Aktiv (21.07.14-)</option>
            <option value="33">WTI Rahmenvertrag Aktiv (01.01.12-)</option>
        </select>
    </div>




    <a href="#" class="ui button modalCreateTask" onclick="$('#modalCreateTask').modal('hide')">Schließen</a>
    <input type="submit" class="ui button green" name="save" value="Anlegen" onclick="event.preventDefault(); $('#modalCreateTask').find('form').submit()">
</g:form>
</div>

<r:script>
        $('.dropdown').dropdown();

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