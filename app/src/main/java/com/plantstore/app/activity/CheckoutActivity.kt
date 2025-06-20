package com.plantstore.app.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.plantstore.app.MainActivity
import com.plantstore.app.R
import com.plantstore.app.roomdb.AppDatabase
import com.plantstore.app.roomdb.ProductModel
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class CheckoutActivity : AppCompatActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)


        val checkout = Checkout()
        checkout.setKeyID("rzp_test_L8yM5GrR3r4uYy");

        val price = intent.getStringExtra("totalCost")

        try {
            val options = JSONObject()
            options.put("name", "Plant_Store")
            options.put("description", "Best Plant Shopping App")
            //You can omit the image option to fetch the image from the dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
            options.put("theme.color", "#FFBB86FC");
            options.put("currency", "INR");
            options.put("amount", (price!!.toInt()*100))//pass amount in currency subunits

            //val retryObj = JSONObject ();
            //retryObj.put("enabled", true);
            //retryObj.put("max_count", 4);
            //options.put("retry", retryObj);

            options.put("prefill.email", "2020530963.akash@ug.sharda.ac.in")
            options.put("prefill.contact", "9876543210")

            //options.put("prefill", prefill)
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()

        uploadData()
    }

    private fun uploadData() {
        val id = intent.getStringArrayListExtra("productIds")
        for(currentId in id!!){
            fetchData(currentId)
        }
    }

    private fun fetchData(productId: String) {

        val dao = AppDatabase.getInstance(this).productDao()

        Firebase.firestore.collection("products")
            .document(productId!!).get().addOnSuccessListener {

                lifecycleScope.launch(Dispatchers.IO){
                    dao.deleteProduct(ProductModel(productId))
                }

                saveData(it.getString("productName"),
                    it.getString("productSp"),
                productId)
            }

    }

    private fun saveData(name: String?, price: String?, productId: String) {
        val preferences = this.getSharedPreferences("user", MODE_PRIVATE)
        val data = hashMapOf<String, Any>()
        data["name"] = name!!
        data["price"] = price!!
        data["productId"] = productId
        data["status"] = "Ordered"
        data["userId"] = preferences.getString("number", "")!!

        val firestore = Firebase.firestore.collection("allOrders")
        val key = firestore.document().id
        data["orderId"] = key

        firestore.document(key).set(data).addOnSuccessListener {
            Toast.makeText(this, "Order Placed", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
    }
}