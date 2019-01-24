package com.k_int

import groovy.transform.CompileStatic
import org.hibernate.proxy.HibernateProxy

@CompileStatic
class ClassUtils {
  public static <T> T deproxy(def element) {
    if (element instanceof HibernateProxy) {
      return (T) ((HibernateProxy) element).getHibernateLazyInitializer().getImplementation();
    }
    return (T) element;
  }
}
