package __TOP_LEVEL_PACKAGE__.ui;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.Renderer;

public class DefaultCell<T> extends AbstractCell<T> {
	private final Renderer<T> renderer;

	DefaultCell(Renderer<T> renderer) {
		this.renderer = renderer;
	}

	@Override
	public void render(Context context, T value, SafeHtmlBuilder sb) {
		sb.appendEscaped(renderer.render(value));
	}
}