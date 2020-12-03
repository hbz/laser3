<div id="jse_tmpl">
    <script>
        console.log('[TMPL_${pos}] script @ body > 5')
    </script>
    <script>
        $(function(){
            console.log('[TMPL_${pos}] script + $(doc).rdy() @ body > 6')
        })
    </script>
    <asset:script type="text/javascript">
        console.log('[TMPL_${pos}] asset:script @ body > 7')
    </asset:script>
    <asset:script type="text/javascript">
        $(function(){
            console.log('[TMPL_${pos}] asset:script + $(doc).rdy() @ body > 7')
        })
    </asset:script>
</div>
