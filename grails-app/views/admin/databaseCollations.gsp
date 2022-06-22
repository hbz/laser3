<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code: "menu.admin.databaseCollations")}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.databaseCollations" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top">${message(code: "menu.admin.databaseCollations")}</h1>

<div class="ui la-float-right">
    <select id="filter" class="ui dropdown">
        <option value="all">Alle anzeigen</option>
        <option value="positive">${laser_german_phonebook}</option>
        <option value="default">default</option>
    </select>
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#filter').on('change', function() {
            var baseSel = 'div[data-tab=first] table tbody tr'
            var selection = $(this).val()
            if (selection != 'all') {
                $(baseSel).addClass('hidden')
                $(baseSel + '.' + selection).removeClass('hidden')
            } else {
                $(baseSel).removeClass('hidden')
            }
        })
    </laser:script>
</div>

<div class="ui secondary stackable pointing tabular la-tab-with-js menu">
    <a data-tab="first" class="item active">Übersicht</a>
    <a data-tab="second" class="item">Beispiele</a>
</div>

<div data-tab="first" class="ui bottom attached tab segment active" style="border-top: 1px solid #d4d4d5;">

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr>
                <th>#</th>
                <th>Schema</th>
                <th>Tabelle</th>
                <th>Feld</th>
                <th>Typ</th>
                <th>Collation</th>
            </tr>
        </thead>
        <tbody>

        <g:each in="${table_columns}" var="row" status="i">
            <g:if test="${row[6] == 'laser_german_phonebook'}">
                <tr class="positive">
                    <td>${i+1}</td>
                    <td><strong>${row[0]}</strong></td>
                    <td><strong>${row[1]}</strong></td>
                    <td><strong>${row[2]}</strong></td>
                    <td><strong>${row[3]}</strong></td>
                    <td><i class="icon sort alphabet down"></i> <strong>${laser_german_phonebook}</strong></td>
                </tr>
            </g:if>
            <g:else>
                <tr class="default">
                    <td>${i+1}</td>
                    <td>${row[0]}</td>
                    <td>${row[1]}</td>
                    <td>${row[2]}</td>
                    <td>${row[3]}</td>
                    <g:if test="${row[4] || row[5] || row[6]}">
                        <td>${row[4]} : ${row[5]} : ${row[6]}</td>
                    </g:if>
                    <g:else>
                        <td class="disabled">default</td>
                    </g:else>
                </tr>
            </g:else>
        </g:each>

        </tbody>
    </table>

</div>
<div data-tab="second" class="ui bottom attached tab segment" style="border-top: 1px solid #d4d4d5;">

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr>
                <th>${laser_german_phonebook}</th>
                <th>default <em>( ${default_collate} )</em></th>
            </tr>
        </thead>
        <tbody>
        <g:each in="${examples['default']}" var="x" status="i">
            <tr>
                <td>${examples['laser_german_phonebook'][i]}</td>
                <td>${examples['default'][i]}</td>
            </tr>
        </g:each>
        </tbody>
    </table>

</div>

</body>

</html>
