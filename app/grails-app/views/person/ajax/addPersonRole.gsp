<%@ page import="com.k_int.kbplus.PersonRole" %>

<div class="template-element">
	
	<strong>* ${roleRdv?.value}</strong>
	
	<g:if test="${roleType=='func'}">
		<input type="hidden" name="functionType.${timestamp}" value="${roleRdv?.id}" />
	</g:if>
	
	<g:if test="${roleType=='resp'}">
		<input type="hidden" name="responsibilityType.${timestamp}" value="${roleRdv?.id}" />
		
		for 
		   
		<g:select 
			name="${subjectType}.${timestamp}"
		    from="${allSubjects}" 
		    optionKey="id" 
		    optionValue="${subjectOptionValue}" />
		    
		<input type="hidden" name="subjectType.${timestamp}" value="${subjectType}" />
	</g:if>
	
	at
	
	<g:select 
		name="org.${timestamp}"
	    from="${allOrgs}" 
		value="${org?.id}"
	    optionKey="id" 
	    optionValue="" />
	
	<button class="template-element-delete-${timestamp}" type="button">Delete</button>
	<script>
		$('.template-element-delete-${timestamp}').click(function(){
			$(this).parent('.template-element').remove()
		})
	</script>
</div>


