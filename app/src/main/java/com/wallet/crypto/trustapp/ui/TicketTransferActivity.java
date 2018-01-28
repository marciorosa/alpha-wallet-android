package com.wallet.crypto.trustapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.ui.barcode.BarcodeCaptureActivity;
import com.wallet.crypto.trustapp.util.BalanceUtils;
import com.wallet.crypto.trustapp.util.QRURLParser;
import com.wallet.crypto.trustapp.viewmodel.TicketTransferViewModel;
import com.wallet.crypto.trustapp.viewmodel.TicketTransferViewModelFactory;
import com.wallet.crypto.trustapp.viewmodel.UseTokenViewModel;
import com.wallet.crypto.trustapp.viewmodel.UseTokenViewModelFactory;
import com.wallet.crypto.trustapp.widget.SystemView;

import org.ethereum.geth.Address;

import java.math.BigInteger;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Created by James on 28/01/2018.
 */

public class TicketTransferActivity extends BaseActivity
{
    private static final int BARCODE_READER_REQUEST_CODE = 1;

    @Inject
    protected TicketTransferViewModelFactory ticketTransferViewModelFactory;
    protected TicketTransferViewModel viewModel;
    private SystemView systemView;

    public TextView name;
    public TextView ids;

    private String address;

    private EditText toAddressText;
    private EditText idsText;
    private TextInputLayout toInputLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transfer_token);
        toolbar();

        address = getIntent().getStringExtra(C.EXTRA_ADDRESS);

        systemView = findViewById(R.id.system_view);
        systemView.hide();

        setTitle(getString(R.string.title_send));

        name = findViewById(R.id.textViewName);
        ids = findViewById(R.id.textViewIDs);
        toInputLayout = findViewById(R.id.to_input_layout);
        toAddressText = findViewById(R.id.send_to_address);
        idsText = findViewById(R.id.send_ids);

        viewModel = ViewModelProviders.of(this, ticketTransferViewModelFactory)
                .get(TicketTransferViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next: {
                onNext();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    QRURLParser parser = QRURLParser.getInstance();
                    String extracted_address = parser.extractAddressFromQrString(barcode.displayValue);
                    if (extracted_address == null) {
                        Toast.makeText(this, R.string.toast_qr_code_no_address, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Point[] p = barcode.cornerPoints;
                    toAddressText.setText(extracted_address);
                }
            } else {
                Log.e("SEND", String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.prepare();
    }

    private void onNext() {
        // Validate input fields
        boolean inputValid = true;
        final String to = toAddressText.getText().toString();
        if (!isAddressValid(to)) {
            toInputLayout.setError(getString(R.string.error_invalid_address));
            inputValid = false;
        }
        final String amount = idsText.getText().toString();
        /*if (!isValidAmount(amount)) {
            amountInputLayout.setError(getString(R.string.error_invalid_amount));
            inputValid = false;
        }*/

        if (!inputValid) {
            return;
        }

        toInputLayout.setErrorEnabled(false);

        //BigInteger amountInSubunits = BalanceUtils.baseToSubunit(amount, decimals);
        //viewModel.openConfirmation(this, to, amountInSubunits, contractAddress, decimals, symbol, sendingTokens);
    }

    boolean isAddressValid(String address) {
        try {
            new Address(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    boolean isValidAmount(String eth) {
        try {
            String wei = BalanceUtils.EthToWei(eth);
            return wei != null;
        } catch (Exception e) {
            return false;
        }
    }
}
