package de.laser

import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages global Nationaler Statistikserver management views
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class StatsController  {

    /**
     * Shows the statistics overview for the Nationaler Statistikserver data situation
     */
  @Secured(['ROLE_ADMIN'])
  def statsHome() { 
    Map<String, Object> result = [:]

    result.orginfo = [:]

    result.instStats = Org.executeQuery('''
select distinct(o), count(u) 
from Org as o, User as u, UserOrg as uo 
where uo.user = u 
and uo.org = o
group by o
''');

    result.instStats.each { r ->
      storeOrgInfo(result.orginfo, r[0], 'userCount', r[1]);
    }

    result.soStats = Subscription.executeQuery('''
select distinct(o), count(s)
from Org as o, Subscription as s, OrgRole as orl
where orl.org = o 
and orl.sub = s 
and orl.roleType.value = 'Subscriber'
group by o
''');

    result.soStats.each { r ->
      storeOrgInfo(result.orginfo, r[0], 'subCount', r[1]);
    }

    result.currentsoStats = Subscription.executeQuery('''
select distinct(o), count(s)
from Org as o, Subscription as s, OrgRole as orl
where orl.org = o 
and orl.sub = s 
and orl.roleType.value = 'Subscriber'
group by o
''');
    result.currentsoStats.each { r ->
      storeOrgInfo(result.orginfo, r[0], 'currentSoCount', r[1]);
    }



    result.lStats = Subscription.executeQuery('''
select distinct(o), count(l)
from Org as o, License as l, OrgRole as orl
where orl.org = o 
and orl.lic = l 
and orl.roleType.value = 'Licensee'
group by o
''');
    result.lStats.each { r ->
      storeOrgInfo(result.orginfo, r[0], 'licCount', r[1]);
    }


   result.currentlStats = Subscription.executeQuery('''
select distinct(o), count(l)
from Org as o, License as l, OrgRole as orl
where orl.org = o 
and orl.lic = l 
and orl.roleType.value = 'Licensee'
group by o
''');
    result.currentlStats.each { r ->
      storeOrgInfo(result.orginfo, r[0], 'currentLicCount', r[1]);
    }

    withFormat {
      html result
      csv {
        response.setHeader("Content-disposition", "attachment; filename=KBPlusStats.csv")
        response.contentType = "text/csv"
        def out = response.outputStream
        out.withWriter { writer ->
          writer.write("Institution,Affiliated Users,Total Subscriptions, Current Subscriptions, Total Licenses, Current Licenses\n")
          result.orginfo.each { is ->
            writer.write("\"${is.key.name?:''}\",\"${is.value['userCount']?:''}\",\"${is.value['subCount']?:''}\",\"${is.value['currentSoCount']?:''}\",\"${is.value['licCount']?:''}\",\"${is.value['currentLicCount']?:''}\"\n")
          }

          writer.flush()
          writer.close()
        }
        out.close()
      }
    }

    result
  }

    /**
     * Sets the given value to the given map key of the given organisation entry
     * @param m the map to process
     * @param o the organisation for which the record should be stored
     * @param prop the key to store the value under
     * @param value the value to store
     */
  private def storeOrgInfo(m, o, prop, value) {
    if ( m[o] == null )
      m[o] = [:]

    m[o][prop] = value
  }
}
