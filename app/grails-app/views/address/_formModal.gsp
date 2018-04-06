<%@ page import="com.k_int.kbplus.Address" %>

<semui:modal id="addressFormModal" text="${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}">

    <g:form class="ui form" url="[controller: 'address', action: 'create']" method="POST">
        <input type="hidden" name="redirect" value="true" />

        <div class="field">
            <div class="three fields">
                <div class="field wide eight fieldcontain ${hasErrors(bean: addressInstance, field: 'street_1', 'error')} required">
                    <label for="street_1">
                        <g:message code="address.street_1.label" default="Street1" />
                        <span class="required-indicator">*</span>
                    </label>
                    <g:textField name="street_1" required="" value="${addressInstance?.street_1}"/>
                </div>

                <div class="field three wide fieldcontain ${hasErrors(bean: addressInstance, field: 'street_2', 'error')} ">
                    <label for="street_2">
                        <g:message code="address.street_2.label" default="Street2" />
                    </label>
                    <g:textField name="street_2" value="${addressInstance?.street_2}"/>
                </div>

                <div class="field five wide fieldcontain ${hasErrors(bean: addressInstance, field: 'pob', 'error')} ">
                    <label for="pob">
                        <g:message code="address.pob.label" default="Pob" />
                    </label>
                    <g:textField name="pob" value="${addressInstance?.pob}"/>
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field eight wide fieldcontain ${hasErrors(bean: addressInstance, field: 'zipcode', 'error')} required">
                    <label for="zipcode">
                        <g:message code="address.zipcode.label" default="Zipcode" />
                        <span class="required-indicator">*</span>
                    </label>
                    <g:textField name="zipcode" required="" value="${addressInstance?.zipcode}"/>
                </div>

                <div class="field eight wide fieldcontain ${hasErrors(bean: addressInstance, field: 'city', 'error')} required">
                    <label for="city">
                        <g:message code="address.city.label" default="City" />
                        <span class="required-indicator">*</span>
                    </label>
                    <g:textField name="city" required="" value="${addressInstance?.city}"/>
                </div>
            </div>

        </div>

        <div class="field">
            <div class="two fields">
                <div class="field eight wide fieldcontain ${hasErrors(bean: addressInstance, field: 'state', 'error')}">
                    <label for="state">
                        <g:message code="address.state.label" default="State" />
                    </label>
                    <laser:select class="ui dropdown" id="state" name="state.id"
                                  from="${com.k_int.kbplus.RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Federal State'))}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${addressInstance?.state?.id}"
                                  noSelection="['null': '']" />
                </div>

                <div class="field eight wide fieldcontain ${hasErrors(bean: addressInstance, field: 'country', 'error')}">
                    <label for="country">
                        <g:message code="address.country.label" default="Country" />
                    </label>
                    <laser:select class="ui dropdown" id="country" name="country.id"
                                  from="${com.k_int.kbplus.RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Country'))}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${addressInstance?.country?.id}"
                                  noSelection="['null': '']" />
                </div>
            </div>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: addressInstance, field: 'type', 'error')} ">
            <label for="type">
                <g:message code="address.type.label" default="Type" />
            </label>
            <laser:select class="ui dropdown" id="type" name="type.id"
                from="${com.k_int.kbplus.Address.getAllRefdataValues()}"
                optionKey="id"
                optionValue="value"
                value="${addressInstance?.type?.id}"
                required=""/>
        </div>

        <g:if test="${!orgId}">
            <div class="field fieldcontain ${hasErrors(bean: addressInstance, field: 'prs', 'error')} ">
                <label for="prs">
                    <g:message code="address.prs.label" default="Prs" />
                </label>
                <g:if test="${prsId}">
                    ${com.k_int.kbplus.Person.findById(prsId)}
                    <input id="prs" name="prs.id" type="hidden" value="${prsId}" />
                </g:if>
                <g:else>
                    <g:select id="prs" name="prs.id" from="${com.k_int.kbplus.Person.list()}" optionKey="id" value="${personInstance?.id}" class="many-to-one" noSelection="['null': '']"/>
                </g:else>
            </div>
        </g:if>

        <g:if test="${!prsId}">
            <div class="field fieldcontain ${hasErrors(bean: addressInstance, field: 'org', 'error')} ">
                <label for="org">
                    <g:message code="address.org.label" default="Org" />
                </label>
                <g:if test="${orgId}">
                    ${com.k_int.kbplus.Org.findById(orgId)}
                    <input id="org" name="org.id" type="hidden" value="${orgId}" />
                </g:if>
                <g:else>
                    <g:select id="org" name="org.id" from="${com.k_int.kbplus.Org.list()}" optionKey="id" value="${org?.id}" class="many-to-one" noSelection="['null': '']"/>
                </g:else>
            </div>
        </g:if>

    </g:form>
</semui:modal>