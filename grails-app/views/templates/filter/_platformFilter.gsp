<%@ page import="de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:render template="/templates/filter/javascript" />
<ui:filter showFilterButton="true">
    <g:form controller="${controllerName}" action="${actionName}" method="get" class="ui form">
        <div class="three fields">
            <div class="field">
                <label>${message(code:'default.search.text')}</label>
                <input type="text" name="q" placeholder="${message(code:'default.search.ph')}" value="${params.q}" />
            </div>

            <div class="field">
                <label for="provider">${message(code: 'default.provider.label')}</label>

                <div class="ui input">
                    <input type="text" id="provider" name="provider"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.provider}"/>
                </div>
            </div>

            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.PLATFORM_STATUS) }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="three fields">
            <div class="field">
                <label for="ipSupport">${message(code: 'platform.auth.ip.supported')}</label>

                <select name="ipSupport" id="ipSupport" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.IP_AUTHENTICATION)}" var="ip">
                        <option <%=(params.list('ipSupport')?.contains(ip.id.toString())) ? 'selected="selected"' : ''%>
                                value="${ip.id}">
                            ${ip.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="shibbolethSupport">
                    ${message(code: 'platform.auth.shibboleth.supported')}
                </label>

                <select name="shibbolethSupport" id="shibbolethSupport" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)+ RDStore.GENERIC_NULL_VALUE}" var="shibboleth">
                        <option <%=(params.list('shibbolethSupport')?.contains(shibboleth.id.toString())) ? 'selected="selected"' : ''%>
                                value="${shibboleth.id}">
                            ${shibboleth.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="counterCertified">
                    ${message(code: 'platform.stats.counter.certified')}
                </label>

                <select name="counterCertified" id="counterCertified" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)+ RDStore.GENERIC_NULL_VALUE}" var="counter">
                        <option <%=(params.list('counterCertified')?.contains(counter.id.toString())) ? 'selected="selected"' : ''%>
                                value="${counter.id}">
                            ${counter.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="three fields">
            <div class="field"></div>
            <div class="field"></div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button" name="filterSet" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </g:form>
</ui:filter>