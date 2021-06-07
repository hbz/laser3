<%@page import="de.laser.reporting.myInstitution.GenericHelper" %>
<div class="ui ${tmplSize ?: 'small'} steps">
    <g:each in="${filterLabels}" var="lblGroup">
        <div class="step">
            <div class="content">
                <div class="title">${lblGroup.value.source}</div>
                <div class="description">
                    <g:each in="${lblGroup.value}" var="label">
                        <g:if test="${label.key != 'source'}">
                            <g:if test="${label.key in ['endDate', 'startDate']}">
                                - ${label.value.label} ${label.value.value} <br/>
                            </g:if>
                            <g:else>
                                - ${label.value.label}:
                                <g:if test="${GenericHelper.isFieldMultiple(label.key)}">
                                    <g:if test="${label.value.value instanceof String}">
                                        ${label.value.value} <br />
                                    </g:if>
                                     <g:else>
                                         ${label.value.value.join(', ')} <br />
                                     </g:else>
                                </g:if>
                                <g:else>
                                    ${label.value.value} <br/>
                                </g:else>
                            </g:else>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </div>
    </g:each>
</div>
