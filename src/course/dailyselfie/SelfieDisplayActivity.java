package course.dailyselfie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

public class SelfieDisplayActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_selfie_display);

		
		Bundle extras = getIntent().getExtras();
		String imagePath = extras.getString(DailySelfieActivity.IMAGE_PATH_KEY);
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		
		ImageView selfieView = (ImageView) findViewById(R.id.big_selfie);
		
		// TODO Consider scaling the image to fill the screen
		selfieView.setImageBitmap(bitmap);
	}
}
