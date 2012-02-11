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
package org.bgp4j.netty.handlers;

import junit.framework.Assert;

import org.bgp4j.netty.BGPv4Constants;
import org.bgp4j.netty.BGPv4TestBase;
import org.bgp4j.netty.MockChannel;
import org.bgp4j.netty.MockChannelHandler;
import org.bgp4j.netty.MockChannelSink;
import org.bgp4j.netty.handlers.BGPv4Reframer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class BGPv4PacketReframerTest extends BGPv4TestBase {

	@Before
	public void before() {
		handler = obtainInstance(MockChannelHandler.class);
		sink = obtainInstance(MockChannelSink.class);;
		pipeline = Channels.pipeline(new ChannelHandler[] { obtainInstance(BGPv4Reframer.class), handler });
		channel = new MockChannel(pipeline, sink);		
	}
	
	@After
	public void after() {
		handler = null;
		sink = null;
		pipeline = null;
		channel = null;
	}

	private MockChannelHandler handler;
	private MockChannelSink sink;
	private ChannelPipeline pipeline;
	private MockChannel channel;
	
	@Test
	public void testValidPacket() throws Exception {
		byte[] packet = new byte[19];
		
		// KEEP alive packet
		for(int i=0; i<BGPv4Constants.BGP_PACKET_MARKER_LENGTH; i++)
			packet[i] = (byte)0xff;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH]     = 0x00;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 1] = 0x13;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 2] = 0x04;
		
		pipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(channel, packet));
		
		Assert.assertEquals(0, sink.getWaitingEventNumber());
		Assert.assertEquals(1, handler.getWaitingEventNumber());
		
		assertChannelEventContents(new byte[] { 0x04 }, handler.nextEvent());
	}

	@Test
	public void testValidPacketTwoParts() throws Exception {
		byte[] packet1 = new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,  
		};
		byte[] packet2 = new byte[] {
				(byte)0xff, (byte)0xff,
				0x00, 0x13, 0x04
		};

		pipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(channel, packet1));
		pipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(channel, packet2));

		Assert.assertEquals(0, sink.getWaitingEventNumber());
		Assert.assertEquals(1, handler.getWaitingEventNumber());
		
		assertChannelEventContents(new byte[] { 0x04 }, handler.nextEvent());
	}	

	@Test
	public void testBrokenMarker() throws Exception {
		byte[] packet = new byte[19];
		
		// KEEP alive packet
		packet[0] = (byte)0xfe; 
		for(int i=1; i<BGPv4Constants.BGP_PACKET_MARKER_LENGTH; i++)
			packet[i] = (byte)0xff;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH]     = 0x00;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 1] = 0x13;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 2] = 0x04;
		
		pipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(channel, packet));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, handler.getWaitingEventNumber());

		assertChannelEventContents(new byte[] { 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
				0x00, 0x15, // length
				0x03,       // notification type code
				0x01, 0x01, // Error code: Message Header Error, Error subcode: Connection not synchronized
				}, sink.nextEvent());
	}

	@Test
	public void testBrokenLengthShortPacket() throws Exception {
		byte[] packet = new byte[19];
		
		// KEEP alive packet
		for(int i=0; i<BGPv4Constants.BGP_PACKET_MARKER_LENGTH; i++)
			packet[i] = (byte)0xff;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH]     = 0x00;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 1] = 0x10;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 2] = 0x04;
		
		pipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(channel, packet));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, handler.getWaitingEventNumber());

		assertChannelEventContents(new byte[] { 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
				0x00, 0x17, // length
				0x03,       // notification type code
				0x01, 0x02, // Error code: Message Header Error, Error subcode: Bad message length
				0x00, 0x10  // Data: Broken length field
				}, sink.nextEvent());
	}

	@Test
	public void testBrokenLengthGiantPacket() throws Exception {
		byte[] packet = new byte[19];
		
		// KEEP alive packet
		for(int i=0; i<BGPv4Constants.BGP_PACKET_MARKER_LENGTH; i++)
			packet[i] = (byte)0xff;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH]     = 0x14; // 20*256 = 5120
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 1] = 0x13;
		packet[BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 2] = 0x04;
		
		pipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(channel, packet));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, handler.getWaitingEventNumber());

		MessageEvent notityEvent = sink.nextEvent();
		ChannelBuffer notifyBuffer = (ChannelBuffer)notityEvent.getMessage();		
		byte[] notifyPacket = new byte[notifyBuffer.readableBytes()];
		
		notifyBuffer.readBytes(notifyPacket);
		
		assertArraysEquals(new byte[] { 
			(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
			(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
			0x00, 0x17, // length
			0x03,       // notification type code
			0x01, 0x02, // Error code: Message Header Error, Error subcode: Bad message length
			0x14, 0x13  // Data: Broken length field
			}, notifyPacket);
	}
}