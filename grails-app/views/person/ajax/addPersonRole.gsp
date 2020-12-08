<%@ page import="de.laser.PersonRole" %>

<%-- DEPRECATED --%>
<%-- DEPRECATED --%>
<%-- DEPRECATED --%>

<div class="ui vertical template-element template-element-${timestamp}">

	<div class="field">
        <label>${roleRdv?.getI10n('value')}</label>

        <div class="two fields">

            <div class="field wide twelve">
                <g:if test="${roleType=='func'}">
                    <input type="hidden" name="functionType.${timestamp}" value="${roleRdv?.id}" />
                </g:if>

                <g:select class="ui search dropdown"
                    name="org.${timestamp}"
                    from="${allOrgs}"
                    value="${org?.id}"
                    optionKey="id"
                    optionValue="" />

                <g:if test="${roleType=='resp'}">
                    <input type="hidden" name="responsibilityType.${timestamp}" value="${roleRdv?.id}" />

                    <g:select class="ui search dropdown"
                            name="${subjectType}.${timestamp}"
                            from="${allSubjects}"
                            optionKey="id"
                            optionValue="${subjectOptionValue}" />

                    <input type="hidden" name="subjectType.${timestamp}" value="${subjectType}" />
                </g:if>
            </div>
            <div class="field wide four">
                <button class="ui button template-element-delete-${timestamp}" type="button">${message('code':'default.button.remove.label')}</button>
            </div>


        </div>
	</div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.ui.search.dropdown').dropdown({
        fullTextSearch: 'exact'
    });

    $('.template-element-delete-${timestamp}').click(function(){
        $('.template-element-${timestamp}').remove()
    })

    $('.template-element-${timestamp} .ui.dropdown').dropdown({duration: 150, transition: 'fade'})
</laser:script>


