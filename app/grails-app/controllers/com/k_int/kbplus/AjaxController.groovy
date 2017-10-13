package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import grails.plugins.springsecurity.Secured
import grails.converters.*
import com.k_int.properties.PropertyDefinition
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.apache.log4j.*

class AjaxController {
    def refdata_config = [
    "ContentProvider" : [
      domain:'Org',
      countQry:'select count(o) from Org as o where lower(o.name) like ?',
      rowQry:'select o from Org as o where lower(o.name) like ? order by o.name asc',
      qryParams:[
              [
                param:'sSearch',
                clos:{ value ->
                    def result = '%'
                    if ( value && ( value.length() > 0 ) )
                        result = "%${value.trim().toLowerCase()}%"
                    result
                }
              ]
      ],
      cols:['name'],
      format:'map'
    ],
    "Licenses" : [
      domain:'License',
      countQry:"select count(l) from License as l",
      rowQry:"select l from License as l",
      qryParams:[],
      cols:['reference'],
      format:'simple'
    ],
    'Currency' : [
      domain:'RefdataValue',
      countQry:"select count(rdv) from RefdataValue as rdv where rdv.owner.desc='Currency'",
      rowQry:"select rdv from RefdataValue as rdv where rdv.owner.desc='Currency'",
      qryParams:[
                   [
                      param:'iDisplayLength',
                      value: 200
                   ]
      ],
      cols:['value'],
      format:'simple'
    ]
  ]


  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def setValue() {
    // [id:1, value:JISC_Collections_NESLi2_Lic_IOP_Institute_of_Physics_NESLi2_2011-2012_01012011-31122012.., type:License, action:inPlaceSave, controller:ajax
    // def clazz=grailsApplication.domainClasses.findByFullName(params.type)
    // log.debug("setValue ${params}");
    def domain_class=grailsApplication.getArtefact('Domain',"com.k_int.kbplus.${params.type}")
    if ( domain_class ) {
      def instance = domain_class.getClazz().get(params.id) 
      if ( instance ) {
        // log.debug("Got instance ${instance}");
        def binding_properties = [ "${params.elementid}":params.value ]
        // log.debug("Merge: ${binding_properties}");
        // see http://grails.org/doc/latest/ref/Controllers/bindData.html
        if ( binding_properties[params.elementid] == '__NULL__' ) {
          binding_properties[params.elementid] = null;
        }
        bindData(instance, binding_properties)
        instance.save(flush:true);
      }
      else {
        log.debug("no instance");
      }
    }
    else {
      log.debug("no type");
    }

    response.setContentType('text/plain')
    def outs = response.outputStream
    outs << params.value
    outs.flush()
    outs.close()
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def setRef() {
    def rdv = RefdataCategory.lookupOrCreate(params.cat, params.value)
    def domain_class=grailsApplication.getArtefact('Domain',"com.k_int.kbplus.${params.type}")
    if ( domain_class ) {
      def instance = domain_class.getClazz().get(params.id)
      if ( instance ) {
        // log.debug("Got instance ${instance}");
        // Lookup refdata value
        def binding_properties = [ "${params.elementid}":rdv ]
        // see http://grails.org/doc/latest/ref/Controllers/bindData.html
        bindData(instance, binding_properties)
        instance.save(flush:true);
      }
      else {
        log.debug("no instance");
      }
    }
    else {
      log.debug("no type");
    }

    response.setContentType('text/plain')
    def outs = response.outputStream
    if ( rdv.icon ) {
      outs << "<span class=\"select-icon ${rdv.icon}\">&nbsp;</span><span>${rdv.value}</span>"
    }
    else {
      outs << "<span>${params.value}</span>"
    }
    outs.flush()
    outs.close()

  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def setFieldNote() {
    def domain_class=grailsApplication.getArtefact('Domain',"com.k_int.kbplus.${params.type}")
    if ( domain_class ) {
      def instance = domain_class.getClazz().get(params.id)
      if ( instance ) {
        if ( params.elementid?.startsWith('__fieldNote_') ) {
          def note_domain = params.elementid.substring(12)
          instance.setNote(note_domain, params.value);
          instance.save(flush:true)
        }
      }
    }
    else {
      log.error("no type");
    }

    response.setContentType('text/plain')
    def outs = response.outputStream
    outs << params.value
    outs.flush()
    outs.close()
  }

   @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def setFieldTableNote() {
    // log.debug("setFieldTableNote(${params})")
    def domain_class=grailsApplication.getArtefact('Domain',"com.k_int.kbplus.${params.type}")
    if ( domain_class ) {
      def instance = domain_class.getClazz().get(params.id)
       
      if ( instance ) {
        String temp = '__fieldNote_'+params.name
        if ( temp?.startsWith('__fieldNote_') ) {
          def note_domain = temp.substring(12)
          // log.debug("note_domain: " + note_domain +" : \""+ params.value+"\"")
          instance.setNote(note_domain, params.value);
          instance.save(flush:true)
        }
      }
    }
    else {
      log.error("no type");
    }

    response.setContentType('text/plain')
    def outs = response.outputStream
    outs << params.value
    outs.flush()
    outs.close()
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def genericSetValue() {
    // [id:1, value:JISC_Collections_NESLi2_Lic_IOP_Institute_of_Physics_NESLi2_2011-2012_01012011-31122012.., type:License, action:inPlaceSave, controller:ajax
    // def clazz=grailsApplication.domainClasses.findByFullName(params.type)
    // log.debug("genericSetValue:${params}");

    // params.elementid (The id from the html element)  must be formed as domain:pk:property:otherstuff
    String[] oid_components = params.elementid.split(":");

    def domain_class=grailsApplication.getArtefact('Domain',"com.k_int.kbplus.${oid_components[0]}")
    def result = params.value

    if ( domain_class ) {
      def instance = domain_class.getClazz().get(oid_components[1])
      if ( instance ) {

        def value = params.value;
        if ( value == '__NULL__' ) {
           value=null;
           result='';
        }
        else {
          if ( params.dt == 'date' ) {
            // log.debug("Special date processing, idf=${params.idf}");
            def formatter = new java.text.SimpleDateFormat(params.idf)
            value = formatter.parse(params.value)
            if ( params.odf ) {
              def of = new java.text.SimpleDateFormat(params.odf)
              result=of.format(value);
            }
            else {
              def of = new java.text.SimpleDateFormat(session.sessionPreferences?.globalDateFormat)
              result=of.format(value)
            }
          }
        }
        // log.debug("Got instance ${instance}");
        def binding_properties = [ "${oid_components[2]}":value ]
        // log.debug("Merge: ${binding_properties}");
        // see http://grails.org/doc/latest/ref/Controllers/bindData.html
        bindData(instance, binding_properties)
        instance.save(flush:true);
      }
      else {
        log.debug("no instance");
      }
    }
    else {
      log.debug("no type");
    }

    response.setContentType('text/plain')
    def outs = response.outputStream
    outs << result
    outs.flush()
    outs.close()
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def genericSetRel() {
    String[] target_components = params.pk.split(":");
    def result = ''

    def target=resolveOID(target_components);
    if ( target ) {
      if ( params.value == '' ) {
        // Allow user to set a rel to null be calling set rel ''
        target[params.name] = null
        target.save(flush:true);
      }
      else {
        String[] value_components = params.value.split(":");
        def value=resolveOID(value_components);

  
        if ( target && value ) {
          def binding_properties = [ "${params.name}":value ]
          bindData(target, binding_properties)
          target.save(flush:true);
          
          // We should clear the session values for a user if this is a user to force reload of the,
          // parameters.
          if (target instanceof User) {
            session.userPereferences = null
          }
          
          if ( params.resultProp ) {
            result = value[params.resultProp]
          }
          else {
            if ( value ) {
              result = renderObjectValue(value);
              // result = value.toString()
            }
          }
        }
        else {
          log.debug("no value (target=${target_components}, value=${value_components}");
        }
      }
    }
    else {
      log.error("no target (target=${target_components}, value=${value_components}");
    }

    // response.setContentType('text/plain')
    def resp = [ newValue: result ]
    // log.debug("return ${resp as JSON}");
    render resp as JSON
    //def outs = response.outputStream
    //outs << result
    //outs.flush()
    //outs.close()
  }

  def resolveOID(oid_components) {
    def result = null;

    def domain_class=null;

    if ( oid_components[0].startsWith("com.k_int.kbplus") ) 
      domain_class = grailsApplication.getArtefact('Domain',oid_components[0])
    else 
      domain_class = grailsApplication.getArtefact('Domain',"com.k_int.kbplus.${oid_components[0]}")


    if ( domain_class ) {
      result = domain_class.getClazz().get(oid_components[1])
    }
    else {
      log.error("resolve OID failed to identify a domain class. Input was ${oid_components}");
    }
    result
  }

  def orgs() {
    // log.debug("Orgs: ${params}");

    def result = [
      options:[]
    ]

    def query_params = ["%${params.query.trim().toLowerCase()}%"];

    // log.debug("q params: ${query_params}");

    // result.options = Org.executeQuery("select o.name from Org as o where lower(o.name) like ? order by o.name desc",["%${params.query.trim().toLowerCase()}%"],[max:10]);
    def ol = Org.executeQuery("select o from Org as o where lower(o.name) like ? order by o.name asc",query_params,[max:10,offset:0]);

    ol.each {
      result.options.add(it.name);
    }

    render result as JSON
  }

  def validatePackageId() {
    def result = [:]
    result.response = false;
    if( params.id ) {
      def p = Package.findByIdentifier(params.id)
      if ( !p ) {
        result.response = true
      }
    }

    render result as JSON
  }
  
  def refdataSearch() {

    // log.debug("refdataSearch params: ${params}");
    
    def result = [:]
    //we call toString in case we got a GString
    def config = refdata_config.get(params.id?.toString())

    if ( config == null ) {
      // If we werent able to locate a specific config override, assume the ID is just a refdata key
      config = [
        domain:'RefdataValue',
        countQry:"select count(rdv) from RefdataValue as rdv where rdv.owner.desc='${params.id}'",
        rowQry:"select rdv from RefdataValue as rdv where rdv.owner.desc='${params.id}'",
        qryParams:[],
        cols:['value'],
        format:'simple'
      ]
    }

    if ( config ) {

      // result.config = config

      def query_params = []
      config.qryParams.each { qp ->
        log.debug("Processing query param ${qp} value will be ${params[qp.param]}");
        if ( qp.clos ) {
          query_params.add(qp.clos(params[qp.param]?:''));
        }
        else {
          query_params.add(params[qp.param]);
        }
      }

      // log.debug("Params: ${query_params}");
      // log.debug("Count qry: ${config.countQry}");
      // log.debug("Row qry: ${config.rowQry}");

      def cq = Org.executeQuery(config.countQry,query_params);    

      def rq = Org.executeQuery(config.rowQry,
                                query_params,
                                [max:params.iDisplayLength?:10,offset:params.iDisplayStart?:0]);

      if ( config.format=='map' ) {
        result.aaData = []
        result.sEcho = params.sEcho
        result.iTotalRecords = cq[0]
        result.iTotalDisplayRecords = cq[0]
    
        rq.each { it ->
          def rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
          int ctr = 0;
          def row = [:]
          config.cols.each { cd ->
            // log.debug("Processing result col ${cd} pos ${ctr}");
            row["${ctr++}"] = rowobj[cd]
          }
          row["DT_RowId"] = "${rowobj.class.name}:${rowobj.id}"
          result.aaData.add(row)
        }
      }
      else {
        rq.each { it ->
          def rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
          result["${rowobj.class.name}:${rowobj.id}"] = rowobj[config.cols[0]];
        }
      }
    }

    // log.debug("refdataSearch returning ${result as JSON}");
    withFormat {
      html {
        result
      }
      json {
        render result as JSON
      }
    }
  }

  def sel2RefdataSearch() {

    // log.debug("sel2RefdataSearch params: ${params}");
    
    def result = []
    //we call toString in case we got a GString
    def config = refdata_config.get(params.id?.toString())

    if ( config == null ) {
      // If we werent able to locate a specific config override, assume the ID is just a refdata key
      config = [
        domain:'RefdataValue',
        countQry:"select count(rdv) from RefdataValue as rdv where rdv.owner.desc='${params.id}'",
        rowQry:"select rdv from RefdataValue as rdv where rdv.owner.desc='${params.id}'",
        qryParams:[],
        cols:['value'],
        format:'simple'
      ]
    }

    if ( config ) {

      def query_params = []
      config.qryParams.each { qp ->
        if ( qp?.clos) {
          query_params.add(qp.clos(params[qp.param]?:''));
        }
        else if(qp?.value) {
            params."${qp.param}" = qp?.value
        }
        else {
          query_params.add(params[qp.param]);
        }
      }

      def cq = RefdataValue.executeQuery(config.countQry,query_params);
      def rq = RefdataValue.executeQuery(config.rowQry,
                                query_params,
                                [max:params.iDisplayLength?:10,offset:params.iDisplayStart?:0]);

      rq.each { it ->
        def rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
        //def no_ws = rowobj[config.cols[0]].replaceAll(' ','');
        //def local_text = message(code:"refdata.${no_ws}", default:"${rowobj[config.cols[0]]}");
        //result.add([value:"${rowobj.class.name}:${rowobj.id}", text:"${local_text}"]);
        // i10n
        result.add([value:"${rowobj.class.name}:${rowobj.id}", text:"${it.getI10n(config.cols[0])}"]);
      }
    }
    else {
      log.error("No config for refdata search ${params.id}");
    }

    withFormat {
      html {
        result
      }
      json {
        render result as JSON
      }
    }
  }

  def addOrgRole() {
    // log.debug("addOrgRole ${params}");
    def org_to_link = resolveOID(params.orm_orgoid?.split(":"))
    def owner = resolveOID(params.parent?.split(":"))
    def rel = RefdataValue.get(params.orm_orgRole);

    // log.debug("Add link to ${org_to_link} from ${owner} rel is ${rel} recip_prop is ${params.recip_prop}");
    def new_link = new OrgRole(org:org_to_link,roleType:rel)
    new_link[params.recip_prop] = owner
    if ( new_link.save(flush:true) ) {
      // log.debug("Org link added");
    }
    else {
      log.error("Problem saving new org link...");
      new_link.errors.each { e ->
        log.error(e);
      }
    }

    redirect(url: request.getHeader('referer'))
  }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def addRefdataValue() {

        def newRefdataValue
        def error
        def msg

        def rdc = RefdataCategory.findById(params.refdata_category_id)

        if (RefdataValue.findByOwnerAndValue(rdc, params.refdata_value)) {
            error = message(code: "refdataValue.create_new.unique")
            log.debug(error)
        }
        else {
            newRefdataValue = new RefdataValue(value: params.refdata_value, owner: rdc, softData: true)
            newRefdataValue.save(flush: true)

            if (newRefdataValue?.hasErrors()) {
                log.error(newRefdataValue.errors)
                error = message(code: 'default.error')
            }
            else {
                msg = message(code: 'refdataValue.created', args: [newRefdataValue.value])
            }
        }

        if (params.reloadReferer) {
            flash.newRefdataValue = newRefdataValue
            flash.error   = error
            flash.message = msg
            redirect(url: params.reloadReferer)
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def addRefdataCategory() {

        def newRefdataCategory
        def error
        def msg

        def rdc = RefdataCategory.findByDesc(params.refdata_category)
        if (rdc) {
            error = message(code: 'refdataCategory.create_new.unique')
            log.debug(error)
        }
        else {
            newRefdataCategory = new RefdataCategory(desc: params.refdata_category, softData: true)
            newRefdataCategory.save(flush: true)

            if (newRefdataCategory?.hasErrors()) {
                log.error(newRefdataCategory.errors)
                error = message(code: 'default.error')
            }
            else {
                msg = message(code: 'refdataCategory.created', args: [newRefdataCategory.desc])
            }
        }

        if (params.reloadReferer) {
            flash.newRefdataCategory = newRefdataCategory
            flash.error   = error
            flash.message = msg
            redirect(url: params.reloadReferer)
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def addCustomPropertyType() {

        def newProp
        def error
        def msg
        def ownerClass = params.ownerClass // we might need this for addCustomPropertyValue
        def owner      = grailsApplication.getArtefact("Domain", ownerClass.replace("class ",""))?.getClazz()?.get(params.ownerId)

        if (PropertyDefinition.findByNameAndDescr(params.cust_prop_name, params.cust_prop_desc)) {
            error = message(code: 'propertyDefinition.name.unique')
        }
        else {
            if (params.cust_prop_type.equals(RefdataValue.toString())) {
                if (params.refdatacategory) {
                    newProp = PropertyDefinition.lookupOrCreate(params.cust_prop_name, params.cust_prop_type, params.cust_prop_desc)
                    def cat = RefdataCategory.get(params.refdatacategory)
                    newProp.setRefdataCategory(cat.desc)
                    newProp.save(flush: true)
                }
                else {
                    error = message(code: 'ajax.addCustPropertyType.error', default: 'Type creation failed. Please select a ref data type.')
                }
            }
            else {
                newProp = PropertyDefinition.lookupOrCreate(params.cust_prop_name, params.cust_prop_type, params.cust_prop_desc)
            }

            if (newProp?.hasErrors()) {
                log.error(newProp.errors)
                error = message(code: 'default.error')
            }
            else {
                msg = message(code: 'ajax.addCustPropertyType.success')
                newProp.softData = true
                newProp.save(flush: true)

                if (params.autoAdd == "on" && newProp) {
                    params.propIdent = newProp.id.toString()
                    chain(action: "addCustomPropertyValue", params: params)
                }
            }
        }

        request.setAttribute("editable", params.editable == "true")

        if (params.reloadReferer) {
            flash.newProp = newProp
            flash.error = error
            flash.message = msg
            redirect(url: params.reloadReferer)
        }
        else if (params.redirect) {
            flash.newProp = newProp
            flash.error = error
            flash.message = msg
            redirect(controller:"propertyDefinition", action:"create")
        }
        else {
            render(template: "/templates/properties/custom", model:[ownobj:owner, newProp:newProp, error:error, message: msg])
        }
    }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def addPrivatePropertyType(){
    // TODO has to be refactored to fit private property
    // TODO this doesnt work !!!
    // TODO has to be refactored to fit private property
    def newProp
    def error
    def ownerClass = params.ownerClass // we might need this for addPrivatePropertyValue
    def owner      = grailsApplication.getArtefact("Domain", ownerClass.replace("class ",""))?.getClazz()?.get(params.ownerId)
    if(params.cust_prop_type.equals(RefdataValue.toString())){
      if(params.refdatacategory){
        newProp = PropertyDefinition.lookupOrCreate(params.cust_prop_name, params.cust_prop_type, params.cust_prop_desc)
        def cat = RefdataCategory.get(params.refdatacategory)
        newProp.setRefdataCategory(cat.desc)
        newProp.save(flush:true)
      } else{
        error = "Type creation failed. Please select a ref data type."
      }
    } else{
      newProp = PropertyDefinition.lookupOrCreate(params.cust_prop_name, params.cust_prop_type, params.cust_prop_desc)
    }
    if(newProp?.hasErrors()){
      log.error(newProp.errors)
    } else{
      if(params.autoAdd == "on" && newProp){
        params.propIdent = newProp.id.toString()
        chain(action: "addPrivatePropertyValue", params:params)
      }
    }
    request.setAttribute("editable", params.editable == "true")
    if(params.redirect){
      flash.newProp = newProp
      flash.error = error
      redirect(controller:"propertyDefinition", action:"create")
    } else{
      render(template: "/templates/properties/private", model:[ownobj:owner, newProp:newProp, error:error])
    }
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def addCustomPropertyValue(){
    def error
    def newProp
    def owner = grailsApplication.getArtefact("Domain",params.ownerClass.replace("class ",""))?.getClazz()?.get(params.ownerId)
    def type  = PropertyDefinition.get(params.propIdent.toLong())

    def existingProp = owner.customProperties.find{it.type.name == type.name}

    if(existingProp == null){
        newProp = PropertyDefinition.createCustomPropertyValue(owner, type)
        if(newProp.hasErrors()){
            log.error(newProp.errors)
        } else{
            log.debug("New custom property created: " + newProp.type.name)
        }
    } else{
        error = message(code:'ajax.addCustomPropertyValue.error', default:'A property of this type is already added')
    }

    owner.refresh()
    request.setAttribute("editable", params.editable == "true")
    render(template: "/templates/properties/custom", model:[
            ownobj:owner,
            newProp:newProp,
            error:error,
            custom_props_div: "${params.custom_props_div}", // JS markup id
            prop_desc: type.descr // form data
    ])
  }

  /**
    * Add domain specific private property
    * @return
    */
  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def addPrivatePropertyValue(){
    def error
    def newProp
    def tenant = Org.get(params.tenantId)
    def owner  = grailsApplication.getArtefact("Domain", params.ownerClass.replace("class ",""))?.getClazz()?.get(params.ownerId)
    def type   = PropertyDefinition.get(params.propIdent.toLong())

    def existingProps = owner.privateProperties.findAll {
      it.owner.id  == owner.id
      it.type.name == type.name // this sucks due lazy proxy problem
      it.tenant.id == tenant.id
    }
    existingProps.removeAll{it.type.name != type.name} // dubious fix

    if(existingProps.size() == 0){
      newProp = PropertyDefinition.createPrivatePropertyValue(owner, tenant, type)
      if(newProp.hasErrors()){
        log.error(newProp.errors)
      } else{
        log.debug("New private property created: " + newProp.type.name)
      }
    } else{
      error = message(code:'ajax.addCustomPropertyValue.error', default:'A property of this type is already added')
    }

    owner.refresh()
    request.setAttribute("editable", params.editable == "true")
    render(template: "/templates/properties/private", model:[
            ownobj: owner,
            tenant: tenant,
            newProp: newProp,
            error: error,
            custom_props_div: "custom_props_div_${tenant.shortcode}", // JS markup id
            prop_desc: type.descr // form data
    ])
  }

  def delOrgRole() {
    // log.debug("delOrgRole ${params}");
    def or = OrgRole.get(params.id)
    or.delete(flush:true);
    // log.debug("Delete link: ${or}");
    redirect(url: request.getHeader('referer'))
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def deleteCustomProperty(){
    def className = params.propclass.split(" ")[1]
    def propClass = Class.forName(className)
    def property  = propClass.get(params.id)
    def owner     =  grailsApplication.getArtefact("Domain", params.ownerClass.replace("class ",""))?.getClazz()?.get(params.ownerId)
    def prop_desc = property.getType().getDescr()
    owner.customProperties.remove(property)
    property.delete(flush:true)

    if(property.hasErrors()){
        log.error(property.errors)
    } else{
        log.debug("Deleted custom property: " + property.type.name)
    }
    request.setAttribute("editable", params.editable == "true")
    render(template: "/templates/properties/custom", model:[
            ownobj:owner,
            newProp:property,
            custom_props_div: "${params.custom_props_div}", // JS markup id
            prop_desc: prop_desc // form data
    ])
  }

  /**
    * Delete domain specific private property
    *
    * @return
    */
  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def deletePrivateProperty(){
    def className = params.propclass.split(" ")[1]
    def propClass = Class.forName(className)
    def property  = propClass.get(params.id)
    def tenant    = property.tenant
    def owner     = grailsApplication.getArtefact("Domain", params.ownerClass.replace("class ",""))?.getClazz()?.get(params.ownerId)
    def prop_desc = property.getType().getDescr()

    owner.privateProperties.remove(property)
    property.delete(flush:true)

    if(property.hasErrors()){
      log.error(property.errors)
    } else{
      log.debug("Deleted private property: " + property.type.name)
    }
    request.setAttribute("editable", params.editable == "true")
    render(template: "/templates/properties/private", model:[
            ownobj: owner,
            tenant: tenant,
            newProp: property,
            custom_props_div: "custom_props_div_${tenant.shortcode}",  // JS markup id
            prop_desc: prop_desc // form data
    ])
  }

  def coreExtend(){
    log.debug("ajax::coreExtend:: ${params}")
    def tipID = params.tipID
    try{
      def sdf = new java.text.SimpleDateFormat(session.sessionPreferences?.globalDateFormat)
      def startDate = sdf.parse(params.coreStartDate)
      def endDate = params.coreEndDate? sdf.parse(params.coreEndDate) : null
      if(tipID && startDate){
        def tip = TitleInstitutionProvider.get(tipID)
        log.debug("Extending tip ${tip.id} with start ${startDate} and end ${endDate}")
        tip.extendCoreExtent(startDate, endDate)
        params.message = message(code:'ajax.coreExtend.success', default:'Core Dates extended')
      }
    }catch (Exception e){
        log.error("Error while extending core dates",e)
        params.message = message(code:'ajax.coreExtend.error', default:'Extending of core date failed.')
    }
    redirect(action:'getTipCoreDates',controller:'ajax',params:params)
  }

  def getTipCoreDates(){
    log.debug("ajax::getTipCoreDates:: ${params}")
    def tipID = params.tipID ?:params.id
    def tip = null
    if(tipID) tip = TitleInstitutionProvider.get(tipID);
    if(tip){
      def dates = tip.coreDates
      log.debug("Returning ${dates}")
      request.setAttribute("editable",params.editable?:true)
      render(template:"/templates/coreAssertionsModal",model:[message:params.message,coreDates:dates,tipID:tip.id,tip:tip]);    
    }
  } 
  def deleteCoreDate(){
    log.debug("ajax:: deleteCoreDate::${params}")
    def date = CoreAssertion.get(params.coreDateID)
    if(date) date.delete(flush:true)
    redirect(action:'getTipCoreDates',controller:'ajax',params:params)
  }

  def lookup() {
    // log.debug("AjaxController::lookup ${params}");
    def result = [:]
    // params.max = params.max ?: 20;
    params.max = params.max ?: 40;
    def domain_class = grailsApplication.getArtefact('Domain',params.baseClass)
    if ( domain_class ) {
      result.values = domain_class.getClazz().refdataFind(params);
    }
    else {
      log.error("Unable to locate domain class ${params.baseClass}");
      result.values=[]
    }
    //result.values = [[id:'Person:45',text:'Fred'],
    //                 [id:'Person:23',text:'Jim'],
    //                 [id:'Person:22',text:'Jimmy'],
    //                 [id:'Person:3',text:'JimBob']]
    render result as JSON
  }

  // used only from IdentifierTabLib.formAddIdentifier
  def lookup2() {
    def result = [:]
    def domain_class = grailsApplication.getArtefact('Domain', params.baseClass)
    if (domain_class) {
      result.values = domain_class.getClazz().refdataFind2(params);
    }
    else {
      log.error("Unable to locate domain class ${params.baseClass}");
      result.values=[]
    }
    render result as JSON
  }

  def addToCollection() {
    log.debug("AjaxController::addToCollection ${params}");

    def contextObj = resolveOID2(params.__context)
    def domain_class = grailsApplication.getArtefact('Domain',params.__newObjectClass)
    if ( domain_class ) {

      if ( contextObj ) {
        // log.debug("Create a new instance of ${params.__newObjectClass}");

        def new_obj = domain_class.getClazz().newInstance();

        domain_class.getPersistentProperties().each { p -> // list of GrailsDomainClassProperty
          // log.debug("${p.name} (assoc=${p.isAssociation()}) (oneToMany=${p.isOneToMany()}) (ManyToOne=${p.isManyToOne()}) (OneToOne=${p.isOneToOne()})");
          if ( params[p.name] ) {
            if ( p.isAssociation() ) {
              if ( p.isManyToOne() || p.isOneToOne() ) {
                // Set ref property
                // log.debug("set assoc ${p.name} to lookup of OID ${params[p.name]}");
                // if ( key == __new__ then we need to create a new instance )
                def new_assoc = resolveOID2(params[p.name])
                if(new_assoc){
                  new_obj[p.name] = new_assoc               
                }
              }
              else {
                // Add to collection
                // log.debug("add to collection ${p.name} for OID ${params[p.name]}");
                new_obj[p.name].add(resolveOID2(params[p.name]))
              }
            }
            else {
              // log.debug("Set simple prop ${p.name} = ${params[p.name]}");
              new_obj[p.name] = params[p.name]
            }
          }
        }

        if ( params.__recip ) {
          // log.debug("Set reciprocal property ${params.__recip} to ${contextObj}");
          new_obj[params.__recip] = contextObj
        }

        // log.debug("Saving ${new_obj}");
        try{
          if ( new_obj.save() ) {
            log.debug("Saved OK");
          }
          else {
            flash.domainError = new_obj
            new_obj.errors.each { e ->
              log.debug("Problem ${e}");
            }
          }
        }catch(Exception ex){

            flash.domainError = new_obj
            new_obj.errors.each { e ->
            log.debug("Problem ${e}");
            }
        }
      }
      else {
        log.debug("Unable to locate instance of context class with oid ${params.__context}");
      }
    }
    else {
      log.error("Unable to lookup domain class ${params.__newObjectClass}");
    }
    redirect(url: request.getHeader('referer'))
  }

  def validateIdentifierUniqueness(){
    log.debug("validateIdentifierUniqueness - ${params}")
    def result = [:]
    def owner = resolveOID2(params.owner)
    def identifier = resolveOID2(params.identifier)

    def owner_type = IdentifierOccurrence.getAttributeName(owner)
    if (!owner_type) {
      log.error("Unexpected Identifier Owner ${owner.class}")
      return null
    }

    // TODO: BUG !? multiple occurrences on the same object allowed
    def duplicates = identifier?.occurrences.findAll{it."${owner_type}" != owner && it."${owner_type}" != null}?.collect{it."${owner_type}"}
    if(duplicates){
      result.duplicates = duplicates
    }
    else{
      result.unique=true
    }
    log.debug("validateIdentifierUniqueness - ${result}")
    render result as JSON
  }

  def resolveOID2(oid) {
    def oid_components = oid.split(':');
    def result = null;
    def domain_class=null;
    domain_class = grailsApplication.getArtefact('Domain',oid_components[0])
    if ( domain_class ) {
      if ( oid_components[1]=='__new__' ) {
        result = domain_class.getClazz().refdataCreate(oid_components)
        // log.debug("Result of create ${oid} is ${result?.id}");
      }
      else {
        result = domain_class.getClazz().get(oid_components[1])
      }
    }
    else {
      log.error("resolve OID failed to identify a domain class. Input was ${oid_components}");
    }
    result
  }

  def deleteThrough() {
    // log.debug("deleteThrough(${params})");
    def context_object = resolveOID2(params.contextOid)
    def target_object = resolveOID2(params.targetOid)
    if ( context_object."${params.contextProperty}".contains(target_object) ) {
      def otr = context_object."${params.contextProperty}".remove(target_object)
      target_object.delete()
      context_object.save(flush:true);
    }
    redirect(url: request.getHeader('referer'))

  }

  def deleteManyToMany() {
    // log.debug("deleteManyToMany(${params})");
    def context_object = resolveOID2(params.contextOid)
    def target_object = resolveOID2(params.targetOid)
    if ( context_object."${params.contextProperty}".contains(target_object) ) {
      context_object."${params.contextProperty}".remove(target_object)
      context_object.save(flush:true);
    }
    redirect(url: request.getHeader('referer'))    
  }
  def validationException(final grails.validation.ValidationException exception){
    log.error(exception)
    response.status = 400
    response.setContentType('text/plain')
    def outs = response.outputStream
    outs << "Value validation failed"
  }

  def editableSetValue() {
    log.debug("editableSetValue ${params}");
    def target_object = resolveOID2(params.pk)
    
    if ( target_object ) {
      if ( params.type=='date' ) {
        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy/MM/dd'))
        if( params.value && params.value.size() > 0 ){
          def parsed_date = sdf.parse(params.value)
          target_object."${params.name}" = parsed_date
        } 
        else {
          target_object."${params.name}" = null
        }
      }
      else {
        def binding_properties = [:]
        binding_properties[ params.name ] = params.value
        bindData(target_object, binding_properties)
        // target_object."${params.name}" = params.value
      }
        target_object.save(failOnError:true,flush:true);

    }

    response.setContentType('text/plain')

    def outs = response.outputStream

    outs << target_object."${params.name}"
    
    outs.flush()
    outs.close()
  }

  def removeUserRole() {
    def user = resolveOID2(params.user);
    def role = resolveOID2(params.role);
    if ( user && role ) {
      com.k_int.kbplus.auth.UserRole.remove(user,role,true);
    }
    redirect(url: request.getHeader('referer'))    
  }

  /**
   * ToDo: This function is a duplicate of the one found in InplaceTagLib, both should be moved to a shared static utility
   */
  def renderObjectValue(value) {
    def result=''
    def not_set = message(code:'refdata.notSet')
    def no_ws =''

    if ( value ) {
      switch ( value.class ) {
        case com.k_int.kbplus.RefdataValue.class:
          no_ws = value.value.replaceAll(' ','')

          if ( value.icon != null ) {
            result="<span class=\"select-icon ${value.icon}\"></span>";
            result += message(code:"refdata.${no_ws}", default:"${value.value ?: not_set}")
          }
          else {
            result = message(code:"refdata.${no_ws}", default:"${value.value ?: not_set}")
          }
          break;
        default:
          if(value instanceof String){

          }else{
            value = value.toString()
          }
          no_ws = value.replaceAll(' ','')

          result = message(code:"refdata.${no_ws}", default:"${value ?: not_set}")
      }
    }
    // log.debug("Result of render: ${value} : ${result}");
    result;
  }

}
