<%@ page import="com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact; com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code:'feedback.title')}</title>
</head>

<body>
            <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'feedback.title')}</h1>
            <div class="ui  segment">
                <g:form action="wcagFeedbackForm" controller="public" method="get" class="form-inline ui small form">
                    <div class="field">
                        <label>Name</label>

                        <div class="ui input">
                            <input type="text" name="q"
                                   placeholder="Name"
                                   value=""/>
                        </div>
                    </div>

                    <div class="field">
                        <label>E-Mail-Adresse</label>

                        <div class="ui input">
                            <input type="text" name="q"
                                   placeholder="E-Mail-Adresse"
                                   value=""/>
                        </div>
                    </div>

                    <div class="field">
                        <label>URL der Seite, die Sie kommentieren</label>

                        <div class="ui input">
                            <input type="text" name="q"
                                   placeholder="URL der Seite, die Sie kommentieren"
                                   value=""/>
                        </div>
                    </div>

                    <div class="field">
                        <label>Kommentar</label>

                        <g:textArea name="description" value="${taskInstance?.description}" rows="5" cols="40"/>

                    </div>

                    <div class="field la-field-right-aligned ">
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="Abschicken">
                    </div>

                </g:form>
            </div>

</body>