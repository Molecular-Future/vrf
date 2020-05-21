package org.mos.vrfblk;

import org.mos.mcore.odb.ODBDao;

import onight.tfw.ojpa.api.ServiceSpec;

public class ODSVRFDao extends ODBDao {

	public ODSVRFDao(ServiceSpec serviceSpec) {
		super(serviceSpec);
	}

	@Override
	public String getDomainName() {
		return "vrf.prop";
	}

	
}
