<laser:serviceInjection />

<div class="ui wide flyout" id="help-content" style="padding:50px 0 10px 0;overflow:scroll">
    <div class="content">
        <% print helpService.parseMarkdown('help/' + helpService.getMapping(controllerName, actionName) + '.md') %>
    </div>
</div>