package tutorial.annotation.processor;

import javax.lang.model.element.Element;

/**
 * Date: 01/02/22
 * Time: 7:14 pm
 * This file is project specific to annotation-processor-tutorial
 * Author: Pramod Khalkar
 */
public class AnnotationProcessException extends Exception {
    private final Element element;

    public AnnotationProcessException(Element element, String msg, Object... args) {
        super(String.format(msg, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
