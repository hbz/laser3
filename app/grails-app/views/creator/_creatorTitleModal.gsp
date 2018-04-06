<semui:modal id="creatorTitle_add_modal" text="${tmplText}">
    <g:form id="create_creator_title_link" class="ui form" url="[controller:'ajax', action:'addCreatorToTitle']" method="post" >
        <input type="hidden" name="creator.id" value="${creatorInstance?.id}"/>

    <div class="fieldcontain ${hasErrors(bean: creatorTitleInstance, field: 'role', 'error')} required">
        <label for="role">
            <g:message code="creatorTitle.role.label" default="Role"/>
            <span class="required-indicator">*</span>
        </label>
        <laser:select id="role" name="role.id"
                      from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('CreatorType')}"
                      optionKey="id" required="" optionValue="value" value="${creatorTitleInstance?.role?.id}"
                      class="many-to-one"/>

    </div>

    <div class="fieldcontain ${hasErrors(bean: creatorTitleInstance, field: 'title', 'error')} required">
        <label for="title">
            <g:message code="creatorTitle.title.label" default="Title"/>
            <span class="required-indicator">*</span>
        </label>
        <g:select id="title" name="title.id" from="${com.k_int.kbplus.TitleInstance.list()}" optionKey="id"
                  optionValue="title" required="" value="${creatorTitleInstance?.title?.id}" class="many-to-one"/>

    </div>
    </g:form>
</semui:modal>