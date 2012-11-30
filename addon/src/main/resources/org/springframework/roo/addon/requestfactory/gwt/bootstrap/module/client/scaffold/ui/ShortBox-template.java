package __TOP_LEVEL_PACKAGE__.ui;

import com.google.gwt.dom.client.Document;
import com.github.gwtbootstrap.client.ui.ValueBox;

/**
 * A ValueBox that uses {@link ShortParser} and {@link ShortRenderer}.
 */
public class ShortBox extends ValueBox<Short> {

	public ShortBox() {
		super(Document.get().createTextInputElement(), ShortRenderer.instance(), ShortParser.instance());
	}
}