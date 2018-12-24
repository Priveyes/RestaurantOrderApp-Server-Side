package com.example.paul_.foodappserver;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.paul_.foodappserver.Common.Common;
import com.example.paul_.foodappserver.Interface.ItemClickListener;
import com.example.paul_.foodappserver.Model.Request;
import com.example.paul_.foodappserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

        FirebaseDatabase db;
        DatabaseReference request;

        MaterialSpinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

    //Firebase
        db = FirebaseDatabase.getInstance();
        request = db.getReference("Requests");

        //Initializarea

        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        
        loadOrders();
    }

    private void loadOrders() {

        //RecyclerAdapterul pentru viewul meniului
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                request

        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {

            //setez campurile pentru comenzi
            viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
            viewHolder.txtOrderStatus.setText(Common.converCodeToStatus(model.getStatus()));
            viewHolder.txtOrderAddress.setText(model.getAddress());
            viewHolder.txtOrderPhone.setText(model.getPhone());

            viewHolder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {

                }
            });
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE))
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        else if(item.getTitle().equals(Common.DELETE))
            deleteOrder(adapter.getRef(item.getOrder()).getKey());

            return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {
        request.child(key).removeValue();
    }

    private void showUpdateDialog(String key, final Request item) {
    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
    alertDialog.setTitle("Modifica statusul comenzii");
    alertDialog.setMessage("Alege noul status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = (MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Comanda plasata","Comanda pe drum","Comanda livrata");

        alertDialog.setView(view);

        final String localkey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)  {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                request.child(localkey).setValue(item);

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }
}
