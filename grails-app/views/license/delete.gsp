<laser:htmlStart message="license.label" serviceInjection="true"/>

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:h1HeaderWithIcon text="${license.reference}" />

    <g:if test="${delResult.status != deletionService.RESULT_SUCCESS}">
        <laser:render template="nav" />
    </g:if>

    <g:if test="${delResult}">
        <g:if test="${delResult.status == deletionService.RESULT_SUCCESS}">
            <semui:msg class="positive" header="" message="deletion.success.msg" />
            <g:link controller="myInstitution" action="currentLicenses" class="ui button">${message(code:'menu.my.licenses')}</g:link>
        </g:if>
        <g:else>
            <semui:msg class="info" header="" message="license.delete.info" />

            <g:if test="${delResult.status == deletionService.RESULT_BLOCKED}">
                <semui:msg class="negative" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.license" />
            </g:if>
            <g:if test="${delResult.status == deletionService.RESULT_ERROR}">
                <semui:msg class="negative" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
            </g:if>

            <g:link controller="myInstitution" action="currentLicenses" class="ui button">${message(code:'menu.my.licenses')}</g:link>
            <g:link controller="license" action="show" params="${[id: license.id]}" class="ui button"><g:message code="default.button.cancel.label"/></g:link>

            <g:if test="${editable}">
                <g:if test="${delResult.deletable}">
                    <g:link controller="license" action="delete" params="${[id: license.id, process: true]}" class="ui button red">${message(code:'deletion.license')}</g:link>
                </g:if>
                <g:else>
                    <input disabled type="submit" class="ui button red" value="${message(code:'deletion.license')}" />
                </g:else>
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

<laser:htmlEnd />
