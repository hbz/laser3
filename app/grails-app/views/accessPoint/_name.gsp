<div required="" class="field required">
    <label>${message(code: 'accessPoint.name', default: 'Name')}</label>
    <div class="ui form">
        <div class="grouped fields">
            <g:each status="i" in="${nameOptions}" var="nameOption" >
                <div class="field">
                    <div class="ui radio checkbox" onclick="$('#name').val('${nameOption.value}')">
                        <input type="radio" name="frequency" ${ (i) == 0 ? 'checked=checked' : ''}>
                        <label>${nameOption.key} </label>
                    </div>
                </div>
            </g:each>
        </div>
    </div>
    <g:textField id='name' name="name" value="${name}" />
</div>
