package hu.mta.sztaki.lpds.cloud.simulator.examples.vmallocmultitenant;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmconsolidation.ResourceVector;

/**
 * This class represents a request of a ComponentType. The variables of a request
 * are necessary to find/create a matching component instance.
 * 
 * @author Rene Ponto	 
 */
class Request {
	
	private String tenant;
	private ComponentType type;
	private ResourceVector cons;
	private boolean crit;
	private boolean custom;
	private boolean supportsSecureEnclaves;


	/**
	 * Defines a Request for a ComponentInstance from a tenant.
	 * @param tenant
	 * 			The name of the requesting tenant.
	 * @param type
	 * 			The requested component type.
	 * @param cons
	 * 			The requested resources.
	 * @param crit
	 * 			Determines the criticality of this request.
	 * @param custom
	 * 			Determines the usage of a custom component type.
	 * @param supportsSecureEnclaves
	 * 			Determines the support of secure enclaves.
	 */
	public Request(String tenant, ComponentType type, ResourceVector cons, boolean crit, boolean custom, boolean supportsSecureEnclaves) {
		this.tenant = tenant;
		this.type = type;
		this.cons = cons;
		this.crit = crit;
		this.custom = custom;
		this.supportsSecureEnclaves = supportsSecureEnclaves;
	}
	
	/**
	 * 
	 * @return The name of the tenant.
	 */
	public String getTenant() {
		return tenant;
	}
	
	/**
	 * 
	 * @return The requested ComponentType.
	 */
	public ComponentType getComponentType() {
		return type;
	}
	
	/**
	 * 
	 * @return The ResourceVector with the requested resources.
	 */
	public ResourceVector getResources() {
		return cons;
	}
	
	/**
	 * 
	 * @return True if critical data is stored.
	 */
	public boolean isCrit() {
		return crit;
	}
	
	/**
	 * 
	 * @return True if a custom component type shall be used.
	 */
	public boolean isCustom() {
		return custom;
	}
	
	/**
	 * 
	 * @return True if secure enclaves are supported.
	 */
	public boolean supportsSecureEnclaves() {
		return supportsSecureEnclaves;
	}
}
