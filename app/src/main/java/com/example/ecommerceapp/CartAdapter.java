package com.example.ecommerceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<ProductCatalogActivity.Product> productList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public CartAdapter(Context context, List<ProductCatalogActivity.Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        ProductCatalogActivity.Product product = productList.get(position);


        holder.nameTextView.setText(product.getName());
        holder.priceTextView.setText("₹" + product.getPrice());


        holder.deleteButton.setOnClickListener(v -> {
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        productList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Deleted from cart", Toast.LENGTH_SHORT).show();

                        if (context instanceof CartActivity) {
                            ((CartActivity) context).recalculateCartTotal();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;
        Button deleteButton;

        CartViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.productNameTextView);
            priceTextView = itemView.findViewById(R.id.productPriceTextView);
            deleteButton = itemView.findViewById(R.id.deleteFromCartButton);
        }
    }
}
