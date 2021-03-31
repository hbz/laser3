<%@ page import="de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'org.sortname.label')}</th>
            <th>Name</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="org" status="i">
                <g:if test="${plusList.contains(org)}">
                    <tr>
                        <td style="text-align: center"><span class="ui label circular green">${i + 1}.</span></td>
                </g:if>
                <g:else>
                    <tr>
                        <td style="text-align: center">${i + 1}.</td>
                </g:else>
                    <td>${org.sortname}</td>
                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<g:if test="${minusList}">
    <div class="ui segment">
        <table class="ui table la-table compact">
            <thead>
            <tr>
                <th></th>
                <th>Sortiername</th>
                <th>Name</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${minusList}" var="org" status="i">
                    <tr class="negative">
                        <td style="text-align: center"><span class="ui label circular red">${i + 1}.</span></td>
                        <td>${org.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>