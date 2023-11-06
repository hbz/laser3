<laser:htmlStart message="menu.admin.mergeOrganisations" serviceInjection="true"/>

    <ui:h1HeaderWithIcon text="${message(code: 'menu.admin.mergeOrganisations')}" />

    <g:if test="${mergeResult}">
        <g:if test="${mergeResult.status == orgnaisationService.RESULT_SUCCESS}">
            <ui:msg class="positive" message="deletion.success.msg" />
        </g:if>
        <g:else>
            <g:if test="${mergeResult.status == organisationService.RESULT_BLOCKED}">
                <ui:msg class="negative" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.subscription" />
            </g:if>
            <g:if test="${mergeResult.status == organisationService.RESULT_ERROR}">
                <ui:msg class="negative" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
            </g:if>
        </g:else>
    </g:if>

        <%-- --%>
    <g:form controller="admin" action="mergeOrganisations">
        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
                <tr>
                    <th colspan="3">
                        <label for="source">${message(code: 'default.agency.provider.plural.label')}</label>
                        <div class="ui search selection fluid dropdown org" id="source">
                            <input type="hidden" name="source"/>
                            <div class="default text"><g:message code="default.select.source.label"/></div>
                            <i class="dropdown icon"></i>
                        </div>
                    </th>
                    <th colspan="3">
                        <label for="target">${message(code: 'default.agency.provider.plural.label')}</label>
                        <div class="ui search selection fluid dropdown org" id="target">
                            <input type="hidden" name="target"/>
                            <div class="default text"><g:message code="default.select.target.label"/></div>
                            <i class="dropdown icon"></i>
                        </div>
                    </th>
                </tr>
                <tr>
                    <th class="sourceHeader" hidden="hidden">Anhängende, bzw. referenzierte Objekte</th>
                    <th class="sourceHeader" hidden="hidden">${message(code:'default.count.label')}</th>
                    <th class="sourceHeader" hidden="hidden">Objekt-Ids</th>
                    <th class="targetHeader" hidden="hidden">Anhängende, bzw. referenzierte Objekte</th>
                    <th class="targetHeader" hidden="hidden">${message(code:'default.count.label')}</th>
                    <th class="targetHeader" hidden="hidden">Objekt-Ids</th>
                </tr>
            </thead>
            <tbody>
                <%--
                <g:each in="${mergeResult.info.sort{ a,b -> a[0] <=> b[0] }}" var="info" status="key">
                    <tr id="row${key}">
                        <g:render template="mergeTableRow" model="[info: info]"/>
                    </tr>
                </g:each>
                --%>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="6"><g:submitButton name="validate" class="ui button primary" value="Gewählte Einrichtungen zusammenführen"/></td>
                </tr>
            </tfoot>
        </table>
    </g:form>

    <laser:script>
        $('.org').each(function() {
            $(this).dropdown({
                apiSettings: {
                    url: '<g:createLink controller="ajaxJson" action="lookupProvidersAgencies"/>?query={query}&displayWekbFlag=true',
                    cache: false
                },
                clearable: true,
                minCharacters: 0
            });
        });

        $('.org').change(function() {
            //console.log("selected org: "+$(this).dropdown('get value')+", please continue implementing!");
            let data = {};
            let success;
            if($(this).attr('id') === 'source') {
                data.source = $(this).dropdown('get value');
                success = function(response) {
                    $.each(response.info, function(i, info) {
                        let tableRow = $('tr#row'+i);
                        if(tableRow.length === 0) {
                            $('tbody').append('<tr id="row'+i+'"></tr>');
                            tableRow = $('tr#row'+i);
                        }
                        tableRow.html('<td>'+info[0]+'</td>');
                        tableRow.append('<td>'+info[1].length+'</td>');
                        let objects = [];
                        $.each(info[1], function(j, objInfo) {
                            objects.push(objInfo.id);
                        });
                        objects.sort();
                        tableRow.append('<td>'+objects.join(', ')+'</td>');
                    });
                };
            }
            else if($(this).attr('id') === 'target') {
                data.target = $(this).dropdown('get value');
                success = function(response) {
                    $.each(response.info, function(i, info) {
                        let tableRow = $('tr#row'+i);
                        let objects = [];
                        $.each(info[1], function(j, objInfo) {
                            objects.push(objInfo.id);
                        });
                        objects.sort();
                        if(tableRow.length === 0) {
                            $('tbody').append('<tr id="row'+i+'"><td></td><td></td><td></td></tr>');
                            tableRow = $('tr#row'+i);
                        }
                        else if(tableRow.find('td').length === 3) {
                            tableRow.append('<td>'+info[0]+'</td>');
                            tableRow.append('<td>'+info[1].length+'</td>');
                            tableRow.append('<td>'+objects.join(', ')+'</td>');
                        }
                        else {
                            tableRow.find('td:nth-child(4)').text(info[0]);
                            tableRow.find('td:nth-child(5)').text(info[1].length);
                            tableRow.find('td:nth-child(6)').text(objects.join(', '));
                        }
                    });
                };
            }
            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="loadOrganisationForMerge"/>',
                data: data,
                success: success
            });
        });
    </laser:script>

<laser:htmlEnd />
