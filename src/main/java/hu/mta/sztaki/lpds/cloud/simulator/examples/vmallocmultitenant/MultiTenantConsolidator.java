package hu.mta.sztaki.lpds.cloud.simulator.examples.vmallocmultitenant;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.consolidation.Consolidator;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;

	/**
	 * A consolidator which supports secure enclaves on physical machines.
	 * 
	 * @author Rene Ponto
	 */

public class MultiTenantConsolidator extends Consolidator{

	public MultiTenantConsolidator(IaaSService toConsolidate, long consFreq) {
		super(toConsolidate, consFreq);
	}

	@Override
	protected void doConsolidation(PhysicalMachine[] pmList) {
		try {
			reoptimize(pmList);
		} catch (VMManagementException e) {
			e.printStackTrace();
		} catch (NetworkException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	private void reoptimize(PhysicalMachine[] pms) throws VMManagementException, NetworkException {
		// first, check if the number of active pms can be minimized
		for(PhysicalMachine actualPm : pms) {
			Map<VirtualMachine, PhysicalMachine> migrations = new HashMap<VirtualMachine, PhysicalMachine>();
			
			for(VirtualMachine vm : actualPm.publicVms) {
				// migrate vm to first fit pm
				PhysicalMachine target = null;
				for(PhysicalMachine pm : pms) {
					//TODO: MAY_PM_HOST_VM
					if(pm.isHostableRequest(vm.getResourceAllocation().allocated)) {
						pm.allocateResources(vm.getResourceAllocation().allocated, false, pm.migrationAllocLen);	// strict == false?
						target = pm;
						break;
					}	
				}
				if(target != null)
					migrations.put(vm, target);
			}
			
			if(actualPm.publicVms.size() == migrations.size()) {
				// commit the tentative migrations
				for(VirtualMachine vm : migrations.keySet()) {
					actualPm.migrateVM(vm, migrations.get(vm));
				}
				
				actualPm.switchoff(null);
			}
			else {
				migrations.clear();
			}
		}
		
		// check if a secure pm can take load from two (non-secure) pms		
		boolean changed = true;
		while(changed) {
			changed = false;
			PhysicalMachine securePm = null;
			for(PhysicalMachine pm : pms) {
//				if(pm.isSecure() && pm.getState().equals(State.OFF)) {
					securePm = pm;		//TODO has to be added to PhysicalMachine
					break;
//				}
			}
			if(securePm != null) {
				PhysicalMachine chosenPm1 = null;
				PhysicalMachine chosenPm2 = null;
				
				for(PhysicalMachine pm1 : pms) {
//					if(pm1.isSecure()) {
//						continue;
//					}
					for(PhysicalMachine pm2 : pms) {
//						if(pm2.isSecure()) {
//							continue;
//						}
						if(pm1 == pm2) {
							continue;
						}						
						// compare the resources
						double occupiedTotalProcessingPowerPm1 = pm1.getCapacities().getTotalProcessingPower() - 
								pm1.availableCapacities.getTotalProcessingPower() - pm1.freeCapacities.getTotalProcessingPower();
						long occupiedMemoryPm1 = pm1.getCapacities().getRequiredMemory() - pm1.availableCapacities.getRequiredMemory() 
								- pm1.freeCapacities.getRequiredMemory();
						double occupiedTotalProcessingPowerPm2 = pm2.getCapacities().getTotalProcessingPower() - 
								pm2.availableCapacities.getTotalProcessingPower() - pm2.freeCapacities.getTotalProcessingPower();
						long occupiedMemoryPm2 = pm2.getCapacities().getRequiredMemory() - pm2.availableCapacities.getRequiredMemory() 
								- pm2.freeCapacities.getRequiredMemory();
						
						if(occupiedTotalProcessingPowerPm1 + occupiedTotalProcessingPowerPm2 <= 
								securePm.getCapacities().getTotalProcessingPower() && occupiedMemoryPm1 + occupiedMemoryPm2 <= 
								securePm.getCapacities().getRequiredMemory()) {
							chosenPm1 = pm1;
							chosenPm2 = pm2;
						}
					}
				}
				
				if(chosenPm1 != null && chosenPm2 != null) {
					
					Logger.getGlobal().info("Secure PM " + securePm.hashCode() + " takes load from PMs " + chosenPm1.hashCode() + 
							" and " + chosenPm2.hashCode());
					securePm.turnon();
					for(VirtualMachine vm : chosenPm1.listVMs()) {
						chosenPm1.migrateVM(vm, securePm);
					}
					for(VirtualMachine vm : chosenPm2.listVMs()) {
						chosenPm2.migrateVM(vm, securePm);
					}
					
					chosenPm1.switchoff(null);
					chosenPm2.switchoff(null);
					changed = true;
				}
				
			}
		}
		// check if the load of a secure pm can be moved to a non-secure pm
		for(PhysicalMachine pm : pms) {
			if(/*pm.isSecure() && */pm.isRunning()) {
				//TODO flag needed for secure
				for(VirtualMachine vm : pm.listVMs()) {
					//TODO
				}
			}
		}
		
//		while(/*bed*/) {
//			PhysicalMachine unsecurePm;
//			unsecurePm.turnon();
//			for(int i = 0; i < securePm.publicVms.size(); i++) {
//				//migrate
//			}
//			securePm.switchOff(null);
//		}		
	}
	
}
