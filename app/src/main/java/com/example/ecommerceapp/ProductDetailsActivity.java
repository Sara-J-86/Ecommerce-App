package com.example.ecommerceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView productImageView;
    private TextView productNameTextView, productPriceTextView, productDescriptionTextView;
    private String productId;
    private FirebaseAuth auth;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        productId = getIntent().getStringExtra("productId");

        productImageView = findViewById(R.id.productImageView);
        productNameTextView = findViewById(R.id.productNameTextView);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        productDescriptionTextView = findViewById(R.id.productDescriptionTextView);
        logoutBtn = findViewById(R.id.logoutBtn);

        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProductDetailsActivity.this, LoginActivity.class));
            finish();
        });

        // Load product details from Firestore
        loadProductDetails(productId);
    }

    private void loadProductDetails(String id) {
        db.collection("products").whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the first document from the results
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        Product product = document.toObject(Product.class);

                        if (product != null) {
                            productNameTextView.setText(product.getName());
                            productPriceTextView.setText("Rs." + product.getPrice());
                            productDescriptionTextView.setText(product.getDescription());

                            Glide.with(ProductDetailsActivity.this)
                                    .load(product.getImageUrl())
                                    .into(productImageView);
                        }
                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load product", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }


    // Inner Product class to map the Firestore data
    public static class Product {
        private String id;
        private String name;
        private String imageUrl;
        private double price;
        private String description;

        public Product() {} // Empty constructor for Firebase

        public Product(String id, String name, String imageUrl, double price, String description) {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
            this.price = price;
            this.description = description;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getImageUrl() { return imageUrl; }
        public double getPrice() { return price; }
        public String getDescription() { return description; }
    }
}
