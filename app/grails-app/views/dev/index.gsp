<%@ page import="java.sql.Timestamp; org.springframework.context.i18n.LocaleContextHolder; com.k_int.kbplus.Org; com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.Task; org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil; de.laser.helper.RDStore;de.laser.helper.RDConstants" %>
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : Komponenten zur Überprüfung der Barrierefreiheit</title>
</head>

<body>
<laser:serviceInjection/>
    <div id="modalCreateTask">
        <h1 class="ui dividing  header">Übersicht über alle in Laser eingesetzten Komponenten</h1>

        <h2 class="ui dividing  header">Einfache Datentabelle</h2>
        <g:render template="simpleTable" />

        <h2 class="ui dividing  header">Hauptnavigation</h2>
        <g:render template="mainNavigation" />


        <h2 class="ui dividing  header">Formulare</h2>
        <g:render template="form" />

        <h2 class="ui dividing header">Buttons und Links</h2>
        <h3 class="ui dividing header">Link funktioniert als Button mit Tooltip</h3>
        <a  role="button" class="ui icon mini button la-audit-button la-popup-tooltip la-delay" href='https://www.spiegel.de' data-content="Das ist der Inhalt des Tooltips">
            <i  class="icon thumbtack la-js-editmode-icon"></i>
        </a>
        <h3 class="ui dividing header">Link funktioniert als Button ohne Tooltip</h3>
        <g:link role="button" aria-label="Das ist eine Beschreibung für den Accessibility Tree" controller="dev" action="frontend" params="" class="ui icon positive button">
            <i aria-hidden="true" class="checkmark icon"></i>
        </g:link>

        <h2 class="ui dividing header">Icons</h2>
        <h3 class="ui dividing header">Dekoratives Icon mit einer Dopplung der Semantik durch Text, der daneben steht.</h3>

        <div class="ui icon info message">
            <i aria-hidden="true" class="exclamation triangle icon"></i>
            <div class="content">
                <div class="header">
                    Achtung
                </div>
                <p>Wenn Sie ein neues Element nicht finden, warten Sie etwa <strong>10 Minuten</strong>, bis sich der Index aktualisiert hat.</p>
            </div>
        </div>
        <h3 class="ui dividing header">Icon, das für assistive Tools keine Bedeutung haben muss - zum Beispiel das Schließen von Meldungen</h3>
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

        %{--<h2 class="ui dividing header">Pagination</h2>--}%
        <h2 class="ui dividing header">Breadcrumb</h2>
        <semui:breadcrumbs>
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>
        %{--<h2 class="ui dividing header">Accordion</h2>--}%
        <h2 class="ui dividing header">Toggle Segment</h2>

            <g:render template="../templates/filter/javascript" />
            <semui:filter showFilterButton="true">
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
            </semui:filter>
        </div>
</body>
</html>
