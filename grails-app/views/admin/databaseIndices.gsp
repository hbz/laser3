<%@ page import="de.laser.helper.DatabaseInfo; groovy.sql.GroovyRowResult; de.laser.RefdataValue; de.laser.storage.BeanStore" %>
%{--<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.DatabaseInfo; groovy.sql.GroovyRowResult; de.laser.RefdataValue; de.laser.storage.BeanStore" %>--}%

<laser:htmlStart message="menu.admin.databaseIndices" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.databaseIndices" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.databaseIndices" type="admin"/>

<g:set var="tables_internal1" value="${indices.findAll{ it[2] in ['id', 'version'] }.collect{ it[0] }}" />
<g:set var="tables_internal2" value="${indices.findAll{ it[2] in ['dateCreated', 'lastUpdated'] }.collect{ it[0] }}" />
<g:set var="tables_collections" value="${indices.findAll{ it[3].toString().startsWith('interface ') }.collect{ it[0] }}" />
<g:set var="tables_laser" value="${indices.findAll{ it[3].toString().startsWith('class de.laser.') }.collect{ it[0] }}" />

<br/>

<div id="filter">
    <button class="ui button negative small" id="internal1">Id/Version</button>
    <button class="ui button negative small" id="internal2">DateCreated/LastUpdated</button>
    <button class="ui button negative small" id="collections">Collections</button>
%{--    <button class="${Btn.NEGATIVE} small" id="internal1">Id/Version</button>--}%
%{--    <button class="${Btn.NEGATIVE} small" id="internal2">DateCreated/LastUpdated</button>--}%
%{--    <button class="${Btn.NEGATIVE} small" id="collections">Collections</button>--}%
    <g:select class="ui dropdown" from="${['1.000':1000, '5.000':5000, '10.000':10000, '50.000':50000, '100.000':100000, '500.000':500000, '1.000.000':1000000, '5.000.000':5000000, '10.000.000':10000000]}"
              id="thresholdvalue" name="thresholdvalue"
              optionValue="${{it.key}}"
              optionKey="${{it.value}}"
              noSelection="${['': 'DB-Einträge (Domainklasse)']}" />

    <button class="ui button icon small" id="threshold"><i class="icon filter"></i><i class="icon sort"></i></button>
</div>

<ui:msg class="info" header="Achtung" text="Die angezeigten Daten sind unvollständig und bieten nur eine Orientierungshilfe."/>

<style>
    tr.info td {
        color: #276f86;
        background-color: #f8ffff;
    }
    tr.threshold td {
        display: none;
    }
    tr.unique td {
        font-style: italic;
    }
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.idxFilter = { internal1: true, internal2: true, collections: true, threshold: false }

    JSPC.app.applyFilter = function() {
        $('#table tr').removeClass('hidden').removeClass('threshold')

        let $$idx = JSPC.app.idxFilter

        if ($$idx.internal1 )   { $('#table tr[data-internal1=true]').addClass('hidden') }
        if ($$idx.internal2 )   { $('#table tr[data-internal2=true]').addClass('hidden') }
        if ($$idx.collections ) { $('#table tr[data-collections=true]').addClass('hidden') }

        let tv = $('#thresholdvalue').dropdown('get value')
        if (tv) {
            let $th = $('#table tr[data-threshold]').filter(function() {
                if ($$idx.threshold ) {
                    console.log('>=: ' + parseInt($(this).attr('data-threshold')) + ' ? ' +  parseInt(tv) + ' == ' + (parseInt($(this).attr('data-threshold')) >= parseInt(tv)))
                    return parseInt($(this).attr('data-threshold')) >= parseInt(tv)
                } else {
                    console.log('<=: ' + parseInt($(this).attr('data-threshold')) + ' ? ' +  parseInt(tv) + ' == ' + (parseInt($(this).attr('data-threshold')) <= parseInt(tv)))
                    return parseInt($(this).attr('data-threshold')) <= parseInt(tv)
                }
            });
            $th.addClass('threshold')
        }
    }
    $('#internal1, #internal2, #collections, #threshold').on('click', function() {
        let $$idx = JSPC.app.idxFilter
        let id = $(this).attr('id')
        $$idx[id] = !$$idx[id]

        if (id != 'threshold') {
            $(this).toggleClass('positive').toggleClass('negative')
        }
        JSPC.app.applyFilter()
    })
    $('#thresholdvalue').on('change', function() {
        JSPC.app.applyFilter()
    })

    JSPC.app.applyFilter()
    $('table').removeClass('hidden')
</laser:script>

<table class="ui celled la-js-responsive-table la-table la-hover-table very compact table hidden" id="table">
        <thead>
            <tr>
                <th class="center aligned">#</th>
                <th>Domainklasse</th>
                <th>Feld</th>
                <th>Mapping</th>
                <th>Typ</th>
                <th class="center aligned">&lArr;</th>
                <th class="center aligned">&rArr;</th>
                <th>Index</th>
                <th>Größe</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${indices}" var="row" status="i">
                <g:if test="${!lastRow || lastRow != row[1]}">
                    <g:set var="lastRow" value="${row[1]}" />
                    <tr class="disabled">
                        <td colspan="9" style="background-color: #f4f8f9"></td>
                    </tr>
                </g:if>
                <g:set var="isDisabled" value="${tables_internal1.contains(i) || tables_internal2.contains(i) || tables_collections.contains(i)}" />
                <g:set var="calcCssUnique" value="${row[5] == 'UNIQUE' ? 'unique' : ''}" />
                <g:set var="calcCssClass" value="${row[5] ? (tables_laser.contains(i) ? 'positive' : 'info') : tables_laser.contains(i) ? 'negative' : ''}" />
                <g:set var="countFrom" value="${counts.get(row[1])}" />
                <g:set var="countTo" value="${counts.get(row[3].toString().replace('class ', ''))}" />
%{--                <g:set var="threshold" value="${Math.max((countFrom && countFrom != '?') ? countFrom : 0, (countTo && countTo != '?') ? countTo : 0)}" />--}%
                <g:set var="threshold" value="${(countFrom && countFrom != '?') ? countFrom : 0}" />

                <tr class="${calcCssClass} ${calcCssUnique} ${isDisabled ? 'disabled ' : ''}"
                    data-internal1="${tables_internal1.contains(i)}"
                    data-internal2="${tables_internal2.contains(i)}"
                    data-collections="${tables_collections.contains(i)}"
                    data-threshold="${threshold}"
                >
                    <td class="center aligned">
                        <%
//                            switch( calcCssClass ) {
//                                case 'positive':    println '<i class="' + Icon.SYM.YES + '"></i>'; break
//                                case 'info':        println '<i class="' + Icon.SYM.YES + '"></i>'; break
//                                case 'warning':     println '<i class="' + Icon.SYM.NO + '"></i>'; break
//                                default:            println (i+1)
//                            }
                            switch( calcCssClass ) {
                                case 'positive':    println '<i class="check icon"></i>'; break
                                case 'info':        println '<i class="check icon"></i>'; break
                                case 'warning':     println '<i class="times icon"></i>'; break
                                default:            println (i+1)
                            }
                        %>
                    </td>
                    <td> ${row[1].split('\\.').last()} </td>
                    <td> ${row[2]} </td>
                    <td> ${row[4]} </td>
                    <td> ${row[3].toString().replace('class ', '').replace('interface ', '')} </td>
                    <td class="center aligned"> ${countFrom} </td>
                    <td class="center aligned"> ${countTo} </td>
                    <td>
                        <g:each in="${row[5]?.split(',')}" var="idx"> ${idx} <br/> </g:each>
                    </td>
                    <td> ${row[6] ? row[6]['index_size'] : ''} </td>
                </tr>
            </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
