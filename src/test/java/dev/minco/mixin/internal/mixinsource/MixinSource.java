package dev.minco.mixin.internal.mixinsource;

import dev.minco.mixin.*;
import dev.minco.mixin.internal.MixinTarget;

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
