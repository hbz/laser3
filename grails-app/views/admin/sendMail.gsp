<%@ page import="de.laser.ui.Btn" %>
<laser:htmlStart message="menu.admin.sendMail" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb message="menu.admin.sendMail" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.sendMail" type="admin"/>

    <ui:messages data="${flash}" />

    <ui:form controller="admin" action="sendMail">
            <div class="field">
                <label for="mailAddress">Mail Address</label>
                <input type="email" id="mailAddress" name="mailAddress" value="laser@hbz-nrw.de" />
            </div>
            <div class="field">
                <label for="subject">Subject</label>
                <input type="text" id="subject" name="subject" value="E-Mail (Test)" />
            </div>
            <div class="field">
                <label for="content">Content</label>
                <textarea id="content" name="content"></textarea>
            </div>

            <div class="field">
                    <input type="submit" name="sendTestMail" class="${Btn.SIMPLE}" value="Send Test Mail" />
            </div>
    </ui:form>

    <g:if test="${mailDisabled}">
        <ui:msg class="warning" header="${message(code:'default.hint.label')}" message="system.config.mail.disabled" />
    </g:if>

<laser:htmlEnd />
