package org.minimallycorrect.mixin.internal;

import lombok.NonNull;
import lombok.val;

import org.minimallycorrect.javatransformer.api.MethodInfo;
import org.minimallycorrect.javatransformer.api.code.CodeFragment;
import org.minimallycorrect.mixin.Inject;

class Injector {
	static void inject(@NonNull MethodInfo target, @NonNull MethodInfo injectable, Inject inject, boolean failOnError) {
		val targetFragment = target.getCodeFragment();
		val injectableFragment = injectable.getCodeFragment();

		if (targetFragment == null || injectableFragment == null) {
			val message = "Null fragments for:" +
				"\n\ttarget =" + target +
				"\n\ttargetFragment =" + targetFragment +
				"\n\tinjectable = " + injectable +
				"\n\tinjectable = " + injectableFragment;
			if (failOnError) {
				throw new MixinError(message);
			}
			System.err.println(message);
			return;
		}

		val fragments = targetFragment.findFragments(inject.type().getFragmentClass());

		val arg = inject.value();
		val index = inject.index();
		int i = 0;
		for (CodeFragment fragment : fragments) {
			if (!"".equals(arg)) {
				if (fragment instanceof CodeFragment.HasName)
					if (!((CodeFragment.HasName) fragment).getName().equals(arg))
						continue;
					else
						throw new UnsupportedOperationException("Unknown fragment class to match Inject.value into " + fragment.getClass());
			}
			// This must be the last check
			if (index == -1 || index == i)
				try {
					fragment.insert(injectableFragment, inject.position().getPosition());
				} catch (Throwable t) {
					val message = "Failed to inject " + injectable + " into " + fragment + " in " + target + " with " + inject;
					if (failOnError) {
						throw new MixinError(message, t);
					}
					System.err.println(message);
					t.printStackTrace();
				}
			i++;
		}
	}
}
