package gov.va.semoss.web.ui;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.servlet.context.ServletUtil;
import org.springframework.web.servlet.view.tiles2.TilesView;

public class AutoTilesView extends TilesView { 

private static final String PREFIX = "/WEB-INF/jsp/";
private static final String SUFFIX = ".jsp";

@Override
protected void renderMergedOutputModel(Map<String, Object> model,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
 
    ServletContext servletContext = getServletContext();
    TilesContainer container = ServletUtil.getContainer(servletContext);

    if (container == null) {
        throw new ServletException("Tiles container is not initialized. " +
                "Have you added a TilesConfigurer to your web application context?");
    }

    AttributeContext attributeContext = container.getAttributeContext(request, response);

    StringBuilder builder = new StringBuilder();
    builder.append(PREFIX);
    builder.append(this.getUrl());
    builder.append(SUFFIX);     

    attributeContext.putAttribute("body", Attribute.createTemplateAttribute(builder.toString()));

    super.renderMergedOutputModel(model, request, response);
}
}
