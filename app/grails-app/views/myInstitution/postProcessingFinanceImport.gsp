<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 07.06.2019
  Time: 12:27
--%>

<%@ page import="grails.converters.JSON;com.k_int.kbplus.OrgRole;de.laser.helper.RDStore" contentType="text/html;charset=UTF-8" %>
<laser:serviceInjection/>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>Finanzdaten importieren - zweiter Schritt</title>
    </head>

    <semui:messages data="${flash}" />

    <body>
        <h2>Zur Zeit nur Test</h2>
        <h3>Es wurden Daten ausgelesen. Ausgabe der Daten samt Fehler:</h3>
        <g:form name="costItemParameter" action="addCostItems" controller="finance" params="[candidates:candidates.keySet() as JSON, costItemGroups:costItemGroups as JSON]" method="post">
            <table>
                <tr>
                    <th>Kostenposten</th>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        Für Teilnehmer sichtbar machen?
                    </g:if>
                    <th>Posten übernehmen?</th>
                </tr>
                <g:each in="${candidates.entrySet()}" var="row" status="r">
                    <tr>
                        <td>
                            <ul>
                                <g:set var="ci" value="${row.getKey()}"/>
                                <g:set var="errors" value="${row.getValue()}"/>
                                <li><g:message code="myinst.financeImport.institution"/>: ${ci.owner?.sortname}</li>
                                <li><g:message code="myinst.financeImport.subscription"/>: ${ci.sub?.dropdownNamingConvention()}</li>
                                <li><g:message code="myinst.financeImport.package"/>: ${ci.subPkg?.pkg?.name}</li>
                                <li><g:message code="myinst.financeImport.issueEntitlement"/>: ${ci.issueEntitlement?.tipp?.title?.title}</li>
                                <li><g:message code="myinst.financeImport.orderNumber"/>: ${ci.order?.orderNumber}</li>
                                <li><g:message code="myinst.financeImport.invoiceNumber"/>: ${ci.invoice?.invoiceNumber}</li>
                                <li><g:message code="myinst.financeImport.currency"/>: ${ci.billingCurrency?.value}</li>
                                <li><g:message code="myinst.financeImport.element"/>: ${ci.costItemElement?.getI10n('value')}</li>
                                <li><g:message code="myinst.financeImport.elementSign"/>: ${ci.costItemElementConfiguration?.getI10n('value')}</li>
                                <li><g:message code="myinst.financeImport.taxType"/> (<g:message code="myinst.financeImport.taxRate"/>): ${ci.taxKey?.taxType?.getI10n('value')} (${ci.taxKey?.taxRate} %)</li>
                                <li><g:message code="myinst.financeImport.invoiceTotal"/>: ${ci.costInBillingCurrency}</li>
                                <li><g:message code="myinst.financeImport.value"/>: ${ci.costInLocalCurrency}</li>
                                <li><g:message code="myinst.financeImport.exchangeRate"/>: ${ci.currencyRate}</li>
                                <li><g:message code="myinst.financeImport.invoiceDate"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.invoiceDate}"/></li>
                                <li><g:hiddenField name="financialYear${r}" value="${ci.financialYear}"/><g:message code="myinst.financeImport.financialYear"/>: ${ci.financialYear}</li>
                                <li><g:message code="myinst.financeImport.title"/>: ${ci.costTitle}</li>
                                <li><g:message code="myinst.financeImport.description"/>: ${ci.costDescription}</li>
                                <li><g:message code="myinst.financeImport.referenceCodes"/>: ${ci.reference}</li>
                                <li><g:message code="myinst.financeImport.budgetCode"/>:
                                    <ul>
                                        <g:each in="${costItemGroups.get(ci)}" var="cig">
                                            <li>${cig.budgetCode}</li>
                                        </g:each>
                                    </ul>
                                </li>
                                <li><g:message code="myinst.financeImport.datePaid"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.datePaid}"/></li>
                                <li><g:message code="myinst.financeImport.dateFrom"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.startDate}"/></li>
                                <li><g:message code="myinst.financeImport.dateTo"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.endDate}"/></li>
                                <li>Fehler:
                                    <ul>
                                        <g:each in="${errors}" var="error">
                                            <g:if test="${error.getKey() == 'ownerMismatchError'}"><li>Falsche Zuordnung der Organisation! Der Identifikator gehört zu ${error.getValue()}</li></g:if>
                                            <g:if test="${error.getKey() == 'noValidSubscription'}"><li>Zum Identifikator ${error.getValue()} wurde keine passende Lizenz gefunden</li></g:if>
                                            <g:if test="${error.getKey() == 'multipleSubError'}"><li>Mehrere Lizenzen zu gegebenem Identifikator gefunden: ${error.getValue()}</li></g:if>
                                            <g:if test="${error.getKey() == 'packageWithoutSubscription'}"><li>Ohne Lizenz kann kein Posten zu einem Paket zugeordnet werden</li></g:if>
                                            <g:if test="${error.getKey() == 'noValidPackage'}"><li>Zum Identifikator ${error.getValue()} wurde kein passendes Paket gefunden</li></g:if>
                                            <g:if test="${error.getKey() == 'multipleSubPkgError'}"><li>Mehrere Pakete zu gegebenem Identifikator gefunden: ${error.getValue()}</li></g:if>
                                            <g:if test="${error.getKey() == 'entitlementWithoutPackageOrSubscription'}"><li>Ohne Lizenz und Paket kann kein Posten zu einem Einzeltitel zugeordnet werden</li></g:if>
                                            <g:if test="${error.getKey() == 'noValidTitle'}"><li>Zum Identifikator ${error.getValue()} wurde keine passende Titelinstanz gefunden</li></g:if>
                                            <g:if test="${error.getKey() == 'multipleTitleError'}"><li>Mehrere Titelinstanzen zu gegebenem Identifikator gefunden: ${error.getValue()}</li></g:if>
                                            <g:if test="${error.getKey() == 'noValidEntitlement'}"><li>Zum Identifikator ${error.getValue()} wurde kein passender Bestandstitel gefunden</li></g:if>
                                            <g:if test="${error.getKey() == 'multipleEntitlementError'}"><li>Mehrere Bestandstitel zu gegebenem Identifikator gefunden: ${error.getValue()}</li></g:if>
                                            <g:if test="${error.getKey() == 'entitlementNotInSubscriptionPackage'}"><li>Der Bestandstitel mit Identifikator ${error.getValue()} ist nicht im angegebenen Paket enthalten</li></g:if>
                                            <g:if test="${error.getKey() == 'multipleOrderError'}"><li>Zur Auftragsnummer ${error.getValue()} wurden mehrere Treffer gefunden</li></g:if>
                                            <g:if test="${error.getKey() == 'multipleInvoiceError'}"><li>Zur Rechnungsnummer ${error.getValue()} wurden mehrere Treffer gefunden</li></g:if>
                                            <g:if test="${error.getKey() == 'noCurrencyError'}"><li>Keine Währung angegeben</li></g:if>
                                            <g:if test="${error.getKey() == 'invalidCurrencyError'}"><li>Ungültige Währung angegeben</li></g:if>
                                            <g:if test="${error.getKey() == 'invoiceTotalInvalid'}"><li>Rechnungssumme konnte nicht verarbeitet werden</li></g:if>
                                            <g:if test="${error.getKey() == 'valueInvalid'}"><li>Wert (in EUR) konnte nicht verarbeitet werden</li></g:if>
                                            <g:if test="${error.getKey() == 'exchangeRateInvalid'}"><li>Umrechnungskurs konnte nicht verarbeitet werden</li></g:if>
                                            <g:if test="${error.getKey() == 'invoiceTotalMissing'}"><li>Rechnungssumme war nicht angegeben</li></g:if>
                                            <g:if test="${error.getKey() == 'valueMissing'}"><li>Wert (in EUR) war nicht angegeben</li></g:if>
                                            <g:if test="${error.getKey() == 'exchangeRateMissing'}"><li>Umrechnungskurs war nicht angegeben</li></g:if>
                                            <g:if test="${error.getKey() == 'invoiceTotalCalculated'}"><li>fehlende Rechnungssumme wurde aus Wert (in EUR) und Umrechnungskurs errechnet</li></g:if>
                                            <g:if test="${error.getKey() == 'valueCalculated'}"><li>fehlender Wert (in EUR) wurde aus Rechnungssumme und Umrechnungskurs errechnet</li></g:if>
                                            <g:if test="${error.getKey() == 'exchangeRateCalculated'}"><li>fehlender Umrechnungskurs wurde aus Wert (in EUR) und Rechnungssumme errechnet</li></g:if>
                                            <g:if test="${error.getKey() == 'invalidTaxType'}"><li>Ungültiger Steuersatz/Steuertyp</li></g:if>
                                            <g:if test="${error.getKey() == 'invalidYearFormat'}"><li>Ungültiges Format für Haushaltsjahr</li></g:if>
                                            <g:if test="${error.getKey() == 'noValidStatus'}"><li>Status ist nicht im definierten Wertebereich</li></g:if>
                                            <g:if test="${error.getKey() == 'noValidElement'}"><li>Kostenelement ist nicht im definierten Wertebereich</li></g:if>
                                            <g:if test="${error.getKey() == 'noValidSign'}"><li>Kostenvorzeichen ist nicht im definierten Wertebereich</li></g:if>
                                        </g:each>
                                    </ul>
                                </li>
                            </ul>
                        </td>
                        <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                            <td>
                                <g:if test="${OrgRole.executeQuery('select oo from OrgRole oo where oo.org = :org and oo.sub = :sub and oo.roleType in :roleType',[org: ci.owner,sub: ci.sub,roleType: [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])}">
                                    sichtbar <input name="visibleForSubscriber${r}" type="radio" value="true"><br>nicht sichtbar <input name="visibleForSubscriber${r}" type="radio" value="false" checked>
                                </g:if>
                            </td>
                        </g:if>
                        <td>

                        </td>
                    </tr>
                </g:each>
            </table>
            <input type="submit" class="ui button primary" value="Speichern">
            <g:link action="financeImport" class="ui button">Zurück</g:link>
        </g:form>
    </body>
</html>