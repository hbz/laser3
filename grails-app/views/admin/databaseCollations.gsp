<%@ page import="de.laser.helper.DatabaseInfo; groovy.sql.GroovyRowResult; de.laser.RefdataValue; de.laser.storage.BeanStore" %>

<laser:htmlStart message="menu.admin.databaseCollations" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.databaseCollations" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.databaseCollations" />

<div class="ui secondary stackable pointing tabular la-tab-with-js menu">
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
    <table class="ui celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
            <tr>
                <th>#</th>
                <th>Tabelle</th>
                <th>Feld</th>
                <th>Datentyp</th>
                <th>Index</th>
                <th></th>
                <th>Collation</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${allTables}" var="row" status="i">
                <tr class="${! row.collation_name ? 'current' : row.collation_name}">
                    <td>${i+1}</td>
                    <td>${row.table_name}</td>
                    <td>
                        <g:if test="${row.column_name.endsWith('_de') || row.column_name.endsWith('_en')}">
                            <strong><em>${row.column_name}</em></strong>
                        </g:if>
                        <g:else>
                            ${row.column_name}
                        </g:else>
                    </td>
                    <td>${row.data_type}</td>
                    <td>${row.index_name}</td>
                    <g:if test="${! row.collation_name}">
                        <td></td>
                        <td class="disabled">${collate_current}</td>
                    </g:if>
                    <g:else>
                        <td>
                            <g:if test="${row.collation_name == DatabaseInfo.DE_U_CO_PHONEBK_X_ICU}">
                                <img class="mini-image" src="${resource(dir:'images', file:'flags/de.svg', absolute:true)}" alt="[DE]" />
                            </g:if>
                            <g:elseif test="${row.collation_name == DatabaseInfo.EN_US_U_VA_POSIX_X_ICU}">
                                <img class="mini-image" src="${resource(dir:'images', file:'flags/us.svg', absolute:true)}" alt="[EN]" />
                            </g:elseif>
                            <g:else>
                                <img class="mini-image" src="${resource(dir:'images', file:'flags/xx.svg', absolute:true)}" alt="[?]" />
                            </g:else>
                        </td>
                        <td>
                            ${row.collation_name}
                        </td>
                    </g:else>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<style>
    img.mini-image { width:26px; vertical-align:middle;}
</style>

<div data-tab="second" class="ui bottom attached tab">
    <div class="ui la-float-right">
        <select id="secondFilter" class="ui dropdown la-not-clearable">
            <option value="country" selected="selected">Merkmale: Länder</option>
            <option value="ddc">Merkmale: DDC</option>
            <option value="org">Organisationen</option>
            <option value="title">Titel</option>
        </select>
    </div>
    <table class="ui celled la-js-responsive-table la-table la-hover-table compact table">
    <thead>
        <tr>
            <th></th>
            <th style="text-transform: none !important;">DEFAULT: ${collate_de}</th>
            <th>Aktive Sortierung</th>
            <th>${DatabaseInfo.DE_U_CO_PHONEBK_X_ICU}</th>
        </tr>
    </thead>
        <tbody>
            <g:each in="${examples.keySet()}" var="cat">
                <g:each in="${examples[cat].get(collate_de)}" var="e" status="i">
                    <tr class="${cat}">
                        <g:set var="phbk_de" value="${examples[cat].get(DatabaseInfo.DE_U_CO_PHONEBK_X_ICU) ? examples[cat].get(DatabaseInfo.DE_U_CO_PHONEBK_X_ICU)[i] : ''}" />

                        <td>${i+1}</td>
                        <td <% if (examples[cat].get(collate_de)[i] == phbk_de){ print 'class="positive"'} else { print 'class="error"'} %>>
                            ${examples[cat].get(collate_de)[i]}
                        </td>
                        <td <% if (examples[cat].get(current_de)[i] == phbk_de){ print 'class="positive"'} else { print 'class="error"'} %>>
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
    <table class="ui celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
        <tr>
            <th></th>
            <th style="text-transform: none !important;">DEFAULT: ${collate_en}</th>
            <th>Aktive Sortierung</th>
            <th>${DatabaseInfo.EN_US_U_VA_POSIX_X_ICU}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${examples.keySet()}" var="cat">
                <g:each in="${examples[cat].get(collate_en)}" var="e" status="i">
                    <tr class="${cat}">
                        <g:set var="phbk_en" value="${examples[cat].get(DatabaseInfo.EN_US_U_VA_POSIX_X_ICU) ? examples[cat].get(DatabaseInfo.EN_US_U_VA_POSIX_X_ICU)[i] : ''}" />

                        <td>${i+1}</td>
                        <td <% if (examples[cat].get(collate_en)[i] == phbk_en){ print 'class="positive"'} else { print 'class="error"'} %>>
                            ${examples[cat].get(collate_en)[i]}
                        </td>
                        <td <% if (examples[cat].get(current_en)[i] == phbk_en){ print 'class="positive"'} else { print 'class="error"'} %>>
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

<laser:htmlEnd />
