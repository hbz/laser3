<%@ page import="de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection />

<ui:objectMineIcon/>

<laser:script file="${this.getGroovyPageFileName()}">
  $(document).ready(function() {
    $('.la-objectIsMine').visibility({
      type   : 'fixed',
      offset : 55,
      zIndex: 101
    })
  })
</laser:script>
