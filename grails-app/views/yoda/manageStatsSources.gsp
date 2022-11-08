<%@page import="grails.converters.JSON" %>

<laser:htmlStart text="Manage Stats Sources" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
            <ui:crumb text="Stats Sources" class="active" />
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon text="Stats Sources" />

        <ui:messages data="${flash}" />

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
                    <%
                        String sushiURL = "", counterRevision = ""
                        if(platformInstanceRecords[platform.gokbId].counterR5SushiApiSupported == 'Yes') {
                            sushiURL = platformInstanceRecords[platform.gokbId].counterR5SushiServerUrl
                            counterRevision = "r5"
                        }
                        else if(platformInstanceRecords[platform.gokbId].counterR5SushiApiSupported == 'No' && platformInstanceRecords[platform.gokbId].counterR4SushiApiSupported == 'Yes') {
                            sushiURL = platformInstanceRecords[platform.gokbId].counterR4SushiServerUrl
                            counterRevision = "r4"
                        }
                    %>
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
                                    data-confirm-tokenMsg="ACHTUNG! Sie sind im Begriff, alle Statistikdaten für ${platform.name} zurückzusetzen! Damit werden auch alle derzeit vorhandenen Daten verworfen und direkt neu geladen! Fortfahren?"
                                    data-confirm-term-how="ok"
                                    controller="yoda"
                                    action="resetStatsData"
                                    role="button"
                                    params="${[platform: platform.id, fullReset: true, sushiURL: sushiURL, counterRevision: counterRevision]}">Alle Statistik-Daten zu dieser Plattform löschen und neu laden</g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>

<laser:htmlEnd />
