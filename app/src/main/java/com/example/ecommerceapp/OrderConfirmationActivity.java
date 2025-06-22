package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView textUsername, textAddress, textPaymentMethod, textTotalAmount, textEstimatedDelivery;
    private Button btnConfirmOrder;
    private String address, paymentMethod;
    private double finalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation2);

        textUsername = findViewById(R.id.textUsername);
        textAddress = findViewById(R.id.textAddress);
        textPaymentMethod = findViewById(R.id.textPaymentMethod);
        textTotalAmount = findViewById(R.id.textTotalAmount);
        textEstimatedDelivery = findViewById(R.id.textEstimatedDelivery);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);


        address = getIntent().getStringExtra("address");
        paymentMethod = getIntent().getStringExtra("paymentMethod");
        finalAmount = getIntent().getDoubleExtra("finalAmount", 0.0);


        textUsername.setText("Hello Guest!");
        textAddress.setText("Delivery Address: " + address);
        textPaymentMethod.setText("Payment Method: " + paymentMethod);
        textTotalAmount.setText("Total Amount: ₹" + String.format("%.2f", finalAmount));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 3);
        String estimatedDate = String.format("Estimated Delivery: %d/%d/%d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
        textEstimatedDelivery.setText(estimatedDate);


        btnConfirmOrder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, ProductCatalogActivity.class);
            startActivity(intent);
        });
    }
}
