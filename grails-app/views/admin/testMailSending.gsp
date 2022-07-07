<laser:htmlStart text="Test E-Mail Sending" />

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin" controller="admin" action="index"/>
        <semui:crumb text="Test E-Mail Sending" class="active"/>
    </semui:breadcrumbs>

    <h2>Test E-Mail Sending</h2>

    <semui:messages data="${flash}" />

    <semui:form>
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
    </semui:form>

    <g:if test="${mailDisabled}">
        <semui:msg class="warning" header="${message(code:'default.hint.label')}" text="${message(code:'system.config.mail.disabled')}" />
    </g:if>

<laser:htmlEnd />
