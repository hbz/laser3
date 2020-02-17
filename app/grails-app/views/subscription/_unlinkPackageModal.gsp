
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
            <i style="color:red" class="fa fa-times-circle"></i>
            <g:set var="actions_needed" value="true"/>

        </g:if>
        <g:else>
            <i style="color:green" class="fa fa-check-circle"></i>
        </g:else>
    </ul>
</g:each>

%{--<div class="actions">--}%
    %{--<g:form action="unlinkPackage"--}%
            %{--onsubmit="return confirm('${message(code: 'subscription.details.unlink.confirm', default: 'Deletion of IEs and PendingChanges is NOT reversable. Continue?')}')"--}%
            %{--method="POST">--}%
        %{--<input type="hidden" name="package" value="${pkg.id}"/>--}%
        %{--<input type="hidden" name="subscription" value="${subscription.id}"/>--}%
        %{--<input type="hidden" name="confirmed" value="Y"/>--}%
        %{--<button type="submit"--}%
                %{--class="ui negative button">${message(code: 'default.button.confirm_delete.label')}</button>--}%
    %{--</g:form>--}%
%{--</div>--}%
