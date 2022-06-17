<div id="unlinkPackageModal" class="ui modal ">

    <div class="header">
        <h6 class="ui header">${message(code: 'default.button.unlink.label')}: ${pkg}</h6>
    </div>

    <div class="content">

        <g:set var="actions_needed" value="false"/>

        <g:each in="${conflicts_list}" var="conflict_item">
            <ul>
                <g:each in="${conflict_item.details}" var="detail_item">
                    <li>
                        <strong>
                            <g:if test="${detail_item.number}">
                                <span>${detail_item.number}</span>&nbsp
                            </g:if>
                            <g:if test="${detail_item.link}">
                                <a href="${detail_item.link}">${detail_item.text}</a>
                            </g:if>
                            <g:else>
                                ${detail_item.text}
                            </g:else>
                        </strong>${conflict_item.action.text}
                    </li>
                </g:each>
                <g:if test="${conflict_item.action.actionRequired}">
                    <i class="fa fa-times-circle"></i>
                    <g:set var="actions_needed" value="true"/>

                </g:if>
                <g:else>
                    <i class="fa fa-check-circle"></i>
                </g:else>
            </ul>
        </g:each>

    </div>

    <div class="actions">
        <g:form action="unlinkPackage"
                method="POST" class="ui form">
            <input type="hidden" name="package" value="${pkg.id}"/>
            <input type="hidden" name="subscription" value="${subscription.id}"/>
            <input type="hidden" name="confirmed" value="Y"/>
        </g:form>
        <a href="#" class="ui button unlinkPackageModal"><g:message code="default.button.close.label"/></a>
        <input type="submit" class="ui button negative" name="save" value="${message(code: 'default.button.confirm_delete.label')}" onclick="event.preventDefault(); $('#unlinkPackageModal').find('form').submit()"/>
    </div>
</div>