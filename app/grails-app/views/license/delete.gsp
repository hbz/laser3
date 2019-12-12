<g:set var="deletionService" bean="deletionService" />
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'license.label')}</title>
</head>

<body>
    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    <br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        ${license.reference}
    </h1>

    <g:if test="${deletionService.RESULT_SUCCESS != delResult?.status}">
        <g:render template="nav" />
    </g:if>

    <g:if test="${delResult}">
        <semui:msg class="info" header="" message="license.delete.info" />

        <%-- --%>

        <g:if test="${delResult.status == deletionService.RESULT_BLOCKED}">
            <semui:msg class="negative" header="Löschvorgang blockiert"
                       text="Es existieren Teilnehmerverträge. Diese müssen zuerst gelöscht werden." />
            <g:link controller="myInstitution" action="currentSubscriptions" class="ui button">Meine Lizenzen</g:link>
        </g:if>
        <g:if test="${delResult.status == deletionService.RESULT_SUCCESS}">
            <semui:msg class="positive" header=""
                       text="Löschvorgang wurde erfolgreich durchgeführt." />
            <g:link controller="myInstitution" action="currentLicenses" class="ui button">Meine Verträge</g:link>
        </g:if>
        <g:if test="${delResult.status == deletionService.RESULT_ERROR}">
            <semui:msg class="negative" header="Unbekannter Fehler"
                       text="Der Löschvorgang wurde abgebrochen." />
            <g:link controller="license" action="delete" params="${[id: license.id]}" class="ui button">Zur Übersicht</g:link>
        </g:if>

        <g:link controller="license" action="show" params="${[id: license.id]}" class="ui button">Vorgang abbrechen</g:link>

        <g:if test="${editable}">
            <g:if test="${delResult.deletable}">
                <g:link controller="license" action="delete" params="${[id: license.id, process: true]}" class="ui button red">Vertrag löschen</g:link>
            </g:if>
            <g:else>
                <input disabled type="submit" class="ui button red" value="Vertrag löschen" />
            </g:else>
        </g:if>

        <%-- --%>

        <table class="ui celled la-table la-table-small table">
            <thead>
            <tr>
                <th>Anhängende, bzw. referenzierte Objekte</th>
                <th>Anzahl</th>
                <th>Objekt-Ids</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${delResult.info.sort{ a,b -> a[0] <=> b[0] }}" var="info">
                <tr>
                    <td>
                        ${info[0]}
                    </td>
                    <td style="text-align:center">
                        <g:if test="${info.size() > 2 && info[1].size() > 0}">
                            <span class="ui circular label la-popup-tooltip la-delay ${info[2]}"
                                <g:if test="${info[2] == 'red'}">
                                    data-content="${message(code:'license.delete.blocker')}"
                                </g:if>
                            >${info[1].size()}</span>
                        </g:if>
                        <g:else>
                            ${info[1].size()}
                        </g:else>
                    </td>
                    <td>
                        <div style="overflow-y:scroll;scrollbar-color:grey white;max-height:14.25em">
                            ${info[1].collect{ item -> item.hasProperty('id') ? item.id : 'x'}.sort().join(', ')}
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>

</body>
</html>
