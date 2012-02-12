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
 * File: org.bgp4j.netty.handlers.UpdateAttributeCheckerTest.java 
 */
package org.bgp4j.netty.handlers;

import java.net.Inet4Address;

import junit.framework.Assert;

import org.bgp4j.netty.ASType;
import org.bgp4j.netty.BGPv4TestBase;
import org.bgp4j.netty.MockChannel;
import org.bgp4j.netty.MockChannelHandler;
import org.bgp4j.netty.MockChannelSink;
import org.bgp4j.netty.PeerConnectionInformation;
import org.bgp4j.netty.protocol.update.ASPathAttribute;
import org.bgp4j.netty.protocol.update.AggregatorPathAttribute;
import org.bgp4j.netty.protocol.update.Attribute;
import org.bgp4j.netty.protocol.update.AttributeFlagsNotificationPacket;
import org.bgp4j.netty.protocol.update.LocalPrefPathAttribute;
import org.bgp4j.netty.protocol.update.MalformedAttributeListNotificationPacket;
import org.bgp4j.netty.protocol.update.MissingWellKnownAttributeNotificationPacket;
import org.bgp4j.netty.protocol.update.NextHopPathAttribute;
import org.bgp4j.netty.protocol.update.OriginPathAttribute;
import org.bgp4j.netty.protocol.update.OriginPathAttribute.Origin;
import org.bgp4j.netty.protocol.update.OriginatorIDPathAttribute;
import org.bgp4j.netty.protocol.update.UpdatePacket;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class UpdateAttributeCheckerTest extends BGPv4TestBase {

	@Before
	public void before() {
		UpdateAttributeChecker checker = obtainInstance(UpdateAttributeChecker.class);
		
		channelHandler = obtainInstance(MockChannelHandler.class);
		sink = obtainInstance(MockChannelSink.class);
		pipeline = Channels.pipeline(new ChannelHandler[] { 
				checker,
				channelHandler });
		channel = new MockChannel(pipeline, sink);
		
		peerInfo = new PeerConnectionInformation();

		// attach the context object to the channel handler
		channel.getPipeline().getContext(checker).setAttachment(peerInfo);
	}
	
	@After
	public void after() {
		channel = null;
		channelHandler = null;
		sink = null;
		pipeline = null;
		peerInfo = null;
	}

	private MockChannelHandler channelHandler;
	private MockChannelSink sink;
	private ChannelPipeline pipeline;
	private MockChannel channel;
	private PeerConnectionInformation peerInfo;
	
	@Test
	public void testPassAllRequiredAttributes2OctetsASIBGPConnection() throws Exception {
		peerInfo.setAsType(ASType.AS_NUMBER_2OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64172);
		
		UpdatePacket update = new UpdatePacket();
		
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));
		
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(0, sink.getWaitingEventNumber());
		Assert.assertEquals(1, channelHandler.getWaitingEventNumber());
	
		UpdatePacket consumed = safeDowncast(safeExtractChannelEvent(channelHandler.nextEvent()), UpdatePacket.class);

		Assert.assertEquals(4, consumed.getPathAttributes().size());
	}
	
	@Test
	public void testPassAllRequiredAttributes4OctetsASIBGPConnection() throws Exception {
		peerInfo.setAsType(ASType.AS_NUMBER_4OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64172);
		
		UpdatePacket update = new UpdatePacket();
		
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_4OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));
		
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(0, sink.getWaitingEventNumber());
		Assert.assertEquals(1, channelHandler.getWaitingEventNumber());
	
		UpdatePacket consumed = safeDowncast(safeExtractChannelEvent(channelHandler.nextEvent()), UpdatePacket.class);

		Assert.assertEquals(4, consumed.getPathAttributes().size());
	}

	
	@Test
	public void testPassAllRequiredAttributes2OctetsASEBGPConnection() throws Exception {
		peerInfo.setAsType(ASType.AS_NUMBER_2OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64173);
		
		UpdatePacket update = new UpdatePacket();
		
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(0, sink.getWaitingEventNumber());
		Assert.assertEquals(1, channelHandler.getWaitingEventNumber());
	
		UpdatePacket consumed = safeDowncast(safeExtractChannelEvent(channelHandler.nextEvent()), UpdatePacket.class);

		Assert.assertEquals(3, consumed.getPathAttributes().size());
	}
	
	@Test
	public void testPassAllRequiredAttributes4OctetsASEBGPConnection() throws Exception {
		peerInfo.setAsType(ASType.AS_NUMBER_4OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64173);
		
		UpdatePacket update = new UpdatePacket();
		
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_4OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(0, sink.getWaitingEventNumber());
		Assert.assertEquals(1, channelHandler.getWaitingEventNumber());
	
		UpdatePacket consumed = safeDowncast(safeExtractChannelEvent(channelHandler.nextEvent()), UpdatePacket.class);

		Assert.assertEquals(3, consumed.getPathAttributes().size());
	}
	
	@Test
	public void testPassAllRequiredAttributesMissing2OctetsASIBGPConnection() throws Exception {
		peerInfo.setAsType(ASType.AS_NUMBER_2OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64172);
		
		UpdatePacket update = new UpdatePacket();
		
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
	}
	
	@Test
	public void testPassOneRequiredAttributesMissing4OctetsASIBGPConnection() throws Exception {
		UpdatePacket update;

		peerInfo.setAsType(ASType.AS_NUMBER_4OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64172);
		
		update = new UpdatePacket();
		// update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_4OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		update = new UpdatePacket();
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		// update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_4OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		update = new UpdatePacket();
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_4OCTETS));
		// update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		update = new UpdatePacket();
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_4OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		// update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
	}

	
	@Test
	public void testPassOneRequiredAttributesMissing2OctetsASIBGPConnection() throws Exception {
		UpdatePacket update;

		peerInfo.setAsType(ASType.AS_NUMBER_2OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64172);
		
		update = new UpdatePacket();
		// update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		update = new UpdatePacket();
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		// update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		update = new UpdatePacket();
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS));
		// update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		update = new UpdatePacket();
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		// update.getPathAttributes().add(new LocalPrefPathAttribute(100));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MissingWellKnownAttributeNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
	}
	
	@Test
	public void testInvalidAttributeFlags() throws Exception {
		UpdatePacket update;
		Attribute attr;
		
		peerInfo.setAsType(ASType.AS_NUMBER_2OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64172);
		
		// well-known mandatory
		update = new UpdatePacket();
		attr = new LocalPrefPathAttribute(100);
		attr.setTransitive(false); // bogus flag, must be true according to RFC 4271
		update.getPathAttributes().add(attr);
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
		Assert.assertEquals(AttributeFlagsNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		// well-known mandatory
		update = new UpdatePacket();
		attr = new LocalPrefPathAttribute(100);
		attr.setOptional(true); // bogus flag, must be false according to RFC 4271
		update.getPathAttributes().add(attr);
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
		Assert.assertEquals(AttributeFlagsNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());

		// optional transitive
		update = new UpdatePacket();
		attr = new AggregatorPathAttribute(ASType.AS_NUMBER_2OCTETS, 64173, (Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x04, 0x01 } ));
		attr.setTransitive(false); // bogus flag, must be true according to RFC 4271
		update.getPathAttributes().add(attr);
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
		Assert.assertEquals(AttributeFlagsNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
		
		// optional transitive
		update = new UpdatePacket();
		attr = new AggregatorPathAttribute(ASType.AS_NUMBER_2OCTETS, 64173, (Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x04, 0x01 } ));
		attr.setOptional(false); // bogus flag, must be true according to RFC 4271
		update.getPathAttributes().add(attr);
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
		Assert.assertEquals(AttributeFlagsNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());

		// optional non-transitive
		update = new UpdatePacket();
		attr = new OriginatorIDPathAttribute(1);
		attr.setOptional(false); // bogus flag, must be true according to RFC 4271
		update.getPathAttributes().add(attr);
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
		Assert.assertEquals(AttributeFlagsNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());

		// optional non-transitive
		update = new UpdatePacket();
		attr = new OriginatorIDPathAttribute(1);
		attr.setTransitive(true); // bogus flag, must be false according to RFC 4271
		update.getPathAttributes().add(attr);
		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
		Assert.assertEquals(AttributeFlagsNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
	}
	
	@Test
	public void testMismatchASNumberSizesFlags() throws Exception {
		UpdatePacket update;
		
		peerInfo.setAsType(ASType.AS_NUMBER_2OCTETS);
		peerInfo.setLocalAS(64172);
		peerInfo.setRemoteAS(64172);
		
		update = new UpdatePacket();
		update.getPathAttributes().add(new OriginPathAttribute(Origin.INCOMPLETE));
		update.getPathAttributes().add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS));
		update.getPathAttributes().add(new NextHopPathAttribute((Inet4Address)Inet4Address.getByAddress(new byte[] { (byte)0xc0, (byte)0xa8, 0x4, 0x1 })));
		update.getPathAttributes().add(new LocalPrefPathAttribute(100));
		update.getPathAttributes().add(new AggregatorPathAttribute(ASType.AS_NUMBER_4OCTETS));

		pipeline.sendUpstream(buildUpstreamBgpMessageEvent(channel, update));
		
		Assert.assertEquals(1, sink.getWaitingEventNumber());
		Assert.assertEquals(0, channelHandler.getWaitingEventNumber());
	
		Assert.assertEquals(MalformedAttributeListNotificationPacket.class, safeExtractChannelEvent(sink.getEvents().remove(0)).getClass());
	}
}