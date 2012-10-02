package at.univie.seattlesensors.sensors;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import at.univie.seattlesensors.SensorRegistry;

public class WifiConnectionSensor extends AbstractSensor {
	WifiManager mainWifi;
	private SensorValue ssid;
	private SensorValue ssid_hidden;
	private SensorValue bssid;
	private SensorValue ip;
	private SensorValue mac;
	private SensorValue supplicant_state;
	private SensorValue rssi;
	private SensorValue speed;
	//private SensorValue sConnectedAP;
	String AP = "";  // the AP that is connected to

	public WifiConnectionSensor(Context context){
		super(context);
		name = "Wifi Connection Sensor";
		
		ssid = new SensorValue(SensorValue.UNIT.STRING, SensorValue.TYPE.SSID);
		ssid_hidden = new SensorValue(SensorValue.UNIT.OTHER, SensorValue.TYPE.SSID_HIDDEN);
		bssid = new SensorValue(SensorValue.UNIT.STRING, SensorValue.TYPE.BSSID);
		ip = new SensorValue(SensorValue.UNIT.STRING, SensorValue.TYPE.DEVICE_IP);
		mac = new SensorValue(SensorValue.UNIT.STRING, SensorValue.TYPE.MAC_ADDRESS);
		supplicant_state = new SensorValue(SensorValue.UNIT.STRING, SensorValue.TYPE.STATE);
		rssi = new SensorValue(SensorValue.UNIT.DBM, SensorValue.TYPE.SIGNALSTRENGTH);
		speed = new SensorValue(SensorValue.UNIT.MBPS,SensorValue.TYPE.SPEED);
		//sConnectedAP = new SensorValue(SensorValue.UNIT.STRING, SensorValue.TYPE.WIFI_CONNECTION);
	}

	@Override
	protected void _enable() {
		mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = mainWifi.getConnectionInfo();  // the network that Android is connected to
		ssid.setValue(info.getSSID());
		ssid_hidden.setValue(info.getHiddenSSID());
		bssid.setValue(info.getBSSID());
		ip.setValue(Formatter.formatIpAddress(info.getIpAddress()));
		mac.setValue(info.getMacAddress());
		supplicant_state.setValue(info.getSupplicantState());
		rssi.setValue(info.getRssi());
		speed.setValue(info.getLinkSpeed());
		
//		List<WifiConfiguration> configs = mainWifi.getConfiguredNetworks();
//		for (WifiConfiguration config : configs) {
//			AP += "\n" + config.toString();
//		}
//		SensorRegistry.getInstance().log("WiFi", AP);
//		sConnectedAP.setValue(AP);
		notifyListeners();
	}

	@Override
	protected void _disable() {
	}
	
	@XMLRPCMethod
	public String ssid(){
		return (String) ssid.getValue();
	}
	
	@XMLRPCMethod
	public Object ssid_hidden(){
		return ssid_hidden.getValue();
	}
	
	@XMLRPCMethod
	public String bssid(){
		return (String) bssid.getValue();
	}
	
	@XMLRPCMethod
	public Object device_ip(){
		return ip.getValue();
	}
	
	@XMLRPCMethod
	public String mac(){
		return (String) mac.getValue();
	}
	
	@XMLRPCMethod
	public Object supplicant_state(){
		return supplicant_state.getValue();
	}
	
	@XMLRPCMethod
	public Object rssi(){
		return rssi.getValue();
	}
	
	@XMLRPCMethod
	public Object speed(){
		return speed.getValue();
	}
}
