<laser:htmlStart text="Datenbereinigung - Vertragsverknüpfungen ohne Einrichtungsverknüpfung" />

        <semui:messages data="${flash}"/>
        <table class="ui table celled">
            <thead>
                <tr>
                    <th>Lizenzname</th>
                    <th>Vertragsname</th>
                    <th>Einrichtungen</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${links}" var="row">
                    <g:set var="link" value="${row[0]}"/>
                    <g:set var="os" value="${row[1]}"/>
                    <tr>
                        <td>${link.destinationSubscription.name} (${link.destinationSubscription.id})</td>
                        <td>${link.sourceLicense.reference} (${link.sourceLicense.id})</td>
                        <td>${os.org.name}</td>
                    </tr>
                </g:each>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="3">
                        <g:link action="updateOrgLicRoles" class="ui negative button js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.createOrgLicLinks')}"
                                data-confirm-term-how="ok">Einrichtungen verknüpfen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>

<laser:htmlEnd />