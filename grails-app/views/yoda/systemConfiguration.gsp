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
<span class="ui label"><i class="icon green tag"></i>spring</span>
<span class="ui label"><i class="icon orange tag"></i>grails</span>
<span class="ui label"><i class="icon yellow tag"></i>grails.plugin(s)</span>
<span class="ui label"><i class="icon red tag"></i>java</span>
<span class="ui label"><i class="icon blue tag"></i>dataSource</span>

<table class="ui sortable celled la-js-responsive-table la-hover-table la-table compact table">
    <thead>
    <tr>
        <th></th>
        <th></th>
        <th></th>
    </tr>
    </thead>
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
            <tr>
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

<laser:htmlEnd />
