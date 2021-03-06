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
 * File: org.bgp4j.netty.protocol.CeaseNotificationPacketTest.java 
 */
package org.bgp4j.netty.protocol;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;
import java.util.Iterator;

import org.bgp4j.net.AddressFamily;
import org.bgp4j.net.SubsequentAddressFamily;
import org.bgp4j.net.capabilities.AutonomousSystem4Capability;
import org.bgp4j.net.capabilities.Capability;
import org.bgp4j.net.capabilities.MultiProtocolCapability;
import org.bgp4j.net.capabilities.RouteRefreshCapability;
import org.bgp4j.net.packets.AdministrativeResetNotificationPacket;
import org.bgp4j.net.packets.AdministrativeShutdownNotificationPacket;
import org.bgp4j.net.packets.BGPv4Packet;
import org.bgp4j.net.packets.ConnectionCollisionResolutionNotificationPacket;
import org.bgp4j.net.packets.ConnectionRejectedNotificationPacket;
import org.bgp4j.net.packets.MaximumNumberOfPrefixesReachedNotificationPacket;
import org.bgp4j.net.packets.OtherConfigurationChangeNotificationPacket;
import org.bgp4j.net.packets.OutOfResourcesNotificationPacket;
import org.bgp4j.net.packets.PeerDeconfiguredNotificationPacket;
import org.bgp4j.net.packets.UnspecifiedCeaseNotificationPacket;
import org.bgp4j.net.packets.open.CapabilityListUnsupportedCapabilityNotificationPacket;
import org.bgp4j.netty.BGPv4TestBase;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class NotificationPacketTest extends BGPv4TestBase {

	private BGPv4PacketDecoder decoder = new BGPv4PacketDecoder();
	private BGPv4PacketEncoderFactory encoderFactory = new BGPv4PacketEncoderFactory();
		
	private ByteBuf encodePacket(BGPv4Packet packet) {
		ByteBuf buffer = allocator.buffer().order(ByteOrder.BIG_ENDIAN);
		
		encoderFactory.encoderForPacket(packet).encodePacket(packet, buffer);
		
		return buffer;
	}
	
	@Test
	public void testDecodeUnspecificCeaseNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x00, // UNSPECIFIC
		})), UnspecifiedCeaseNotificationPacket.class);
	}

	@Test
	public void testEncodeUnspecificCeaseNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x00, // UNSPECIFIC
		}, encodePacket(new UnspecifiedCeaseNotificationPacket()));
	}

	@Test
	public void testDecodeUnknownCeaseNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x0f, // Unknown subcode
		})), UnspecifiedCeaseNotificationPacket.class);
	}

	@Test
	public void testDecodeMaximumNumberOfPrefixesReachedNotificationPacket() {
		MaximumNumberOfPrefixesReachedNotificationPacket packet = safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x01, // Maximum number of prefixes reached
		})), MaximumNumberOfPrefixesReachedNotificationPacket.class);
		
		Assert.assertNull(packet.getAddressFamily());
		Assert.assertNull(packet.getSubsequentAddressFamily());
		Assert.assertEquals(0, packet.getPrefixUpperBound());

		packet = safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x01, // Maximum number of prefixes reached
				0x01,       // AFI IPv4
		})), MaximumNumberOfPrefixesReachedNotificationPacket.class);
		
		Assert.assertNull(packet.getAddressFamily());
		Assert.assertNull(packet.getSubsequentAddressFamily());
		Assert.assertEquals(0, packet.getPrefixUpperBound());

		packet = safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x01, // Maximum number of prefixes reached
				0x00, 0x01, // AFI IPv4
				0x01,       // SAI Unicast forwarding  
		})), MaximumNumberOfPrefixesReachedNotificationPacket.class);
		
		Assert.assertNull(packet.getAddressFamily());
		Assert.assertNull(packet.getSubsequentAddressFamily());
		Assert.assertEquals(0, packet.getPrefixUpperBound());

		packet = safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x01, // Maximum number of prefixes reached
				0x00, 0x7f, // AFI Illegal
				0x01,       // SAFI Unicast routing
				0x00, 0x00, 0x12, 0x34, // upper bound 0x1234
		})), MaximumNumberOfPrefixesReachedNotificationPacket.class);
		
		Assert.assertNull(packet.getAddressFamily());
		Assert.assertNull(packet.getSubsequentAddressFamily());
		Assert.assertEquals(0, packet.getPrefixUpperBound());

		packet = safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x01, // Maximum number of prefixes reached
				0x00, 0x01, // AFI IPv4
				0x01,       // SAFI Unicast routing
				0x00, 0x00, 0x12, 0x34, // upper bound 0x1234
		})), MaximumNumberOfPrefixesReachedNotificationPacket.class);
		
		Assert.assertEquals(AddressFamily.IPv4, packet.getAddressFamily());
		Assert.assertEquals(SubsequentAddressFamily.NLRI_UNICAST_FORWARDING, packet.getSubsequentAddressFamily());
		Assert.assertEquals(0x1234, packet.getPrefixUpperBound());
	}

	@Test
	public void testEncodeMaximumNumberOfPrefixesReachedPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x01, // Maximum number of prefixes reached
		}, encodePacket(new MaximumNumberOfPrefixesReachedNotificationPacket()));

		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x1c, // length 28 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x01, // Maximum number of prefixes reached
				0x00, 0x01, // AFI IPv4
				0x01,       // SAFI Unicast routing
				0x00, 0x00, 0x12, 0x34, // upper bound 0x1234
		}, encodePacket(new MaximumNumberOfPrefixesReachedNotificationPacket(AddressFamily.IPv4, 
				SubsequentAddressFamily.NLRI_UNICAST_FORWARDING, 0x1234)));
	}

	@Test
	public void testDecodeAdministrativeShutdownNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x02, // ADMINISTRATIVE SHUTDOWN
		})), AdministrativeShutdownNotificationPacket.class);
	}

	@Test
	public void testEncodeAdministrativeShutdownNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x02, // Administrative shutodwn
		}, encodePacket(new AdministrativeShutdownNotificationPacket()));
	}
	
	@Test
	public void testDecodePeerDecofiguredNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x03, // Peer deconfigured
		})), PeerDeconfiguredNotificationPacket.class);
	}

	@Test
	public void testEncodePeerDeconfiguredNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x03, // Peer deconfigured
		}, encodePacket(new PeerDeconfiguredNotificationPacket()));
	}
	
	@Test
	public void testDecodeAdministrativeResetNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x04, // Administrative reset
		})), AdministrativeResetNotificationPacket.class);
	}

	@Test
	public void testEncodeAdministrativeResetNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x04, // administrative reset
		}, encodePacket(new AdministrativeResetNotificationPacket()));
	}

	@Test
	public void testDecodeConnectionRejectedNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x05, // Connection rejected
		})), ConnectionRejectedNotificationPacket.class);
	}

	@Test
	public void testEncodeConnectionRejectedNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x05, // connection rejected
		}, encodePacket(new ConnectionRejectedNotificationPacket()));
	}

	@Test
	public void testDecodeOtherConfigurationChangeNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x06, // Other configuration change
		})), OtherConfigurationChangeNotificationPacket.class);
	}

	@Test
	public void testEncodeOtherConfigurationChangeNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x06, // other configuration change
		}, encodePacket(new OtherConfigurationChangeNotificationPacket()));
	}

	@Test
	public void testDecodeConnectionCollisionResolutionNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x07, // Connection collision resolution
		})), ConnectionCollisionResolutionNotificationPacket.class);
	}

	@Test
	public void testEncodeConnectionCollisionResolutionNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x07, // Connection collision resolution
		}, encodePacket(new ConnectionCollisionResolutionNotificationPacket()));
	}
	
	@Test
	public void testDecodeOutOfResourcesNotificationPacket() {
		safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x08, // UNSPECIFIC
		})), OutOfResourcesNotificationPacket.class);
	}

	@Test
	public void testEncodeOutOfResourcesNotificationPacket() {
		assertBufferContents(new byte[] {
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
				0x00, 0x15, // length 21 octets
				(byte)0x03, // type code NOTIFICATION
				(byte)0x06, // CEASE error code
				(byte)0x08, // Out of resources
		}, encodePacket(new OutOfResourcesNotificationPacket()));
	}
	
	@Test
	public void testDecodeUnsupportedCapabilityNotificationPacket() {
		CapabilityListUnsupportedCapabilityNotificationPacket packet = safeDowncast(decoder.decodePacket(buildProtocolPacket(new byte[] {
				(byte)0x03, // type code NOTIFICATION
				(byte)0x02, // error code: BGP Open
				(byte)0x07, // suberror code: Unsupported capability
				(byte)0x41, // Capability code 65: 4-octet AS numbers 
				(byte)0x04, // Capability length: 4 octets 
				(byte)0x00, (byte)0x00, (byte)0xfc, (byte)0x00, // AS number 64512
				0x02, // Capability code 2: Route refresh
				0x00, // Capability length: 0 octets
				0x01, // Capability code 1: Multi-Protocol 
				0x04, // Capability length: 4 octets
				0x00, 0x01, // Address Family 1: IPv4
				0x00,  // reserved
				0x01,  // Subsequent address family 1: Unicast forwarding
		})), CapabilityListUnsupportedCapabilityNotificationPacket.class);

		Iterator<Capability> capIt = packet.getCapabilities().iterator();
		
		Assert.assertTrue(capIt.hasNext());
		AutonomousSystem4Capability as4cap = (AutonomousSystem4Capability)capIt.next();
		Assert.assertEquals(64512, as4cap.getAutonomousSystem());
		
		Assert.assertTrue(capIt.hasNext());
		Assert.assertEquals(RouteRefreshCapability.class, capIt.next().getClass());
		
		Assert.assertTrue(capIt.hasNext());
		MultiProtocolCapability mpCap = (MultiProtocolCapability)capIt.next();
		Assert.assertEquals(AddressFamily.IPv4, mpCap.getAfi());
		Assert.assertEquals(SubsequentAddressFamily.NLRI_UNICAST_FORWARDING, mpCap.getSafi());
		
		Assert.assertFalse(capIt.hasNext());
	}

}
