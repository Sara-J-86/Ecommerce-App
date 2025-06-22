package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<ProductCatalogActivity.Product> cartList;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TextView textTotal;
    private EditText etCoupon;
    private Button btnApplyCoupon,btnCheckout, btnBackToCatalog;
    private double totalAmount = 0.0;
    private double discountAmount = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Button logoutBtn = findViewById(R.id.logoutBtn);

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList);
        recyclerView.setAdapter(cartAdapter);

        btnBackToCatalog = findViewById(R.id.btnBackToCatalog);
        textTotal = findViewById(R.id.textTotal);
        etCoupon = findViewById(R.id.etCoupon);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        btnCheckout = findViewById(R.id.btncheckout);

        btnApplyCoupon.setOnClickListener(v -> applyCoupon());


        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        double finalAmount = totalAmount - discountAmount;

        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, LoginActivity.class);
            startActivity(intent);
        });


        btnBackToCatalog.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ProductCatalogActivity.class);
            startActivity(intent);
        });

        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this,CheckoutActivity.class);
            intent.putExtra("totalAmount", finalAmount);
            startActivity(intent);
        });

        if (user != null) {
            loadCartItems();
        } else {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadCartItems() {
        db.collection("users")
                .document(user.getUid())
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductCatalogActivity.Product product = doc.toObject(ProductCatalogActivity.Product.class);
                        product.setId(doc.getId());
                        cartList.add(product);
                    }
                    cartAdapter.notifyDataSetChanged();
                    calculateTotal();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(CartActivity.this, "Error loading cart", Toast.LENGTH_SHORT).show()
                );
    }

    private void calculateTotal() {
        totalAmount = 0;
        for (ProductCatalogActivity.Product product : cartList) {
            totalAmount += product.getPrice();
        }
        double finalAmount = totalAmount - discountAmount;
        if (finalAmount < 0) finalAmount = 0;
        textTotal.setText("Total: ₹" + String.format("%.2f", finalAmount));
    }

    public void recalculateCartTotal() {
        calculateTotal();
    }


    private void applyCoupon() {
        String code = etCoupon.getText().toString().trim().toUpperCase();
        discountAmount = 0;


        if (code.equals("SAVE10")) {
            discountAmount = totalAmount * 0.10;
            Toast.makeText(this, "Coupon Applied: 10% Off", Toast.LENGTH_SHORT).show();
        } else if (code.equals("FREESHIP")) {
            discountAmount = 30;
            Toast.makeText(this, "₹30 Shipping Discount", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid Coupon", Toast.LENGTH_SHORT).show();
        }

        double finalAmount = totalAmount - discountAmount;
        textTotal.setText("Total: ₹" + String.format("%.2f",finalAmount));
    }


}
