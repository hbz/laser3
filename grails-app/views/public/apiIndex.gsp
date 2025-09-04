<%@ page import="de.laser.api.v0.ApiManager" %>
<laser:htmlStart text="${message(code: 'apiRelease')}" />

<ui:h1HeaderWithIcon text="${message(code: 'apiIndex')}" type="help"/>

<div class="ui segment">

    <h2 class="ui heading">Wofür gibt es die LAS:eR-API?</h2>
    <p>Als zentrales bundesweites ERM-System enthält LAS:eR wichtige Daten zur Lizenzverwaltung. Über die Datenschnittstelle/API (Application Programming Interface) ist es Drittsystemen möglich, diese Daten automatisiert abzurufen, wenn eine entsprechende Freigabe erteilt und autorisiert wurde, z.B. für die Übernahme in das lokale Bibliothekssystem. Die Nutzung von LAS:eR als Datenquelle reduziert manuelle/doppelte Datenhaltung.</p>

    <p>Die LAS:eR-API stellt u.a. die folgenden eigenen und freigegebenen Daten strukturiert und maschinenlesbar zum Abruf bereit:</p>

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
    <p>Zur Nutzung der API ist eine <strong>LAS:eR-Pro-Lizenz</strong> oder eine <strong>LAS:eR-API-Lizenz</strong> erforderlich. Bei Fragen wenden Sie sich bitte an das Service-Team unter <a class="content" href="mailto:laser@hbz-nrw.de">laser@hbz-nrw.de</a>.</p>

    <p>Falls Ihre Einrichtung bereits über eine entsprechende Lizenz verfügt, finden Sie Ihre Zugangsdaten <g:link controller="org" action="settings" params="${[tab: 'api', id:contextService.getOrg().id]}" >im Reiter Datenweitergabe des Einrichtungsprofils</g:link>.</p>

    <p>Für die technische Anbindung steht die entsprechende Spezifikation sowie eine Möglichkeit zum Test der API direkt unter <g:link controller="api" target="_blank">laser.hbz-nrw.de/api</g:link> bereit.</p>

    <p> Weitere Informationen zur aktuellen Entwicklung unserer API finden sich in den <g:link controller="public" action="api" params="${[id: 4]}" >Release Notes zur API </g:link>.</p>


</div>

<laser:htmlEnd />