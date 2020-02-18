<%@ page import="java.sql.Timestamp; org.springframework.context.i18n.LocaleContextHolder; com.k_int.kbplus.Org; com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.Task; org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil; de.laser.helper.RDStore;de.laser.helper.RDConstants" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : Komponenten zur Überprüfung der Barrierefreiheit</title>
</head>

<body>
<laser:serviceInjection/>
    <div id="modalCreateTask">
        <h1 class="ui dividing  header">Übersicht über alle in Laser eingestzten Komponenten</h1>
        <h2 class="ui dividing  header">Formulare</h2>
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
    </r:script>
<h2 class="ui dividing header">Buttons und Links</h2>
<h3 class="ui dividing header">Button mit Icon und Tooltip</h3>
<a  role="button" class="ui icon mini button la-audit-button la-popup-tooltip la-delay" href='https://www.spiegel.de' data-content="Das ist der Inhalt des Tooltips">
    <i  class="icon thumbtack la-js-editmode-icon"></i>
</a>
<h3 class="ui dividing header">Icons in einem Link ohne zusätzlichen Text</h3>
<g:link aria-label="Das ist eine Beschreibung für den Accessibility Tree" controller="dev" action="frontend" params="" class="ui icon positive button">
    <i aria-hidden="true" class="checkmark icon"></i>
</g:link>

<h2 class="ui dividing header">Icons</h2>
<h3 class="ui dividing header">Dekorative Icon mit einer Dopplung der Semantik durch Text, der daneben steht.</h3>

<div class="ui icon info message">
    <i aria-hidden="true" class="exclamation triangle icon"></i>
    <div class="content">
        <div class="header">
            Achtung
        </div>
        <p>Wenn Sie ein neues Element nicht finden, warten Sie etwa <strong>10 Minuten</strong>, bis sich der Index aktualisiert hat.</p>
    </div>
</div>
<h3 class="ui dividing header">Icons, die für assistive Tools keine Bedeutung haben müssen - zum Beispiel das Schließen von Meldungen</h3>
<div class="ui icon info message">
    <i aria-hidden="true" class="close icon"></i>
    <div class="content">
        <div class="header">
            Achtung
        </div>
        <p>Wenn Sie ein neues Element nicht finden, warten Sie etwa <strong>10 Minuten</strong>, bis sich der Index aktualisiert hat.</p>
    </div>
</div>
<h3 class="ui dividing header">Icons in einem Button, der eine Bezeichnung hat</h3>
<button class="ui   button la-inline-labeled la-js-filterButton la-clearfix blue"> Filter <i
        aria-hidden="true" class="filter icon"></i> <span
        class="ui circular label la-js-filter-total hidden">0</span>
</button>

<h3 class="ui dividing header"></h3>
<h2 class="ui dividing header">Modals</h2>
<h3 class="ui dividing header">Confimation Modal</h3>
<p class="la-clear-before">
    <g:link controller="dev"
            action="frontend"
            params=""
            data-content="Hier kommt der Tooltip rein"
            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
            data-confirm-term-how="delete"
            class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
            role="button">
        <i aria-hidden="true" class="trash alternate icon"></i>
    </g:link>
</p>

<h2 class="ui dividing header">Pagination</h2>
<h2 class="ui dividing header">Breadcrumb</h2>
<h2 class="ui dividing header">Accordion</h2>
<h2 class="ui dividing header">Toggle Segment</h2>
<button class="ui button la-inline-labeled la-js-filterButton la-clearfix blue">Filter
    <i aria-hidden="true" class="filter icon"></i>
    <span class="ui circular label la-js-filter-total hidden">0</span>
</button>
<div class="ui la-filter segment la-clear-before" >
    <form class="ui form" autocomplete="off">
        <div class="four fields">
            <div class="field">
                <label>Titel</label>
                <div class="ui input">
                    <input type="text" name="taskName" placeholder="Suchbegriff eingeben" value="">
                </div>
            </div>

            <div class="field fieldcontain"><label for="endDateFrom">Fälligkeitsdatum (von)</label><div class="ui calendar datepicker"><div class="ui input left icon"><div class="ui popup calendar"><table class="ui celled center aligned unstackable table seven column day"><thead><tr><th colspan="7"><span class="link">Februar 2020</span><span class="prev link"><i class="chevron left icon"></i></span><span class="next link"><i class="chevron right icon"></i></span></th></tr><tr><th>Mo</th><th>Di</th><th>Mi</th><th>Do</th><th>Fr</th><th>Sa</th><th>So</th></tr></thead><tbody><tr><td class="link adjacent disabled">27</td><td class="link adjacent disabled">28</td><td class="link adjacent disabled">29</td><td class="link adjacent disabled">30</td><td class="link adjacent disabled">31</td><td class="link">1</td><td class="link">2</td></tr><tr><td class="link">3</td><td class="link">4</td><td class="link">5</td><td class="link">6</td><td class="link">7</td><td class="link">8</td><td class="link">9</td></tr><tr><td class="link">10</td><td class="link">11</td><td class="link">12</td><td class="link">13</td><td class="link">14</td><td class="link">15</td><td class="link">16</td></tr><tr><td class="link">17</td><td class="link today focus">18</td><td class="link">19</td><td class="link">20</td><td class="link">21</td><td class="link">22</td><td class="link">23</td></tr><tr><td class="link">24</td><td class="link">25</td><td class="link">26</td><td class="link">27</td><td class="link">28</td><td class="link">29</td><td class="link adjacent disabled">1</td></tr><tr><td class="link adjacent disabled">2</td><td class="link adjacent disabled">3</td><td class="link adjacent disabled">4</td><td class="link adjacent disabled">5</td><td class="link adjacent disabled">6</td><td class="link adjacent disabled">7</td><td class="link adjacent disabled">8</td></tr></tbody></table></div><i aria-hidden="true" class="calendar icon"></i><input class="" name="endDateFrom" id="endDateFrom" type="text" placeholder="Datum" value=""></div></div></div>

            <div class="field fieldcontain"><label for="endDateTo">Fälligkeitsdatum (bis)</label><div class="ui calendar datepicker"><div class="ui input left icon"><div class="ui popup calendar"><table class="ui celled center aligned unstackable table seven column day"><thead><tr><th colspan="7"><span class="link">Februar 2020</span><span class="prev link"><i class="chevron left icon"></i></span><span class="next link"><i class="chevron right icon"></i></span></th></tr><tr><th>Mo</th><th>Di</th><th>Mi</th><th>Do</th><th>Fr</th><th>Sa</th><th>So</th></tr></thead><tbody><tr><td class="link adjacent disabled">27</td><td class="link adjacent disabled">28</td><td class="link adjacent disabled">29</td><td class="link adjacent disabled">30</td><td class="link adjacent disabled">31</td><td class="link">1</td><td class="link">2</td></tr><tr><td class="link">3</td><td class="link">4</td><td class="link">5</td><td class="link">6</td><td class="link">7</td><td class="link">8</td><td class="link">9</td></tr><tr><td class="link">10</td><td class="link">11</td><td class="link">12</td><td class="link">13</td><td class="link">14</td><td class="link">15</td><td class="link">16</td></tr><tr><td class="link">17</td><td class="link today focus">18</td><td class="link">19</td><td class="link">20</td><td class="link">21</td><td class="link">22</td><td class="link">23</td></tr><tr><td class="link">24</td><td class="link">25</td><td class="link">26</td><td class="link">27</td><td class="link">28</td><td class="link">29</td><td class="link adjacent disabled">1</td></tr><tr><td class="link adjacent disabled">2</td><td class="link adjacent disabled">3</td><td class="link adjacent disabled">4</td><td class="link adjacent disabled">5</td><td class="link adjacent disabled">6</td><td class="link adjacent disabled">7</td><td class="link adjacent disabled">8</td></tr></tbody></table></div><i aria-hidden="true" class="calendar icon"></i><input class="" name="endDateTo" id="endDateTo" type="text" placeholder="Datum" value=""></div></div></div>

            <div class="field">
                <label>Aufgabenstatus</label>
                <div class="ui fluid dropdown selection" tabindex="0"><select name="taskStatus" id="taskStatus">
                    <option value="">Alle</option>
                    <option value="121">Erledigt</option>
                    <option value="120">Offen</option>
                    <option value="122">Zurückgestellt</option>
                </select><i class="dropdown icon"></i><div class="default text">Alle</div><div class="menu" tabindex="-1"><div class="item" data-value="121">Erledigt</div><div class="item" data-value="120">Offen</div><div class="item" data-value="122">Zurückgestellt</div></div></div>
            </div>
        </div>
        <div class="field la-field-right-aligned">
            <a href="/laser/myInstitution/tasks" class="ui reset primary button">Filter zurücksetzen</a>
            <input type="submit" class="ui secondary button" value="Filtern">
        </div>
    </form>
</div>
</body>
</html>
