<laser:serviceInjection/>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'org.label')}</title>
</head>

<body>
    <laser:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

    <semui:h1HeaderWithIcon text="${orgInstance?.name}" />

    <g:if test="${delResult.status != deletionService.RESULT_SUCCESS}">
        <laser:render template="nav" />
    </g:if>

    <h2 class="ui header">[ Funktionalität unvollständig implementiert ]</h2>

    <g:if test="${delResult}">
        <g:if test="${delResult.status == deletionService.RESULT_SUCCESS}">
            <semui:msg class="positive" header="" message="deletion.success.msg" />
            <g:link controller="organisation" action="listInstitution" class="ui button">${message(code:'menu.public.all_insts')}</g:link>
        </g:if>
        <g:else>
            <g:if test="${delResult.status == deletionService.RESULT_SUBSTITUTE_NEEDED}">
                <semui:msg class="info" header="" message="org.delete.info2" />
            </g:if>
            <g:else>
                <semui:msg class="info" header="" message="org.delete.info" />
            </g:else>

            <g:if test="${delResult.status == deletionService.RESULT_BLOCKED}">
                <semui:msg class="negative" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.org" />
            </g:if>
            <g:if test="${delResult.status == deletionService.RESULT_ERROR}">
                <semui:msg class="negative" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
            </g:if>

            <g:link controller="organisation" action="listInstitution" class="ui button">${message(code:'menu.public.all_insts')}</g:link>
            <g:link controller="organisation" action="show" params="${[id: orgInstance.id]}" class="ui button"><g:message code="default.button.cancel.label"/></g:link>

            <g:if test="${editable}">
                <g:form controller="organisation" action="delete" params="${[id: orgInstance.id, process: true]}" style="display:inline-block;vertical-align:top">

                    <g:if test="${delResult.deletable}">
                        <g:if test="${delResult.status == deletionService.RESULT_SUBSTITUTE_NEEDED}">
                            <p>Löschen mit Datenübertrag wird noch nicht unterstützt.</p>
                            <%--<input type="submit" class="ui button red" value="Organisation löschen" />

                            <br /><br />
                            Beim Löschen relevante Daten an folgende Organisation übertragen:

                            <g:select id="orgReplacement" name="orgReplacement" class="ui dropdown selection"
                                      from="${substituteList.sort()}"
                                      optionKey="${{Org.class.name + ':' + it.id}}" optionValue="${{(it.sortname ?: it.shortname) + ' (' + it.name + ')'}}" />
                                      --%>
                        </g:if>
                        <g:elseif test="${delResult.status != deletionService.RESULT_ERROR}">
                            <input type="submit" class="ui button red" value="${message(code:'deletion.org')}" />
                        </g:elseif>
                    </g:if>
                    <g:else>
                        <input disabled type="submit" class="ui button red" value="${message(code:'deletion.org')}" />
                    </g:else>
                </g:form>
            </g:if>

        </g:else>

        <%-- --%>

        <table class="ui celled la-js-responsive-table la-table compact table">
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
