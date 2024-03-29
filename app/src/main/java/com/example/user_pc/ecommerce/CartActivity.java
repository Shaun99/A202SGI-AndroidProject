package com.example.user_pc.ecommerce;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_pc.ecommerce.Model.Cart;
import com.example.user_pc.ecommerce.Prevalent.Prevalent;
import com.example.user_pc.ecommerce.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    //Declare and initialise variables
    private Button NextProcessBtn ,BackButton;
    private TextView txtTotalAmount, txtMsg1;

    private double TotalPrice = 0;
    //Set the total price to two decimal points
    DecimalFormat price = new DecimalFormat("###,###.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //Locate the view having id from the layout resource file
        NextProcessBtn = (Button) findViewById(R.id.next_btn);
        txtTotalAmount = (TextView) findViewById(R.id.total_price);
        txtMsg1 = (TextView) findViewById(R.id.msg1);

        //Redirect user back to Main page
        BackButton = (Button) findViewById(R.id.cancel_btn);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        NextProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {   //Redirect the user to checkout Page
                Intent intent = new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(TotalPrice));
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart()
    { //Display the items on the card list
        super.onStart();

        //Create a database reference
        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        //Pass the constructor Cart
        FirebaseRecyclerOptions<Cart> options =
                new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartListRef.child("User View")
                                .child(Prevalent.currentOnlineUser.getPhone())
                                .child("Products"), Cart.class)
                        .build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter
                = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model)
            {   //Display the information
                holder.txtProductQuantity.setText("Quantity = " + model.getQuantity());
                holder.txtProductPrice.setText("RM" + model.getPrice());
                holder.txtProductName.setText(model.getPname());

                double oneTyprProductTPrice = ((Double.valueOf(model.getPrice()))) * Integer.valueOf(model.getQuantity());
                TotalPrice = TotalPrice + oneTyprProductTPrice;
                //Display the total price
                txtTotalAmount.setText("Subtotal: RM" + price.format(TotalPrice));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {   //Prompt a dialog box
                        CharSequence options[] = new CharSequence[]
                            {
                                "Edit Product Quantity",
                                "Remove Product"
                            };

                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options:");
                        //Function to edit or delete product
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)//Edit
                                {   //Redirect user back to product details page to change the quantity
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid", model.getPid()); //Get the product Id
                                    startActivity(intent);
                                }
                                if (i == 1)//Delete
                                {   //Delete the product from the user and admin view
                                    cartListRef.child("User View")
                                            .child(Prevalent.currentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(model.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone())
                                                                .child("Products").child(model.getPid())
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if (task.isSuccessful())
                                                                        {   //Prompt successful message
                                                                            Toast.makeText(CartActivity.this, "Item removed successfully.", Toast.LENGTH_SHORT).show();
                                                                            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                });

                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

}
