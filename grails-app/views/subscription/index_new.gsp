<laser:htmlStart serviceInjection="true"/>
    <table>
        <g:each in="${issueEntitlements}" var="ie">
            <tr>
                <td>${ie.id}</td>
                <td>${ie.tipp.name}</td>
            </tr>
        </g:each>
    </table>
<laser:htmlEnd/>