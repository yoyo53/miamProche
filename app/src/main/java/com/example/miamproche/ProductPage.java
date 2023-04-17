package com.example.miamproche;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            set_txt(R.id.textView_producer_description, "Je m'appelle Réné, je cultive des légumes de saison avec des pratiques agricoles durables et respectueuses de l'environnement pour produire des légumes saint et bon.");
            set_suggestion();
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

    protected void set_img(int id, Bitmap img){
        ImageView img_view= findViewById(id);
        img_view.setImageBitmap(img);
    }

    protected void set_txt(int id, String str)throws IOException{
        TextView pricetxt = findViewById(id);
        pricetxt.setText(str);
    }

    protected void set_suggestion() throws IOException {
        // automation was too hard sorry
        String[][] data = new String[][]{
                {"https://img-3.journaldesfemmes.fr/D2DNQB0aIz4Bf_qiYbcl5xMNNa4=/1500x/smart/b9339668d757434c893f1a26bd114b3b/ccmcms-jdf/25934728.jpg", "patate_douce"},
                {"https://cdn-s-www.ledauphine.com/images/0D290058-E714-4F25-9F8F-BDEAAA15ABEB/NW_raw/ce-sont-les-glucides-qui-conferent-a-la-fraise-son-gout-delicatement-sucre-photo-by-natasha-skov-on-unsplash-1648565758.jpg", "friases"},
                {"https://img.passeportsante.net/1200x675/2021-05-03/i102200-vin.webp", "Vin Rouge"}
        };
        int counter = 1;
        int id = 0;
        String VS = "view_suggestion";
        String tmp = "";

        for(String[] S : data){
            tmp = VS + counter +"_IV";
            id = this.getResources().getIdentifier(tmp, "id", this.getPackageName());
            System.out.println("test:"+id);
            set_img_url(id, S[0]);
            tmp = VS + counter + "_TV";
            id = this.getResources().getIdentifier(tmp, "id", this.getPackageName());
            set_txt(id, S[1]);
            counter++;
        }
    }

}