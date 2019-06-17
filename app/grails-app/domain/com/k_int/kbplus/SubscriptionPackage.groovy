package com.k_int.kbplus

import javax.persistence.Transient

class SubscriptionPackage {

  Subscription subscription
  Package pkg

  static mapping = {
                id column:'sp_id'
           version column:'sp_version'
      subscription column:'sp_sub_fk'
               pkg column:'sp_pkg_fk'
  }

  static constraints = {
    subscription(nullable:true, blank:false)
    pkg(nullable:true, blank:false)
  }

  @Transient
  static def refdataFind(params) {

    def result = [];
    def hqlParams = []
    def hqlString = "select sp from SubscriptionPackage as sp"

    if ( params.subFilter ) {
      hqlString += ' where sp.subscription.id = ?'
      hqlParams.add(params.long('subFilter'))
    }

    def results = SubscriptionPackage.executeQuery(hqlString,hqlParams)

    results?.each { t ->
      def resultText = t.subscription.name + '/' + t.pkg.name
      result.add([id:"${t.class.name}:${t.id}",text:resultText])
    }

    result
  }

  def getIssueEntitlementsofPackage(){

    def result = []

    this.subscription.issueEntitlements.findAll{it.status?.value == 'Current'}.each { iE ->

      if(TitleInstancePackagePlatform.findByIdAndPkg(iE.tipp?.id, pkg))
      {
        result << iE
      }

    }

    result
  }

  def getIEandPackageSize(){

    return '(<span data-tooltip="Titel in der Lizenz"><i class="ui icon archive"></i></span>' + this.getIssueEntitlementsofPackage().size() + ' / <span data-tooltip="Titel im Paket"><i class="ui icon book"></i></span>' + this.getCurrentTippsofPkg()?.size() + ')'
  }
  def getCurrentTippsofPkg()
  {
    def result = this.pkg.tipps?.findAll{it?.status?.value == 'Current'}

    result

  }

  def getPackageName(){

    return this.pkg.name
  }

}
