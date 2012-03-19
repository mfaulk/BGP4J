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
 */
package org.bgp4j.netty.protocol.update;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class UnknownPathAttribute extends PathAttribute {

	private int typeCode;
	private ChannelBuffer value;
	
	public UnknownPathAttribute(int typeCode, ChannelBuffer valueBuffer) {
		super(Category.OPTIONAL_TRANSITIVE);
		
		this.typeCode = typeCode;
		this.value = valueBuffer;
	}

	/**
	 * @return the value
	 */
	public ChannelBuffer getValue() {
		return ChannelBuffers.copiedBuffer(value);
	}

	/**
	 * @return the typeCode
	 */
	int getTypeCode() {
		return typeCode;
	}

}
