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

package io.activej.rpc.client.sender;

import io.activej.common.annotation.ExposedInternals;
import io.activej.rpc.client.RpcClientConnectionPool;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

@ExposedInternals
public final class RpcStrategy_FirstAvailable implements RpcStrategy {
	private final List<? extends RpcStrategy> list;

	public RpcStrategy_FirstAvailable(List<? extends RpcStrategy> list) {
		this.list = list;
	}

	public List<? extends RpcStrategy> getList() {
		return list;
	}

	@Override
	public Set<InetSocketAddress> getAddresses() {
		return Utils.getAddresses(list);
	}

	@Override
	public @Nullable RpcSender createSender(RpcClientConnectionPool pool) {
		List<RpcSender> senders = Utils.listOfSenders(list, pool);
		if (senders.isEmpty())
			return null;
		return senders.get(0);
	}
}
