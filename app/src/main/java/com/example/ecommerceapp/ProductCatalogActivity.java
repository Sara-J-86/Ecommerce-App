package com.example.ecommerceapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProductCatalogActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private SearchView searchView;
    private TextView userEmail;
    private Button logoutBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_catalog);
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        userEmail = findViewById(R.id.userEmail);
        logoutBtn = findViewById(R.id.logoutBtn);
        Button viewCartBtn = findViewById(R.id.viewCartBtn);
        viewCartBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCatalogActivity.this, CartActivity.class);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        loadProducts();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userEmail.setText(user.getEmail());
        }

        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(ProductCatalogActivity.this, LoginActivity.class));
            finish();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void loadProducts() {
        db.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(ProductCatalogActivity.this, "Error loading products", Toast.LENGTH_SHORT).show());
    }

    private void filterProducts(String text) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(product);
            }
        }
        adapter.updateList(filteredList);
    }

    public static class Product {
        private String id;
        private String name;
        private String imageUrl;
        private double price;

        private String description;
        public Product() {}

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

        public void setId(String id) {
            this.id = id;
        }

    }

    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private Context context;
        private List<Product> productList;
        private Set<String> wishlist;

        public ProductAdapter(Context context, List<Product> productList) {
            this.context = context;
            this.productList = productList;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = productList.get(position);

            holder.productName.setText(product.getName());
            holder.productPrice.setText("Rs." + product.getPrice());
            Glide.with(context).load(product.getImageUrl()).into(holder.productImage);
            holder.productDescription.setText(product.getDescription());

            holder.addToCartButton.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = user.getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference cartRef = db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(product.getId());

                cartRef.set(product)
                        .addOnSuccessListener(unused -> Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to add to Cart", Toast.LENGTH_SHORT).show());
            });

            String productId = product.getId();

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra("productId", product.getId());
                context.startActivity(intent);
            });

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference wishlistRef = db.collection("users")
                    .document(userId)
                    .collection("wishlist")
                    .document(productId);

            // First check if this product is wishlisted for this user
            wishlistRef.get().addOnSuccessListener(snapshot -> {
                boolean[] isWishlisted = { snapshot.exists() };
                holder.wishlistButton.setImageResource(isWishlisted[0]? R.drawable.wishlist_selected : R.drawable.wishlist_unselected);

                holder.wishlistButton.setOnClickListener(v -> {
                    if (isWishlisted[0]) {
                        wishlistRef.delete().addOnSuccessListener(unused -> {
                            holder.wishlistButton.setImageResource(R.drawable.wishlist_unselected);
                            Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        wishlistRef.set(product).addOnSuccessListener(unused -> {
                            isWishlisted[0] = true;
                            holder.wishlistButton.setImageResource(R.drawable.wishlist_selected);
                            Toast.makeText(context, "Added to Wishlist", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public void updateList(List<Product> newList) {
            this.productList = newList;
            notifyDataSetChanged();
        }

        public static class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productPrice;
            ImageButton wishlistButton;
            TextView productDescription;
            Button addToCartButton;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productName = itemView.findViewById(R.id.productName);
                productPrice = itemView.findViewById(R.id.productPrice);
                productDescription = itemView.findViewById(R.id.productDescription);
                wishlistButton = itemView.findViewById(R.id.wishlistButton);
                addToCartButton = itemView.findViewById(R.id.addToCartButton);
            }
        }
    }
}