<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: "menu.admin.dataConsistency")}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.dataConsistency" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header">${message(code: "menu.admin.dataConsistency")}</h1>

<div class="ui grid">
    <div class="twelve wide column">

        <h2 class="ui headerline">${message(code: "admin.duplicateImpIds")}</h2>

        <g:each in="${importIds}" var="obj">
            <g:if test="${obj.value}">

                <h3 class="ui headerline" id="jumpMark_1_${obj.key}">${obj.key} (${obj.value.size()})</h3>

                <table class="ui sortable celled la-table la-table-small ignore-floatThead table">
                    <thead>
                        <tr>
                            <th>ImpId</th>
                            <th>Vorkommen</th>
                            <th>${message(code:'default.actions')}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${obj.value}" var="row">
                            <tr>
                                <td>${row[0]}</td>
                                <td>${row[1]}</td>
                                <td class="x">
                                    <button class="ui button icon" data-key="${obj.key}" data-key2="impId" data-value="${row[0]}"><i class="ui icon search"></i></button>
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>

            </g:if>
        </g:each>

        <h2 class="ui headerline">${message(code: "admin.duplicateNamesAndTitles")}</h2>

        <g:each in="${titles}" var="obj">
            <g:if test="${true}">

                <h3 class="ui headerline" id="jumpMark_2_${obj.key}">${obj.key}</h3>

                <table class="ui sortable celled la-table la-table-small ignore-floatThead table">
                    <thead>
                    <tr>
                        <th>Attribut</th>
                        <th>Wert</th>
                        <th>Vorkommen</th>
                        <th>${message(code:'default.actions')}</th>
                    </tr>
                    </thead>
                    <tbody>
                        <g:each in="${obj.value}" var="row">
                            <g:each in="${row.value}" var="entry">
                                <tr>
                                    <td>${row.key}</td>
                                    <td>${entry[0]}</td>
                                    <td>
                                        ${entry[1]}
                                    </td>
                                    <td class="x">
                                        <button class="ui button icon" data-key="${obj.key}" data-key2="${row.key}" data-value="${entry[0]}"><i class="ui icon search"></i></button>
                                    </td>
                                </tr>
                            </g:each>
                        </g:each>
                    </tbody>
                </table>

            </g:if>
        </g:each>

    </div>
    <div class="four wide column">
        <div class="ui sticky">
            <aside>
                <p>${message(code: "admin.duplicateImpIds")}</p>

                <g:each in="${importIds}" var="obj">
                    <g:if test="${obj.value}">
                        <a href="#jumpMark_1_${obj.key}">${obj.key}</a> <br />
                    </g:if>
                </g:each>

                <br />
                <p>${message(code: "admin.duplicateNamesAndTitles")}</p>

                <g:each in="${titles}" var="obj">
                    <a href="#jumpMark_2_${obj.key}">${obj.key}</a> <br />
                </g:each>
            </aside>
        </div>
    </div>

</div>

<semui:modal id="modalConsistencyCheck" message="menu.admin.dataConsistency" hideSubmitButton="true">
    <form>
        <h4 class="ui header"></h4>
        <br />
        <div class="ui relaxed divided list">
        </div>
    </form>
</semui:modal>

<r:script>
    $('.x button').on('click', function(){

        var key = $(this).attr('data-key')
        var key2 = $(this).attr('data-key2')
        var value = $(this).attr('data-value')

        $.ajax({
            url: '<g:createLink controller="ajax" action="consistencyCheck"/>',
            method: 'POST',
            data: {
                key: key,
                key2: key2,
                value: value
            },
            success: function(res, code, xhr) {
                var $h4 = $('#modalConsistencyCheck form h4')
                $h4.empty().append( key + '.' + key2 + ' = ' + value )

                var $html = $('#modalConsistencyCheck form div')
                $html.empty()

                $.each( res, function( i, elem) {
                    $html.append('<div class="item">' +
                        '<div class="right floated content">' + elem.created + ' | ' + elem.updated + '</div>' +
                        '<a target="_blank" href="' + elem.link + '">( ' + elem.id + ' ) &nbsp; ' + elem.name + '</a>' +
                        '</div>'
                    )
                })

                $('#modalConsistencyCheck').modal('show')
            }
        });
    })
</r:script>

</body>
</html>
