<%@ page import="de.laser.api.v0.ApiManager" %>
<laser:htmlStart text="${message(code: 'apiRelease')}" />

<ui:h1HeaderWithIcon text="${message(code: 'apiIndex')}" type="help"/>

<div class="ui segment">

    <h2 class="ui heading">Wofür gibt es die LAS:eR-API?</h2>
    <p>Stellen Sie sich vor, Sie möchten eine Liste Ihrer Lizenzen in einem Fremdsystem nachnutzen. Sie können im LAS:eR-System die entsprechende Tabelle unter
        <g:link controller="myInstitution" action="currentSubscriptions" target="_blank">https://laser.hbz-nrw.de/myInstitution/currentSubscriptions</g:link>
    aufrufen und direkt einsehen.</p>

    <p>Doch nicht nur im Browser lassen sich diese Daten anzeigen. Auch Programme können automatisiert darauf zugreifen – über eine sogenannte API (Application Programming Interface). Damit können z.B. Lizenzlisten direkt in anderen Anwendungen verarbeitet werden, ohne dass Sie Inhalte manuell kopieren und einfügen müssen.</p>

    <p>Die LAS:eR-API stellt die Daten strukturiert und maschinenlesbar zur Verfügung. Sie kann von verschiedenen Programmiersprachen wie Python, Perl oder PHP genutzt werden – oder einfach per curl-Befehl über die Kommandozeile.</p>

    <p>Mit der LAS:eR-API können unter anderem diese Daten abgerufen werden (eigene und freigegebene):</p>

    <ul>
        <li>Lizenzen</li>
        <li>Verträge</li>
        <li>Kostenelemente</li>
        <li>Dokumente</li>
        <li>Pakete</li>
        <li>Plattformen</li>
        <li>Anbieter</li>
        <li>Zugangsdaten</li>
    </ul>
    <h2 class="ui heading">Nutzung der LAS:eR-API</h2>
    <p>Zur Nutzung der API ist eine <strong>LAS:eR-Pro-Lizenz</strong> oder eine <strong>LAS:eR-API-Lizenz</strong> erforderlich. Wenden Sie sich für weitere Informationen bei Bedarf an unser Service-Team unter <a class="content" href="mailto:laser@hbz-nrw.de">laser@hbz-nrw.de</a>.</p>

    <p>Sofern Ihre Einrichtung bereits über eine entsprechende Lizenz verfügt, finden Sie <g:link controller="org" action="settings" params="${[tab: 'api', id:contextService.getOrg().id]}" >hier Ihre Zugangsdaten</g:link>.</p>

    <p>Für Techniker haben wir eine Webseite bereit gestellt, auf der zum einen die LAS:eR-API beschrieben wird und zum anderen die API testweise ausprobiert werden kann (vgl. <g:link controller="api" target="_blank">laser.hbz-nrw.de/api</g:link>). Weitere Informationen zur aktuellen Entwicklung unserer API finden sich in den Release Notes zur API unter

    <g:link controller="public" action="api" params="${[id: 4]}" >https://laser-qa.hbz-nrw.de/public/api/4</g:link>.</p>

</div>

<laser:htmlEnd />