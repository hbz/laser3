<%@ page import="de.laser.ui.Icon" %>
<laser:htmlStart message="menu.yoda.systemConfiguration" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.systemConfiguration" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.systemConfiguration" type="yoda" />

<%
    Set<String> dubs = []
    Set<String> sortedKeySet = currentConfig.keySet().sort()

    sortedKeySet.each { it1 ->
        sortedKeySet.each { it2 ->
            if (it2.startsWith(it1 + '.')) {
                dubs.add(it1)
                return
            }
        }
    }
    Set<String> keySet = sortedKeySet - dubs
%>

<br />

<div id="cfgFilter">
    <span class="ui label" data-class="*">Alle anzeigen</span>
    <span class="ui label" data-class="green"><i class="${Icon.SYM.SQUARE} green"></i>spring</span>
    <span class="ui label" data-class="orange"><i class="${Icon.SYM.SQUARE} orange"></i>grails</span>
    <span class="ui label" data-class="yellow"><i class="${Icon.SYM.SQUARE} yellow"></i>grails.plugin(s)</span>
    <span class="ui label" data-class="red"><i class="${Icon.SYM.SQUARE} red"></i>java</span>
    <span class="ui label" data-class="blue"><i class="${Icon.SYM.SQUARE} blue"></i>dataSource</span>
    <span class="ui label" data-class="native"><i class="${Icon.SYM.SQUARE} grey"></i>default</span>
</div>

<div class="ui fluid card">
    <div class="content">


<table id="cfgTable" class="ui sortable celled la-js-responsive-table la-hover-table la-table compact table">
    <tbody>
        <g:each in="${keySet}" var="key" status="i">
            <%
                String color = ''
                if (key.startsWith('grails.plugin'))        { color = 'yellow' }
                else if (key.startsWith('grails'))          { color = 'orange' }
                else if (key.startsWith('dataSource'))      { color = 'blue' }
                else if (key.startsWith('java'))            { color = 'red' }
                else if (key.startsWith('spring'))          { color = 'green' }
            %>
            <tr data-class="${color ?: 'native'}">
                <td class="center aligned">
                    <span class="ui mini label ${color}">${i+1}</span>
                </td>
                <td>${key}</td>
                <td>
                    <g:if test="${blacklist.contains(key)}">
                        <span class="ui label"> hidden </span>
                    </g:if>
                    <g:else>
                        <g:if test="${key == 'java.class.path'}">
                            <div style="overflow:scroll; max-height:150px">
                                ${currentConfig.get(key)}
                            </div>
                        </g:if>
                        <g:else>
                            ${currentConfig.get(key)}
                        </g:else>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

    </div>
</div>

<style>
    #cfgFilter { padding: 1em 2em; }
    #cfgFilter span[data-class]:hover { cursor: pointer }
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#cfgFilter span[data-class]').on ('click', function () {
        let cls = $(this).attr('data-class')
        if (cls == '*') {
            $('#cfgTable tbody tr').show()
        } else {
            $('#cfgTable tbody tr').hide()
            $('#cfgTable tbody tr[data-class=' + cls + ']').show()
        }
    })
</laser:script>

<laser:htmlEnd />
