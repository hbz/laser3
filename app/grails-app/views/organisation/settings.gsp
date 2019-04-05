<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition; com.k_int.properties.PropertyDefinitionGroup" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <title>${message(code:'laser', default:'LAS:eR')} : Settings</title>
    </head>
    <body>

    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
        ${orgInstance.name}
    </h1>

    <g:render template="nav"/>

    <semui:objectStatus object="${orgInstance}" status="${orgInstance.status}" />

    <semui:messages data="${flash}" />

    <div class="ui grid">
        <div class="twelve wide column">

            <table class="ui la-table table">
                <thead>
                    <tr>
                        <th>Key</th>
                        <th>Value</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${settings}" var="os">
                        <tr>
                            <td>${os.key}</td>
                            <td>${os.getValue()}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>

        </div>
    </div>
  </body>
</html>
