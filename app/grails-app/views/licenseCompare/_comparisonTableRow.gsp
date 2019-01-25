<%@page import="de.laser.helper.RDStore;com.k_int.kbplus.*" %>
<g:each in="${group}" var="prop">
    <%-- leave it for debugging
    <tr>
        <td colspan="999">${prop}</td>
    </tr>--%>
    <tr>
        <td>${prop.getKey()}</td>
        <g:set value="${prop.getValue()}" var="propValues"/>
        <g:each in="${licenses}" var="l">
            <g:if test="${propValues.containsKey(l)}">
                <g:set var="propValue" value="${propValues.get(l)}"/>
                <%
                    String value
                    if(propValue.prop.value) {
                        switch(propValue.prop.type.type) {
                            case "class ${RefdataValue.class.name}":
                                String spanOpen = '<span data-tooltip="'+propValue.prop.refValue.getI10n("value")+'">'
                                switch(propValue.prop.refValue.owner) {
                                case RefdataCategory.findByDesc("YN"):
                                case RefdataCategory.findByDesc("YNO"):
                                    switch(propValue.prop.refValue) {
                                        case RDStore.YN_YES:
                                        case RDStore.YNO_YES: value = raw(spanOpen+'<i class="green thumbs up icon large"></i></span>')
                                            break
                                        case RDStore.YN_NO:
                                        case RDStore.YNO_NO: value = raw(spanOpen+'<i class="red thumbs down icon large"></i></span>')
                                            break
                                        case RDStore.YNO_OTHER: value = raw(spanOpen+'<i class="yellow dot circle large"></i></span>')
                                            break
                                    }
                                    break
                                case RefdataCategory.findByDesc("Permissions"):
                                    switch(propValue.prop.refValue){
                                        case RDStore.PERM_PERM_EXPL: value = raw(spanOpen+'<i class="green check circle icon large"></i></span>')
                                            break
                                        case RDStore.PERM_PERM_INTERP: value = raw(spanOpen+'<i class="green check circle outline icon large"></i></span>')
                                            break
                                        case RDStore.PERM_PROH_EXPL: value = raw(spanOpen+'<i class="red times circle icon large"></i></span>')
                                            break
                                        case RDStore.PERM_PROH_INTERP: value = raw(spanOpen+'<i class="red times circle outline icon large"></i></span>')
                                            break
                                        case RDStore.PERM_SILENT: value = raw(spanOpen+'<i class="hand point up large"></i></span>')
                                            break
                                        case RDStore.PERM_NOT_APPLICABLE: value = raw(spanOpen+'<i class="exclamation large"></i></span>')
                                            break
                                        case RDStore.PERM_UNKNOWN: value = raw(spanOpen+'<i class="question circle icon large"></i></span>')
                                            break
                                    }
                                    break
                                default: value = propValue.prop.refValue.getI10n("value")
                                    break
                                }
                                break
                            default: value = propValue.prop.value
                                break
                        }
                    }
                    else if(propValue.prop.paragraph)
                        value = propValue.prop.paragraph
                %>
                <td>
                    ${value}
                    <g:if test="${propValue.binding}"><div class="ui orange tag label">${message(code:'financials.isVisibleForSubscriber')}</div></g:if>
                </td>
            </g:if>
            <g:else>
                <td>
                    <span data-tooltip="${RDStore.PERM_UNKNOWN.getI10n("value")}"><i class="question circle icon large"></i></span>
                </td>
            </g:else>
        </g:each>
    </tr>
</g:each>