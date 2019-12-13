<g:set var="deletionService" bean="deletionService" />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'org.label')}</title>
</head>

<body>
    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>
    <br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        ${orgInstance?.name}
    </h1>

    <g:if test="${deletionService.RESULT_SUCCESS != result?.status}">
        <g:render template="nav" />
    </g:if>

    <h3>[ Funktionalität unvollständig implementiert ]</h3>

    <g:if test="${delResult}">
        <g:if test="${delResult.status == deletionService.RESULT_SUBSTITUTE_NEEDED}">
            <semui:msg class="info" header="" message="org.delete.info2" />
        </g:if>
        <g:else>
            <semui:msg class="info" header="" message="org.delete.info" />
        </g:else>

        <%-- --%>

        <g:if test="${delResult.status == deletionService.RESULT_BLOCKED}">
            <semui:msg class="negative" header="Löschvorgang blockiert"
                       text="Es existieren relevante Verknüpfungen. Diese müssen zuerst gelöscht werden." />
            <g:link controller="organisation" action="_delete" params="${[id: orgInstance.id]}" class="ui button">Zur Übersicht</g:link>
        </g:if>
        <g:if test="${delResult.status == deletionService.RESULT_SUCCESS}">
            <semui:msg class="positive" header=""
                       text="Löschvorgang wurde erfolgreich durchgeführt." />
            <g:link controller="organisation" action="listInstitution" class="ui button">Alle Einrichtungen</g:link>
        </g:if>
        <g:if test="${delResult.status == deletionService.RESULT_ERROR}">
            <semui:msg class="negative" header="Unbekannter Fehler"
                       text="Der Löschvorgang wurde abgebrochen." />
            <g:link controller="organisation" action="_delete" params="${[id: orgInstance.id]}" class="ui button">Zur Übersicht</g:link>
        </g:if>

        <g:if test="${editable}">
            <g:form controller="organisation" action="_delete" params="${[id: orgInstance.id, process: true]}" style="display:inline-block;">
                <g:link controller="organisation" action="show" params="${[id: orgInstance.id]}" class="ui button">Vorgang abbrechen</g:link>

                <g:if test="${delResult.deletable}">
                    <input type="submit" class="ui button red" value="Organisation löschen" />

                    <g:if test="${delResult.status == deletionService.RESULT_SUBSTITUTE_NEEDED}">

                        <br /><br />
                        Die gekennzeichneten Daten dabei an folgende Organisation übertragen:

                        <g:select id="orgReplacement" name="orgReplacement" class="ui dropdown selection"
                                  from="${substituteList.sort()}"
                                  optionKey="${{'com.k_int.kbplus.auth.Org:' + it.id}}" optionValue="${{(it.sortname ?: it.shortname) + ' (' + it.name + ')'}}" />
                    </g:if>
                </g:if>
                <g:else>
                    <input disabled type="submit" class="ui button red" value="Organisation löschen" />
                </g:else>
            </g:form>
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
                                    data-content="${message(code:'subscription.delete.blocker')}"
                                </g:if>
                                <g:if test="${info[2] == 'blue'}">
                                    data-content="${message(code:'org.delete.moveToNewOrg')}"
                                </g:if>
                                <g:if test="${info[2] == 'yellow'}">
                                    data-content="${message(code:'subscription.existingCostItems.warning')}"
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
