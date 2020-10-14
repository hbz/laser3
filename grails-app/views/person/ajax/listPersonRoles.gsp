<%-- DEPRECATED --%>
<%-- DEPRECATED --%>
<%-- DEPRECATED --%>

<g:each in="${existingPrsLinks}" var="link">
	<div class="ui vertical ui-delete">

		<div class="field">
        	<label>${link.functionType?.getI10n('value')}${link.responsibilityType?.getI10n('value')}</label>

            <div class="two fields">
                <div class="field wide twelve">
                    <g:if test="${link.lic}">
                        <g:link controller="${linkController}" action="show" id="${link.lic.id}">${link.lic.reference}</g:link>  <br />
                    </g:if>
                    <g:if test="${link.pkg}">
                        <g:link controller="${linkController}" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>  <br />
                    </g:if>
                    <g:if test="${link.sub}">
                        <g:link controller="${linkController}" action="show" id="${link.sub.id}">${link.sub.name}</g:link>  <br />
                    </g:if>
                    <g:if test="${link.title}">
                        <g:link controller="${linkController}" action="show" id="${link.title.id}">${link.title.normTitle}</g:link>  <br />
                    </g:if>

                    <g:link controller="organisation" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                </div>
                <div class="field wide four">
                    <g:checkBox name="personRoleDeleteIds.${link?.id}" value="${link?.id}" checked="false" /> ${message('code':'default.button.delete.label')}
                </div>
            </div>
		</div>
	</div>
</g:each>



