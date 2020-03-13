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
        <h1 class="ui dividing  header">Komponenten zur Überprüfung der Barrierefreiheit</h1>

        <h2 class="ui dividing  header">Hauptnavigation</h2>
        <g:render template="templatesWCAGTest/mainNavigation" />

        <h2 class="ui dividing header">Brotkrumennavigation</h2>
        <semui:breadcrumbs>
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>

        <h2 class="ui dividing header">Buttons</h2>
        <g:render template="templatesWCAGTest/button" />

        <h2 class="ui dividing  header">Einfache Datentabelle</h2>
        <g:render template="templatesWCAGTest/simpleTable" />

        <h2 class="ui dividing  header">Formulare</h2>
        <g:render template="templatesWCAGTest/form" />


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


        <h2 class="ui dividing header">Toggle Segment</h2>

            <g:render template="../templates/filter/javascript" />
            <g:render template="templatesWCAGTest/toggle" />

    </div>
</body>
</html>
