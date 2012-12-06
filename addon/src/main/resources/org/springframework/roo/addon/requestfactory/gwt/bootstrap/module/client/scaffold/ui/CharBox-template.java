package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.dom.client.Document;
import com.github.gwtbootstrap.client.ui.ValueBox;

/**
 * A ValueBox that uses {@link CharParser} and {@link CharRenderer}.
 */
public class CharBox extends ValueBox<Character> {

    public CharBox() {
        super(Document.get().createTextInputElement(), CharRenderer.instance(), CharParser.instance());
    }
}
