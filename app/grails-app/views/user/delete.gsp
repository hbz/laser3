<g:set var="deletionService" bean="deletionService" />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'user.label')}</title>
</head>

<body>
    <g:render template="breadcrumb" model="${[ user:user, params:params ]}"/>
    <br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        ${user?.username} : ${user?.displayName?:'Nutzer unbekannt'}
    </h1>

    <g:if test="${dryRun}">
        <g:if test="${dryRun?.status == deletionService.RESULT_SUBSTITUTE_NEEDED}">
            <semui:msg class="info" header="" message="user.delete.info2" />
        </g:if>
        <g:else>
            <semui:msg class="info" header="" message="user.delete.info" />
        </g:else>
        <br />

        <g:if test="${editable}">
            <g:form controller="user" action="_delete" params="${[id: user.id, process: true]}">
                <g:link controller="user" action="edit" params="${[id: user.id]}" class="ui button">Vorgang abbrechen</g:link>

                <g:if test="${dryRun?.deletable}">
                    <input type="submit" class="ui button red" value="Benutzer löschen" />

                    <g:if test="${dryRun?.status == deletionService.RESULT_SUBSTITUTE_NEEDED}">
                        <br /><br />
                        Die gekennzeichneten Daten dabei an folgenden Nutzer übertragen:

                        <g:select id="userReplacement" name="userReplacement" class="ui dropdown selection"
                              from="${substituteList.sort()}"
                              optionKey="${{'com.k_int.kbplus.auth.User:' + it.id}}" optionValue="${{it.displayName + ' (' + it.username + ')'}}" />
                    </g:if>
                </g:if>
                <g:else>
                    <input disabled type="submit" class="ui button red" value="Benutzer löschen" />
                </g:else>
            </g:form>
        </g:if>

        <br />

        <table class="ui celled la-table la-table-small table">
            <thead>
            <tr>
                <th>Anhängende, bzw. referenzierte Objekte</th>
                <th>Anzahl</th>
                <th>Objekt-Ids</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${dryRun.info.sort{ a,b -> a[0] <=> b[0] }}" var="info">
                <tr>
                    <td>
                        ${info[0]}
                    </td>
                    <td style="text-align:center">
                        <g:if test="${info.size() > 2 && info[1].size() > 0}">
                            <span class="ui circular label la-popup-tooltip la-delay ${info[2]}"
                                <g:if test="${info[2] == 'blue'}">
                                    data-content="${message(code:'user.delete.moveToNewUser')}"
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

    <g:if test="${result?.status == deletionService.RESULT_SUCCESS}">
        <semui:msg class="positive" header=""
                   text="Löschvorgang wurde erfolgreich durchgeführt." />
        <g:link controller="organisation" action="users" params="${[id: contextService.getOrg()?.id]}" class="ui button">Nutzerverwaltung</g:link>
    </g:if>
    <g:if test="${result?.status == deletionService.RESULT_QUIT}">
        <semui:msg class="negative" header="Löschvorgang abgebrochen"
                   text="Es existieren Referenzen. Diese müssen zuerst gelöscht werden." />
        <g:link controller="user" action="delete" params="${[id: user.id]}" class="ui button">Zur Übersicht</g:link>
    </g:if>
    <g:if test="${result?.status == deletionService.RESULT_ERROR}">
        <semui:msg class="negative" header="Unbekannter Fehler"
                   text="Der Löschvorgang wurde abgebrochen." />
        <g:link controller="user" action="delete" params="${[id: user.id]}" class="ui button">Zur Übersicht</g:link>
    </g:if>

</body>
</html>
