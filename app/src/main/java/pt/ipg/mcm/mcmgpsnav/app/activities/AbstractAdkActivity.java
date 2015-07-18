package pt.ipg.mcm.mcmgpsnav.app.activities;

/**
 * Created by Tiago Fernandes on 15-06-2015.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public abstract class AbstractAdkActivity extends Activity {

    private static int REQUEST_USB_PERMISSION = 0;
    // The permission identifier which must match the one from the Arduino
    private static final String ACTION_USB_PERMISSION = "WorkshopArduinoAndroidADK2014.usb_permission";
    private PendingIntent pendingIntentUsbPerm;

    private UsbManager usbManager;
    private UsbAccessory usbAccessory;
    private ParcelFileDescriptor parcelFileDescriptor;
    private FileInputStream adkInputStream;
    private FileOutputStream adkOutputStream;
    boolean firstRequestPermission;

    // Abstract functions that must be implemented in the
    protected abstract void doOnCreate(Bundle savedInstanceState);
    protected abstract void doAdkRead(String stringIn);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the System USB Service
        usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        // Create an Intent filter and filter USB Accessory Detach events
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        // Register a Broadcast receiver for the Intent events
        registerReceiver(usbBroadcastReceiver, intentFilter);

        // Create an Intent to Ask USB Permission from user
        Intent intentUsbPerm = new Intent(ACTION_USB_PERMISSION);
        pendingIntentUsbPerm = PendingIntent.getBroadcast(
                this,      			// context
                REQUEST_USB_PERMISSION,	// request code
                intentUsbPerm, 		// intent
                0);      			// flags

        // Create an Intent filter and filter USB Permission request events
        IntentFilter intentFilterUsbPerm = new IntentFilter(ACTION_USB_PERMISSION);

        // Register a Broadcast receiver for the Intent events
        registerReceiver(usbPermBroadcastReceiver, intentFilterUsbPerm);

        // Set as the first request permission
        firstRequestPermission = true;

        // Run the doOnCreate function
        doOnCreate(savedInstanceState);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected void onResume() {
        super.onResume();

        if(adkInputStream == null || adkOutputStream == null) {

            UsbAccessory[] usbAccessoryList = usbManager.getAccessoryList();
            UsbAccessory usbAccessory = null;
            if(usbAccessoryList != null){
                // Get the first USB Accessory from the Accessory list
                usbAccessory = usbAccessoryList[0];

                // See if it's a valid USB Accessory
                if(usbAccessory != null){
                    // If we already have the permission open the Accessory
                    if(usbManager.hasPermission(usbAccessory)){
                        // Open the USB Accessory
                        openUsbAccessory(usbAccessory);
                    } // Else request the permission from the user
                    else {
                        if(firstRequestPermission) {
                            firstRequestPermission = false;
                            // We may have several threads so this should be
                            // synchronized
                            synchronized(usbBroadcastReceiver) {
                                // Request permission from the user
                                usbManager.requestPermission(usbAccessory, pendingIntentUsbPerm);
                            }
                        }

                    }
                }
            }
        }
    }



    // Write String to Adk (Arduino)
    protected void writeAdk(String text){
        byte[] buffer = text.getBytes();

        if(adkOutputStream != null){
            try {
                // Write buffer data to the ADK Output stream
                adkOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        // Close the USB Accessory
        closeUsbAccessory();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the Broadcast receivers
        unregisterReceiver(usbBroadcastReceiver);
        unregisterReceiver(usbPermBroadcastReceiver);
    }




    // Runnable that will be responsible for reading data from the ADK
    private final Runnable runnableReadAdk = new Runnable() {
        @Override
        public void run() {
            int numberOfByteRead = 0;
            // When reading data from an accessory with a FileInputStream object we must
            // ensure that the buffer that we use is big enough to store the USB packet data
            // The Android accessory protocol supports packet buffers up to 16384 bytes,
            // so we can choose to always declare the buffer to be of this size for simplicity
            // For the purpose of this workshop 255 is enough
            byte[] buffer = new byte[255];

            // While have bytes to read, read them (only fails when the reading fails)
            while(numberOfByteRead >= 0) {
                try {
                    // Read from ADK input stream
                    numberOfByteRead = adkInputStream.read(buffer, 0, buffer.length);
                    final StringBuilder stringBuilder = new StringBuilder();
                    for(int i=0; i<numberOfByteRead; i++) {
                        stringBuilder.append((char)buffer[i]);
                    }
                    // Run on UI Thread to update the values on the UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doAdkRead(stringBuilder.toString());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

    };


    // We can communicate with the accessory by using the UsbManager to obtain a file descriptor
    // thatonecan set up input and output streams to read and write data to descriptor.
    // The streams represent the accessory's input and output bulk endpoints.
    // One should set up the communication between the device and accessory in another thread
    // so we don't lock the main UI thread
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void openUsbAccessory(UsbAccessory acc) {
        // Get the file descriptors from the USB accessory
        parcelFileDescriptor = usbManager.openAccessory(acc);

        // Opens the USB Accessory and initialize two File streams: one Input and one Output
        // These streams will be used to send and receive data to / from the Arduino Mega ADK
        if(parcelFileDescriptor != null){
            usbAccessory = acc;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            adkInputStream = new FileInputStream(fileDescriptor);
            adkOutputStream = new FileOutputStream(fileDescriptor);

            Thread thread = new Thread(runnableReadAdk);
            thread.start();
        }
    }

    // Close the file descriptors when closing the USB accessory
    private void closeUsbAccessory() {
        if(parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        parcelFileDescriptor = null;
        usbAccessory = null;
    }


    // Broadcast receiver that will receive intents for the USB detach actions
    private final BroadcastReceiver usbBroadcastReceiver = new BroadcastReceiver() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // If the USB is detached we should close it
            if(action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)){
                UsbAccessory mUsbAccessory =
                        (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

                if(mUsbAccessory!=null && mUsbAccessory.equals(AbstractAdkActivity.this.usbAccessory)){
                    // Close the USB accessory
                    closeUsbAccessory();
                }
            }
        }
    };

    // Broadcast receiver that will receive intents for the USB permission actions
    private final BroadcastReceiver usbPermBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equals(ACTION_USB_PERMISSION)){
                synchronized(this){
                    UsbAccessory accessory =
                            (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    // If we get permission from the user
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // And the accessory is not null
                        if(accessory != null) {
                            // We open the USB accessory
                            openUsbAccessory(accessory);
                        }
                    }
                    else {
                        // We don�t have permission to the USB accessory so we will exit
                        finish();
                    }
                }
            }
        }

    };
}

