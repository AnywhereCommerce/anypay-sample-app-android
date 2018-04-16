package com.anywherecommerce.android.sdk.sampleapp.samples;

import com.anywherecommerce.android.sdk.GenericEventListener;
import com.anywherecommerce.android.sdk.MeaningfulError;
import com.anywherecommerce.android.sdk.models.AccountType;
import com.anywherecommerce.android.sdk.models.SplitFundingFeeLineItem;
import com.anywherecommerce.android.sdk.models.TransactionType;
import com.anywherecommerce.android.sdk.transactions.listener.TransactionListener;
import com.anywherecommerce.android.sdk.devices.CardReader;
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetTransaction;
import com.anywherecommerce.android.sdk.models.FeeLineItem;
import com.anywherecommerce.android.sdk.models.GatewayResponse;
import com.anywherecommerce.android.sdk.models.TaxLineItem;
import com.anywherecommerce.android.sdk.models.TipLineItem;
import com.anywherecommerce.android.sdk.transactions.SwipeTransaction;
import com.anywherecommerce.android.sdk.util.Amount;

/**
 * Created by Admin on 11/27/2017.
 */

public class SampleSwipeSaleTransaction {

    public static GenericEventListener signatureRequiredListener;

    public static void executeUsingCardReader(CardReader reader, final TransactionListener mainActivityCallback)
    {
        // Step 1 - create the Transaction object for the particular gateway you want to use.

        // Here, we are using a Worldnet transaction, which is a transaction that points to the Worldnet gateway.
        // For another gateway, use the appropriate transaction, such as AuthNetTransaction, or PropayTransaction

        final SwipeTransaction transaction = new WorldnetTransaction();
        transaction.setTransactionType(TransactionType.SALE);

        // Step 2 - set the amount.  You can either set a basic total amount (via setTotalAmount), or a more complex arrangement (subtotal + taxes + tips) etc..

        // If using the simple method, uncomment this line and skip to Step 3.  Be sure to comment everything else in Step 2 out.
        // transaction.setTotalAmount(new Amount("10.00"));

        // OPTIONAL:  To add tax, a tip, or a convenience fee, just set a subtotal and invoke the corresponding Add methods for the type of surcharge.
        //            You can add as many as you like (though you can only add one tip)

        transaction.setSubtotal(new Amount("10.00"));

        transaction.addTax(new TaxLineItem("GST", "5.00%"));
        transaction.addTax(new TaxLineItem("PST", "6.00%"));
        transaction.setTip(new TipLineItem("15.00%"));                            // Accepts percentages or fixed values (e.g. $1.50).
        transaction.addFee(new FeeLineItem("Convenience Fee", "3.00%"));    // Accepts percentages or fixed values (e.g. $1.50).

        // Note:  With some gateways, it is possible to create a split-funding fee, which will settle to a different account.
        //        To make this happen, just make the fee an instance of a SplitFundingFeeLineItem.

        transaction.addFee(new SplitFundingFeeLineItem("Processor Fee", "2.50%", "1234567"));

        // OPTIONAL: Once a subtotal has been set, you can automatically calculate the taxes, tips, fees, and total amount by using the calculateAmounts() helper
        //           method. It will calculate the relevant tax, tip, and fee amounts, and the final total.

        transaction.calculateAmounts();
        String taxAmount = transaction.getTax("GST").getAmount().toLocalizedCurrencyString();               // should be "0.50" (5.00% of $10.00 subtotal)
        String tipAmount = transaction.getTip().getAmount().toLocalizedCurrencyString();                          // should be "1.50" (15% of $10.00 subtotal)
        String feeAmount = transaction.getFee("Convenience Fee").getAmount().toLocalizedCurrencyString();   // should be "0.30" (3.00% of $10.00 subtotal)
        String totalAmount = transaction.getTotalAmount().toLocalizedCurrencyString();                            // Should be "13.15" ($10 subtotal + 5% GST, 6% PST + 3% fee + 2.5% fee + 15% tip)

        // SIDEBAR:  The calculateAmounts() method will always calculate everything against the subtotal.
        //           If you need something more advanced (such as calculating tips on the total after taxes),
        //           you can calculate the amounts yourself.  To do this, simply call the calculateOn method
        //           with the value you want to calculate against.

        // Example:
        String tipOnTotalAmount = transaction.getTip()
                .calculateOn(transaction.getTotalAmount())
                .toLocalizedCurrencyString();

        // Step 3 - set optional fields (if required)

        // OPTIONAL:  Any of the fields below can be added to the transaction if required

        //transaction.setGeolocationData(GeolocationController.getLastKnownPosition());
        //transaction.setOperator(Session.getInstance().user.getUsername());
        //transaction.setCurrency("USD");                        // The default currency will typically be specified by the gateway.  Use this method to override it.
        //transaction.setPaymentMethod(PaymentMethod.CREDIT);    // The payment method type (i.e. CASH, CREDIT, DEBIT, CRYPTO, etc..)
        //transaction.setKeywords("Shirt Cotton Blue Sweater");  // Keywords that can be used in a future search to find the transaction quickly.  Separated by spaces
        //transaction.setNotes("This purchase is a gift");       // Freeform text field
        //transaction.addCustomField("yourCustomFieldName", "yourCustomFieldValue");  // You can add as many of these as you wish.

        // Step 4 - Attach a card reader (required for card-present transactions, omit for keyed transactions)

        // The following line tells the transaction that it should receive card information from the associated reader.
        // When transaction.execute() is called, it will automatically invoke the reader and submit the transaction
        // to the endpoint server when the user swipes/dips/taps the card.

        transaction.useCardReader(reader);

        // Step 5 - Register optional callbacks

        // OPTIONAL:  The following method sets the callback that will get invoked if the transaction requires a signature (i.e. chip and signature transaction)
        //            Required for chip and signature transactions and swiped transactions.

        transaction.setOnSignatureRequiredListener(signatureRequiredListener);

        // OPTIONAL:  The following method sets the callback that will get invoked if the transaction requires account selection (i.e. choosing between CHECKING and SAVINGS transactions)
        //            This typically would only be fired if you support bank debit cards.
        transaction.setOnAccountSelectionRequiredListener(new GenericEventListener() {
            @Override
            public void onEvent() {
                // TODO - You can display a user interface to allow the user to select the account type.

                // For this example, we'll just default to CHECKING.
                transaction.setAccountType(AccountType.CHECKING);
                // This will block the transaction until you proceed, so be sure to call proceed() when done to resume the transaction.
                transaction.proceed();
            }
        });

        // OPTIONAL:  If you want to receive notification of card reader events, such as CARD_INSERTED, uncomment the following lines:
        /*
        transaction.subscribeCardReaderEvents(new MeaningfulMessageListener() {
            @Override
            public void onMessage(MeaningfulMessage message) {
                // Generally speaking, notification events have no functional value other
                // than human interface considerations and shouldn't require any special logic.

                // TODO - Update UI or other trivial operation.
            }
        });
        */

        // OPTIONAL:  If you want to receive notification of warnings (i.e. non-fatal but unexpected events) that occurred during a transaction,
        //            uncomment the following lines:
        /*
        transaction.setOnWarningListener(new MeaningfulMessageListener() {
            @Override
            public void onMessage(MeaningfulMessage message) {
                // Something unexpected happened, but the transaction will continue.

                // TODO - Notify user to take some custom action.
            }
        });
        */

        // Step 6 - execute the transaction
        //          This will activate the specified card reader and send the data to the server.

        transaction.execute(new TransactionListener() {
            @Override
            public void onTransactionCompleted() {

                // The transaction completed (i.e. received a valid response from the server).
                // This does NOT mean the transaction was approved.

                if ( transaction.isPartiallyApproved() )
                {
                    // Transaction was approved, but not for the full amount.  Check the ApprovedAmount field
                    // to see how much was actually approved.
                }
                else if ( transaction.isApproved() )
                {
                    // Transaction approved for the full amount requested
                }
                else
                {
                    // Transaction declined.  Check the response for details.
                    GatewayResponse response = transaction.getGatewayResponse();
                }

                // Notify the user
                mainActivityCallback.onTransactionCompleted();
            }

            @Override
            public void onTransactionFailed(MeaningfulError reason) {
                // Something went wrong and a valid response was not received from the server.

                // Reverse the transaction to ensure consistency.
                // The cancel method will queue the transaction for reversal when an internet
                // connection is present again.
                transaction.cancel();

                mainActivityCallback.onTransactionFailed(reason);
            }
        });
    }
}
