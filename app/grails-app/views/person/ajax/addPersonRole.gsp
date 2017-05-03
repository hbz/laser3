<div class="template-element">	
	Role:
	<g:select 
		name="roleType.${timestamp}"
	    from="${roleTypes}" 
	    optionKey="id" 
	    optionValue="value" /> 
	
	<g:if test="${subjectType !='personRole'}">
		Subject:
		<g:select 
			name="${type}.${timestamp}"
		    from="${allSubjects}" 
		    optionKey="id" 
		    optionValue="${subjectOptionValue}" /> 
	</g:if>
	
	Context:
	<g:select 
		name="org.${timestamp}"
	    from="${allOrgs}" 
		value="${org?.id}"
	    optionKey="id" 
	    optionValue="" />
	
	<input type="hidden" name="type.${timestamp}" value="${type}" />
	
	<button class="template-element-delete-${timestamp}" type="button">Delete</button>
	<script>
		$('.template-element-delete-${timestamp}').click(function(){
			$(this).parent('.template-element').remove()
		})
	</script>
</div>


