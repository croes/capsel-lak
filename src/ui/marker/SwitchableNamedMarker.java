package ui.marker;

import de.fhpotsdam.unfolding.geo.Location;
import de.looksgood.ani.Ani;
import de.looksgood.ani.easing.Easing;

public class SwitchableNamedMarker extends NamedMarker {

	private static enum SwitchState {
		ONE, TO_TWO, TWO, TO_ONE;
	}

	private final String str_one;
	private final String str_two;
	
	private final Location loc_one;
	private final Location loc_two;

	private float switchCurrent;

	private SwitchState switchState;

	public SwitchableNamedMarker(String s1, Location l1, String s2, Location l2) {
		this(s1, l1, s2, l2, true);
	}
	
	public SwitchableNamedMarker(String s1, Location l1, String s2, Location l2, boolean showTwo) {
		this(s1, l1, s2, l2, showTwo, true);
	}

	public SwitchableNamedMarker(String s1, Location l1, String s2, Location l2, boolean showTwo, boolean animated) {
		super(s1, new Location(showTwo ? l2 : l1), animated);
		
		str_one = s1;
		str_two = s2;
		
		loc_one = l1;
		loc_two = l2;
		
		switchState = showTwo ? SwitchState.TWO : SwitchState.ONE;
		switchCurrent = showTwo ? 1 : 0;
	}

	@Override
	public String getText() {
		if (switchState == SwitchState.TWO)
			return str_two;
		return str_one;
	}
	
	@SuppressWarnings("unused")
	private void switchCallback() {
		if (switchState == SwitchState.TO_TWO)
			switchState = SwitchState.TWO;
		else
			switchState = SwitchState.ONE;
	}
	
	public void switchMarker(boolean two) {
		if (two && (switchState == SwitchState.TWO || switchState == SwitchState.TO_TWO))
			return;
		if (!two && (switchState == SwitchState.ONE || switchState == SwitchState.TO_ONE))
			return;
		
		if (!animated) {
			switchState = two ? SwitchState.TWO : SwitchState.ONE;
			switchCurrent = two ? 100 : 0;
			return;
		}
		
		if (two) {
			switchState = SwitchState.TO_TWO;
			Ani.to(this, ANI_DURATION, "switchCurrent", 100, Easing.LINEAR, "onUpdate:updateLocation,onEnd:switchCallback").start();
		} else {
			switchState = SwitchState.TO_ONE;
			Ani.to(this, ANI_DURATION, "switchCurrent", 0, Easing.LINEAR, "onUpdate:updateLocation,onEnd:switchCallback").start();
		}
	}
	
	@SuppressWarnings("unused")
	private void updateLocation() {
		switch(switchState) {
		case ONE:
			location.set(loc_one);
			break;
		case TWO:
			location.set(loc_two);
			break;
		default:
			location.set(
					(loc_two.x * switchCurrent + loc_one.x * (100 - switchCurrent)) / 100f,
					(loc_two.y * switchCurrent + loc_one.y * (100 - switchCurrent)) / 100f,
					0
					);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((str_one == null) ? 0 : str_one.hashCode());
		result = prime * result + ((str_two == null) ? 0 : str_two.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwitchableNamedMarker other = (SwitchableNamedMarker) obj;
		if (str_one == null) {
			if (other.str_one != null)
				return false;
		} else if (!str_one.equals(other.str_one))
			return false;
		if (str_two == null) {
			if (other.str_two != null)
				return false;
		} else if (!str_two.equals(other.str_two))
			return false;
		return true;
	}
	
	
}