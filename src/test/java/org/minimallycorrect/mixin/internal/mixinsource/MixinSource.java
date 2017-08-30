package org.minimallycorrect.mixin.internal.mixinsource;

import org.minimallycorrect.mixin.*;
import org.minimallycorrect.mixin.internal.MixinTarget;

@Mixin
public abstract class MixinSource extends MixinTarget {
	@Add
	public static void addTest() {
		throw new RuntimeException();
	}

	@Override
	@Overwrite
	public String toString() {
		return "mixin applied";
	}

	@Injectable
	public void voidInjectableTest() {
		System.setProperty("mixinInjection", "true");
	}

	@Override
	@Inject(injectable = "voidInjectableTest", type = Type.BODY)
	@Synchronize
	public abstract void voidInjectionTest();
}
