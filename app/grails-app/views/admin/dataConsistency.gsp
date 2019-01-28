<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} - ${message(code: "menu.admin.dataConsistency")}</title>
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
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${obj.value}" var="row">
                            <tr>
                                <td>${row[0]}</td>
                                <td>${row[1]}</td>
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

</body>
</html>
