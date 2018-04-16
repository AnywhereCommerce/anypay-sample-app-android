package com.anywherecommerce.android.sdk.sampleapp.samples;

import android.bluetooth.BluetoothDevice;

import com.anywherecommerce.android.sdk.MeaningfulError;
import com.anywherecommerce.android.sdk.devices.BluetoothCardReaderConnectionListener;
import com.anywherecommerce.android.sdk.devices.CardReader;
import com.anywherecommerce.android.sdk.devices.CardReaderConnectionListener;

import java.util.List;

/**
 * Created by Admin on 11/27/2017.
 */

public class SampleConnectToCardReader {

    // Card reader controllers are done through a singleton CardReaderController, which ensures only
    // one device is connected at any given point in time.

    private static CardReaderConnectionListener mainActivityCallback;

    /*
    public static void connectToCardReaderViaAudioJack(CardReaderConnectionListener listener)
    {
        mainActivityCallback = listener;
        // For this example, we are connecting a Walker C2X via the audio jack.
        // To change the reader to a different, one, simply

        CardReaderController
                .getControllerFor(WalkerC2X.class)
                .subscribeOnCardReaderConnected(cardReaderConnectedHandler)
                .subscribeOnCardReaderConnectFailed(cardReaderConnectionFailedHandler)
                .connectAudioJack();
    }

    public static void connectToCardReaderViaBluetooth()
    {
        // Card reader connections are inherently asynchronous and thus you'll need to handle the callbacks
        // when the card reader is connected or disconnected.

        // If a single candidate bluetooth device is available, it will connect to it and fire the onCardReaderConnected callback.
        // It is possible that multiple bluetooth device candidates are in the vicinity.  In that case, the onMultipleBluetoothDevices callback
        // will be called instead.
        CardReaderController
                .getControllerFor(WalkerC2XBT.class)
                .subscribeOnCardReaderConnected(cardReaderConnectedHandler)
                .subscribeOnCardReaderConnectFailed(cardReaderConnectionFailedHandler)
                .connectBluetooth(new MultipleBluetoothDevicesFoundListener() {
                    // This can get invoked if there are multiple candidates in the vicinity.  You will need
                    // to select the specific device you want to connect to.
                    @Override
                    public void onMultipleBluetoothDevicesFound(List<BluetoothDevice> matchingDevices) {

                        //?TODO? - Prompt the user to select one device to connect out of the matchingDevices candidate list.

                        // For this example, we'll just take the first one in the list for simplicity and connect to it.
                        BluetoothDevice device = matchingDevices.get(0);
                        // Since we already still have the connection event handlers registered at this point, we can just invoke the method
                        // again and it will fire the corresponding callback.
                        CardReaderController.getControllerFor(WalkerC2XBT.class).connectSpecificBluetoothDevice(device);
                    }
                });
    }
    */

    public static void connectViaBluetooth(final BluetoothCardReaderConnectionListener listener)
    {
        // To connect via bluetooth, simply pass the BluetoothCardReaderConnectionListener parameter.
        CardReader.connect(new BluetoothCardReaderConnectionListener() {

            @Override
            public void onCardReaderConnected(CardReader connectedReader) {
                // The WalkerC2XBT instance is now connected and active and can be used to run transactions.
                listener.onCardReaderConnected(connectedReader);
            }

            @Override
            public void onCardReaderConnectionFailed(MeaningfulError error) {
                // Something happened and the device couldn't be connected.  Check the error parameter for details.
                listener.onCardReaderConnectionFailed(error);
            }

            @Override
            public void onMultipleBluetoothDevicesFound(List<BluetoothDevice> matchingDevices) {

                // In the unlikely event that there are more than one bluetooth devices in range,
                // this callback will get fired.  You will need to select one of the devices and connect to it.

                //?TODO? - Prompt the user to select one device to connect out of the matchingDevices candidate list.

                // For this example, we'll just take the first one in the list for simplicity and connect to it.
                BluetoothDevice device = matchingDevices.get(0);

                CardReader.connect(device, listener);
            }
        });


        // BETTER ALTERNATIVE:  If you know the type of reader you want to connect, you can specify it as a generic parameter
        //                      to the listener and avoid the need to cast.  This will also reduce the chances of onMultipleBluetoothDeviceFound
        //                      being called as the method will first search for the specific reader you want connected even if
        //                      multiple bluetooth devices are found.
        /*
        CardReader.connect(new BluetoothCardReaderConnectionListener<WalkerC2XBT>() {

            @Override
            public void onCardReaderConnected(WalkerC2XBT connectedReader) {
                // The WalkerC2XBT instance is now connected and active and can be used to run transactions.
                listener.onCardReaderConnected(connectedReader);
            }

            @Override
            public void onCardReaderConnectionFailed(MeaningfulError error) {
                // Something happened and the device couldn't be connected.  Check the error parameter for details.
                listener.onCardReaderConnectionFailed(error);
            }

            @Override
            public void onMultipleBluetoothDevicesFound(List<BluetoothDevice> matchingDevices) {

                // In the unlikely event that there are more than one bluetooth devices in range,
                // this callback will get fired.  You will need to select one of the devices and connect to it.

                //?TODO? - Prompt the user to select one device to connect out of the matchingDevices candidate list.

                // For this example, we'll just take the first one in the list for simplicity and connect to it.
                BluetoothDevice device = matchingDevices.get(0);

                CardReader.connect(device, listener);
            }
        });
        */
    }

    public static void connectViaAudioJack(final CardReaderConnectionListener listener)
    {
        CardReader.connect(new CardReaderConnectionListener() {

            @Override
            public void onCardReaderConnected(CardReader connectedReader) {
                // The WalkerC2XBT instance is now connected and active and can be used to run transactions.
                listener.onCardReaderConnected(connectedReader);
            }

            @Override
            public void onCardReaderConnectionFailed(MeaningfulError error) {
                // Something happened and the device couldn't be connected.  Check the error parameter for details.
                listener.onCardReaderConnectionFailed(error);
            }

        });

        // BETTER ALTERNATIVE:  If you know the type of reader you want to connect, you can specify it as a generic parameter
        //                      to the listener and avoid the need to cast.

        /*
        CardReader.connect(new CardReaderConnectionListener<WalkerC2X>() {

            @Override
            public void onCardReaderConnected(WalkerC2XBT connectedReader) {
                // The WalkerC2X instance is now connected and active and can be used to run transactions.
                listener.onCardReaderConnected(connectedReader);
            }

            @Override
            public void onCardReaderConnectionFailed(MeaningfulError error) {
                // Something happened and the device couldn't be connected.  Check the error parameter for details.
                listener.onCardReaderConnectionFailed(error);
            }

        });
        */
    }

}
