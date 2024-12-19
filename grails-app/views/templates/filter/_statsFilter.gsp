<g:if test="${metricTypes}">
    <div class="field dynFilter">
        <label for="metricType"><g:message code="default.usage.metricType"/></label>
        <select name="metricType" id="metricType" <g:if test="${multiple}"> multiple="multiple" </g:if> class="ui search selection dropdown">
            <g:if test="${multiple}">
                <option value=""><g:message code="default.select.choose.label"/></option>
            </g:if>
            <g:each in="${metricTypes}" var="metricType">
                <option <%=(params.list('metricType')?.contains(metricType)) ? 'selected="selected"' : ''%> value="${metricType}">
                    ${metricType}
                </option>
            </g:each>
            <g:if test="${metricTypes.size() == 0}">
                <option value="<g:message code="default.stats.noMetric" />"><g:message code="default.stats.noMetric" /></option>
            </g:if>
        </select>
        <%--<div id="metricType" class="ui multiple search selection dropdown">
            <input type="hidden" name="metricType"/>
            <div class="text"></div>
            <i class="dropdown icon"></i>
        </div>
        --%>
    </div>
</g:if>
<g:if test="${accessMethods}">
    <div class="field dynFilter">
        <label for="accessMethod"><g:message code="default.usage.accessMethod"/></label>
        <select name="accessMethod" id="accessMethod" <g:if test="${multiple}"> multiple="multiple" </g:if> class="ui search selection dropdown">
            <g:if test="${multiple}">
                <option value=""><g:message code="default.select.choose.label"/></option>
            </g:if>
            <g:each in="${accessMethods}" var="accessMethod">
                <option <%=(params.list('accessMethod')?.contains(accessMethod)) ? 'selected="selected"' : ''%> value="${accessMethod}">
                    ${accessMethod}
                </option>
            </g:each>
            <g:if test="${accessMethods.size() == 0}">
                <option value="<g:message code="default.stats.noAccessMethod" />"><g:message code="default.stats.noAccessMethod" /></option>
            </g:if>
        </select>
        <%--<div id="accessMethod" class="ui multiple search selection dropdown">
            <input type="hidden" name="accessMethod"/>
            <div class="text"></div>
            <i class="dropdown icon"></i>
        </div>
        --%>
    </div>
</g:if>
<g:if test="${accessTypes}">
    <div class="field dynFilter">
        <label for="accessType"><g:message code="default.usage.accessType"/></label>
        <select name="accessType" id="accessType" <g:if test="${multiple}"> multiple="multiple" </g:if> class="ui search selection dropdown">
            <g:if test="${multiple}">
                <option value=""><g:message code="default.select.choose.label"/></option>
            </g:if>
            <g:each in="${accessTypes}" var="accessType">
                <option <%=(params.list('accessType')?.contains(accessType)) ? 'selected="selected"' : ''%> value="${accessType}">
                    ${accessType}
                </option>
            </g:each>
            <g:if test="${accessTypes.size() == 0}">
                <option value="<g:message code="default.stats.noAccessType" />"><g:message code="default.stats.noAccessType" /></option>
            </g:if>
        </select>
        <%--<div id="accessType" class="ui multiple search selection dropdown">
            <input type="hidden" name="accessType"/>
            <div class="text"></div>
            <i class="dropdown icon"></i>
        </div>
        --%>
    </div>
</g:if>
