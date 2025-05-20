<%@ page import="de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.wekb.TitleInstancePackagePlatform; grails.converters.JSON" %>

<laser:htmlStart text="Datenbereinigung: TIPPs ohne we:kb ID" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="Datenbereinigung" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="Datenbereinigung" type="yoda" />

        <ui:messages data="${flash}"/>
        <table>
            <thead>
                <tr>
                    <th>TIPP ID</th>
                    <th>Titel</th>
                    <th>alternatives TIPP mit we:kb ID</th>
                    <th>betroffene Titel in Lizenzen</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${tipps}" var="entry">
                    <tr>
                        <td>${entry.tipp.id}</td>
                        <td>${entry.name}</td>
                        <td>
                            <g:if test="${entry.tipp.altTIPP}">
                                alternatives TIPP im gleichen Paket: ${entry.altTIPP.id}, verfügt über we:kb ID ${entry.altTIPP.gokbId}
                            </g:if>
                            <g:else>
                                kein alternatives TIPP vorhanden!
                            </g:else>
                        </td>
                        <td>
                            <ul>
                                <g:each in="${issueEntitlements.get(entry.tipp)}" var="ie">
                                    <li>${ie.id} -> ${ie.subscription.dropdownNamingConvention()}</li>
                                </g:each>
                            </ul>
                        </td>
                    </tr>
                </g:each>
            </tbody>
            <tfoot>
                <tr>
                    <td>
                        <g:link action="purgeTIPPsWithoutGOKBId" params="${[doIt: true, toDelete: toDelete as JSON, toUUIDfy: toUUIDfy as JSON]}" class="${Btn.NEGATIVE_CONFIRM}" data-confirm-tokenMsg = "${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                    <td>
                        <g:link action="purgeTIPPsWithoutGOKBId" params="${[doIt: false, toDelete: toDelete as JSON, toUUIDfy: toUUIDfy as JSON]}" class="${Btn.SIMPLE}">Testlauf (gefahrlos)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>

<laser:htmlEnd />