
/**
 * Created by Ryan on 01/08/15.
 */

package cloudsim.ext.datacenter;

import java.util.*;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
//import cloudsim.ext.datacenter.KMeans;
import java.util.Map;


public class BeeVmLoadBalancer extends VmLoadBalancer implements CloudSimEventListener {

    /** Holds the count current active allcoations on each VM */
    private Map<Integer, VirtualMachineState> vmStatesList;//available or not available
    private Map<Integer, Integer> suitableNectarSource;

    public BeeVmLoadBalancer(DatacenterController dcb){
        dcb.addCloudSimEventListener(this);
        this.vmStatesList = dcb.getVmStatesList();
        this.suitableNectarSource =  new LinkedHashMap<Integer, Integer>();
    }
    /**
     * @return The VM id of a VM so that the number of active tasks on each VM is kept
     * 			evenly distributed among the VMs.
     */
    @Override
    public int getNextAvailableVm(){
        int vmId = -1;

        //Find the vm with least number of allocations

        // Convert Map to List
        List<Map.Entry<Integer, Integer>> list =
                new LinkedList<Map.Entry<Integer, Integer>>(suitableNectarSource.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
        for (Iterator<Map.Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
            Map.Entry<Integer, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
            suitableNectarSource.put(entry.getKey(), entry.getValue());
        }

//                for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
//                    System.out.println("[Key] : " + entry.getKey()
//                            + " [Value] : " + entry.getValue());

        //scoutBee.put(entry.getKey(), entry.getValue());
//                }

/****
 *
 *
 * */
        if (sortedMap.size() < vmStatesList.size()){
            for (int availableVmId : vmStatesList.keySet()){
                if (!sortedMap.containsKey(availableVmId)){
                    vmId = availableVmId;
                    break;
                }
            }
        } else {
            int currCount;
            int minCount = Integer.MAX_VALUE;

            for (int thisVmId : sortedMap.keySet()){
                currCount = sortedMap.get(thisVmId);
                if (currCount < minCount){
                    minCount = currCount;
                    vmId = thisVmId;
                }
            }
        }
        allocatedVm(vmId);
        return vmId;
    }

    //Waggle dance
    public void cloudSimEventFired(CloudSimEvent e) {
        System.out.println("-------------------------------CloudSimFired------------------------------------");
        System.out.println();
        System.out.println("CloudSimEventFired, SuitableNectarSource: " + suitableNectarSource);

        if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
            int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);

            Integer currCount = suitableNectarSource.remove(vmId);
            if (currCount == null){
                currCount = 1;
            } else {
                currCount++;
            }

            suitableNectarSource.put(vmId, currCount);

        } else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
            int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
            Integer currCount = suitableNectarSource.remove(vmId);
            if (currCount != null){
                currCount--;
                suitableNectarSource.put(vmId, currCount);
            }
        }


    }
}

