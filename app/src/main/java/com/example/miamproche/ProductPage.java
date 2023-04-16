package com.example.miamproche;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);
        try {
            set_img_url(R.id.product_img, "https://blog-images-1.pharmeasy.in/blog/production/wp-content/uploads/2021/04/23175719/shutterstock_440493100-1.jpg");
            set_img_url(R.id.imageView_pp, "https://img.freepik.com/icones-gratuites/utilisateur_318-159711.jpg");
            set_txt(R.id.textView_description, "Description :\nLorem ipsum dolor sit amet, consectetur adipisci elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrum exercitationem");
            set_txt(R.id.textView_price, "Lorem ipsum dolor si €/kg");
            set_txt(R.id.textView_producer_description, "Je m'appelle Réné, je cultive des légumes de saison avec des pratiques agricoles durables et respectueuses de l'environnement pour produire des légumes saint et bon. J'aime vendre localement pour nouer des liens et réduire mon empreinte carbone. Je m'efforce de fournir une expérience agricole unique et de qualité à mes clients.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void set_img_url(int id, String url){
        ExecutorService mExecutor = Executors.newSingleThreadExecutor();
        Handler mHandler = new Handler(Looper.getMainLooper());
        mExecutor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoInput(true);
                conn.connect();
                Bitmap result = BitmapFactory.decodeStream(conn.getInputStream());

                mHandler.post(() -> set_img(id, result));
            }
            catch (Exception ignored) {}
            finally {
                if (conn != null)
                    conn.disconnect();
            }
        });
    }

    protected void set_img(int id, Bitmap img) throws RuntimeException {

        ImageView img_view= (ImageView) findViewById(id);
        img_view.setImageBitmap(img);
    }

    protected void set_txt(int id, String str)throws IOException{
        TextView pricetxt = (TextView)findViewById(id);
        pricetxt.setText(str);
    }


}