/**
 * 
 */
package org.bgp4j.extension.snmp4j.extension;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.bgp4j.extension.snmp4j.config.nodes.EasyboxConfiguration;
import org.bgp4j.extension.snmp4j.config.nodes.impl.EasyBoxConfigurationParser;
import org.bgp4j.extensions.Extension;
import org.bgp4j.extensions.ExtensionBase;
import org.bgp4j.extensions.ExtensionBeanFactory;
import org.bgp4j.extensions.ProvidedRIBs;

/**
 * @author rainer
 *
 */
public class EasyboxSnmpExtension extends ExtensionBase implements Extension {

	private EasyBoxConfigurationParser parser;
	private List<EasyboxConfiguration> easyboxConfigurations = new LinkedList<EasyboxConfiguration>();
	
	@Override
	public String getName() {
		return "snmp4k-easybox";
	}

	@Override
	public void configure(HierarchicalConfiguration config) throws ConfigurationException {
		Set<String> keys = new HashSet<String>();
		
		for(HierarchicalConfiguration subConfig : config.configurationsAt("Easybox")) {
			EasyboxConfiguration ebc =parser.parseConfguration(subConfig);
			
			if(keys.contains(ebc.getName()))
				throw new ConfigurationException("duplicate Easybox " + ebc.getName());
			
			easyboxConfigurations.add(ebc);
			keys.add(ebc.getName());
		}
	}

	@Override
	public void startExtension() throws Exception {
	}

	@Override
	public void stopExtension() throws Exception {
	}

	@Override
	public Collection<ProvidedRIBs> getProvidedRIBs() {
		List<ProvidedRIBs> providedRibs = new LinkedList<ProvidedRIBs>();
		
		return providedRibs;
	}

	@Override
	public boolean isReadyForService() {
		return false;
	}

	@Override
	public void initialize(ExtensionBeanFactory beanFactory) {
		parser = beanFactory.getBeanInstance(EasyBoxConfigurationParser.class);
	}

}