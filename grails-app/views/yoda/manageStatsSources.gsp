<%@page import="grails.converters.JSON; de.laser.RefdataValue; de.laser.storage.RDConstants" %>

<laser:htmlStart text="Manage Stats Sources" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
            <ui:crumb text="Stats Sources" class="active" />
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon text="Stats Sources" type="yoda" />

        <ui:messages data="${flash}" />

        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Anbieter</th>
                    <th>Plattform</th>
                    <th>Intervall</th>
                    <th>SUSHI-Authentifizierungskonfiguration</th>
                    <th>zentraler API-Schlüssel</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${platforms}" var="platform">
                    <tr>
                        <td>Plattform - ${platform.id}</td>
                        <td>${platform.org ? platform.org.name : null}</td>
                        <td>${platform.name}</td>
                        <td>
                            <g:if test="${platformInstanceRecords[platform.gokbId].statisticsUpdate}">
                                ${RefdataValue.getByValueAndCategory(platformInstanceRecords[platform.gokbId].statisticsUpdate, RDConstants.PLATFORM_STATISTICS_FREQUENCY)?.getI10n("value")}
                            </g:if>
                        </td>
                        <td>${platformInstanceRecords[platform.gokbId].sushiApiAuthenticationMethod}</td>
                        <td>${platformInstanceRecords[platform.gokbId].centralApiKey}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

<laser:htmlEnd />
