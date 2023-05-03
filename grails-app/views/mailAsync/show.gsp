<g:set var="entityName" value="${message(code: 'menu.yoda.mailAysnc.list')}" />
<laser:htmlStart text="${message(code:"default.list.label", args:[entityName])}" />
<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.mailAysnc.list" controller="mailAsync" action="index"/>
    <ui:crumb message="menu.yoda.mailAysnc.show" class="active"/>
</ui:breadcrumbs>
<ui:h1HeaderWithIcon message="menu.yoda.mailAysnc.show" type="yoda" />
    <%-- Flash message --%>
    <g:render template="flashMessage"/>
    <div class="ui stackable grid">
        <div class="sixteen wide column">

            <div class="la-inline-lists">
                <div class="ui card" id="js-confirmationCard">
                    <div class="content">
                        <dl>
                            <dt>From:</dt>
                            <dd>
                                <g:if test="${message.from}">
                                    <a href="mailto:${message.from.encodeAsURL()}">${message.from.encodeAsHTML()}</a>
                                </g:if>
                            </dd>
                        </dl>
                        <dl>
                            <dt>Reply to:</dt>
                            <dd>
                                <g:if test="${message.replyTo}">
                                    <a href="mailto:${message.replyTo.encodeAsURL()}">${message.replyTo.encodeAsHTML()}</a>
                                </g:if>
                            </dd>
                        </dl>
                        <dl>
                            <dt>To:</dt>
                            <dd><g:render template="listAddr" bean="${message.to}"/></dd>
                        </dl>
                        <dl>
                            <dt>Cc:</dt>
                            <dd><g:render template="listAddr" bean="${message.cc}"/></dd>
                        </dl>
                        <dl>
                            <dt>Bcc:</dt>
                            <dd><g:render template="listAddr" bean="${message.bcc}"/></dd>
                        </dl>
                        <dl>
                            <dt>Headers:</dt>
                            <dd>
                                <g:each var="entry" in="${message.headers}" status="status"><g:if
                                        test="${status != 0}">,</g:if>
                                    ${entry.key?.encodeAsHTML()}:${entry.value?.encodeAsHTML()}</g:each>
                            </dd>
                        </dl>
                        <dl>
                            <dt>Subject:</dt>
                            <dd>${message.subject?.encodeAsHTML()}</dd>
                        </dl>
                        <dl>
                            <dt>Subject:</dt>
                            <dd>${message.text?.encodeAsHTML()}</dd>
                        </dl>
                        <dl>
                            <dt>Attachments:</dt>
                            <dd>
                                <g:each var="attachment" in="${message.attachments}" status="status"><g:if
                                        test="${status != 0}">,</g:if>
                                    ${attachment.attachmentName?.encodeAsHTML()}</g:each>
                            </dd>

                        </dl>
                        <dl>
                            <dt>Create date:</dt>
                            <dd><g:formatDate date="${message.createDate}" format="yyyy-MM-dd HH:mm:ss"/></dd>
                        </dl>
                        <dl>
                            <dt>Status:</dt>
                            <dd>${message.status.encodeAsHTML()}</dd>
                        </dl>
                        <dl>
                            <dt>Last attempt date:</dt>
                            <dd>
                                <g:formatDate date="${message.lastAttemptDate}" format="yyyy-MM-dd HH:mm:ss"/></dd>
                        </dl>
                        <dl>
                            <dt>Attempts count:</dt>
                            <dd>${fieldValue(bean: message, field: 'attemptsCount')}</dd>
                        </dl>
                        <dl>
                            <dt>Sent date:</dt>
                            <dd><g:formatDate date="${message.sentDate}" format="yyyy-MM-dd HH:mm:ss"/></dd>
                        </dl>
                        <dl>
                            <dt>Max attempts count:</dt>
                            <dd>
                                <g:if test="${message.maxAttemptsCount == 0}">INFINITE</g:if>
                                <g:else>${fieldValue(bean: message, field: 'maxAttemptsCount')}</g:else>
                            </dd>
                        </dl>
                        <dl>
                            <dt>Begin date:</dt>
                            <dd><g:formatDate date="${message.beginDate}" format="yyyy-MM-dd HH:mm:ss"/></dd>
                        </dl>
                        <dl>
                            <dt>Attempt interval (ms):</dt>

                            <dd><g:formatNumber number="${message.attemptInterval}" format="#,##0"/></dd>
                        </dl>
                        <dl>
                            <dt>End date:</dt>
                            <dd><g:formatDate date="${message.endDate}" format="yyyy-MM-dd HH:mm:ss"/></dd>
                        </dl>
                        <dl>
                            <dt>Priority:</dt>
                            <dd><g:formatNumber number="${message.priority}" format="#,##0"/></dd>
                        </dl>
                        <dl>
                            <dt>HTML:</dt>
                            <dd>${fieldValue(bean: message, field: 'html')}</dd>
                        </dl>
                        <dl>
                            <dt>Delete after sent:</dt>
                            <dd>${fieldValue(bean: message, field: 'markDelete')}</dd>
                        </dl>

        <div class="buttons">
            <g:form>
                <input type="hidden" name="id" value="${message?.id}"/>
                <g:if test="${message.abortable}">
                    <g:actionSubmit class="ui button" onclick="return confirm('Are you sure?');"
                                                         action="abort" value="Abort"/>
                </g:if>
                <g:actionSubmit class="ui button" onclick="return confirm('Are you sure?');"
                                                     action="delete" value="Delete"/>
            </g:form>
        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

<laser:htmlEnd/>
