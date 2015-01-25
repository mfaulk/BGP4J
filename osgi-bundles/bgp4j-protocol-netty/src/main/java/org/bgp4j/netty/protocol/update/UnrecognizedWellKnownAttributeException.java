/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 * File: org.bgp4j.netty.protocol.UnrecognizedAttributeException.java 
 */
package org.bgp4j.netty.protocol.update;

import org.bgp4j.net.attributes.PathAttribute;
import org.bgp4j.net.packets.NotificationPacket;
import org.bgp4j.net.packets.update.UnrecognizedWellKnownAttributeNotificationPacket;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class UnrecognizedWellKnownAttributeException extends AttributeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8298311237342239339L;

	/**
	 * @param offendingAttribute
	 */
	public UnrecognizedWellKnownAttributeException(PathAttribute offendingAttributes) {
		super(offendingAttributes);
	}

	/**
	 * @param message
	 * @param offendingAttribute
	 */
	public UnrecognizedWellKnownAttributeException(String message, PathAttribute offendingAttributes) {
		super(message, offendingAttributes);
	}

	@Override
	protected NotificationPacket toNotificationPacketUsingAttributes() {
		return new UnrecognizedWellKnownAttributeNotificationPacket(getOffendingAttribute());
	}

	@Override
	protected NotificationPacket toNotificationPacketUsingBytes() {
		return new UnrecognizedWellKnownAttributeNotificationPacket(getRawOffendingAttributes());
	}

}
