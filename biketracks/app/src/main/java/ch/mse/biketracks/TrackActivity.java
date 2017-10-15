package ch.mse.biketracks;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TrackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        //this set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //this is set custom image to back button
        final Drawable backArrow = getResources().getDrawable(R.drawable.back);
        backArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}