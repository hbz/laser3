<%@ page import="de.laser.ui.Btn; de.laser.Subscription; de.laser.IssueEntitlement; grails.converters.JSON" %>

<laser:htmlStart text="Paket-Duplikate" serviceInjection="true" />

        <g:set var="toDelete" value="${[]}"/>

        <ui:h1HeaderWithIcon text="Pakete mit gleicher we:kb-ID" />

        <ui:messages data="${flash}" />

            <div class="ui grid">
                <div class="row">
                    <div class="sixteen wide column">
                        <h2 class="ui header">Pakete mit gleicher we:kb ID</h2>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        LAS:eR ID
                    </div>
                    <div class="four wide column">
                        we:kb ID
                    </div>
                    <div class="eight wide column">
                        Name
                    </div>
                </div>
                <g:each in="${pkgDuplicates}" var="duplicate">
                    <div class="row">
                        <div class="four wide column">
                            ${duplicate.id}
                        </div>
                        <div class="four wide column">
                            ${duplicate.gokbId}
                        </div>
                        <div class="eight wide column">
                            ${duplicate.name}
                        </div>
                    </div>
                </g:each>
            </div>
            <div class="ui grid">
                <div class="row">
                    <div class="sixteen wide column">
                        <h2 class="ui header">Pakete <strong>ohne</strong> TIPPs</h2>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        LAS:eR ID
                    </div>
                    <div class="four wide column">
                        we:kb ID
                    </div>
                    <div class="eight wide column">
                        Name
                    </div>
                </div>
                <g:each in="${pkgDupsWithoutTipps}" var="duplicate">
                    <div class="row">
                        <div class="four wide column">
                            ${duplicate.id}
                        </div>
                        <div class="four wide column">
                            ${duplicate.gokbId}
                        </div>
                        <div class="eight wide column">
                            ${duplicate.name}
                        </div>
                        <%
                            toDelete << duplicate.id
                        %>
                    </div>
                </g:each>
            </div>
            <div class="ui grid">
                <div class="row">
                    <div class="sixteen wide column">
                        <h2 class="ui header">Pakete <strong>mit</strong> TIPPs</h2>
                    </div>
                </div>
                <div class="row">
                    <div class="three wide column">
                        LAS:eR ID
                    </div>
                    <div class="three wide column">
                        we:kb ID
                    </div>
                    <div class="two wide column">
                        Name
                    </div>
                    <div class="eight wide column">
                        In Lizenzbeständen?
                    </div>
                </div>
                <g:each in="${pkgDupsWithTipps}" var="duplicate">
                    <div class="row">
                        <div class="three wide column">
                            ${duplicate.id}
                        </div>
                        <div class="three wide column">
                            ${duplicate.gokbId}
                        </div>
                        <div class="two wide column">
                            ${duplicate.name}
                        </div>
                        <div class="eight wide column">
                            <%
                                List<Subscription> concernedSubs = Subscription.executeQuery('select distinct(ie.subscription) from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg',[pkg:duplicate])
                            %>
                            <g:each in="${concernedSubs}" var="sub">
                                ${sub.dropdownNamingConvention(contextService.getOrg())}<br />
                            </g:each>
                            <g:if test="${!concernedSubs}">
                                Keine Lizenz anhängig, freigegeben zum Löschen!
                                <%
                                    toDelete << duplicate.id
                                %>
                            </g:if>
                        </div>
                    </div>
                </g:each>
            </div>
            <div class="ui grid">
                <div class="row">
                    <div class="sixteen wide column">
                        <g:link action="purgeDuplicatePackages" params="${[doIt: true, toDelete: toDelete as JSON]}" class="${Btn.NEGATIVE_CONFIRM}" data-confirm-tokenMsg = "${message(code: 'confirmation.content.deleteDuplicatePackages')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                        <g:link action="purgeDuplicatePackages" params="${[doIt: false, toDelete: toDelete as JSON]}" class="${Btn.SIMPLE}">Testlauf (gefahrlos)</g:link>
                    </div>
                </div>
            </div>

<laser:htmlEnd />