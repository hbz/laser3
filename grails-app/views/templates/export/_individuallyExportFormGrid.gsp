<%
    Map<String, Object> costItemFields = fields.value.fields.get('costItemsElements'),
                        otherFields = fields.value.fields.findAll { Map.Entry f -> f.getKey() != 'costItemsElements' }
%>
<g:if test="${costItemFields}">
    <div class="ui grid">
        <g:each in="${costItemFields}" var="field" status="gc">
            <g:if test="${gc == 0 || gc == Math.floor((costItemFields.size() / 2))}">
                <div class="wide eight field">
            </g:if>
            <div class="field">
                <g:if test="${subTabPrefix}">
                    <g:if test="${field.key.contains(subTabPrefix)}">
                        <div class="ui checkbox">
                            <input type="checkbox" name="iex:${field.key}" id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                            <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                        </div>
                    </g:if>
                </g:if>
                <g:else>
                    <div class="ui checkbox">
                        <input type="checkbox" name="iex:${field.key}" id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                        <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                    </div>
                </g:else>
            </div>
            <g:if test="${gc == Math.floor((costItemFields.size() / 2))-1 || gc == costItemFields.size()-1}">
                </div><!-- .wide eight gc -->
            </g:if>
        </g:each>
    </div><!-- .grid -->
    <div class="ui divider"></div>
</g:if>
<div class="ui grid">
    <g:each in="${otherFields}" var="field" status="fc">
        <g:if test="${fc == 0 || fc == Math.floor((otherFields.size() / 2))}">
            <div class="wide eight field">
        </g:if>
            <div class="field">
                <g:if test="${subTabPrefix}">
                    <g:if test="${field.key.contains(subTabPrefix)}">
                        <div class="ui checkbox">
                            <input type="checkbox" name="iex:${field.key}" id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                            <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                        </div>
                    </g:if>
                </g:if>
                <g:else>
                    <div class="ui checkbox">
                        <input type="checkbox" name="iex:${field.key}" id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                        <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                    </div>
                </g:else>
            </div>
        <g:if test="${fc == Math.floor((otherFields.size() / 2))-1 || fc == otherFields.size()-1}">
            </div><!-- .wide eight fc -->
        </g:if>
    </g:each>
</div><!-- .grid -->