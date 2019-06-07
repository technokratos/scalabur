package org.repocrud.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Span;

public class HtmlText extends Composite<Span> implements HasText {

        private Span content = new Span();
        private String text;

        public HtmlText(String htmlText) {
            setText(htmlText);
        }

        @Override
        protected Span initContent() {
            return content;
        }

        @Override
        public void setText(String htmlText) {
            if(htmlText == null) {
                htmlText = "";
            }
            if(htmlText.equals(text)) {
                return;
            }
            text = htmlText;
            content.removeAll();
            content.add(new Html("<div>" + htmlText + "</div>"));
        }

        @Override
        public String getText() {
            return text;
        }
    }