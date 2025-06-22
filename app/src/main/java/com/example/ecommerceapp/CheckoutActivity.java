package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

public class CheckoutActivity extends AppCompatActivity {
    private EditText etAddress;
    private RadioGroup radioPaymentOptions;
    private Button btnPlaceOrder;

    private String userAddress;
    private String paymentMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        etAddress = findViewById(R.id.etAddress);
        radioPaymentOptions = findViewById(R.id.radioPaymentOptions);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);


        double finalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);

        btnPlaceOrder.setOnClickListener(v -> {
            userAddress = etAddress.getText().toString().trim();
            int selectedPaymentOptionId = radioPaymentOptions.getCheckedRadioButtonId();
            RadioButton selectedPaymentOption = findViewById(selectedPaymentOptionId);
            paymentMethod = selectedPaymentOption != null ? selectedPaymentOption.getText().toString() : "";

            if (userAddress.isEmpty()) {
                etAddress.setError("Address is required");
                return;
            }

            Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
            intent.putExtra("address", userAddress);
            intent.putExtra("paymentMethod", paymentMethod);
            intent.putExtra("finalAmount", finalAmount);
            startActivity(intent);
        });
    }
}
