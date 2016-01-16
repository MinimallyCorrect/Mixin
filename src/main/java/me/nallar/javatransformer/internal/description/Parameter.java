package me.nallar.javatransformer.internal.description;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
public class Parameter extends Type {
	public final String name;

	public Parameter(Type t, String name) {
		super(t.real, t.generic);
		this.name = name;
	}

	public Parameter(String real, String generic, String name) {
		super(real, generic);
		this.name = name;
	}
}
