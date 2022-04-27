<%@ page import="de.laser.helper.DatabaseUtils; groovy.sql.GroovyRowResult; de.laser.RefdataValue; de.laser.storage.BeanStorage" %>
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
                <tr class="${! row.collation_name ? 'current' : row.collation_name}">
                    <td>${i+1}</td>
                    <td>${row.table_name}</td>
                    <td>${row.column_name}</td>
                    <td>${row.data_type}</td>
                    <td>${row.index_name}</td>
                    <g:if test="${! row.collation_name}">
                        <td class="x disabled">${collate_current}</td>
                    </g:if>
                    <g:else>
                        <td class="x">
                            <span class="ui image label">
                            <g:if test="${row.collation_name == DatabaseUtils.DE_U_CO_PHONEBK_X_ICU}">
                                <img class="ui mini" src="${resource(dir:'images', file:'flags/de.svg', absolute:true)}" alt="[DE]" /> ${row.collation_name}
                            </g:if>
                            <g:elseif test="${row.collation_name == DatabaseUtils.EN_US_U_VA_POSIX_X_ICU}">
                                <img class="ui mini" src="${resource(dir:'images', file:'flags/us.svg', absolute:true)}" alt="[EN]" /> ${row.collation_name}
                            </g:elseif>
                            <g:else>
                                <img class="ui mini" src="${resource(dir:'images', file:'flags/xx.svg', absolute:true)}" alt="[?]" /> ${row.collation_name}
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
            <th style="text-transform: none !important;">COLLATE: ${DatabaseUtils.DE_U_CO_PHONEBK_X_ICU}</th>
            <th>Current: HQL Result</th>
        </tr>
    </thead>
        <tbody>
            <g:each in="${examples.keySet()}" var="cat">
                <g:each in="${examples[cat].get(collate_de)}" var="e" status="i">
                    <tr class="${cat}">
                        <g:set var="phbk_de" value="${examples[cat].get(DatabaseUtils.DE_U_CO_PHONEBK_X_ICU) ? examples[cat].get(DatabaseUtils.DE_U_CO_PHONEBK_X_ICU)[i] : ''}" />

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
            <th style="text-transform: none !important;">COLLATE: ${DatabaseUtils.EN_US_U_VA_POSIX_X_ICU}</th>
            <th>Current: HQL Result</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${examples.keySet()}" var="cat">
                <g:each in="${examples[cat].get(collate_en)}" var="e" status="i">
                    <tr class="${cat}">
                        <g:set var="phbk_en" value="${examples[cat].get(DatabaseUtils.EN_US_U_VA_POSIX_X_ICU) ? examples[cat].get(DatabaseUtils.EN_US_U_VA_POSIX_X_ICU)[i] : ''}" />

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
    }).trigger('change')
</laser:script>

</body>

</html>
