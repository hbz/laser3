<%@page import="de.laser.helper.RDStore;com.k_int.kbplus.*" %>
<%
    String unknownString = raw("<span data-tooltip=\"${RDStore.PERM_UNKNOWN.getI10n("value")}\"><i class=\"question circle icon huge\"></i></span>")
%>
<tr>
    <th>${key}</th>
    <g:each in="${licenses}" var="l">
        <g:if test="${propBinding && propBinding.get(l)?.visibleForConsortiaMembers}">
            <th>${l.reference}<span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span></th>
        </g:if>
        <g:else>
            <th>${l.reference}</th>
        </g:else>
    </g:each>
</tr>
<g:each in="${group}" var="prop">
    <%-- leave it for debugging
    <tr>
        <td colspan="999">${prop.getValue()}</td>
    </tr>--%>
    <tr>
        <td>${prop.getKey()}</td>
        <g:each in="${licenses}" var="l">
            <g:set var="propValues" value="${prop.getValue()}" />
            <g:if test="${propValues.containsKey(l)}">
                <g:set var="propValue" value="${propValues.get(l)}"/>
                <%
                    String value
                    if(propValue.value) {
                        switch(propValue.type.type) {
                            case "class ${RefdataValue.class.name}":
                                String spanOpen = '<span data-tooltip="'+propValue.refValue.getI10n("value")+'">'
                                switch(propValue.refValue.owner) {
                                case RefdataCategory.findByDesc("YN"):
                                case RefdataCategory.findByDesc("YNO"):
                                    switch(propValue.refValue) {
                                        case RDStore.YN_YES:
                                        case RDStore.YNO_YES: value = raw(spanOpen+'<i class="green thumbs up icon huge"></i></span>')
                                            break
                                        case RDStore.YN_NO:
                                        case RDStore.YNO_NO: value = raw(spanOpen+'<i class="red thumbs down icon huge"></i></span>')
                                            break
                                        case RDStore.YNO_OTHER: value = raw(spanOpen+'<i class="yellow dot circle huge"></i></span>')
                                            break
                                    }
                                    break
                                case RefdataCategory.findByDesc("Permissions"):
                                    switch(propValue.refValue){
                                        case RDStore.PERM_PERM_EXPL: value = raw(spanOpen+'<i class="green check circle icon huge"></i></span>')
                                            break
                                        case RDStore.PERM_PERM_INTERP: value = raw(spanOpen+'<i class="green check circle outline icon huge"></i></span>')
                                            break
                                        case RDStore.PERM_PROH_EXPL: value = raw(spanOpen+'<i class="red times circle icon huge"></i></span>')
                                            break
                                        case RDStore.PERM_PROH_INTERP: value = raw(spanOpen+'<i class="red times circle outline icon huge"></i></span>')
                                            break
                                        case RDStore.PERM_SILENT: value = raw(spanOpen+'<i class="hand point up huge"></i></span>')
                                            break
                                        case RDStore.PERM_NOT_APPLICABLE: value = raw(spanOpen+'<i class="exclamation huge"></i></span>')
                                            break
                                        case RDStore.PERM_UNKNOWN: value = raw(spanOpen+'<i class="question circle icon huge"></i></span>')
                                            break
                                    }
                                    break
                                default: value = propValue.refValue.getI10n("value")
                                    break
                                }
                                break
                            default: value = propValue.value
                                break
                        }
                    }
                    else value = unknownString
                %>
                <td>
                    ${value} <div class="ui circular massive label la-long-tooltip" data-tooltip="${propValue.paragraph}">§</div>
                </td>
            </g:if>
            <g:else>
                <td>
                    ${unknownString} <div class="ui circular massive label la-long-tooltip" data-tooltip="${propValue.paragraph}">§</div>
                </td>
            </g:else>
        </g:each>
    </tr>
</g:each>