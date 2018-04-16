package com.anywherecommerce.android.sdk.sampleapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anywherecommerce.android.sdk.AppBackgroundingManager;
import com.anywherecommerce.android.sdk.Logger;
import com.anywherecommerce.android.sdk.MeaningfulError;
import com.anywherecommerce.android.sdk.MeaningfulErrorListener;
import com.anywherecommerce.android.sdk.MeaningfulMessage;
import com.anywherecommerce.android.sdk.Terminal;
import com.anywherecommerce.android.sdk.devices.CardReaderController;
import com.anywherecommerce.android.sdk.devices.bbpos.BBPOSDevice;
import com.anywherecommerce.android.sdk.devices.bbpos.BBPOSDeviceCardReaderController;
import com.anywherecommerce.android.sdk.devices.CardReader;
import com.anywherecommerce.android.sdk.devices.MultipleBluetoothDevicesFoundListener;
import com.anywherecommerce.android.sdk.RequestListener;
import com.anywherecommerce.android.sdk.GenericEventListener;
import com.anywherecommerce.android.sdk.GenericEventListenerWithParam;
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetEndpoint;
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetReferenceTransaction;
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetTerminal;
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetTransaction;
import com.anywherecommerce.android.sdk.models.TransactionType;
import com.anywherecommerce.android.sdk.transactions.CardTransaction;
import com.anywherecommerce.android.sdk.transactions.KeyedTransaction;
import com.anywherecommerce.android.sdk.transactions.listener.CardTransactionListener;
import com.anywherecommerce.android.sdk.transactions.listener.TransactionListener;
import com.anywherecommerce.android.sdk.endpoints.AuthenticationListener;
import com.anywherecommerce.android.sdk.util.Amount;

import java.util.List;


/**
 * Created by Admin on 10/4/2017.
 */

public class MainActivity extends Activity {

    TextView txtPanel;
    EditText txtReferenceId;
    Button btnConnectAudio, btnDisconnectAudio, btnIsDeviceConnected, btnStartEMV, btnConnectBT, btnDisconnectBT, btnGetTransactions, btnTerminalLogin, btnKeyedsale;
    Button btnNewRefund, btnRefRefund;
    DialogManager dialogs = new DialogManager();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testharness_device);


        if (!PermissionsController.verifyAppPermissions(this)) {
            PermissionsController.requestAppPermissions(this, PermissionsController.permissions, 1001);
        }


        AppBackgroundingManager.get().registerListener(new AppBackgroundingManager.AppBackroundedListener() {
            @Override
            public void onBecameForeground() {
                Logger.trace("Caught app in foreground.");
            }

            @Override
            public void onBecameBackground() {
                Logger.trace("Caught app in background.");
            }
        });

        txtPanel = (TextView) findViewById(R.id.txtTextHarnessPanel);
        txtPanel.setMovementMethod(new ScrollingMovementMethod());

        btnConnectAudio = (Button) findViewById(R.id.audioConnect);
        btnDisconnectAudio = (Button) findViewById(R.id.btnDisconnectAudio);
        btnConnectBT = (Button) findViewById(R.id.btConnect);
        btnDisconnectBT = (Button) findViewById(R.id.btnDisconnectBT);
        btnIsDeviceConnected = (Button) findViewById(R.id.btnIsDeviceConnected);
        btnGetTransactions = (Button) findViewById(R.id.btnGetTransactions);
        btnStartEMV = (Button) findViewById(R.id.btnStartEmvSale);
        btnTerminalLogin = (Button) findViewById(R.id.terminalLogin);
        btnKeyedsale = (Button) findViewById(R.id.keyedsale);
        btnNewRefund = (Button) findViewById(R.id.unrefRefund);
        btnRefRefund = (Button) findViewById(R.id.refRefund);
        txtReferenceId = (EditText) findViewById(R.id.txtReferenceId);

        final CardReaderController cardReaderController = CardReaderController.getControllerFor(BBPOSDevice.class);

        cardReaderController.subscribeOnCardReaderConnected(new GenericEventListenerWithParam<CardReader>() {
            @Override
            public void onEvent(CardReader deviceInfo) {
                if ( deviceInfo == null )
                    addText("\r\nUnknown device connected");
                else
                    addText("\nDevice connected " + deviceInfo.getModelDisplayName());
            }
        });

        cardReaderController.subscribeOnCardReaderDisconnected(new GenericEventListener() {
            @Override
            public void onEvent() {
                addText("\nDevice disconnected");
            }
        });

        cardReaderController.subscribeOnCardReaderConnectFailed(new MeaningfulErrorListener() {
            @Override
            public void onError(MeaningfulError error) {
                addText("\nDevice connect failed: " + error.toString());
            }
        });

        cardReaderController.subscribeOnCardReaderError(new MeaningfulErrorListener() {
            @Override
            public void onError(MeaningfulError error) {
                addText("\nDevice error: " + error.toString());
            }
        });

        btnConnectAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText("\nConnecting to audio jack (with polling)\r\n");
                cardReaderController.connectAudioJack();
            }
        });

        btnDisconnectAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText("\r\nNot implemented");
                //addText("Disconnecting audio jack\r\n");
                //BBPOSDeviceCardReaderController.getInstance().disconnectAudioJack();
            }
        });

        btnConnectBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText("\nConnecting to BT\r\n");
                cardReaderController.connectBluetooth(new MultipleBluetoothDevicesFoundListener() {
                    @Override
                    public void onMultipleBluetoothDevicesFound(List<BluetoothDevice> matchingDevices) {
                        addText("Many BT devices.  Select one and then call connectSpecificBluetoothDevice");
                    }
                });
            }
        });

        btnDisconnectBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText("Disconnecting bluetooth\r\n");
                cardReaderController.disconnectReader();
            }
        });


        btnIsDeviceConnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BBPOSDeviceCardReaderController.getController().setDebugLogEnabled(true);
                if (latch) {
                    addText("\r\nStopping audio");
                    cardReaderController.disconnectReader();
                }
                else {
                    addText("\r\nStarting audio");
                    cardReaderController.connectAudioJack();
                }

                latch = !latch;
                //addText("Checking connection\r\n");
                //BBPOSDeviceCardReaderController.getInstance().disconnectAudioJack();
            }
        });

        btnStartEMV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BBPOSDeviceCardReaderController.getController().setDebugLogEnabled(true);

                addText("\nStarting EMV transaction");
                if ( !CardReaderController.isCardReaderConnected() ) {
                    addText("\r\nNo card reader connected");
                    return;
                }

                dialogs.showProgressDialog(MainActivity.this, "Please Wait...");
                final CardTransaction t = new WorldnetTransaction();
                t.setTransactionType(TransactionType.SALE);
                t.setTotalAmount(new Amount("200.00"));
                t.setCurrency("USD");
                t.useCardReader(CardReaderController.getConnectedReader()); // either instance, or
                t.setEndpoint(new WorldnetEndpoint());
                t.setOnSignatureRequiredListener(new GenericEventListener() {
                    @Override
                    public void onEvent() {
                        addText("\r\n------>onSignatureRequired: sending null and proceeding");
                        String signature = "base64-encoded image or point map";
                        t.setSignature(signature);
                        t.proceed();
                    }
                });

                //t.enableLogging();
                t.execute(new CardTransactionListener() {
                    @Override
                    public void onCardReaderEvent(MeaningfulMessage event) {
                        addText("\r\n------>onCardReaderEvent: " + event.message);
                    }

                    @Override
                    public void onTransactionCompleted() {
                        dialogs.hideProgressDialog();
                        addText("\r\n------>onTransactionCompleted");
                    }

                    @Override
                    public void onTransactionFailed(MeaningfulError reason) {
                        dialogs.hideProgressDialog();
                        addText("\r\n------>onTransactionFailed: " + reason.toString());
                    }


                });
            }
        });

        btnGetTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText("Getting transactions\r\n");
                WorldnetEndpoint.fetchTransactions(1, "06-01-2017", "09-01-2017", new RequestListener() {
                    @Override
                    public void onRequestComplete(Object response) {
                        addText("\r\nGot transactions");
                    }

                    @Override
                    public void onRequestFailed(MeaningfulError reason) {
                        addText("\r\nfailed " + reason.toString());
                    }
                });
            }
        });


        btnTerminalLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateTerminal();
            }
        });

        btnKeyedsale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addText("\r\nExecuting Keyed Sale Transaction. Please Wait...");
                dialogs.showProgressDialog(MainActivity.this, "Please Wait...");

                final WorldnetTransaction transaction = new WorldnetTransaction();
                transaction.setTransactionType(TransactionType.SALE);
                transaction.setCardExpiryMonth("10");
                transaction.setCardExpiryYear("20");
                transaction.setAddress("123 Main Street");
                transaction.setPostalCode("30004");
                transaction.setCVV2("999");
                transaction.setCardholderName("Jane Doe");
                transaction.setCardNumber("4012888888881881");
                transaction.setTotalAmount(new Amount("120.47"));
                transaction.setCurrency("USD");

                transaction.setOnSignatureRequiredListener(new GenericEventListener() {
                    @Override
                    public void onEvent() {
                        addText("\r\n------>onSignatureRequired: sending null and proceeding");
                        String signature = "base64-encoded image or point map";
                        transaction.setSignature(signature);
                        transaction.proceed();
                    }
                });

                //t.enableLogging();
                transaction.execute(transactionListener);
            }
        });


        btnNewRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addText("----> Starting New Refund Transaction <----");
                dialogs.showProgressDialog(MainActivity.this, "Please Wait...");

                final KeyedTransaction transaction = new WorldnetTransaction();
                transaction.setTransactionType(TransactionType.REFUND);
                transaction.setTotalAmount(new Amount("20.25"));
                transaction.setCardExpiryMonth("10");
                transaction.setCardExpiryYear("20");
                transaction.setAddress("123 Main Street");
                transaction.setPostalCode("30004");
                transaction.setCVV2("999");
                transaction.setCardholderName("Jane Doe");
                transaction.setCardNumber("4012888888881881");
                transaction.execute(transactionListener);
            }
        });

        btnRefRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addText("----> Starting Referenced Refund Transaction <----");

                if (!TextUtils.isEmpty(txtReferenceId.getText())) {
                    dialogs.showProgressDialog(MainActivity.this, "Please Wait...");

                    final WorldnetReferenceTransaction transaction = new WorldnetReferenceTransaction();
                    transaction.setTotalAmount(new Amount("0.05"));
                    transaction.setTransactionType(TransactionType.REFUND);
                    transaction.setExternalId(txtReferenceId.getText().toString());
                    transaction.execute(transactionListener);
                }
                else {
                    addText("----> ALERT: No Reference ID entered <----");
                }
            }
        });

//        FileUtils.writeToFile("abc.txt", "sandy", true);
//        addText(FileUtils.readFromFile("abc.txt", true));

        authenticateTerminal();
    }

    static boolean latch = false;
    private void addText(String text)
    {
        txtPanel.append("\r\n" + text + "\r\n\n");
    }

    private void authenticateTerminal() {
        addText("Authenticating Terminal. Please Wait...");

        Terminal.initializeAs(new WorldnetTerminal("2994001", "password"));
        ((WorldnetTerminal)Terminal.getInstance()).setGatewayUrl("https://testpayments.anywherecommerce.com/merchant");
        Terminal.getInstance().authenticate(new AuthenticationListener() {
            @Override
            public void onAuthenticationComplete() {
                addText("Terminal Authenticated Successfully");
            }

            @Override
            public void onAuthenticationFailed(MeaningfulError error) {
                addText("Terminal Authentication Failed");
            }
        });
    }

    private TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void onTransactionCompleted() {
            dialogs.hideProgressDialog();
            addText("\r\n------>onTransactionCompleted");
        }

        @Override
        public void onTransactionFailed(MeaningfulError reason) {
            dialogs.hideProgressDialog();
            addText("\r\n------>onTransactionFailed: " + reason.toString());
        }
    };
}
