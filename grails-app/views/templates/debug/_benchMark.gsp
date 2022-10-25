<g:if test="${debug}">

    <div style="padding: 1em 0;">
        <h5 class="ui header">BenchMark</h5>

        <table class="ui celled la-js-responsive-table la-table compact table la-ignore-fixed">
            <thead>
                <tr>
                    <th>Step</th>
                    <th>Comment</th>
                    <th>(Step_x+1 - Step_x) MS</th>
                </tr>
            </thead>
            <g:each in="${debug}" status="c" var="bm">
                <tr>
                    <td>${c+1}</td>
                    <td>${bm[0]}</td>
                    <td>
                        <%
                            if (c < debug.size() - 1) {
                                print debug[c+1][1] - bm[1]
                            } else {
                                print '&rArr; ' + ( bm[1] - debug[0][1] ) + ' &lArr;'
                            }
                        %>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>

</g:if>
