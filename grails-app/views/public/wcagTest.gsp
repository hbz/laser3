<%@ page import="java.sql.Timestamp; org.springframework.context.i18n.LocaleContextHolder; de.laser.Org; de.laser.License; de.laser.Subscription; de.laser.Task; de.laser.storage.RDStore;de.laser.storage.RDConstants" %>
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : Komponenten zur Überprüfung der Barrierefreiheit</title>
</head>

<body>
<laser:serviceInjection/>
    <div id="modalCreateTask">
        <h1 class="ui dividing  header">Komponenten zur Überprüfung der Barrierefreiheit</h1>

        <h2 class="ui dividing  header">1. Hauptnavigation</h2>
        <g:render template="templatesWCAGTest/mainNavigation" />

        <h2 class="ui dividing header">2. Brotkrumennavigation</h2>
        <semui:breadcrumbs>
            <semui:crumb message="myinst.currentSubscriptions.label"  />
        </semui:breadcrumbs>

        <h2 class="ui dividing header">3. Buttons</h2>
        <g:render template="templatesWCAGTest/button" />

        <h2 class="ui dividing header">4. Tooltips</h2>
        <g:render template="templatesWCAGTest/tooltip" />

        <h2 class="ui dividing  header">5. Einfache Datentabelle</h2>
        <g:render template="templatesWCAGTest/simpleTable" />

        <h2 class="ui dividing  header">6. Formulare</h2>
        <g:render template="templatesWCAGTest/form" />


%{--        <h2 class="ui dividing header">7. Icons</h2>
        <h3 class="ui dividing header">7.1. Dekoratives Icon mit einer Dopplung der Semantik durch Text, der daneben steht.</h3>--}%
%{--
        <div class="ui icon info message">
            <i aria-hidden="true" class="exclamation triangle icon"></i>
            <div class="content">
                <div class="header">
                    Achtung
                </div>
                <p>Wenn Sie ein neues Element nicht finden, warten Sie etwa <strong>10 Minuten</strong>, bis sich der Index aktualisiert hat.</p>
            </div>
        </div>--}%




        <h2 class="ui dividing header">7. Paginierung</h2>
        <g:render template="templatesWCAGTest/pagination" />

        <h2 class="ui dividing header">8. X-Editable</h2>
        <g:render template="templatesWCAGTest/xeditable" />


        <h2 class="ui dividing header">9. Unternavigation - Alternativ zur Registernavigation</h2>
        <g:render template="templatesWCAGTest/submenue" />

        <h2 class="ui dividing header">10. Datepicker</h2>
        <g:render template="templatesWCAGTest/datepicker" />

        <h2 class="ui dividing header">11. Modals</h2>
        <h3 class="ui dividing header">11.1. Confimation Modal</h3>
        <g:render template="templatesWCAGTest/modal" />




    </div>
</body>
</html>
