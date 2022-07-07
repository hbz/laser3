<%@page import="grails.converters.JSON" %>

<laser:htmlStart text="Manage Stats Sources" />

        <semui:breadcrumbs>
            <semui:crumb message="menu.yoda" controller="yoda" action="index"/>
            <semui:crumb text="Stats Sources" class="active" />
        </semui:breadcrumbs>

        <semui:messages data="${flash}" />

        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Anbieter</th>
                    <th>Plattform</th>
                    <th>Aktionen</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${platforms}" var="platform">
                    <tr>
                        <td>Plattform - ${platform.id}</td>
                        <td>${platform.org.name}</td>
                        <td>${platform.name}</td>
                        <td>
                            <g:link class="ui negative button js-open-confirm-modal"
                                    data-confirm-tokenMsg="ACHTUNG! Sie sind im Begriff, die Zeiger für ${platform.name} zu entfernen! Damit werden beim nächsten Synclauf die Daten ab Anfang neu geladen! Derzeit vorhandene Daten werden bis dahin aber behalten! Fortfahren?"
                                    data-confirm-term-how="ok"
                                    controller="yoda"
                                    action="resetStatsData"
                                    role="button"
                                    params="${[platform: platform.id, fullReset: false]}">Cursor zurücksetzen</g:link>
                            <g:link class="ui negative button js-open-confirm-modal"
                                    data-confirm-tokenMsg="ACHTUNG! Sie sind im Begriff, alle Statistikdaten für ${platform.name} zurückzusetzen! Damit werden auch alle derzeit vorhandenen Daten verworfen und beim nächsten Synclauf neu geladen! Fortfahren?"
                                    data-confirm-term-how="ok"
                                    controller="yoda"
                                    action="resetStatsData"
                                    role="button"
                                    params="${[platform: platform.id, fullReset: true]}">Alle Statistik-Daten zu dieser Plattform löschen</g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>

<laser:htmlEnd />
