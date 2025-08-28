<%@ page import="de.laser.api.v0.ApiManager" %>
<laser:htmlStart text="${message(code: 'apiRelease')}" />

<ui:h1HeaderWithIcon text="${message(code: 'apiIndex')}" type="help"/>

<div class="ui segment">
    Die LAS:eR-API erlaubt den Abruf der folgenden Daten der eigenen Einrichtung und freigegebener Konsortialinformationen:
    <ul>
        <li>Dokumente</li>
        <li>Einrichtungsinformationen</li>
        <li>Kosteninformationen</li>
        <li>Lizenzinformationen</li>
        <li>Paketinformationen</li>
        <li>Plattforminformationen</li>
        <li>Vertragsinformationen</li>
        <li>Zugangsinformationen</li>
    </ul>
    Zur Nutzung der LAS:eR-API ist eine LAS:eR-Pro oder LAS:eR-API Lizenz erforderlich. Wenden Sie sich für weitere Informationen an laser@hbz-nrw.de.
</div>

<laser:htmlEnd />