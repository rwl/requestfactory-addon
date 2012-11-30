package __TOP_LEVEL_PACKAGE__.activity;

import __TOP_LEVEL_PACKAGE__.place.ProxyListPlace;
import __TOP_LEVEL_PACKAGE__.place.ProxyPlace;
import __TOP_LEVEL_PACKAGE__.place.ProxyPlace.Operation;

import com.google.gwt.place.shared.Place;
import com.googlecode.mgwt.mvp.client.Animation;
import com.googlecode.mgwt.mvp.client.AnimationMapper;


public class ScaffoldAnimationMapper implements AnimationMapper {

	private static final Animation DEFAULT_ANIMATION = Animation.POP;

	@Override
	public Animation getAnimation(Place oldPlace, Place newPlace) {
		final Animation animation;

		if (oldPlace instanceof ProxyListPlace && newPlace instanceof ProxyListPlace) {
			animation = Animation.POP;
		} else if (oldPlace instanceof ProxyListPlace && newPlace instanceof ProxyPlace) {

			if (((ProxyListPlace) oldPlace).getProxyClass().equals(((ProxyPlace) newPlace).getProxyClass())) {
				animation = Animation.SLIDE;
			} else {
				animation = DEFAULT_ANIMATION;
			}
		} else if (oldPlace instanceof ProxyPlace && newPlace instanceof ProxyListPlace) {

			if (((ProxyPlace) oldPlace).getProxyClass().equals(((ProxyListPlace) newPlace).getProxyClass())) {
				animation = Animation.SLIDE_REVERSE;
			} else {
				animation = DEFAULT_ANIMATION;
			}
		} else if (oldPlace instanceof ProxyPlace && newPlace instanceof ProxyPlace) {

			if (((ProxyPlace) oldPlace).getOperation().equals(Operation.DETAILS) && ((ProxyPlace) newPlace).getOperation().equals(Operation.EDIT)) {
				animation = Animation.FLIP;
			} else if (((ProxyPlace) oldPlace).getOperation().equals(Operation.EDIT) && ((ProxyPlace) newPlace).getOperation().equals(Operation.DETAILS)) {
				animation = Animation.FLIP_REVERSE;
			} else if (((ProxyPlace) oldPlace).getOperation().equals(Operation.CREATE) && ((ProxyPlace) newPlace).getOperation().equals(Operation.DETAILS)) {
				animation = Animation.FLIP_REVERSE;
			} else {
				animation = Animation.POP;
			}
		} else {
			animation = DEFAULT_ANIMATION;
		}
		return animation;
	}
}
