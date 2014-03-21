package es.csic.lec.lightson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class RxTxDriverTest {
	
	class SerialPortEventsReceiver implements SerialPortEventListener{
		
		boolean dataAvailable;
		
		boolean outputBufferEmpty;
		
		boolean overrunError;
		
		public SerialPortEventsReceiver(){
			dataAvailable=false;
			outputBufferEmpty=false;
			overrunError=false;
		}
		
		@Override
		public void serialEvent(SerialPortEvent ev) {
			switch(ev.getEventType()){
				case SerialPortEvent.DATA_AVAILABLE:
					dataAvailable=true;
					System.out.println("DATA_AVAILABLE");
					break;
				
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				outputBufferEmpty=true;
				System.out.println("OUTPUT_BUFFER_EMPTY");
				break;
			
			case SerialPortEvent.OE:
				overrunError=true;
				System.out.println("OVERRUN_ERROR");
				break;
			}
		}
		
	}
	
	
	 SerialPort serialPort;
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
	     * Default bits per second for COM port.
	     */
	    private static final int DATA_RATE = 9600;
	
	    SerialPortEventsReceiver eventsReceiver;
	    
	@Before
	public void setup(){
		CommPortIdentifier portId = null;
		
		eventsReceiver=new SerialPortEventsReceiver();
		
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		
		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
		    CommPortIdentifier currPortId = portEnum.nextElement();
		    if (currPortId.getName().equals("COM1")) {
		        portId = currPortId;
		        break;
		    }
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
			        SerialPort.DATABITS_8,
			        SerialPort.STOPBITS_2,
			        SerialPort.PARITY_NONE);

			serialPort.enableReceiveTimeout(TIME_OUT);
			serialPort.addEventListener(eventsReceiver);
			serialPort.notifyOnOutputEmpty(true);
			serialPort.notifyOnDataAvailable(true);
			serialPort.notifyOnOverrunError(true);
			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			System.out.println("RXTX Ready");
		} catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException e) {
			fail(e.getMessage());
		}
	}
	
	@After
	public void cleanup(){
		try {
			if(input!=null)
				input.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		try {
			if(output!=null)
				output.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		
		if(serialPort!=null)
			serialPort.close();
		
	}
	
	@Test
	public void testRead() {
		try {
			int value=input.read();
			System.out.println(value);
			assertFalse(value!=-1);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testWrite() {
		try {
			output.write(5);
			Thread.sleep(5000);  
			System.out.println("Escrito");
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
		
	}

}
