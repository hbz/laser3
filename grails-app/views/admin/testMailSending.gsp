<laser:htmlStart text="Test E-Mail Sending" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb text="Test E-Mail Sending" class="active"/>
    </ui:breadcrumbs>

    <h2>Test E-Mail Sending</h2>

    <ui:messages data="${flash}" />

    <ui:form>
        <g:form action="testMailSending" class="ui form">
            <div class="field">
                <label for="mailAddress">Mail Address</label>
                <input type="email" id="mailAddress" name="mailAddress" value="laser@hbz-nrw.de" />
            </div>
            <div class="field">
                <label for="subject">Subject</label>
                <input type="text" id="subject" name="subject" value="Test Mail" />
            </div>
            <div class="field">
                <label for="content">Content</label>
                <textarea id="content" name="content"></textarea>
            </div>

            <div class="field">
                    <input type="submit" name="sendTestMail" class="ui button" value="Send Test Mail" />
            </div>
        </g:form>
    </ui:form>

    <g:if test="${mailDisabled}">
        <ui:msg class="warning" header="${message(code:'default.hint.label')}" message="system.config.mail.disabled" />
    </g:if>

<laser:htmlEnd />
