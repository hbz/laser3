<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn; de.laser.addressbook.PersonRole; de.laser.addressbook.Contact; de.laser.OrgRole; de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition" %>
<laser:htmlStart message="feedback.title" description="${message(code:'metaDescription.sendFeedbackForm')}"/>

    <h1 class="ui header">
        <icon:universalAccess />
        <span class="content">
            ${message(code:'feedback.title')}
        </span>
    </h1>

    <div class="ui segment">
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

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="Abschicken">
            </div>

        </g:form>
    </div>

<laser:htmlEnd />