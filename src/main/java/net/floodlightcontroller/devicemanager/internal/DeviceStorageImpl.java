package net.floodlightcontroller.devicemanager.internal;

import java.util.List;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.thinkaurelius.titan.core.TitanException;
import net.floodlightcontroller.core.INetMapTopologyObjects.IDeviceObject;
import net.floodlightcontroller.core.INetMapTopologyObjects.IPortObject;
import net.floodlightcontroller.core.internal.SwitchStorageImpl;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceStorage;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.onrc.onos.util.GraphDBConnection;
import net.onrc.onos.util.GraphDBOperation;

public class DeviceStorageImpl implements IDeviceStorage {
	
	public GraphDBOperation op;
	protected static Logger log = LoggerFactory.getLogger(SwitchStorageImpl.class);

	@Override
	public void init(String conf) {
		op = new GraphDBOperation(GraphDBConnection.getInstance(conf));
	}	

	public void finalize() {		
		close();
	}
	
	@Override
	public void close() {
		op.close();
	}

	@Override
	public IDeviceObject addDevice(IDevice device) {
		// TODO Auto-generated method stub
		IDeviceObject obj = null;
 		try {
            if ((obj = op.searchDevice(device.getMACAddressString())) != null) {
                log.debug("Adding device {}: found existing device",device.getMACAddressString());
            } else {
            	obj = op.newDevice();
                log.debug("Adding device {}: creating new device",device.getMACAddressString());
            }
            changeDeviceAttachments(device, obj);
            
 			obj.setIPAddress(device.getIPv4Addresses().toString());
 			obj.setMACAddress(device.getMACAddressString());
 			obj.setState("ACTIVE");
 			op.commit();
 			
 			log.debug("Adding device {}",device.getMACAddressString());
		} catch (Exception e) {
            // TODO: handle exceptions
          	op.rollback();
			log.error(":addDevice mac:{} failed", device.getMACAddressString());
		}	
		
		return obj;
	}

	@Override
	public IDeviceObject updateDevice(IDevice device) {
		return addDevice(device);
	}

	@Override
	public void removeDevice(IDevice device) {
		// TODO Auto-generated method stub
		IDeviceObject dev;
		try {
			if ((dev = op.searchDevice(device.getMACAddressString())) != null) {
             	op.removeDevice(dev);
              	op.commit();
            	log.error("DeviceStorage:removeDevice mac:{} done", device.getMACAddressString());
            }
		} catch (Exception e) {
             // TODO: handle exceptions
          	op.rollback();
			log.error("DeviceStorage:removeDevice mac:{} failed", device.getMACAddressString());
		}
	}

	@Override
	public IDeviceObject getDeviceByMac(String mac) {
		return op.searchDevice(mac);
	}

	@Override
	public IDeviceObject getDeviceByIP(String ip) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void changeDeviceAttachments(IDevice device) {
		// TODO Auto-generated method stub
		IDeviceObject obj = null;
 		try {
            if ((obj = op.searchDevice(device.getMACAddressString())) != null) {
                log.debug("Changing device ports {}: found existing device",device.getMACAddressString());
                changeDeviceAttachments(device, obj);
     			op.commit();
           } else {
   				log.debug("failed to search device...now adding {}",device.getMACAddressString());
   				addDevice(device);
           }            			
		} catch (Exception e) {
            // TODO: handle exceptions
          	op.rollback();
			log.error(":addDevice mac:{} failed", device.getMACAddressString());
		}	
	}
	
	public void changeDeviceAttachments(IDevice device, IDeviceObject obj) {
		SwitchPort[] attachmentPoints = device.getAttachmentPoints();
		List<IPortObject> attachedPorts = Lists.newArrayList(obj.getAttachedPorts());

        for (SwitchPort ap : attachmentPoints) {
       	 IPortObject port = op.searchPort(
       			 HexString.toHexString(ap.getSwitchDPID()),
       			 (short) ap.getPort());
       	if (attachedPorts.contains(port)) {
       		attachedPorts.remove(port);
       	} else {
               log.debug("Adding device {}: attaching to port",device.getMACAddressString());
               port.setDevice(obj);
       		//obj.setHostPort(port);
       	}            		
       }
       for (IPortObject port: attachedPorts) {
       		port.removeDevice(obj);
       	//	obj.removeHostPort(port);
       }	
	}

	@Override
	public void changeDeviceIPv4Address(IDevice device) {
		// TODO Auto-generated method stub
		IDeviceObject obj;
  		try {
  			if ((obj = op.searchDevice(device.getMACAddressString())) != null) {
            	obj.setIPAddress(device.getIPv4Addresses().toString());
              	op.commit(); 
  			} else {
            	log.error(":changeDeviceIPv4Address mac:{} failed", device.getMACAddressString());
             }		
  		} catch (TitanException e) {
            // TODO: handle exceptions
          	op.rollback();
			log.error(":changeDeviceIPv4Address mac:{} failed due to exception {}", device.getMACAddressString(),e);
		}
	}

}
