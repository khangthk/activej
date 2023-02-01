/*
 * Copyright (C) 2020 ActiveJ LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.activej.codegen.expression;

import io.activej.codegen.Context;
import io.activej.common.annotation.ExplicitlyExposed;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import static io.activej.codegen.util.TypeChecks.*;

@ExplicitlyExposed
public final class Expression_Length implements Expression {
	private final Expression value;

	public Expression_Length(Expression value) {
		this.value = value;
	}

	public Expression getValue() {
		return value;
	}

	@Override
	public Type load(Context ctx) {
		GeneratorAdapter g = ctx.getGeneratorAdapter();

		Type valueType = value.load(ctx);
		checkType(valueType, isArray().or(isObject()));

		if (valueType.getSort() == Type.ARRAY) {
			g.arrayLength();
		} else if (valueType.getSort() == Type.OBJECT) {
			ctx.invoke(valueType, "size");
		}
		return Type.INT_TYPE;
	}
}
