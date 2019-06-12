<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 07.06.2019
  Time: 12:27
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<laser:serviceInjection/>
<html>
    <head>
        <title>Finanzdaten importieren - zweiter Schritt</title>
    </head>

    <body>
        <h2>Zur Zeit nur Test</h2>
        <h3>Es wurden Daten ausgelesen. Ausgabe der Daten samt Fehler:</h3>
        <table>
            <g:each in="${candidates.entrySet()}" var="row">
                <tr>
                    <td>${row.getKey()}</td>
                    <td>${row.getValue()}</td>
                </tr>
            </g:each>
        </table>
    </body>
</html>