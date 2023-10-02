<%@ page import="de.laser.utils.LocaleUtils; de.laser.reporting.export.LocalExportHelper;" %>
<laser:serviceInjection />
<%
    String lang = (LocaleUtils.getCurrentLang() == 'en') ? 'en' : 'de'

    Map<String, Map> text = [
            'timeline-member' : [
                    'de' : [
                            'Die Abfrage visualisiert die zeitliche Entwicklung der Einrichtungen für diese Lizenz',
                            'Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit ihren jeweiligen Datumsgrenzen. Für jede Lizenz sind mögliche Veränderungen, bezogen auf den Vorgänger, wie folgt erkennbar:',
                            'Einrichtungen hinzugefügt',
                            'Einrichtungen entfernt',
                            'Aktuelle Einrichtungen'
                    ],
                    'en' : [
                            'The query visualizes the chronological development of the participants for this subscription.',
                            'All relevant subscriptions (referenced predecessors or successors) are listed with their respective date limits. For each subscription, possible changes related to the predecessor can be identified as follows:',
                            'Participant added',
                            'Participant removed',
                            'Current Participants'
                    ]
            ],
            'timeline-member-cost' : [
                    'de' : [
                            'Die Abfrage visualisiert die zeitliche Entwicklung der Einrichtungskosten für diese Lizenz',
                            'Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit ihren jeweiligen Datumsgrenzen. ' +
                                    'Über betroffene Einrichtungslizenzen und existierende Kosten werden folgende Informationen berechnet - eigene Kosten werden dabei ignoriert:',
                            'Kumulierter Endpreis in Euro (nach Steuern)',
                            'Kumulierter Wert in Euro',
                            'Neutrale Kosten in Euro (nach Steuern)',
                            'Neutrale Kosten in Euro'
                    ],
                    'en' : [
                            'The query visualizes the development of the participants costs for this subscription over time',
                            'All relevant subscriptions (referenced predecessors or successors) are listed with their respective date limits. ' +
                                    'The following information is calculated on affected participant subscriptions and existing cost items - own costs will be ignored:',
                            'Accumulated final price in euros (after taxes)',
                            'Accumulated value in euros',
                            'Neutral costs in euros (after taxes)',
                            'Neutral costs in euros'
                    ]
            ],
            'timeline-participant-cost' : [
                    'de' : [
                            'Die Abfrage visualisiert die zeitliche Entwicklung der Teilnahmekosten für diese Lizenz',
                            'Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit ihren jeweiligen Datumsgrenzen. ' +
                                    'Über existierende Teilnahmekosten werden folgende Informationen berechnet - eigene Kosten werden dabei ignoriert:',
                            'Kumulierter Endpreis in Euro (nach Steuern)',
                            'Kumulierter Wert in Euro',
                            'Neutrale Kosten in Euro (nach Steuern)',
                            'Neutrale Kosten in Euro'
                    ],
                    'en' : [
                            'The query visualizes the development of the member costs for this subscription over time',
                            'All relevant subscriptions (referenced predecessors or successors) are listed with their respective date limits. ' +
                                    'The following information is calculated on existing cost items - own costs will be ignored:',
                            'Accumulated final price in euros (after taxes)',
                            'Accumulated value in euros',
                            'Neutral costs in euros (after taxes)',
                            'Neutral costs in euros'
                    ]
            ],
            'timeline-entitlement' : [
                    'de' : [
                            'Die Abfrage visualisiert die zeitliche Entwicklung des Bestands für diese Lizenz',
                            'Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit ihren jeweiligen Datumsgrenzen. Für jede Lizenz sind mögliche Veränderungen, bezogen auf den Vorgänger, wie folgt erkennbar:',
                            'Titel hinzugefügt',
                            'Titel entfernt',
                            'Aktueller Bestand'
                    ],
                    'en' : [
                            'The query visualizes the development of the entitlements for this subscription over time',
                            'All relevant subscriptions (referenced predecessors or successors) are listed with their respective date limits. For each subscription, possible changes in relation to the predecessor can be identified as follows:',
                            'Title added',
                            'Title removed',
                            'Current entitlements'
                    ]
            ],
            'timeline-annualMember-subscription' : [
                    'de' : [
                            'Die Abfrage visualisiert die zeitliche Entwicklung aller für diese Lizenz relevanten Einrichtungslizenzen',
                            'Gruppiert werden die Einrichtungslizenzen in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.',
                            'TODO',
                            'über ggf. vorhandene Vorgänger oder Nachfolger referenzierte Einrichtungslizenzen dagegen schon.'
                    ],
                    'en' : [
                            'The query visualizes the chronological development of all participant subscriptions relevant to this subscription',
                            'The participant subscriptions are grouped in annual rings - depending on the respective date boundaries.',
                            'TODO',
                            'On the other hand, participant subscriptions referenced via any predecessors or successors do.'
                    ]
            ],
    ]
%>


<!-- _helpModal.gsp -->
<ui:infoModal id="${modalID}">

    <div class="help-section" data-help-section="timeline-member">
        <p class="ui header">
            ${text['timeline-member'][lang][0]}
        </p>
        <p>
            ${text['timeline-member'][lang][1]}
        </p>
        <p>
            <i class="icon circle green"></i> ${text['timeline-member'][lang][2]}, <br />
            <i class="icon circle red"></i> ${text['timeline-member'][lang][3]}, <br />
            <i class="icon circle blue"></i> ${text['timeline-member'][lang][4]}
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-member-cost">
        <p class="ui header">
            ${text['timeline-member-cost'][lang][0]}
        </p>
        <p>
            ${text['timeline-member-cost'][lang][1]}
        </p>
        <p>
            <i class="icon circle blue"></i> ${text['timeline-member-cost'][lang][2]}, <br />
            <i class="icon circle green"></i> ${text['timeline-member-cost'][lang][3]}, <br />
            <i class="icon circle orange"></i> ${text['timeline-member-cost'][lang][4]}, <br />
            <i class="icon circle yellow"></i> ${text['timeline-member-cost'][lang][5]}
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-participant-cost">
        <p class="ui header">
            ${text['timeline-participant-cost'][lang][0]}
        </p>
        <p>
            ${text['timeline-participant-cost'][lang][1]}
        </p>
        <p>
            <i class="icon circle blue"></i> ${text['timeline-participant-cost'][lang][2]}, <br />
            <i class="icon circle green"></i> ${text['timeline-participant-cost'][lang][3]}, <br />
            <i class="icon circle orange"></i> ${text['timeline-participant-cost'][lang][4]}, <br />
            <i class="icon circle yellow"></i> ${text['timeline-participant-cost'][lang][5]}
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-entitlement">
        <p class="ui header">
            ${text['timeline-entitlement'][lang][0]}
        </p>
        <p>
            ${text['timeline-entitlement'][lang][1]}
        </p>
        <p>
            <i class="icon circle green"></i> ${text['timeline-entitlement'][lang][2]}, <br />
            <i class="icon circle red"></i> ${text['timeline-entitlement'][lang][3]}, <br />
            <i class="icon circle blue"></i> ${text['timeline-entitlement'][lang][4]}
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-annualMember-subscription">
        <p class="ui header">
            ${text['timeline-annualMember-subscription'][lang][0]}
        </p>
        <p>
            ${text['timeline-annualMember-subscription'][lang][1]}
        </p>
        <p>
            <g:if test="${lang == 'de'}">
                Start- und Enddatum <strong>in dieser Lizenz</strong> werden bei der Abfrage nicht beachtet; <br />
            </g:if>
            <g:if test="${lang == 'en'}">
                Start and end dates <strong>in this subscription</strong> are ignored when querying; <br />
            </g:if>
            ${text['timeline-annualMember-subscription'][lang][3]}
        </p>
    </div>

    <div class="help-section" data-help-section="default">
        ${message(code:'reporting.ui.global.help.missing')}
    </div>
</ui:infoModal>

<style>
#queryHelpModal .items .item {
    padding: 1em;
}
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.callbacks.modal.onShow['${modalID}'] = function(trigger) {
        $('#${modalID} .help-section').hide();
        $current = $('#${modalID} .help-section[data-help-section=' + JSPC.app.reporting.current.request.query + ']');
        if (! $current.length) {
            $current = $('#${modalID} .help-section[data-help-section=default]');
        }
        $current.show();
    }
</laser:script>
<!-- _helpModal.gsp -->

