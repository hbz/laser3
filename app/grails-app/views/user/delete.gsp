<g:set var="deletionService" bean="deletionService" />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'user.label')}</title>
</head>

<body>
    <g:render template="breadcrumb" model="${[ user:user, params:params ]}"/>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
        ${user.username} : ${user.displayName?:'No username'}
    </h1>

    <g:if test="${dryRun}">
        <semui:msg class="info" header="" message="user.delete.info" />
        <br />

        <g:if test="${editable}">
            <g:form controller="user" action="_delete" params="${[id: user.id, process: true]}">
                <g:link controller="user" action="edit" params="${[id: user.id]}" class="ui button">Vorgang abbrechen</g:link>
                <input type="submit" class="ui button red" value="Benutzer löschen" />
                <br />
                <br />
                <p>
                    Gekennzeichnete Daten dabei an folgenden Nutzer übertragen:

                    <g:select id="userReplacement" name="userReplacement" class="ui dropdown selection"
                          from="${ctxOrgUserList.sort()}"
                          optionKey="${{'com.k_int.kbplus.auth.User:' + it.id}}" optionValue="${{it.displayName + ' (' + it.username + ')'}}" />
                </p>
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
                            <span class="ui circular label ${info[2]}"
                                <g:if test="${info[2] == 'blue'}">
                                    data-tooltip="${message(code:'user.delete.moveToNewUser')}"
                                </g:if>
                                <g:if test="${info[2] == 'teal'}">
                                    data-tooltip="${message(code:'user.delete.moveToNewUser')}"
                                </g:if>
                            >${info[1].size()}</span>
                        </g:if>
                        <g:else>
                            ${info[1].size()}
                        </g:else>
                    </td>
                    <td>
                        ${info[1].collect{ item -> item.hasProperty('id') ? item.id : 'x'}.join(', ')}
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
