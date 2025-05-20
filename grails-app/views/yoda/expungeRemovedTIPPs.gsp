<%@ page import="de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.wekb.TitleInstancePackagePlatform; grails.converters.JSON" %>

<laser:htmlStart text="Datenbereinigung: Gelöschte TIPPs" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="Datenbereinigung" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="Datenbereinigung" type="yoda" />

        <ui:messages data="${flash}"/>
        <table>
            <thead>
                <tr>
                    <th colspan="5">
                        <g:link action="expungeRemovedTIPPs" params="[doIt: true]" class="${Btn.NEGATIVE_CONFIRM}" data-confirm-tokenMsg="${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </th>
                </tr>
                <tr>
                    <th>TIPP ID + we:kb-ID</th>
                    <th>Titel</th>
                    <th>Status we:kb</th>
                    <th>Status LAS:eR</th>
                    <th>betroffene Titel in Lizenzen</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${titles}" var="entry">
                    <tr>
                        <td>${entry.tippId} + ${entry.wekbId}</td>
                        <td>${entry.name}</td>
                        <td>${entry.wekbStatus}</td>
                        <td>${entry.laserStatus}</td>
                        <td>
                            <ul>
                                <g:each in="${entry.issueEntitlements}" var="ie">
                                    <li>${ie.id} -> ${ie.subscriptionName}</li>
                                </g:each>
                            </ul>
                        </td>
                    </tr>
                </g:each>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="5">
                        <g:link action="expungeRemovedTIPPs" params="[doIt: true]" class="${Btn.NEGATIVE_CONFIRM}" data-confirm-tokenMsg="${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>

<laser:htmlEnd />