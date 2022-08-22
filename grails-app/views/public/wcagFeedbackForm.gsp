<%@ page import="de.laser.PersonRole; de.laser.Contact; de.laser.OrgRole; de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition" %>
<laser:htmlStart message="feedback.title" />

    <h1 class="ui header">
        <i class="universal access icon"></i>
        <div class="content">
            ${message(code:'feedback.title')}
        </div>
    </h1>

    <div class="ui  segment">
        <g:form action="sendFeedbackForm" controller="public" method="get" class="ui small form">
            <div class="field">
                <label>Name</label>

                <div class="ui input">
                    <input type="text" name="name"
                           placeholder="Name"
                           value=""/>
                </div>
            </div>

            <div class="field">
                <label>E-Mail-Adresse</label>

                <div class="ui input">
                    <input type="text" name="email"
                           placeholder="E-Mail-Adresse"
                           value=""/>
                </div>
            </div>

            <div class="field">
                <label>URL der Seite, die Sie kommentieren</label>

                <div class="ui input">
                    <input type="text" name="url"
                           placeholder="URL der Seite, die Sie kommentieren"
                           value=""/>
                </div>
            </div>

            <div class="field">
                <label>Kommentar</label>

                <g:textArea name="comment"  rows="5" cols="40"/>

            </div>

            <div class="field la-field-right-aligned ">
                <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui primary button" value="Abschicken">
            </div>

        </g:form>
    </div>

<laser:htmlEnd />