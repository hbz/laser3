<%@ page import="de.laser.helper.MigrationHelper; groovy.sql.GroovyRowResult; de.laser.RefdataValue; de.laser.storage.BeanStorage" %>
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

<div class="ui secondary stackable pointing tabular menu">
    <a data-tab="first" class="item active">Übersicht</a>
    <a data-tab="second" class="item">Test (DE)</a>
    <a data-tab="third" class="item">Test (EN)</a>
</div>

<div data-tab="first" class="ui bottom attached tab active">
    <div class="ui la-float-right">
        <select id="firstFilter" class="ui dropdown la-not-clearable">
            <option value="all" selected="selected">Alle anzeigen</option>
            <option value="current">${collate_current} (default)</option>
            <g:each in="${examples['country'].keySet() - [collate_de, current_de, collate_en, current_en]}" var="key">
                <option value="${key}">${key}</option>
            </g:each>
        </select>
    </div>
    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr>
                <th>#</th>
                <th>Tabelle</th>
                <th>Feld</th>
                <th>Datentyp</th>
                <th>Index</th>
                <th>Collation</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${allTables}" var="row" status="i">
                <tr class="${! row[6] ? 'current' : row[6]}">
                    <td>${i+1}</td>
                    <td>${row[1]}</td>
                    <td>${row[2]}</td>
                    <td>${row[3]}</td>
                    <td>${row[7]}</td>
                    <g:if test="${! row[6]}">
                        <td class="x disabled">${collate_current}</td>
                    </g:if>
                    <g:else>
                        <td class="x">
                            <span class="ui image label">
                            <g:if test="${row[6] == MigrationHelper.DE_U_CO_PHONEBK_X_ICU}">
                                <img class="ui mini" src="${resource(dir:'images', file:'flags/de.svg', absolute:true)}" alt="[DE]" /> ${row[6]}
                            </g:if>
                            <g:elseif test="${row[6] == MigrationHelper.EN_US_U_VA_POSIX_X_ICU}">
                                <img class="ui mini" src="${resource(dir:'images', file:'flags/us.svg', absolute:true)}" alt="[EN]" /> ${row[6]}
                            </g:elseif>
                            <g:else>
                                <img class="ui mini" src="${resource(dir:'images', file:'flags/xx.svg', absolute:true)}" alt="[?]" /> ${row[6]}
                            </g:else>
                        </span>
                        </td>
                    </g:else>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<div data-tab="second" class="ui bottom attached tab">
    <div class="ui la-float-right">
        <select id="secondFilter" class="ui dropdown la-not-clearable">
            <option value="country" selected="selected">Merkmale: Länder</option>
            <option value="ddc">Merkmale: DDC</option>
            <option value="org">Organisationen</option>
            <option value="title">Titel</option>
        </select>
    </div>
    <table class="ui celled la-js-responsive-table la-table compact table">
    <thead>
        <tr>
            <th>#</th>
            <th style="text-transform: none !important;">COLLATE: ${collate_de}</th>
            <th style="text-transform: none !important;">COLLATE: ${MigrationHelper.DE_U_CO_PHONEBK_X_ICU}</th>
            <th>Current Query Result</th>
        </tr>
    </thead>
        <tbody>
            <g:each in="${examples.keySet()}" var="cat">
                <g:each in="${examples[cat].get(collate_de)}" var="e" status="i">
                    <tr class="${cat}">
                        <g:set var="phbk_de" value="${examples[cat].get(MigrationHelper.DE_U_CO_PHONEBK_X_ICU) ? examples[cat].get(MigrationHelper.DE_U_CO_PHONEBK_X_ICU)[i] : ''}" />

                        <td>${i+1}</td>
                        <td <% if (examples[cat].get(collate_de)[i] == phbk_de){ print 'class="positive"'} else { print 'class="negative"'} %>>
                            ${examples[cat].get(collate_de)[i]}
                        </td>
                        <td <% if (examples[cat].get(current_de)[i] == phbk_de){ print 'class="positive"'} else { print 'class="negative"'} %>>
                            ${examples[cat].get(current_de)[i]}
                        </td>
                        <td>
                            ${phbk_de}
                        </td>
                    </tr>
                </g:each>
            </g:each>
        </tbody>
    </table>
</div>

<div data-tab="third" class="ui bottom attached tab">
    <div class="ui la-float-right">
        <select id="thirdFilter" class="ui dropdown la-not-clearable">
            <option value="country" selected="selected">Merkmale: Länder</option>
            <option value="ddc">Merkmale: DDC</option>
            <option value="org">Organisationen</option>
            <option value="title">Titel</option>
        </select>
    </div>
    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
        <tr>
            <th>#</th>
            <th style="text-transform: none !important;">COLLATE: ${collate_en}</th>
            <th style="text-transform: none !important;">COLLATE: ${MigrationHelper.EN_US_U_VA_POSIX_X_ICU}</th>
            <th>Current Query Result</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${examples.keySet()}" var="cat">
                <g:each in="${examples[cat].get(collate_en)}" var="e" status="i">
                    <tr class="${cat}">
                        <g:set var="phbk_en" value="${examples[cat].get(MigrationHelper.EN_US_U_VA_POSIX_X_ICU) ? examples[cat].get(MigrationHelper.EN_US_U_VA_POSIX_X_ICU)[i] : ''}" />

                        <td>${i+1}</td>
                        <td <% if (examples[cat].get(collate_en)[i] == phbk_en){ print 'class="positive"'} else { print 'class="negative"'} %>>
                            ${examples[cat].get(collate_en)[i]}
                        </td>
                        <td <% if (examples[cat].get(current_en)[i] == phbk_en){ print 'class="positive"'} else { print 'class="negative"'} %>>
                            ${examples[cat].get(current_en)[i]}
                        </td>
                        <td>
                            ${phbk_en}
                        </td>
                    </tr>
                </g:each>
            </g:each>
        </tbody>
    </table>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#firstFilter, #secondFilter, #thirdFilter').on('change', function() {
        var baseSel = 'div[data-tab=' + this.id.replace(/Filter/, '') + '] table tbody tr'
        var selection = $(this).val()
        if (selection != 'all') {
            $(baseSel).addClass('hidden')
            $(baseSel + '.' + selection).removeClass('hidden')
        } else {
            $(baseSel).removeClass('hidden')
        }
    })
</laser:script>

</body>

</html>
