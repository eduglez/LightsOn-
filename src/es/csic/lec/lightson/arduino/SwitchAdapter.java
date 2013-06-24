package es.csic.lec.lightson.arduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.ArrayList;
import java.util.Enumeration;


public class SwitchAdapter implements ISwitch, ICommNameProvider, SerialPortEventListener{
	
	//SINGLETON
	
	private static SwitchAdapter instance;
	
	public synchronized static SwitchAdapter getInstance(){
		if(instance==null){
			instance = new SwitchAdapter();
		}
		return instance;
	}
	
	private SwitchAdapter(){
		
	}
	
	
	
	
	
    private SerialPort serialPort;
    
    /**
     * Buffered input stream from the port
     */
    private InputStream input;
    
    /**
     * The output stream to the port
     */
    private OutputStream output;
    
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    
    /**
     * Arduino Data rate
     */
    private static final int DATA_RATE = 9600;
    
    
    public static byte COMMAND = 0x01;
    public static byte SWITCH_ON = 0x02;
    public static byte SWITCH_OFF = 0x04;
    public static byte IDENTIFY = 0x08;
    
//    public static byte RESPONSE = 0x00;
    public static byte OK=0x10;
    public static byte FAIL = 0x20;
    public static byte ON = 0x40;
    public static byte OFF = (byte)0x80;
    
    public void switchLightsOnAsync(){
    	byte commando = (byte)(COMMAND|SWITCH_ON);
    	
    	try {
			output.write(commando);
		} catch (IOException e) {
			arduinoCommandReturnListener.onException(new Exception("IOException Switching ON"));
		}
    }

    
    public void switchLightsOffAsync(){
    	byte commando = (byte)(COMMAND|SWITCH_OFF);
    	
    	try {
			output.write(commando);
		} catch (IOException e) {
			arduinoCommandReturnListener.onException(new Exception("IOException Switching OFF"));
		}
    }
    
    public void identifyAsync(){
    	byte commando = (byte)(COMMAND|IDENTIFY);
    	
    	try {
			output.write(commando);
		} catch (IOException e) {
			arduinoCommandReturnListener.onException(new Exception("IOException IDENTIFYing"));
		}
    	
    }
    
    public void close(){
    	try {
			if(input!=null){
				input.close();
			}
		} catch (IOException e) {
			arduinoCommandReturnListener.onException(new Exception("IO Exception on INPUT CLOSE"));
		}
    	
    	try{
    		if(output!=null){
    			output.close();
    		}
    	} catch (IOException e) {
			arduinoCommandReturnListener.onException(new Exception("IO Exception on OUTPUT CLOSE"));
		}
    	
        if (serialPort != null) {
            serialPort.close();
        }
        
        commPortStatusListener.onCommPortStatusChanged(COMM_PORT_STATUS.NO_CONNECTED);
    }
    

	@Override
	public String[] getCommNames() {
		ArrayList<String> puertosLibres = new ArrayList<String>();

        @SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier c = portEnum.nextElement();
            if (!c.isCurrentlyOwned()&&(c.getPortType()==CommPortIdentifier.PORT_SERIAL)) {
                puertosLibres.add(c.getName());
            }
        }

		return puertosLibres.toArray(new String[puertosLibres.size()]);
	}

	@Override
	public void setCommPort(String port){
		try {
			close();

			CommPortIdentifier portId = null;
			
			@SuppressWarnings("unchecked")
			Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
			
			// iterate through, looking for the port
			while (portEnum.hasMoreElements()) {
			    CommPortIdentifier currPortId = portEnum.nextElement();
			    if (currPortId.getName().equals(port)) {
			        portId = currPortId;
			        break;
			    }
			}

			if (portId == null) {
			    throw new Exception("Could not find "+port+" port.");
			}

			serialPort = (SerialPort) portId.open(this.getClass().getName(),
			        TIME_OUT);

			serialPort.setSerialPortParams(DATA_RATE,
			        SerialPort.DATABITS_8,
			        SerialPort.STOPBITS_2,
			        SerialPort.PARITY_NONE);

			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			if(input==null || output==null){
				throw new Exception("Could not connect to "+port+"!");
			}
			
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			if(commPortStatusListener!=null){
				commPortStatusListener.onCommPortStatusChanged(COMM_PORT_STATUS.CONNECTED);
			}
			
		} catch (Exception e) {
			close();
			commPortStatusListener.onCommPortException(e);
		}
	}

	private ICommPortStatusListener commPortStatusListener;
	
	@Override
	public void setCommPortStatusListener(
			ICommPortStatusListener commPortStatusListener) {
		this.commPortStatusListener=commPortStatusListener;
		
	}
	
	private ISwitchListener arduinoCommandReturnListener;
	
	@Override
	public void setSwtichCommandReceiver(
			ISwitchListener arduinoCommandListener) {
		this.arduinoCommandReturnListener=arduinoCommandListener;
		
	}
	
	private boolean is(byte value, byte mask){
		return ((value&mask)!=0);
	}
	
	@Override
	public void serialEvent(SerialPortEvent ev) {
		switch(ev.getEventType()){
			case SerialPortEvent.DATA_AVAILABLE:
				int data;
				
			try {
				while((data=input.read())!=-1){
					
					byte dataByte = (byte)data;
					if(!is(dataByte,COMMAND)){
						ISwitch.COMMAND command=null;
						ISwitch.RETURN ret=null;
						
						if(is(dataByte, SWITCH_ON)){
							command = ISwitch.COMMAND.SWITCH_ON;
						}
						
						if(is(dataByte, SWITCH_OFF)){
							command = ISwitch.COMMAND.SWITCH_OFF;
						}
						
						if(is(dataByte, IDENTIFY)){
							command = ISwitch.COMMAND.IDENTIFY;
						}
						
						if(is(dataByte, OK)){
							ret = ISwitch.RETURN.OK;
						}
						
						if(is(dataByte, FAIL)){
							ret = ISwitch.RETURN.FAIL;
						}
						
						if(is(dataByte, ON)){
							ret = ISwitch.RETURN.ON;
						}
						
						if(is(dataByte, OFF)){
							ret = ISwitch.RETURN.OFF;
						}
						
						if(command!=null && ret!=null){
							arduinoCommandReturnListener.switchCommandReturn(command, ret);
						}else{
							assert false:"Command received from arduino not known";
						}
						
					}else if(is(dataByte, COMMAND)){
						assert false:"Arduino can't send COMMANDS yet";
					}
				}
			} catch (IOException e) {
				arduinoCommandReturnListener.onException(new Exception("IOException Receiving data"));
			}
			
			break;
			
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				assert false:"Not activated EVENT for OUTPUT BUFFER EMPTY";
				break;
				
				
			case SerialPortEvent.OE:
				assert false:"Not activated EVENT for OVERFLOW";
				break;
			
			case SerialPortEvent.BI:
				assert false:"Not activated EVENT for BI";
				break;
			
			case SerialPortEvent.CD:
				assert false:"Not activated EVENT for CD";
				break;
			
			case SerialPortEvent.CTS:
				assert false:"Not activated EVENT for CTS";
				break;
			
			case SerialPortEvent.DSR:
				assert false:"Not activated EVENT for DSR";
				break;
				
			case SerialPortEvent.FE:
				assert false:"Not activated EVENT for FE";
				break;
				
			case SerialPortEvent.PE:
				assert false:"Not activated EVENT for PE";
				break;
			
			case SerialPortEvent.RI:
				assert false:"Not activated EVENT for RI";
				break;
				
			default:
				assert false:"SerialPortEvent Type Unknown";
				
		}
		
	}


	@Override
	public synchronized void sendCommand(COMMAND command) {
		switch(command){
			case SWITCH_ON:
				switchLightsOnAsync();
				break;
			case SWITCH_OFF:
				switchLightsOffAsync();
				break;
			case IDENTIFY:
				identifyAsync();
				break;
			case NOTHING:
				assert false:"Do your really want to send NOTHING to Arduino?";
				break;
			default:
				assert false:"Unknown COMMAND to send to Arduino";
		}
		
	}


}
