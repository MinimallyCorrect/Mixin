package org.minimallycorrect.mixin.internal;

import lombok.val;
import org.minimallycorrect.javatransformer.api.MethodInfo;
import org.minimallycorrect.javatransformer.api.code.CodeFragment;
import org.minimallycorrect.mixin.Inject;

class Injector {
	static void inject(MethodInfo target, MethodInfo injectable, Inject inject) {
		val targetFragment = target.getCodeFragment();
		val injectableFragment = injectable.getCodeFragment();

		if (targetFragment == null || injectableFragment == null)
			// TODO: log?/throw?
			return;

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
					throw new MixinError("Failed to inject " + injectable + " into " + fragment + " in " + target + " with " + inject, t);
				}
			i++;
		}
	}
}
