package org.minimallycorrect.mixin.internal;

public class MixinTarget {
	@Override
	public String toString() {
		return "mixin not applied";
	}

	private boolean boolMethodCallTarget() {
		return false;
	}

	public void voidInjectionTest() {
		if (boolMethodCallTarget()) {
			System.setProperty("boolMethodCallTarget", "true");
			return;
		}
		System.setProperty("boolMethodCallTarget", "false");
	}
}
