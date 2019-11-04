<div required="" class="field required">
    <label>${message(code: 'accessPoint.name', default: 'Name')}</label>
    <div class="ui form">
        <div class="grouped fields">
            <g:each status="i" in="${nameOptions}" var="nameOption" >
                <div class="field">
                    <div class="ui radio checkbox" onclick="fillNameField('${nameOption.value}');">
                        <input type="radio" name="frequency" ${ (i) == 0 ? 'checked=checked' : ''}>
                        <label>${nameOption.key} </label>
                    </div>
                </div>
            </g:each>
        </div>
    </div>
    <g:field readonly="${name != ''}" type="text" name="name" value="${name}" />
</div>
<r:script>
    function fillNameField(name) {
        $('#name').val(name);
        if (name !== '') {
            document.getElementById('name').readOnly = true;
        } else {
            document.getElementById('name').readOnly = false;
        }
    }
</r:script>
