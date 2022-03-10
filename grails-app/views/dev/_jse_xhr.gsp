<div>
    <script>
        console.log('[XHR] script @ body > 1')
    </script>
    <script>
        $(function(){
            console.log('[XHR] script + $(doc).rdy() @ body > 2')
        })
    </script>
    <asset:script type="text/javascript">
        console.log('[XHR] asset:script @ body > 3')
    </asset:script>
    <asset:script type="text/javascript">
        $(function(){
            console.log('[XHR] asset:script + $(doc).rdy() @ body > 4')
        })
    </asset:script>
    <p><strong>SUCCESS</strong></p>

    <laser:script file="${this.getGroovyPageFileName()}">
        console.log('[XHR] laser:script @ body > 5')
    </laser:script>
</div>
