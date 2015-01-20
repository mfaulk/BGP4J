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

import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bgp4j.net.NetworkLayerReachabilityInformation;
import org.bgp4j.net.attributes.PathAttribute;
import org.bgp4j.netty.BGPv4Constants;
import org.bgp4j.netty.NLRICodec;
import org.bgp4j.netty.protocol.BGPv4Packet;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class UpdatePacket extends BGPv4Packet {

	private List<NetworkLayerReachabilityInformation> withdrawnRoutes = new LinkedList<NetworkLayerReachabilityInformation>();
	private List<NetworkLayerReachabilityInformation> nlris = new LinkedList<NetworkLayerReachabilityInformation>();
	private List<PathAttribute> pathAttributes = new LinkedList<PathAttribute>();
	
	/* (non-Javadoc)
	 * @see org.bgp4j.netty.protocol.BGPv4Packet#encodePayload()
	 */
	@Override
	protected void encodePayload(ByteBuf buffer) {
		int withdrawnRoutesLengthIndex = buffer.writerIndex();
		
		buffer.writeShort(0); // placeholder for withdrawn routes length
		encodeWithdrawnRoutes(buffer);
		buffer.setShort(withdrawnRoutesLengthIndex, buffer.writerIndex() - withdrawnRoutesLengthIndex -2);
		
		int pathAttributesLengthIndex = buffer.writerIndex();
		
		buffer.writeShort(0); // placeholder for path attributes length
		encodePathAttributes(buffer);
		buffer.setShort(pathAttributesLengthIndex, buffer.writerIndex() - pathAttributesLengthIndex -2);
		
		encodeNlris(buffer);
	}

	/*
	public int calculatePacketSize() {
		int size = BGPv4Constants.BGP_PACKET_MIN_SIZE_UPDATE;

		size += calculateSizeWithdrawnRoutes();
		size += calculateSizePathAttributes();
		size += calculateSizeNlris();
		
		return size;
	}
	*/
	
	private void encodeWithdrawnRoutes(ByteBuf buffer) {
		if(this.withdrawnRoutes != null) {
			for (NetworkLayerReachabilityInformation route : withdrawnRoutes) {
				NLRICodec.encodeNLRI(buffer, route);
			}
		}
	}

	private void encodePathAttributes(ByteBuf buffer) {
		if(this.pathAttributes != null) {
			for(PathAttribute pathAttribute : pathAttributes) {
				PathAttributeCodec.encodePathAttribute(buffer, pathAttribute);
			}
		}
	}
	
	private void encodeNlris(ByteBuf buffer) {
		if(this.nlris != null) {
			for (NetworkLayerReachabilityInformation nlri : nlris) {
				NLRICodec.encodeNLRI(buffer, nlri);
			}
		}
	}

	/*
	private int calculateSizeWithdrawnRoutes() {
		int size = 0;

		if(this.withdrawnRoutes != null) {
			for (NetworkLayerReachabilityInformation route : withdrawnRoutes) {
				size += NLRICodec.calculateEncodedNLRILength(route);
			}
		}

		return size;
	}
	
	private int calculateSizeNlris() {
		int size = 0;

		if(this.nlris != null) {
			for (NetworkLayerReachabilityInformation nlri : nlris) {
				size += NLRICodec.calculateEncodedNLRILength(nlri);
			}
		}

		return size;
	}
	
	private int calculateSizePathAttributes() {
		int size = 0;
		
		if(this.pathAttributes != null) {
			for(PathAttribute  attr : pathAttributes)
				size += PathAttributeCodec.calculateEncodedPathAttributeLength(attr);
		}
		
		return size;
	}
	*/
	
	/* (non-Javadoc)
	 * @see org.bgp4j.netty.protocol.BGPv4Packet#getType()
	 */
	@Override
	public int getType() {
		return BGPv4Constants.BGP_PACKET_TYPE_UPDATE;
	}

	/**
	 * @return the withdrawnRoutes
	 */
	public List<NetworkLayerReachabilityInformation> getWithdrawnRoutes() {
		return withdrawnRoutes;
	}

	/**
	 * @param withdrawnRoutes the withdrawnRoutes to set
	 */
	public void setWithdrawnRoutes(List<NetworkLayerReachabilityInformation> withdrawnRoutes) {
		this.withdrawnRoutes = withdrawnRoutes;
	}

	/**
	 * @return the nlris
	 */
	public List<NetworkLayerReachabilityInformation> getNlris() {
		return nlris;
	}

	/**
	 * @param nlris the nlris to set
	 */
	public void setNlris(List<NetworkLayerReachabilityInformation> nlris) {
		this.nlris = nlris;
	}

	/**
	 * @return the pathAttributes
	 */
	public List<PathAttribute> getPathAttributes() {
		return pathAttributes;
	}

	/**
	 * @param pathAttributes the pathAttributes to set
	 */
	public void setPathAttributes(List<PathAttribute> pathAttributes) {
		this.pathAttributes = pathAttributes;
	}

	/**
	 * look up path attributes of a given type passed in this update packet
	 */
	@SuppressWarnings("unchecked")
	public <T extends PathAttribute> Set<T> lookupPathAttributes(Class<T> paClass) {
		Set<T> result = new HashSet<T>();
		
		for(PathAttribute pa : pathAttributes) {
			if(pa.getClass().equals(paClass))
				result.add((T)pa);
		}
		
		return result;
	}
	
	/**
	 * get the path attributes of this packet filtered by given PathAttribute classes
	 * 
	 * @param filteredClasses
	 * @return
	 */
	public Set<PathAttribute> filterPathAttributes(Collection<Class<? extends PathAttribute>> filteredClasses) {
		Set<PathAttribute> attrs = new HashSet<PathAttribute>();

		for(PathAttribute attr : getPathAttributes()) {
			if(!filteredClasses.contains(attr.getClass()))
				attrs.add(attr);
		}
		
		return attrs;
	}
	
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		
		for(NetworkLayerReachabilityInformation n : withdrawnRoutes)
			builder.append("withdrawnRoute", n);
		
		for(NetworkLayerReachabilityInformation n : nlris)
			builder.append("nlri", n);

		for(PathAttribute a : pathAttributes)
			builder.append("pathAttribute", a);
		
		return builder.toString();
	}
}
