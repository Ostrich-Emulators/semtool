<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <tiles:insertAttribute name="header-includes" />
    <title>SEMOSS Webserver</title>
  </head>

  <body>
    <div id="pagebanner">
      <div class="banner-style">
        <tiles:insertAttribute name="banner-content" />
      </div>
    </div>


    <div id="pagewrapper">
      <div id="contentwrapper" class="ui-widget-content">
        <div class="content-title-style">
          <c:if test="${not empty pagetitle}">${pagetitle}</c:if>
          <c:if test="${empty pagetitle}">
            <tiles:insertAttribute name="title-content" />
          </c:if>
        </div>

        <div class="content-style">
          <tiles:insertAttribute name="primary-content" />
        </div>
      </div>
      <div class="footer-style ui-widget-header ui-corner-bottom">
        <tiles:insertAttribute name="footer-content" />
      </div>
    </div>
  </body>
</html>
