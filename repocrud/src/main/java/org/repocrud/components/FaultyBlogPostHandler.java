package org.repocrud.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;

import javax.servlet.http.HttpServletResponse;

@Tag(Tag.DIV)
public class FaultyBlogPostHandler extends Component
        implements HasErrorParameter<IllegalArgumentException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event,
            ErrorParameter<IllegalArgumentException> parameter) {
        Label message = new Label(parameter.getCustomMessage());
        getElement().appendChild(message.getElement());

        return HttpServletResponse.SC_NOT_FOUND;
    }
}